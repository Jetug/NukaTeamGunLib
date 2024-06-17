package com.nukateam.ntgl.common.base.gun;

import com.google.gson.JsonArray;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.data.annotation.Ignored;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunJsonUtil;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.nukateam.ntgl.client.ClientHandler.*;


public class Gun implements INBTSerializable<CompoundTag>, IEditorMenu {
    protected General general = new General();
    protected Projectile projectile = new Projectile();
    //    private Reloads reloads = new Reloads();
    protected Sounds sounds = new Sounds();
    protected Display display = new Display();
    protected Modules modules = new Modules();

    public General getGeneral() {
        return this.general;
    }

    public Projectile getProjectile() {
        return this.projectile;
    }

    public Sounds getSounds() {
        return this.sounds;
    }

    public Display getDisplay() {
        return this.display;
    }

    public Modules getModules() {
        return this.modules;
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

    public static class General implements INBTSerializable<CompoundTag> {
        public static final String LOADING_TYPE = "LoadingType";
        public static final String RATE = "Rate";
        public static final String GRIP_TYPE = "GripType";
        public static final String AUTO = "Auto";
        public static final String MAX_AMMO = "MaxAmmo";
        public static final String RELOAD_SPEED = "ReloadSpeed";
        public static final String RELOAD_TIME = "ReloadTime";
        public static final String RECOIL_ANGLE = "RecoilAngle";
        public static final String RECOIL_KICK = "RecoilKick";
        public static final String RECOIL_DURATION_OFFSET = "RecoilDurationOffset";
        public static final String RECOIL_ADS_REDUCTION = "RecoilAdsReduction";
        public static final String PROJECTILE_AMOUNT = "ProjectileAmount";
        public static final String ALWAYS_SPREAD = "AlwaysSpread";
        public static final String SPREAD = "Spread";
        public static final String MAGAZINE = "magazine";
        public static final String PER_CARTRIDGE = "per_cartridge";
        public static final String CATEGORY = "category";

        @Ignored
        private FireMode fireMode = FireMode.SEMI_AUTO;
        @Optional
        private boolean fullCharge = false;
        private int rate;
        @Ignored
        private GripType gripType = GripType.ONE_HANDED;
        private int maxAmmo;
        @Optional
        private int reloadTime = 1;
        @Optional
        private String loadingType = MAGAZINE;
        @Optional
        private String category = "pistol";
        @Optional
        private int reloadAmount = 1;
        @Optional
        private float recoilAngle;
        @Optional
        private float recoilKick;
        @Optional
        private float recoilDurationOffset;
        @Optional
        private float recoilAdsReduction = 0.2F;
        @Optional
        private int projectileAmount = 1;
        @Optional
        private boolean alwaysSpread;
        @Optional
        private float spread;
        @Optional
        private int fireTimer;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(RATE, this.rate);
            tag.putBoolean("FullCharge", this.fullCharge);
            tag.putInt("FireTimer", this.fireTimer);
            tag.putString("FireMode", this.fireMode.getId().toString());
            tag.putString(GRIP_TYPE, this.gripType.getId().toString());
            tag.putInt(MAX_AMMO, this.maxAmmo);
            tag.putInt(RELOAD_SPEED, this.reloadAmount);
            tag.putInt(RELOAD_TIME, this.reloadTime);
            tag.putString(LOADING_TYPE, this.loadingType);
            tag.putString(CATEGORY, this.category);
            tag.putFloat(RECOIL_ANGLE, this.recoilAngle);
            tag.putFloat(RECOIL_KICK, this.recoilKick);
            tag.putFloat(RECOIL_DURATION_OFFSET, this.recoilDurationOffset);
            tag.putFloat(RECOIL_ADS_REDUCTION, this.recoilAdsReduction);
            tag.putInt(PROJECTILE_AMOUNT, this.projectileAmount);
            tag.putFloat(SPREAD, this.spread);
            tag.putBoolean(ALWAYS_SPREAD, this.alwaysSpread);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("FireMode", Tag.TAG_STRING)) {
                this.fireMode = FireMode.getType(ResourceLocation.tryParse(tag.getString("FireMode")));
            }
            if (tag.contains("FullCharge", Tag.TAG_ANY_NUMERIC)) {
                this.fullCharge = tag.getBoolean("FullCharge");
            }
            if (tag.contains(RATE, Tag.TAG_ANY_NUMERIC)) {
                this.rate = tag.getInt(RATE);
            }
            if (tag.contains("FireTimer", Tag.TAG_ANY_NUMERIC)) {
                this.fireTimer = tag.getInt("FireTimer");
            }
            if (tag.contains(GRIP_TYPE, Tag.TAG_STRING)) {
                this.gripType = GripType.getType(ResourceLocation.tryParse(tag.getString(GRIP_TYPE)));
            }
            if (tag.contains(MAX_AMMO, Tag.TAG_ANY_NUMERIC)) {
                this.maxAmmo = tag.getInt(MAX_AMMO);
            }
            if (tag.contains(RELOAD_SPEED, Tag.TAG_ANY_NUMERIC)) {
                this.reloadAmount = tag.getInt(RELOAD_SPEED);
            }
            if (tag.contains(RELOAD_TIME, Tag.TAG_ANY_NUMERIC)) {
                this.reloadTime = tag.getInt(RELOAD_TIME);
            }
            if (tag.contains(LOADING_TYPE, Tag.TAG_STRING)) {
                this.loadingType = tag.getString(LOADING_TYPE);
            }
            if (tag.contains(CATEGORY, Tag.TAG_STRING)) {
                this.category = tag.getString(CATEGORY);
            }
            if (tag.contains(RECOIL_ANGLE, Tag.TAG_ANY_NUMERIC)) {
                this.recoilAngle = tag.getFloat(RECOIL_ANGLE);
            }
            if (tag.contains(RECOIL_KICK, Tag.TAG_ANY_NUMERIC)) {
                this.recoilKick = tag.getFloat(RECOIL_KICK);
            }
            if (tag.contains(RECOIL_DURATION_OFFSET, Tag.TAG_ANY_NUMERIC)) {
                this.recoilDurationOffset = tag.getFloat(RECOIL_DURATION_OFFSET);
            }
            if (tag.contains(RECOIL_ADS_REDUCTION, Tag.TAG_ANY_NUMERIC)) {
                this.recoilAdsReduction = tag.getFloat(RECOIL_ADS_REDUCTION);
            }
            if (tag.contains(PROJECTILE_AMOUNT, Tag.TAG_ANY_NUMERIC)) {
                this.projectileAmount = tag.getInt(PROJECTILE_AMOUNT);
            }
            if (tag.contains(ALWAYS_SPREAD, Tag.TAG_ANY_NUMERIC)) {
                this.alwaysSpread = tag.getBoolean(ALWAYS_SPREAD);
            }
            if (tag.contains(SPREAD, Tag.TAG_ANY_NUMERIC)) {
                this.spread = tag.getFloat(SPREAD);
            }
        }

        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.rate > 0, "Rate must be more than zero");
            Preconditions.checkArgument(this.maxAmmo > 0, "Max ammo must be more than zero");
            Preconditions.checkArgument(this.reloadAmount >= 1, "Reload amount must be more than or equal to zero");
            Preconditions.checkArgument(this.reloadTime >= 1, "Reload time must be more than or equal to zero");
            Preconditions.checkArgument(!this.loadingType.equals(MAGAZINE) && !this.loadingType.equals(PER_CARTRIDGE), "Loading type must be " + MAGAZINE + " or " + PER_CARTRIDGE);
            Preconditions.checkArgument(this.recoilAngle >= 0.0F, "Recoil angle must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilKick >= 0.0F, "Recoil kick must be more than or equal to zero");
            Preconditions.checkArgument(this.recoilDurationOffset >= 0.0F && this.recoilDurationOffset <= 1.0F, "Recoil duration offset must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.recoilAdsReduction >= 0.0F && this.recoilAdsReduction <= 1.0F, "Recoil ads reduction must be between 0.0 and 1.0");
            Preconditions.checkArgument(this.projectileAmount >= 1, "Projectile amount must be more than or equal to one");
            Preconditions.checkArgument(this.spread >= 0.0F, "Spread must be more than or equal to zero");
            JsonObject object = new JsonObject();
            if (this.fullCharge) object.addProperty("fullCharge", true);
            object.addProperty("rate", this.rate);
            if (this.fireTimer != 0) object.addProperty("fireTimer", this.fireTimer);
            object.addProperty("fireMode", this.fireMode.getId().toString());
            object.addProperty("gripType", this.gripType.getId().toString());
            object.addProperty("maxAmmo", this.maxAmmo);
            if (this.reloadAmount != 1) object.addProperty("reloadAmount", this.reloadAmount);
            if (this.reloadTime != 1) object.addProperty("reloadTime", this.reloadTime);
            if (this.loadingType.equals(MAGAZINE) || this.loadingType.equals(PER_CARTRIDGE))
                object.addProperty("loadingType", this.loadingType);
            if (this.recoilAngle != 0.0F) object.addProperty("recoilAngle", this.recoilAngle);
            if (this.recoilKick != 0.0F) object.addProperty("recoilKick", this.recoilKick);
            if (this.recoilDurationOffset != 0.0F)
                object.addProperty("recoilDurationOffset", this.recoilDurationOffset);
            if (this.recoilAdsReduction != 0.2F) object.addProperty("recoilAdsReduction", this.recoilAdsReduction);
            if (this.projectileAmount != 1) object.addProperty("projectileAmount", this.projectileAmount);
            if (this.alwaysSpread) object.addProperty("alwaysSpread", true);
            if (this.spread != 0.0F) object.addProperty("spread", this.spread);
//            object.add("", new JsonArray());
            return object;
        }

        /**
         * @return A copy of the general get
         */
        public General copy() {
            General general = new General();
            general.fireMode = this.fireMode;
            general.fullCharge = this.fullCharge;
            general.rate = this.rate;
            general.fireTimer = this.fireTimer;
            general.gripType = this.gripType;
            general.maxAmmo = this.maxAmmo;
            general.reloadAmount = this.reloadAmount;
            general.reloadTime = this.reloadTime;
            general.loadingType = this.loadingType;
            general.category = this.category;
            general.recoilAngle = this.recoilAngle;
            general.recoilKick = this.recoilKick;
            general.recoilDurationOffset = this.recoilDurationOffset;
            general.recoilAdsReduction = this.recoilAdsReduction;
            general.projectileAmount = this.projectileAmount;
            general.alwaysSpread = this.alwaysSpread;
            general.spread = this.spread;
            return general;
        }

        /**
         * @return The type of grip this weapon uses
         */
        public FireMode getFireMode() {
            return this.fireMode;
        }

        /**
         * @return If this gun is automatic or not
         */
        public boolean isAuto() {
            return this.fireMode == FireMode.AUTO;
        }

        /**
         * @return If this gun need a full charge to fire
         */
        public boolean isFullCharge() {
            return this.fullCharge;
        }

        /**
         * @return The fire rate of this weapon in ticks
         */
        public int getRate() {
            return this.rate;
        }

        /**
         * @return The timer before firing
         */
        public int getFireTimer() {
            return this.fireTimer;
        }


        /**
         * @return The type of grip this weapon uses
         */
        public GripType getGripType() {
            return this.gripType;
        }

        /**
         * @return The maximum amount of ammo this weapon can hold
         */
        public int getMaxAmmo(@Nullable ItemStack gunStack) {
            if (gunStack != null && gunStack.getItem() instanceof GunItem gunItem) {
                var gun = gunItem.getModifiedGun(gunStack);

                if (gun.getProjectile().isMagazineMode()) {
                    var id = gun.getProjectile().getItem();
                    var item = ForgeRegistries.ITEMS.getValue(id);

                    return item.getMaxDamage(new ItemStack(item));
                }

            }
            return this.maxAmmo;
        }

        /**
         * @return The amount of ammo to add to the weapon each reload cycle
         */
        public int getReloadAmount() {
            return this.reloadAmount;
        }

        /**
         * @return Time to reload the gun
         */
        public int getReloadTime() {
            return this.reloadTime;
        }


        /**
         * @return Type of loading
         */
        public String getLoadingType() {
            return this.loadingType;
        }

        /**
         * @return Weapon category
         */
        public String getCategory() {
            return this.category;
        }

        /**
         * @return The amount of recoil this gun produces upon firing in degrees
         */
        public float getRecoilAngle() {
            return this.recoilAngle;
        }

        /**
         * @return The amount of kick this gun produces upon firing
         */
        public float getRecoilKick() {
            return this.recoilKick;
        }

        /**
         * @return The duration offset for recoil. This reduces the duration of recoil animation
         */
        public float getRecoilDurationOffset() {
            return this.recoilDurationOffset;
        }

        /**
         * @return The amount of reduction applied when aiming down this weapon's sight
         */
        public float getRecoilAdsReduction() {
            return this.recoilAdsReduction;
        }

        /**
         * @return The amount of projectiles this weapon fires
         */
        public int getProjectileAmount() {
            return this.projectileAmount;
        }

        /**
         * @return If this weapon should always spread it's projectiles according to {@link #getSpread()}
         */
        public boolean isAlwaysSpread() {
            return this.alwaysSpread;
        }

        /**
         * @return The maximum amount of degrees applied to the initial pitch and yaw direction of
         * the fired projectile.
         */
        public float getSpread() {
            return this.spread;
        }
    }

    public static class Projectile implements INBTSerializable<CompoundTag> {
        private ResourceLocation item = new ResourceLocation(Ntgl.MOD_ID, "basic_ammo");
        @Optional
        private boolean visible;
        private float damage;
        private float size;
        private double speed;
        private int life;
        @Optional
        private boolean gravity;
        @Optional
        private boolean damageReduceOverLife;
        @Optional
        private boolean magazineMode;
        @Optional
        private int trailColor = 0xFFD289;
        @Optional
        private double trailLengthMultiplier = 1.0;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Item", this.item.toString());
            tag.putBoolean("Visible", this.visible);
            tag.putFloat("Damage", this.damage);
            tag.putFloat("Size", this.size);
            tag.putDouble("Speed", this.speed);
            tag.putInt("Life", this.life);
            tag.putBoolean("Gravity", this.gravity);
            tag.putBoolean("DamageReduceOverLife", this.damageReduceOverLife);
            tag.putBoolean("MagazineMode", this.magazineMode);
            tag.putInt("TrailColor", this.trailColor);
            tag.putDouble("TrailLengthMultiplier", this.trailLengthMultiplier);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Item", Tag.TAG_STRING)) {
                this.item = new ResourceLocation(tag.getString("Item"));
            }
            if (tag.contains("Visible", Tag.TAG_ANY_NUMERIC)) {
                this.visible = tag.getBoolean("Visible");
            }
            if (tag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
                this.damage = tag.getFloat("Damage");
            }
            if (tag.contains("Size", Tag.TAG_ANY_NUMERIC)) {
                this.size = tag.getFloat("Size");
            }
            if (tag.contains("Speed", Tag.TAG_ANY_NUMERIC)) {
                this.speed = tag.getDouble("Speed");
            }
            if (tag.contains("Life", Tag.TAG_ANY_NUMERIC)) {
                this.life = tag.getInt("Life");
            }
            if (tag.contains("Gravity", Tag.TAG_ANY_NUMERIC)) {
                this.gravity = tag.getBoolean("Gravity");
            }
            if (tag.contains("DamageReduceOverLife", Tag.TAG_ANY_NUMERIC)) {
                this.damageReduceOverLife = tag.getBoolean("DamageReduceOverLife");
            }
            if (tag.contains("MagazineMode", Tag.TAG_ANY_NUMERIC)) {
                this.magazineMode = tag.getBoolean("MagazineMode");
            }
            if (tag.contains("TrailColor", Tag.TAG_ANY_NUMERIC)) {
                this.trailColor = tag.getInt("TrailColor");
            }
            if (tag.contains("TrailLengthMultiplier", Tag.TAG_ANY_NUMERIC)) {
                this.trailLengthMultiplier = tag.getDouble("TrailLengthMultiplier");
            }
        }

        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.damage >= 0.0F, "Damage must be more than or equal to zero");
            Preconditions.checkArgument(this.size >= 0.0F, "Projectile size must be more than or equal to zero");
            Preconditions.checkArgument(this.speed >= 0.0, "Projectile speed must be more than or equal to zero");
            Preconditions.checkArgument(this.life > 0, "Projectile life must be more than zero");
            Preconditions.checkArgument(this.trailLengthMultiplier >= 0.0, "Projectile trail length multiplier must be more than or equal to zero");
            JsonObject object = new JsonObject();
            object.addProperty("item", this.item.toString());
            if (this.visible) object.addProperty("visible", true);
            object.addProperty("damage", this.damage);
            object.addProperty("size", this.size);
            object.addProperty("speed", this.speed);
            object.addProperty("life", this.life);
            if (this.gravity) object.addProperty("gravity", true);
            if (this.damageReduceOverLife) object.addProperty("damageReduceOverLife", this.damageReduceOverLife);
            if (this.magazineMode) object.addProperty("magazineMode", this.magazineMode);
            if (this.trailColor != 0xFFD289) object.addProperty("trailColor", this.trailColor);
            if (this.trailLengthMultiplier != 1.0)
                object.addProperty("trailLengthMultiplier", this.trailLengthMultiplier);
            return object;
        }

        public Projectile copy() {
            Projectile projectile = new Projectile();
            projectile.item = this.item;
            projectile.visible = this.visible;
            projectile.damage = this.damage;
            projectile.size = this.size;
            projectile.speed = this.speed;
            projectile.life = this.life;
            projectile.gravity = this.gravity;
            projectile.damageReduceOverLife = this.damageReduceOverLife;
            projectile.magazineMode = this.magazineMode;
            projectile.trailColor = this.trailColor;
            projectile.trailLengthMultiplier = this.trailLengthMultiplier;
            return projectile;
        }

        /**
         * @return The registry id of the ammo item
         */
        public ResourceLocation getItem() {
            return this.item;
        }

        /**
         * @return If this projectile should be visible when rendering
         */
        public boolean isVisible() {
            return this.visible;
        }

        /**
         * @return The damage caused by this projectile
         */
        public float getDamage() {
            return this.damage;
        }

        /**
         * @return The size of the projectile entity bounding box
         */
        public float getSize() {
            return this.size;
        }

        /**
         * @return The speed the projectile moves every tick
         */
        public double getSpeed() {
            return this.speed;
        }

        /**
         * @return The amount of ticks before this projectile is removed
         */
        public int getLife() {
            return this.life;
        }

        /**
         * @return If gravity should be applied to the projectile
         */
        public boolean isGravity() {
            return this.gravity;
        }

        /**
         * @return If the damage should reduce the further the projectile travels
         */
        public boolean isDamageReduceOverLife() {
            return this.damageReduceOverLife;
        }


        public boolean isMagazineMode() {
            return this.magazineMode;
        }

        /**
         * @return The color of the projectile trail in rgba integer format
         */
        public int getTrailColor() {
            return this.trailColor;
        }

        /**
         * @return The multiplier to change the length of the projectile trail
         */
        public double getTrailLengthMultiplier() {
            return this.trailLengthMultiplier;
        }
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
        private Attachments attachments = new Attachments();

        @Nullable
        public Zoom getZoom() {
            return this.zoom;
        }

        public Attachments getAttachments() {
            return this.attachments;
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

        public static class Attachments implements INBTSerializable<CompoundTag> {
            @Optional
            @Nullable
            private ScaledPositioned scope;
            @Optional
            @Nullable
            private ScaledPositioned barrel;
            @Optional
            @Nullable
            private ScaledPositioned stock;
            @Optional
            @Nullable
            private ScaledPositioned underBarrel;

            @Nullable
            public ScaledPositioned getScope() {
                return this.scope;
            }

            @Nullable
            public ScaledPositioned getBarrel() {
                return this.barrel;
            }

            @Nullable
            public ScaledPositioned getStock() {
                return this.stock;
            }

            @Nullable
            public ScaledPositioned getUnderBarrel() {
                return this.underBarrel;
            }

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = new CompoundTag();
                if (this.scope != null) {
                    tag.put("Scope", this.scope.serializeNBT());
                }
                if (this.barrel != null) {
                    tag.put("Barrel", this.barrel.serializeNBT());
                }
                if (this.stock != null) {
                    tag.put("Stock", this.stock.serializeNBT());
                }
                if (this.underBarrel != null) {
                    tag.put("UnderBarrel", this.underBarrel.serializeNBT());
                }
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                if (tag.contains("Scope", Tag.TAG_COMPOUND)) {
                    this.scope = this.createScaledPositioned(tag, "Scope");
                }
                if (tag.contains("Barrel", Tag.TAG_COMPOUND)) {
                    this.barrel = this.createScaledPositioned(tag, "Barrel");
                }
                if (tag.contains("Stock", Tag.TAG_COMPOUND)) {
                    this.stock = this.createScaledPositioned(tag, "Stock");
                }
                if (tag.contains("UnderBarrel", Tag.TAG_COMPOUND)) {
                    this.underBarrel = this.createScaledPositioned(tag, "UnderBarrel");
                }
            }

            public JsonObject toJsonObject() {
                JsonObject object = new JsonObject();
                if (this.scope != null) {
                    object.add("scope", this.scope.toJsonObject());
                }
                if (this.barrel != null) {
                    object.add("barrel", this.barrel.toJsonObject());
                }
                if (this.stock != null) {
                    object.add("stock", this.stock.toJsonObject());
                }
                if (this.underBarrel != null) {
                    object.add("underBarrel", this.underBarrel.toJsonObject());
                }
                return object;
            }

            public Attachments copy() {
                Attachments attachments = new Attachments();
                if (this.scope != null) {
                    attachments.scope = this.scope.copy();
                }
                if (this.barrel != null) {
                    attachments.barrel = this.barrel.copy();
                }
                if (this.stock != null) {
                    attachments.stock = this.stock.copy();
                }
                if (this.underBarrel != null) {
                    attachments.underBarrel = this.underBarrel.copy();
                }
                return attachments;
            }

            @Nullable
            private ScaledPositioned createScaledPositioned(CompoundTag tag, String key) {
                CompoundTag attachment = tag.getCompound(key);
                return attachment.isEmpty() ? null : new ScaledPositioned(attachment);
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.zoom != null) {
                tag.put("Zoom", this.zoom.serializeNBT());
            }
            tag.put("Attachments", this.attachments.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Zoom", Tag.TAG_COMPOUND)) {
                Zoom zoom = new Zoom();
                zoom.deserializeNBT(tag.getCompound("Zoom"));
                this.zoom = zoom;
            }
            if (tag.contains("Attachments", Tag.TAG_COMPOUND)) {
                this.attachments.deserializeNBT(tag.getCompound("Attachments"));
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.zoom != null) {
                object.add("zoom", this.zoom.toJsonObject());
            }
            GunJsonUtil.addObjectIfNotEmpty(object, "attachments", this.attachments.toJsonObject());
            return object;
        }

        public Modules copy() {
            Modules modules = new Modules();
            if (this.zoom != null) {
                modules.zoom = this.zoom.copy();
            }
            modules.attachments = this.attachments.copy();
            return modules;
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
        tag.put("Projectile", this.projectile.serializeNBT());
        tag.put("Sounds", this.sounds.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        tag.put("Modules", this.modules.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("General", Tag.TAG_COMPOUND)) {
            this.general.deserializeNBT(tag.getCompound("General"));
        }
        if (tag.contains("Projectile", Tag.TAG_COMPOUND)) {
            this.projectile.deserializeNBT(tag.getCompound("Projectile"));
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
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("general", this.general.toJsonObject());
        object.add("projectile", this.projectile.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "sounds", this.sounds.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "display", this.display.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object, "modules", this.modules.toJsonObject());
        return object;
    }

    public static Gun create(CompoundTag tag) {
        Gun gun = new Gun();
        gun.deserializeNBT(tag);
        return gun;
    }

    public Gun copy() {
        Gun gun = new Gun();
        gun.general = this.general.copy();
        gun.projectile = this.projectile.copy();
        gun.sounds = this.sounds.copy();
        gun.display = this.display.copy();
        gun.modules = this.modules.copy();
        return gun;
    }

    public boolean canAttachType(@Nullable IAttachment.Type type) {
        if (this.modules.attachments != null && type != null) {
            switch (type) {
                case SCOPE:
                    return this.modules.attachments.scope != null;
                case BARREL:
                    return this.modules.attachments.barrel != null;
                case STOCK:
                    return this.modules.attachments.stock != null;
                case UNDER_BARREL:
                    return this.modules.attachments.underBarrel != null;
            }
        }
        return false;
    }

    @Nullable
    public ScaledPositioned getAttachmentPosition(IAttachment.Type type) {
        if (this.modules.attachments != null) {
            switch (type) {
                case SCOPE:
                    return this.modules.attachments.scope;
                case BARREL:
                    return this.modules.attachments.barrel;
                case STOCK:
                    return this.modules.attachments.stock;
                case UNDER_BARREL:
                    return this.modules.attachments.underBarrel;
            }
        }
        return null;
    }

    public boolean canAimDownSight() {
        return this.canAttachType(IAttachment.Type.SCOPE) || this.modules.zoom != null;
    }

    public static ItemStack getScopeStack(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound("Scope"));
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, Gun gun, IAttachment.Type type) {
        if (!gun.canAttachType(type))
            return false;

        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            return attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    @Nullable
    public static Scope getScope(ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains("Scope", Tag.TAG_COMPOUND)) {
                ItemStack scopeStack = ItemStack.of(attachment.getCompound("Scope"));
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

    public static ItemStack getAttachment(IAttachment.Type type, ItemStack gun) {
        CompoundTag compound = gun.getTag();
        if (compound != null && compound.contains("Attachments", Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound("Attachments");
            if (attachment.contains(type.getTagKey(), Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound(type.getTagKey()));
            }
        }
        return ItemStack.EMPTY;
    }

    public static float getAdditionalDamage(ItemStack gunStack) {
        CompoundTag tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static AmmoContext findAmmo(Player player, ResourceLocation id) {
        if (player.isCreative()) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            ItemStack ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
            return new AmmoContext(ammo, null);
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isAmmo(stack, id)) {
                return new AmmoContext(stack, player.getInventory());
            }
        }
        if (Ntgl.backpackedLoaded) {
            return BackpackHelper.findAmmo(player, id);
        }
        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Player player, ResourceLocation id) {
        if (player.isCreative()) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            ItemStack ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
            return new AmmoContext(ammo, null);
        }

        ItemStack ammo = null;

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0)
                    return new AmmoContext(stack, player.getInventory());
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage()))
                    ammo = stack;
            }
        }

        if (ammo != null) return new AmmoContext(ammo, player.getInventory());

        if (Ntgl.backpackedLoaded) {
            return BackpackHelper.findMagazine(player, id);
        }

        return AmmoContext.NONE;
    }

    public static boolean isAmmo(ItemStack stack, ResourceLocation id) {
        return stack != null && Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), id);
    }

    public static int getAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getInt("AmmoCount");
    }

    public static boolean hasAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getBoolean("IgnoreAmmo") || tag.getInt(Tags.AMMO_COUNT) > 0;
    }

    public static void fillAmmo(ItemStack gunStack) {
        if (gunStack.getItem() instanceof GunItem gunItem) {
            var tag = gunStack.getOrCreateTag();
            var maxAmmo = gunItem.getModifiedGun(gunStack).getGeneral().getMaxAmmo(gunStack);
            tag.putInt(Tags.AMMO_COUNT, maxAmmo);
        }
    }

    public static float getFovModifier(ItemStack stack, Gun modifiedGun) {
        float modifier = 0.0F;
        if (hasAttachmentEquipped(stack, modifiedGun, IAttachment.Type.SCOPE)) {
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

        public static Builder create() {
            return new Builder();
        }

        public Gun build() {
            return this.gun.copy(); //Copy since the builder could be used again
        }

        public Builder setFireRate(int rate) {
            this.gun.general.rate = rate;
            return this;
        }

        public Builder setGripType(GripType gripType) {
            this.gun.general.gripType = gripType;
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

        public Builder setLoadingType(String loadingType) {
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

        public Builder setAmmo(Item item) {
            this.gun.projectile.item = ForgeRegistries.ITEMS.getKey(item);
            return this;
        }

        public Builder setProjectileVisible(boolean visible) {
            this.gun.projectile.visible = visible;
            return this;
        }

        public Builder setProjectileSize(float size) {
            this.gun.projectile.size = size;
            return this;
        }

        public Builder setProjectileSpeed(double speed) {
            this.gun.projectile.speed = speed;
            return this;
        }

        public Builder setProjectileLife(int life) {
            this.gun.projectile.life = life;
            return this;
        }

        public Builder setProjectileAffectedByGravity(boolean gravity) {
            this.gun.projectile.gravity = gravity;
            return this;
        }

        public Builder setProjectileTrailColor(int trailColor) {
            this.gun.projectile.trailColor = trailColor;
            return this;
        }

        public Builder setProjectileTrailLengthMultiplier(int trailLengthMultiplier) {
            this.gun.projectile.trailLengthMultiplier = trailLengthMultiplier;
            return this;
        }

        public Builder setDamage(float damage) {
            this.gun.projectile.damage = damage;
            return this;
        }

        public Builder setReduceDamageOverLife(boolean damageReduceOverLife) {
            this.gun.projectile.damageReduceOverLife = damageReduceOverLife;
            return this;
        }

        public Builder setMagazineMode(boolean magazineMode) {
            this.gun.projectile.magazineMode = magazineMode;
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
            Display.Flash flash = new Display.Flash();
            flash.size = size;
            flash.xOffset = xOffset;
            flash.yOffset = yOffset;
            flash.zOffset = zOffset;
            this.gun.display.flash = flash;
            return this;
        }

        public Builder setZoom(float fovModifier, double xOffset, double yOffset, double zOffset) {
            Modules.Zoom zoom = new Modules.Zoom();
            zoom.fovModifier = fovModifier;
            zoom.xOffset = xOffset;
            zoom.yOffset = yOffset;
            zoom.zOffset = zOffset;
            this.gun.modules.zoom = zoom;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setZoom(Modules.Zoom.Builder builder) {
            this.gun.modules.zoom = builder.build();
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setScope(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.scope = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setBarrel(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.barrel = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setStock(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.stock = positioned;
            return this;
        }

        @Deprecated(since = "1.3.0", forRemoval = true)
        public Builder setUnderBarrel(float scale, double xOffset, double yOffset, double zOffset) {
            ScaledPositioned positioned = new ScaledPositioned();
            positioned.scale = scale;
            positioned.xOffset = xOffset;
            positioned.yOffset = yOffset;
            positioned.zOffset = zOffset;
            this.gun.modules.attachments.underBarrel = positioned;
            return this;
        }
    }
}
