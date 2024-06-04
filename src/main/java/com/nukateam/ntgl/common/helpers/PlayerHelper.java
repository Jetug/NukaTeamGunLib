package com.nukateam.ntgl.common.helpers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class PlayerHelper {
    public static HumanoidArm convertHand(InteractionHand hand){
        return hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }
}
