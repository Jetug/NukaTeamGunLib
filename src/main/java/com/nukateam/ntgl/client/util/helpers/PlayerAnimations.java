package com.nukateam.ntgl.client.util.helpers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.data.holders.LoadingType;
import com.nukateam.ntgl.common.util.helpers.compatibility.PlayerAnimationHelper;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class PlayerAnimations {
    public static void playFireAnimation(Player player, InteractionHand hand) {
        if (Ntgl.playerAnimatorLoaded) {
            var gunData = new WeaponData(player.getItemInHand(hand), Minecraft.getInstance().player);
            var rate = WeaponModifierHelper.getRate(gunData);
            var animation = WeaponModifierHelper.getAnimation(AnimationType.FIRE, gunData);

            PlayerAnimationHelper.playAnim((AbstractClientPlayer)player, animation, rate, hand == InteractionHand.OFF_HAND);
        }
    }

    public static void playMeleeAnimation(Player player, InteractionHand hand) {
        if (Ntgl.playerAnimatorLoaded) {
            var gunData = new WeaponData(player.getItemInHand(hand), Minecraft.getInstance().player);
            var delay = WeaponModifierHelper.getMeleeDelay(gunData);
            var cooldown = WeaponModifierHelper.getMeleeCooldown(gunData);

            var animation = WeaponModifierHelper.getAnimation(AnimationType.MELEE, gunData);

            PlayerAnimationHelper.playAnim((AbstractClientPlayer)player, animation, delay + cooldown, hand == InteractionHand.OFF_HAND);
        }
    }

    public static void playReloadAnimation(Player player, InteractionHand hand) {
        if (Ntgl.playerAnimatorLoaded) {
            var reloadDuration = 0;
            var gunData = new WeaponData(player.getItemInHand(hand), player);
            var reloadTime = WeaponModifierHelper.getReloadTime(gunData);
            var loadingType = WeaponModifierHelper.getLoadingType(gunData);

            if(loadingType.equals(LoadingType.PER_CARTRIDGE)){
                var ammoCount =  WeaponModifierHelper.getMaxAmmo(gunData) - WeaponStateHelper.getAmmoCount(gunData);

                for (var i = 0; i < ammoCount; i++) {
                    reloadDuration += reloadTime;
                }
            }
            else reloadDuration = reloadTime;

            var reloadAnimation =  WeaponModifierHelper.getAnimation(AnimationType.RELOAD, gunData);
            PlayerAnimationHelper.playAnim((AbstractClientPlayer)player, reloadAnimation, reloadDuration, hand == InteractionHand.OFF_HAND);
        }
    }
}
