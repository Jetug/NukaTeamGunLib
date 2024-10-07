package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;


public class ResourceHolder {
    protected final ResourceLocation id;

    public ResourceHolder(ResourceLocation id) {
        this.id = id;
    }

    public ResourceHolder(String name) {
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
