package com.nukateam.ntgl.common.data.config.gun;

import com.google.gson.Gson;
import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.config.*;
import com.nukateam.ntgl.common.data.holders.*;

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
    protected HashMap<AttackMode, WeaponSettings> modes = new HashMap<>();
    protected Display display = new Display();
    protected Modules modules = new Modules();
    protected HashMap<AnimationType, ResourceLocation> animations = new HashMap<>();
    protected LinkedHashMap<ResourceLocation, AmmoData> ammoData = new LinkedHashMap<>();
    protected LinkedHashMap<ResourceLocation, Fuel> fuel = new LinkedHashMap<>();
    protected HashMap<String, ResourceLocation> sounds = new HashMap<>();
    protected HashMap<String, ResourceLocation> textures = new HashMap<>();

    private static General getWeapon(){
        var gun = new General();
        gun.weaponMode = WeaponMode.GUN;
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
            var scope = GunStateHelper.getScopeStack(heldItem);
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
        tag.put(DISPLAY, this.display.serializeNBT());
        tag.put(MODULES, this.modules.serializeNBT());
        tag.put(TEXTURES, NbtUtils.serializeStringMap(this.textures));
        tag.put(ANIMATIONS, NbtUtils.serializeStringMap(this.animations));
        tag.put(AMMO_DATA, NbtUtils.serializeMap(this.ammoData));
        tag.put(SECONDARY_AMMO, NbtUtils.serializeMap(this.fuel));
        tag.put(MODES, NbtUtils.serializeMap(this.modes));
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
        if (tag.contains(DISPLAY, Tag.TAG_COMPOUND)) {
            this.display.deserializeNBT(tag.getCompound(DISPLAY));
        }
        if (tag.contains(MODULES, Tag.TAG_COMPOUND)) {
            this.modules.deserializeNBT(tag.getCompound(MODULES));
        }
        if (tag.contains(TEXTURES, Tag.TAG_COMPOUND)) {
            this.textures = NbtUtils.deserializeRLMap(tag.getCompound(TEXTURES));
        }
        if (tag.contains(ANIMATIONS, Tag.TAG_COMPOUND)) {
            this.animations = NbtUtils.deserializeMap(tag.getCompound(ANIMATIONS),
                    (nbt) -> AnimationType.getType(nbt),
                    (nbt, key) -> ResourceLocation.tryParse(nbt.getString(key))
            );
        }
        if (tag.contains(AMMO_DATA, Tag.TAG_COMPOUND)) {
            this.ammoData = NbtUtils.deserializeLinkedMap(tag.getCompound(AMMO_DATA), (nbt) -> AmmoData.create(nbt));
        }
        if (tag.contains(SECONDARY_AMMO, Tag.TAG_COMPOUND)) {
            this.fuel = NbtUtils.deserializeLinkedMap(tag.getCompound(SECONDARY_AMMO), (nbt) -> Fuel.create(nbt));
        }
        if (tag.contains(MODES, Tag.TAG_COMPOUND)) {
            this.modes = NbtUtils.deserializeMap(tag.getCompound(MODES),
                    AttackMode::getType,
                    (nbt, key) -> WeaponSettings.create(nbt.getCompound(key)));
        }
    }

    public JsonObject toJsonObject() {
        var gson = new Gson();
        var object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        object.add("melee", this.melee.toJsonObject());
        object.add("throwable", this.throwable.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"ammoData", gson.toJsonTree(this.ammoData).getAsJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"sounds", gson.toJsonTree(this.sounds).getAsJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "display", this.display.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules", this.modules.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modes", gson.toJsonTree(this.modes).getAsJsonObject());
        return object;
    }

    public WeaponConfig copy() {
        var gun = new WeaponConfig();
        gun.general = this.general.copy();
        gun.melee = this.melee.copy();
        gun.throwable = this.throwable.copy();
        gun.sounds   = (HashMap<String, ResourceLocation>)  this.sounds.clone();
        gun.textures = (HashMap<String, ResourceLocation>)  this.textures.clone();
        gun.animations = (HashMap<AnimationType, ResourceLocation>) this.animations.clone();
        gun.ammoData = (LinkedHashMap<ResourceLocation, AmmoData>) this.ammoData.clone();
        gun.fuel = (LinkedHashMap<ResourceLocation, Fuel>) this.fuel.clone();
        gun.display = this.display.copy();
        gun.modules = this.modules.copy();
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

    public Sounds getSounds() {
        return new Sounds(this);
    }

    public HashMap<String, ResourceLocation> getSoundsMap() {
        return sounds;
    }

    public Display getDisplay() {
        return this.display;
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

    public boolean canAttachType(@Nullable AttachmentType type, WeaponConfig weaponConfig) {
        var attachments = weaponConfig.getModules().getAttachments();
        if(attachments == null)
            return false;
        return attachments.containsKey(type);
    } 

    public boolean canAimDownSight() {
        return this.modules.zoom != null;
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

    public void playCockSound(LivingEntity player) {
        if(!player.level().isClientSide) {
            var cockSound = this.getSounds().getCock();
            if (!player.isAlive()) return;

            if (cockSound == null) cockSound = ModSounds.ITEM_PISTOL_COCK.get().getLocation();

            var radius = Config.SERVER.reloadMaxDistance.get();
            var messageSound = new S2CMessageGunSound(cockSound,
                    SoundSource.PLAYERS, player,
                    1.0F, 1.0F,
                    false, true);

            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(player.level(), player.getX(), player.getY() + 1.0, player.getZ(), radius),
                    messageSound);
        }
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

    public WeaponMode getWeaponMode(AttackMode mode){
        if(mode == AttackMode.PRIMARY){
            return general.weaponMode;
        }
        else {
            var value = modes.get(mode);
            if(value != null)
                return value.getGeneral().getWeaponMode();
            else return WeaponMode.NONE;
        }
    }

    public General getGeneral(AttackMode mode) {
        if(mode == AttackMode.PRIMARY)
            return general;
        else return modes.getOrDefault(mode, new WeaponSettings()).getGeneral();
    }

    public Melee getMelee(AttackMode mode) {
        if(mode == AttackMode.PRIMARY)
            return melee;
        else return modes.getOrDefault(mode, new WeaponSettings()).getMelee();
    }

    public ThrowableConfig getThrowable(AttackMode mode) {
        if(mode == AttackMode.PRIMARY)
            return throwable;
        else return modes.getOrDefault(mode, new WeaponSettings()).getThrowable();
    }

    private static ResourceLocation prepareTexture(String itemId, ResourceLocation path) {
        return ResourceLocation.tryBuild(path.getNamespace(), "textures/guns/" + itemId + "/" + path.getPath() + ".png");
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

        public Builder setRecoilKick(float recoilKick) {
            this.weaponConfig.general.recoilKick = recoilKick;
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

//        public Builder setFireSound(SoundEvent sound) {
//            this.gun.sounds.fire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
//            return this;
//        }
//
//        public Builder setReloadSound(SoundEvent sound) {
//            this.gun.sounds.reload = ForgeRegistries.SOUND_EVENTS.getKey(sound);
//            return this;
//        }
//
//        public Builder setCockSound(SoundEvent sound) {
//            this.gun.sounds.cock = ForgeRegistries.SOUND_EVENTS.getKey(sound);
//            return this;
//        }
//
//        public Builder setSilencedFireSound(SoundEvent sound) {
//            this.gun.sounds.silencedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
//            return this;
//        }
//
//        public Builder setEnchantedFireSound(SoundEvent sound) {
//            this.gun.sounds.enchantedFire = ForgeRegistries.SOUND_EVENTS.getKey(sound);
//            return this;
//        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setMuzzleFlash(double size, double xOffset, double yOffset, double zOffset) {
            var flash = new Display.Flash();
            flash.size = size;
            flash.xOffset = xOffset;
            flash.yOffset = yOffset;
            flash.zOffset = zOffset;
            this.weaponConfig.display.flash = flash;
            return this;
        }

        public Builder setZoom(float fovModifier, double xOffset, double yOffset, double zOffset) {
            var zoom = new Modules.Zoom();
            zoom.fovModifier = fovModifier;
            zoom.xOffset = xOffset;
            zoom.yOffset = yOffset;
            zoom.zOffset = zOffset;
            this.weaponConfig.modules.zoom = zoom;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setZoom(Modules.Zoom.Builder builder) {
            this.weaponConfig.modules.zoom = builder.build();
            return this;
        }
    }
}
