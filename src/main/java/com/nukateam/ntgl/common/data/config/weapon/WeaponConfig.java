package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.Gson;
import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.holders.*;

import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageGunSound;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static com.nukateam.ntgl.client.handlers.ClientHandler.*;

public class WeaponConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String GENERAL = "General";
    public static final String MELEE = "Melee";
    public static final String THROWABLE = "Throwable";
    public static final String SOUNDS = "Sounds";
    public static final String DISPLAY = "Display";
    public static final String MODULES = "Modules";
    public static final String TEXTURES = "Textures";
    public static final String ANIMATIONS = "Animations";
    public static final String AMMO_DATA = "AmmoData";
    public static final String SECONDARY_AMMO = "SecondaryAmmo";
    public static final String MODES = "Modes";
    protected General general = getWeapon();
    protected Melee melee = new Melee();
    protected ThrowableConfig throwable = new ThrowableConfig();
    protected HashMap<WeaponMode, WeaponSettings> modes = new HashMap<>(Map.of());
    protected Modules modules = new Modules();
    @Optional
    protected Zoom zoom = new Zoom();
    protected HashMap<AnimationType, ResourceLocation> animations = new HashMap<>();
    protected LinkedHashMap<ResourceLocation, AmmoData> ammoData = new LinkedHashMap<>();
    protected LinkedHashMap<ResourceLocation, Fuel> fuel = new LinkedHashMap<>();
    protected HashMap<String, ResourceLocation> sounds = new HashMap<>();
    protected HashMap<String, ResourceLocation> textures = new HashMap<>();

    private static General getWeapon(){
        var gun = new General();
        gun.action = WeaponAction.SHOT;
        return gun;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
            var scope = WeaponStateHelper.getScopeStack(heldItem);
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

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put(GENERAL, this.general.serializeNBT());
        tag.put(MELEE, this.melee.serializeNBT());
        tag.put(THROWABLE, this.throwable.serializeNBT());
        tag.put(SOUNDS, NbtUtils.serializeStringMap(this.sounds));
        tag.put(MODULES, this.modules.serializeNBT());
        tag.put(TEXTURES, NbtUtils.serializeStringMap(this.textures));
        tag.put(ANIMATIONS, NbtUtils.serializeStringMap(this.animations));
        tag.put(AMMO_DATA, NbtUtils.serializeMap(this.ammoData));
        tag.put(SECONDARY_AMMO, NbtUtils.serializeMap(this.fuel));
        tag.put(MODES, NbtUtils.serializeMap(this.modes));
        if (this.zoom != null) {
            tag.put("Zoom", this.zoom.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(GENERAL, Tag.TAG_COMPOUND)) {
            this.general.deserializeNBT(tag.getCompound(GENERAL));
        }
        if (tag.contains(MELEE, Tag.TAG_COMPOUND)) {
            this.melee.deserializeNBT(tag.getCompound(MELEE));
        }
        if (tag.contains(THROWABLE, Tag.TAG_COMPOUND)) {
            this.throwable.deserializeNBT(tag.getCompound(THROWABLE));
        }
        if (tag.contains(SOUNDS, Tag.TAG_COMPOUND)) {
            this.sounds = deserializeSounds(tag.getCompound(SOUNDS));
        }
        if (tag.contains(MODULES, Tag.TAG_COMPOUND)) {
            this.modules.deserializeNBT(tag.getCompound(MODULES));
        }
        if (tag.contains(TEXTURES, Tag.TAG_COMPOUND)) {
            this.textures = NbtUtils.deserializeRLMap(tag.getCompound(TEXTURES));
        }
        if (tag.contains(ANIMATIONS, Tag.TAG_COMPOUND)) {
            this.animations = NbtUtils.deserializeMap(tag.getCompound(ANIMATIONS),
                    AnimationType::getType,
                    (nbt, key) -> ResourceLocation.tryParse(nbt.getString(key))
            );
        }
        if (tag.contains(AMMO_DATA, Tag.TAG_COMPOUND)) {
            this.ammoData = NbtUtils.deserializeLinkedMap(tag.getCompound(AMMO_DATA), AmmoData::create);
        }
        if (tag.contains(SECONDARY_AMMO, Tag.TAG_COMPOUND)) {
            this.fuel = NbtUtils.deserializeLinkedMap(tag.getCompound(SECONDARY_AMMO), Fuel::create);
        }
        if (tag.contains(MODES, Tag.TAG_COMPOUND)) {
            this.modes = NbtUtils.deserializeMap(tag.getCompound(MODES),
                    WeaponMode::getType,
                    (nbt, key) -> WeaponSettings.create(nbt.getCompound(key)));
        }
        if(tag.contains("Zoom", Tag.TAG_COMPOUND)) {
            this.zoom = Zoom.create(tag.getCompound("Zoom"));
        }
    }

    public JsonObject toJsonObject() {
        var gson = new Gson();
        var object = new JsonObject();
        object.add("general"    , this.general.toJsonObject());
        object.add("melee"      , this.melee.toJsonObject());
        object.add("throwable"  , this.throwable.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "ammoData", gson.toJsonTree(this.ammoData).getAsJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "sounds"  , gson.toJsonTree(this.sounds).getAsJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules" , this.modules.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modes"   , gson.toJsonTree(this.modes).getAsJsonObject());
        if (this.zoom != null)
            object.add("zoom", this.zoom.toJsonObject());
        return object;
    }

    public WeaponConfig copy() {
        var gun = new WeaponConfig();
        gun.general     = this.general.copy();
        gun.melee       = this.melee.copy();
        gun.throwable   = this.throwable.copy();
        gun.sounds      = (HashMap<String, ResourceLocation>) this.sounds.clone();
        gun.textures    = (HashMap<String, ResourceLocation>) this.textures.clone();
        gun.animations  = (HashMap<AnimationType, ResourceLocation>) this.animations.clone();
        gun.ammoData    = (LinkedHashMap<ResourceLocation, AmmoData>) this.ammoData.clone();
        gun.fuel        = (LinkedHashMap<ResourceLocation, Fuel>) this.fuel.clone();
        gun.modules     = this.modules.copy();
        gun.zoom        = this.zoom.copy();
        return gun;
    }

    public static WeaponConfig create(ResourceLocation id, CompoundTag tag) {
        var gun = new WeaponConfig();
        gun.deserializeNBT(tag);
        return gun;
    }

    public void onCreated(String id){}

    public General getGeneral() {
        return this.general;
    }

    public Melee getMelee() {
        return this.melee;
    }

    public ThrowableConfig getThrowable() {
        return this.throwable;
    }

    public HashMap<String, ResourceLocation> getSoundsMap() {
        return sounds;
    }

    public ResourceLocation getSound(String name){
        return sounds.get(name);
    }


    public Modules getModules() {
        return this.modules;
    }
//    public ResourceLocation getTexture(String variant) {
//        return preparedTextures.computeIfAbsent(variant, v ->
//                prepareTexture(textures.get(variant))
//        );
//    }

    public Map<String, ResourceLocation> getTextures() {
        return textures;
    }

    public HashMap<AnimationType, ResourceLocation> getAnimations() {
        return animations;
    }

    public ResourceLocation getAnimation(AnimationType type) {
        return animations.get(type);
    }

    public boolean canAttachType(@Nullable AttachmentType type) {
        var attachments = this.getModules().getAttachments();
        if(attachments == null)
            return false;
        return attachments.containsKey(type);
    }

    public ArrayList<Modules.Attachment> getAttachmentConfigs(ArrayList<ItemStack> itemStacks) {
        var result = new ArrayList<Modules.Attachment>();

        for (var stack : itemStacks) {
            var item = stack.getItem();
            var attachment = findAttachment(item);

            if(attachment != null)
                result.add(attachment);
        }
        return result;
    }

    public Modules.Attachment findAttachment(Item item) {
        var itemId = ForgeRegistries.ITEMS.getKey(item);

        if(item instanceof IAttachment attachmentItem){
            var attachmentType = attachmentItem.getType();

            if(!getModules().getAttachments().containsKey(attachmentType))
                return new Modules.Attachment();

            var attachments = getModules().getAttachments().get(attachmentType);

            for (var attachment : attachments) {
                if(attachment.getItemId() != null && attachment.getItemId().equals(itemId)){
                    return attachment;
                }
            }
        }
        return new Modules.Attachment();
    }

    public AmmoData getAmmoData(ResourceLocation ammo) {
        return ammoData.getOrDefault(ammo, new AmmoData());
    }

    public Fuel getFuelData(ResourceLocation ammo) {
        return fuel.getOrDefault(ammo, new Fuel());
    }

    public boolean hasAmmo(ResourceLocation ammo){
        return ammoData.containsKey(ammo);
    }

    public ProjectileConfig getProjectileConfig(ResourceLocation ammo){
        return getAmmoData(ammo).getProjectile();
    }

    public AmmoConfig getAmmoConfig(ResourceLocation ammo){
        return getAmmoData(ammo).getAmmo();
    }

    public Fuel getFuelConfig(ResourceLocation ammo) {
        return getFuelData(ammo);
    }

    public AmmoConfig getFuelAmmoConfig(ResourceLocation ammo){
        return getFuelData(ammo).getAmmo();
    }

    public General getGeneral(WeaponMode mode) {
        if(mode == WeaponMode.PRIMARY)
            return general;
        else return modes.getOrDefault(mode, new WeaponSettings()).getGeneral();
    }

    public Melee getMelee(WeaponMode mode) {
        if(mode == WeaponMode.PRIMARY)
            return melee;
        else return modes.getOrDefault(mode, new WeaponSettings()).getMelee();
    }

    public AmmoData getAmmoData(WeaponMode mode, ResourceLocation ammoId) {
        if(mode == WeaponMode.PRIMARY)
            return ammoData.get(ammoId);
        else return modes.getOrDefault(mode, new WeaponSettings()).getAmmoData(ammoId);
    }

    public ThrowableConfig getThrowable(WeaponMode mode) {
        if(mode == WeaponMode.PRIMARY)
            return throwable;
        else return modes.getOrDefault(mode, new WeaponSettings()).getThrowable();
    }

    public Zoom getZoom(WeaponMode mode) {
        if(mode == WeaponMode.PRIMARY)
            return zoom;
        else return modes.getOrDefault(mode, new WeaponSettings()).getZoom();
    }

    public HashMap<WeaponMode, WeaponSettings> getModes() {
        return modes;
    }

    private HashMap<String, ResourceLocation> deserializeSounds(CompoundTag tag){
        var result = new HashMap<String, ResourceLocation>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                result.put(key, createSound(tag, key));
            }
        }
        return result;
    }

    private ResourceLocation createSound(CompoundTag tag, String key) {
        var sound = tag.getString(key);
        return sound.isEmpty() ? null : ResourceLocation.tryParse(sound);
    }

    public static class Builder {
        private final WeaponConfig weaponConfig;

        private Builder() {
            this.weaponConfig = new WeaponConfig();
        }

        private Builder(WeaponConfig weaponConfig) {
            this.weaponConfig = weaponConfig.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(WeaponConfig weaponConfig) {
            return new Builder(weaponConfig);
        }

        public WeaponConfig build() {
            return this.weaponConfig.copy(); //Copy since the builder could be used again
        }

        public WeaponConfig.Builder addAmmo(AmmoHolder id) {
            this.weaponConfig.general.ammo.add(id);
            return this;
        }

        public Builder setFireRate(int rate) {
            this.weaponConfig.general.rate = rate;
            return this;
        }

        public Builder setGripType(GripType gripType) {
            this.weaponConfig.general.gripType = gripType;
            return this;
        }

        public Builder setMaxAmmo(int maxAmmo) {
            this.weaponConfig.general.maxAmmo = maxAmmo;
            return this;
        }

        public Builder setReloadAmount(int reloadAmount) {
            this.weaponConfig.general.reloadAmount = reloadAmount;
            return this;
        }

        public Builder setReloadTime(int reloadTime) {
            this.weaponConfig.general.reloadTime = reloadTime;
            return this;
        }

        public Builder setLoadingType(LoadingType loadingType) {
            this.weaponConfig.general.loadingType = loadingType;
            return this;
        }

        public Builder setCategory(String category) {
            this.weaponConfig.general.category = category;
            return this;
        }

        public Builder setRecoilAngle(float recoilAngle) {
            this.weaponConfig.general.recoilAngle = recoilAngle;
            return this;
        }

        public Builder setRecoilDurationOffset(float recoilDurationOffset) {
            this.weaponConfig.general.recoilDurationOffset = recoilDurationOffset;
            return this;
        }

        public Builder setRecoilAdsReduction(float recoilAdsReduction) {
            this.weaponConfig.general.recoilAdsReduction = recoilAdsReduction;
            return this;
        }

        public Builder setProjectileAmount(int projectileAmount) {
            this.weaponConfig.general.projectileAmount = projectileAmount;
            return this;
        }

        public Builder setAlwaysSpread(boolean alwaysSpread) {
            this.weaponConfig.general.alwaysSpread = alwaysSpread;
            return this;
        }

        public Builder setSpread(float spread) {
            this.weaponConfig.general.spread = spread;
            return this;
        }
    }
}
