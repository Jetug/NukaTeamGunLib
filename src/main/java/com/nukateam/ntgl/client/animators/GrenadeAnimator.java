package com.nukateam.ntgl.client.animators;

import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.model.gun.GeoGrenadeModel;
import com.nukateam.ntgl.client.model.gun.GeoGunModel;
import com.nukateam.ntgl.client.render.renderers.gun.DynamicGrenadeRenderer;
import com.nukateam.ntgl.client.render.renderers.gun.DynamicGunRenderer;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Animations;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.util.AnimationHelper;
import com.nukateam.ntgl.common.util.util.Cycler;
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
public class GrenadeAnimator extends ItemAnimator implements IConfigProvider<ThrowableConfig> {
    protected final DynamicGrenadeRenderer<GrenadeAnimator> renderer;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final AnimationHelper<GrenadeAnimator> animationHelper;

    protected final AnimationController<GrenadeAnimator> TRIGGER_CONTROLLER;
    protected final AnimationController<GrenadeAnimator> MAIN_CONTROLLER;
    protected final InteractionHand arm;

    protected int equipTime;
    protected int prepareTime;

    public GrenadeAnimator(ItemDisplayContext transformType, DynamicGrenadeRenderer<GrenadeAnimator> renderer) {
        super(transformType);
        this.renderer = renderer;
        this.arm = getArm();

        ClientTickHandler.addTicker(this, this::tick);
        TRIGGER_CONTROLLER = createController("triggerController", event -> PlayState.CONTINUE);
        MAIN_CONTROLLER = createController("mainController", animate()).setSoundKeyframeHandler(this::handleSoundEvent);
        animationHelper = new AnimationHelper<>(this, GeoGrenadeModel.INSTANCE);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(MAIN_CONTROLLER);
        controllerRegistrar.add(TRIGGER_CONTROLLER);
    }

    @Override
    public ThrowableConfig getConfig() {
        if (getStack().getItem() instanceof IThrowable item) {
            return item.getConfig();
        }

        return new ThrowableConfig();
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
            prepareTime = throwable.getPrepareTime();
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
    protected AnimationController<GrenadeAnimator> createController(String name, AnimationStateHandler<GrenadeAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
    }

    protected InteractionHand getArm() {
        return isRightHand(transformType) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    protected AnimationStateHandler<GrenadeAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var shooter = getEntity();
                var holdAnimation = getHoldAnimation(event);

                if (!isHandTransform(transformType))
                    return event.setAndContinue(holdAnimation);

                var animation = begin();
                var item = (IThrowable)getStack().getItem();
//                if(equipTime > 0 && shooter instanceof Player player && EquipTracker.isEquiping(player, getArm())) {
//                    animation = getEquipAnimation(event);
//                }
                if(item.isPreparing()){
                    animation = getPrepareAnimation(event);
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

    protected static @NotNull RawAnimation getHideAnimation() {
        return begin().then(Animations.HIDE, HOLD_ON_LAST_FRAME);
    }

    protected RawAnimation getInspectionAnimation(AnimationState<GrenadeAnimator> event) {
        RawAnimation animation;
        animation = playGunAnim(Animations.INSPECT, PLAY_ONCE);
        animationHelper.syncAnimation(event, Animations.INSPECT, ClientHandler.getMaxInspectionTicks());
        return animation;
    }

    protected RawAnimation getHoldAnimation(AnimationState<GrenadeAnimator> event) {
        return playGunAnim(HOLD, LOOP);
    }

    protected RawAnimation getPrepareAnimation(AnimationState<GrenadeAnimator> event) {
        var animation = playGunAnim("pin", LOOP);
        animationHelper.syncAnimation(event, "pin", prepareTime);
        return animation;
    }

    protected void handleSoundEvent(SoundKeyframeEvent<GrenadeAnimator> event) {
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
