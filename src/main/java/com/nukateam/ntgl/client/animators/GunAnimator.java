package com.nukateam.ntgl.client.animators;

import com.nukateam.geo.render.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.*;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.client.model.gun.*;
import com.nukateam.ntgl.client.render.renderers.weapon.*;
import com.nukateam.ntgl.client.util.util.TransformUtils;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Animations;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import mod.azure.azurelib.core.animation.*;
import mod.azure.azurelib.core.animation.AnimationController.*;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.keyframe.event.SoundKeyframeEvent;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.client.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;

import static com.nukateam.ntgl.client.util.util.TransformUtils.*;
import static com.nukateam.ntgl.common.data.constants.Animations.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.*;
import static mod.azure.azurelib.core.animation.Animation.*;
import static mod.azure.azurelib.core.animation.Animation.LoopType.*;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;

@OnlyIn(Dist.CLIENT)
public class GunAnimator extends ItemAnimator implements IConfigProvider<Gun> {
    protected final DynamicGunRenderer<GunAnimator> renderer;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ShootingHandler shootingHandler = ShootingHandler.get();
    protected final ClientReloadHandler reloadHandler = ClientReloadHandler.get();
    protected final AnimationHelper<GunAnimator> animationHelper;

    protected final AnimationController<GunAnimator> TRIGGER_CONTROLLER;
    protected final AnimationController<GunAnimator> MAIN_CONTROLLER;
    protected final AnimationController<GunAnimator> REVOLVER_CONTROLLER;
    protected final AnimationController<GunAnimator> BARREL_CONTROLLER;
    protected final InteractionHand arm;

    protected Cycler barrelCycler = new Cycler(1, getBarrelAmount());
    protected Cycler chamberCycler = null;

    protected WeaponItem currentGun = null;
    protected int rate;
    protected int equipTime;
    protected int meleeDelay;
    protected int meleeCooldown;
    protected int fireDelay;
    protected int reloadTime;
    protected int reloadStartTime;
    protected int reloadEndTime;
    protected boolean isEquiping;

    public GunAnimator(ItemDisplayContext transformType, DynamicGunRenderer<GunAnimator> renderer) {
        super(transformType);
        this.renderer = renderer;
        this.arm = getArm();

        ClientTickHandler.addTicker(this, this::tick);
        TRIGGER_CONTROLLER = createController("triggerController", event -> PlayState.CONTINUE);
        MAIN_CONTROLLER = createController("mainController", animate())
                .setSoundKeyframeHandler(this::handleSoundEvent)
                .triggerableAnim(EQUIP, begin().then(EQUIP, LOOP));
        REVOLVER_CONTROLLER = createController("revolverController", animateRevolver());
        BARREL_CONTROLLER = createController("barrelController", animateBarrels());
        animationHelper = new AnimationHelper<>(this, GeoGunModel.INSTANCE);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(MAIN_CONTROLLER);
        controllerRegistrar.add(TRIGGER_CONTROLLER);
        controllerRegistrar.add(REVOLVER_CONTROLLER);
        controllerRegistrar.add(BARREL_CONTROLLER);
    }

    @Override
    public Gun getConfig() {
        if (getStack().getItem() instanceof IConfigProvider config) {
            if (config.getConfig() instanceof Gun gun)
                return gun;
        }

        return new Gun();
    }

    public void tick(TickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickStart();
        } else {
            tickEnd();
        }
    }

    protected void tickStart() {
        if (!(getStack().getItem() instanceof WeaponItem))
            return;
        var data = getGunData();
        this.rate = GunModifierHelper.getRate(data);
        this.equipTime = GunModifierHelper.getEquipTime(data);
        this.isEquiping = EquipTracker.isEquiping(getEntity(), getArm());
        this.meleeDelay = GunModifierHelper.getMeleeDelay(data);
        this.meleeCooldown = GunModifierHelper.getMeleeCooldown(data);
        this.fireDelay = GunModifierHelper.getFireDelay(data);
        this.reloadTime = GunModifierHelper.getReloadTime(data);
        this.reloadStartTime = GunModifierHelper.getReloadStart(data);
        this.reloadEndTime = GunModifierHelper.getReloadEnd(data);
//        Ntgl.LOGGER.info("! Is equiping: " + isEquiping);

        if(isEquiping) {
            Ntgl.LOGGER.info("! Equip time: " + equipTime);
        }
        setupCycledAnimations();
    }

    protected void tickEnd() {}

    protected int getBarrelAmount() {
        return 1;
    }

    protected LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    protected WeaponItem getGunItem() {
        return (WeaponItem) getStack().getItem();
    }

    protected @NotNull GunData getGunData() {
        return new GunData(getStack(), getEntity());
    }

    protected boolean isOneHanded(ItemStack stack) {
        return stack.getItem() instanceof WeaponItem && GunModifierHelper.getGripType(getGunData()).isOneHanded();
    }

    @NotNull
    protected AnimationController<GunAnimator> createController(String name, AnimationStateHandler<GunAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
    }

    protected InteractionHand getArm() {
        return isRightHand(transformType) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    protected AnimationStateHandler<GunAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var shooter = getEntity();
                var holdAnimation = getHoldAnimation(event);

                if (!isHandTransform(transformType))
                    return event.setAndContinue(holdAnimation);

                var isShooting = shootingHandler.isShooting(shooter, arm);
                var data = shootingHandler.getShootingData(arm);
                var animation = begin();

                if(equipTime > 0 && isEquiping) {
                    animation = getEquipAnimation(event);
                }
                else if(ClientMeleeHandler.isOnDelay(shooter, arm)){
                    animation = getMeleeDelayAnimation(event);
                }
                else if(ClientMeleeHandler.isOnCooldown(shooter, arm)){
                    animation = getMeleeCooldownAnimation(event);
                }
                else if (fireDelay > 0 && data.fireTimer > 0 && fireDelay != data.fireTimer) {
                    animation = getChargingAnimation(event, data);
                }
                else if (reloadHandler.isReloading(shooter, arm)) {
                    animation = getReloadingAnimation(event);
                }
                else if (isShooting) {
                    animation = getShootingAnimation(event);
                }
                else if (reloadHandler.isReloading(shooter, PlayerHelper.getOpposite(arm))) {
                    animation = getHideAnimation(event);
                }
                else if (ClientHandler.getInspectionTicks(getArm()) > 0) {
                    animation = getInspectionAnimation(event);
                }
                else {
                    if (currentGun == getGunItem())
                        animation = holdAnimation;
                    else {
                        currentGun = getGunItem();
                        animation = playGunAnim(SHOT, LOOP);
                    }
                }

                return animation != null ? event.setAndContinue(animation): PlayState.STOP;
            } catch (Exception e) {
                return PlayState.STOP;
            }
        };
    }

    protected AnimationStateHandler<GunAnimator> animateRevolver() {
        return (event) -> getCycledAnimation(event, Animations.CHAMBER, this.chamberCycler);
    }

    protected AnimationStateHandler<GunAnimator> animateBarrels() {
        return (event) -> getCycledAnimation(event, Animations.BARREL, this.barrelCycler);
    }

    protected PlayState getCycledAnimation(AnimationState<GunAnimator> event, String animationName, Cycler cycler) {
        event.getController().setAnimationSpeed(1.0);

        if (TransformUtils.isHandTransform(this.transformType) && cycler != null) {
            var entity = this.getEntity();
            var isShooting = shootingHandler.isShooting(entity, TransformUtils.getHand(this.transformType));
            var finalAnim = animationName + cycler.getCurrent();

            RawAnimation animation = null;
            if (isShooting && this.animationHelper.hasAnimation(finalAnim)) {
                animation = RawAnimation.begin().then(finalAnim, LoopType.HOLD_ON_LAST_FRAME);
                this.animationHelper.syncAnimation(event, finalAnim, rate);
            }

            return event.setAndContinue(animation);
        }
        return PlayState.STOP;
    }

    protected RawAnimation getHoldAnimation(AnimationState<GunAnimator> event) {
//        if(isFirstPerson(transformType))
            return playGunAnim(HOLD, LOOP);
//        else return null;
    }

    protected RawAnimation getHideAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType))
            return begin().then(Animations.HIDE, HOLD_ON_LAST_FRAME);
        else return getHoldAnimation(event);
    }

    protected RawAnimation getInspectionAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType)) {
            RawAnimation animation;
            animation = playGunAnim(Animations.INSPECT, PLAY_ONCE);
            animationHelper.syncAnimation(event, Animations.INSPECT, ClientHandler.getMaxInspectionTicks());
            return animation;
        }
        else return getHoldAnimation(event);
    }

    protected RawAnimation getChargingAnimation(AnimationState<GunAnimator> event, ShootingData shootingData) {
        if(isFirstPerson(transformType)) {

            var animation = begin();
            if (animationHelper.hasAnimation(Animations.CHARGE)) {
                BARREL_CONTROLLER.stop();
                BARREL_CONTROLLER.setAnimation(begin().then("void", PLAY_ONCE));
                animation = playGunAnim(Animations.CHARGE, LOOP);
                animationHelper.syncAnimation(event, Animations.CHARGE, fireDelay);
            }
            return animation;
        }
        else return getHoldAnimation(event);
    }

//    protected RawAnimation getMeleeAnimation(AnimationState<GunAnimator> event) {
//        if(animationHelper.hasAnimation(MELEE_END)){
//            var animation = begin()
//                    .then(getGunAnim(MELEE), PLAY_ONCE)
//                    .then(getGunAnim(MELEE_END), LOOP);
//            animationHelper.syncAnimation(event, meleeDelay + meleeCooldown, MELEE, MELEE_END);
//            return animation;
//        }
//        else {
//            var animation = begin().then(getGunAnim(MELEE), HOLD_ON_LAST_FRAME);
//            animationHelper.syncAnimation(event, MELEE, meleeDelay);
//            return animation;
//        }
//    }

    protected RawAnimation getMeleeDelayAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType)) {
            var animation = playGunAnim(MELEE, LOOP);
            animationHelper.syncAnimation(event, MELEE, meleeDelay);
            return animation;
        }
        else return getHoldAnimation(event);
    }

    protected RawAnimation getMeleeCooldownAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType)) {
            if (!animationHelper.hasAnimation(MELEE_END))
                return getHoldAnimation(event);

            var animation = playGunAnim(MELEE_END, LOOP);
            animationHelper.syncAnimation(event, MELEE_END, meleeCooldown);
            return animation;
        }
        else return getHoldAnimation(event);
    }

    protected RawAnimation getEquipAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType)) {
            var animation = playGunAnim(EQUIP, LOOP);
            animationHelper.syncAnimation(event, EQUIP, equipTime);
            return animation;
        }
        else return getHoldAnimation(event);
    }

    protected RawAnimation getShootingAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType)) {
            var animation = playGunAnim(SHOT, LOOP);
            animationHelper.syncAnimation(event, SHOT, rate);
            return animation;
        }
        else return getHoldAnimation(event);
    }

    protected RawAnimation getReloadingAnimation(AnimationState<GunAnimator> event) {
        if(isFirstPerson(transformType)) {
            var animation = begin();

            if (ModSyncedDataKeys.RELOAD_START.getValue(getEntity())) {
                animation = getStartReloadAnimation(event);
            } else if (ModSyncedDataKeys.RELOAD_END.getValue(getEntity())) {
                animation = getEndReloadAnimation(event);
            } else {
                animation = getDefaultReloadAnimation(event);
            }

            return animation;
        }
        else return getHoldAnimation(event);
    }

    protected RawAnimation getDefaultReloadAnimation(AnimationState<GunAnimator> event) {
        var animation = playGunAnim(RELOAD, LOOP);
        animationHelper.syncAnimation(event, RELOAD, reloadTime);
        return animation;
    }

    protected RawAnimation getEndReloadAnimation(AnimationState<GunAnimator> event) {
        var animation = playGunAnim(Animations.RELOAD_END, PLAY_ONCE);
        animationHelper.syncAnimation(event, Animations.RELOAD_END, reloadEndTime);
        return animation;
    }

    protected RawAnimation getStartReloadAnimation(AnimationState<GunAnimator> event) {
        var animation = playGunAnim(Animations.RELOAD_START, PLAY_ONCE);
        animationHelper.syncAnimation(event, Animations.RELOAD_START, reloadStartTime);
        return animation;
    }

    protected void handleSoundEvent(SoundKeyframeEvent<GunAnimator> event) {
        var player = minecraft.player;
        var name = event.getKeyframeData().getSound();
        var sounds = getGunItem().getGun().getSoundsMap();
        var sound = sounds.get(name);

        if (sound != null && player != null) {
            minecraft.getSoundManager().play(new GunShotSound(sound, SoundSource.PLAYERS,
                    player.position(), 1, 1, true));
        }
    }

    protected RawAnimation playGunAnim(String name, LoopType loopType) {
        return begin().then(getGunAnim(name), loopType);
    }

    protected String getGunAnim(String name){
        var entity = getEntity();
        var currentItem = entity.getItemInHand(arm);
        var oppositeItem = entity.getItemInHand(PlayerHelper.getOpposite(arm));
        var isOneHanded = isOneHanded(currentItem) && isOneHanded(oppositeItem) || arm == InteractionHand.OFF_HAND || !oppositeItem.isEmpty();
        var hasShield = isOneHanded(currentItem) && oppositeItem.getItem() instanceof ShieldItem;
        if ((hasShield || isOneHanded) && animationHelper.hasAnimation(name + Animations.ONE_HAND_SUFFIX))
            return name + Animations.ONE_HAND_SUFFIX;
        return name;
    }

    private void setupCycledAnimations() {
        var entity = getEntity();
        var cooldown = shootingHandler.getCooldown(entity, arm);
        var data = getGunData();
        var maxAmmo = GunModifierHelper.getMaxAmmo(data);

        if (chamberCycler == null || chamberCycler.getMax() != maxAmmo)
            chamberCycler = new Cycler(1, maxAmmo);

        if (cooldown == rate) {
            barrelCycler.cycle();
            chamberCycler.cycle();
        }
    }
}
