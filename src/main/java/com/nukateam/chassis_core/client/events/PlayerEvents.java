package com.nukateam.chassis_core.client.events;

import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.*;

@EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (isWearingChassis(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onRenderHand(RenderArmEvent event) {
        if (isLocalWearingChassis() && getLocalPlayerChassis().renderHand()) {
            var poseStack = event.getPoseStack();
            poseStack.pushPose();
            {
                var isRight = event.getArm() == HumanoidArm.RIGHT;
                var side = isRight ? 1 : -1;
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                if(isRight) {
                    poseStack.translate(18 / 10D / 16D, 91 / 10D / 16D, -155 / 10D / 16D);
                }
                else {
                    poseStack.translate(140 / 10D / 16D, 106 / 10D / 16D,  -155 / 10D / 16D);
                }

                poseStack.mulPose(Axis.ZP.rotationDegrees(180 * side));

                renderChassisHand(event.getArm(), poseStack, event.getMultiBufferSource(), event.getPackedLight());
            }
            poseStack.popPose();
            event.setCanceled(true);
        }
    }
}