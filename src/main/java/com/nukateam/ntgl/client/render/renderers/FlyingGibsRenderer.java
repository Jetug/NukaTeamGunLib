package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.data.util.Rgba;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.GeoReplacedEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.ntgl.client.render.renderers.DeathEffectEntityRenderer.MAX_DEATH_TIME;
import static com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect.getGoreData;

public class FlyingGibsRenderer extends EntityRenderer<FlyingGib> {
    public FlyingGibsRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(FlyingGib flyingGib, float pEntityYaw, float pPartialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        var entity = flyingGib.getLocalEntity();
        if(entity == null) return;

        var data = getGoreData(entity);

        if (data.model != null) {
            poseStack.pushPose();
            {
                var render = ClientProxy.getEntityRenderer(entity);
                if (render instanceof LivingEntityRenderer<?, ?>) {
                    try {
                        if (data.texture == null)
                            data.texture = render.getTextureLocation(entity);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                }
                else if(render instanceof GeoRenderer geoRenderer && entity instanceof GeoAnimatable animatable){
                    if (data.texture == null) {
                        var geoModel = geoRenderer.getGeoModel();
                        data.texture = geoModel.getTextureResource(animatable);
                    }
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                }

                var partialTickTime = Minecraft.getInstance().getFrameTime();

                float angle;
                float rot_angle = 90.0f;

                if (flyingGib.onGround()) {
                    angle = 5 + ((float) flyingGib.hitGroundTTL / (float) flyingGib.maxTimeToLive) * 15.0f;
                    rot_angle += ((float) (flyingGib.maxTimeToLive - flyingGib.hitGroundTTL) * angle);

                    if (flyingGib.timeToLive <= 20) {
                        float offsetY = ((20 - flyingGib.timeToLive) + partialTickTime) * -0.05f;
                        poseStack.translate(0.0f, offsetY, 0.0f);
                    }

                } else {
                    angle = 5 + ((float) flyingGib.timeToLive / (float) flyingGib.maxTimeToLive) * 15.0f;
                    rot_angle += ((float) flyingGib.tickCount + partialTickTime) * angle;
                }

//                poseStack.rotate(rot_angle, (float) flyingGib.rotationAxis.x, (float) flyingGib.rotationAxis.y,
//                        (float) flyingGib.rotationAxis.z);

//                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
//                GlStateManager.disableCull();

                var texture = data.texture;
                var rendertype = RenderType.itemEntityTranslucentCull(texture);
                var vertexConsumer = buffer.getBuffer(rendertype);

//                poseStack.mulPose(Axis.ZP.rotationDegrees(180));

//
                poseStack.translate(0,-entity.getType().getHeight() / 2,0);

//                poseStack.mulPose(new Quaternionf(flyingGib.rotationAxis.x, flyingGib.rotationAxis.y, flyingGib.rotationAxis.z, rot_angle));

                var prog = ((float) entity.deathTime / (float) MAX_DEATH_TIME);
                var mainAlpha = 1.0f - prog;
                var scale = 1.0f + prog / 2;
                var rgba = Rgba.DEFAULT;


                switch (flyingGib.getData().deathType){
                    case LASER -> {
                        poseStack.scale(scale, scale, scale);
                        poseStack.translate(0, -scale / 2, 0);
                        rgba = rgba.setAlpha(mainAlpha);
                    }
                    case GORE -> {
                        poseStack.mulPose(Axis.XP.rotationDegrees(prog * (float) flyingGib.rotationAxis.x));
                        poseStack.mulPose(Axis.YP.rotationDegrees(prog * (float) flyingGib.rotationAxis.y));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(prog * (float) flyingGib.rotationAxis.z));
                    }
                }

                data.model.render(entity, flyingGib.getPartId(), poseStack, rendertype, buffer,
                        vertexConsumer, packedLight, 0xFFFFFF, rgba);


                super.render(flyingGib, pEntityYaw, pPartialTick, poseStack, buffer, packedLight);
            }
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingGib entity) {
        var render = ClientProxy.getEntityRenderer(entity.getLocalEntity());
        return render.getTextureLocation(entity.getLocalEntity());
//        return getGoreData(entity.entity).texture;
    }
}
