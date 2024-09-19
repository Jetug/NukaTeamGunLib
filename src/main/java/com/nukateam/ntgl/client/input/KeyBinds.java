package com.nukateam.ntgl.client.input;

import com.nukateam.ntgl.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class KeyBinds {
    public static final KeyMapping KEY_RELOAD = new KeyMapping("key.ntgl.reload", GLFW.GLFW_KEY_R, "key.categories.ntgl");
    public static final KeyMapping KEY_UNLOAD = new KeyMapping("key.ntgl.unload", GLFW.GLFW_KEY_U, "key.categories.ntgl");
    public static final KeyMapping KEY_ATTACHMENTS = new KeyMapping("key.ntgl.attachments", GLFW.GLFW_KEY_Z, "key.categories.ntgl");
    public static final KeyMapping KEY_INSPECT = new KeyMapping("key.ntgl.inspect", GLFW.GLFW_KEY_I, "key.categories.ntgl");
    public static final KeyMapping KEY_FIRE_SELECT = new KeyMapping("key.ntgl.fire_select", GLFW.GLFW_KEY_B, "key.categories.ntgl");
    public static final KeyMapping KEY_AMMO_SELECT = new KeyMapping("key.ntgl.ammo_select", GLFW.GLFW_KEY_N, "key.categories.ntgl");

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_RELOAD);
        event.register(KEY_UNLOAD);
        event.register(KEY_ATTACHMENTS);
        event.register(KEY_INSPECT);
        event.register(KEY_FIRE_SELECT);
        event.register(KEY_AMMO_SELECT);
    }

    public static KeyMapping getAimMapping() {
        Minecraft mc = Minecraft.getInstance();
        return Config.CLIENT.controls.flipControls.get() ? mc.options.keyAttack : mc.options.keyUse;
    }

    public static KeyMapping getShootMapping() {
        Minecraft mc = Minecraft.getInstance();
        return Config.CLIENT.controls.flipControls.get() ? mc.options.keyUse : mc.options.keyAttack;
    }
}
