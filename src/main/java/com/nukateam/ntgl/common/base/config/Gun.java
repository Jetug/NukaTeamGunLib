package com.nukateam.ntgl.common.base.config;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.base.holders.*;
import com.nukateam.ntgl.common.base.utils.NbtUtils;
import com.nukateam.ntgl.common.data.annotation.Ignored;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunJsonUtil;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.data.util.SuperBuilder;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.debug.screen.widget.DebugSlider;
import com.nukateam.ntgl.common.debug.screen.widget.DebugToggle;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;
import com.nukateam.ntgl.common.helpers.BackpackHelper;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.nukateam.example.common.data.utils.ResourceUtils.resourceExists;
import static com.nukateam.ntgl.client.ClientHandler.*;
import static com.nukateam.ntgl.common.base.config.Gun.Display.*;
import static com.nukateam.ntgl.common.base.config.Gun.Modules.*;

public class Gun implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String ATTACHMENTS = "Attachments";
    protected General general = new General();
    protected Sounds sounds = new Sounds();
    protected Display display = new Display();
    protected Modules modules = new Modules();
    protected Map<String, ResourceLocation> textures = new HashMap<>();
    @Ignored
    protected Map<String, ResourceLocation> preparedTextures = new HashMap<>();

    public General getGeneral() {
        return this.general;
    }

//    public Projectile getAmmo() {
//        return this.ammo;
//    }

    public Sounds getSounds() {
        return this.sounds;
    }

    public Display getDisplay() {
        return this.display;
    }

    public Modules getModules() {
        return this.modules;
    }

    public Map<String, ResourceLocation> getTextures() {
        return preparedTextures;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
            ItemStack scope = Gun.getScopeStack(heldItem);
            if (scope.getItem() instanceof ScopeItem scopeItem) {
                widgets.add(Pair.of(scope.getItem().getName(scope), () -> new DebugButton(Component.literal("Edit"), btn -> {
                    Minecraft.getInstance().setScreen(createEditorScreen(Debug.getScope(scopeItem)));
                })));
            }

            widgets.add(Pair.of(this.modules.getEditorLabel(), () -> new DebugButton(Component.literal(">"), btn -> {
                Minecraft.getInstance().setScreen(createEditorScreen(this.modules));
            })));
        });
    }

    public static class Sounds implements INBTSerializable<CompoundTag> {
        @Optional
        @Nullable
        private ResourceLocation fire;
        @Optional
        @Nullable
        private ResourceLocation reload;
        @Optional
        @Nullable
        private ResourceLocation cock;
        @Optional
        @Nullable
        private ResourceLocation silencedFire;
        @Optional
        @Nullable
        private ResourceLocation enchantedFire;
        @Optional
        @Nullable
        private ResourceLocation preFire;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.fire != null) {
                tag.putString("Fire", this.fire.toString());
            }
            if (this.reload != null) {
                tag.putString("Reload", this.reload.toString());
            }
            if (this.cock != null) {
                tag.putString("Cock", this.cock.toString());
            }
            if (this.silencedFire != null) {
                tag.putString("SilencedFire", this.silencedFire.toString());
            }
            if (this.enchantedFire != null) {
                tag.putString("EnchantedFire", this.enchantedFire.toString());
            }
            if (this.preFire != null) {
                tag.putString("PreFire", this.preFire.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Fire", Tag.TAG_STRING)) {
                this.fire = this.createSound(tag, "Fire");
            }
            if (tag.contains("Reload", Tag.TAG_STRING)) {
                this.reload = this.createSound(tag, "Reload");
            }
            if (tag.contains("Cock", Tag.TAG_STRING)) {
                this.cock = this.createSound(tag, "Cock");
            }
            if (tag.contains("SilencedFire", Tag.TAG_STRING)) {
                this.silencedFire = this.createSound(tag, "SilencedFire");
            }
            if (tag.contains("EnchantedFire", Tag.TAG_STRING)) {
                this.enchantedFire = this.createSound(tag, "EnchantedFire");
            }
            if (tag.contains("PreFire", Tag.TAG_STRING)) {
                this.preFire = this.createSound(tag, "PreFire");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.fire != null) {
                object.addProperty("fire", this.fire.toString());
            }
            if (this.reload != null) {
                object.addProperty("reload", this.reload.toString());
            }
            if (this.cock != null) {
                object.addProperty("cock", this.cock.toString());
            }
            if (this.silencedFire != null) {
                object.addProperty("silencedFire", this.silencedFire.toString());
            }
            if (this.enchantedFire != null) {
                object.addProperty("enchantedFire", this.enchantedFire.toString());
            }
            if (this.preFire != null) {
                object.addProperty("preFire", this.preFire.toString());
            }
            return object;
        }

        public Sounds copy() {
            Sounds sounds = new Sounds();
            sounds.fire = this.fire;
            sounds.reload = this.reload;
            sounds.cock = this.cock;
            sounds.silencedFire = this.silencedFire;
            sounds.enchantedFire = this.enchantedFire;
            sounds.preFire = this.preFire;
            return sounds;
        }

        @Nullable
        private ResourceLocation createSound(CompoundTag tag, String key) {
            String sound = tag.getString(key);
            return sound.isEmpty() ? null : new ResourceLocation(sound);
        }

        /**
         * @return The registry id of the sound event when firing this weapon
         */
        @Nullable
        public ResourceLocation getFire() {
            return this.fire;
        }

        /**
         * @return The registry iid of the sound event when reloading this weapon
         */
        @Nullable
        public ResourceLocation getReload() {
            return this.reload;
        }

        /**
         * @return The registry iid of the sound event when cocking this weapon
         */
        @Nullable
        public ResourceLocation getCock() {
            return this.cock;
        }

        /**
         * @return The registry iid of the sound event when silenced firing this weapon
         */
        @Nullable
        public ResourceLocation getSilencedFire() {
            return this.silencedFire;
        }

        /**
         * @return The registry iid of the sound event when silenced firing this weapon
         */
        @Nullable
        public ResourceLocation getEnchantedFire() {
            return this.enchantedFire;
        }
        @Nullable
        public ResourceLocation getPreFire() {
            return this.preFire;
        }
    }

    public static class Display implements INBTSerializable<CompoundTag> {
        @Optional
        @Nullable
        protected Flash flash;

        @Nullable
        public Flash getFlash() {
            return this.flash;
        }

        public static class Flash extends Positioned {
            private double size = 0.5;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = super.serializeNBT();
                tag.putDouble("Size", this.size);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                super.deserializeNBT(tag);
                if (tag.contains("Size", Tag.TAG_ANY_NUMERIC)) {
                    this.size = tag.getDouble("Size");
                }
            }

            @Override
            public JsonObject toJsonObject() {
                Preconditions.checkArgument(this.size >= 0, "Muzzle flash size must be more than or equal to zero");
                JsonObject object = super.toJsonObject();
                if (this.size != 0.5) {
                    object.addProperty("size", this.size);
                }
                return object;
            }

            public Flash copy() {
                Flash flash = new Flash();
                flash.size = this.size;
                flash.xOffset = this.xOffset;
                flash.yOffset = this.yOffset;
                flash.zOffset = this.zOffset;
                return flash;
            }

            /**
             * @return The size/scale of the muzzle flash render
             */
            public double getSize() {
                return this.size;
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.flash != null) {
                tag.put("Flash", this.flash.serializeNBT());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Flash", Tag.TAG_COMPOUND)) {
                CompoundTag flashTag = tag.getCompound("Flash");
                if (!flashTag.isEmpty()) {
                    Flash flash = new Flash();
                    flash.deserializeNBT(tag.getCompound("Flash"));
                    this.flash = flash;
                } else {
                    this.flash = null;
                }
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.flash != null) {
                GunJsonUtil.addObjectIfNotEmpty(object, "flash", this.flash.toJsonObject());
            }
            return object;
        }

        public Display copy() {
            Display display = new Display();
            if (this.flash != null) {
                display.flash = this.flash.copy();
            }
            return display;
        }
    }

    public static class Modules implements INBTSerializable<CompoundTag>, IEditorMenu {
        private transient Zoom cachedZoom;

        @Optional
        @Nullable
        private Zoom zoom;
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
            if(getAttachments() == null) return null;
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

            if( attachments != null && !attachments.isEmpty())
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
            if(tag.contains("Attachments", Tag.TAG_COMPOUND)){
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
            private float fovModifier;

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

            public static Builder builder() {
                return new Builder();
            }

            public static class Builder extends AbstractBuilder<Builder> {
            }

            protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends Positioned.AbstractBuilder<T> {
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

        public static class Attachment extends Positioned{
            @Optional
            @Nullable
            private String name;
            @Optional
            @Nullable
            private ResourceLocation item;
            @Optional
            private ArrayList<String> hide = new ArrayList<>();

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

    public static class Positioned implements INBTSerializable<CompoundTag> {
        @Optional
        protected double xOffset;
        @Optional
        protected double yOffset;
        @Optional
        protected double zOffset;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("XOffset", this.xOffset);
            tag.putDouble("YOffset", this.yOffset);
            tag.putDouble("ZOffset", this.zOffset);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("XOffset", Tag.TAG_ANY_NUMERIC)) {
                this.xOffset = tag.getDouble("XOffset");
            }
            if (tag.contains("YOffset", Tag.TAG_ANY_NUMERIC)) {
                this.yOffset = tag.getDouble("YOffset");
            }
            if (tag.contains("ZOffset", Tag.TAG_ANY_NUMERIC)) {
                this.zOffset = tag.getDouble("ZOffset");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.xOffset != 0) {
                object.addProperty("xOffset", this.xOffset);
            }
            if (this.yOffset != 0) {
                object.addProperty("yOffset", this.yOffset);
            }
            if (this.zOffset != 0) {
                object.addProperty("zOffset", this.zOffset);
            }
            return object;
        }

        public double getXOffset() {
            return this.xOffset;
        }

        public double getYOffset() {
            return this.yOffset;
        }

        public double getZOffset() {
            return this.zOffset;
        }

        public Positioned copy() {
            Positioned positioned = new Positioned();
            positioned.xOffset = this.xOffset;
            positioned.yOffset = this.yOffset;
            positioned.zOffset = this.zOffset;
            return positioned;
        }

        public static class Builder extends AbstractBuilder<Builder> {
        }

        protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends SuperBuilder<Positioned, T> {
            private final Positioned positioned;

            private AbstractBuilder() {
                this(new Positioned());
            }

            protected AbstractBuilder(Positioned positioned) {
                this.positioned = positioned;
            }

            public T setOffset(double xOffset, double yOffset, double zOffset) {
                this.positioned.xOffset = xOffset;
                this.positioned.yOffset = yOffset;
                this.positioned.zOffset = zOffset;
                return this.self();
            }

            public T setXOffset(double xOffset) {
                this.positioned.xOffset = xOffset;
                return this.self();
            }

            public T setYOffset(double yOffset) {
                this.positioned.yOffset = yOffset;
                return this.self();
            }

            public T setZOffset(double zOffset) {
                this.positioned.zOffset = zOffset;
                return this.self();
            }

            @Override
            public Positioned build() {
                return this.positioned.copy();
            }
        }
    }

    public static class ScaledPositioned extends Positioned {
        @Optional
        protected double scale = 1.0;

        public ScaledPositioned() {
        }

        public ScaledPositioned(CompoundTag tag) {
            this.deserializeNBT(tag);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();
            tag.putDouble("Scale", this.scale);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
            if (tag.contains("Scale", Tag.TAG_ANY_NUMERIC)) {
                this.scale = tag.getDouble("Scale");
            }
        }

        @Override
        public JsonObject toJsonObject() {
            JsonObject object = super.toJsonObject();
            if (this.scale != 1.0) {
                object.addProperty("scale", this.scale);
            }
            return object;
        }

        public double getScale() {
            return this.scale;
        }

        @Override
        public ScaledPositioned copy() {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.xOffset = this.xOffset;
            positioned.yOffset = this.yOffset;
            positioned.zOffset = this.zOffset;
            positioned.scale = this.scale;
            return positioned;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("General", this.general.serializeNBT());
        tag.put("Sounds", this.sounds.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        tag.put("Modules", this.modules.serializeNBT());
        tag.put("Textures", NbtUtils.serializeStringMap(this.textures));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("General", Tag.TAG_COMPOUND)) {
            this.general.deserializeNBT(tag.getCompound("General"));
        }
        if (tag.contains("Sounds", Tag.TAG_COMPOUND)) {
            this.sounds.deserializeNBT(tag.getCompound("Sounds"));
        }
        if (tag.contains("Display", Tag.TAG_COMPOUND)) {
            this.display.deserializeNBT(tag.getCompound("Display"));
        }
        if (tag.contains("Modules", Tag.TAG_COMPOUND)) {
            this.modules.deserializeNBT(tag.getCompound("Modules"));
        }
        if (tag.contains("Textures", Tag.TAG_COMPOUND)) {
            this.textures = NbtUtils.deserializeRLMap(tag.getCompound("Textures"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "sounds", this.sounds.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "display", this.display.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules", this.modules.toJsonObject());
        return object;
    }

    public static Gun create(ResourceLocation id, CompoundTag tag) {
        var gun = new Gun();
        gun.deserializeNBT(tag);
        prepareTextures(id.getPath(), gun);
        return gun;
    }

    public void onCreated(String id){
        prepareTextures(id, this);
    }

    private static void prepareTextures(String itemId, Gun gun) {
        var thread = new Thread(() ->
            gun.textures.forEach((variant, path) -> {
                var texture = resourceExists(path) ? path : getTexture(itemId, path);
                gun.preparedTextures.put(variant, texture);
            })
        );
        thread.start();
    }

    @NotNull
    private static ResourceLocation getTexture(String itemId, ResourceLocation path) {
        return new ResourceLocation(path.getNamespace(), "textures/guns/" + itemId + "/" + path.getPath() + ".png");
    }

    public Gun copy() {
        var gun = new Gun();
        gun.general = this.general.copy();
        gun.sounds = this.sounds.copy();
        gun.display = this.display.copy();
        gun.modules = this.modules.copy();
        return gun;
    }

    public boolean canAttachType(@Nullable AttachmentType type, Gun gun) {
        var attachments = gun.getModules().getAttachments();
        if(attachments == null)
            return false;
        return attachments.containsKey(type);
    }

//    @Nullable
//    public ScaledPositioned getAttachmentPosition(ResourceLocation type) {
//        if (this.modules.attachments != null && this.modules.attachments.containsKey(type)) {
//            if (type.equals(SCOPE)) {
//                return this.modules.attachments.get(type);
//            } else if (type.equals(BARREL)) {
//                return this.modules.attachments.barrelItem;
//            } else if (type.equals(STOCK)) {
//                return this.modules.attachments.stock;
//            } else if (type.equals(UNDER_BARREL)) {
//                return this.modules.attachments.underBarrel;
//            }
//        }
//        return null;
//    }

    public boolean canAimDownSight() {
        return /*this.canAttachType(SCOPE, ) || */this.modules.zoom != null;
    }

    public static ItemStack getScopeStack(ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound("Scope"));
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, Gun gun, AttachmentType type) {
        if (!gun.canAttachType(type, gun))
            return false;

        var compound = stack.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound(ATTACHMENTS);
            return attachment.contains(type.toString(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    @Nullable
    public static Scope getScope(ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                var scopeStack = ItemStack.of(attachment.getCompound("Scope"));
                Scope scope = null;
                if (scopeStack.getItem() instanceof ScopeItem scopeItem) {
                    if (Ntgl.isDebugging()) {
                        return Debug.getScope(scopeItem);
                    }
                    scope = scopeItem.getProperties();
                }
                return scope;
            }
        }
        return null;
    }

    public ArrayList<Attachment> getAttachments(ArrayList<ItemStack> itemStacks) {
        var result = new ArrayList<Attachment>();

        for (var stack : itemStacks) {
            var item = stack.getItem();
            var itemRegistryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
            var attachment = findAttachment(item, itemRegistryName);

            if(attachment != null)
                result.add(attachment);
        }
        return result;
    }

    private Attachment findAttachment(Item item, ResourceLocation itemRegistryName) {
        Attachment result = null;
        if(item instanceof IAttachment attachmentItem){
            var attachmentType = attachmentItem.getType();
            if(!getModules().getAttachments().containsKey(attachmentType)) return result;
            var attachments = getModules().getAttachments().get(attachmentType);

            for (var attachment : attachments) {
                if(attachment.item != null && attachment.item.equals(itemRegistryName)){
                    result = attachment;
                }
            }
        }
        return result;
    }

    public static ArrayList<ItemStack> getAttachmentItems(ItemStack gun) {
        var compound = gun.getTag();
        var result = new ArrayList<ItemStack>();

        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            for (var slot: attachment.getAllKeys()){
                if (attachment.contains(slot, Tag.TAG_COMPOUND))
                    result.add(ItemStack.of(attachment.getCompound(slot)));
            }
        }
        return result;
    }

    public static ItemStack getAttachmentItem(AttachmentType type, ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains(type.toString(), Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound(type.toString()));
            }
        }
        return ItemStack.EMPTY;
    }

    public static float getAdditionalDamage(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static AmmoContext findAmmo(LivingEntity entity, ItemStack weapon) {
        var id = GunModifierHelper.getCurrentAmmo(weapon);

        if (entity instanceof Player player) {
            var context = findPlayerAmmo(player, id);

            if(context == AmmoContext.NONE){
                var set = GunModifierHelper.getAmmoItems(weapon);
                for (var value: set) {
                    if(!value.equals(id) && Gun.getAmmo(weapon) == 0){
                        id = value;
                        context = findPlayerAmmo(player, id);
                        if(context != AmmoContext.NONE) {
                            GunModifierHelper.setCurrentAmmo(weapon, id);
                            return context;
                        }
                    }
                }
            }

            return context;
        }
        return getCreativeAmmoContext(id);
    }

    public static AmmoContext findMagazine(LivingEntity entity, ItemStack weapon) {
        var id = GunModifierHelper.getCurrentAmmo(weapon);

        if (entity instanceof Player player) {
            var context = findPlayerMagazine(player, id);

            if(context == AmmoContext.NONE){
                var set = GunModifierHelper.getAmmoItems(weapon);
                for (var value: set) {
                    if(!value.equals(id) && Gun.getAmmo(weapon) == 0){
                        id = value;
                        context = findPlayerMagazine(player, id);
                        if(context != AmmoContext.NONE) {
                            GunModifierHelper.setCurrentAmmo(weapon, id);
                            return context;
                        }
                    }
                }
            }

            return context;
        }

        return getCreativeAmmoContext(id);
    }

    public static AmmoContext findPlayerAmmo(Player player, ResourceLocation id) {
        if (player.isCreative())
            return getCreativeAmmoContext(id);

        var context = findAmmo(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        if (Ntgl.backpackedLoaded) {
            return BackpackHelper.findAmmo(player, id);
        }
        return AmmoContext.NONE;
    }

    public static AmmoContext findAmmo(Container inventory, ResourceLocation id){
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findPlayerMagazine(Player player, ResourceLocation id) {
        if (player.isCreative()) {
            return getCreativeAmmoContext(id);
        }

        var context = findMagazine(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        if (Ntgl.backpackedLoaded) {
            return BackpackHelper.findMagazine(player, id);
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Container inventory, ResourceLocation id){
        ItemStack ammoStack = null;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var foundStack = inventory.getItem(i);

            if (isAmmo(foundStack, id)) {
                if (foundStack.getDamageValue() == 0)
                    return new AmmoContext(foundStack, inventory);

                if (ammoStack == null || hasMoreAmmo(ammoStack, foundStack))
                    ammoStack = foundStack;
            }
        }

        if (ammoStack != null)
            return new AmmoContext(ammoStack, inventory);

        return AmmoContext.NONE;
    }

    private static ResourceLocation getKey(ItemStack stack){
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    /**
     * @return True if the second stack contains more ammo then the first
     */
    private static boolean hasMoreAmmo(ItemStack first, ItemStack second) {
        return second.getDamageValue() < first.getDamageValue() && first.getDamageValue() < first.getMaxDamage();
    }

    @NotNull
    private static AmmoContext getCreativeAmmoContext(ResourceLocation id) {
        var item = ForgeRegistries.ITEMS.getValue(id);
        var ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
        return new AmmoContext(ammo, null);
    }

    public static boolean isAmmo(ItemStack stack, ResourceLocation id) {
        return stack != null && Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), id);
    }

    public static int getAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT);
    }

    public static void setAmmo(ItemStack gunStack, int amount) {
        var tag = gunStack.getOrCreateTag();
        tag.putInt(Tags.AMMO_COUNT, amount);
    }

    public static boolean hasAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getBoolean("IgnoreAmmo") || tag.getInt(Tags.AMMO_COUNT) > 0;
    }

    public static void fillAmmo(ItemStack gunStack) {
        if (gunStack.getItem() instanceof GunItem gunItem) {
            var tag = gunStack.getOrCreateTag();
//            var maxAmmo = gunItem.getModifiedGun(gunStack).getGeneral().getMaxAmmo(gunStack);
            var maxAmmo = GunModifierHelper.getMaxAmmo(gunStack);

            tag.putInt(Tags.AMMO_COUNT, maxAmmo);
        }
    }

    public static float getFovModifier(ItemStack stack, Gun modifiedGun) {
        float modifier = 0.0F;
        if (hasAttachmentEquipped(stack, modifiedGun, AttachmentType.SCOPE)) {
            var scope = Gun.getScope(stack);
            if (scope != null) {
                if (scope.getFovModifier() < 1.0F) {
                    return Mth.clamp(scope.getFovModifier(), 0.01F, 1.0F);
                }
                modifier -= scope.getFovModifier();
            }
        }
        var zoom = modifiedGun.getModules().getZoom();
        return zoom != null ? modifier + zoom.getFovModifier() : 0F;
    }

    public static class Builder {
        private final Gun gun;

        private Builder() {
            this.gun = new Gun();
        }

        private Builder(Gun gun) {
            this.gun = gun.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(Gun gun) {
            return new Builder(gun);
        }

        public Gun build() {
            return this.gun.copy(); //Copy since the builder could be used again
        }

        public Gun.Builder addAmmo(ResourceLocation id) {
            this.gun.general.ammo.add(id);
            return this;
        }

        public Builder setFireRate(int rate) {
            this.gun.general.rate = rate;
            return this;
        }

        public Builder setGripType(GripType gripType) {
            this.gun.general.gripType = gripType;
            return this;
        }

        public Builder setReloadType(ResourceLocation reloadType) {
            this.gun.general.reloadType = reloadType;
            return this;
        }

        public Builder setMaxAmmo(int maxAmmo) {
            this.gun.general.maxAmmo = maxAmmo;
            return this;
        }

        public Builder setReloadAmount(int reloadAmount) {
            this.gun.general.reloadAmount = reloadAmount;
            return this;
        }

        public Builder setReloadTime(int reloadTime) {
            this.gun.general.reloadTime = reloadTime;
            return this;
        }

        public Builder setLoadingType(LoadingType loadingType) {
            this.gun.general.loadingType = loadingType;
            return this;
        }

        public Builder setCategory(String category) {
            this.gun.general.category = category;
            return this;
        }

        public Builder setRecoilAngle(float recoilAngle) {
            this.gun.general.recoilAngle = recoilAngle;
            return this;
        }

        public Builder setRecoilKick(float recoilKick) {
            this.gun.general.recoilKick = recoilKick;
            return this;
        }

        public Builder setRecoilDurationOffset(float recoilDurationOffset) {
            this.gun.general.recoilDurationOffset = recoilDurationOffset;
            return this;
        }

        public Builder setRecoilAdsReduction(float recoilAdsReduction) {
            this.gun.general.recoilAdsReduction = recoilAdsReduction;
            return this;
        }

        public Builder setProjectileAmount(int projectileAmount) {
            this.gun.general.projectileAmount = projectileAmount;
            return this;
        }

        public Builder setAlwaysSpread(boolean alwaysSpread) {
            this.gun.general.alwaysSpread = alwaysSpread;
            return this;
        }

        public Builder setSpread(float spread) {
            this.gun.general.spread = spread;
            return this;
        }

        public Builder setFireSound(SoundEvent sound) {
            this.gun.sounds.fire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setReloadSound(SoundEvent sound) {
            this.gun.sounds.reload = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setCockSound(SoundEvent sound) {
            this.gun.sounds.cock = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setSilencedFireSound(SoundEvent sound) {
            this.gun.sounds.silencedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        public Builder setEnchantedFireSound(SoundEvent sound) {
            this.gun.sounds.enchantedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setMuzzleFlash(double size, double xOffset, double yOffset, double zOffset) {
            var flash = new Flash();
            flash.size = size;
            flash.xOffset = xOffset;
            flash.yOffset = yOffset;
            flash.zOffset = zOffset;
            this.gun.display.flash = flash;
            return this;
        }

        public Builder setZoom(float fovModifier, double xOffset, double yOffset, double zOffset) {
            var zoom = new Zoom();
            zoom.fovModifier = fovModifier;
            zoom.xOffset = xOffset;
            zoom.yOffset = yOffset;
            zoom.zOffset = zOffset;
            this.gun.modules.zoom = zoom;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setZoom(Zoom.Builder builder) {
            this.gun.modules.zoom = builder.build();
            return this;
        }
    }
}
