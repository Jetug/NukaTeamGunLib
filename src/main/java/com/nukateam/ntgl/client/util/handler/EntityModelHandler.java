package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.INtglItem;

import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class EntityModelHandler {
    @SubscribeEvent
    public void onRenderEntityPre(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
        var entity = event.getEntity();
        var heldItem = entity.getMainHandItem();

        if (heldItem.getItem() instanceof INtglItem) {
            var heldAnimation = WeaponModifierHelper.getGripType(new WeaponData(heldItem, entity))
                    .getHeldAnimation();

            var aimProgress = AimingHandler.get()
                    .getAimProgress(event.getEntity(), event.getPartialTick());

            heldAnimation.applyEntityPreRender(
                    entity,
                    InteractionHand.MAIN_HAND,
                    aimProgress,
                    event.getPoseStack(),
                    event.getMultiBufferSource());
        }
    }

    @SubscribeEvent
    public void onRenderEntityPost(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
        /* Makes sure the model part positions reset back to original definitions */
//        var model = event.getRenderer().getModel();
//        boolean slim = event.getEntity() instanceof AbstractClientPlayer player
//                && player.getModelName().equals("slim");

//        if(model instanceof HumanoidModel<LivingEntity> humanoidModel) {
//            humanoidModel.rightArm.x = -5.0F;
//            humanoidModel.rightArm.y = slim ? 2.5F : 2.0F;
//            humanoidModel.rightArm.z = 0.0F;
//            humanoidModel.leftArm.x = 5.0F;
//            humanoidModel.leftArm.y = slim ? 2.5F : 2.0F;
//            humanoidModel.leftArm.z = 0.0F;
//        }

        /*model.head.x = 5.0F;
        model.leftArm.y = slim ? 2.5F : 2.0F;
        model.leftArm.z = 0.0F;*/
    }
}
