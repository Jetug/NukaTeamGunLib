package com.nukateam.ntgl.client.util.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

import static net.minecraft.world.item.ItemDisplayContext.*;

public class TransformUtils {
    public static boolean isHandTransform(ItemDisplayContext transformType){
        return switch (transformType){
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> true;
            default -> false;
        };
    }

    public static InteractionHand getHand(ItemDisplayContext transformType){
        return TransformUtils.isRightHand(transformType) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static boolean isRightHand(ItemDisplayContext transformType){
        return transformType == FIRST_PERSON_RIGHT_HAND || transformType == THIRD_PERSON_RIGHT_HAND;
    }

    public static boolean isFirstPerson(ItemDisplayContext transformType) {
        return transformType == FIRST_PERSON_RIGHT_HAND || transformType == FIRST_PERSON_LEFT_HAND;
    }
}
