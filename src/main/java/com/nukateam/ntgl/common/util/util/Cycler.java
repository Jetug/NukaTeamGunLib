package com.nukateam.ntgl.common.util.util;

import net.minecraft.util.Mth;

public class Cycler {
    private final int min;
    private final int max;
    private int current;

    public Cycler(int max){
        this.min = 0;
        this.max = max;
    }

    public Cycler(int min, int max){
        this.min = min;
        this.max = max;
        this.current = min;
    }

    public int getCurrent(){
        return current;
    }

    public void setCurrent(int value){
        current = Mth.clamp(value, min,max);
    }

    public int cycle(){
        if(current < max)
            current++;
        else current = min;

        return current;
    }
}
