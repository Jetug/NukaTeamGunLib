package com.nukateam.ntgl.common.util.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class PlayerHelper {
    public static HumanoidArm convertHand(InteractionHand hand){
        return hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    public static boolean isRight(InteractionHand hand){
        var mainHand = Minecraft.getInstance().options.mainHand().get();
        return mainHand == HumanoidArm.RIGHT ?
                hand == InteractionHand.MAIN_HAND :
                hand == InteractionHand.OFF_HAND;
    }

    public static InteractionHand convertHand(HumanoidArm arm){
        return arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static InteractionHand getOpposite(InteractionHand arm) {
        return arm == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
}
