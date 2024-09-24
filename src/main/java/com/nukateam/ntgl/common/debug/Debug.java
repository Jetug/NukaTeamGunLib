package com.nukateam.ntgl.common.debug;

import com.nukateam.ntgl.client.ClientHandler;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.debug.screen.widget.DebugToggle;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    private static final Map<Item, Gun> GUNS = new HashMap<>();
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

    public static Gun getGun(GunItem item) {
        return GUNS.computeIfAbsent(item, item1 -> item.getGun().copy());
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
                ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
                if (heldItem.getItem() instanceof GunItem gunItem) {
                    widgets.add(Pair.of(Component.translatable(gunItem.getDescriptionId()), () -> new DebugButton(Component.literal("Edit"), btn -> {
                        Minecraft.getInstance().setScreen(ClientHandler.createEditorScreen(getGun(gunItem)));
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
