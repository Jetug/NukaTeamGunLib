package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEvents {
//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent()
//    public static void onRenderHand(RenderArmEvent event) {
//        var reloadHandler = ClientReloadHandler.get();
//        var player = Minecraft.getInstance().player;
//        var itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
//
//        var isReloadingLeft = reloadHandler.isReloadingLeft(player);
//        var isRightArm = event.getArm() == HumanoidArm.RIGHT;
//
//        var isReloadingRight = reloadHandler.isReloadingRight(player);
//        var isLeftArm = event.getArm() == HumanoidArm.LEFT;
//
//        if ((isReloadingLeft && isRightArm) || (isReloadingRight && isLeftArm)) {
//            event.setCanceled(true);
//        }
//    }

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
}