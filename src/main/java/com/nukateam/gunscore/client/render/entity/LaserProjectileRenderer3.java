package com.nukateam.gunscore.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public class LaserProjectileRenderer3 extends EntityRenderer<LaserProjectile> {
    public static ResourceLocation texture = new ResourceLocation(GunMod.MOD_ID, "textures/fx/laser2.png");
    private final double laserWidth = 3.0;

    private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(texture);

    public LaserProjectileRenderer3(EntityRendererProvider.Context context) {
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
