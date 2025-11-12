package com.nukateam.chassis_core.common.util.helpers;

import static com.nukateam.chassis_core.common.util.helpers.MathHelper.getInPercents;

public class HeatController {
    protected int heat = 0;

    protected int heatCapacity = 0;

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = heat;
    }

    public void addHeat(int value) {
        if (value <= 0) return;

        if (heat + value <= getHeatCapacity())
            heat += value;
        else heat = getHeatCapacity();
    }

    public void subHeat(int value) {
        if (value <= 0 || heat == 0) return;

        if (heat - value >= 0)
            heat -= value;
        else heat = 0;
    }

    public int getHeatCapacity() {
        return heatCapacity;
    }

    public int getHeatInPercent() {
        return getInPercents(heat, getHeatCapacity());
    }

    public void doHeatAction(int heat, Runnable action) {
        if (!canDoAction(heat)) return;
        action.run();
        addHeat(heat);
    }

    public boolean canDoAction(int heat) {
        return heat + this.heat <= getHeatCapacity();
    }
}
