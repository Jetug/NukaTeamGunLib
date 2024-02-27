package com.nukateam.gunscore.client.input;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;

/**
 * Author: MrCrayfish
 */
public class GunButtonBindings {
    public static final ButtonBinding SHOOT = new ButtonBinding(Buttons.RIGHT_TRIGGER, "gunscore.button.shoot", "button.categories.gunscore", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding AIM = new ButtonBinding(Buttons.LEFT_TRIGGER, "gunscore.button.aim", "button.categories.gunscore", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding RELOAD = new ButtonBinding(Buttons.X, "gunscore.button.reload", "button.categories.gunscore", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding OPEN_ATTACHMENTS = new ButtonBinding(Buttons.B, "gunscore.button.attachments", "button.categories.gunscore", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding STEADY_AIM = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "gunscore.button.steadyAim", "button.categories.gunscore", GunConflictContext.IN_GAME_HOLDING_WEAPON);

    public static void register() {
        BindingRegistry.getInstance().register(SHOOT);
        BindingRegistry.getInstance().register(AIM);
        BindingRegistry.getInstance().register(RELOAD);
        BindingRegistry.getInstance().register(OPEN_ATTACHMENTS);
        BindingRegistry.getInstance().register(STEADY_AIM);
    }
}
