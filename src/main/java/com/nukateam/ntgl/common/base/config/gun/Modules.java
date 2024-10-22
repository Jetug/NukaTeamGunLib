package com.nukateam.ntgl.common.base.config.gun;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.base.utils.NbtUtils;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.debug.screen.widget.DebugSlider;
import com.nukateam.ntgl.common.debug.screen.widget.DebugToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.nukateam.ntgl.client.ClientHandler.createEditorScreen;

public class Modules implements INBTSerializable<CompoundTag>, IEditorMenu {
    private transient Zoom cachedZoom;

    @Optional
    @Nullable
    Zoom zoom;
    @Optional
    private Map<AttachmentType, ArrayList<Attachment>> attachments = new HashMap<>();

    @Nullable
    public Zoom getZoom() {
        return this.zoom;
    }

    public Map<AttachmentType, ArrayList<Attachment>> getAttachments() {
        return this.attachments;
    }

    @Nullable
    public Attachment getAttachmentByBone(String name) {
        if (getAttachments() == null) return null;
        AtomicReference<Attachment> result = new AtomicReference<>();
        getAttachments().forEach((k, v) -> {
            var att = v.stream().filter((s) -> s.name.equals(name)).findFirst();
            att.ifPresent(result::set);
        });

        return result.get();
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Modules");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            widgets.add(Pair.of(Component.literal("Enabled Iron Sights"), () -> new DebugToggle(this.zoom != null, val -> {
                if (val) {
                    if (this.cachedZoom != null) {
                        this.zoom = this.cachedZoom;
                    } else {
                        this.zoom = new Zoom();
                        this.cachedZoom = this.zoom;
                    }
                } else {
                    this.cachedZoom = this.zoom;
                    this.zoom = null;
                }
            })));

            widgets.add(Pair.of(Component.literal("Adjust Iron Sights"), () -> new DebugButton(Component.literal(">"), btn -> {
                if (btn.active && this.zoom != null) {
                    Minecraft.getInstance().setScreen(createEditorScreen(this.zoom));
                }
            }, () -> this.zoom != null)));
        });
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (this.zoom != null)
            tag.put("Zoom", this.zoom.serializeNBT());

        if (attachments != null && !attachments.isEmpty())
            tag.put("Attachments", NbtUtils.serializeArrayMap(attachments));

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Zoom", Tag.TAG_COMPOUND)) {
            var zoom = new Zoom();
            zoom.deserializeNBT(tag.getCompound("Zoom"));
            this.zoom = zoom;
        }
        if (tag.contains("Attachments", Tag.TAG_COMPOUND)) {
            var nbt = tag.getCompound("Attachments");
            this.attachments = NbtUtils.deserializeAttachmentMap(nbt);
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        if (this.zoom != null) {
            object.add("zoom", this.zoom.toJsonObject());
        }

        return object;
    }

    public Modules copy() {
        Modules modules = new Modules();
        modules.attachments = new HashMap<>(this.attachments);
        if (this.zoom != null) {
            modules.zoom = this.zoom.copy();
        }
        return modules;
    }

    public static class Zoom extends Positioned implements IEditorMenu {
        @Optional
        float fovModifier;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();
            tag.putFloat("FovModifier", this.fovModifier);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
            if (tag.contains("FovModifier", Tag.TAG_ANY_NUMERIC)) {
                this.fovModifier = tag.getFloat("FovModifier");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = super.toJsonObject();
            object.addProperty("fovModifier", this.fovModifier);
            return object;
        }

        public Zoom copy() {
            Zoom zoom = new Zoom();
            zoom.fovModifier = this.fovModifier;
            zoom.xOffset = this.xOffset;
            zoom.yOffset = this.yOffset;
            zoom.zOffset = this.zOffset;
            return zoom;
        }

        @Override
        public Component getEditorLabel() {
            return Component.literal("Zoom");
        }

        @Override
        public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                widgets.add(Pair.of(Component.literal("FOV Modifier"), () -> new DebugSlider(0.0, 1.0, this.fovModifier, 0.01, 3, val -> {
                    this.fovModifier = val.floatValue();
                })));
            });
        }

        public float getFovModifier() {
            return this.fovModifier;
        }

        public static Zoom.Builder builder() {
            return new Zoom.Builder();
        }

        public static class Builder extends Zoom.AbstractBuilder<Zoom.Builder> {
        }

        protected static abstract class AbstractBuilder<T extends Zoom.AbstractBuilder<T>> extends Positioned.AbstractBuilder<T> {
            protected final Zoom zoom;

            protected AbstractBuilder() {
                this(new Zoom());
            }

            protected AbstractBuilder(Zoom zoom) {
                super(zoom);
                this.zoom = zoom;
            }

            public T setFovModifier(float fovModifier) {
                this.zoom.fovModifier = fovModifier;
                return this.self();
            }

            @Override
            public Zoom build() {
                return this.zoom.copy();
            }
        }
    }

    public static class Attachment extends Positioned {
        @Optional
        @Nullable
        String name;
        @Optional
        @Nullable
        ResourceLocation item;
        @Optional
        ArrayList<String> hide = new ArrayList<>();

        @Nullable
        public String getName() {
            return this.name;
        }

        @Nullable
        public ResourceLocation getItem() {
            return this.item;
        }

        public ArrayList<String> getHidden() {
            return this.hide;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.name != null) {
                tag.putString("Name", this.name);
            }
            if (this.item != null) {
                tag.putString("Item", this.item.toString());
            }
            if (this.hide != null) {
                tag.put("Hide", NbtUtils.serializeStringArray(this.hide));
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Name", Tag.TAG_STRING)) {
                this.name = tag.getString("Name");
            }
            if (tag.contains("Item", Tag.TAG_STRING)) {
                this.item = ResourceLocation.tryParse(tag.getString("Item"));
            }
            if (tag.contains("Hide", Tag.TAG_COMPOUND)) {
                this.hide = NbtUtils.deserializeStringArray(tag.getCompound("Hide"));
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.name != null) {
                object.addProperty("Name", this.name);
            }
            if (this.item != null) {
                object.addProperty("Item", this.item.toString());
            }
            return object;
        }

        public Attachment copy() {
            var attachments = new Attachment();
            if (this.name != null) {
                attachments.name = this.name;
            }
            if (this.item != null) {
                attachments.item = this.item;
            }
            if (this.hide != null) {
                attachments.hide = this.hide;
            }
            return attachments;
        }
    }
}
