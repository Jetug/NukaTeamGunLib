package com.nukateam.ntgl.client.animators;

import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.model.gun.ThrowableItemModel;
import com.nukateam.ntgl.client.render.renderers.weapon.ThrowableItemRenderer;
import com.nukateam.ntgl.client.util.handler.ClientEquipHandler;
import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.data.constants.Animations;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.util.AnimationHelper;
import com.nukateam.ntgl.common.util.util.ThrowableStateHelper;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationController.AnimationStateHandler;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.keyframe.event.SoundKeyframeEvent;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;

import static com.nukateam.ntgl.client.util.util.TransformUtils.*;
import static com.nukateam.ntgl.common.data.constants.Animations.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import static mod.azure.azurelib.core.animation.Animation.LoopType;
import static mod.azure.azurelib.core.animation.Animation.LoopType.*;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;

@OnlyIn(Dist.CLIENT)
public class ThrowableAnimator extends ItemAnimator implements IConfigProvider<WeaponConfig> {
    public static final String PREPARE = "prepare";
    public static final String PREPARE_SAFE = "prepare_safe";
    public static final String THROW = "throw";
    public static final String THROW_SAFE = "throw_safe";
    protected final ThrowableItemRenderer<ThrowableAnimator> renderer;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final AnimationHelper<ThrowableAnimator> animationHelper;

    protected final AnimationController<ThrowableAnimator> TRIGGER_CONTROLLER;
    protected final AnimationController<ThrowableAnimator> MAIN_CONTROLLER;
    protected final AnimationController<ThrowableAnimator> TICKING_CONTROLLER;
    protected final InteractionHand arm;

    protected int equipTime;
    protected int prepareTime;
    protected int throwingTime;
    protected ThrowMode mode;
    protected boolean isEquiping;
    private ItemStack itemCache = ItemStack.EMPTY;

    public ThrowableAnimator(ItemDisplayContext transformType, ThrowableItemRenderer<ThrowableAnimator> renderer) {
        super(transformType);
        this.renderer = renderer;
        this.arm = getArm();

        ClientTickHandler.addTicker(this, this::tick);
        MAIN_CONTROLLER = createController("mainController", animate()).setSoundKeyframeHandler(this::handleSoundEvent);
        TRIGGER_CONTROLLER = createController("triggerController", event -> PlayState.CONTINUE);
        TICKING_CONTROLLER = createController("tickingController", animateTick());
        animationHelper = new AnimationHelper<>(this, ThrowableItemModel.INSTANCE);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(MAIN_CONTROLLER);
        controllerRegistrar.add(TRIGGER_CONTROLLER);
        controllerRegistrar.add(TICKING_CONTROLLER);
    }

    @Override
    public WeaponConfig getConfig() {
        if (getStack().getItem() instanceof IThrowable item) {
            return item.getConfig();
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
        if (getStack().getItem() instanceof IThrowable throwable) {
            prepareTime = throwable.getConfig().getThrowable().getPrepareTime();
            throwingTime = throwable.getConfig().getThrowable().getThrowTime();
            equipTime = throwable.getConfig().getGeneral().getEquipTime();
            isEquiping = EquipTracker.isEquiping(getEntity(), getArm());
            mode = ThrowableStateHelper.getThrowMode(getStack());
        }
    }

    protected void tickEnd() {}

    protected LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    protected IThrowable getItem() {
        return (IThrowable) getStack().getItem();
    }

    @NotNull
    protected AnimationController<ThrowableAnimator> createController(String name, AnimationStateHandler<ThrowableAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
    }

    protected InteractionHand getArm() {
        return isRightHand(transformType) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    protected AnimationStateHandler<ThrowableAnimator> animate() {
        return event -> {
            if(itemCache != getStack()) {
                itemCache = getStack();
                return event.setAndContinue(begin().then("void", PLAY_ONCE));
            }
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var holdAnimation = getHoldAnimation(event);

                if (!isFirstPerson(transformType))
                    return PlayState.STOP;

                var animation = begin();

                if(ClientEquipHandler.get().isEquiping(arm)) {
                    animation = getEquipAnimation(event);
                }
                else if(isPreparing()){
                    animation = getPrepareAnimation(event);
                }
                else if(isThrowing()){
                    animation = getThrowingAnimation(event);
                }
                else if (ClientHandler.getInspectionTicks(getArm()) > 0) {
                    animation = getInspectionAnimation(event);
                }
                else {
                    animation = holdAnimation;
                }

                return event.setAndContinue(animation);
            } catch (Exception e) {
                return PlayState.STOP;
            }
        };
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

    protected AnimationStateHandler<ThrowableAnimator> animateTick() {
        return event -> {
            var controller = event.getController();
            controller.setAnimationSpeed(1);
            var holdAnimation = getHoldAnimation(event);

            if (!isHandTransform(transformType))
                return PlayState.STOP;

            var animation = begin();
            if(mode == ThrowMode.UNSAFE && (isHolding() || isThrowing())){
                animation = getTickingAnimation(event);
            }
            else {
                return PlayState.STOP;
            }

            return event.setAndContinue(animation);
        };
    }

    protected static @NotNull RawAnimation getHideAnimation() {
        return begin().then(Animations.HIDE, HOLD_ON_LAST_FRAME);
    }

    protected RawAnimation getInspectionAnimation(AnimationState<ThrowableAnimator> event) {
        RawAnimation animation;
        animation = playGunAnim(Animations.INSPECT, PLAY_ONCE);
        animationHelper.syncAnimation(event, Animations.INSPECT, ClientHandler.getMaxInspectionTicks());
        return animation;
    }

    protected RawAnimation getHoldAnimation(AnimationState<ThrowableAnimator> event) {
        return playGunAnim(HOLD, LOOP);
    }

    protected RawAnimation getTickingAnimation(AnimationState<ThrowableAnimator> event) {
        var animation = playGunAnim(TICKING, LOOP);
        return animation;
    }

    protected RawAnimation getEquipAnimation(AnimationState<ThrowableAnimator> event) {
        var animation = playGunAnim(EQUIP, LOOP);
        animationHelper.syncAnimation(event, equipTime, EQUIP);
        return animation;
    }

    protected RawAnimation getPrepareAnimation(AnimationState<ThrowableAnimator> event) {
        var name = PREPARE;
        if(mode == ThrowMode.SAFE && animationHelper.hasAnimation(PREPARE_SAFE)){
            name = PREPARE_SAFE;
        }

        var animation = playGunAnim(name, HOLD_ON_LAST_FRAME);
        animationHelper.syncAnimation(event, prepareTime, name);
        return animation;
    }

    protected RawAnimation getThrowingAnimation(AnimationState<ThrowableAnimator> event) {
        var name = THROW;
        if(mode == ThrowMode.SAFE && animationHelper.hasAnimation(THROW_SAFE)){
            name = THROW_SAFE;
        }
        var animation = playGunAnim(name, HOLD_ON_LAST_FRAME);
        animationHelper.syncAnimation(event, throwingTime, name);
        return animation;
    }

    protected void handleSoundEvent(SoundKeyframeEvent<ThrowableAnimator> event) {
        var player = minecraft.player;
        var name = event.getKeyframeData().getSound();
        var sounds = getItem().getConfig().getSoundsMap();
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
        var isOneHanded = isOneHanded(currentItem) && isOneHanded(oppositeItem) || arm == InteractionHand.OFF_HAND;
        var hasShield = isOneHanded(currentItem) && oppositeItem.getItem() instanceof ShieldItem;
        if ((hasShield || isOneHanded) && animationHelper.hasAnimation(name + Animations.ONE_HAND_SUFFIX))
            return name + Animations.ONE_HAND_SUFFIX;
        return name;
    }

    public boolean isOneHanded(ItemStack item){
        return false;
    }
}
