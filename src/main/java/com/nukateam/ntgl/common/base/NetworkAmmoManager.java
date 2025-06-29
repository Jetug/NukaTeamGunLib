package com.nukateam.ntgl.common.base;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.config.Projectile;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
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

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkAmmoManager extends SimplePreparableReloadListener<Map<IAmmo, Projectile>> {
    public static final String PATH = "ammo";
    private static final List<IAmmo> clientRegisteredAmmo = new ArrayList<>();
    private static NetworkAmmoManager instance;

    private Map<ResourceLocation, Projectile> registeredAmmo = new HashMap<>();

    @Override
    protected Map<IAmmo, Projectile> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return ConfigUtils.getConfigMap(manager, (v) -> v instanceof IAmmo, Projectile.class, PATH);
    }

    @Override
    protected void apply(Map<IAmmo, Projectile> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, Projectile> builder = ImmutableMap.builder();

        objects.forEach((item, ammo) -> {
            Validate.notNull(ITEMS.getKey((Item)item));
            builder.put(ITEMS.getKey((Item)item), ammo);
            item.setConfig(new ConfigSupplier<>(ammo));
        });

        this.registeredAmmo = builder.build();
    }

    /**
     * Writes all registered projectile into the provided packet buffer
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
     * Reads all registered projectile from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered projectile from the server
     */
    public static ImmutableMap<ResourceLocation, Projectile> readRegisteredAmmo(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            ImmutableMap.Builder<ResourceLocation, Projectile> builder = ImmutableMap.builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                var ammo = Projectile.create(buffer.readNbt());
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
     * Updates registered projectile from data provided by the server
     *
     * @return true if all registered projectile were able to update their corresponding projectile item
     */
    private static boolean updateRegisteredAmmo(Map<ResourceLocation, Projectile> registeredAmmo) {
        clientRegisteredAmmo.clear();
        if (registeredAmmo != null) {
            for (Map.Entry<ResourceLocation, Projectile> entry : registeredAmmo.entrySet()) {
                Item item = ITEMS.getValue(entry.getKey());
                if (!(item instanceof IAmmo)) {
                    return false;
                }
                ((IAmmo) item).setConfig(new ConfigSupplier<>(entry.getValue()));
                clientRegisteredAmmo.add((IAmmo) item);
            }
            return true;
        }
        return false;
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
     * Gets the network projectile manager. This will be null if the client isn't running an integrated
     * server or the client is connected to a dedicated server.
     *
     * @return the network projectile manager
     */
    @Nullable
    public static NetworkAmmoManager get() {
        return instance;
    }

    /**
     * A simple wrapper for a projectile object to pass to IAmmo. This is to indicate to developers that
     * Projectile instances shouldn't be changed on GunItems as they are controlled by NetworkAmmoManager.
     * Changes to projectile properties should be made through the JSON file.
     */
    public static class Supplier {
        private Projectile projectile;

        private Supplier(Projectile projectile) {
            this.projectile = projectile;
        }

        public Projectile getAmmo() {
            return this.projectile;
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
