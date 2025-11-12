package com.nukateam.chassis_core.common.util.helpers;

import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

import static com.nukateam.chassis_core.common.network.PacketSender.doServerAction;

public class PlayerUtils {
    public static void addEffect(Player player, MobEffect effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, WearableChassis.EFFECT_DURATION, amplifier, false, false));
    }

    @OnlyIn(Dist.CLIENT)
    public static LocalPlayer getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isLocalWearingChassis() {
        return isWearingChassis(getLocalPlayer());
    }

    public static boolean isWearingChassis(Entity entity) {
        return entity != null && entity.getVehicle() instanceof WearableChassis;
    }

    @OnlyIn(Dist.CLIENT)
    public static WearableChassis getLocalPlayerChassis() {
        return getEntityChassis(getLocalPlayer());
    }

    @Nullable
    public static WearableChassis getEntityChassis(Entity entity) {
        if (entity.getVehicle() instanceof WearableChassis)
            return (WearableChassis) entity.getVehicle();
        else return null;
    }

    public static void stopWearingArmor(Player player) {
        if(PlayerUtils.isWearingChassis(player)) {
            player.stopRiding();
            doServerAction(ActionType.DISMOUNT);
            player.setInvisible(false);
        }
    }

    public static void renderChassisHand(PoseStack poseStack, boolean isRight, HumanoidArm hand, int packedLight) {
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));

        int x = 0;

        if(hand == HumanoidArm.RIGHT){
            x = isRight ? -155 : 5;
        }
        else if(hand == HumanoidArm.LEFT){
            x = isRight ? 5 : -155;
        }
        poseStack.translate(x / 10D / 16D, -100 / 10D / 16D, -140 / 10D / 16D);
        renderChassisHand(hand, poseStack, null, packedLight);
    }

    public static void renderChassisHand(HumanoidArm arm, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        getLocalPlayerChassis().getHandRenderer().render(
                arm,
                poseStack,
                multiBufferSource,
                packedLight);
    }
}
