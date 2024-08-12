package com.nukateam.ntgl.common.helpers;

import com.nukateam.ntgl.Ntgl;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
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

    @OnlyIn(Dist.CLIENT)
    public static void playAnim(Player player, ResourceLocation name) {
        playAnim(player, name, false);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playAnim(Player player, ResourceLocation name, boolean mirror) {
        if (player == null) return;
        var animationLayer = getAnimationLayer((AbstractClientPlayer)player, ANIMATION);
        var mirrorLayer = getAnimationLayer((AbstractClientPlayer)player, MIRROR_ANIMATION);

        if (animationLayer != null) {
            var animationResource = name; //new ResourceLocation(Ntgl.MOD_ID, name);
            var animation = PlayerAnimationRegistry.getAnimation(animationResource);
            if(mirror) {
                mirrorLayer.addModifier(new MirrorModifier(), 0);
            }
            else
                animationLayer.removeModifier(0);

            if(animation != null)
                animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static @Nullable ModifierLayer<IAnimation> getAnimationLayer(AbstractClientPlayer player, ResourceLocation resourceLocation) {
        return (ModifierLayer<IAnimation>) getPlayerAssociatedData(player).get(resourceLocation);
    }
}
