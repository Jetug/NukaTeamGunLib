package com.nukateam.ntgl.client.input;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.context.InGameContext;
import com.mrcrayfish.controllable.client.binding.handlers.TickingHandler;
import com.mrcrayfish.controllable.client.binding.handlers.impl.DropHandler;
import com.mrcrayfish.controllable.client.input.Buttons;
import org.jetbrains.annotations.NotNull;

import javax.naming.ldap.Control;

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
        BindingRegistry registry = Controllable.getBindingRegistry();
        registry.register(SHOOT);
        registry.register(AIM);
        registry.register(RELOAD);
        registry.register(OPEN_ATTACHMENTS);
        registry.register(STEADY_AIM);
        registry.register(INSPECT);
        registry.register(SELECT_FIRE);
        registry.register(SELECT_AMMO);
    }

    private static @NotNull ButtonBinding createGunBinding(int button, String name) {
        return new ButtonBinding(button, "ntgl.button." + name,
                CATEGORIES_NTGL, InGameContext.INSTANCE, new DropHandler());
    }
}
