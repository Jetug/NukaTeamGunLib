package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.lang.reflect.InvocationTargetException;

import static com.nukateam.ntgl.client.util.ClientDebug.*;

@EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onRenderHand(RenderHandEvent event) {
        var reloadHandler = ClientReloadHandler.get();
        var player = Minecraft.getInstance().player;

        var isReloadingLeft = reloadHandler.isReloadingLeft(player);
        var isRightArm = event.getHand() == InteractionHand.MAIN_HAND;

        var isReloadingRight = reloadHandler.isReloadingRight(player);
        var isLeftArm = event.getHand() == InteractionHand.OFF_HAND;

        if ((isReloadingLeft && isRightArm) || (isReloadingRight && isLeftArm)) {
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderHand2(RenderHandEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        var mc = Minecraft.getInstance();
//        var poseStack = event.getPoseStack();
//        poseStack.pushPose();
//        {
//            poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
//            poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
//
//            poseStack.translate(-65 / 10d / 16d, 0, 0);
//            renderFirstPersonArm(mc.player, HumanoidArm.LEFT, poseStack, event.getPackedLight());
//
//            poseStack.translate(50 / 10d / 16d, -20 / 10d / 16d, 0);
//            renderFirstPersonArm(mc.player, HumanoidArm.RIGHT, poseStack, event.getPackedLight());
//        }
//
//        poseStack.popPose();

//        event.setCanceled(true);
//
//        var mc = Minecraft.getInstance();
//        var partialTicks = event.getPartialTick();
//        var buffer = event.getMultiBufferSource();
//        var poseStack = event.getPoseStack();
//        var packedLight = event.getPackedLight();
//        var player = mc.player;
//        poseStack.pushPose();
//        {
////        renderHand(mc.player, poseStack, buffer, partialTicks, event.getPackedLight());
//            var playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
//            poseStack.translate(X / 10d / 16d, Y / 10d / 16d, Z / 10d / 16d);
//            playerRenderer.renderRightHand(poseStack, buffer, packedLight, player);
//
////            if (flag) {
////                playerRenderer.renderRightHand(poseStack, buffer, packedLight, player);
////            } else {
////                playerRenderer.renderLeftHand(poseStack, buffer, packedLight, abstractclientplayer);
////            }
//        }
//        poseStack.popPose();
    }

}