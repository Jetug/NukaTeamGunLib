package com.nukateam.ntgl.common.util.trackers;

import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.event.GunReloadEvent;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.holders.LoadingType;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageReload;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.WeakHashMap;

import static com.nukateam.ntgl.common.util.util.LivingEntityUtils.getInteractionHand;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ReloadTracker {
    private static final Map<LivingEntity, ReloadTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();

    private final int startTick;
    private final LivingEntity shooter;
    private int slot = 0;
    private final InteractionHand arm;
    private final ItemStack weapon;
    private final WeaponItem weaponItem;
    private final Gun gun;

    public int reloadTick;
    public boolean isStart = false;
    public boolean isEnd = false;

    private ReloadTracker(LivingEntity entity, InteractionHand arm) {
        this.startTick = entity.tickCount;
        this.arm = arm;
        this.weapon = entity.getItemInHand(arm);
        this.weaponItem = ((WeaponItem) weapon.getItem());
        this.gun = weaponItem.getModifiedGun(weapon);
        this.shooter = entity;

        if(entity instanceof Player player) {
            this.slot = arm == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        }

        var data = new GunData(weapon, entity);
        reloadTick = GunModifierHelper.getReloadTime(data);

        var loadingType = GunModifierHelper.getLoadingType(data);
        if(loadingType == LoadingType.PER_CARTRIDGE){
            ModSyncedDataKeys.RELOAD_START.setValue(entity, true);
            reloadTick = GunModifierHelper.getReloadStart(data);
            isStart = true;
        }

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
        if(arm == InteractionHand.MAIN_HAND)
            return !this.weapon.isEmpty() && entity.getMainHandItem() == this.weapon;
        else return !this.weapon.isEmpty() && entity.getOffhandItem() == this.weapon;
    }

    private boolean isWeaponFull() {
        var data = new GunData(weapon, shooter);
        return GunStateHelper.getAmmoCount(data) >= GunEnchantmentHelper.getAmmoCapacity(data);
    }

    private boolean hasNoAmmo(LivingEntity player) {
        return !InventoryUtil.hasAmmo(player, weapon);
    }

    private boolean canReload(Player player) {
        int deltaTicks = player.tickCount - this.startTick;
        int interval = GunEnchantmentHelper.getReloadInterval(this.weapon);
        return deltaTicks > 0 && deltaTicks % interval == 0;
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

    private static void handTick(LivingEntity entity) {
        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(entity)) {
            handTick(entity, InteractionHand.MAIN_HAND);
        }
        else if (ModSyncedDataKeys.RELOADING_LEFT.getValue(entity)) {
            handTick(entity, InteractionHand.OFF_HAND);
        }
        else if (RELOAD_TRACKER_MAP.containsKey(entity)) {
            RELOAD_TRACKER_MAP.remove(entity);
        }
    }

    private static void handTick(LivingEntity shooter, InteractionHand arm) {
        if (addTracker(shooter, arm)) return;
        var tracker = RELOAD_TRACKER_MAP.get(shooter);
        var data = new GunData(tracker.weapon, shooter);
        var loadingType = GunModifierHelper.getLoadingType(data);
        final var gun = tracker.gun;
        var isSameWeapon = !tracker.isSameWeapon(shooter);
        var isWeaponFull = tracker.isWeaponFull();
        var hasNoAmmo    = tracker.hasNoAmmo(shooter);

        if (isSameWeapon || (!tracker.isEnd && (isWeaponFull || hasNoAmmo))) {
            RELOAD_TRACKER_MAP.remove(shooter);
            var reloadKey = ModSyncedDataKeys.getReloadKey(arm);
            reloadKey.setValue(shooter, false);
        }
        else if(loadingType == LoadingType.MAGAZINE){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                tracker.reloadMagazine(shooter);
                stopReloading(data, gun, arm);
            }
        }
        else if(loadingType == LoadingType.PER_CARTRIDGE){
            if(tracker.reloadTick > 0)
                tracker.reloadTick--;

            if(tracker.reloadTick == 0){
                if(tracker.isStart){
                    resetTracker(tracker, data);
                    tracker.isStart = false;
                    ModSyncedDataKeys.RELOAD_START.setValue(shooter, false);
                }
                else{
                    tracker.addCartridge(shooter);
                    if (tracker.isWeaponFull() || tracker.hasNoAmmo(shooter)) {
                        if(tracker.isEnd) {
                            ModSyncedDataKeys.RELOAD_END.setValue(shooter, false);
                            stopReloading(data, gun, arm);
                        }
                        else {
                            tracker.isEnd = true;
                            ModSyncedDataKeys.RELOAD_END.setValue(shooter, true);
                            tracker.reloadTick = GunModifierHelper.getReloadEnd(data);
                        }
                    }
                    else resetTracker(tracker, data);
                }
            }
        }
    }

    private void reloadMagazine(LivingEntity entity) {
        var data = new GunData(weapon, entity);

        if(entity instanceof Player player && !player.isCreative()) {
            if(GunStateHelper.getProjectileConfig(data).isMagazineMode()){
                addMagazine(entity);
            }
            else{
                addAmmo(entity);
            }
        }
        else {
            GunStateHelper.setMaxAmmo(data);
        }
    }

    private void addCartridge(LivingEntity entity) {
        var gunData = new GunData(weapon, shooter);
        var reloadAmount = GunModifierHelper.getReloadAmount(gunData);

        if(entity instanceof Player player && !player.isCreative()) {
            addAmmo(entity, reloadAmount);
        }
        else {
            GunStateHelper.addAmmo(gunData, reloadAmount);
        }
    }

    private void addAmmo(LivingEntity entity) {
        var data = new GunData(weapon, entity);
        var amount = GunModifierHelper.getMaxAmmo(data);

        while (isNotReloaded(entity)){
            addAmmo(entity, amount);
        }
    }

    private void addAmmo(LivingEntity entity, int amount) {
        var context = InventoryUtil.findAmmo(entity, weapon);
        var ammo = context.stack();

        var data = new GunData(weapon, entity);
        var ammoHandler = GunStateHelper.getCurrentAmmo(data);

        if (!ammo.isEmpty()) {
            var tag = this.weapon.getTag();
            var value = ammoHandler.getValue(ammo);
            var currentAmount = GunStateHelper.getAmmoCount(data);

            amount = Math.min(ammo.getCount() * value, amount);

            if (tag != null) {
                var gunData = new GunData(weapon, shooter);
                var maxAmmo = GunEnchantmentHelper.getAmmoCapacity(gunData);
                amount = Math.min(amount, maxAmmo - tag.getInt(Tags.AMMO_COUNT));
                GunStateHelper.addAmmo(gunData, amount);
            }

            context.shrink(amount, ammoHandler, entity);
        }
    }

    private boolean isNotReloaded(LivingEntity entity) {
        var data = new GunData(weapon, entity);
        var tag = this.weapon.getTag();
        var hasAmmo = InventoryUtil.hasAmmo(entity, weapon);
        var ammoCount = GunStateHelper.getAmmoCount(data);
        var ammoCapacity = GunEnchantmentHelper.getAmmoCapacity(data);
        return hasAmmo && ammoCount < ammoCapacity;
    }

//    private boolean isNotReloaded(LivingEntity entity) {
//        var data = new GunData(weapon, entity);
//        var ammoItem = GunStateHelper.getAmmoId(data);
//        var tag = this.weapon.getTag();
//
//        return !InventoryUtil.findAmmo(entity, weapon).stack().isEmpty() &&
//                tag.getInt(Tags.AMMO_COUNT) < GunEnchantmentHelper.getAmmoCapacity(data);
//    }

    private void addMagazine(LivingEntity entity) {
        var data = new GunData(weapon, entity);
        var ammoHolder = GunStateHelper.getCurrentAmmo(data);
        var context = InventoryUtil.findMagazine(entity, weapon);
        var ammo = context.stack();

        if (!ammo.isEmpty()) {
            var amount = StackUtils.getDurability(ammo);
            var tag = this.weapon.getTag();
            amount = Math.min(GunModifierHelper.getMaxAmmo(data), amount);

            if (tag != null) {
                var maxAmmo = GunEnchantmentHelper.getAmmoCapacity(data);
                var currentAmmo = tag.getInt(Tags.AMMO_COUNT);

                if(currentAmmo > 0 && ammoHolder.canReturnAmmo()) {
                    var usedMagazine = new ItemStack(ForgeRegistries.ITEMS.getValue(ammoHolder.getId()));
                    StackUtils.setDurability(usedMagazine, currentAmmo);

                    if(entity instanceof Player player)
                        addOrDropStack(player, usedMagazine);
                }
                tag.putInt(Tags.AMMO_COUNT, amount);
            }
            context.shrink(1, ammoHolder, entity);
        }
    }

    private static void resetTracker(ReloadTracker tracker, GunData data) {
        tracker.reloadTick = GunModifierHelper.getReloadTime(data);
    }

    public static void startReloading(LivingEntity entity, InteractionHand arm){
        var reloadKey = ModSyncedDataKeys.getReloadKey(arm);
        reloadKey.setValue(entity, true);
        addTracker(entity, arm);
    }

//    private static SyncedDataKey<LivingEntity, Boolean> getReloadKey(HumanoidArm arm) {
//        getReloadKey()
//        var reloadKey = arm == InteractionHand.MAIN_HAND ?
//                ModSyncedDataKeys.RELOADING_RIGHT: ModSyncedDataKeys.RELOADING_LEFT;
//        return reloadKey;
//    }

    private static boolean addTracker(LivingEntity entity, InteractionHand arm) {
        var reloadKey = ModSyncedDataKeys.getReloadKey(arm);

        var gunItem = entity.getItemInHand(arm).getItem();

        if (!RELOAD_TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof WeaponItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            RELOAD_TRACKER_MAP.put(entity, new ReloadTracker(entity, arm));
        }
        return false;
    }

    private static void stopReloading(GunData data, Gun gun, InteractionHand hand) {
        var reloadKey = ModSyncedDataKeys.getReloadKey(hand);
        var entity = data.shooter;
        RELOAD_TRACKER_MAP.remove(entity);
        reloadKey.setValue(entity, false);
        final var finalPlayer = entity;
//        DelayedTask.runAfter(4, () -> gun.playCockSound(finalPlayer));

        var oppositeHand = LivingEntityUtils.getOppositeHand(hand);
        var oppositeStack = entity.getItemInHand(oppositeHand);

        if (hand == InteractionHand.MAIN_HAND
                && oppositeStack.getItem() instanceof WeaponItem
                && !GunModifierHelper.isWeaponFull(new GunData(oppositeStack, entity))) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) entity, new S2CMessageReload(true, oppositeHand));
        }

        MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post((ServerPlayer)entity, data.gun, hand));
    }
}
