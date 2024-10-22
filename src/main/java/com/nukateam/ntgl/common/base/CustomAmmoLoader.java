package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.config.CustomAmmo;
import com.nukateam.ntgl.common.data.annotation.Validator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.base.ConfigUtils.GSON_INSTANCE;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class CustomAmmoLoader extends SimpleJsonResourceReloadListener {
    private static CustomAmmoLoader instance;

    private Map<ResourceLocation, CustomAmmo> customAmmoMap = new HashMap<>();

    public CustomAmmoLoader() {
        super(GSON_INSTANCE, "custom_ammo");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, CustomAmmo> builder = ImmutableMap.builder();
        objects.forEach((resourceLocation, object) ->
        {
            try {
                var customAmmo = GSON_INSTANCE.fromJson(object, CustomAmmo.class);
                if (customAmmo != null && Validator.isValidObject(customAmmo)) {
                    builder.put(resourceLocation, customAmmo);
                } else {
                    Ntgl.LOGGER.error("Couldn't load data file {} as it is missing or malformed", resourceLocation);
                }
            } catch (InvalidObjectException e) {
                Ntgl.LOGGER.error("Missing required properties for {}", resourceLocation);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        this.customAmmoMap = builder.build();
    }

    /**
     * Writes all custom guns into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeCustomAmmo(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.customAmmoMap.size());
        this.customAmmoMap.forEach((id, gun) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(gun.serializeNBT());
        });
    }

    /**
     * Reads all registered guns from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered guns from the server
     */
    public static ImmutableMap<ResourceLocation, CustomAmmo> readCustomAmmo(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size > 0) {
            ImmutableMap.Builder<ResourceLocation, CustomAmmo> builder = ImmutableMap.builder();
            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var customAmmo = new CustomAmmo();
                customAmmo.deserializeNBT(buffer.readNbt());
                builder.put(id, customAmmo);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        CustomAmmoLoader customGunLoader = new CustomAmmoLoader();
        event.addListener(customGunLoader);
        CustomAmmoLoader.instance = customGunLoader;
    }

    @Nullable
    public static CustomAmmoLoader get() {
        return instance;
    }
}
