package com.nukateam.ntgl.common.base;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.data.util.Easings;
import com.nukateam.ntgl.common.base.gun.*;
import com.nukateam.ntgl.common.base.utils.JsonDeserializers;
import com.nukateam.ntgl.common.data.annotation.Validator;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateGuns;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static net.minecraftforge.registries.ForgeRegistries.*;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkGunManager extends SimplePreparableReloadListener<Map<GunItem, Gun>> {
    private static final int FILE_TYPE_LENGTH_VALUE = ".json".length();
    private static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.registerTypeAdapter(GripType.class, JsonDeserializers.GRIP_TYPE);
        builder.registerTypeAdapter(FireMode.class, JsonDeserializers.FIRE_MODE);
        builder.registerTypeAdapter(AttachmentType.class, JsonDeserializers.ATTACHMENT_TYPE);
        builder.registerTypeAdapter(AmmoType.class, JsonDeserializers.AMMO_TYPE);
        builder.registerTypeAdapter(Easings.class, JsonDeserializers.EASING);
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        return builder.create();
    });

    private static List<GunItem> clientRegisteredGuns = new ArrayList<>();
    private static NetworkGunManager instance;

    private Map<ResourceLocation, Gun> registeredGuns = new HashMap<>();

    @Override
    protected Map<GunItem, Gun> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return getConfigMap(manager, (v) -> v instanceof GunItem, Gun.class, "guns");
    }

    @NotNull
    private static<T extends Item, Y> Map<T, Y> getConfigMap(ResourceManager manager, Function<Item, Boolean> tClass, Class<Y> yClass, String resourcePath) {
        Map<T, Y> map = new HashMap<>();

        ITEMS.getValues().stream().filter(tClass::apply).forEach(item ->
        {
            var id = ITEMS.getKey(item);

            if (id != null) {
                var resources = new ArrayList<>(getJsonResources(manager, resourcePath, id).keySet());

                resources.sort((r1, r2) -> {
                    if (r1.getNamespace().equals(r2.getNamespace())) return 0;
                    return r2.getNamespace().equals(Ntgl.MOD_ID) ? 1 : -1;
                });

                resources.forEach(resourceLocation ->
                {
                    var path = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - FILE_TYPE_LENGTH_VALUE);
                    var splitPath = path.split("/");

                    // Makes sure the file name matches exactly with the id of the gun
                    if (!id.getPath().equals(splitPath[splitPath.length - 1]))
                        return;

                    // Also check if the mod id matches with the gun's registered namespace
                    if (!id.getNamespace().equals(resourceLocation.getNamespace()))
                        return;

                    manager.getResource(resourceLocation).ifPresent(resource ->
                    {
                        try (var reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                            var gun = GsonHelper.fromJson(GSON_INSTANCE, reader, yClass);

                            if (Validator.isValidObject(gun)) {
                                map.put((T) item, gun);
                            }
                            else {
                                Ntgl.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default gun data", resourceLocation);
                                map.putIfAbsent((T) item, yClass.getDeclaredConstructor().newInstance());
                            }
                        }
                        catch (InvalidObjectException e) {
                            Ntgl.LOGGER.error("Missing required properties for {}", resourceLocation);
                            e.printStackTrace();
                        }
                        catch (IOException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                            Ntgl.LOGGER.error("Couldn't parse data file {}", resourceLocation);
                        }
                        catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                });
            }
        });
        return map;
    }

    @NotNull
    private static Map<ResourceLocation, Resource> getJsonResources(ResourceManager manager, String path, ResourceLocation id) {
        return manager.listResources(path, (fileName) -> fileName.getPath().endsWith(id.getPath() + ".json"));
    }

    @Override
    protected void apply(Map<GunItem, Gun> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, Gun> builder = ImmutableMap.builder();

        objects.forEach((item, gun) -> {
            Validate.notNull(ITEMS.getKey(item));
            builder.put(ITEMS.getKey(item), gun);
            item.setGun(new Supplier(gun));
        });

        this.registeredGuns = builder.build();
    }

    /**
     * Writes all registered guns into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeRegisteredGuns(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.registeredGuns.size());
        this.registeredGuns.forEach((id, gun) -> {
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
    public static ImmutableMap<ResourceLocation, Gun> readRegisteredGuns(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            ImmutableMap.Builder<ResourceLocation, Gun> builder = ImmutableMap.builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                Gun gun = Gun.create(buffer.readNbt());
                builder.put(id, gun);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredGuns(S2CMessageUpdateGuns message) {
        return updateRegisteredGuns(message.getRegisteredGuns());
    }

    /**
     * Updates registered guns from data provided by the server
     *
     * @return true if all registered guns were able to update their corresponding gun item
     */
    private static boolean updateRegisteredGuns(Map<ResourceLocation, Gun> registeredGuns) {
        clientRegisteredGuns.clear();
        if (registeredGuns != null) {
            for (Map.Entry<ResourceLocation, Gun> entry : registeredGuns.entrySet()) {
                Item item = ITEMS.getValue(entry.getKey());
                if (!(item instanceof GunItem)) {
                    return false;
                }
                ((GunItem) item).setGun(new Supplier(entry.getValue()));
                clientRegisteredGuns.add((GunItem) item);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a map of all the registered guns objects. Note, this is an immutable map.
     *
     * @return a map of registered gun objects
     */
    public Map<ResourceLocation, Gun> getRegisteredGuns() {
        return this.registeredGuns;
    }

    /**
     * Gets a list of all the guns registered on the client side. Note, this is an immutable list.
     *
     * @return a map of guns registered on the client
     */
    public static List<GunItem> getClientRegisteredGuns() {
        return ImmutableList.copyOf(clientRegisteredGuns);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkGunManager.instance = null;
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkGunManager networkGunManager = new NetworkGunManager();
        event.addListener(networkGunManager);
        NetworkGunManager.instance = networkGunManager;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateGuns());
        }
    }

    /**
     * Gets the network gun manager. This will be null if the client isn't running an integrated
     * server or the client is connected to a dedicated server.
     *
     * @return the network gun manager
     */
    @Nullable
    public static NetworkGunManager get() {
        return instance;
    }

    /**
     * A simple wrapper for a gun object to pass to GunItem. This is to indicate to developers that
     * Gun instances shouldn't be changed on GunItems as they are controlled by NetworkGunManager.
     * Changes to gun properties should be made through the JSON file.
     */
    public static class Supplier {
        private Gun gun;

        private Supplier(Gun gun) {
            this.gun = gun;
        }

        public Gun getGun() {
            return this.gun;
        }
    }

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkGunManager.get());
            NetworkGunManager.get().writeRegisteredGuns(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            Map<ResourceLocation, Gun> registeredGuns = NetworkGunManager.readRegisteredGuns(buffer);
            NetworkGunManager.updateRegisteredGuns(registeredGuns);
            return Optional.empty();
        }
    }
}
