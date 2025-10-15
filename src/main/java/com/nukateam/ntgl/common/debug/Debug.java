package com.nukateam.ntgl.common.debug;

import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.debug.screen.widget.DebugToggle;

import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
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

    public static class Menu implements IEditorMenu {
        @Override
        public Component getEditorLabel() {
            return Component.literal("Editor Menu");
        }

        @Override
        public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
                if (heldItem.getItem() instanceof IWeapon weaponItem) {
                    widgets.add(Pair.of(Component.translatable(heldItem.getDescriptionId()), () -> new DebugButton(Component.literal("Edit"), btn -> {
                        Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(getGun(weaponItem)));
                    })));
                }
                widgets.add(Pair.of(Component.literal("Settings"), () -> new DebugButton(Component.literal(">"), btn -> {
                    Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(new Settings()));
                })));
            });
        }
    }

    public static class Settings implements IEditorMenu {
        @Override
        public Component getEditorLabel() {
            return Component.literal("Settings");
        }

        @Override
        public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                widgets.add(Pair.of(Component.literal("Force Aim"), () -> new DebugToggle(Debug.forceAim, value -> Debug.forceAim = value)));
            });
        }
    }
}
