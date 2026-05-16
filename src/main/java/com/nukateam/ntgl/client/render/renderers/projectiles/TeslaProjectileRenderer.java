package com.nukateam.ntgl.client.render.renderers.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import com.nukateam.ntgl.common.data.holders.ProjectileVariant;
import com.nukateam.ntgl.common.util.data.RGB;
import com.nukateam.ntgl.client.util.helpers.render.RenderUtil;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.foundation.entity.TeslaProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TeslaProjectileRenderer extends EntityRenderer<TeslaProjectile> {
    public static ResourceLocation texture = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/fx/tesla.png");
    private final float laserRadius = 0.05F / 5;
    private final float laserGlowRadius = 0.055F / 5;
    private static final int MIN_ANGLE = -45;
    private static final int MAX_ANGLE = 45;

    static final double offset = 0.50; // Distance per bolt vertex

    public TeslaProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TeslaProjectile entity) {
        var variant = entity.getProjectile().getProjectileVariant();
        return variant == ProjectileVariant.STANDARD ? texture : variant.getIcon();
    }

    @Override
    public boolean shouldRender(TeslaProjectile livingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    public void render(TeslaProjectile projectile, float entityYaw, float partialTicks,
                        PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        int shooterId = projectile.getShooterId();
        Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);

        if (muzzleWorldPos != null) {
            double projX = Mth.lerp(partialTicks, projectile.xOld, projectile.getX());
            double projY = Mth.lerp(partialTicks, projectile.yOld, projectile.getY());
            double projZ = Mth.lerp(partialTicks, projectile.zOld, projectile.getZ());

            double yOffset = 0.0;
            boolean isLocalFirstPerson = Minecraft.getInstance().player != null
                    && shooterId == Minecraft.getInstance().player.getId()
                    && Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if (isLocalFirstPerson) {
                yOffset = 0.15;
            }

            poseStack.translate(muzzleWorldPos.x() - projX, muzzleWorldPos.y() - projY + yOffset, muzzleWorldPos.z() - projZ);
        }

        renderLightning(projectile, partialTicks, poseStack, bufferSource, true, muzzleWorldPos != null);
        renderLightning(projectile, partialTicks, poseStack, bufferSource, false, muzzleWorldPos != null);
    }

    private void renderLightning(TeslaProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, boolean isVertical, boolean hasMuzzle) {
        var prog = ((float) projectile.tickCount) / ((float) projectile.getLife());
        var fadingValue = Math.sin(Math.sqrt(prog) * Math.PI);
        var radius = (float) (laserRadius * fadingValue * 2);
        var glowRadius = (float) (laserGlowRadius * fadingValue * 2);
        var distance = projectile.getDistance();
        var count = (int) Math.round(distance / offset);
        var playerPos = projectile.getEndVec();
        var laserPos = projectile.getStartVec();
        var pos = playerPos.subtract(laserPos);

        poseStack.pushPose();
        {
            pos = pos.normalize();
            var yPos = (float) Math.acos(pos.y);
            var xzPos = (float) Math.atan2(pos.z, pos.x);
            var side = projectile.isRightHand() ? -1 : 1;

            poseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Axis.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));
            if (!hasMuzzle) {
                poseStack.translate(side * 0.25, 0, 0);
            }

            var angleX = projectile.angle;
            var flag = 1;
            var length = distance / count;

            for (int i = 0; i <= count; i++) {
            poseStack.pushPose();
                if(flag > 0) angleX = getRandomAngle();

                var radiansX = (angleX * (Math.PI)) / 180;
                var offsetZ = length * Math.sin(Math.abs(radiansX)) * -flag;
                var offsetY = length * Math.cos(Math.abs(radiansX));

                if(isVertical) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(angleX * flag));
                    if (angleX < 0) offsetZ = -offsetZ;
                    poseStack.translate(0, 0, offsetZ / 2);
                }
                else {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(angleX * flag));
                    if(angleX > 0) offsetZ = -offsetZ;
                    poseStack.translate(offsetZ / 2, 0,  0);
                }

                var gameTime = projectile.level().getGameTime();
                var yOffset = 0;
                var color = new RGB(projectile.getProjectile().getColor()).toRgba();

                RenderUtils.renderBeam(poseStack, bufferSource, getTextureLocation(projectile), partialTicks, 1.0F,
                        gameTime, (float)yOffset - 0.1f, (float)(length + 0.1), color, radius, glowRadius);

            poseStack.popPose();
                poseStack.translate(0, offsetY, 0);
                flag = -flag;
            }
        }
        poseStack.popPose();
    }

    public static int getRandomAngle(){
        return ThreadLocalRandom.current().nextInt(MIN_ANGLE, MAX_ANGLE + 1);
    }
}
