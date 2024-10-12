package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.config.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateGuns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkManager<T, Y extends INBTSerializable<CompoundTag>> extends SimplePreparableReloadListener<Map<T, Y>> {

    private static List<GunItem> clientRegisteredGuns = new ArrayList<>();
    private static NetworkManager instance;

    private Map<ResourceLocation, Gun> registeredGuns = new HashMap<>();

    @Override
    protected Map<T, Y> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, (v) -> v instanceof GunItem, Gun.class, "guns");
    }

    @Override
    protected void apply(Map<T, Y> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        var builder = ImmutableMap.<ResourceLocation, T>builder();

        objects.forEach((item, gun) -> {
            Validate.notNull(ITEMS.getKey(item));
            builder.put(ITEMS.getKey(item), gun);
            item.setGun(new NetworkGunManager.Supplier(gun));
        });

        this.registeredGuns = builder.build();
    }

//    @Override
//    protected void apply(Map<GunItem, Gun> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
//        ImmutableMap.Builder<ResourceLocation, Gun> builder = ImmutableMap.builder();
//
//        objects.forEach((item, gun) -> {
//            Validate.notNull(ITEMS.getKey(item));
//            builder.put(ITEMS.getKey(item), gun);
//            item.setGun(new Supplier(gun));
//        });
//
//        this.registeredGuns = builder.build();
//    }

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
        NetworkManager.instance = null;
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkManager networkGunManager = new NetworkManager();
        event.addListener(networkGunManager);
        NetworkManager.instance = networkGunManager;
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
    public static NetworkManager get() {
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
            Validate.notNull(NetworkManager.get());
            NetworkManager.get().writeRegisteredGuns(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registeredGuns = NetworkManager.readRegisteredGuns(buffer);
            NetworkManager.updateRegisteredGuns(registeredGuns);
            return Optional.empty();
        }
    }
}
