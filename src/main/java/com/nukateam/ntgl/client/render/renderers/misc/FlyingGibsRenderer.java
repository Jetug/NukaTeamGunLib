package com.nukateam.ntgl.client.render.renderers.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.ntgl.client.render.renderers.misc.DeathFxRenderer.setupGoreData;
import static com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect.getGoreData;

public class FlyingGibsRenderer extends EntityRenderer<FlyingGib> {
    public static final int MAX_DEATH_TIME = 20;

    public FlyingGibsRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(FlyingGib flyingGib, float pEntityYaw, float pPartialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        var entity = flyingGib.getLocalEntity();
        if(entity == null) return;

        var data = flyingGib.getData(); //getGoreData(entity);
//        setupGoreData(entity, data);

        var isGeoModel = false;

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
                    isGeoModel = true;
                }

                var partialTickTime = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

                if (flyingGib.onGround()) {
                    if (flyingGib.timeToLive <= 20) {
                        float offsetY = ((20 - flyingGib.timeToLive) + partialTickTime) * -0.05f;

                        if(isGeoModel)
                            poseStack.translate(0.0f, offsetY, 0.0f);
                        else poseStack.translate(0.0f, -offsetY, 0.0f);
                    }
                }

                poseStack.translate(0,-entity.getType().getHeight() / 2,0);

                var partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                var texture = data.texture;
                var rendertype = RenderType.itemEntityTranslucentCull(texture);
                var vertexConsumer = buffer.getBuffer(rendertype);
                var prog = (entity.deathTime + partialTicks - 1.0F) / MAX_DEATH_TIME;
                var reverseProg = 1.0f - prog;
                var scale = 1.0f + prog / 2;
                var reverseScale = 1.0f - prog / 4;
                var rgba = Rgba.DEFAULT;


                switch (flyingGib.getData().deathType){
                    case LASER:
                        poseStack.scale(scale, scale, scale);

                        if(isGeoModel)
                            poseStack.translate(0, (-scale / 2) / 16D, 0);
                        else poseStack.translate(0, -scale / 2, 0);

                        rgba = rgba.setAlpha(reverseProg);
                        break;
                    case FIRE :
                        poseStack.scale(reverseScale, 1, reverseScale);

                        if(isGeoModel)
                            poseStack.translate(0, (-reverseScale / 2) / 16D, 0);
                        else poseStack.translate(0, -reverseScale / 2, 0);

//                        if (entity.deathTime > 0) {
//                            var partialTicks = Minecraft.getInstance().getFrameTime();
//                            var rotProg = ((float)entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
//                            rotProg = Mth.sqrt(rotProg);
//                            if (rotProg > 1.0F) rotProg = 1.0F;
//                            poseStack.mulPose(Axis.ZP.rotationDegrees(rotProg *  90.0F));
//                        }

//                        rgba = rgba.setAlpha(reverseProg);
                        break;

                    case GORE:
                        poseStack.mulPose(Axis.XP.rotationDegrees(prog * (float) flyingGib.rotationAxis.x));
                        poseStack.mulPose(Axis.YP.rotationDegrees(prog * (float) flyingGib.rotationAxis.y));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(prog * (float) flyingGib.rotationAxis.z));
                        break;
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