package com.nukateam.ntgl.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class NtglKeyBinds {
    public static final KeyMapping KEY_RELOAD = new KeyMapping("key.ntgl.reload", GLFW.GLFW_KEY_R, "key.categories.ntgl");
    public static final KeyMapping KEY_UNLOAD = new KeyMapping("key.ntgl.unload", GLFW.GLFW_KEY_U, "key.categories.ntgl");
    public static final KeyMapping KEY_ATTACHMENTS = new KeyMapping("key.ntgl.attachments", GLFW.GLFW_KEY_Z, "key.categories.ntgl");
    public static final KeyMapping KEY_INSPECT = new KeyMapping("key.ntgl.inspect", GLFW.GLFW_KEY_I, "key.categories.ntgl");
    public static final KeyMapping KEY_FIRE_SELECT = new KeyMapping("key.ntgl.fire_select", GLFW.GLFW_KEY_B, "key.categories.ntgl");
    public static final KeyMapping KEY_AMMO_SELECT = new KeyMapping("key.ntgl.ammo_select", GLFW.GLFW_KEY_N, "key.categories.ntgl");
    public static final KeyMapping KEY_ADD_ATTACK = new KeyMapping("key.ntgl.melee", GLFW.GLFW_KEY_V, "key.categories.ntgl");
    public static final KeyMapping KEY_ALT_ATTACK = new KeyMapping("key.ntgl.alt", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.ntgl");
    public static final KeyMapping KEY_TIPS = new KeyMapping("key.ntgl.key_tips", GLFW.GLFW_KEY_F4, "key.categories.ntgl");

    public static final KeyMapping KEY_DEBUG_X_ADD = new KeyMapping("key.ntgl.debug_x_add", GLFW.GLFW_KEY_KP_1, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_Y_ADD = new KeyMapping("key.ntgl.debug_y_add", GLFW.GLFW_KEY_KP_3, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_Z_ADD = new KeyMapping("key.ntgl.debug_z_add", GLFW.GLFW_KEY_KP_5, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_X_SUB = new KeyMapping("key.ntgl.debug_x_sub", GLFW.GLFW_KEY_KP_2, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_Y_SUB = new KeyMapping("key.ntgl.debug_y_sub", GLFW.GLFW_KEY_KP_4, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_Z_SUB = new KeyMapping("key.ntgl.debug_z_sub", GLFW.GLFW_KEY_KP_6, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_ZERO = new KeyMapping("key.ntgl.debug_zero", GLFW.GLFW_KEY_KP_ENTER, "key.categories.ntgl");
    public static final KeyMapping KEY_DEBUG_SHOW = new KeyMapping("key.ntgl.debug_show", GLFW.GLFW_KEY_KP_MULTIPLY, "key.categories.ntgl");
    public static final KeyMapping LEAVE = new KeyMapping("key.ntgl.leave", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.ntgl.armor");


    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_RELOAD);
        event.register(KEY_UNLOAD);
        event.register(KEY_ATTACHMENTS);
        event.register(KEY_INSPECT);
        event.register(KEY_FIRE_SELECT);
        event.register(KEY_AMMO_SELECT);
        event.register(KEY_ADD_ATTACK);
        event.register(KEY_ALT_ATTACK);
        event.register(KEY_TIPS);
        event.register(LEAVE);

        if(Ntgl.isDebugging()){
            registerDebugKeys(event);
        }
    }

    private static void registerDebugKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_DEBUG_X_ADD);
        event.register(KEY_DEBUG_X_SUB);
        event.register(KEY_DEBUG_Y_ADD);
        event.register(KEY_DEBUG_Y_SUB);
        event.register(KEY_DEBUG_Z_ADD);
        event.register(KEY_DEBUG_Z_SUB);
        event.register(KEY_DEBUG_ZERO);
        event.register(KEY_DEBUG_SHOW);
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
