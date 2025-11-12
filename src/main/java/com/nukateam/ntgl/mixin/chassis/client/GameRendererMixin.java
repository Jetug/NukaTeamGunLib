package com.nukateam.ntgl.mixin.chassis.client;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.nukateam.chassis_core.common.util.GlobalMixinData.BobType;
import static com.nukateam.chassis_core.common.util.GlobalMixinData.CURRENT;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements AutoCloseable {
    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("HEAD"), cancellable = true)
    private void bobView(PoseStack pPoseStack, float pPartialTicks, CallbackInfo ci) {
        if (this.minecraft.getCameraEntity() instanceof Player player &&
                PlayerUtils.isWearingChassis(player) &&
                CURRENT == BobType.HAND
        ) {
            var chassis = PlayerUtils.getEntityChassis(player);
            assert chassis != null;
            float speed = chassis.walkDist - chassis.walkDistO;
            float f1 = -(chassis.walkDist + speed * pPartialTicks);
            float bob = Mth.lerp(pPartialTicks, chassis.oBob, chassis.bob);
            pPoseStack.translate(
                    Mth.sin(f1 * (float) Math.PI) * bob * 0.5F,
                    -Math.abs(Mth.cos(f1 * (float) Math.PI) * bob),
                    0.0F);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * bob * 3.0F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * bob) * 5.0F));
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V"))
    private void setHandBobType(PoseStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        CURRENT = BobType.HAND;
    }

    @Inject(method = "renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V", at = @At("TAIL"))
    private void setFinishRenderHand(PoseStack pPoseStack, Camera pActiveRenderInfo, float pPartialTicks, CallbackInfo ci) {
        CURRENT = BobType.NONE;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void setCameraBobType(float tickDelta, long limitTime, PoseStack matrices, CallbackInfo ci) {
        CURRENT = BobType.CAMERA;
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void setFinishRenderWorld(float tickDelta, long limitTime, PoseStack matrices, CallbackInfo ci) {
        CURRENT = BobType.NONE;
    }
}
