package com.nukateam.ntgl.client.registry;

import com.nukateam.ntgl.client.util.helpers.PlayerAnimations;
import com.nukateam.ntgl.common.data.holders.AnimationType;

public class AnimationRegistry {
    public static void register(){
        AnimationType.FIRE.setAnimation(PlayerAnimations::playFireAnimation);
        AnimationType.RELOAD.setAnimation(PlayerAnimations::playReloadAnimation);
        AnimationType.MELEE.setAnimation(PlayerAnimations::playMeleeAnimation);
    }
}
