package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CounterType extends ResourceHolder {
    public static final CounterType NUMBER = new CounterType(Ntgl.ntglResource("number"));
    public static final CounterType PERCENT = new CounterType(Ntgl.ntglResource("percent"));
    public static final CounterType BAR = new CounterType(Ntgl.ntglResource( "bar"));

    private static final Map<ResourceLocation, CounterType> fireModeMap = new HashMap<>();

    static {
        registerType(NUMBER);
        registerType(PERCENT);
        registerType(BAR);
    }

    public CounterType(ResourceLocation id) {
        super(id);
    }

    public static void registerType(CounterType mode) {
        fireModeMap.putIfAbsent(mode.getId(), mode);
    }

    public static CounterType getType(ResourceLocation id) {
        return fireModeMap.getOrDefault(id, NUMBER);
    }

    public static CounterType getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
