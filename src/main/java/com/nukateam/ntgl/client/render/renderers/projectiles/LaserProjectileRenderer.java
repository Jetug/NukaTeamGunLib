package com.nukateam.ntgl.client.render.renderers.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.helpers.render.RenderUtil;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import com.nukateam.ntgl.client.util.helpers.render.RenderUtils;
import com.nukateam.ntgl.common.data.holders.ProjectileVariant;
import com.nukateam.ntgl.common.util.data.RGB;
import com.nukateam.ntgl.common.util.data.Rgba;
import com.nukateam.ntgl.common.foundation.entity.LaserProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation LASER_TEXTURE = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/fx/laser.png");
    private static final float LASER_RADIUS = 0.05F / 4;
    private static final float LASER_GLOW_RADIUS = 0.055F / 4;

    public LaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        var variant = entity.getProjectile().getProjectileVariant();
        return variant == ProjectileVariant.STANDARD ? LASER_TEXTURE : variant.getIcon();
    }

    @Override
    public boolean shouldRender(LaserProjectile livingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    protected Vector3f getBeamOffset(){
        return new Vector3f(0.20f, 0.25f, 0.07f);
    }

    protected float getLaserRadius(){
        return LASER_RADIUS;
    }

    protected float getLaserGlowRadius(){
        return LASER_GLOW_RADIUS;
    }

    @Override
    public void render(LaserProjectile projectile, float entityYaw, float partialTicks,
                       PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int light) {
        var prog = ((float) projectile.tickCount) / ((float) projectile.getLife());
        var fadingValue = Math.sin(Math.sqrt(prog) * Math.PI);
        var radius = (float) (getLaserRadius() * fadingValue * 2);
        var glowRadius = (float) (getLaserGlowRadius() * fadingValue * 2);
        var shooterId = projectile.getOwnerId();
        var shooter = Minecraft.getInstance().level.getEntity(shooterId);

        if (shooter == null) return;

        Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);
        boolean hasMuzzle = muzzleWorldPos != null;

        var playerPos = projectile.getEndVec();
        var laserPos = hasMuzzle ? muzzleWorldPos : shooter.getEyePosition(partialTicks);
        var pos = playerPos.subtract(laserPos);
        var offset = hasMuzzle ? new Vector3f(0, 0, 0) : getBeamOffset();
        var distance = projectile.getDistance() - offset.y;

        pos = pos.normalize();

        var yPos = (float) Math.acos(pos.y);
        var xzPos = (float) Math.atan2(pos.z, pos.x);
        var side = projectile.isRightHand() ? -1 : 1;

        if (hasMuzzle) {
            double projX = Mth.lerp(partialTicks, projectile.xOld, projectile.getX());
            double projY = Mth.lerp(partialTicks, projectile.yOld, projectile.getY());
            double projZ = Mth.lerp(partialTicks, projectile.zOld, projectile.getZ());
            poseStack.translate(muzzleWorldPos.x() - projX, muzzleWorldPos.y() - projY, muzzleWorldPos.z() - projZ);
        }

        poseStack.pushPose();
        {
            poseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Axis.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));

            poseStack.translate(side * offset.x, offset.y, offset.z);
            long gameTime = projectile.level().getGameTime();
            int yOffset = 0;
            var color = new RGB(projectile.getProjectile().getColor()).toRgba();

            RenderUtil.renderBeam(poseStack, bufferSource, getTextureLocation(projectile), partialTicks, 1.0F,
                    gameTime, (float) yOffset, distance, color, radius, glowRadius);
        }
        poseStack.popPose();
    }
}
