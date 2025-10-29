package com.nukateam.ntgl.client.settings;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static net.minecraft.client.Options.genericValueLabel;

public class OptionInstances {
    public static OptionInstance<Double> createSensitivitySlider() {
        return new OptionInstance<>(
                "ntgl.options.adsSensitivity",
                OptionInstance.noTooltip(),
                OptionInstances::createLabel,
                OptionInstance.UnitDouble.INSTANCE,
                Ntgl.getOptions().getAdsSensitivity(),
                OptionInstances::onSensitivityChanged
        );
    }

    public static OptionInstance<Double> createSoundSlider() {
        return new OptionInstance<>(
                "ntgl.options.gun_volume",
                OptionInstance.noTooltip(),
                OptionInstances::createLabel,
                OptionInstance.UnitDouble.INSTANCE,
                Ntgl.getOptions().getGunVolume() ,
                OptionInstances::onGunVolumeChanged
        );
    }

    private static Component createLabel(Component component, Double value) {
        return value == 0.0D ?
                genericValueLabel(component, CommonComponents.OPTION_OFF) :
                percentValueLabel(component, value);
    }

    private static void onGunVolumeChanged(Double value) {
        Ntgl.getOptions().setGunVolume(value);
        Ntgl.getOptions().saveOptions();
    }

    private static void onSensitivityChanged(Double value) {
        Ntgl.getOptions().setAdsSensitivity(value);
        Ntgl.getOptions().saveOptions();
    }

    private static Component percentValueLabel(Component p_231898_, double p_231899_) {
        return Component.translatable("options.percent_value", p_231898_, (int)(p_231899_ * 100.0D));
    }
}
