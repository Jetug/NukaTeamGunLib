package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class BaseType {
    protected final ResourceLocation id;

    public BaseType(ResourceLocation id) {
        this.id = id;
    }

    public BaseType(String name) {
        this.id = new ResourceLocation(Ntgl.MOD_ID, name);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean equals(ResourceLocation obj) {
        return this.id.equals(obj);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
