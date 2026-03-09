package com.nukateam.ntgl.modules.datapack.managers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.modules.constants.Paths;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.modules.datapack.ConfigUtils;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateWeapons;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

public class NetworkWeaponManager extends SimplePreparableReloadListener<Map<IWeapon, WeaponConfig>> {
    private static final List<IWeapon> clientRegisteredWeapons = new ArrayList<>();
    private static NetworkWeaponManager instance;

    private Map<ResourceLocation, WeaponConfig> registeredWeapons = new HashMap<>();

    public static void onServerStopped() {
        NetworkWeaponManager.instance = null;
    }

    public static void register(AddReloadListenerEvent event) {
        NetworkWeaponManager networkWeaponManager = new NetworkWeaponManager();
        event.addListener(networkWeaponManager);
        NetworkWeaponManager.instance = networkWeaponManager;
    }

    @Override
    protected Map<IWeapon, WeaponConfig> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<IWeapon, WeaponConfig> map = ConfigUtils.getConfigMap(manager, BuiltInRegistries.ITEM, (v) -> v instanceof IWeapon, WeaponConfig.class, Paths.WEAPONS);
        return map;
    }

    @Override
    protected void apply(Map<IWeapon, WeaponConfig> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, WeaponConfig> builder = ImmutableMap.builder();

        objects.forEach((abstractItem, gun) -> {
            if(abstractItem instanceof Item item) {
                Validate.notNull(BuiltInRegistries.ITEM.getKey(item));
                builder.put(BuiltInRegistries.ITEM.getKey(item), gun);
                abstractItem.setConfig(new ConfigSupplier<>(gun));
            }
        });

        this.registeredWeapons = builder.build();
    }

    /**
     * Writes all registered weapons into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeRegisteredGuns(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.registeredWeapons.size());
        this.registeredWeapons.forEach((id, gun) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(gun.serializeNBT(null));
        });
    }

    /**
     * Reads all registered weapon from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered weapons from the server
     */
    public static ImmutableMap<ResourceLocation, WeaponConfig> readRegisteredWeapons(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();

        if (size > 0) {
            ImmutableMap.Builder<ResourceLocation, WeaponConfig> builder = ImmutableMap.builder();

            for (int i = 0; i < size; i++) {
                var id = buffer.readResourceLocation();
                WeaponConfig weaponConfig = WeaponConfig.create(id, buffer.readNbt());
                builder.put(id, weaponConfig);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredWeapons(S2CMessageUpdateWeapons message) {
        return updateRegisteredWeapons(message.getRegisteredGuns());
    }

    /**
     * Updates registered weapons from data provided by the server
     *
     * @return true if all registered weapons were able to update their corresponding weapon item
     */
    private static boolean updateRegisteredWeapons(Map<ResourceLocation, WeaponConfig> registeredConfigs) {
        clientRegisteredWeapons.clear();
        if (registeredConfigs != null) {
            for (Map.Entry<ResourceLocation, WeaponConfig> entry : registeredConfigs.entrySet()) {
                var item = BuiltInRegistries.ITEM.get(entry.getKey());
                if (!(item instanceof IWeapon)) {
                    return false;
                }
                ((IWeapon) item).setConfig(new ConfigSupplier<>(entry.getValue()));
                clientRegisteredWeapons.add((IWeapon) item);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a map of all the registered weapons objects. Note, this is an immutable map.
     *
     * @return a map of registered weapon objects
     */
    public Map<ResourceLocation, WeaponConfig> getRegisteredWeapons() {
        return this.registeredWeapons;
    }

    /**
     * Gets a list of all the weapons registered on the client side. Note, this is an immutable list.
     *
     * @return a list of weapons registered on the client
     */
    public static List<IWeapon> getClientRegisteredWeapons() {
        return ImmutableList.copyOf(clientRegisteredWeapons);
    }


    /**
     * Gets the network weapon manager. This will be null if the client isn't running an integrated
     * server or the client is connected to a dedicated server.
     *
     * @return the network weapon manager
     */
    @Nullable
    public static NetworkWeaponManager get() {
        return instance;
    }

}
