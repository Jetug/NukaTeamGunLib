package com.nukateam.gunscore.common.data.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LivingEntityUtils {
    public static InteractionHand getInteractionHand(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static ItemStack getItemInHand(LivingEntity entity, HumanoidArm arm) {
        return entity.getItemInHand(getInteractionHand(arm));
    }
}
