package com.nukateam.ntgl.client.animators;

import com.nukateam.geo.render.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.*;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.client.model.gun.*;
import com.nukateam.ntgl.client.render.renderers.weapon.*;
import com.nukateam.ntgl.client.util.helpers.TransformUtils;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.data.constants.Animations;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationController.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import net.minecraft.client.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;

import static com.nukateam.ntgl.client.util.helpers.TransformUtils.*;
import static com.nukateam.ntgl.common.data.constants.Animations.*;
import static software.bernie.geckolib.core.animation.AnimatableManager.*;
import static software.bernie.geckolib.core.animation.Animation.*;
import static software.bernie.geckolib.core.animation.Animation.LoopType.*;
import static software.bernie.geckolib.core.animation.RawAnimation.begin;

@OnlyIn(Dist.CLIENT)
public class WeaponAnimator extends ItemAnimator implements IConfigProvider<WeaponConfig> {
    public static final String STATIC = "static";
    public static final String PREPARE = "prepare";
    public static final String PREPARE_SAFE = "prepare_safe";
    public static final String THROW = "throw";
    public static final String THROW_SAFE = "throw_safe";
    public static final String VOID = "void";

    protected final DynamicWeaponRenderer<WeaponAnimator> renderer;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ClientShootingHandler shootingHandler = ClientShootingHandler.get();
    protected final ClientReloadHandler reloadHandler = ClientReloadHandler.get();
    protected final AnimationHelper<WeaponAnimator> animationHelper;

    protected final AnimationController<WeaponAnimator> TRIGGER_CONTROLLER;
    protected final AnimationController<WeaponAnimator> MAIN_CONTROLLER;
    protected final AnimationController<WeaponAnimator> REVOLVER_CONTROLLER;
    protected final AnimationController<WeaponAnimator> BARREL_CONTROLLER;
    protected final AnimationController<WeaponAnimator> TICKING_CONTROLLER;

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

    public WeaponAnimator(ItemDisplayContext transformType, DynamicWeaponRenderer<WeaponAnimator> renderer) {
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
        animationHelper = new AnimationHelper<>(this, GeoWeaponModel.INSTANCE);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(MAIN_CONTROLLER);
        controllerRegistrar.add(TRIGGER_CONTROLLER);
        controllerRegistrar.add(REVOLVER_CONTROLLER);
        controllerRegistrar.add(BARREL_CONTROLLER);
        controllerRegistrar.add(TICKING_CONTROLLER);
    }

    @Override
    public WeaponConfig getConfig() {
        if (getStack().getItem() instanceof IConfigProvider config) {
            if (config.getConfig() instanceof WeaponConfig weaponConfig)
                return weaponConfig;
        }

        return new WeaponConfig();
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

        if(getEntity().getItemInHand(getArm()).getItem() instanceof IWeapon) {
            this.rate = WeaponModifierHelper.getRate(shootingHandler.getWeaponData(getEntity(), getArm()));
        }
        this.equipTime = WeaponModifierHelper.getEquipTime(data);
        this.isEquiping = EquipTracker.isEquiping(getEntity(), getArm());
        this.meleeDelay = WeaponModifierHelper.getMeleeDelay(data);
        this.meleeCooldown = WeaponModifierHelper.getMeleeCooldown(data);
        this.fireDelay = WeaponModifierHelper.getFireDelay(data);
        this.reloadTime = WeaponModifierHelper.getReloadTime(data);
        this.reloadStartTime = WeaponModifierHelper.getReloadStart(data);
        this.reloadEndTime = WeaponModifierHelper.getReloadEnd(data);

        var conging = weapon.getModifiedConfig(getStack());
        this.prepareTime  = WeaponModifierHelper.getPrepareTime(data);
        this.throwingTime = WeaponModifierHelper.getThrowTime(data);
        this.throwMode = ThrowableStateHelper.getThrowMode(data);

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

    protected IWeapon getWeapon() {
        return (IWeapon) getStack().getItem();
    }

    protected @NotNull WeaponData getGunData() {
        return new WeaponData(getStack(), getEntity());
    }

    protected boolean isOneHanded(ItemStack stack) {
        return WeaponModifierHelper.isOneHanded(new WeaponData(stack, getEntity()));
    }

    @NotNull
    protected AnimationController<WeaponAnimator> createController(String name, AnimationStateHandler<WeaponAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
    }

    protected InteractionHand getArm() {
        return isRightHand(transformType) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    protected AnimationStateHandler<WeaponAnimator> animate() {
        return event -> {
            if(itemCache != getStack()) {
                itemCache = getStack();
                return event.setAndContinue(playVoid());
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
                    if (currentGun == getWeapon())
                        animation = getHoldAnimation(event);
                    else {
                        currentGun = getWeapon();
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

    private @NotNull RawAnimation playVoid() {
        if(animationHelper.hasAnimation(VOID)) {
            return begin().then(VOID, PLAY_ONCE);
        }
        else return begin();
    }

    protected AnimationStateHandler<WeaponAnimator> animateTick() {
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

    protected AnimationStateHandler<WeaponAnimator> animateRevolver() {
        return (event) -> getCycledAnimation(event, Animations.CHAMBER, this.chamberCycler);
    }

    protected AnimationStateHandler<WeaponAnimator> animateBarrels() {
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

    protected PlayState getCycledAnimation(AnimationState<WeaponAnimator> event, String animationName, Cycler cycler) {
        event.getController().setAnimationSpeed(1.0);

        if (TransformUtils.isHandTransform(this.transformType) && cycler != null) {
            var entity = this.getEntity();
            var isShooting = shootingHandler.isShooting(entity, TransformUtils.getHand(this.transformType));
            var finalAnim = animationName + cycler.getCurrent();

            RawAnimation animation = null;
            if (isShooting && this.animationHelper.hasAnimation(finalAnim)) {
                animation = RawAnimation.begin().then(finalAnim, LoopType.HOLD_ON_LAST_FRAME);
                this.animationHelper.syncAnimation(event, rate, finalAnim);
            }

            return event.setAndContinue(animation);
        }
        return PlayState.STOP;
    }

    protected RawAnimation getHoldAnimation(AnimationState<WeaponAnimator> event) {
        return playGunAnim(HOLD, LOOP);
    }

    protected RawAnimation getStaticAnimation(AnimationState<WeaponAnimator> event) {
        return playGunAnim(STATIC, LOOP);
    }

    protected RawAnimation getHideAnimation(AnimationState<WeaponAnimator> event) {
        return begin().then(Animations.HIDE, HOLD_ON_LAST_FRAME);
    }

    protected RawAnimation getInspectionAnimation(AnimationState<WeaponAnimator> event) {
            RawAnimation animation;
            animation = playGunAnim(Animations.INSPECT, PLAY_ONCE);
            animationHelper.syncAnimation(event, ClientHandler.getMaxInspectionTicks(), Animations.INSPECT);
            return animation;
    }

    protected RawAnimation getChargingAnimation(AnimationState<WeaponAnimator> event, ShootingData shootingData) {
            var animation = begin();
            if (animationHelper.hasAnimation(Animations.CHARGE)) {
                BARREL_CONTROLLER.stop();
                BARREL_CONTROLLER.setAnimation(playVoid());
                animation = playGunAnim(Animations.CHARGE, LOOP);
                animationHelper.syncAnimation(event, fireDelay, Animations.CHARGE);
            }
            return animation;
    }

    protected RawAnimation getTickingAnimation(AnimationState<WeaponAnimator> event) {
        return playGunAnim(TICKING, LOOP);
    }

    protected RawAnimation getMeleeDelayAnimation(AnimationState<WeaponAnimator> event) {
            var animation = playGunAnim(MELEE, LOOP);
            animationHelper.syncAnimation(event, meleeDelay, MELEE);
            return animation;
    }

    protected RawAnimation getMeleeCooldownAnimation(AnimationState<WeaponAnimator> event) {
            if (!animationHelper.hasAnimation(MELEE_END))
                return getHoldAnimation(event);

            var animation = playGunAnim(MELEE_END, LOOP);
            animationHelper.syncAnimation(event, meleeCooldown, MELEE_END);
            return animation;
    }

    protected RawAnimation getEquipAnimation(AnimationState<WeaponAnimator> event) {
            var animation = playGunAnim(EQUIP, LOOP);
            animationHelper.syncAnimation(event, equipTime, EQUIP);
            return animation;
    }

    protected RawAnimation getShootingAnimation(AnimationState<WeaponAnimator> event) {
            var animation = playGunAnim(SHOT, LOOP);
            animationHelper.syncAnimation(event, rate, SHOT);
            return animation;
    }

    protected RawAnimation getReloadingAnimation(AnimationState<WeaponAnimator> event) {
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

    protected RawAnimation getDefaultReloadAnimation(AnimationState<WeaponAnimator> event) {
        var animation = playGunAnim(RELOAD, LOOP);
        animationHelper.syncAnimation(event, reloadTime, RELOAD);
        return animation;
    }

    protected RawAnimation getEndReloadAnimation(AnimationState<WeaponAnimator> event) {
        var animation = playGunAnim(Animations.RELOAD_END, PLAY_ONCE);
        animationHelper.syncAnimation(event, reloadEndTime, Animations.RELOAD_END);
        return animation;
    }

    protected RawAnimation getStartReloadAnimation(AnimationState<WeaponAnimator> event) {
        var animation = playGunAnim(Animations.RELOAD_START, PLAY_ONCE);
        animationHelper.syncAnimation(event, reloadStartTime, Animations.RELOAD_START);
        return animation;
    }

    protected RawAnimation getPrepareAnimation(AnimationState<WeaponAnimator> event) {
        var name = PREPARE;
        if(throwMode == ThrowMode.SAFE && animationHelper.hasAnimation(PREPARE_SAFE)){
            name = PREPARE_SAFE;
        }

        var animation = playGunAnim(name, HOLD_ON_LAST_FRAME);
        animationHelper.syncAnimation(event, prepareTime, name);
        return animation;
    }

    protected RawAnimation getThrowingAnimation(AnimationState<WeaponAnimator> event) {
        var name = THROW;
        if(throwMode == ThrowMode.SAFE && animationHelper.hasAnimation(THROW_SAFE)){
            name = THROW_SAFE;
        }
        var animation = playGunAnim(name, HOLD_ON_LAST_FRAME);
        animationHelper.syncAnimation(event, throwingTime, name);
        return animation;
    }

    protected void handleSoundEvent(SoundKeyframeEvent<WeaponAnimator> event) {
        var player = minecraft.player;
        var name = event.getKeyframeData().getSound();
        var sounds = getWeapon().getConfig().getSoundsMap();
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
        var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);

        if (chamberCycler == null || chamberCycler.getMax() != maxAmmo)
            chamberCycler = new Cycler(1, maxAmmo);

        if (cooldown == rate) {
            barrelCycler.cycle();
            chamberCycler.cycle();
        }
    }
}
