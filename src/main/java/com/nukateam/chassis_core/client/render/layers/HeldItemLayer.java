package com.nukateam.chassis_core.client.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import com.nukateam.ntgl.client.util.handler.WeaponRenderingHandler;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;

import java.util.function.BiFunction;

import static com.nukateam.chassis_core.common.data.constants.Bones.LEFT_HAND;
import static com.nukateam.chassis_core.common.data.constants.Bones.RIGHT_HAND;

public class HeldItemLayer<T extends GeoAnimatable> extends BlockAndItemGeoLayer<T> {
    public HeldItemLayer(GeoRenderer<T> renderer, BiFunction<GeoBone, T, ItemStack> stackForBone) {
        super(renderer, stackForBone, (i, g) -> null);
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                      T animatable, MultiBufferSource bufferSource,
                                      float partialTick, int packedLight, int packedOverlay) {
        if (animatable instanceof LivingEntity livingEntity) {
            var actualShooter = livingEntity;
            if (livingEntity instanceof WearableChassis chassis && chassis.getFirstPassenger() instanceof LivingEntity passenger) {
                actualShooter = passenger;
            }

            boolean isLeftHand = bone.getName().equals(LEFT_HAND);

            if (stack.getItem() instanceof IWeapon) {
                poseStack.pushPose();
                {
                    var isWeapon = stack.getItem() instanceof WeaponItem;

                    if (isWeapon) {
//                        var globalId = new ResourceLocation("nukacraft", "global_offsets");
//                        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
//                        var global = PAWeaponOffsets.get(globalId);
//                        var offset = PAWeaponOffsets.get(id);
//
//                        float dx = 0;
//                        float dy = 0;
//                        float dz = 0;
//                        float rx = 0;
//                        float ry = 0;
//                        float rz = 0;
//
//                        var isDebuggingItem = Ntgl.isDebugging() && ClientDebug.currentlyTuningItem != null && ClientDebug.currentlyTuningItem.equals(id);
//
//                        if (isDebuggingItem) {
//                            dx = (ClientDebug.X + ClientDebug.gX) / 16.0F;
//                            dy = (ClientDebug.Y + ClientDebug.gY) / 16.0F;
//                            dz = (ClientDebug.Z + ClientDebug.gZ) / 16.0F;
//                            rx = ClientDebug.RX + ClientDebug.gRX;
//                            ry = ClientDebug.RY + ClientDebug.gRY;
//                            rz = ClientDebug.RZ + ClientDebug.gRZ;
//                        } else {
//                            float ux = offset != null ? offset.x : 0;
//                            float uy = offset != null ? offset.y : 0;
//                            float uz = offset != null ? offset.z : 0;
//                            float urx = offset != null ? offset.rx : 0;
//                            float ury = offset != null ? offset.ry : 0;
//                            float urz = offset != null ? offset.rz : 0;
//
//                            float gx = global != null ? global.x : 0;
//                            float gy = global != null ? global.y : 0;
//                            float gz = global != null ? global.z : 0;
//                            float grx = global != null ? global.rx : 0;
//                            float gry = global != null ? global.ry : 0;
//                            float grz = global != null ? global.rz : 0;
//
//                            dx = (ux + gx) / 16.0F;
//                            dy = (uy + gy) / 16.0F;
//                            dz = (uz + gz) / 16.0F;
//                            rx = urx + grx;
//                            ry = ury + gry;
//                            rz = urz + grz;
//                        }
//
//                        poseStack.mulPose(Axis.XP.rotationDegrees(rx));
//                        poseStack.mulPose(Axis.YP.rotationDegrees(ry));
//                        poseStack.mulPose(Axis.ZP.rotationDegrees(rz));
                        poseStack.translate(-7.5 / 16d, -8.0 / 16d, -15.5 / 16d);
                        WeaponRenderingHandler.get().renderWeapon(
                                actualShooter, stack,
                                isLeftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                                poseStack, bufferSource, packedLight
                        );
                    } else {
                        Minecraft.getInstance().getItemRenderer().renderStatic(
                                actualShooter, stack,
                                isLeftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                                isLeftHand, poseStack, bufferSource, actualShooter.level(), packedLight,
                                OverlayTexture.NO_OVERLAY,
                                actualShooter.getId()
                        );
                    }
                }
                poseStack.popPose();
            } else {
                poseStack.pushPose();
                {
                    super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                }
                poseStack.popPose();
            }
        }
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, GeoAnimatable animatable) {
        return switch (bone.getName()) {
            case RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            case LEFT_HAND -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            default -> ItemDisplayContext.NONE;
        };
    }
}
