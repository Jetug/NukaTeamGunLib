package com.nukateam.ntgl.modules.wheel;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class ActionWheel {
    private static final double MOVEMENT_THRESHOLD = 2.0;
    private static final double MOVEMENT_DECAY = 0.7;

    private final List<WheelAction> actions = new ArrayList<>();
    private Component title;
    private boolean isVisible = false;
    private int selectedSegment = -1;
    private long openTime = 0;
    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private double accumulatedDeltaX = 0;
    private double accumulatedDeltaY = 0;
    private Runnable defaultAction = () -> {};

    public ActionWheel() {}

    public Component getTitle() {
        return title;
    }

    public List<WheelAction> getActions() {
        return actions;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public long getOpenTime() {
        return openTime;
    }

    public int getSelectedSegment() {
        return selectedSegment;
    }

    public void show(List<WheelAction> actions, Component title, Runnable defaultAction) {
        this.actions.clear();
        this.actions.addAll(actions);
        this.defaultAction = defaultAction;
        this.title = title;
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
            else {
                defaultAction.run();
            }
            defaultAction = () -> {};
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

        var deltaX = mouseX - lastMouseX;
        var deltaY = mouseY - lastMouseY;
        var movement = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        lastMouseX = mouseX;
        lastMouseY = mouseY;

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

        var angle = getMovementAngle(accumulatedDeltaX, accumulatedDeltaY);
        var count = actions.size();
        var anglePerSegment = 360.0f / count;

        angle = (angle + 360) % 360;

        selectedSegment = (int) (angle / anglePerSegment);

        if (selectedSegment >= count) {
            selectedSegment = count - 1;
        }
    }

    private double getMovementAngle(double deltaX, double deltaY) {
        var movement = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (movement < 0.1)
            return -1;

        var angleRad = Math.atan2(deltaY, deltaX);
        var angleDeg = Math.toDegrees(angleRad);

        angleDeg += 90;

        if (angleDeg < 0) angleDeg += 360;

        return angleDeg;
    }

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
}