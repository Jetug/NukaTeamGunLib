package com.nukateam.ntgl.client.input;

import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.input.Buttons;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class GunButtonBindings {
    public static final String CATEGORIES_NTGL = "button.categories.ntgl";
    
    public static final ButtonBinding SHOOT = createGunBinding(Buttons.RIGHT_TRIGGER, "shoot");
    public static final ButtonBinding AIM = createGunBinding(Buttons.LEFT_TRIGGER, "aim");
    public static final ButtonBinding RELOAD = createGunBinding(Buttons.B, "reload");
    public static final ButtonBinding OPEN_ATTACHMENTS = createGunBinding(Buttons.DPAD_LEFT, "attachments");
    public static final ButtonBinding STEADY_AIM = createGunBinding(Buttons.RIGHT_THUMB_STICK, "steadyAim");
    public static final ButtonBinding INSPECT = createGunBinding(Buttons.MISC, "inspect");
    public static final ButtonBinding SELECT_FIRE = createGunBinding(Buttons.MISC,"fire_select");
    public static final ButtonBinding SELECT_AMMO = createGunBinding(Buttons.MISC, "ammo_select");

    public static void register() {
        BindingRegistry.getInstance().register(SHOOT);
        BindingRegistry.getInstance().register(AIM);
        BindingRegistry.getInstance().register(RELOAD);
        BindingRegistry.getInstance().register(OPEN_ATTACHMENTS);
        BindingRegistry.getInstance().register(STEADY_AIM);
        BindingRegistry.getInstance().register(INSPECT);
        BindingRegistry.getInstance().register(SELECT_FIRE);
        BindingRegistry.getInstance().register(SELECT_AMMO);
    }

    private static @NotNull ButtonBinding createGunBinding(int button, String name) {
        return new ButtonBinding(button, "ntgl.button." + name,
                CATEGORIES_NTGL, GunConflictContext.IN_GAME_HOLDING_WEAPON);
    }
}