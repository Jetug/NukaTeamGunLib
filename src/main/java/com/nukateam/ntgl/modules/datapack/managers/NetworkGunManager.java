package com.nukateam.ntgl.modules.datapack.managers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.modules.datapack.DataUtils;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateGuns;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.*;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkGunManager extends SimplePreparableReloadListener<Map<IWeapon, Gun>> {
    private static final List<IWeapon> clientRegisteredGuns = new ArrayList<>();
    private static NetworkGunManager instance;

    private Map<ResourceLocation, Gun> registeredGuns = new HashMap<>();

    public static void onServerStopped() {
        NetworkGunManager.instance = null;
    }

    public static void register(AddReloadListenerEvent event) {
        NetworkGunManager networkGunManager = new NetworkGunManager();
        event.addListener(networkGunManager);
        NetworkGunManager.instance = networkGunManager;
    }

    @Override
    protected Map<IWeapon, Gun> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return DataUtils.getConfigMap(manager, (v) -> v instanceof IWeapon, Gun.class, "guns");
    }

    @Override
    protected void apply(Map<IWeapon, Gun> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, Gun> builder = ImmutableMap.builder();

        objects.forEach((abstractItem, gun) -> {
            if(abstractItem instanceof Item item) {
                Validate.notNull(ITEMS.getKey(item));
                builder.put(ITEMS.getKey(item), gun);
                abstractItem.setConfig(new ConfigSupplier<>(gun));
            }
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
                Gun gun = Gun.create(id, buffer.readNbt());
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
                if (!(item instanceof IWeapon)) {
                    return false;
                }
                ((IWeapon) item).setConfig(new ConfigSupplier<>(entry.getValue()));
                clientRegisteredGuns.add((IWeapon) item);
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
     * @return a list of guns registered on the client
     */
    public static List<IWeapon> getClientRegisteredGuns() {
        return ImmutableList.copyOf(clientRegisteredGuns);
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

    public static class LoginData implements ILoginData {
        @Override
        public void writeData(FriendlyByteBuf buffer) {
            Validate.notNull(NetworkGunManager.get());
            NetworkGunManager.get().writeRegisteredGuns(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer) {
            var registeredGuns = NetworkGunManager.readRegisteredGuns(buffer);
            NetworkGunManager.updateRegisteredGuns(registeredGuns);
            return Optional.empty();
        }
    }
}
