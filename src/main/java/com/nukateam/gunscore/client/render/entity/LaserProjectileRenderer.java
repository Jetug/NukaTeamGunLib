package com.nukateam.gunscore.client.render.entity;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.nukateam.gunscore.GunMod;
import com.nukateam.gunscore.common.data.util.Rgba;
import com.nukateam.gunscore.common.foundation.entity.LaserProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation texture = new ResourceLocation(GunMod.MOD_ID, "textures/fx/laser.png");
    private final double laserRadius = 0.05F;
    private final double laserGlowRadius = 0.06F;

    private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(texture);

    public LaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        return texture;
    }

    private Vec3 getPosition(Entity pLivingEntity, double pYOffset, float pPartialTick) {
        double d0 = Mth.lerp(pPartialTick, pLivingEntity.xOld, pLivingEntity.getX());
        double d1 = Mth.lerp(pPartialTick, pLivingEntity.yOld, pLivingEntity.getY()) + pYOffset;
        double d2 = Mth.lerp(pPartialTick, pLivingEntity.zOld, pLivingEntity.getZ());
        return new Vec3(d0, d1, d2);
    }

    public void render(LaserProjectile laserProjectile, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {

//        var poseStack = new PoseStack();
//        var player = Minecraft.getInstance().player;
//        poseStack.translate(player.getX(), player.getY(), player.getZ());

        var eyeHeight = laserProjectile.getEyeHeight();
        var shooterId = laserProjectile.getShooterId();
        var shooter = Minecraft.getInstance().level.getEntity(shooterId);

        float prog = ((float)laserProjectile.tickCount)/((float)laserProjectile.getLife());

        float radius = (float)(laserRadius * (Math.sin(Math.sqrt(prog)*Math.PI)) * 2);
        float glowRadius = (float)(laserGlowRadius * (Math.sin(Math.sqrt(prog)*Math.PI)) * 2);

        if (shooter == null) return;

        poseStack.pushPose();
        {
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(entityYaw));
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot() - 90));
//            poseStack.translate(0, -1, 0);
//            poseStack.translate(-100, 0, 0);
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(entityYaw));


//            poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(shooter.getXRot()));
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(shooter.getYRot()));

//            poseStack.translate(0, 15, 0);

//            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
            poseStack.translate(0.0D, eyeHeight, 0.0D);

//            poseStack.scale(0.25f, 1, 0.25f);

//            var playerPos = this.getPosition(shooter, (double) shooter.getBbHeight() * 0.5D, partialTicks);
//            var laserPos = this.getPosition(laserProjectile, eyeHeight, partialTicks); //getPosition(laserProjectile.endVec, (double) eyeHeight);

            var playerPos = laserProjectile.endVec;
            var laserPos  = laserProjectile.startVec;
            var pos       = playerPos.subtract(laserPos);

            var y = (float) (pos.length() + 1.0D);
            pos = pos.normalize();
            float yPos = (float) Math.acos(pos.y);
            float xzPos = (float) Math.atan2(pos.z, pos.x);

            poseStack.mulPose(Vector3f.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));

//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));


            long gameTime = laserProjectile.getLevel().getGameTime();
            int yOffset = 0; //(int) laserProjectile.position().y;
            var color = new Rgba(1, 1, 1, 1);

            renderBeam(poseStack, bufferSource, texture, partialTicks, 1.0F,
                    gameTime, (float) yOffset, (float)laserProjectile.distance, color, radius, glowRadius);
        }
        poseStack.popPose();
    }

    //
//    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER
//            = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullShader);
//
//    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL = Util.memoize((p_173233_, p_173234_) -> {
//        RenderType.CompositeState rendertype$compositestate = RenderType
//                .CompositeState.builder()
//                .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
//                .setTextureState(new RenderStateShard.TextureStateShard(p_173233_, false, false))
//                .setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(p_173234_);
//        return RenderType.create("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
//    });

    public static void renderBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                  float pPartialTick, float pTextureScale, long pGameTime, float pYOffset, float pHeight,
                                  Rgba pColors, float pBeamRadius, float pGlowRadius) {
        var maxY = pYOffset + pHeight;
        pPoseStack.pushPose();

        //jet
//        pPoseStack.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(pGameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        pPoseStack.pushPose();

//        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        var minX = -pGlowRadius;
        var maxX = -pGlowRadius;
        var minZ = -pGlowRadius;
        var maxZ = -pBeamRadius;
        var f12  = -pBeamRadius;
        var v = -1.0F + f2;
        var u = pHeight * pTextureScale * (BEAM_ALPHA / pBeamRadius) + v;

        var vertexConsumer = pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, false));

        renderPart(pPoseStack, vertexConsumer, pColors.setAlpha(1.0F),
                pYOffset, maxY,
                0.0F, pBeamRadius,
                pBeamRadius, 0.0F,
                maxZ, 0.0F,
                0.0F, f12,
                u, v);

        pPoseStack.popPose();

        maxZ = -pGlowRadius;
        v = -1.0F + f2;
        u = (float)pHeight * pTextureScale + v;

        renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
                pColors.setAlpha(BEAM_ALPHA), pYOffset, maxY, minX, maxX, pGlowRadius, minZ, maxZ,
                pGlowRadius, pGlowRadius, pGlowRadius, u, v);

        pPoseStack.popPose();
    }

    private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer,
                                   Rgba pColors,
                                   float pMinY, float pMaxY,
                                   float minX, float maxX,
                                   float minZ, float maxZ,
                                   float pX2, float pZ2,
                                   float pX3, float pZ3,
                                   float u, float v) {
        var posestack$pose = pPoseStack.last();
        var matrix4f = posestack$pose.pose();
        var matrix3f = posestack$pose.normal();

        float red   = pColors.r();
        float green = pColors.g();
        float blue  = pColors.b();
        float alpha = pColors.a();

        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minX, maxX, minZ, maxZ, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minZ, maxZ, pX3, pZ3, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX2, pZ2, minX, maxX, u, v);
    }

    private static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                   float pRed, float pGreen, float pBlue, float pAlpha,
                                   float pMinY, float pMaxY,
                                   float pMinX, float pMinZ,
                                   float pMaxX, float pMaxZ,

                                   float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, 1, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, 1, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, 0, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, 0, pMinV);
    }

    private static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                  float pRed, float pGreen, float pBlue, float pAlpha, float pY,
                                  float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, pY, pZ)
                .color(pRed, pGreen, pBlue, pAlpha).uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRender(LaserProjectile pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}
