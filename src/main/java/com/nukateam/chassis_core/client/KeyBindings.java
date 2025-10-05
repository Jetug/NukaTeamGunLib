package com.nukateam.chassis_core.client;

import com.nukateam.chassis_core.ChassisCore;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping LEAVE = new KeyMapping("key.leave", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.armor");

    public static final KeyMapping KEY_DEBUG_X_ADD = new KeyMapping("key.chassiscore.debug_x_add", GLFW.GLFW_KEY_KP_1, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_Y_ADD = new KeyMapping("key.chassiscore.debug_y_add", GLFW.GLFW_KEY_KP_3, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_Z_ADD = new KeyMapping("key.chassiscore.debug_z_add", GLFW.GLFW_KEY_KP_5, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_X_SUB = new KeyMapping("key.chassiscore.debug_x_sub", GLFW.GLFW_KEY_KP_2, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_Y_SUB = new KeyMapping("key.chassiscore.debug_y_sub", GLFW.GLFW_KEY_KP_4, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_Z_SUB = new KeyMapping("key.chassiscore.debug_z_sub", GLFW.GLFW_KEY_KP_6, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_ZERO = new KeyMapping( "key.chassiscore.debug_zero", GLFW.GLFW_KEY_KP_ENTER, "key.categories.armor");
    public static final KeyMapping KEY_DEBUG_SHOW = new KeyMapping( "key.chassiscore.debug_show", GLFW.GLFW_KEY_KP_MULTIPLY, "key.categories.armor");

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(LEAVE);

        if(ChassisCore.isDebugging()){
            event.register(KEY_DEBUG_X_ADD);
            event.register(KEY_DEBUG_X_SUB);
            event.register(KEY_DEBUG_Y_ADD);
            event.register(KEY_DEBUG_Y_SUB);
            event.register(KEY_DEBUG_Z_ADD);
            event.register(KEY_DEBUG_Z_SUB);
            event.register(KEY_DEBUG_ZERO);
            event.register(KEY_DEBUG_SHOW);
        }
    }

//    private static List<KeyMapping> getKeys() {
//        List<KeyMapping> keys = new ArrayList<>();
//
//        for (Field field : KeyBindings.class.getFields())
//            try {
//                if (field.get(null) instanceof KeyMapping)
//                    keys.add((KeyMapping) field.get(null));
//            } catch (IllegalAccessException ignored) {}
//        return keys;
//    }
}
