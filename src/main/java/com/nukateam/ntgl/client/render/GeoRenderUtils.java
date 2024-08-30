package com.nukateam.ntgl.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.GeoBone;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;

public class GeoRenderUtils {
    public static void renderRightArm(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay,
                                       PlayerModel<AbstractClientPlayer> playerEntityModel, VertexConsumer arm, VertexConsumer sleeve) {
        playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightArm.setRotation(0, 0, 0);
        playerEntityModel.rightArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

        playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightSleeve.setRotation(0, 0, 0);
        playerEntityModel.rightSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
    }

    public static void renderLeftArm(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay,
                                      PlayerModel<AbstractClientPlayer> playerEntityModel, VertexConsumer arm, VertexConsumer sleeve) {
        playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftArm.setRotation(0, 0, 0);
        playerEntityModel.leftArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);
//
//        playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
//        playerEntityModel.leftSleeve.setRotation(0, 0, 0);
//        playerEntityModel.leftSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
    }
}
