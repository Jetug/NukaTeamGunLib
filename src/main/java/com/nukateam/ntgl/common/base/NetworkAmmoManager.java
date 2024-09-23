package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.foundation.item.AmmoItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateAmmo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
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
public class NetworkAmmoManager extends SimplePreparableReloadListener<Map<AmmoItem, Ammo>> {
    private static List<AmmoItem> clientRegisteredAmmo = new ArrayList<>();
    private static NetworkAmmoManager instance;

    private Map<ResourceLocation, Ammo> registeredAmmo = new HashMap<>();

    @Override
    protected Map<AmmoItem, Ammo> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, (v) -> v instanceof AmmoItem, Ammo.class, "ammo");
    }

    @Override
    protected void apply(Map<AmmoItem, Ammo> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, Ammo> builder = ImmutableMap.builder();

        objects.forEach((item, ammo) -> {
            Validate.notNull(ITEMS.getKey(item));
            builder.put(ITEMS.getKey(item), ammo);
            item.setAmmo(new Supplier(ammo));
        });

        this.registeredAmmo = builder.build();
    }

    /**
     * Writes all registered ammo into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeRegisteredAmmo(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.registeredAmmo.size());
        this.registeredAmmo.forEach((id, ammo) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(ammo.serializeNBT());
        });
    }

    /**
     * Reads all registered ammo from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered ammo from the server
     */
    public static ImmutableMap<ResourceLocation, Ammo> readRegisteredAmmo(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            ImmutableMap.Builder<ResourceLocation, Ammo> builder = ImmutableMap.builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var ammo = Ammo.create(buffer.readNbt());
                builder.put(id, ammo);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredAmmo(S2CMessageUpdateAmmo message) {
        return updateRegisteredAmmo(message.getRegisteredAmmo());
    }

    /**
     * Updates registered ammo from data provided by the server
     *
     * @return true if all registered ammo were able to update their corresponding ammo item
     */
    private static boolean updateRegisteredAmmo(Map<ResourceLocation, Ammo> registeredAmmo) {
        clientRegisteredAmmo.clear();
        if (registeredAmmo != null) {
            for (Map.Entry<ResourceLocation, Ammo> entry : registeredAmmo.entrySet()) {
                Item item = ITEMS.getValue(entry.getKey());
                if (!(item instanceof AmmoItem)) {
                    return false;
                }
                ((AmmoItem) item).setAmmo(new Supplier(entry.getValue()));
                clientRegisteredAmmo.add((AmmoItem) item);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a map of all the registered ammo objects. Note, this is an immutable map.
     *
     * @return a map of registered ammo objects
     */
    public Map<ResourceLocation, Ammo> getRegisteredAmmo() {
        return this.registeredAmmo;
    }

    /**
     * Gets a list of all the ammo registered on the client side. Note, this is an immutable list.
     *
     * @return a map of ammo registered on the client
     */
    public static List<AmmoItem> getClientRegisteredAmmo() {
        return ImmutableList.copyOf(clientRegisteredAmmo);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkAmmoManager.instance = null;
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkAmmoManager networkGunManager = new NetworkAmmoManager();
        event.addListener(networkGunManager);
        NetworkAmmoManager.instance = networkGunManager;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAmmo());
        }
    }

    /**
     * Gets the network ammo manager. This will be null if the client isn't running an integrated
     * server or the client is connected to a dedicated server.
     *
     * @return the network ammo manager
     */
    @Nullable
    public static NetworkAmmoManager get() {
        return instance;
    }

    /**
     * A simple wrapper for a ammo object to pass to AmmoItem. This is to indicate to developers that
     * Ammo instances shouldn't be changed on GunItems as they are controlled by NetworkAmmoManager.
     * Changes to ammo properties should be made through the JSON file.
     */
    public static class Supplier {
        private Ammo ammo;

        private Supplier(Ammo ammo) {
            this.ammo = ammo;
        }

        public Ammo getAmmo() {
            return this.ammo;
        }
    }

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkAmmoManager.get());
            NetworkAmmoManager.get().writeRegisteredAmmo(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registeredAmmo = NetworkAmmoManager.readRegisteredAmmo(buffer);
            NetworkAmmoManager.updateRegisteredAmmo(registeredAmmo);
            return Optional.empty();
        }
    }
}
