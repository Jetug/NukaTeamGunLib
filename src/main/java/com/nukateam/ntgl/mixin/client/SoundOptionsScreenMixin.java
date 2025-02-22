package com.nukateam.ntgl.mixin.client;

import com.electronwill.nightconfig.core.io.WritingException;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

import static com.nukateam.ntgl.client.settings.OptionInstances.*;
import static net.minecraft.client.Options.genericValueLabel;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenMixin extends Screen {
    @Shadow private OptionsList list;

    protected SoundOptionsScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(method = "getAllSoundOptionsExceptMaster", at = @At("RETURN"), cancellable = true)
    private void getAllSoundOptionsExceptMaster(CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        var result = cir.getReturnValue();
        result = Arrays.copyOf(result, result.length + 1);
        result[result.length - 1] = createSoundSlider();

        cir.setReturnValue(result);
    }
}