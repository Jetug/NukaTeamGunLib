package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.common.util.INBTSerializable;

public class AttributeModifier implements INBTSerializable<CompoundTag> {
    protected ResourceLocation attribute;
    protected double value = 0;
    protected Operation operation = Operation.MULTIPLY_BASE;

    public static AttributeModifier create(CompoundTag tag) {
        var config = new AttributeModifier();
        config.deserializeNBT(tag);
        return config;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("attribute", attribute.toString());
        tag.putDouble("value", value);
        tag.putString("operation", operation.name());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
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

        public AttributeModifier attribute(ResourceLocation attribute) {
            attributeModifier.attribute = attribute;
            return attributeModifier;
        }

        public AttributeModifier value(Double value) {
            attributeModifier.value = value;
            return attributeModifier;
        }

        public AttributeModifier operation(Operation operation) {
            attributeModifier.operation = operation;
            return attributeModifier;
        }

        public AttributeModifier build() {
            return this.attributeModifier.copy();
        }
    }
}
