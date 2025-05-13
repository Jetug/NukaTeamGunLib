package com.nukateam.ntgl.common.util.helpers.compatibility;

import com.jetug.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.world.entity.HumanoidArm;

public class ChassisHelper {
    public static boolean isPlayerInChassis(){
        if(Ntgl.chassisCoreLoaded){
            return PlayerUtils.isLocalWearingChassis();
        }
        return false;
    }

    public static void renderChassisHand(PoseStack poseStack, boolean isRight, HumanoidArm hand, int packedLight){
        if(Ntgl.chassisCoreLoaded){
            PlayerUtils.renderChassisHand(poseStack, isRight, hand, packedLight);
        }
    }
}
