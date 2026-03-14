package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class AttributeModifier implements INBTSerializable<CompoundTag> {
    protected ResourceLocation attribute;
    protected double value = 0;
    protected Operation operation = Operation.ADD_MULTIPLIED_BASE;

    public static AttributeModifier create(CompoundTag tag) {
        var config = new AttributeModifier();
        config.deserializeNBT(null, tag);
        return config;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("attribute", attribute.toString());
        tag.putDouble("value", value);
        tag.putString("operation", operation.name());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("attribute", Tag.TAG_STRING)) {
            this.attribute = ResourceLocation.tryParse(tag.getString("attribute"));
            this.value = tag.getDouble("value");
            this.operation = Operation.valueOf(tag.getString("operation"));
        }
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        return object;
    }


    public AttributeModifier copy() {
        var positioned = new AttributeModifier();
        positioned.attribute = this.attribute;
        positioned.value = this.value;
        positioned.operation = this.operation;
        return positioned;
    }

    public ResourceLocation getAttribute() {
        return attribute;
    }

    public double getValue() {
        return value;
    }

    public Operation getOperation() {
        return operation;
    }

    public static class Builder{
        private final AttributeModifier attributeModifier;

        private Builder() {
            this(new AttributeModifier());
        }

        protected Builder(AttributeModifier attributeModifier) {
            this.attributeModifier = attributeModifier;
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder attribute(ResourceLocation attribute) {
            attributeModifier.attribute = attribute;
            return this;
        }

        public Builder value(Double value) {
            attributeModifier.value = value;
            return this;
        }

        public Builder operation(Operation operation) {
            attributeModifier.operation = operation;
            return this;
        }

        public AttributeModifier build() {
            return this.attributeModifier.copy();
        }
    }
}
