package com.nukateam.ntgl.modules.wheel;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ActionWheelManager {
    private static final ActionWheelManager INSTANCE = new ActionWheelManager();
    private final ActionWheel wheel;

    private ActionWheelManager() {
        this.wheel = new ActionWheel(Minecraft.getInstance());
    }

    public static ActionWheelManager getInstance() {
        return INSTANCE;
    }

    public void onKeyPressed() {
        if (!wheel.isVisible()) {
            showDefaultWheel();
//            Minecraft.getInstance().setScreen(new ActionWheelScreen());
        }
    }

    public void onKeyReleased() {
        if (wheel.isVisible()) {
            wheel.hide();
            if (Minecraft.getInstance().screen instanceof ActionWheelScreen) {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    public void showDefaultWheel() {
        List<ActionWheel.WheelAction> actions = new ArrayList<>();

        actions.add(new ActionWheel.WheelAction(
                new ItemStack(Items.DIAMOND_SWORD),
                Component.literal("Атаковать"),
                () -> {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Выбрано: Атаковать"));
                },
                0xFF00FF00
        ));

        actions.add(new ActionWheel.WheelAction(
                new ItemStack(Items.SHIELD),
                Component.literal("Защита"),
                () -> {
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Выбрано: Защита"));
                },
                0xFF0000FF
        ));

        wheel.show(actions);
    }

    public void showCustomWheel(List<ActionWheel.WheelAction> actions) {
        wheel.show(actions);
        Minecraft.getInstance().setScreen(new ActionWheelScreen());
    }

    public ActionWheel getWheel() {
        return wheel;
    }

    public boolean isWheelActive() {
        return wheel.isVisible();
    }
}