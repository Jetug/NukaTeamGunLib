package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.helpers.PlayerAnimationHelper;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory.ANIMATION_DATA_FACTORY;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerAnimatorEvents{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ANIMATION_DATA_FACTORY.registerFactory(
                PlayerAnimationHelper.ANIMATION,
                42,
                PlayerAnimatorEvents::registerPlayerAnimation);

        ANIMATION_DATA_FACTORY.registerFactory(
                PlayerAnimationHelper.MIRROR_ANIMATION,
                43,
                PlayerAnimatorEvents::registerPlayerAnimation);
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }
}
