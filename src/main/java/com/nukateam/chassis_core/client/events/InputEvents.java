package com.nukateam.chassis_core.client.events;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.client.KeyBindings;
import com.nukateam.chassis_core.client.utils.KeyUtils;
import com.nukateam.chassis_core.common.input.CommonInputHandler;
import com.nukateam.chassis_core.common.input.KeyAction;
import com.nukateam.chassis_core.common.network.actions.InputAction;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static com.nukateam.chassis_core.client.ClientConfig.OPTIONS;
import static com.nukateam.chassis_core.client.KeyBindings.*;
import static com.nukateam.chassis_core.common.network.PacketSender.doServerAction;
import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.getLocalPlayer;
import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.stopWearingArmor;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InputEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onKeyInput(InputEvent.Key event) {
        if (isNotInGame()) return;

        KeyAction action;
        if (event.getAction() == GLFW.GLFW_PRESS) {
            action = KeyAction.PRESS;
            if (event.getKey() == KeyBindings.LEAVE.getKey().getValue())
                stopWearingArmor(Minecraft.getInstance().player);
        } else if (event.getAction() == GLFW.GLFW_RELEASE)
            action = KeyAction.RELEASE;
        else
            action = KeyAction.REPEAT;

        handleInput(event.getKey(), action);
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onMouseKeyInput(InputEvent.MouseButton event) {
        switch (event.getAction()) {
            case GLFW.GLFW_PRESS -> {

            }
            case GLFW.GLFW_RELEASE -> {
                if (event.getButton() != OPTIONS.keyUse.getKey().getValue() && isNotInGame()) return;
                handleInput(event.getButton(), KeyAction.RELEASE);
            }
        }
    }

    public static void onDoubleClick(InputEvent.Key event) {
        if (isNotInGame()) return;
        handleInput(event.getKey(), KeyAction.DOUBLE_CLICK);
    }

    public static void onLongClick(int key, int ticks) {
        if (isNotInGame()) return;
        handleInput(key, KeyAction.LONG_PRESS);
    }

    public static void onLongRelease(int key, int ticks) {
    }

    private static void handleInput(int key, KeyAction action) {
        var inputKey = KeyUtils.getByKey(key);
        if (getLocalPlayer() == null || inputKey == null) return;

        doServerAction(new InputAction(inputKey, action), -1);
        CommonInputHandler.onKeyInput(inputKey, action, getLocalPlayer());
    }

    public static boolean isNotInGame() {
        return Minecraft.getInstance().screen != null;
    }
}
