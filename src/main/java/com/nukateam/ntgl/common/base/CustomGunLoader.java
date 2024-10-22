package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.config.gun.CustomGun;
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
public class CustomGunLoader extends SimpleJsonResourceReloadListener {
    private static CustomGunLoader instance;

    private Map<ResourceLocation, CustomGun> customGunMap = new HashMap<>();

    public CustomGunLoader() {
        super(GSON_INSTANCE, "custom_guns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        var builder = ImmutableMap.<ResourceLocation, CustomGun>builder();

        objects.forEach((resourceLocation, object) -> {
            try {
                var customGun = GSON_INSTANCE.fromJson(object, CustomGun.class);
                if (customGun != null && Validator.isValidObject(customGun))
                    builder.put(resourceLocation, customGun);
                else
                    Ntgl.LOGGER.error("Couldn't load data file {} as it is missing or malformed", resourceLocation);
            } catch (InvalidObjectException e) {
                Ntgl.LOGGER.error("Missing required properties for {}", resourceLocation);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        this.customGunMap = builder.build();
    }

    /**
     * Writes all custom guns into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeCustomGuns(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.customGunMap.size());
        this.customGunMap.forEach((id, gun) -> {
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
    public static ImmutableMap<ResourceLocation, CustomGun> readCustomGuns(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        if (size > 0) {
            ImmutableMap.Builder<ResourceLocation, CustomGun> builder = ImmutableMap.builder();
            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var customGun = new CustomGun();
                customGun.deserializeNBT(buffer.readNbt());
                builder.put(id, customGun);
            }
            return builder.build();
        }

        return ImmutableMap.of();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        var customGunLoader = new CustomGunLoader();
        event.addListener(customGunLoader);
        CustomGunLoader.instance = customGunLoader;
    }

    @Nullable
    public static CustomGunLoader get() {
        return instance;
    }
}
