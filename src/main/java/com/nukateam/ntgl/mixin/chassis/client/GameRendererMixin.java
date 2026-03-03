package com.nukateam.ntgl.mixin.chassis.client;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
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

    @Inject(method = "bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("HEAD"), cancellable = true, remap=false)
    private void bobView(PoseStack poseStack, float pPartialTicks, CallbackInfo ci) {
        if (this.minecraft.getCameraEntity() instanceof Player player &&
                PlayerUtils.isWearingChassis(player) &&
                CURRENT == BobType.HAND
        ) {
            var chassis = PlayerUtils.getEntityChassis(player);
            assert chassis != null;
            float speed = chassis.walkDist - chassis.walkDistO;
            float f1 = -(chassis.walkDist + speed * pPartialTicks);
            float bob = Mth.lerp(pPartialTicks, chassis.oBob, chassis.bob);
            poseStack.translate(
                    Mth.sin(f1 * (float) Math.PI) * bob * 0.5F,
                    -Math.abs(Mth.cos(f1 * (float) Math.PI) * bob),
                    0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * bob * 3.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * bob) * 5.0F));
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V"), remap=false)
    private void setHandBobType(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
        CURRENT = BobType.HAND;
    }

    @Inject(method = "renderItemInHand", at = @At("TAIL"), remap=false)
    private void setFinishRenderHand(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
        CURRENT = BobType.NONE;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"), remap=false)
    private void setCameraBobType(DeltaTracker deltaTracker, CallbackInfo ci) {
        CURRENT = BobType.CAMERA;
    }

    @Inject(method = "renderLevel", at = @At("TAIL"), remap=false)
    private void setFinishRenderWorld(DeltaTracker deltaTracker, CallbackInfo ci) {
        CURRENT = BobType.NONE;
    }
}
