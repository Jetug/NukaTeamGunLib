package com.nukateam.chassis_core.common.data.holders;

import com.nukateam.chassis_core.ChassisCore;
import net.minecraft.resources.ResourceLocation;

public class ResourceHolder {
    protected final ResourceLocation id;

    public ResourceHolder(ResourceLocation id) {
        this.id = id;
    }

    public ResourceHolder(String name) {
        this.id = ResourceLocation.tryBuild(ChassisCore.MOD_ID, name);
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
