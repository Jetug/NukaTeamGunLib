package com.nukateam.ntgl.client.render.renderers.projectiles;

import com.nukateam.ntgl.common.foundation.entity.StunGrenadeEntity;
import com.nukateam.ntgl.common.foundation.entity.ThrowableGrenadeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class ThrowableGrenadeRenderer extends EntityRenderer<ThrowableGrenadeEntity> {
    public ThrowableGrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(@NotNull ThrowableGrenadeEntity entity) {
        return null;
    }

    @Override
    public void render(ThrowableGrenadeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        poseStack.pushPose();

        /* Makes the grenade face in the direction of travel */
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));

        /* Offsets to the center of the grenade before applying rotation */
        var rotation = entity.prevRotation + (entity.rotation - entity.prevRotation) * partialTicks;
        poseStack.translate(0, 0.15, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-rotation));
        poseStack.translate(0, -0.15, 0);

        if (entity instanceof StunGrenadeEntity) {
            poseStack.translate(0, entity.getDimensions(Pose.STANDING).height / 2, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
            poseStack.translate(0, -entity.getDimensions(Pose.STANDING).height / 2, 0);
        }

        /* */
        poseStack.translate(0.0, 0.5, 0.0);

        Minecraft.getInstance().getItemRenderer().renderStatic(entity.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, entity.level(), 0);

        poseStack.popPose();
    }
}
