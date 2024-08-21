package com.nukateam.ntgl.common.helpers;

import com.nukateam.ntgl.Ntgl;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import static dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess.getPlayerAssociatedData;

@OnlyIn(Dist.CLIENT)
public class PlayerAnimationHelper {
    public static final ResourceLocation ANIMATION = new ResourceLocation(Ntgl.MOD_ID, "animation");
    public static final ResourceLocation MIRROR_ANIMATION = new ResourceLocation(Ntgl.MOD_ID, "mirror_animation");
    public static final SpeedModifier SPEED_NORMAL = new SpeedModifier(1);
    public static final SpeedModifier SPEED_MIRROR = new SpeedModifier(1);

    public static ModifierLayer<IAnimation> normalLayer;
    public static ModifierLayer<IAnimation> mirrorLayer;

    @OnlyIn(Dist.CLIENT)
    public static void playAnim(Player player, ResourceLocation name, int length, boolean mirror) {
        if (player == null) return;
        if(mirrorLayer == null){
            mirrorLayer = getAnimationLayer((AbstractClientPlayer)player, MIRROR_ANIMATION);
            mirrorLayer.addModifier(SPEED_MIRROR, 0);
            mirrorLayer.addModifier(new MirrorModifier(), 1);
        }
        if(normalLayer == null){
            normalLayer = getAnimationLayer((AbstractClientPlayer)player, ANIMATION);
            normalLayer.addModifier(SPEED_NORMAL, 0);
        }

        var animationLayer = mirror ? mirrorLayer : normalLayer;
        var speedModifier = mirror ? SPEED_MIRROR : SPEED_NORMAL;
        var animation = PlayerAnimationRegistry.getAnimation(name);

        if (animationLayer != null && animation != null) {
            var duration = animation.getLength();
            speedModifier.speed = (float)duration / (float)length;
            animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
        }
    }
//
//    @OnlyIn(Dist.CLIENT)
//    public static void playAnim(Player player, ResourceLocation name, int length, boolean mirror) {
//        if (player == null) return;
//
//        var animationLayer = getAnimationLayer((AbstractClientPlayer)player, MIRROR_ANIMATION);
//        var animation = PlayerAnimationRegistry.getAnimation(name);
//
//        if (animationLayer != null && animation != null) {
//            var duration = animation.getLength();
//            var multiplier = (float)duration / (float)length;
//
////            SPEED_NORMAL.speed = multiplier;
//
//            try{
//                animationLayer.removeModifier(0);
//            } catch (Exception e){}
//
//            animationLayer.addModifier(new SpeedModifier(multiplier), 0);
////            if(mirror) animationLayer.addModifier(new MirrorModifier(), 1);
//
//            animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
//        }
//    }

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
}
