package com.nukateam.ntgl.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nukateam.ntgl.client.util.ClientDebug;
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
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import software.bernie.geckolib.util.ClientUtil;

public class GeoRenderUtils {


    public static void renderRightArm(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay,
                                      VertexConsumer arm, VertexConsumer sleeve, MultiBufferSource bufferSource) {
//        var playerEntityModel = getPlayerModel();
//        playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
//        playerEntityModel.rightArm.setRotation(0, 0, 0);
//        playerEntityModel.rightArm.render(poseStack, arm, packedLight, packedOverlay);
//
//        playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
//        playerEntityModel.rightSleeve.setRotation(0, 0, 0);
//        playerEntityModel.rightSleeve.render(poseStack, sleeve, packedLight, packedOverlay);

        var mc = Minecraft.getInstance();
        var playerModel = mc.getEntityModels().bakeLayer(
                net.minecraft.client.model.geom.ModelLayers.PLAYER);
        applyBoneTransform(poseStack, bone);
        var playerSkin = ((LocalPlayer) ClientUtil.getClientPlayer()).getSkin().texture();

        HumanoidModel<Player> armorModelOuter;

        try {
            armorModelOuter = new HumanoidModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        } catch (Exception e) {
            armorModelOuter = new HumanoidModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));
        }

        renderHand(playerModel.getChild("right_arm"), playerSkin, poseStack,
                bufferSource, packedLight);
        renderHand(playerModel.getChild("right_sleeve"), playerSkin, poseStack,
                bufferSource, packedLight);

        poseStack.pushPose();
        poseStack.translate(0, -24 / 16d, 0);

//        poseStack.translate(-3 / 16d, -12 / 16d, 0);
        var scale = 2f;
        poseStack.scale(scale, scale, scale);
        renderArmorOnHand(mc.player, EquipmentSlot.CHEST, armorModelOuter,
                poseStack, bufferSource, packedLight, true);
        poseStack.popPose();
    }

    private static void renderArmorOnHand(Player player, EquipmentSlot slot,
                                          HumanoidModel<Player> armorModelOuter,
                                          PoseStack poseStack, MultiBufferSource bufferSource,
                                          int packedLight, boolean isRight) {

        ItemStack armorStack = player.getItemBySlot(slot);

        if (armorStack.getItem() instanceof ArmorItem armorItem) {
            setPartVisibility(armorModelOuter, slot, isRight);
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

//            ArmorTrim armortrim = armorStack.get(DataComponents.TRIM);
//            if (armortrim != null) {
//                this.renderTrim(armorItem.getMaterial(), poseStack, bufferSource, packedLight, armortrim, model, false);
//            }

            if (armorStack.hasFoil()) {
                renderGlint(poseStack, bufferSource, packedLight, model);
            }
        }
    }

    private static void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,  net.minecraft.client.model.Model model) {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY);
    }

//    private void renderTrim(
//            Holder<ArmorMaterial> p_323506_, PoseStack p_289687_, MultiBufferSource p_289643_, int p_289683_, ArmorTrim p_289692_, net.minecraft.client.model.Model p_289663_, boolean p_289651_
//    ) {
//        TextureAtlasSprite textureatlassprite = this.armorTrimAtlas
//                .getSprite(p_289651_ ? p_289692_.innerTexture(p_323506_) : p_289692_.outerTexture(p_323506_));
//        VertexConsumer vertexconsumer = textureatlassprite.wrap(p_289643_.getBuffer(Sheets.armorTrimsSheet(p_289692_.pattern().value().decal())));
//        p_289663_.renderToBuffer(p_289687_, vertexconsumer, p_289683_, OverlayTexture.NO_OVERLAY);
//    }


    protected static <T extends LivingEntity, A extends HumanoidModel<T>> void setPartVisibility(A model, EquipmentSlot slot, boolean isRight) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD:
                model.head.visible = true;
                model.hat.visible = true;
                break;
            case CHEST:
                model.rightArm.visible = isRight;
                model.leftArm.visible = !isRight;
                break;
            case LEGS:
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            case FEET:
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
        }
    }

    protected static <T extends LivingEntity, A extends HumanoidModel<T>> Model getArmorModelHook(
            LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, A model) {
        return ClientHooks.getArmorModel(entity, itemStack, slot, model);
    }

    private static void renderModel(PoseStack p_289664_, MultiBufferSource p_289689_, int p_289681_, net.minecraft.client.model.Model p_289658_, int p_350798_, ResourceLocation p_324344_) {
        VertexConsumer vertexconsumer = p_289689_.getBuffer(RenderType.armorCutoutNoCull(p_324344_));
        p_289658_.renderToBuffer(p_289664_, vertexconsumer, p_289681_, OverlayTexture.NO_OVERLAY, p_350798_);
    }

//    private static ResourceLocation getArmorTexture(ItemStack armorStack, EquipmentSlot slot, boolean inner) {
//        ArmorItem armorItem = (ArmorItem) armorStack.getItem();
//        String material = armorItem.getMaterial().getRegisteredName();
//        String type = slot == EquipmentSlot.LEGS ? "leggings" : "chestplate";
//
//        // Формируем путь к текстуре брони
//        String texturePath = "textures/models/armor/" + material + "_layer_" + (inner ? "1" : "2") + ".png";
//
//        // Для разных слотов могут быть разные текстуры
//        if (slot == EquipmentSlot.LEGS) {
//            texturePath = "textures/models/armor/" + material + "_layer_2.png";
//        }
//
//        return ResourceLocation.tryBuild("minecraft", texturePath);
//    }

    public static void renderLeftArm(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay,
                                     VertexConsumer arm, VertexConsumer sleeve, MultiBufferSource bufferSource) {
        var playerEntityModel = getPlayerModel();
        playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftArm.setRotation(0, 0, 0);
        playerEntityModel.leftArm.render(poseStack, arm, packedLight, packedOverlay);

        playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftSleeve.setRotation(0, 0, 0);
        playerEntityModel.leftSleeve.render(poseStack, sleeve, packedLight, packedOverlay);
    }


    public static PlayerModel<AbstractClientPlayer> getPlayerModel() {
        var client = Minecraft.getInstance();
        var playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
        return playerEntityRenderer.getModel();
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

        // Масштабируем обратно, так как кубы обычно меньше рук
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