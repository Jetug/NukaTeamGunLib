//package com.nukateam.gunscore.common.base.utils;
//
//import com.nukateam.gunscore.Config;
//import com.nukateam.gunscore.GunMod;
//import com.nukateam.gunscore.common.base.gun.Gun;
//import com.nukateam.gunscore.common.data.constants.Tags;
//import com.nukateam.gunscore.common.data.util.GunEnchantmentHelper;
//import com.nukateam.gunscore.common.data.util.GunModifierHelper;
//import com.nukateam.gunscore.common.data.util.LivingEntityUtils;
//import com.nukateam.gunscore.common.data.util.StackUtils;
//import com.nukateam.gunscore.common.foundation.init.ModSyncedDataKeys;
//import com.nukateam.gunscore.common.foundation.item.GunItem;
//import com.nukateam.gunscore.common.network.PacketHandler;
//import com.nukateam.gunscore.common.network.message.MessageGunSound;
//import com.nukateam.gunscore.common.network.message.S2CMessageReload;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.entity.HumanoidArm;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.event.entity.player.PlayerEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.network.PacketDistributor;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.Map;
//import java.util.WeakHashMap;
//
//import static com.nukateam.gunscore.common.data.util.LivingEntityUtils.getInteractionHand;
//
///**
// * Author: MrCrayfish
// */
//@SuppressWarnings("unused")
//@Mod.EventBusSubscriber(modid = GunMod.MOD_ID)
//public class ChargeTracker {
//    private static final Map<Player, ChargeTracker> RELOAD_TRACKER_MAP = new WeakHashMap<>();
//
//    private final int startTick;
//    private final int slot;
//    private final HumanoidArm arm;
//    private final ItemStack stack;
//    private final GunItem gunItem;
//    private final Gun gun;
//
//    public int reloadTick = 0;
//
//    private ChargeTracker(Player player, HumanoidArm arm) {
//        this.startTick = player.tickCount;
//        this.arm = arm;
//        this.slot = arm == HumanoidArm.RIGHT ?
//                player.getInventory().selected :
//                Inventory.SLOT_OFFHAND;
//        this.stack = player.getItemInHand(getInteractionHand(arm));
//        this.gunItem = ((GunItem) stack.getItem());
//        this.gun = gunItem.getModifiedGun(stack);
//
//        reloadTick = gun.getGeneral().getReloadTime();
//
////        playReloadSound(player);
//    }
//
//    @SubscribeEvent
//    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//        if (event.phase == TickEvent.Phase.START && !event.player.level.isClientSide) {
//            var player = event.player;
//
//            if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(player)) {
//                handTick(player, HumanoidArm.RIGHT);
//            }
//            else if (ModSyncedDataKeys.RELOADING_LEFT.getValue(player)) {
//                handTick(player, HumanoidArm.LEFT);
//            }
//            else if (RELOAD_TRACKER_MAP.containsKey(player)) {
//                RELOAD_TRACKER_MAP.remove(player);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event) {
//        MinecraftServer server = event.getPlayer().getServer();
//        if (server != null) {
//            server.execute(() -> RELOAD_TRACKER_MAP.remove(event.getPlayer()));
//        }
//    }
//
//    /**
//     * Tests if the current item the player is holding is the same as the one being reloaded
//     *
//     * @param player the player to check
//     * @return True if it's the same weapon and slot
//     */
//    private boolean isSameWeapon(Player player) {
//        if(arm == HumanoidArm.RIGHT)
//            return !this.stack.isEmpty() && player.getInventory().selected == this.slot && player.getInventory().getSelected() == this.stack;
//        else return !this.stack.isEmpty() && player.getOffhandItem() == this.stack;
//    }
//
//    private boolean isWeaponFull() {
//        CompoundTag tag = this.stack.getOrCreateTag();
//        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(this.stack, this.gun);
//    }
//
//    private boolean hasNoAmmo(Player player) {
//        return Gun.findAmmo(player, this.gun.getProjectile().getItem()).stack().isEmpty();
//    }
//
//    private boolean canReload(Player player) {
//        int deltaTicks = player.tickCount - this.startTick;
//        int interval = GunEnchantmentHelper.getReloadInterval(this.stack);
//        return deltaTicks > 0 && deltaTicks % interval == 0;
//    }
//
//    private void playReloadSound(Player player) {
//        var reloadSound = this.gun.getSounds().getReload();
//        if (reloadSound != null) {
//            var pos = player.position().add(0, 1, 0);
//            var radius = Config.SERVER.reloadMaxDistance.get();
//            var message = new MessageGunSound(reloadSound, SoundSource.PLAYERS, pos,
//                    1.0F, 1.0F, player.getId(), false, true);
//            PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() ->
//                    new PacketDistributor.TargetPoint(
//                            player.getX(), (player.getY() + 1.0), player.getZ(), radius, player.level.dimension())), message);
//        }
//    }
//
//    private static void handTick(Player player, HumanoidArm arm) {
//        var reloadKey = arm == HumanoidArm.RIGHT ?
//                ModSyncedDataKeys.RELOADING_RIGHT: ModSyncedDataKeys.RELOADING_LEFT;
//
//        if (addTracker(player, arm)) return;
//
//        var tracker = RELOAD_TRACKER_MAP.get(player);
//        final var gun = tracker.gun;
//
//        if (!tracker.isSameWeapon(player) || tracker.isWeaponFull() || tracker.hasNoAmmo(player)) {
//            RELOAD_TRACKER_MAP.remove(player);
//            reloadKey.setValue(player, false);
//            return;
//        }
//        else if(gun.getGeneral().getLoadingType().equals(Gun.General.MAGAZINE)){
//            if(tracker.reloadTick > 0)
//                tracker.reloadTick--;
//
//            if(tracker.reloadTick == 0){
//                tracker.reloadMagazine(player);
//                stopReloading(player, gun, arm);
//            }
//        }
//        else if(gun.getGeneral().getLoadingType().equals(Gun.General.PER_CARTRIDGE)){
//            if(tracker.reloadTick > 0)
//                tracker.reloadTick--;
//
//            if(tracker.reloadTick == 0){
//                tracker.addCartridge(player);
//                if (tracker.isWeaponFull() || tracker.hasNoAmmo(player))
//                    stopReloading(player, gun, arm);
//                else tracker.reloadTick = gun.getGeneral().getReloadTime();
//            }
//        }
//    }
//
//    private static boolean addTracker(Player player, HumanoidArm arm) {
//        var reloadKey = arm == HumanoidArm.RIGHT ?
//                ModSyncedDataKeys.RELOADING_RIGHT: ModSyncedDataKeys.RELOADING_LEFT;
//
//        var gunItem = arm == HumanoidArm.RIGHT ?
//                player.getInventory().getSelected().getItem():
//                player.getOffhandItem().getItem();
//
//        if (!RELOAD_TRACKER_MAP.containsKey(player)) {
//            if (!(gunItem instanceof GunItem)) {
//                reloadKey.setValue(player, false);
//                return true;
//            }
//            RELOAD_TRACKER_MAP.put(player, new ChargeTracker(player, arm));
//        }
//        return false;
//    }
//
//    private static void stopReloading(Player player, Gun gun, HumanoidArm arm) {
//        var reloadKey = arm == HumanoidArm.RIGHT ?
//                ModSyncedDataKeys.RELOADING_RIGHT: ModSyncedDataKeys.RELOADING_LEFT;
//
//        RELOAD_TRACKER_MAP.remove(player);
//        reloadKey.setValue(player, false);
//        final var finalPlayer = player;
//
////        DelayedTask.runAfter(4, () -> {
////            playCockSound(gun, finalPlayer);
////        });
//
//        var oppositeStack = LivingEntityUtils.getItemInHand(player, arm.getOpposite());
//        if (arm == HumanoidArm.RIGHT && oppositeStack.getItem() instanceof GunItem && !GunModifierHelper.isWeaponFull(oppositeStack)) {
//            PacketHandler.getPlayChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new S2CMessageReload(true, arm.getOpposite()));
//        }
//    }
//
//    private static void playCockSound(Gun gun, Player finalPlayer) {
//        var cockSound = gun.getSounds().getCock();
//        if (cockSound != null && finalPlayer.isAlive()) {
//            var radius = Config.SERVER.reloadMaxDistance.get();
//            var messageSound = new MessageGunSound(cockSound, SoundSource.PLAYERS, finalPlayer,
//                    1.0F, 1.0F, false, true);
//            PacketHandler.getPlayChannel().send(PacketDistributor.NEAR.with(() ->
//                    new PacketDistributor.TargetPoint(finalPlayer.getX(), finalPlayer.getY() + 1.0, finalPlayer.getZ(),
//                            radius, finalPlayer.level.dimension())), messageSound);
//        }
//    }
//}
