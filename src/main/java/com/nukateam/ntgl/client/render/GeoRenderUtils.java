package com.nukateam.ntgl.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.Minecraft;
import software.bernie.geckolib.util.ClientUtil;

public class GeoRenderUtils {
    public static void renderArm(PoseStack poseStack, GeoBone bone, int packedLight,
                                 MultiBufferSource bufferSource, boolean right) {
        var mc = Minecraft.getInstance();
        var playerModel = mc.getEntityModels().bakeLayer(ModelLayers.PLAYER);
        applyBoneTransform(poseStack, bone);
        var playerSkin = ((LocalPlayer) ClientUtil.getClientPlayer()).getSkin().texture();

        HumanoidModel<Player> armorModelOuter;
        armorModelOuter = new HumanoidModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));

        var handModel = right ? "right_arm" : "left_arm";
        var sleeveModel = right ? "right_sleeve" : "left_sleeve";

        renderHand(playerModel.getChild(handModel), playerSkin, poseStack, bufferSource, packedLight);
        renderHand(playerModel.getChild(sleeveModel), playerSkin, poseStack, bufferSource, packedLight);

        poseStack.pushPose();
        poseStack.translate(0, -24 / 16d, 0);
        poseStack.scale(2f, 2f, 2f);
        renderArmorOnHand(mc.player, EquipmentSlot.CHEST, armorModelOuter,
                poseStack, bufferSource, packedLight, right);
        poseStack.popPose();
    }

    private static void renderArmorOnHand(Player player, EquipmentSlot slot,
                                          HumanoidModel<Player> armorModelOuter,
                                          PoseStack poseStack, MultiBufferSource bufferSource,
                                          int packedLight, boolean isRight) {

        ItemStack armorStack = player.getItemBySlot(slot);

        if (armorStack.getItem() instanceof ArmorItem armorItem) {
            armorModelOuter.setAllVisible(false);
            armorModelOuter.rightArm.visible = isRight;
            armorModelOuter.leftArm.visible = !isRight;

            var model = getArmorModelHook(player, armorStack, slot, armorModelOuter);
            var armormaterial = armorItem.getMaterial().value();
            var extensions = IClientItemExtensions.of(armorStack);
            var fallbackColor = extensions.getDefaultDyeColor(armorStack);

            for (int layerIdx = 0; layerIdx < armormaterial.layers().size(); layerIdx++) {
                var layer = armormaterial.layers().get(layerIdx);
                int j = extensions.getArmorLayerTintColor(armorStack, player, layer, layerIdx, fallbackColor);
                if (j != 0) {
                    var texture = ClientHooks.getArmorTexture(player, armorStack, layer, false, slot);

                    renderModel(poseStack, bufferSource, packedLight, model, j, texture);
                }
            }

            if (armorStack.hasFoil()) {
                renderGlint(poseStack, bufferSource, packedLight, model);
            }
        }
    }

    private static void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,  net.minecraft.client.model.Model model) {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY);
    }

    protected static <T extends LivingEntity, A extends HumanoidModel<T>> Model getArmorModelHook(
            LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, A model) {
        return ClientHooks.getArmorModel(entity, itemStack, slot, model);
    }

    private static void renderModel(PoseStack p_289664_, MultiBufferSource p_289689_, int p_289681_, net.minecraft.client.model.Model p_289658_, int p_350798_, ResourceLocation p_324344_) {
        VertexConsumer vertexconsumer = p_289689_.getBuffer(RenderType.armorCutoutNoCull(p_324344_));
        p_289658_.renderToBuffer(p_289664_, vertexconsumer, p_289681_, OverlayTexture.NO_OVERLAY, p_350798_);
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

    private static void renderHand(ModelPart handPart,
                                   ResourceLocation texture, PoseStack poseStack,
                                   MultiBufferSource bufferSource, int packedLight) {

        handPart.resetPose();

        handPart.xRot = 0f;
        handPart.yRot = 0f;
        handPart.zRot = 0f;

        handPart.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(texture)),
                packedLight, OverlayTexture.NO_OVERLAY);
    }
}