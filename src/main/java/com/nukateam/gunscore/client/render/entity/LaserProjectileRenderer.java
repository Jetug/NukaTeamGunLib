package com.nukateam.gunscore.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.nukateam.gunscore.GunMod;
import com.nukateam.gunscore.common.foundation.entity.LaserProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    public static ResourceLocation texture = new ResourceLocation(GunMod.MOD_ID, "textures/fx/laser2.png");
    private final double laserWidth = 3.0;

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

    private Vec3 getPosition(Vec3 vec3, double pYOffset) {
        return new Vec3(vec3.x, vec3.y + pYOffset, vec3.z);
    }

    public void render2(LaserProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        GunMod.LOGGER.warn("Laser render");

        var distance = (float)entity.distance;
        var maxTicks = entity.getLife();
        var prog = ((float)entity.tickCount) / ((float)maxTicks);
        var width = (float)(laserWidth * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);
        var distance_start = (float)Math.min(1.0d, distance);
        var u = (float)(distance / laserWidth) * 2.0f;

//        bindTexture(texture); // Замените на соответствующий метод загрузки текстуры
        Minecraft.getInstance().getTextureManager().bindForSetup(texture);

        poseStack.pushPose();
        RenderSystem.enableBlend();
//        RenderSystem.enableRescaleNormal();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        poseStack.translate(entity.getX(), entity.getY(), entity.getZ());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(entity.laserYaw - 90F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(entity.laserPitch));

        float f10 = 0.0125F;

        var bufferbuilder = renderTypeBuffer.getBuffer(BEAM_RENDER_TYPE);


        distance *= 80.0D;
        distance_start *= 80.0D;

        float rot_x = 45f + (prog * 180f);

        poseStack.mulPose(Vector3f.XP.rotationDegrees(rot_x + 90f));
        poseStack.scale(f10, f10, f10);

        float brightness = (float) Math.sin(Math.sqrt(prog) * Math.PI);

        var pose = poseStack.last();
        var matrix4f = pose.pose();
        var matrix3f = pose.normal();

        int r = 255;
        int g = 255;
        int b = 255;

        if (distance > distance_start) { // Beam Segment
            poseStack.pushPose();
            for (int i = 0; i < 2; ++i) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                vertex(bufferbuilder, matrix4f, matrix3f,   distance      , -width, 0.0f,       r, g, b, u + prog, 0);
                vertex(bufferbuilder, matrix4f, matrix3f,   distance_start, -width, 0.0f,       r, g, b,        prog, 0);
                vertex(bufferbuilder, matrix4f, matrix3f,   distance_start,  width, 0.0f,       r, g, b,        prog, 1);
                vertex(bufferbuilder, matrix4f, matrix3f,   distance      ,  width, 0.0f,       r, g, b, u + prog, 1);


//                bufferbuilder.vertex(distance, -width, 0.0D         ).uv(u + prog, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//                bufferbuilder.vertex(distance_start, -width, 0.0D   ).uv(prog        , 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//                bufferbuilder.vertex(distance_start, width, 0.0D    ).uv(prog        , 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//                bufferbuilder.vertex(distance, width, 0.0D          ).uv(u + prog, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();

            }
            poseStack.popPose();
        }

        poseStack.pushPose();
        for (int i = 0; i < 2; ++i) { // Beam start segment
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            vertex(bufferbuilder, matrix4f, matrix3f,   distance_start, -width, 0.0f,       r, g, b, 1, 0);
            vertex(bufferbuilder, matrix4f, matrix3f,   0          , -width, 0.0f,       r, g, b, 0, 0);
            vertex(bufferbuilder, matrix4f, matrix3f,   0          ,  width, 0.0f,       r, g, b, 0, 1);
            vertex(bufferbuilder, matrix4f, matrix3f,   distance_start,  width, 0.0f,       r, g, b, 1, 1);

//            bufferbuilder.vertex(distance_start, -width, 0.0D).uv(1, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//            bufferbuilder.vertex(0, -width, 0.0D         ).uv(0, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//            bufferbuilder.vertex(0, width, 0.0D          ).uv(0, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//            bufferbuilder.vertex(distance_start, width, 0.0D ).uv(1, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
        }
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
//        RenderSystem.disableRescaleNormal();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public void render(LaserProjectile laserProjectile, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(laserProjectile, pEntityYaw, pPartialTicks, poseStack, pBuffer, pPackedLight);
        var entity = Minecraft.getInstance().player;
        if (entity != null) {
            var gameTime = (float)laserProjectile.level.getGameTime() + pPartialTicks;
            var f2 = gameTime * 0.5F % 1.0F;
            var eyeHeight = laserProjectile.getEyeHeight();

            poseStack.pushPose();
            {
                poseStack.translate(0.0D, eyeHeight, 0.0D);
                var playerPos = this.getPosition(entity, (double) entity.getBbHeight() * 0.5D, pPartialTicks);
                var laserPos = this.getPosition(laserProjectile, eyeHeight, pPartialTicks); //getPosition(laserProjectile.endVec, (double) eyeHeight);
                var pos = playerPos.subtract(laserPos);
                var y = (float) (pos.length() + 1.0D);
                pos = pos.normalize();
                float yPos = (float) Math.acos(pos.y);
                float xzPos = (float) Math.atan2(pos.z, pos.x);

                poseStack.mulPose(Vector3f.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));

                float f7 = 1 * 0.05F * -1.5F;

                int r = 255;
                int g = 255;
                int b = 255;

                float x1 = Mth.cos(f7 + 2.3561945F) * 0.282F;
                float z1 = Mth.sin(f7 + 2.3561945F) * 0.282F;
                float x2 = Mth.cos(f7 + ((float) Math.PI / 4F)) * 0.282F;
                float z2 = Mth.sin(f7 + ((float) Math.PI / 4F)) * 0.282F;
                float x4 = Mth.cos(f7 + 3.926991F) * 0.282F;
                float z4 = Mth.sin(f7 + 3.926991F) * 0.282F;
                float x3 = Mth.cos(f7 + 5.4977875F) * 0.282F;
                float z3 = Mth.sin(f7 + 5.4977875F) * 0.282F;
                float x5 = Mth.cos(f7 + (float) Math.PI) * 0.2F;
                float z5 = Mth.sin(f7 + (float) Math.PI) * 0.2F;
                float x6 = Mth.cos(f7 + 0.0F) * 0.2F;
                float z6 = Mth.sin(f7 + 0.0F) * 0.2F;
                float x7 = Mth.cos(f7 + ((float) Math.PI / 2F)) * 0.2F;
                float z7 = Mth.sin(f7 + ((float) Math.PI / 2F)) * 0.2F;
                float x8 = Mth.cos(f7 + ((float) Math.PI * 1.5F)) * 0.2F;
                float z8 = Mth.sin(f7 + ((float) Math.PI * 1.5F)) * 0.2F;
                float v2 = -1.0F + f2;
                float v = y * 2.5F + v2;

                var vertexconsumer = pBuffer.getBuffer(BEAM_RENDER_TYPE);
                var pose = poseStack.last();
                var matrix4f = pose.pose();
                var matrix3f = pose.normal();

                vertex(vertexconsumer, matrix4f, matrix3f, x5, y, z5,       r, g, b, 0.4999F, v);
                vertex(vertexconsumer, matrix4f, matrix3f, x5, 0.0F, z5, r, g, b, 0.4999F, v2);
                vertex(vertexconsumer, matrix4f, matrix3f, x6, 0.0F, z6, r, g, b, 0.0F  , v2);
                vertex(vertexconsumer, matrix4f, matrix3f, x6, y, z6,       r, g, b, 0.0F   , v);


                vertex(vertexconsumer, matrix4f, matrix3f, x7, y, z7,       r, g, b, 0.4999F, v);
                vertex(vertexconsumer, matrix4f, matrix3f, x7, 0.0F, z7, r, g, b, 0.4999F, v2);
                vertex(vertexconsumer, matrix4f, matrix3f, x8, 0.0F, z8, r, g, b, 0.0F, v2);
                vertex(vertexconsumer, matrix4f, matrix3f, x8, y, z8,       r, g, b, 0.0F, v);

                var v3 = 0.0F;

                if (laserProjectile.tickCount % 2 == 0)
                    v3 = 0.5F;

                vertex(vertexconsumer, matrix4f, matrix3f, x1, y, z1, r, g, b, 0.5F, v3 + 0.5F);
                vertex(vertexconsumer, matrix4f, matrix3f, x2, y, z2, r, g, b, 1.0F, v3 + 0.5F);
                vertex(vertexconsumer, matrix4f, matrix3f, x3, y, z3, r, g, b, 1.0F, v3);
                vertex(vertexconsumer, matrix4f, matrix3f, x4, y, z4, r, g, b, 0.5F, v3);
            }
            poseStack.popPose();
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f,
                               float x, float y, float z,
                               int r, int g, int b, float u, float v) {
        vertexConsumer.vertex(matrix4f, x, y, z)
                .color(r, g, b, 255)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
