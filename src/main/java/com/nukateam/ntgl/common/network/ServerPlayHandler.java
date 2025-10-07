package com.nukateam.ntgl.common.network;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.trackers.*;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.event.GunReloadEvent;
import com.nukateam.ntgl.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.container.WorkbenchContainer;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipe;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipes;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import com.nukateam.ntgl.common.network.message.C2SMessagePreFireSound;
import com.nukateam.ntgl.common.network.message.C2SMessageShoot;
import com.nukateam.ntgl.common.network.message.S2CMessageBulletTrail;
import com.nukateam.ntgl.common.network.message.*;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

import static com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys.getReloadKey;
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

        var reloadKey = message.isMainHand() ?
                ModSyncedDataKeys.RELOADING_RIGHT:
                ModSyncedDataKeys.RELOADING_LEFT;

        if(reloadKey.getValue(shooter)){
            return;
        }

        var heldItem = shooter.getItemInHand(hand);

        if (heldItem.getItem() instanceof WeaponItem weaponItem
                && (GunStateHelper.hasAmmo(heldItem) || (shooter instanceof Player player && player.isCreative()))) {
            var modifiedGun = weaponItem.getModifiedConfig(heldItem);
            var data = new GunData(heldItem, shooter);

            if (modifiedGun != null) {
                if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(shooter, heldItem, hand))) {
                    return;
                }

                /* Updates the yaw and pitch with the clients current yaw and pitch */
                shooter.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
                shooter.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));

                var tracker = ShootTracker.getShootTracker(shooter, hand);

                if (tracker.hasCooldown() && tracker.getRemaining() > Config.SERVER.cooldownThreshold.get()) {
                    Ntgl.LOGGER.warn(shooter.getName().getContents() +
                            "(" + shooter.getUUID() + ") tried to fire before cooldown finished or server is lagging? Remaining milliseconds: "
                            + tracker.getRemaining());
                    return;
                }

                tracker.putCooldown(heldItem, shooter);

                if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(shooter)) {
                    ModSyncedDataKeys.RELOADING_RIGHT.setValue(shooter, false);
                }

                if (ModSyncedDataKeys.RELOADING_LEFT.getValue(shooter)) {
                    ModSyncedDataKeys.RELOADING_LEFT.setValue(shooter, false);
                }

                var gunSpread = GunModifierHelper.getModifiedSpread(data);

                if (!GunModifierHelper.isAlwaysSpread(data) && gunSpread > 0.0F) {
                    SpreadTracker.get(shooter).update(shooter, weaponItem);
                }

                var fireMode = GunStateHelper.getFireMode(data);
                var multishotAmount = GunModifierHelper.getMultishotAmount(data);

                var count = GunModifierHelper.getProjectileAmount(data);

                if(fireMode == FireMode.MULTI && multishotAmount > 1){
                    var currentAmmo = GunStateHelper.getAmmoCount(data);
                    multishotAmount = Math.min(currentAmmo, multishotAmount);
                    count *= multishotAmount;
                }

                var projectileProps = GunStateHelper.getProjectileConfig(data);
                var spawnedProjectiles = new ProjectileEntity[count];

                for (int i = 0; i < count; i++) {
                    var factory = ProjectileManager.getInstance().getFactory(data);
                    var projectileEntity = factory.create(world, shooter, heldItem, weaponItem, modifiedGun);
                    projectileEntity.setWeapon(heldItem);
                    projectileEntity.setAdditionalDamage(GunStateHelper.getAdditionalDamage(heldItem));
                    world.addFreshEntity(projectileEntity);
                    spawnedProjectiles[i] = projectileEntity;
                    projectileEntity.tick();
                }
                if (!projectileProps.isVisible()) {
                    var spawnX = shooter.getX();
                    var spawnY = shooter.getY() + 1.0;
                    var spawnZ = shooter.getZ();
                    var radius = Config.COMMON.network.projectileTrackingRange.get();
                    var particleOptions = GunEnchantmentHelper.getParticle(heldItem);
                    var messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), particleOptions);

                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(shooter.level(), spawnX, spawnY, spawnZ, radius), messageBulletTrail);
                }


                if (Config.COMMON.aggroMobs.enabled.get()) {
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(data, Config.COMMON.aggroMobs.unsilencedRange.get());
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

                var fireSound = getFireSound(data, modifiedGun);

                if (fireSound != null) {
                    var posX = shooter.getX();
                    var posY = shooter.getY() + shooter.getEyeHeight();
                    var posZ = shooter.getZ();
                    var volume = GunModifierHelper.getFireSoundVolume(data);
                    var pitch = 0.9F + world.random.nextFloat() * 0.2F;
                    var radius = GunModifierHelper.getModifiedFireSoundRadius(data, Config.SERVER.gunShotMaxDistance.get());
                    var muzzle = modifiedGun.getDisplay().getFlash() != null;
                    var messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, shooter.getId(), muzzle, false);
                    PacketHandler
                            .getPlayChannel()
                            .sendToNearbyPlayers(() ->
                                    LevelLocation.create(shooter.level(), posX, posY, posZ, radius), messageSound);
                }

                if (!(shooter instanceof Player player && player.isCreative())) {
                    if (!GunStateHelper.isAmmoIgnored(heldItem)) {
                        GunStateHelper.consumeAmmo(data);
                    }
                }

                if (shooter instanceof Player player)
                    player.awardStat(Stats.ITEM_USED.get(weaponItem));

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(shooter, heldItem, hand));
            }
        } else {
            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
        }
    }

    public static void handlePreFireSound(C2SMessagePreFireSound message, ServerPlayer player) {
        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() instanceof WeaponItem item && (GunStateHelper.hasAmmo(heldItem) || player.isCreative())) {
            Gun modifiedGun = item.getModifiedConfig(heldItem);
            ResourceLocation fireSound = getPreFireSound(heldItem, modifiedGun);
            if (fireSound != null) {
                var posX = player.getX();
                var posY = player.getY() + player.getEyeHeight();
                var posZ = player.getZ();
                var data = new GunData(heldItem, player);
                var volume = GunModifierHelper.getFireSoundVolume(data);
                var pitch = 0.9F + world.random.nextFloat() * 0.2F;
                var radius = GunModifierHelper.getModifiedFireSoundRadius(data, Config.SERVER.gunShotMaxDistance.get());
                S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), false, false);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), posX, posY, posZ, radius), messageSound);
            }
        }
    }

    private static ResourceLocation getFireSound(GunData data, Gun modifiedGun) {
        ResourceLocation fireSound = null;
        if (GunModifierHelper.isSilencedFire(data)) {
            fireSound = modifiedGun.getSounds().getSilencedFire();
        } else if (data.gun.isEnchanted()) {
            fireSound = modifiedGun.getSounds().getEnchantedFire();
        }
        if (fireSound != null) {
            return fireSound;
        }
        return GunModifierHelper.getFireSound(data);
    }

    private static ResourceLocation getPreFireSound(ItemStack stack, Gun modifiedGun) {
        return modifiedGun.getSounds().getPreFire();
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

    public static void handleUnload(ServerPlayer player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof WeaponItem) {
            unloadGun(player, stack);
        }
    }

    public static void unloadGun(ServerPlayer player, ItemStack stack) {
        var data = new GunData(stack, player);
        if (GunStateHelper.getProjectileConfig(data).isMagazineMode())
            unloadMagazine(player, stack);
        else unloadAmmo(player, stack);
    }

    private static void unloadAmmo(ServerPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof WeaponItem) {
            var tag = stack.getTag();
            if (tag != null && tag.contains(Tags.AMMO_COUNT, Tag.TAG_INT)) {
                int count = tag.getInt(Tags.AMMO_COUNT);
                tag.putInt(Tags.AMMO_COUNT, 0);
                var data = new GunData(stack, player);
                var itemHolder = GunStateHelper.getCurrentAmmoWithoutCheck(data);

                if(itemHolder.canReturnAmmo()) {
                    var id = itemHolder.getId();
                    var item = ForgeRegistries.ITEMS.getValue(id);

                    if (item != null && !player.isCreative()) {
                        givePlayerAmmo(player, item, count);
                    }
                }
            }
        }
    }

    private static void unloadMagazine(ServerPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof WeaponItem) {
            var tag = stack.getTag();
            if (tag != null && tag.contains(Tags.AMMO_COUNT, Tag.TAG_INT)) {
                int count = tag.getInt(Tags.AMMO_COUNT);
                if (count == 0) return;

                tag.putInt(Tags.AMMO_COUNT, 0);
                var data = new GunData(stack, player);
                var ammoHolder = GunStateHelper.getCurrentAmmoWithoutCheck(data);

                if(ammoHolder.canReturnAmmo()) {
                    var item = ForgeRegistries.ITEMS.getValue(ammoHolder.getId());

                    if (item != null && !player.isCreative()) {
                        var usedMagazine = new ItemStack(item);
                        StackUtils.setDurability(usedMagazine, count);
                        spawnAmmo(player, usedMagazine);
                    }
                }
            }
        }
    }

    private static void givePlayerAmmo(ServerPlayer player, Item item, int count) {
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

    private static void spawnAmmo(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (stack.getCount() > 0) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    public static void handleAttachments(ServerPlayer player) {
        var heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof WeaponItem && ((WeaponItem)heldItem.getItem()).getModifiedConfig(heldItem).getModules().attachmentScreen()) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, player1) ->
                    new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.ntgl.attachments")));
        }
    }

    public static void handleReload(C2SMessageReload message, ServerPlayer player) {
        var dataKey = message.getHand() == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.RELOADING_RIGHT:
                ModSyncedDataKeys.RELOADING_LEFT;

        dataKey.setValue(player, message.isReload()); // This has to be set in order to verify the packet is sent if the event is cancelled
        if (!message.isReload())
            return;

        var gun = player.getItemInHand(message.getHand());

        if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, gun, message.getHand()))) {
            dataKey.setValue(player, false);
            return;
        }
    }

    public static void handleGrenade(C2SMessageGrenade message, ServerPlayer player) {
        var action = message.getAction();

        if(action == KeyAction.HOLD){
            ThrowingTracker.start(player, message.getHand());
        }
        else if(action == KeyAction.RELEASE){
            ThrowingTracker.onRelease(player, message.getHand());
        }
    }

    public static void handleHandAction(C2SMessageHandAction message, ServerPlayer player) {
        var stack = player.getItemInHand(message.getHand());

        if(stack.getItem() instanceof WeaponItem) {
            switch (message.getHandAction()) {
                case SWITCH_FIRE_MODE -> handleFireModeSwitch(player, stack);
                case SWITCH_AMMO -> handleAmmoSwitch(message.getHand(), player, stack);
            }
        }
        else if(stack.getItem() instanceof IThrowable){
            switch (message.getHandAction()) {
                case SWITCH_THROW_MODE -> handleThrowModeSwitch(player, stack, message.getHand());
            }
        }
    }

    public static void handleMeleeAttack(C2SMessageMeleeAttack message, ServerPlayer player) {
        var stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var gunData = new GunData(stack, player);
        if(stack.getItem() instanceof WeaponItem
                && GunModifierHelper.canMelee(gunData)
                && !EquipTracker.isEquiping(player,InteractionHand.MAIN_HAND)) {
            MeleeTracker.start(player, InteractionHand.MAIN_HAND);
//            ModSyncedDataKeys.MELEE_RIGHT.setValue(player, true);
        }
    }

    public static void handleFireModeSwitch(ServerPlayer player, ItemStack stack) {
        var data = new GunData(stack, player);
        GunStateHelper.switchFireMode(data);
        player.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
    }

    public static void handleThrowModeSwitch(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        var isNotPreparing = !ModSyncedDataKeys.getPreparingDataKey(hand).getValue(player);
        var isNotThrowing = !ModSyncedDataKeys.getThrowingDataKey(hand).getValue(player);

        if(isNotPreparing && isNotThrowing) {
            ThrowableStateHelper.switchThrowMode(stack);
            player.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
        }
    }

    public static void handleAmmoSwitch(InteractionHand hand, ServerPlayer player, ItemStack weapon) {
        var isReloading = getReloadKey(hand);
        var data = new GunData(weapon, player);

        if (!isReloading.getValue(player) && GunModifierHelper.getAmmoItems(data).size() > 1){
            handleUnload(player, hand);
            GunStateHelper.switchAmmo(data);
            reloadGun(hand, player);
            player.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
        }
    }

    public static void reloadGun(InteractionHand hand, ServerPlayer player) {
        handleReload(new C2SMessageReload(true, hand), player);
    }
}
