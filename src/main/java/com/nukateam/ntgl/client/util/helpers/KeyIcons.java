package com.nukateam.ntgl.client.util.helpers;

import com.mojang.blaze3d.platform.InputConstants;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

public class KeyIcons {
    public static final int MOUSE_BUTTON_4 = 3;
    public static final int MOUSE_BUTTON_5 = 4;
    public static final int ENTER = 257;

    private static final HashMap<Integer, ResourceLocation> icons = new HashMap<>();

    static {
        icons.put(InputConstants.MOUSE_BUTTON_RIGHT, getIcon("rmb"));
        icons.put(InputConstants.MOUSE_BUTTON_LEFT, getIcon("lmb"));
        icons.put(InputConstants.MOUSE_BUTTON_MIDDLE, getIcon("mmb"));
        icons.put(MOUSE_BUTTON_4, getIcon("b4"));
        icons.put(MOUSE_BUTTON_5, getIcon("b5"));
        icons.put(InputConstants.KEY_LSHIFT, getIcon("r_shift"));
        icons.put(InputConstants.KEY_RSHIFT, getIcon("l_shift"));

        icons.put(InputConstants.KEY_LALT, getIcon("l_alt"));
        icons.put(InputConstants.KEY_RALT, getIcon("r_alt"));
        icons.put(InputConstants.KEY_LCONTROL, getIcon("l_ctrl"));
        icons.put(InputConstants.KEY_RCONTROL, getIcon("r_ctrl"));

        icons.put(InputConstants.KEY_NUMPADENTER, getIcon("enter"));
        icons.put(ENTER, getIcon("enter"));
        icons.put(InputConstants.KEY_SPACE, getIcon("space"));
        icons.put(InputConstants.KEY_BACKSPACE, getIcon("backspace"));

        icons.put(InputConstants.KEY_LEFT   , getIcon("l_arrow"));
        icons.put(InputConstants.KEY_RIGHT  , getIcon("r_arrow"));
        icons.put(InputConstants.KEY_UP     , getIcon("up_arrow"));
        icons.put(InputConstants.KEY_DOWN   , getIcon("down_arrow"));
    }

    @Nullable
    public static ResourceLocation getIcon(int key){
        return icons.get(key);
    }

    private static ResourceLocation getIcon(String name){
        return ResourceLocation.tryBuild(Ntgl.MOD_ID,"textures/hud/keys/" + name + ".png");
    }
}
