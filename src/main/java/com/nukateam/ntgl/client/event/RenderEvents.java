package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.renderers.DeathEffectEntityRenderer;
import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
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
    public static void onRenderLivingEventPre(RenderLivingEvent.Post event) {
        var dt = ClientProxy.getDamageType(event.getEntity());

        if (dt != null && dt.is(ModDamageTypes.BULLET)) {
//            event.setCanceled(true);
            DeathEffectEntityRenderer.doRender(event.getRenderer(), event.getEntity(), event.getPoseStack(),
                    event.getMultiBufferSource(), event.getPackedLight(), event.getEntity().position());
        }
    }
}
