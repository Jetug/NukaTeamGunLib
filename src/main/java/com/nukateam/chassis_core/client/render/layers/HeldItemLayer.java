package com.nukateam.chassis_core.client.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import com.nukateam.ntgl.client.util.handler.GunRenderingHandler;
import com.nukateam.ntgl.client.util.ClientDebug;
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
            LivingEntity actualShooter = livingEntity;
            if (livingEntity instanceof WearableChassis chassis && chassis.getFirstPassenger() instanceof LivingEntity passenger) {
                actualShooter = passenger;
            }

            var transformType = getTransformTypeForStack(bone, stack, animatable);
            boolean isLeftHand = bone.getName().equals(LEFT_HAND);

            if (stack.getItem() instanceof IWeapon) {
                poseStack.pushPose();
                {

                    var isWeapon = stack.getItem() instanceof com.nukateam.geo.interfaces.DynamicGeoItem;

                    if (isWeapon) {
                        var globalId = ResourceLocation.fromNamespaceAndPath("nukacraft", "global_offsets");
                        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        var global = com.nukateam.ntgl.client.util.PAWeaponOffsets.get(globalId);
                        var offset = com.nukateam.ntgl.client.util.PAWeaponOffsets.get(id);

                        float dx = 0; float dy = 0; float dz = 0;
                        float rx = 0; float ry = 0; float rz = 0;

                        boolean isDebuggingItem = com.nukateam.ntgl.Ntgl.isDebugging() && com.nukateam.ntgl.client.util.ClientDebug.currentlyTuningItem != null && com.nukateam.ntgl.client.util.ClientDebug.currentlyTuningItem.equals(id);
                        
                        if (isDebuggingItem) {
                            dx = (com.nukateam.ntgl.client.util.ClientDebug.X + com.nukateam.ntgl.client.util.ClientDebug.gX) / 16.0F; 
                            dy = (com.nukateam.ntgl.client.util.ClientDebug.Y + com.nukateam.ntgl.client.util.ClientDebug.gY) / 16.0F; 
                            dz = (com.nukateam.ntgl.client.util.ClientDebug.Z + com.nukateam.ntgl.client.util.ClientDebug.gZ) / 16.0F;
                            rx = com.nukateam.ntgl.client.util.ClientDebug.RX + com.nukateam.ntgl.client.util.ClientDebug.gRX; 
                            ry = com.nukateam.ntgl.client.util.ClientDebug.RY + com.nukateam.ntgl.client.util.ClientDebug.gRY; 
                            rz = com.nukateam.ntgl.client.util.ClientDebug.RZ + com.nukateam.ntgl.client.util.ClientDebug.gRZ;
                        } else {
                            float ux = offset != null ? offset.x : 0;
                            float uy = offset != null ? offset.y : 0;
                            float uz = offset != null ? offset.z : 0;
                            float urx = offset != null ? offset.rx : 0;
                            float ury = offset != null ? offset.ry : 0;
                            float urz = offset != null ? offset.rz : 0;
                            
                            float gx = global != null ? global.x : 0;
                            float gy = global != null ? global.y : 0;
                            float gz = global != null ? global.z : 0;
                            float grx = global != null ? global.rx : 0;
                            float gry = global != null ? global.ry : 0;
                            float grz = global != null ? global.rz : 0;

                            dx = (ux + gx) / 16.0F; dy = (uy + gy) / 16.0F; dz = (uz + gz) / 16.0F;
                            rx = urx + grx; ry = ury + gry; rz = urz + grz;
                        }

                        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rx));
                        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(ry));
                        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rz));
                        poseStack.translate(dx, dy, dz);

                        com.nukateam.ntgl.client.util.handler.GunRenderingHandler.get().renderWeapon(
                                actualShooter, stack,
                                isLeftHand ? net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND : net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                                poseStack, bufferSource, packedLight
                        );
                    } else {
                        Minecraft.getInstance().getItemRenderer().renderStatic(
                                actualShooter, stack,
                                isLeftHand ? net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND : net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                                isLeftHand, poseStack, bufferSource, actualShooter.level(), packedLight,
                                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
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
