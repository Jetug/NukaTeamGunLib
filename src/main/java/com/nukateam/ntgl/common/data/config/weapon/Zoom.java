package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugSlider;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class Zoom implements INBTSerializable<CompoundTag>, IEditorMenu {
    @Optional protected float fovModifier;
    @Optional protected Vec3 offset;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("FovModifier", this.fovModifier);
        tag.put("offset", NbtUtils.writeVec3(offset));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("FovModifier", Tag.TAG_ANY_NUMERIC)) {
            this.fovModifier = tag.getFloat("FovModifier");
        }
        if (tag.contains("offset", Tag.TAG_ANY_NUMERIC)) {
            this.offset = NbtUtils.readVec3(tag.getCompound("offset"));
        }
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.addProperty("fovModifier", this.fovModifier);
        return object;
    }

    public static Zoom create(CompoundTag tag){
        var zoom = new Zoom();
        zoom.deserializeNBT(tag);
        return zoom;
    }

    public Zoom copy() {
        Zoom zoom = new Zoom();
        zoom.fovModifier = this.fovModifier;
        return zoom;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Zoom");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            widgets.add(Pair.of(Component.literal("FOV Modifier"),
                    () -> new DebugSlider(0.0, 1.0, this.fovModifier, 0.01, 3, val -> {
                this.fovModifier = val.floatValue();
            })));
        });
    }

    public float getFovModifier() {
        return this.fovModifier;
    }

    public Vec3 getOffset() {
        return offset;
    }
}
