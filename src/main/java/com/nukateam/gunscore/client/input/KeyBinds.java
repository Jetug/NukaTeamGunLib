package com.nukateam.gunscore.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class KeyBinds {
    public static final KeyMapping KEY_RELOAD = new KeyMapping("key.gunscore.reload", GLFW.GLFW_KEY_R, "key.categories.gunscore");
    public static final KeyMapping KEY_UNLOAD = new KeyMapping("key.gunscore.unload", GLFW.GLFW_KEY_U, "key.categories.gunscore");
    public static final KeyMapping KEY_ATTACHMENTS = new KeyMapping("key.gunscore.attachments", GLFW.GLFW_KEY_Z, "key.categories.gunscore");

    public static void register(){
        for (KeyMapping key: getKeys())
            ClientRegistry.registerKeyBinding(key);
    }

    private static List<KeyMapping> getKeys() {
        List<KeyMapping> keys = new ArrayList<>();

        for (Field field: KeyBinds.class.getFields()) try {
            if (field.get(null) instanceof KeyMapping)
                keys.add((KeyMapping)field.get(null));
        } catch (IllegalAccessException ignored) {}

        return keys;
    }
}
