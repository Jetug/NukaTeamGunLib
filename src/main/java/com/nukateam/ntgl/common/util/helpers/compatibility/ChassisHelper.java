package com.nukateam.ntgl.common.util.helpers.compatibility;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

public class ChassisHelper {
    public static boolean isPlayerInChassis(){
        return PlayerUtils.isLocalWearingChassis();
    }

    public static void renderChassisHand(PoseStack poseStack, boolean isRight, HumanoidArm hand, int packedLight){
        PlayerUtils.renderChassisHand(poseStack, isRight, hand, packedLight);
    }
}
