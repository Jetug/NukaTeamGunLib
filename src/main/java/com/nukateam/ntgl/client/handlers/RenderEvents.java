package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.enums.DeathType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class RenderEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderLivingEventPre(RenderLivingEvent.Pre event) {
        var dt = ClientProxy.getDamageType(event.getEntity());

        if (dt != null && (dt == DeathType.LASER || dt == DeathType.FIRE || dt == DeathType.GORE)) {
            event.setCanceled(true);
//            DeathEffectEntityRenderer.doRender(event.getRenderer(), event.getEntity(), event.getPoseStack(),
//            event.getMultiBufferSource(), event.getPackedLight(), event.getEntity().position());
        }
    }
}
