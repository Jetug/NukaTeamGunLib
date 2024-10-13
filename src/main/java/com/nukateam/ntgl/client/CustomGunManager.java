package com.nukateam.ntgl.client;

import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.CustomGunLoader;
import com.nukateam.ntgl.common.base.config.CustomGun;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateGuns;
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
public class CustomGunManager {
    private static Map<ResourceLocation, CustomGun> customGunMap;

    public static boolean updateCustomGuns(S2CMessageUpdateGuns message) {
        return updateCustomGuns(message.getCustomGuns());
    }

    private static boolean updateCustomGuns(Map<ResourceLocation, CustomGun> customGunMap) {
        CustomGunManager.customGunMap = customGunMap;
        return true;
    }

//    public static void fill(NonNullList<ItemStack> items) {
//        if (customGunMap != null) {
//            customGunMap.forEach((id, gun) ->
//            {
//                ItemStack stack = new ItemStack(ModGuns.PISTOL.get());
//                stack.setHoverName(Component.translatable("item." + id.getNamespace() + "." + id.getPath() + ".name"));
//                CompoundTag tag = stack.getOrCreateTag();
//                tag.put("Model", gun.getModel().save(new CompoundTag()));
//                tag.put("Gun", gun.getGun().serializeNBT());
//                tag.putBoolean("Custom", true);
//                tag.putInt(Tags.AMMO_COUNT, gun.getGun().getGeneral().getMaxAmmo());
//                items.add(stack);
//            });
//        }
//    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        customGunMap = null;
    }

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(CustomGunLoader.get());
            CustomGunLoader.get().writeCustomGuns(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            Map<ResourceLocation, CustomGun> customGuns = CustomGunLoader.readCustomGuns(buffer);
            CustomGunManager.updateCustomGuns(customGuns);
            return Optional.empty();
        }
    }
}
