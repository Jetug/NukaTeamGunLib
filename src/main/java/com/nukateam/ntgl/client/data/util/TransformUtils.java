package com.nukateam.ntgl.client.data.util;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;

import static net.minecraft.world.item.ItemDisplayContext.*;

public class TransformUtils {
    public static boolean isHandTransform(ItemDisplayContext transformType){
        return switch (transformType){
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> true;
            default -> false;
        };
    }

    public static boolean isRightHand(ItemDisplayContext transformType){
        return transformType == FIRST_PERSON_RIGHT_HAND || transformType == THIRD_PERSON_RIGHT_HAND;
    }

    public static boolean isFirstPerson(ItemDisplayContext transformType) {
        return transformType == FIRST_PERSON_RIGHT_HAND || transformType == FIRST_PERSON_LEFT_HAND;
    }
}
