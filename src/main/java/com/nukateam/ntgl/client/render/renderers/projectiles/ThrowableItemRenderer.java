package com.nukateam.ntgl.client.render.renderers.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrowableItemRenderer extends EntityRenderer<ThrowableItemEntity> {
    public static final int MAX_SIZE_TICK = 5;

    public ThrowableItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(@NotNull ThrowableItemEntity entity) {
        return null;
    }

    @Override
    public void render(ThrowableItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        poseStack.pushPose();
        {
            /* Makes the grenade face in the direction of travel */
            poseStack.mulPose(Axis.YP.rotationDegrees(180F));
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));

            /* Offsets to the center of the grenade before applying rotation */
            var rotation = entity.prevRotation + (entity.rotation - entity.prevRotation) * partialTicks;
            poseStack.translate(0, 0.15, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(-rotation));
            poseStack.translate(0, -0.15, 0);

            var scale = 1f;

            if(entity.tickCount < MAX_SIZE_TICK) {
                scale = (entity.tickCount + partialTicks) / MAX_SIZE_TICK;
            }

            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0, 0.5, 0.0);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    entity.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                    poseStack, renderTypeBuffer, entity.level(), 0);
        }
        poseStack.popPose();
    }
}
