package com.nukateam.ntgl.common.network;

import com.nukateam.ntgl.common.network.message.weapon.*;
import com.nukateam.ntgl.modules.network.LevelLocation;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.enums.SoundType;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.enums.KeyAction;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;

import com.nukateam.ntgl.common.util.trackers.*;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.event.GunReloadEvent;
import com.nukateam.ntgl.common.foundation.container.AttachmentContainer;
import com.nukateam.ntgl.common.foundation.container.WorkbenchContainer;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipes;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.core.BlockPos;
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
        if (shooter.isSpectator() || shooter.getUseItem().getItem() == Items.SHIELD)
            return;

        var level = shooter.level();
        var hand = message.getHand();
        var reloadKey = ModSyncedDataKeys.getReloadKey(hand);

        if (reloadKey.getValue(shooter)) {
            return;
        }

        var heldItem = shooter.getItemInHand(hand);
        var data = new WeaponData(heldItem, shooter).setWeaponMode(message.getMode());

        if (heldItem.getItem() instanceof IWeapon weaponItem
                && (WeaponStateHelper.hasAmmo(data) || (shooter instanceof Player player && player.isCreative()))) {
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

            tracker.putCooldown(data);

            if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(shooter)) {
                ModSyncedDataKeys.RELOADING_RIGHT.setValue(shooter, false);
            }

            if (ModSyncedDataKeys.RELOADING_LEFT.getValue(shooter)) {
                ModSyncedDataKeys.RELOADING_LEFT.setValue(shooter, false);
            }

            var gunSpread = WeaponModifierHelper.getSpread(data);

            if (!WeaponModifierHelper.isAlwaysSpread(data) && gunSpread > 0.0F) {
                SpreadTracker.get(shooter).update(shooter, weaponItem);
            }

            spawnProjectiles(data, level);
            aggroMobs(data, level);
            playFireSound(data, level);

            if (!(shooter instanceof Player player && player.isCreative())) {
                if (!WeaponStateHelper.isAmmoIgnored(heldItem)) {
                    WeaponStateHelper.consumeAmmo(data);
                }
            }

            if (shooter instanceof Player player)
                player.awardStat(Stats.ITEM_USED.get(heldItem.getItem()));

            MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(shooter, heldItem, hand));
        } else {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
        }
    }

    private static void spawnProjectiles(WeaponData data, Level level) {
        var fireMode = WeaponStateHelper.getFireMode(data);
        var multishotAmount = WeaponModifierHelper.getMultishotAmount(data);
        var count = WeaponModifierHelper.getProjectileAmount(data);

        if (fireMode == FireMode.MULTI && multishotAmount > 1) {
            var currentAmmo = WeaponStateHelper.getAmmoCount(data);
            multishotAmount = Math.min(currentAmmo, multishotAmount);
            count *= multishotAmount;
        }

        for (int i = 0; i < count; i++) {
            spawnProjectile(data, level);
        }
    }

    private static void aggroMobs(WeaponData data, Level level) {
        var shooter = data.wielder;
        var silenced = WeaponModifierHelper.isSilencedFire(data);
        if (!silenced && Config.COMMON.aggroMobs.enabled.get() && Config.COMMON.aggroMobs.angerHostileMobs.get()) {
            var radius = WeaponModifierHelper.getModifiedFireSoundRadius(data, Config.COMMON.aggroMobs.unsilencedRange.get());
            var x = shooter.getX();
            var y = shooter.getY() + 0.5;
            var z = shooter.getZ();
            var box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            radius *= radius;
            double dx, dy, dz;

            for (var hostile : level.getEntitiesOfClass(LivingEntity.class, box, HOSTILE_ENTITIES)) {
                dx = x - hostile.getX();
                dy = y - hostile.getY();
                dz = z - hostile.getZ();
                if (dx * dx + dy * dy + dz * dz <= radius) {
                    hostile.setLastHurtByMob(shooter);
                }
            }
        }
    }

    private static void playFireSound(WeaponData data, Level level) {
        var shooter = data.wielder;
        var fireSound = getFireSound(data);

        if (fireSound != null) {
            var posX = shooter.getX();
            var posY = shooter.getY() + shooter.getEyeHeight();
            var posZ = shooter.getZ();
            var volume = WeaponModifierHelper.getFireSoundVolume(data);
            var pitch = 0.9F + level.random.nextFloat() * 0.2F;
            var radius = WeaponModifierHelper.getModifiedFireSoundRadius(data, Config.SERVER.gunShotMaxDistance.get());
            var messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, shooter.getId(), false);

            PacketHandler.getPlayChannel()
                    .sendToNearbyPlayers(() ->
                            LevelLocation.create(shooter.level(), posX, posY, posZ, radius), messageSound);
        }
    }

    private static void spawnProjectile(WeaponData data, Level world) {
        var factory = ProjectileManager.getInstance().getFactory(data);
        var projectileEntity = factory.create(world, data);
        projectileEntity.setWeapon(data.weapon);
        world.addFreshEntity(projectileEntity);
        projectileEntity.tick();
    }

    public static void handlePreFireSound(C2SMessagePreFireSound message, ServerPlayer player) {
        var world = player.level();
        var heldItem = player.getItemInHand(message.getHand());
        var data = new WeaponData(heldItem, player);

        if (heldItem.getItem() instanceof IWeapon item && (WeaponStateHelper.hasAmmo(data) || player.isCreative())) {
            var fireSound = WeaponModifierHelper.getSound(SoundType.PRE_FIRE.getName(), data);

            if (fireSound != null) {
                var posX = player.getX();
                var posY = player.getY() + player.getEyeHeight();
                var posZ = player.getZ();
                var volume = WeaponModifierHelper.getFireSoundVolume(data);
                var pitch = 0.9F + world.random.nextFloat() * 0.2F;
                var radius = WeaponModifierHelper.getModifiedFireSoundRadius(data, Config.SERVER.gunShotMaxDistance.get());
                var messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), false);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), posX, posY, posZ, radius), messageSound);
            }
        }
    }

    private static ResourceLocation getFireSound(WeaponData data) {
        ResourceLocation fireSound = null;
        if (WeaponModifierHelper.isSilencedFire(data)) {
            fireSound = WeaponModifierHelper.getSound(SoundType.SILENCED_FIRE.getName(), data);
        } else if (data.weapon.isEnchanted()) {
            fireSound = WeaponModifierHelper.getSound(SoundType.ENCHANTED_FIRE.getName(), data);
        }
        if (fireSound != null) {
            return fireSound;
        }
        return WeaponModifierHelper.getSound(SoundType.FIRE.getName(), data);
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
                var recipe = WorkbenchRecipes.getRecipeById(world, id);
                if (recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);
                Containers.dropItemStack(world,
                        pos.getX() + 0.5,
                        pos.getY() + 1.125,
                        pos.getZ() + 0.5,
                        recipe.getItem());
            }
        }
    }

    public static void handleUnload(ServerPlayer player, C2SMessageUnload message) {
        var stack = player.getItemInHand(message.getHand());
        if (stack.getItem() instanceof IWeapon) {
            unloadGun(new WeaponData(stack, player).setWeaponMode(message.getWeaponMode()));
        }
    }

    public static void unloadGun(WeaponData data) {
        if (WeaponStateHelper.getProjectileConfig(data).isMagazineMode())
            unloadMagazine(data);
        else unloadAmmo(data);
    }

    private static void unloadAmmo(WeaponData data) {
        if (data.weapon.getItem() instanceof IWeapon) {
            var count = WeaponStateHelper.getAmmoCount(data);
            var itemHolder = WeaponStateHelper.getCurrentAmmoWithoutCheck(data);

            WeaponStateHelper.setAmmoCount(data, 0);

            if (itemHolder.canReturnAmmo()) {
                var id = itemHolder.getId();
                var item = ForgeRegistries.ITEMS.getValue(id);

                if (item != null && data.wielder instanceof Player player && !player.isCreative()) {
                    givePlayerAmmo(player, item, count);
                }
            }
        }
    }

    private static void unloadMagazine(WeaponData data) {
        if (data.weapon.getItem() instanceof IWeapon) {
            var count = WeaponStateHelper.getAmmoCount(data);
            if (count == 0) return;
            WeaponStateHelper.setAmmoCount(data, 0);

            var ammoHolder = WeaponStateHelper.getCurrentAmmoWithoutCheck(data);

            if (ammoHolder.canReturnAmmo()) {
                var item = ForgeRegistries.ITEMS.getValue(ammoHolder.getId());

                if (item != null && data.wielder instanceof Player player && !player.isCreative()) {
                    var usedMagazine = new ItemStack(item);
                    StackUtils.setDurability(usedMagazine, count);
                    spawnAmmo(player, usedMagazine);
                }
            }
        }
    }

    private static void givePlayerAmmo(Player player, Item item, int count) {
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

    private static void spawnAmmo(Player player, ItemStack stack) {
        player.getInventory().add(stack);
        if (stack.getCount() > 0) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    public static void handleAttachments(ServerPlayer player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof IWeapon && ((IWeapon) heldItem.getItem()).getModifiedConfig(heldItem).getModules().attachmentScreen()) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, player1) ->
                    new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.ntgl.attachments")));
        }
    }

    public static void handleReload(C2SMessageReload message, ServerPlayer player) {
        var hand = message.getHand();
        var weapon = player.getItemInHand(hand);
        var data = new WeaponData(weapon, player).setWeaponMode(message.getWeaponMode());

        if (!MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(data, hand))) {
            ReloadTracker.startReloading(data, hand);
        }
    }

    public static void handleStopReload(C2SMessageReloadStop message, ServerPlayer player) {
        var dataKey = ModSyncedDataKeys.getReloadKey(message.getHand());
        dataKey.setValue(player, false);

        ReloadTracker.stopReloading(player, message.getHand());
    }

    public static void handleGrenade(C2SMessageGrenade message, ServerPlayer player) {
        var action = message.getAction();

        var weapon = player.getItemInHand(message.getHand());
        var weaponData = new WeaponData(weapon, player).setWeaponMode(message.getAttackMode());

        if (action == KeyAction.HOLD) {
            ThrowingTracker.start(weaponData, message.getHand());
        } else if (action == KeyAction.RELEASE) {
            ThrowingTracker.onRelease(weaponData, message.getHand());
        }
    }

    public static void handleHandAction(C2SMessageHandAction message, ServerPlayer player) {
        var stack = player.getItemInHand(message.getHand());

        if (stack.getItem() instanceof IWeapon) {
            switch (message.getHandAction()) {
                case SWITCH_FIRE_MODE -> handleFireModeSwitch(player, stack, message.getHand());
                case SWITCH_AMMO -> handleAmmoSwitch(message.getHand(), player, stack);
            }
        }
    }

    public static void handleAmmoChange(C2SMessageChangeAmmo message, ServerPlayer player) {
        var hand = message.getHand();
        var weapon = player.getItemInHand(hand);
        var isReloading = getReloadKey(hand);
        var data = new WeaponData(weapon, player).setWeaponMode(message.getWeaponMode());
        var notReloading = !isReloading.getValue(player);
        var canSwitch = WeaponModifierHelper.getAmmoItems(data).size() > 1;
        var ammoItems = WeaponModifierHelper.getAmmoItems(data);
        var ammoHolder = new AmmoHolder(message.getAmmo());
//        var ammoAllowed = ammoItems.contains(ammoHolder);

        var ammoAllowed = ammoItems.stream().filter((i) -> i.getId().equals(message.getAmmo())).count() > 0;

        if (notReloading && canSwitch && ammoAllowed) {
            unloadGun(data);
            WeaponStateHelper.switchAmmo(data);

            WeaponStateHelper.setCurrentAmmo(data, message.getAmmo());

            handleReload(new C2SMessageReload(hand, data.weaponMode), player);
            player.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
        }
    }

    public static void handleMeleeAttack(C2SMessageMeleeAttack message, ServerPlayer player) {
        var stack = player.getItemInHand(message.getHand());
        var gunData = new WeaponData(stack, player).setWeaponMode(message.getAction());
        if (stack.getItem() instanceof IWeapon
                && WeaponModifierHelper.canMelee(gunData)
                && !EquipTracker.isEquiping(player, message.getHand())) {
            var heldItem = player.getItemInHand(message.getHand());

            MeleeTracker.start(new WeaponData(heldItem, player).setWeaponMode(message.getAction()), message.getHand());
        }
    }

    public static void handleFireModeSwitch(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (WeaponModifierHelper.getWeaponAction(new WeaponData(stack, player)) == WeaponAction.THROW) {
            var data = new WeaponData(stack, player).setWeaponMode(WeaponMode.PRIMARY);
            handleThrowModeSwitch(data, hand);
        } else {
            var data = new WeaponData(stack, player);
            WeaponStateHelper.switchFireMode(data);
            player.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
        }
    }

    public static void handleThrowModeSwitch(WeaponData data, InteractionHand hand) {
        var isNotPreparing = !ModSyncedDataKeys.getPreparingDataKey(hand).getValue(data.wielder);
        var isNotThrowing = !ModSyncedDataKeys.getThrowingDataKey(hand).getValue(data.wielder);

        if (isNotPreparing && isNotThrowing) {
            ThrowableStateHelper.switchThrowMode(data);
            data.wielder.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
        }
    }

    public static void handleAmmoSwitch(InteractionHand hand, ServerPlayer player, ItemStack weapon) {
        var isReloading = getReloadKey(hand);
        var data = new WeaponData(weapon, player);

        if (!isReloading.getValue(player) && WeaponModifierHelper.getAmmoItems(data).size() > 1) {
            unloadGun(data);
            WeaponStateHelper.switchAmmo(data);
            handleReload(new C2SMessageReload(hand, data.weaponMode), player);
            player.playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
        }
    }

    public static void reloadGun(InteractionHand hand, WeaponData data) {
        handleReload(new C2SMessageReload(hand, data.weaponMode), (ServerPlayer) data.wielder);
    }
}
