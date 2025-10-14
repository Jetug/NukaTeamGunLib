package com.nukateam.ntgl.client.util.handler;

import com.ibm.icu.impl.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AttackMode;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.util.helpers.compatibility.PlayerReviveHelper;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.util.util.GunModifierHelper.*;
import static net.minecraftforge.event.TickEvent.Type.RENDER;

/**
 * Author: MrCrayfish
 */
public class ClientShootingHandler {
    private static ClientShootingHandler instance;
    public static float shootMsGap = 0F;
    private boolean shooting;

    private final HashMap<Pair<InteractionHand, LivingEntity>, Integer> entityShootGaps = new HashMap<>();
    private final Map<InteractionHand, ShootingData> shootingData = Map.of(
            InteractionHand.MAIN_HAND, new ShootingData(0, null),
            InteractionHand.OFF_HAND, new ShootingData(0, null)
    );

    private ClientShootingHandler() {}

    public static ClientShootingHandler get() {
        if (instance == null) {
            instance = new ClientShootingHandler();
        }
        return instance;
    }

    public static boolean isInGame() {
        var mc = Minecraft.getInstance();
        if (mc.getOverlay() != null)
            return false;
        if (mc.screen != null)
            return false;
        if (!mc.mouseHandler.isMouseGrabbed())
            return false;
        return mc.isWindowActive();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.MouseButton event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null)
            return;

        var isRightHand = event.getButton() == mc.options.keyAttack.getKey().getValue();
        var isLeftHand = event.getButton() == mc.options.keyUse.getKey().getValue();

        var heldItem = isRightHand ?
                player.getMainHandItem() :
                player.getOffhandItem();

        if (heldItem.getItem() instanceof IWeapon) {
            var data = new GunData(heldItem, player).setWeaponAction(AttackMode.PRIMARY);
            if (event.getAction() == GLFW.GLFW_PRESS) {
                if (isRightHand) {
                    setupShootingData(data, InteractionHand.MAIN_HAND);
                }
                if (isLeftHand) {
                    setupShootingData(data, InteractionHand.OFF_HAND);
                }
            } else if(event.getAction() == GLFW.GLFW_RELEASE) {
                if (isRightHand) {
                    resetShootingData(data, InteractionHand.MAIN_HAND);
                }
                if (isLeftHand) {
                    resetShootingData(data, InteractionHand.OFF_HAND);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled())
            return;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null || PlayerReviveHelper.isBleeding(player))
            return;

        if (event.isAttack()) {
            var heldItem = player.getMainHandItem();

            if (heldItem.getItem() instanceof IWeapon) {
                cancelSwing(event);
            }
        } else if (event.isUseItem()) {
            var mainHandItem = player.getMainHandItem();
            var offhandItem = player.getOffhandItem();

            if (offhandItem.getItem() instanceof IWeapon && canUseOffhandWeapon(player)) {
                cancelSwing(event);
                return;
            }

            if (mainHandItem.getItem() instanceof IWeapon) {
                if (event.getHand() == InteractionHand.OFF_HAND) {
                    // Allow shields to be used if weapon is one-handed
                    if (offhandItem.getItem() == Items.SHIELD) {
                        if (GunModifierHelper.getGripType(new GunData(mainHandItem, player)).isOneHanded()) {
                            return;
                        }
                    }
                    cancelSwing(event);
                    return;
                }
                if (AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock()) {
                    cancelSwing(event);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (!isInGame()) return;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null || PlayerReviveHelper.isBleeding(player)) return;

        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        if (mainHandItem.getItem() instanceof IWeapon && isKeyAttackDown()) {
            var data = new GunData(mainHandItem, player).setWeaponAction(AttackMode.PRIMARY);
            handleFireInput(data, InteractionHand.MAIN_HAND);
        }

        if (offhandItem.getItem() instanceof IWeapon && isUseKeyDown() && canUseOffhandWeapon(player)) {
            var data = new GunData(offhandItem, player).setWeaponAction(AttackMode.PRIMARY);
            handleFireInput(data, InteractionHand.OFF_HAND);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderTickLow(TickEvent.RenderTickEvent evt) {
        if (!evt.type.equals(RENDER) || evt.phase.equals(TickEvent.Phase.START))
            return;

        if (shootMsGap > 0F) {
            shootMsGap -= evt.renderTickTime * visualCooldownMultiplier();
        } else if (shootMsGap < -0.05F)
            shootMsGap = 0F;
    }

    @OnlyIn(Dist.CLIENT)
    private void cancelSwing(InputEvent.InteractionKeyMappingTriggered event) {
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHandleShooting(TickEvent.ClientTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            reduceGaps();

            if (!isInGame()) return;

            var player = Minecraft.getInstance().player;

//            if (player != null) {
//                var mainHandItem = player.getMainHandItem();
//                if (mainHandItem.getItem() instanceof IWeapon && (GunStateHelper.hasAmmo(mainHandItem) || player.isCreative())) {
//                    var shooting = isKeyAttackDown();
//                    if (Ntgl.controllableLoaded) {
//                        shooting |= ControllerHandler.isShooting();
//                    }
//                    if (shooting ^ this.shooting) {
//                        this.shooting = shooting;
//                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(shooting));
//                    }
//                } else if (this.shooting) {
//                    this.shooting = false;
//                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
//                }
//            }
//            else this.shooting = false;
        }
    }

    public ShootingData getShootingData(InteractionHand arm){
        return shootingData.get(arm);
    }

    public boolean isOnCooldown(LivingEntity entity, InteractionHand arm){
        return getCooldown(entity, arm) > 0;
    }

    public float getCooldownPercent(LivingEntity entity, InteractionHand hand) {
        var heldItem = entity.getItemInHand(hand);

        if (heldItem.getItem() instanceof IWeapon) {
            var data = new GunData(heldItem, entity);
            var rate = GunModifierHelper.getRate(data);
            var cooldown = getCooldown(entity, hand);
            return cooldown / (float)rate;
        }
        return 0;
    }

    public void setCooldown(LivingEntity entity, InteractionHand arm, int cooldown) {
        entityShootGaps.put(Pair.of(arm, entity), cooldown);
    }

    public int getCooldown(LivingEntity entity, InteractionHand arm) {
        return entityShootGaps.getOrDefault(Pair.of(arm, entity), 0);
    }

    public boolean isShooting(LivingEntity entity, InteractionHand arm){
        return getCooldown(entity, arm) > 0;
    }

    public static float calcShootTickGap(int rpm) {
        float shootTickGap = 60F / rpm * 20F;
        return shootTickGap;
    }

    public void fire(GunData data) {
        var shooter = data.shooter;
        var heldItem = data.gun;

        if (heldItem.getItem() instanceof IWeapon
                && (GunStateHelper.hasAmmo(heldItem) /*|| (shooter instanceof Player player && player.isCreative())*/)
                && isGunMode(data)
                && !shooter.isSpectator()) {
            var isMainHand = shooter.getMainHandItem() == heldItem;
            var hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            var shootGap = entityShootGaps.getOrDefault(Pair.of(hand, shooter), 0);

            if (shootGap <= 0) {
                if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(shooter, heldItem, hand)))
                    return;

                // CHECK HERE: Change this to test different rpm settings.
                // TODO: Test serverside, possible issues 0.3.4-alpha
                var gunData = new GunData(heldItem, shooter);
                final var rpm = GunModifierHelper.getRate(gunData); // Rounds per sec. Should come from gun properties in the end.
                shootGap += rpm;
                entityShootGaps.put(Pair.of(hand, shooter), shootGap);
                shootMsGap = calcShootTickGap(rpm);
                RecoilHandler.get().lastRandPitch = RecoilHandler.get().lastRandPitch;
                RecoilHandler.get().lastRandYaw = RecoilHandler.get().lastRandYaw;

                try {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(shooter.getId(), shooter.getViewYRot(1),
                            shooter.getViewXRot(1),
                            RecoilHandler.get().lastRandPitch, RecoilHandler.get().lastRandYaw, hand, data.weaponAction));
                } catch (NullPointerException e) {
                    Ntgl.LOGGER.error(e.getMessage(), e);
                }

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(shooter, heldItem, hand));
            }
        } else {
            return;
        }
    }

    private static boolean isKeyAttackDown() {
        return Minecraft.getInstance().options.keyAttack.isDown();
    }

    private static boolean isUseKeyDown() {
        return Minecraft.getInstance().options.keyUse.isDown();
    }

    private void reduceGaps(){
        entityShootGaps.forEach((key, val) -> {
            if(val > 0) val--;
            entityShootGaps.put(key, val);
        } );
    }

    private void setupShootingData(GunData gunData, InteractionHand arm) {
        assert gunData.gun != null;
        if(!GunStateHelper.hasAmmo(gunData.gun)) return;
        var data = shootingData.get(arm);

        data.fireTimer = GunModifierHelper.getFireDelay(gunData);
        data.gun = (IWeapon)gunData.gun.getItem();
    }

    private void resetShootingData(GunData gunData, InteractionHand arm) {
        var data = shootingData.get(arm);
        if(data.fireTimer != 0 && ! GunModifierHelper.needsFullCharge(gunData)){
            this.fire(gunData);
        }

        data.fireTimer = 0;
        data.gun = null;
    }

    private static boolean isGunMode(GunData gunData) {
        return GunModifierHelper.getWeaponMode(gunData) == WeaponMode.GUN;
    }

    private void handleFireInput(GunData gunData, InteractionHand arm) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var key = arm == InteractionHand.MAIN_HAND ? mc.options.keyAttack : mc.options.keyUse;
        var data = shootingData.get(arm);
        var fireMode =  GunStateHelper.getFireMode(gunData);
        var maxChargeTime = GunModifierHelper.getFireDelay(gunData);

        if (!isGunMode(gunData)) {
            return;
        }

        if (maxChargeTime != 0) {
            var isOnCooldown = ClientShootingHandler.get().isOnCooldown(player, arm);

            if (data.fireTimer > 0 && !isOnCooldown) {
                if (data.fireTimer == maxChargeTime - 2) {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
                }
                data.fireTimer--;
            } else {
                this.fire(gunData);
                if (data.fireTimer == 0 && !GunModifierHelper.isOneTimeCharge(gunData))
                    setupShootingData(gunData, arm);
                if (maxChargeTime > 0) {
                    if (fireMode != FireMode.AUTO)
                        key.setDown(false);
                }
            }
        } else {
            this.fire(gunData);
            if (fireMode != FireMode.AUTO) {
                key.setDown(false);
            }
        }
    }

    private static float visualCooldownMultiplier() {
        int fps = Minecraft.getInstance().getFps();
        if (fps < 11)
            return 8f;
        else if (fps < 21)
            return 6.25f;
        else if (fps < 31)
            return 1.25f;
        else if (fps < 61)
            return 0.95f;
        else if (fps < 121)
            return 0.625f;
        else if (fps < 181)
            return 0.425f;
        else if (fps < 201)
            return 0.35f;
        else
            return 0.25f;
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void renderTick(TickEvent.RenderTickEvent evt) {
//        // Upper is to handle rendering, bellow is handling animation calls and burst tracking
//
//        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.isAlive() || Minecraft.getInstance().player.getMainHandItem().getItem() instanceof IWeapon)
//            return;
//        GunAnimationController controller = GunAnimationController.fromItem(Minecraft.getInstance().player.getMainHandItem().getItem());
//        if (controller == null)
//            return;
//        else if (controller.isAnimationRunning() && (shootMsGap < 0F && this.burstTracker != 0)) {
//            if (controller.isAnimationRunning(GunAnimationController.AnimationLabel.PUMP) || controller.isAnimationRunning(GunAnimationController.AnimationLabel.PULL_BOLT))
//                return;
//            if (Config.CLIENT.controls.burstPress.get())
//                this.burstTracker = 0;
//            this.clickUp = true;
//        }
//    }

//    public int burstTracker = 0;
}
