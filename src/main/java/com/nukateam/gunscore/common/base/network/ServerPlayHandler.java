package com.nukateam.gunscore.common.base.network;

import com.nukateam.gunscore.Config;
import com.nukateam.gunscore.common.base.gun.Gun;
import com.nukateam.gunscore.common.base.utils.ProjectileManager;
import com.nukateam.gunscore.common.base.utils.ShootTracker;
import com.nukateam.gunscore.common.base.utils.SpreadTracker;
import com.nukateam.gunscore.common.data.constants.Tags;
import com.nukateam.gunscore.common.foundation.container.AttachmentContainer;
import com.nukateam.gunscore.common.foundation.container.WorkbenchContainer;
import com.nukateam.gunscore.common.data.util.GunEnchantmentHelper;
import com.nukateam.gunscore.common.data.util.GunModifierHelper;
import com.nukateam.gunscore.common.event.GunFireEvent;
import com.nukateam.gunscore.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.gunscore.common.foundation.crafting.WorkbenchRecipe;
import com.nukateam.gunscore.common.foundation.crafting.WorkbenchRecipes;
import com.nukateam.gunscore.common.foundation.entity.ProjectileEntity;
import com.nukateam.gunscore.common.foundation.init.ModEnchantments;
import com.nukateam.gunscore.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.gunscore.common.foundation.item.GunItem;
import com.nukateam.gunscore.common.foundation.item.IColored;
import com.nukateam.gunscore.common.network.PacketHandler;
import com.nukateam.gunscore.common.network.message.MessageBulletTrail;
import com.nukateam.gunscore.common.network.message.MessageGunSound;
import com.nukateam.gunscore.common.network.message.MessageShoot;
import com.nukateam.gunscore.GunMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

import static com.nukateam.gunscore.client.data.handler.ShootingHandler.*;
//import static com.nukateam.guns.client.handler.ShootingHandler.gunCooldown;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler {
    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity -> entity.getSoundSource() == SoundSource.HOSTILE && !(entity instanceof NeutralMob) && !Config.COMMON.aggroMobs.exemptEntities.get().contains(entity.getType().getRegistryName().toString());

    /**
     * Fires the weapon the player is currently holding.
     * This is only intended for use on the logical server.
     *
     * @param entity the living entity for who's weapon to fire
     */
    public static void handleShoot(MessageShoot message, LivingEntity entity) {
        if (entity.isSpectator())
            return;

        if (entity.getUseItem().getItem() == Items.SHIELD)
            return;

        var world = entity.level;
        var hand = message.isMainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var heldItem = entity.getItemInHand(hand);

        if (heldItem.getItem() instanceof GunItem item
                && (Gun.hasAmmo(heldItem) || (entity instanceof Player player && player.isCreative()))) {
            var modifiedGun = item.getModifiedGun(heldItem);
            var tag =  heldItem.getOrCreateTag();

            if (modifiedGun != null) {
                if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(entity, heldItem)))
                    return;

                /* Updates the yaw and pitch with the clients current yaw and pitch */
                entity.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
                entity.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));

                ShootTracker tracker = ShootTracker.getShootTracker(entity);

                if (tracker.hasCooldown(item) && tracker.getRemaining(item) > Config.SERVER.cooldownThreshold.get()) {
                    GunMod.LOGGER.warn(entity.getName().getContents() + "(" + entity.getUUID() + ") tried to fire before cooldown finished or server is lagging? Remaining milliseconds: " + tracker.getRemaining(item));
                    return;
                }

                tracker.putCooldown(heldItem, item, modifiedGun);

                if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(entity)) {
                    ModSyncedDataKeys.RELOADING_RIGHT.setValue(entity, false);
                }

                if (!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) {
                    SpreadTracker.get(entity).update(entity, item);
                }

                int count = modifiedGun.getGeneral().getProjectileAmount();
                var projectileProps = modifiedGun.getProjectile();
                var spawnedProjectiles = new ProjectileEntity[count];

                for (int i = 0; i < count; i++) {
                    var factory = ProjectileManager.getInstance().getFactory(projectileProps);
                    var projectileEntity = factory.create(world, entity, heldItem, item, modifiedGun);
                    projectileEntity.setWeapon(heldItem);
                    projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                    world.addFreshEntity(projectileEntity);
                    spawnedProjectiles[i] = projectileEntity;
                    projectileEntity.tick();
                }
                if (!projectileProps.isVisible()) {
                    var data = GunEnchantmentHelper.getParticle(heldItem);
                    var messageBulletTrail = new MessageBulletTrail(spawnedProjectiles, projectileProps, entity.getId(), data);
                    PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), Config.COMMON.network.projectileTrackingRange.get(), entity.level.dimension())), messageBulletTrail);
                }

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(entity, heldItem));

                if (Config.COMMON.aggroMobs.enabled.get()) {
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.COMMON.aggroMobs.unsilencedRange.get());
                    double x = entity.getX();
                    double y = entity.getY() + 0.5;
                    double z = entity.getZ();
                    AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
                    radius *= radius;
                    double dx, dy, dz;
                    for (LivingEntity hostile : world.getEntitiesOfClass(LivingEntity.class, box, HOSTILE_ENTITIES)) {
                        dx = x - hostile.getX();
                        dy = y - hostile.getY();
                        dz = z - hostile.getZ();
                        if (dx * dx + dy * dy + dz * dz <= radius) {
                            hostile.setLastHurtByMob(Config.COMMON.aggroMobs.angerHostileMobs.get() ? hostile : hostile);
                        }
                    }
                }

                ResourceLocation fireSound = getFireSound(heldItem, modifiedGun);
                if (fireSound != null) {
                    double posX = entity.getX();
                    double posY = entity.getY() + entity.getEyeHeight();
                    double posZ = entity.getZ();
                    float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                    float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                    boolean muzzle = modifiedGun.getDisplay().getFlash() != null;
                    var messageSound = new MessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, entity.getId(), muzzle, false);
                    PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(posX, posY, posZ, radius, entity.level.dimension());
                    PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() -> targetPoint), messageSound);
                }

                if (!(entity instanceof Player player && player.isCreative())) {
                    tag = heldItem.getOrCreateTag();
                    if (!tag.getBoolean("IgnoreAmmo")) {
                        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RECLAIMED.get(), heldItem);
                        if (level == 0 || entity.level.random.nextInt(4 - Mth.clamp(level, 1, 2)) != 0) {
                            tag.putInt(Tags.AMMO_COUNT, Math.max(0, tag.getInt(Tags.AMMO_COUNT) - 1));
                        }
                    }
                }

                int rate = GunEnchantmentHelper.getRate(heldItem, modifiedGun);
                rate = GunModifierHelper.getModifiedRate(heldItem, rate);
                tag.putInt(COOLDOWN, rate);

                if(entity instanceof Player player)
                    player.awardStat(Stats.ITEM_USED.get(item));
            }
        } else {
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
        }
    }

    private static ResourceLocation getFireSound(ItemStack stack, Gun modifiedGun) {
        ResourceLocation fireSound = null;
        if (GunModifierHelper.isSilencedFire(stack)) {
            fireSound = modifiedGun.getSounds().getSilencedFire();
        } else if (stack.isEnchanted()) {
            fireSound = modifiedGun.getSounds().getEnchantedFire();
        }
        if (fireSound != null) {
            return fireSound;
        }
        return modifiedGun.getSounds().getFire();
    }

    /**
     * Crafts the specified item at the workstation the player is currently using.
     * This is only intended for use on the logical server.
     *
     * @param player the player who is crafting
     * @param id     the id of an item which is registered as a valid workstation recipe
     * @param pos    the block position of the workstation the player is using
     */
    public static void handleCraft(ServerPlayer player, ResourceLocation id, BlockPos pos) {
        Level world = player.level;

        if (player.containerMenu instanceof WorkbenchContainer workbench) {
            if (workbench.getPos().equals(pos)) {
                WorkbenchRecipe recipe = WorkbenchRecipes.getRecipeById(world, id);
                if (recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);

                WorkbenchBlockEntity workbenchBlockEntity = workbench.getWorkbench();

                /* Gets the color based on the dye */
                ItemStack stack = recipe.getItem();
                ItemStack dyeStack = workbenchBlockEntity.getInventory().get(0);
                if (dyeStack.getItem() instanceof DyeItem) {
                    DyeItem dyeItem = (DyeItem) dyeStack.getItem();
                    int color = dyeItem.getDyeColor().getTextColor();

                    if (IColored.isDyeable(stack)) {
                        IColored colored = (IColored) stack.getItem();
                        colored.setColor(stack, color);
                        workbenchBlockEntity.getInventory().set(0, ItemStack.EMPTY);
                    }
                }

                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }
    }

    /**
     * @param player
     */
    public static void handleUnload(ServerPlayer player) {
        unloadArm(player, player.getMainHandItem());
        unloadArm(player, player.getOffhandItem());
    }

    private static void unloadArm(ServerPlayer player, ItemStack stack) {
//        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                int count = tag.getInt("AmmoCount");
                tag.putInt("AmmoCount", 0);

                var gunItem = (GunItem) stack.getItem();
                var gun = gunItem.getModifiedGun(stack);
                var id = gun.getProjectile().getItem();
                var item = ForgeRegistries.ITEMS.getValue(id);

                if (item == null) {
                    return;
                }

                int maxStackSize = item.getMaxStackSize();
                int stacks = count / maxStackSize;

                for (int i = 0; i < stacks; i++) {
                    spawnAmmo(player, new ItemStack(item, maxStackSize));
                }

                int remaining = count % maxStackSize;
                if (remaining > 0) {
                    spawnAmmo(player, new ItemStack(item, remaining));
                }
            }
        }
    }

    /**
     * @param player
     * @param stack
     */
    private static void spawnAmmo(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (stack.getCount() > 0) {
            player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    /**
     * @param player
     */
    public static void handleAttachments(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            NetworkHooks.openGui(player, new SimpleMenuProvider((windowId, playerInventory, player1) ->
                    new AttachmentContainer(windowId, playerInventory, heldItem), new TranslatableComponent("container.gunscore.attachments")));
        }
    }
}
