package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.client.model.gibs.ModelGibsGeo;
import com.nukateam.ntgl.common.foundation.entity.FlyingGibs;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

import static com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect.getGoreData;

public class RenderFlyingGibs extends EntityRenderer<FlyingGibs> {
    public RenderFlyingGibs(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(FlyingGibs pEntity, float pEntityYaw, float pPartialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var entity = pEntity.getLocalEntity();
        if(entity == null) return;

        var data = getGoreData(entity);

        if (data.model != null) {
            poseStack.pushPose();
            {
                var render = ClientProxy.getEntityRenderer(entity);
                if (render instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                    try {
                        if (data.texture == null) {
                            data.texture = render.getTextureLocation(entity);
                        }

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                else if(render instanceof GeoEntityRenderer geoRenderer && entity instanceof GeoAnimatable animatable){
                    if (data.texture == null) {
                        var geoModel = geoRenderer.getGeoModel();
                        data.texture = geoModel.getTextureResource(animatable);
                    }
                }

                var partialTickTime = Minecraft.getInstance().getFrameTime();

                float angle;
                float rot_angle = 90.0f;

                if (pEntity.onGround()) {
                    angle = 5 + ((float) pEntity.hitGroundTTL / (float) pEntity.maxTimeToLive) * 15.0f;
                    rot_angle += ((float) (pEntity.maxTimeToLive - pEntity.hitGroundTTL) * angle);

                    if (pEntity.timeToLive <= 20) {
                        float offsetY = ((20 - pEntity.timeToLive) + partialTickTime) * -0.05f;
                        poseStack.translate(0.0f, offsetY, 0.0f);
                    }

                } else {
                    angle = 5 + ((float) pEntity.timeToLive / (float) pEntity.maxTimeToLive) * 15.0f;
                    rot_angle += ((float) pEntity.tickCount + partialTickTime) * angle;
                }

//                poseStack.rotate(rot_angle, (float) pEntity.rotationAxis.x, (float) pEntity.rotationAxis.y,
//                        (float) pEntity.rotationAxis.z);

//                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
//                GlStateManager.disableCull();

                var texture = data.texture;
                var rendertype = RenderType.itemEntityTranslucentCull(texture);
                var vertexConsumer = buffer.getBuffer(rendertype);

                poseStack.mulPose(Axis.ZP.rotationDegrees(180));

//                poseStack.mulPose(Axis.XP.rotationDegrees((float)(rot_angle * pEntity.rotationAxis.x)));
//                poseStack.mulPose(Axis.YP.rotationDegrees((float)(rot_angle * pEntity.rotationAxis.y)));
//                poseStack.mulPose(Axis.ZP.rotationDegrees((float)(rot_angle * pEntity.rotationAxis.z)));
                poseStack.translate(0,-entity.getType().getHeight() / 2,0);

//                poseStack.mulPose(new Quaternionf(pEntity.rotationAxis.x, pEntity.rotationAxis.y, pEntity.rotationAxis.z, rot_angle));

                data.model.render(entity, pEntity.getPartId(), poseStack, rendertype, buffer, vertexConsumer, packedLight, 0xFFFFFF);

                super.render(pEntity, pEntityYaw, pPartialTick, poseStack, buffer, packedLight);
            }
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingGibs entity) {
        var render = ClientProxy.getEntityRenderer(entity.getLocalEntity());
        return render.getTextureLocation(entity.getLocalEntity());
//        return getGoreData(entity.entity).texture;
    }
}
