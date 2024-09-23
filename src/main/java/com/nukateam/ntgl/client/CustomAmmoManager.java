package com.nukateam.ntgl.client;

import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.CustomAmmoLoader;
import com.nukateam.ntgl.common.base.gun.CustomAmmo;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateAmmo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class CustomAmmoManager {
    private static Map<ResourceLocation, CustomAmmo> customAmmoMap;

    public static boolean updateCustomAmmo(S2CMessageUpdateAmmo message) {
        return updateCustomCustomAmmo(message.getCustomAmmo());
    }

    private static boolean updateCustomCustomAmmo(Map<ResourceLocation, CustomAmmo> customGunMap) {
        CustomAmmoManager.customAmmoMap = customGunMap;
        return true;
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        customAmmoMap = null;
    }

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(CustomAmmoLoader.get());
            CustomAmmoLoader.get().writeCustomAmmo(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var customCustomAmmo = CustomAmmoLoader.readCustomAmmo(buffer);
            CustomAmmoManager.updateCustomCustomAmmo(customCustomAmmo);
            return Optional.empty();
        }
    }
}
