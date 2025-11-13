package com.nukateam.ntgl.common.debug;

import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;

import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class Debug {
    private static final Map<IWeapon, WeaponConfig> GUNS = new HashMap<>();
    private static final Map<Item, Scope> SCOPES = new HashMap<>();
    private static boolean forceAim = false;

    @SubscribeEvent
    public static void onServerStarting(ServerStartedEvent event) {
        // Resets the cache every time when joining a world
        event.getServer().execute(() ->
        {
            GUNS.clear();
            SCOPES.clear();
        });
    }

    public static WeaponConfig getGun(IWeapon item) {
        return GUNS.computeIfAbsent(item, item1 -> item.getConfig().copy());
    }

    public static Scope getScope(ScopeItem item) {
        return SCOPES.computeIfAbsent(item, item1 -> item.getProperties().copy());
    }

    public static boolean isForceAim() {
        return forceAim;
    }

    public static void setForceAim(boolean forceAim) {
        Debug.forceAim = forceAim;
    }
}
