package com.nukateam.ntgl.client.util.util;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.data.holders.LoadingType;
import com.nukateam.ntgl.common.util.helpers.compatibility.PlayerAnimationHelper;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class PlayerAnimations {
    public static void playMeleeAnimation(Player player, InteractionHand hand) {
        if (Ntgl.playerAnimatorLoaded) {
            var gunData = new GunData(player.getItemInHand(hand), Minecraft.getInstance().player);
            var delay = GunModifierHelper.getMeleeDelay(gunData);
            var cooldown = GunModifierHelper.getMeleeCooldown(gunData);

            var animation = GunModifierHelper.getAnimation(AnimationType.MELEE, gunData);

            PlayerAnimationHelper.playAnim(player, animation, delay + cooldown, hand == InteractionHand.OFF_HAND);
        }
    }

    public static void playReloadAnimation(Player player, InteractionHand hand) {
        if (Ntgl.playerAnimatorLoaded) {
            var reloadDuration = 0;
            var gunData = new GunData(player.getItemInHand(hand), player);
            var reloadTime = GunModifierHelper.getReloadTime(gunData);
            var loadingType = GunModifierHelper.getLoadingType(gunData);

            if(loadingType.equals(LoadingType.PER_CARTRIDGE)){
                var ammoCount =  GunModifierHelper.getMaxAmmo(gunData) - GunStateHelper.getAmmoCount(gunData);

                for (var i = 0; i < ammoCount; i++) {
                    reloadDuration += reloadTime;
                }
            }
            else reloadDuration = reloadTime;

            var reloadAnimation =  GunModifierHelper.getAnimation(AnimationType.RELOAD, gunData);
            PlayerAnimationHelper.playAnim(player, reloadAnimation, reloadDuration, hand == InteractionHand.OFF_HAND);
        }
    }
}
