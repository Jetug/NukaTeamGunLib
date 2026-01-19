package com.nukateam.ntgl.modules.wheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ActionWheel {

    private final List<WheelAction> actions = new ArrayList<>();
    private boolean isVisible = false;
    private int selectedSegment = -1;
    private long openTime = 0;
    private final Minecraft minecraft;

    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private double accumulatedDeltaX = 0;
    private double accumulatedDeltaY = 0;
    private static final double MOVEMENT_THRESHOLD = 2.0;
    private static final double MOVEMENT_DECAY = 0.7;

    public static class WheelAction {
        private ResourceLocation icon;
        private Component title = Component.literal("");
        private Runnable action = () -> {};
        private int color = 0xC6C6C6FF;

        public WheelAction() {}

        public ResourceLocation getIcon() { return icon; }
        public Component getTitle() { return title; }
        public Runnable getAction() { return action; }
        public int getColor() { return color; }


        public WheelAction setIcon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public WheelAction setTitle(Component title) {
            this.title = title;
            return this;
        }

        public WheelAction setAction(Runnable action) {
            this.action = action;
            return this;
        }

        public WheelAction setColor(int color) {
            this.color = color;
            return this;
        }
    }

    public ActionWheel(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void show(List<WheelAction> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        this.isVisible = true;
        this.openTime = System.currentTimeMillis();
        this.selectedSegment = -1;
        this.lastMouseX = -1;
        this.lastMouseY = -1;
        this.accumulatedDeltaX = 0;
        this.accumulatedDeltaY = 0;
    }

    public void hide() {
        if (isVisible) {
            isVisible = false;
            if (selectedSegment >= 0 && selectedSegment < actions.size()) {
                actions.get(selectedSegment).getAction().run();
            }
        }
    }



    public void updateMousePosition(double mouseX, double mouseY) {
        if (!isVisible || actions.isEmpty()) {
            selectedSegment = -1;
            return;
        }

        if (lastMouseX == -1 || lastMouseY == -1) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return;
        }

        double deltaX = mouseX - lastMouseX;
        double deltaY = mouseY - lastMouseY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        double movement = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (movement < MOVEMENT_THRESHOLD) {
            accumulatedDeltaX *= MOVEMENT_DECAY;
            accumulatedDeltaY *= MOVEMENT_DECAY;

            if (Math.abs(accumulatedDeltaX) < 0.1 && Math.abs(accumulatedDeltaY) < 0.1) {
                return;
            }
        } else {
            accumulatedDeltaX += deltaX;
            accumulatedDeltaY += deltaY;
        }

        double angle = getMovementAngle(accumulatedDeltaX, accumulatedDeltaY);

        int count = actions.size();
        float anglePerSegment = 360.0f / count;

        angle = (angle + 360) % 360;

        selectedSegment = (int) (angle / anglePerSegment);

        if (selectedSegment >= count) {
            selectedSegment = count - 1;
        }
    }

    private double getMovementAngle(double deltaX, double deltaY) {
        double movement = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (movement < 0.1) {
            return -1; // Не выбирать сегмент
        }

        double angleRad = Math.atan2(deltaY, deltaX);
        double angleDeg = Math.toDegrees(angleRad);

        angleDeg += 90;

        if (angleDeg < 0) angleDeg += 360;

        return angleDeg;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public List<WheelAction> getActions() {
        return actions;
    }

    public long getOpenTime() {
        return openTime;
    }

    public int getSelectedSegment() {
        return selectedSegment;
    }
}