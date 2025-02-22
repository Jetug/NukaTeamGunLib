package com.nukateam.ntgl.client.render.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class OptionInstanceSliderButton extends AbstractOptionSliderButton {
    private final double instance;
//    private final OptionInstance.SliderableValueSet<Double> values;
//    private final Consumer<Double> onValueChanged;

    public OptionInstanceSliderButton(int pX, int pY, int pWidth, int pHeight, double pInstance) {
        super(Minecraft.getInstance().options, pX, pY, pWidth, pHeight, 0d);
        this.instance = pInstance;
//        this.values = pValues;
//        this.onValueChanged = pOnValueChanged;
        this.updateMessage();
    }

    protected void updateMessage() {
//        this.setMessage(this.instance.toString.apply(this.instance.get()));
//        this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
    }

    protected void applyValue() {
//        this.instance.set(1d);
//        this.options.save();
//        this.onValueChanged.accept(this.instance.get());
    }
}
