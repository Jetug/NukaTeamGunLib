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
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Animations;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
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
    public static final String STATIC = "static";
    public static final String PREPARE = "prepare";
    public static final String PREPARE_SAFE = "prepare_safe";
    public static final String THROW = "throw";
    public static final String THROW_SAFE = "throw_safe";

    protected final DynamicGunRenderer<GunAnimator> renderer;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ClientShootingHandler shootingHandler = ClientShootingHandler.get();
    protected final ClientReloadHandler reloadHandler = ClientReloadHandler.get();
    protected final AnimationHelper<GunAnimator> animationHelper;

    protected final AnimationController<GunAnimator> TRIGGER_CONTROLLER;
    protected final AnimationController<GunAnimator> MAIN_CONTROLLER;
    protected final AnimationController<GunAnimator> REVOLVER_CONTROLLER;
    protected final AnimationController<GunAnimator> BARREL_CONTROLLER;
    protected final AnimationController<GunAnimator> TICKING_CONTROLLER;

    protected final InteractionHand arm;

    protected Cycler barrelCycler = new Cycler(1, getBarrelAmount());
    protected Cycler chamberCycler = null;

    protected IWeapon currentGun = null;
    protected int rate;
    protected int equipTime;
    protected int meleeDelay;
    protected int meleeCooldown;
    protected int fireDelay;
    protected int reloadTime;
    protected int reloadStartTime;
    protected int reloadEndTime;
    protected boolean isEquiping;
    protected ItemStack itemCache = ItemStack.EMPTY;
    protected ThrowMode throwMode;
    protected int prepareTime;
    protected int throwingTime;

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
        TICKING_CONTROLLER = createController("tickingController", animateTick());
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
        if (!(getStack().getItem() instanceof IWeapon weapon))
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

        var conging = weapon.getModifiedConfig(getStack());
        this.prepareTime  = conging.getThrowable().getPrepareTime();
        this.throwingTime = conging.getThrowable().getThrowTime();
        this.throwMode = ThrowableStateHelper.getThrowMode(getStack());

//        Ntgl.LOGGER.info("! Is equiping: " + isEquiping);

//        if(isEquiping) {
//            Ntgl.LOGGER.info("! Equip time: " + equipTime);
//        }
        setupCycledAnimations();
    }

    protected void tickEnd() {}

    protected int getBarrelAmount() {
        return 1;
    }

    protected LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    protected IWeapon getGunItem() {
        return (IWeapon) getStack().getItem();
    }

    protected @NotNull GunData getGunData() {
        return new GunData(getStack(), getEntity());
    }

    protected boolean isOneHanded(ItemStack stack) {
        return GunStateHelper.isOneHanded(new GunData(stack, getEntity()));
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
            if(itemCache != getStack()) {
                itemCache = getStack();
                return event.setAndContinue(begin().then("void", PLAY_ONCE));
            }
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var shooter = getEntity();

                if (!isHandTransform(transformType))
                    return event.setAndContinue(getHoldAnimation(event));

                var isShooting = shootingHandler.isShooting(shooter, arm);
                var data = shootingHandler.getShootingData(arm);
                var animation = begin();

                if(ClientEquipHandler.get().isEquiping(arm)) {
                    animation = getEquipAnimation(event);
//                    Ntgl.LOGGER.debug("!Equip");
                }
                else if(isPreparing()){
                    animation = getPrepareAnimation(event);
                }
                else if(isThrowing()){
                    animation = getThrowingAnimation(event);
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
                        animation = getHoldAnimation(event);
                    else {
                        currentGun = getGunItem();
                        animation = playGunAnim(SHOT, LOOP);
                    }
//                    Ntgl.LOGGER.debug("! Hold");
                }

                return animation != null ? event.setAndContinue(animation): PlayState.STOP;
            } catch (Exception e) {
                Ntgl.LOGGER.error(e.getMessage());
                return PlayState.STOP;
            }
        };
    }

    protected AnimationStateHandler<GunAnimator> animateTick() {
        return event -> {
            var controller = event.getController();
            controller.setAnimationSpeed(1);
            var holdAnimation = getHoldAnimation(event);

            if (!isHandTransform(transformType))
                return PlayState.STOP;

            var animation = begin();
            if(throwMode == ThrowMode.UNSAFE && (isHolding() || isThrowing())){
                animation = getTickingAnimation(event);
            }
            else {
                return PlayState.STOP;
            }

            return event.setAndContinue(animation);
        };
    }

    protected AnimationStateHandler<GunAnimator> animateRevolver() {
        return (event) -> getCycledAnimation(event, Animations.CHAMBER, this.chamberCycler);
    }

    protected AnimationStateHandler<GunAnimator> animateBarrels() {
        return (event) -> getCycledAnimation(event, Animations.BARREL, this.barrelCycler);
    }

    protected boolean isPreparing() {
        return ModSyncedDataKeys.getPreparingDataKey(getArm()).getValue(getEntity());
    }

    protected boolean isHolding() {
        return ModSyncedDataKeys.getHoldingDataKey(getArm()).getValue(getEntity());
    }

    protected boolean isThrowing() {
        return ModSyncedDataKeys.getThrowingDataKey(getArm()).getValue(getEntity());
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
        return playGunAnim(HOLD, LOOP);
    }

    protected RawAnimation getStaticAnimation(AnimationState<GunAnimator> event) {
        return playGunAnim(STATIC, LOOP);
    }

    protected RawAnimation getHideAnimation(AnimationState<GunAnimator> event) {
        return begin().then(Animations.HIDE, HOLD_ON_LAST_FRAME);
    }

    protected RawAnimation getInspectionAnimation(AnimationState<GunAnimator> event) {
            RawAnimation animation;
            animation = playGunAnim(Animations.INSPECT, PLAY_ONCE);
            animationHelper.syncAnimation(event, Animations.INSPECT, ClientHandler.getMaxInspectionTicks());
            return animation;
    }

    protected RawAnimation getChargingAnimation(AnimationState<GunAnimator> event, ShootingData shootingData) {
            var animation = begin();
            if (animationHelper.hasAnimation(Animations.CHARGE)) {
                BARREL_CONTROLLER.stop();
                BARREL_CONTROLLER.setAnimation(begin().then("void", PLAY_ONCE));
                animation = playGunAnim(Animations.CHARGE, LOOP);
                animationHelper.syncAnimation(event, Animations.CHARGE, fireDelay);
            }
            return animation;
    }

    protected RawAnimation getTickingAnimation(AnimationState<GunAnimator> event) {
        var animation = playGunAnim(TICKING, LOOP);
        return animation;
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
            var animation = playGunAnim(MELEE, LOOP);
            animationHelper.syncAnimation(event, MELEE, meleeDelay);
            return animation;
    }

    protected RawAnimation getMeleeCooldownAnimation(AnimationState<GunAnimator> event) {
            if (!animationHelper.hasAnimation(MELEE_END))
                return getHoldAnimation(event);

            var animation = playGunAnim(MELEE_END, LOOP);
            animationHelper.syncAnimation(event, MELEE_END, meleeCooldown);
            return animation;
    }

    protected RawAnimation getEquipAnimation(AnimationState<GunAnimator> event) {
            var animation = playGunAnim(EQUIP, LOOP);
            animationHelper.syncAnimation(event, EQUIP, equipTime);
            return animation;
    }

    protected RawAnimation getShootingAnimation(AnimationState<GunAnimator> event) {
            var animation = playGunAnim(SHOT, LOOP);
            animationHelper.syncAnimation(event, SHOT, rate);
            return animation;
    }

    protected RawAnimation getReloadingAnimation(AnimationState<GunAnimator> event) {
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

    protected RawAnimation getPrepareAnimation(AnimationState<GunAnimator> event) {
        var name = PREPARE;
        if(throwMode == ThrowMode.SAFE && animationHelper.hasAnimation(PREPARE_SAFE)){
            name = PREPARE_SAFE;
        }

        var animation = playGunAnim(name, HOLD_ON_LAST_FRAME);
        animationHelper.syncAnimation(event, prepareTime, name);
        return animation;
    }

    protected RawAnimation getThrowingAnimation(AnimationState<GunAnimator> event) {
        var name = THROW;
        if(throwMode == ThrowMode.SAFE && animationHelper.hasAnimation(THROW_SAFE)){
            name = THROW_SAFE;
        }
        var animation = playGunAnim(name, HOLD_ON_LAST_FRAME);
        animationHelper.syncAnimation(event, throwingTime, name);
        return animation;
    }

    protected void handleSoundEvent(SoundKeyframeEvent<GunAnimator> event) {
        var player = minecraft.player;
        var name = event.getKeyframeData().getSound();
        var sounds = getGunItem().getConfig().getSoundsMap();
        var sound = sounds.get(name);

        if (sound != null && player != null) {
            minecraft.getSoundManager().play(new GunShotSound(sound, SoundSource.PLAYERS,
                    player.position(), 1, 1, true));
        }
    }

    protected RawAnimation playGunAnim(String name, LoopType loopType) {
        if(isFirstPerson(getTransformType())) {
            var gunAnim = getGunAnim(name);
            return begin().then(gunAnim, loopType);
        }
        else {
            var tpvOneHand = name + ONE_HAND_SUFFIX + TPV_SUFFIX;
            var tpv = name + TPV_SUFFIX;

            if(isOneHanded() && animationHelper.hasAnimation(tpvOneHand)){
                return begin().then(tpvOneHand, loopType);
            }
            else if(animationHelper.hasAnimation(tpv)){
                return begin().then(tpv, loopType);
            }
            return begin();
        }
    }

    protected String getGunAnim(String name){
        var isOneHanded = isOneHanded();

        if (isOneHanded && animationHelper.hasAnimation(name + Animations.ONE_HAND_SUFFIX)) {
            name += Animations.ONE_HAND_SUFFIX;
        }

        return name;
    }

    private boolean isOneHanded() {
        var entity = getEntity();

        var currentItem = entity.getItemInHand(arm);
        var oppositeItem = entity.getItemInHand(PlayerHelper.getOpposite(arm));
        var isOneHanded = (isOneHanded(currentItem) && isOneHanded(oppositeItem) && !oppositeItem.isEmpty())
                || arm == InteractionHand.OFF_HAND;
        var hasShield = isOneHanded(currentItem) && oppositeItem.getItem() instanceof ShieldItem;
        var isS = (hasShield || isOneHanded);
        return isS;
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
