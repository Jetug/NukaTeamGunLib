package com.nukateam.ntgl.common.helpers;

import com.nukateam.ntgl.Ntgl;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess.getPlayerAssociatedData;

public class PlayerAnimationHelper {
    public static final ResourceLocation ANIMATION = new ResourceLocation(Ntgl.MOD_ID, "animation");

    public static void playAnim(Player player, String name) {
        if (player == null) return;
        var animationLayer = (ModifierLayer<IAnimation>) getPlayerAssociatedData((AbstractClientPlayer)player).get(ANIMATION);

        if (animationLayer != null) {
            var animationResource = new ResourceLocation(Ntgl.MOD_ID, name);
            var animation = PlayerAnimationRegistry.getAnimation(animationResource);
            if(animation != null)
                animationLayer.setAnimation(new KeyframeAnimationPlayer(animation));
        }
    }
}
