package com.nukateam.ntgl.common.util.helpers.compatibility;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess.getPlayerAssociatedData;
import static dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory.ANIMATION_DATA_FACTORY;

@OnlyIn(Dist.CLIENT)
public class PlayerAnimationHelper {
    public static final ResourceLocation ANIMATION = ResourceLocation.tryBuild(Ntgl.MOD_ID, "animation");
    public static final ResourceLocation MIRROR_ANIMATION = ResourceLocation.tryBuild(Ntgl.MOD_ID, "mirror_animation");
    public static final SpeedModifier SPEED_NORMAL = new SpeedModifier(1);
    public static final SpeedModifier SPEED_MIRROR = new SpeedModifier(1);

    public static final HashMap<Pair<Player, Boolean>, SpeedModifier> speedModifiers = new HashMap<>();

//    public static ModifierLayer<IAnimation> normalLayer;
//    public static ModifierLayer<IAnimation> mirrorLayer;

    @OnlyIn(Dist.CLIENT)
    public static void playAnim(Player player, ResourceLocation name, int length, boolean mirror) {
        if (player == null) return;

        var animation = (KeyframeAnimation)PlayerAnimationRegistry.getAnimation(name);

        if (animation != null) {
            ModifierLayer<IAnimation> animationLayer;
            SpeedModifier speedModifier = speedModifiers.computeIfAbsent(Pair.of(player, mirror), (k) -> new SpeedModifier(1));

            if(mirror){
//                speedModifier = SPEED_MIRROR;
                animationLayer = (ModifierLayer<IAnimation>) getPlayerAssociatedData((AbstractClientPlayer)player).get(MIRROR_ANIMATION);
                animationLayer.addModifier(speedModifier, 0);
                animationLayer.addModifier(new MirrorModifier(), 1);
            }
            else {
//                speedModifier = SPEED_NORMAL;
                animationLayer = (ModifierLayer<IAnimation>) getPlayerAssociatedData((AbstractClientPlayer)player).get(ANIMATION);
                animationLayer.addModifier(speedModifier, 0);
            }

            var duration = animation.getLength();
            speedModifier.speed = (float)duration / (float)length;
            animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
        }
    }

//    @OnlyIn(Dist.CLIENT)
//    public static void playAnim(AbstractClientPlayer player, ResourceLocation name, int length, boolean mirror) {
//        if (player == null) return;
//
//        var animationLayer = mirror ? getMirrorLayer(player) : getNormalLayer(player);
//        var speedModifier = mirror ? SPEED_MIRROR : SPEED_NORMAL;
//        var animation = PlayerAnimationRegistry.getAnimation(name);
//
//        if (animation != null) {
//            var duration = animation.getLength();
//            speedModifier.speed = (float)duration / (float)length;
//            animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
//        }
//    }

//    public static void playAnim(Player player, ResourceLocation animationResource, int length, boolean mirror) {
//        if (player == null) return;
//        var animationLayer = (ModifierLayer<IAnimation>) getPlayerAssociatedData((AbstractClientPlayer)player).get(ANIMATION);
//
//        if (animationLayer != null) {
//            var animation = PlayerAnimationRegistry.getAnimation(animationResource);
//            if(animation != null)
//                animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
//        }
//    }

    private static @NotNull ModifierLayer<IAnimation> getNormalLayer(AbstractClientPlayer player) {
        var normalLayer = getAnimationLayer(player, ANIMATION);
        normalLayer.addModifier(SPEED_NORMAL, 0);
        return normalLayer;
    }

    private static @NotNull ModifierLayer<IAnimation> getMirrorLayer(AbstractClientPlayer player) {
        var mirrorLayer = getAnimationLayer(player, MIRROR_ANIMATION);
        mirrorLayer.addModifier(SPEED_MIRROR, 0);
        mirrorLayer.addModifier(new MirrorModifier(), 1);
        return mirrorLayer;
    }

    @OnlyIn(Dist.CLIENT)
    public static void stopAnim(Player player, boolean mirror) {
        if (player == null) return;
        var animationLayer = getAnimationLayer((AbstractClientPlayer)player, ANIMATION);
        var mirrorLayer = getAnimationLayer((AbstractClientPlayer)player, MIRROR_ANIMATION);

        if (animationLayer != null && mirrorLayer != null) {
            if(mirror) mirrorLayer.setAnimation(null);
            else animationLayer.setAnimation(null);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static @Nullable ModifierLayer<IAnimation> getAnimationLayer(AbstractClientPlayer player, ResourceLocation resourceLocation) {
        return (ModifierLayer<IAnimation>) getPlayerAssociatedData(player).get(resourceLocation);
    }

    public static void register(){
        ANIMATION_DATA_FACTORY.registerFactory(
                PlayerAnimationHelper.ANIMATION,
                42,
                PlayerAnimationHelper::registerPlayerAnimation);

        ANIMATION_DATA_FACTORY.registerFactory(
                PlayerAnimationHelper.MIRROR_ANIMATION,
                43,
                PlayerAnimationHelper::registerPlayerAnimation);
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }
}
