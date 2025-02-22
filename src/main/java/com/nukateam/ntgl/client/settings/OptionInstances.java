package com.nukateam.ntgl.client.settings;

import com.electronwill.nightconfig.core.io.WritingException;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static net.minecraft.client.Options.genericValueLabel;

public class OptionInstances {
    //    public static final OptionInstance<Double> ADS_SENSITIVITY
//            = new GunSliderPercentageOption("cgm.options.adsSensitivity",
//            0.0, 2.0, 0.01F, gameSettings -> {
//        return Ntgl.getOptions().adsSensitivity;
//    }, (gameSettings, value) -> {
//        Ntgl.getOptions().adsSensitivity = MathHelper.clamp(value, 0.0, 2.0);
//    }, (gameSettings, option) -> {
//        double adsSensitivity = Ntgl.getOptions().adsSensitivity;
//        return I18n.format("cgm.options.adsSensitivity.format", FORMAT.format(adsSensitivity));
//    });

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
                OptionInstances::onValueChanged
        );
    }

    private static Component createLabel(Component component, Double value) {
        return value == 0.0D ?
                genericValueLabel(component, CommonComponents.OPTION_OFF) :
                percentValueLabel(component, value);
    }

    private static void onValueChanged(Double value) {
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
