package com.nukateam.ntgl.common.base.utils;

import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.base.gun.LoadingTypes;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageReload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.WeakHashMap;

import static com.nukateam.ntgl.common.data.util.LivingEntityUtils.getInteractionHand;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ReloadTracker {
    private static final Map<LivingEntity, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private int slot = 0;
    private final HumanoidArm arm;
    private final ItemStack stack;
    private final GunItem gunItem;
    private final Gun gun;

    public int reloadTick = 0;

    private ReloadTracker(LivingEntity entity, HumanoidArm arm) {
        this.startTick = entity.tickCount;
        this.arm = arm;
        if(entity instanceof Player player) {
            this.slot = arm == HumanoidArm.RIGHT ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        }
        this.stack = entity.getItemInHand(getInteractionHand(arm));
        this.gunItem = ((GunItem) stack.getItem());
        this.gun = gunItem.getModifiedGun(stack);

        reloadTick = GunModifierHelper.getReloadTime(stack);

//        playReloadSound(entity);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
                var player = event.player;
                handTick(player);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START) {
                for (var entity: RELOAD_TRACKER_MAP.keySet()) {
                    if(entity instanceof Player) continue;
                    handTick(entity);
                }
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    private static void handTick(LivingEntity entity) {
        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(entity)) {
            handTick(entity, HumanoidArm.RIGHT);
        }
        else if (ModSyncedDataKeys.RELOADING_LEFT.getValue(entity)) {
            handTick(entity, HumanoidArm.LEFT);
        }
        else if (RELOAD_TRACKER_MAP.containsKey(entity)) {
            RELOAD_TRACKER_MAP.remove(entity);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server != null) {
            server.execute(() -> RELOAD_TRACKER_MAP.remove(event.getEntity()));
        }
    }

    /**
     * Tests if the current item the player is holding is the same as the one being reloaded
     *
     * @param entity the entity to check
     * @return True if it's the same weapon and slot
     */
    private boolean isSameWeapon(LivingEntity entity) {
        if(arm == HumanoidArm.RIGHT)
            return !this.stack.isEmpty() && entity.getMainHandItem() == this.stack;
        else return !this.stack.isEmpty() && entity.getOffhandItem() == this.stack;
    }

    private boolean isWeaponFull() {
        CompoundTag tag = this.stack.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(this.stack);
    }

    private boolean hasNoAmmo(LivingEntity player) {
        return Gun.findAmmo(player, stack).stack().isEmpty();
    }

    private boolean canReload(Player player) {
        int deltaTicks = player.tickCount - this.startTick;
        int interval = GunEnchantmentHelper.getReloadInterval(this.stack);
        return deltaTicks > 0 && deltaTicks % interval == 0;
    }

    private void reloadMagazine(LivingEntity player) {
        if(GunModifierHelper.getCurrentProjectile(stack).isMagazineMode()){
            addMagazine(player);
        }
        else{
//            var amount = this.gun.getGeneral().getMaxAmmo(stack);
            addAmmo(player);
        }
    }

    private void addCartridge(LivingEntity entity) {
        addAmmo(entity, gun.getGeneral().getReloadAmount());
    }

    private void addAmmo(LivingEntity entity) {
//        var amount = this.gun.getGeneral().getMaxAmmo(stack);
        var amount = GunModifierHelper.getMaxAmmo(stack);

        while (isNotReloaded(entity)){
            addAmmo(entity, amount);
        }
    }

    private void addAmmo(LivingEntity entity, int amount) {
        var context = Gun.findAmmo(entity, stack);
        var ammo = context.stack();

        if (!ammo.isEmpty()) {
            var tag = this.stack.getTag();
            amount = Math.min(ammo.getCount(), amount);

            if (tag != null) {
                int maxAmmo = GunEnchantmentHelper.getAmmoCapacity(this.stack);
                amount = Math.min(amount, maxAmmo - tag.getInt(Tags.AMMO_COUNT));
                tag.putInt(Tags.AMMO_COUNT, tag.getInt(Tags.AMMO_COUNT) + amount);
            }
            ammo.shrink(amount);

            // Trigger that the container changed
            var container = context.container();
            if (container != null) {
                container.setChanged();
            }
        }
//        playReloadSound(player);
    }

    private boolean isNotReloaded(LivingEntity player) {
        var ammoItem = GunModifierHelper.getAmmoItem(stack);
        var tag = this.stack.getTag();

        return !Gun.findAmmo(player, stack).stack().isEmpty() &&
                tag.getInt(Tags.AMMO_COUNT) < GunEnchantmentHelper.getAmmoCapacity(this.stack);
    }

    private void addMagazine(LivingEntity entity) {
        var ammoId = GunModifierHelper.getAmmoItem(stack);
        var context = Gun.findMagazine(entity, stack);
        var ammo = context.stack();

        if (!ammo.isEmpty()) {
            var amount = StackUtils.getDurability(ammo);
            var tag = this.stack.getTag();
//            amount = Math.min(this.gun.getGeneral().getMaxAmmo(stack), amount);
            amount = Math.min(GunModifierHelper.getMaxAmmo(stack), amount);

            if (tag != null) {
                var maxAmmo = GunEnchantmentHelper.getAmmoCapacity(this.stack);
                var currentAmmo = tag.getInt(Tags.AMMO_COUNT);

                if(currentAmmo > 0) {
                    var usedMagazine = new ItemStack(ForgeRegistries.ITEMS.getValue(ammoId));
                    StackUtils.setDurability(usedMagazine, currentAmmo);

                    if(entity instanceof Player player)
                        addOrDropStack(player, usedMagazine);
                }
//                amount = Math.min(amount, maxAmmo - currentAmmo);
                tag.putInt(Tags.AMMO_COUNT, amount);
            }

            ammo.shrink(1);

            // Trigger that the container changed
            var container = context.container();
            if (container != null) {
                container.setChanged();
            }
        }
//        playReloadSound(player);
    }

    private static void addOrDropStack(Player player, ItemStack usedMagazine) {
        if(!player.addItem(usedMagazine)){
            player.drop(usedMagazine, false);
        }
    }

//    private void playReloadSound(Player player) {
//        var reloadSound = this.gun.getSounds().getReload();
//        if (reloadSound != null) {
//            var pos = player.position().add(0, 1, 0);
//            var radius = Config.SERVER.reloadMaxDistance.get();
//            var message = new S2CMessageGunSound(reloadSound, SoundSource.PLAYERS, pos,
//                    1.0F, 1.0F, player.getId(), false, true);
//            PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() ->
//                    new PacketDistributor.TargetPoint(
//                            player.getX(), (player.getY() + 1.0), player.getZ(), radius, player.level.dimension())), message);
//        }
//    }

    private static void handTick(LivingEntity player, HumanoidArm arm) {
        if (addTracker(player, arm)) return;
        var tracker = RELOAD_TRACKER_MAP.get(player);
        final var gun = tracker.gun;

        if (!tracker.isSameWeapon(player) || tracker.isWeaponFull() || tracker.hasNoAmmo(player)) {
            RELOAD_TRACKER_MAP.remove(player);
            var reloadKey = getReloadKey(arm);
            reloadKey.setValue(player, false);
        }
        else if(gun.getGeneral().getLoadingType().equals(LoadingTypes.MAGAZINE)){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                tracker.reloadMagazine(player);
                stopReloading(player, gun, arm);
            }
        }
        else if(gun.getGeneral().getLoadingType().equals(LoadingTypes.PER_CARTRIDGE)){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                tracker.addCartridge(player);
                if (tracker.isWeaponFull() || tracker.hasNoAmmo(player))
                    stopReloading(player, gun, arm);
                else tracker.reloadTick = GunModifierHelper.getReloadTime(tracker.stack);
            }
        }
    }

    public static void startReloading(LivingEntity entity, HumanoidArm arm){
        var reloadKey = getReloadKey(arm);
        reloadKey.setValue(entity, true);
        addTracker(entity, arm);
    }

    private static SyncedDataKey<LivingEntity, Boolean> getReloadKey(HumanoidArm arm) {
        var reloadKey = arm == HumanoidArm.RIGHT ?
                ModSyncedDataKeys.RELOADING_RIGHT: ModSyncedDataKeys.RELOADING_LEFT;
        return reloadKey;
    }

    private static boolean addTracker(LivingEntity entity, HumanoidArm arm) {
        var reloadKey = getReloadKey(arm);

        var gunItem = arm == HumanoidArm.RIGHT ?
                entity.getMainHandItem().getItem():
                entity.getOffhandItem().getItem();

        if (!RELOAD_TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof GunItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            RELOAD_TRACKER_MAP.put(entity, new ReloadTracker(entity, arm));
        }
        return false;
    }

    private static void stopReloading(LivingEntity entity, Gun gun, HumanoidArm arm) {
        var reloadKey = getReloadKey(arm);

        RELOAD_TRACKER_MAP.remove(entity);
        reloadKey.setValue(entity, false);
//        final var finalPlayer = entity;
//        DelayedTask.runAfter(4, () -> {
//            playCockSound(gun, finalPlayer);
//        });

        var oppositeStack = LivingEntityUtils.getItemInHand(entity, arm.getOpposite());
        if (arm == HumanoidArm.RIGHT && oppositeStack.getItem() instanceof GunItem && !GunModifierHelper.isWeaponFull(oppositeStack)) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) entity, new S2CMessageReload(true, arm.getOpposite()));
        }
    }

//    private static void playCockSound(Gun gun, Player finalPlayer) {
//        var cockSound = gun.getSounds().getCock();
//        if (cockSound != null && finalPlayer.isAlive()) {
//            var radius = Config.SERVER.reloadMaxDistance.get();
//            var messageSound = new S2CMessageGunSound(cockSound, SoundSource.PLAYERS, finalPlayer,
//                    1.0F, 1.0F, false, true);
//            PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
//                            LevelLocation.create(finalPlayer.level,
//                                    finalPlayer.getX(),
//                                    finalPlayer.getY() + 1.0,
//                                    finalPlayer.getZ(), radius),
//                            messageSound);
//        }
//    }
}
