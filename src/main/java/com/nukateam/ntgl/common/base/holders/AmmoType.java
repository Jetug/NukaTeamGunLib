package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.common.foundation.init.ModDamageTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.Map;

public class AmmoType extends ResourceHolder {
    public static AmmoType STANDARD             = AmmoType.create("standard"             ).damageType(ModDamageTypes.BULLET).build();
    public static AmmoType PIERCING             = AmmoType.create("piercing"             ).damageType(ModDamageTypes.BULLET).build();
    public static AmmoType INCENDIARY           = AmmoType.create("incendiary"           ).damageType(ModDamageTypes.FIRE).build();
    public static AmmoType LASER                = AmmoType.create("laser"                ).damageType(ModDamageTypes.ENERGY).build();
    public static AmmoType TESLA                = AmmoType.create("tesla"                ).damageType(ModDamageTypes.ENERGY).build();
    public static AmmoType ENERGETIC            = AmmoType.create("energetic"            ).damageType(ModDamageTypes.ENERGY).build();
    public static AmmoType EXPLOSIVE            = AmmoType.create("explosive"            ).damageType(ModDamageTypes.EXPLOSIVE).build();
    public static AmmoType EXPLOSIVE_INCENDIARY = AmmoType.create("explosive_incendiary" ).damageType(ModDamageTypes.FIRE).build();
    public static AmmoType FIRE                 = AmmoType.create("fire"                 ).damageType(ModDamageTypes.FIRE).build();
    public static AmmoType SLUG                 = AmmoType.create("slug"                 ).damageType(ModDamageTypes.BULLET).build();
    public static AmmoType BUCKSHOT             = AmmoType.create("buckshot"             ).damageType(ModDamageTypes.BULLET).build();

    private static final Map<ResourceLocation, AmmoType> typeMap = new HashMap<>();
    
    static {
        registerType(STANDARD               );
        registerType(PIERCING               );
        registerType(INCENDIARY             );
        registerType(LASER                  );
        registerType(TESLA                  );
        registerType(ENERGETIC              );
        registerType(EXPLOSIVE              );
        registerType(EXPLOSIVE_INCENDIARY   );
        registerType(FIRE                   );
    }

    private ResourceKey<DamageType> damageType;

    public AmmoType(ResourceLocation id) {
        super(id);
    }

    public AmmoType(String name) {
        super(name);
    }

    public ResourceKey<DamageType> getDamageType() {
        return damageType;
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation(id.getNamespace(), "textures/hud/ammo_type/" + id.getPath() + ".png");
    }

    public static void registerType(AmmoType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AmmoType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, STANDARD);
    }

    public static AmmoType getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }

    public static Builder create(String name) {
        return new Builder(new AmmoType(name));
    }

    public static class Builder{
        AmmoType ammoType;

        private Builder(AmmoType ammoType){
            this.ammoType = ammoType;
        }

        public Builder damageType(ResourceKey<DamageType> damageType) {
            ammoType.damageType = damageType;
            return this;
        }

        public AmmoType build() {
            return ammoType;
        }
    }
}
