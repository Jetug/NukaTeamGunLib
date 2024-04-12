package com.nukateam.ntgl.common.base.network;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.base.utils.ProjectileManager;
import com.nukateam.ntgl.common.base.utils.ShootTracker;
import com.nukateam.ntgl.common.base.utils.SpreadTracker;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.data.util.StackUtils;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.container.WorkbenchContainer;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipe;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipes;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.init.ModEnchantments;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.IColored;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageShoot;
import com.nukateam.ntgl.common.network.message.S2CMessageBulletTrail;
import com.nukateam.ntgl.common.network.message.S2CMessageGunSound;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

import static com.nukateam.ntgl.client.data.handler.ShootingHandler.COOLDOWN;
//import static com.nukateam.guns.client.handler.ShootingHandler.gunCooldown;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler {
    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity -> entity.getSoundSource() == SoundSource.HOSTILE &&
            !(entity instanceof NeutralMob) && !Config.COMMON.aggroMobs.exemptEntities.get().contains(EntityType.getKey(entity.getType()).toString());


    /**
     * Fires the weapon the player is currently holding.
     * This is only intended for use on the logical server.
     *
     * @param shooter the living entity for whose weapon to fire
     */
    public static void handleShoot(C2SMessageShoot message, LivingEntity shooter) {
        if (shooter.isSpectator())
            return;

        if (shooter.getUseItem().getItem() == Items.SHIELD)
            return;

        var world = shooter.level();
        var hand = message.isMainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var heldItem = shooter.getItemInHand(hand);

        if (heldItem.getItem() instanceof GunItem item
                && (Gun.hasAmmo(heldItem) || (shooter instanceof Player player && player.isCreative()))) {
            var modifiedGun = item.getModifiedGun(heldItem);
            var tag = heldItem.getOrCreateTag();

            if (modifiedGun != null) {
                if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(shooter, heldItem)))
                    return;

                /* Updates the yaw and pitch with the clients current yaw and pitch */
                shooter.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
                shooter.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));

                ShootTracker tracker = ShootTracker.getShootTracker(shooter);

                if (tracker.hasCooldown(item) && tracker.getRemaining(item) > Config.SERVER.cooldownThreshold.get()) {
                    Ntgl.LOGGER.warn(shooter.getName().getContents() + "(" + shooter.getUUID() + ") tried to fire before cooldown finished or server is lagging? Remaining milliseconds: " + tracker.getRemaining(item));
                    return;
                }

                tracker.putCooldown(heldItem, item, modifiedGun);

                if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(shooter)) {
                    ModSyncedDataKeys.RELOADING_RIGHT.setValue(shooter, false);
                }

                if (!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) {
                    SpreadTracker.get(shooter).update(shooter, item);
                }

                int count = modifiedGun.getGeneral().getProjectileAmount();
                var projectileProps = modifiedGun.getProjectile();
                var spawnedProjectiles = new ProjectileEntity[count];

                for (int i = 0; i < count; i++) {
                    var factory = ProjectileManager.getInstance().getFactory(projectileProps.getItem());
                    var projectileEntity = factory.create(world, shooter, heldItem, item, modifiedGun);
                    projectileEntity.setWeapon(heldItem);
                    projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                    world.addFreshEntity(projectileEntity);
                    spawnedProjectiles[i] = projectileEntity;
                    projectileEntity.tick();
                }
                if (!projectileProps.isVisible()) {
                    var spawnX = shooter.getX();
                    var spawnY = shooter.getY() + 1.0;
                    var spawnZ = shooter.getZ();
                    var radius = Config.COMMON.network.projectileTrackingRange.get();
                    var data = GunEnchantmentHelper.getParticle(heldItem);
                    var messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data);

                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(shooter.level(), spawnX, spawnY, spawnZ, radius), messageBulletTrail);
                }

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(shooter, heldItem));

                if (Config.COMMON.aggroMobs.enabled.get()) {
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.COMMON.aggroMobs.unsilencedRange.get());
                    double x = shooter.getX();
                    double y = shooter.getY() + 0.5;
                    double z = shooter.getZ();
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
                    double posX = shooter.getX();
                    double posY = shooter.getY() + shooter.getEyeHeight();
                    double posZ = shooter.getZ();
                    float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                    float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                    boolean muzzle = modifiedGun.getDisplay().getFlash() != null;
                    var messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, shooter.getId(), muzzle, false);
                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(shooter.level(), posX, posY, posZ, radius), messageSound);
                }

                if (!(shooter instanceof Player player && player.isCreative())) {
                    tag = heldItem.getOrCreateTag();
                    if (!tag.getBoolean("IgnoreAmmo")) {
                        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RECLAIMED.get(), heldItem);
                        if (level == 0 || shooter.level().random.nextInt(4 - Mth.clamp(level, 1, 2)) != 0) {
                            tag.putInt(Tags.AMMO_COUNT, Math.max(0, tag.getInt(Tags.AMMO_COUNT) - 1));
                        }
                    }
                }

                int rate = GunEnchantmentHelper.getRate(heldItem, modifiedGun);
                rate = GunModifierHelper.getModifiedRate(heldItem, rate);
                tag.putInt(COOLDOWN, rate);

                if (shooter instanceof Player player)
                    player.awardStat(Stats.ITEM_USED.get(item));
            }
        } else {
            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
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
        Level world = player.level();

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
        var gunItem = (GunItem) stack.getItem();
        var gun = gunItem.getModifiedGun(stack);

        if (gun.getProjectile().isMagazineMode())
            unloadMagazine(player, stack);
        else
            unloadAmmo(player, stack);
    }

    private static void unloadAmmo(ServerPlayer player, ItemStack stack) {
//        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem) {
            var tag = stack.getTag();
            if (tag != null && tag.contains(Tags.AMMO_COUNT, Tag.TAG_INT)) {
                int count = tag.getInt(Tags.AMMO_COUNT);
                tag.putInt(Tags.AMMO_COUNT, 0);

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

    private static void unloadMagazine(ServerPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof GunItem) {
            var tag = stack.getTag();
            if (tag != null && tag.contains(Tags.AMMO_COUNT, Tag.TAG_INT)) {
                int count = tag.getInt(Tags.AMMO_COUNT);
                if (count == 0) return;

                tag.putInt(Tags.AMMO_COUNT, 0);

                var gunItem = (GunItem) stack.getItem();
                var gun = gunItem.getModifiedGun(stack);
                var id = gun.getProjectile().getItem();
                var item = ForgeRegistries.ITEMS.getValue(id);

                if (item == null) return;

                var usedMagazine = new ItemStack(item);
                StackUtils.setDurability(usedMagazine, count);
//                player.addItem(usedMagazine);
                spawnAmmo(player, usedMagazine);
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
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    /**
     * @param player
     */
    public static void handleAttachments(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, player1) ->
                    new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.ntgl.attachments")));
        }
    }
}
