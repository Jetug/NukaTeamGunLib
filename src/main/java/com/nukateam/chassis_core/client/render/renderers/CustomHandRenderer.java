package com.nukateam.chassis_core.client.render.renderers;

import com.nukateam.chassis_core.client.animators.HandAnimator;
import com.nukateam.chassis_core.client.model.LeftHandModel;
import com.nukateam.chassis_core.client.model.RightHandModel;
import com.nukateam.chassis_core.client.render.utils.GeoUtils;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.nukateam.chassis_core.common.foundation.entity.Chassis.*;
import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.getLocalPlayerChassis;

public class CustomHandRenderer extends GeoObjectRenderer<HandAnimator> {
    public static final String RIGHT_HAND_BONE = "right_arm_pov";
    public static final String LEFT_HAND_BONE = "left_arm_pov";
    public static final Lazy<RightHandModel> RIGHT_HAND_MODEL = Lazy.of(RightHandModel::new);
    public static final Lazy<LeftHandModel> LEFT_HAND_MODEL = Lazy.of(LeftHandModel::new);
    public static final String RIGHT_FOREARM_ARMOR = "right_forearm_armor";
    public static final String LEFT_FOREARM_ARMOR = "left_forearm_armor";
    protected HumanoidArm arm;

    public CustomHandRenderer() {
        super(new RightHandModel());
//        addRenderLayer(new HandEquipmentLayer<>(this));
    }

    @Override
    public GeoModel<HandAnimator> getGeoModel() {
        return arm == HumanoidArm.RIGHT ? RIGHT_HAND_MODEL.get() : LEFT_HAND_MODEL.get();
    }

    public void render(HumanoidArm arm, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, int packedLight) {
        this.arm = arm;
        super.render(poseStack, getLocalPlayerChassis().getHandEntity(), bufferSource, null, null, packedLight);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, HandAnimator animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay,
                                  int colour) {
        if(PlayerUtils.isLocalWearingChassis() && (Objects.equals(bone.getName(), RIGHT_HAND_BONE) || Objects.equals(bone.getName(), LEFT_HAND_BONE))){
            var chassis = PlayerUtils.getLocalPlayerChassis();
            if(chassis.isEquipmentVisible(getArmorSlot())) {
                renderArmor(poseStack, animatable, chassis, buffer, bufferSource, partialTick, packedLight, packedOverlay, colour);
            }
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer,
                    isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    protected void renderArmor(PoseStack poseStack, HandAnimator animatable, WearableChassis chassis,
                               VertexConsumer buffer, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay,
                               int colour) {
        if(isArmorVisible(chassis)) {
            var armorBones = gerArmor(chassis);

            poseStack.pushPose();
            {
//                var modelPose = animatable.getSecondaryBoneTransform();
//                poseStack.mulPoseMatrix(modelPose.last().pose());
                translateArmor(poseStack);

                for (var bone : armorBones) {
                    renderBone(bone, poseStack, buffer, bufferSource, partialTick, packedLight, packedOverlay, colour);
                }
            }
            poseStack.popPose();
        }
    }

    protected ChassisPart getArmorSlot() {
        return arm == HumanoidArm.RIGHT ? ChassisPart.RIGHT_ARM_ARMOR : ChassisPart.LEFT_ARM_ARMOR;
    }

    protected GeoBone[] gerArmor(WearableChassis chassis) {
        var armor = getAsChassisEquipment(chassis.getEquipment(getArmorSlot()));
        var config = armor.getConfig();
        if (config != null) {
            var armorModel = armor.getConfig().getModel();
            if (arm == HumanoidArm.RIGHT) {
                return new GeoBone[]{GeoUtils.getBone(armorModel, RIGHT_FOREARM_ARMOR)};
            } else {
                return new GeoBone[]{GeoUtils.getBone(armorModel, LEFT_FOREARM_ARMOR)};
            }
        }

        return new GeoBone[0];
    }

    protected boolean isArmorVisible(WearableChassis chassis){
        return chassis.isEquipmentVisible(getArmorSlot());
    }

    protected void translateArmor(PoseStack poseStack){
        if(arm == HumanoidArm.RIGHT) {
            poseStack.translate(-82 / 10D / 16D, -220 / 10D / 16D, 30 / 10D / 16D);
        }
        else {
            poseStack.translate(78 / 10D / 16D, -215 / 10D / 16D, 31 / 10D / 16D);
        }
    }

    private void renderBone(GeoBone armorBone, PoseStack poseStack, VertexConsumer buffer,
                            MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay,
                            int colour) {
        var chassis = PlayerUtils.getLocalPlayerChassis();

        var textureOverride = GeoUtils.getTextureForBone(armorBone, chassis);
        var texture = textureOverride == null ? getTextureLocation(this.animatable) : textureOverride;

        if (texture != null) {
            var renderTypeOverride = getRenderType(this.animatable, texture, bufferSource, partialTick);
            buffer = bufferSource.getBuffer(renderTypeOverride);

            for (var cube : armorBone.getCubes()) {
                poseStack.pushPose();
                {
                    var newCube = new GeoCube(cube.quads(), new Vec3(0, 0, 0),
                            cube.rotation(), cube.size(), cube.inflate(), cube.mirror());
                    renderCube(poseStack, newCube, buffer, packedLight, packedOverlay, colour);
                }
                poseStack.popPose();
            }
            buffer = bufferSource.getBuffer(getRenderType(this.animatable, getTextureLocation(this.animatable), bufferSource, partialTick));
        }
    }
}
