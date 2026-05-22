package com.nukateam.ntgl.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.Minecraft;

import java.lang.reflect.InvocationTargetException;

import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class GeoRenderUtils {
    public static void renderArm(PoseStack poseStack2, GeoBone bone, int packedLight,
                                 MultiBufferSource bufferSource, int light, float partialTick, HumanoidArm arm) {
        var minecraft = Minecraft.getInstance();

//        minecraft.gameRenderer.resetProjectionMatrix(minecraft.gameRenderer.getProjectionMatrix(minecraft.gameRenderer.getFov(camera, partialTick, false)));

        var camera = minecraft.gameRenderer.getMainCamera();
        var quaternionf = camera.rotation().conjugate(new Quaternionf());
        var projectionMatrix = new Matrix4f().rotation(quaternionf);
        var posestack = new PoseStack();

        posestack.pushPose();
        {
            posestack.mulPose(projectionMatrix.invert(new Matrix4f()));
            var matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix().mul(projectionMatrix);
            {
                RenderSystem.applyModelViewMatrix();

//        this.bobHurt(posestack, partialTick);
//        if (this.minecraft.options.bobView().get()) {
//            this.bobView(posestack, partialTick);
//        }

                var poseStack = new PoseStack();
                applyBoneTransform(poseStack, bone);
                poseStack.pushPose();
                {
//            poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
//            renderFirstPersonArm(Minecraft.getInstance().player, arm, poseStack, bufferSource, light);
                    renderHand(Minecraft.getInstance().player, HumanoidArm.RIGHT, poseStack, bufferSource, partialTick, light);

                }
                poseStack.popPose();

//            boolean flag = minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity) minecraft.getCameraEntity()).isSleeping();
//            if (minecraft.options.getCameraType().isFirstPerson()
//                    && !flag
//                    && !minecraft.options.hideGui
//                    && minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
//                lightTexture.turnOnLightLayer();
//                itemInHandRenderer
//                        .renderHandsWithItems(
//                                partialTick,
//                                posestack,
//                                renderBuffers.bufferSource(),
//                                minecraft.player,
//                                minecraft.getEntityRenderDispatcher().getPackedLightCoords(minecraft.player, partialTick)
//                        );
//                lightTexture.turnOffLightLayer();
//            }


            }
            matrix4fstack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
        posestack.popPose();
//        if (minecraft.options.getCameraType().isFirstPerson() && !flag) {
//            ScreenEffectRenderer.renderScreenEffect(minecraft, posestack);
//        }
    }

    public static void renderFirstPersonArm(
            LocalPlayer player, HumanoidArm hand, PoseStack poseStack,
            MultiBufferSource bufferSource, int combinedLight
    ) {
        var mc = Minecraft.getInstance();
        var renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);

        poseStack.pushPose();

        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(poseStack, bufferSource, combinedLight, player);
            renderHand(player, hand, poseStack, bufferSource, mc.getTimer().getRealtimeDeltaTicks(), combinedLight);
        } else {
            renderer.renderLeftHand(poseStack, bufferSource, combinedLight, player);
            renderHand(player, hand, poseStack, bufferSource, mc.getTimer().getRealtimeDeltaTicks(), combinedLight);
        }

        poseStack.popPose();
    }

//    public static void renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, float partialTicks, AbstractClientPlayer player) {
//        renderHand(player, poseStack, buffer, partialTicks, combinedLight);
//    }

    public static void renderHand(AbstractClientPlayer player, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, int light) {
        poseStack.pushPose();
        {
            poseStack.translate(0 / 10d / 16d, 160 / 10d / 16d, 0 / 10d / 16d);
            poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
            var mc = Minecraft.getInstance();
            float f1 = Mth.lerp(partialTicks, player.xRotO, player.getXRot());

            var hand = arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

            try {
                var renderer = ItemInHandRenderer.class.getDeclaredMethod("renderArmWithItem", AbstractClientPlayer.class, float.class, float.class,
                        InteractionHand.class, float.class,
                        ItemStack.class, float.class,
                        PoseStack.class, MultiBufferSource.class, int.class);

                renderer.setAccessible(true);

                renderer.invoke(mc.gameRenderer.itemInHandRenderer, mc.player, partialTicks, f1, hand, 0, ItemStack.EMPTY, 1.0F, poseStack, buffer, light);
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        }
        poseStack.popPose();

//        var mc = Minecraft.getInstance();
//        var playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
//        poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
//        playerRenderer.renderRightHand(poseStack, buffer, light, player);
    }

    public static void renderRightHand2(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player) {
        if(!net.neoforged.neoforge.client.ClientHooks.renderSpecificFirstPersonArm(poseStack, buffer, combinedLight, player, HumanoidArm.RIGHT))
            renderHand(renderer, poseStack, buffer, combinedLight, player, renderer.getModel().rightArm, renderer.getModel().rightSleeve);
    }

    private static  void renderHand(PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                            AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear
    ) {
        PlayerModel<AbstractClientPlayer> playermodel = renderer.getModel();
        setModelProperties(renderer, player);
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        playermodel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rendererArm.xRot = 0.0F;
        ResourceLocation resourcelocation = player.getSkin().texture();
        rendererArm.render(poseStack, buffer.getBuffer(RenderType.entitySolid(resourcelocation)), combinedLight, OverlayTexture.NO_OVERLAY);
        rendererArmwear.xRot = 0.0F;
        rendererArmwear.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(resourcelocation)), combinedLight, OverlayTexture.NO_OVERLAY);
    }

    public static void render(ModelPart modelPart, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (modelPart.visible) {
            if (!modelPart.cubes.isEmpty() || !modelPart.children.isEmpty()) {
                poseStack.pushPose();
                modelPart.translateAndRotate(poseStack);
                if (!modelPart.skipDraw) {
                    compile(modelPart, poseStack.last(), buffer, packedLight, packedOverlay, -1);
                }

                for (ModelPart modelpart : modelPart.children.values()) {
                    render(modelpart, poseStack, buffer, packedLight, packedOverlay, color);
                }

                poseStack.popPose();
            }
        }
    }

    private static void compile(ModelPart modelPart, PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart.Cube modelpart$cube : modelPart.cubes) {
            modelpart$cube.compile(pose, buffer, packedLight, packedOverlay, color);
        }
    }

//    public void compile(ModelPart.Cube cube, PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
//        Matrix4f matrix4f = pose.pose();
//        Vector3f vector3f = new Vector3f();
//
//        for (var modelpart$polygon : cube.polygons) {
//            Vector3f vector3f1 = pose.transformNormal(modelpart$polygon.normal, vector3f);
//            float f = vector3f1.x();
//            float f1 = vector3f1.y();
//            float f2 = vector3f1.z();
//
//            for (ModelPart.Vertex modelpart$vertex : modelpart$polygon.vertices) {
//                float f3 = modelpart$vertex.pos.x() / 16.0F;
//                float f4 = modelpart$vertex.pos.y() / 16.0F;
//                float f5 = modelpart$vertex.pos.z() / 16.0F;
//                Vector3f vector3f2 = matrix4f.transformPosition(f3, f4, f5, vector3f);
//                buffer.addVertex(
//                        vector3f2.x(), vector3f2.y(), vector3f2.z(), color, modelpart$vertex.u, modelpart$vertex.v, packedOverlay, packedLight, f, f1, f2
//                );
//            }
//        }
//    }

    private static void setModelProperties(PlayerRenderer renderer, AbstractClientPlayer clientPlayer) {
        PlayerModel<AbstractClientPlayer> playermodel = renderer.getModel();
        if (clientPlayer.isSpectator()) {
            playermodel.setAllVisible(false);
            playermodel.head.visible = true;
            playermodel.hat.visible = true;
        } else {
            playermodel.setAllVisible(true);
            playermodel.hat.visible = clientPlayer.isModelPartShown(PlayerModelPart.HAT);
            playermodel.jacket.visible = clientPlayer.isModelPartShown(PlayerModelPart.JACKET);
            playermodel.leftPants.visible = clientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.rightPants.visible = clientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.leftSleeve.visible = clientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playermodel.rightSleeve.visible = clientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            playermodel.crouching = clientPlayer.isCrouching();
            HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(clientPlayer, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(clientPlayer, InteractionHand.OFF_HAND);
            if (humanoidmodel$armpose.isTwoHanded()) {
                humanoidmodel$armpose1 = clientPlayer.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            if (clientPlayer.getMainArm() == HumanoidArm.RIGHT) {
                playermodel.rightArmPose = humanoidmodel$armpose;
                playermodel.leftArmPose = humanoidmodel$armpose1;
            } else {
                playermodel.rightArmPose = humanoidmodel$armpose1;
                playermodel.leftArmPose = humanoidmodel$armpose;
            }
        }
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useanim == UseAnim.CROSSBOW && hand == player.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (useanim == UseAnim.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (useanim == UseAnim.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            } else if (!player.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
            HumanoidModel.ArmPose forgeArmPose = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemstack).getArmPose(player, hand, itemstack);
            if (forgeArmPose != null) return forgeArmPose;

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    private static void applyBoneTransform(PoseStack poseStack, GeoBone bone) {
        poseStack.translate(
                bone.getPivotX() / 16f,
                bone.getPivotY() / 16f,
                bone.getPivotZ() / 16f
        );

        poseStack.mulPose(Axis.XP.rotationDegrees(bone.getRotX()));
        poseStack.mulPose(Axis.YP.rotationDegrees(bone.getRotY()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(bone.getRotZ()));

        poseStack.scale(1.0f, 1.0f, 1.0f);
    }
}