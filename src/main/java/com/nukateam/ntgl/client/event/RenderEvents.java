package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.renderers.DeathEffectEntityRenderer;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class RenderEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderLivingEventPre(RenderLivingEvent.Pre event) {
//        if (event.getEntity() instanceof Player ply) {
//
//            ItemStack stack = ply.getItemMainHand();
//            if(!stack.isEmpty() && stack.getItem() instanceof GenericGun && ((GenericGun) stack.getItem()).hasBowAnim()){
//                ModelBase mdl = event.getRenderer().getMainModel();
//                if (mdl instanceof ModelPlayer) {
//                    ModelPlayer model = (ModelPlayer) mdl;
//                    if (ply.getPrimaryHand()==EnumHandSide.RIGHT) {
//                        model.rightArmPose = ArmPose.BOW_AND_ARROW;
//                    } else {
//                        model.leftArmPose = ArmPose.BOW_AND_ARROW;
//                    }
//                }
//            } else {
//
//                ItemStack stack2 =ply.getHeldItemOffhand();
//                if(!stack2.isEmpty() && stack2.getItem() instanceof GenericGun && ((GenericGun) stack2.getItem()).hasBowAnim()){
//                    ModelBase mdl = event.getRenderer().getMainModel();
//                    if (mdl instanceof ModelPlayer) {
//                        ModelPlayer model = (ModelPlayer) mdl;
//
//                        if (ShooterValues.getIsCurrentlyUsingGun(ply,true)){
//
//                            if (ply.getPrimaryHand()==EnumHandSide.RIGHT) {
//                                model.leftArmPose = ArmPose.BOW_AND_ARROW;
//                            } else {
//                                model.rightArmPose = ArmPose.BOW_AND_ARROW;
//                            }
//                        }
//                    }
//                }
//            }
//        }

        var dt = ClientProxy.getDamageType(event.getEntity());

        if (dt != null && dt.is(ModDamageTypes.BULLET)) {
            event.setCanceled(true);
            DeathEffectEntityRenderer.doRender(event.getRenderer(), event.getEntity(), event.getPoseStack(),
                    event.getMultiBufferSource(), event.getPackedLight(), event.getEntity().position());
        }

//        switch (dt) {
//            case GORE:
//                event.setCanceled(true);
//                break;
//            case BIO:
//            case LASER:
//                event.setCanceled(true);
//                DeathEffectEntityRenderer.doRender(event.getRenderer(), event.getEntity(), event.getEntity().position(),
//                        event.getMultiBufferSource(), event.getPackedLight());
//                break;
//            default:
//                break;
//        }

    }
}
