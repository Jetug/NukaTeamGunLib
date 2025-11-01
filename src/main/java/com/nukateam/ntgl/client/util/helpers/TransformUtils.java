package com.nukateam.ntgl.client.util.helpers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static net.minecraft.world.item.ItemDisplayContext.*;

public class TransformUtils {
    private static final Set<ItemDisplayContext> NON_HAND_TRANSFORM_TYPES = Collections.unmodifiableSet(EnumSet.of(ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED));

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

    public static boolean isThirdPerson(ItemDisplayContext transformType) {
        return transformType == THIRD_PERSON_RIGHT_HAND || transformType == THIRD_PERSON_LEFT_HAND;
    }

    public static boolean isNonHand(ItemDisplayContext transformType) {
        return NON_HAND_TRANSFORM_TYPES.contains(transformType);
    }
}
