package com.nukateam.ntgl.common.data.config.attachment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.holders.*;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.util.util.GunData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Modifiers implements INBTSerializable<CompoundTag>, IGunModifier {
    @Optional String fireSoundVolume = "";
    @Optional String fireSound = "";
    @Optional String silencedFire = "";
    @Optional float additionalDamage = 0;
    @Optional String damage = "";
    @Optional String projectileSpeed = "";
    @Optional String spread = "";
    @Optional float additionalProjectileGravity = 0;
    @Optional String projectileGravity = "";
    @Optional String projectileLife = "";
    @Optional float recoilModifier = 1;
    @Optional float kickModifier = 1;
    @Optional String muzzleFlashSize = "";
    @Optional String muzzleFlashScale = "";
    @Optional String aimDownSightSpeed = "";
    @Optional String fireRate = "";
    @Optional float criticalChance = 0;
    @Optional String maxAmmo = "";
    @Optional String projectileAmount = "";
    @Optional String fireDelay = "";
    @Optional String reloadStart = "";
    @Optional String reloadTime = "";
    @Optional String reloadEnd = "";
    @Optional String equipTime = "";
    @Optional String ammoPerShot = "";
    @Optional HashMap<FuelType, Integer> maxFuel = new HashMap<>();
    @Optional Set<FireMode> fireModes = new HashSet<>();
    @Optional GripType gripType = null;
    @Optional String needsFullCharge = "";
    @Optional String oneTimeCharge = "";
    @Optional Set<ResourceLocation> ammoItems = new HashSet<>();
    @Optional String autoReload = "";
    @Optional String renderHud = "";
    @Optional LoadingType loadingType = null;
    @Optional Set<FuelType> fuel = new HashSet<>();

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        // Existing serialization
        tag.putString("fireSoundVolume", this.fireSoundVolume);
        tag.putString("fireSound", this.fireSound);
        tag.putString("silencedFire", this.silencedFire);

        // Numeric fields
        tag.putFloat("additionalDamage", this.additionalDamage);
        tag.putString("damage", this.damage);
        tag.putString("projectileSpeed", this.projectileSpeed);
        tag.putString("spread", this.spread);
        tag.putFloat("additionalProjectileGravity", this.additionalProjectileGravity);
        tag.putString("projectileGravity", this.projectileGravity);
        tag.putString("projectileLife", this.projectileLife);
        tag.putFloat("recoilModifier", this.recoilModifier);
        tag.putFloat("kickModifier", this.kickModifier);
        tag.putString("muzzleFlashSize", this.muzzleFlashSize);
        tag.putString("muzzleFlashScale", this.muzzleFlashScale);
        tag.putString("aimDownSightSpeed", this.aimDownSightSpeed);
        tag.putString("fireRate", this.fireRate);
        tag.putFloat("criticalChance", this.criticalChance);
        tag.putString("maxAmmo", this.maxAmmo);
        tag.putString("projectileAmount", this.projectileAmount);
        tag.putString("fireDelay", this.fireDelay);
        tag.putString("reloadStart", this.reloadStart);
        tag.putString("reloadTime", this.reloadTime);
        tag.putString("reloadEnd", this.reloadEnd);
        tag.putString("equipTime", this.equipTime);
        tag.putString("ammoPerShot", this.ammoPerShot);

        // Collections and enums
        writeFireModes(tag);
        writeGripType(tag);
        tag.putString("needsFullCharge", this.needsFullCharge);
        tag.putString("oneTimeCharge", this.oneTimeCharge);
        writeAmmoItems(tag);
        tag.putString("autoReload", this.autoReload);
        tag.putString("renderHud", this.renderHud);
        writeLoadingType(tag);
        writeFuelTypes(tag);
        writeFuelMax(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // Existing deserialization
        if (tag.contains("fireSoundVolume", Tag.TAG_STRING)) this.fireSoundVolume = tag.getString("fireSoundVolume");
        if (tag.contains("fireSound", Tag.TAG_STRING)) this.fireSound = tag.getString("fireSound");
        if (tag.contains("silencedFire", Tag.TAG_BYTE)) this.silencedFire = tag.getString("silencedFire");

        // Numeric fields
        if (tag.contains("additionalDamage", Tag.TAG_FLOAT)) this.additionalDamage = tag.getFloat("additionalDamage");
        if (tag.contains("damage", Tag.TAG_STRING)) this.damage = tag.getString("damage");
        if (tag.contains("projectileSpeed", Tag.TAG_STRING)) this.projectileSpeed = tag.getString("projectileSpeed");
        if (tag.contains("spread", Tag.TAG_STRING)) this.spread = tag.getString("spread");
        if (tag.contains("additionalProjectileGravity", Tag.TAG_FLOAT)) this.additionalProjectileGravity = tag.getFloat("additionalProjectileGravity");
        if (tag.contains("projectileGravity", Tag.TAG_STRING)) this.projectileGravity = tag.getString("projectileGravity");
        if (tag.contains("projectileLife", Tag.TAG_STRING)) this.projectileLife = tag.getString("projectileLife");
        if (tag.contains("recoilModifier", Tag.TAG_FLOAT)) this.recoilModifier = tag.getFloat("recoilModifier");
        if (tag.contains("kickModifier", Tag.TAG_FLOAT)) this.kickModifier = tag.getFloat("kickModifier");
        if (tag.contains("muzzleFlashSize", Tag.TAG_STRING)) this.muzzleFlashSize = tag.getString("muzzleFlashSize");
        if (tag.contains("muzzleFlashScale", Tag.TAG_STRING)) this.muzzleFlashScale = tag.getString("muzzleFlashScale");
        if (tag.contains("aimDownSightSpeed", Tag.TAG_STRING)) this.aimDownSightSpeed = tag.getString("aimDownSightSpeed");
        if (tag.contains("fireRate", Tag.TAG_STRING)) this.fireRate = tag.getString("fireRate");
        if (tag.contains("criticalChance", Tag.TAG_FLOAT)) this.criticalChance = tag.getFloat("criticalChance");
        if (tag.contains("maxAmmo", Tag.TAG_STRING)) this.maxAmmo = tag.getString("maxAmmo");
        if (tag.contains("projectileAmount", Tag.TAG_STRING)) this.projectileAmount = tag.getString("projectileAmount");
        if (tag.contains("fireDelay", Tag.TAG_STRING)) this.fireDelay = tag.getString("fireDelay");
        if (tag.contains("reloadStart", Tag.TAG_STRING)) this.reloadStart = tag.getString("reloadStart");
        if (tag.contains("reloadTime", Tag.TAG_STRING)) this.reloadTime = tag.getString("reloadTime");
        if (tag.contains("reloadEnd", Tag.TAG_STRING)) this.reloadEnd = tag.getString("reloadEnd");
        if (tag.contains("equipTime", Tag.TAG_STRING)) this.equipTime = tag.getString("equipTime");
        if (tag.contains("ammoPerShot", Tag.TAG_STRING)) this.ammoPerShot = tag.getString("ammoPerShot");

        // Collections and enums
        readFireModes(tag);
        readGripType(tag);
        if (tag.contains("needsFullCharge", Tag.TAG_BYTE)) this.needsFullCharge = tag.getString("needsFullCharge");
        if (tag.contains("oneTimeCharge", Tag.TAG_BYTE)) this.oneTimeCharge = tag.getString("oneTimeCharge");
        readAmmoItems(tag);
        if (tag.contains("autoReload", Tag.TAG_BYTE)) this.autoReload = tag.getString("autoReload");
        if (tag.contains("renderHud", Tag.TAG_BYTE)) this.renderHud = tag.getString("renderHud");
        readLoadingType(tag);
        readFuelTypes(tag);
        readFuelMax(tag);
    }

    // Helper methods for collections/enums
    private void writeFireModes(CompoundTag tag) {
        ListTag list = new ListTag();
        this.fireModes.forEach(mode -> list.add(StringTag.valueOf(mode.toString())));
        tag.put("fireModes", list);
    }

    private void readFireModes(CompoundTag tag) {
        if (tag.contains("fireModes", Tag.TAG_LIST)) {
            this.fireModes.clear();
            tag.getList("fireModes", Tag.TAG_STRING).forEach(t ->
                    this.fireModes.add(FireMode.getType(t.getAsString()))
            );
        }
    }

    private void writeGripType(CompoundTag tag) {
        if (this.gripType != null) {
            tag.putString("gripType", this.gripType.toString());
        }
    }

    private void readGripType(CompoundTag tag) {
        if (tag.contains("gripType", Tag.TAG_STRING)) {
            this.gripType = GripType.getType(tag.getString("gripType"));
        }
    }

    private void writeAmmoItems(CompoundTag tag) {
        ListTag list = new ListTag();
        this.ammoItems.forEach(item -> list.add(StringTag.valueOf(item.toString())));
        tag.put("ammoItems", list);
    }

    private void readAmmoItems(CompoundTag tag) {
        if (tag.contains("ammoItems", Tag.TAG_LIST)) {
            this.ammoItems.clear();
            tag.getList("ammoItems", Tag.TAG_STRING).forEach(t ->
                    this.ammoItems.add(ResourceLocation.tryParse(t.getAsString()))
            );
        }
    }

    private void writeLoadingType(CompoundTag tag) {
        if (this.loadingType != null) {
            tag.putString("loadingType", this.loadingType.toString());
        }
    }

    private void readLoadingType(CompoundTag tag) {
        if (tag.contains("loadingType", Tag.TAG_STRING)) {
            this.loadingType = LoadingType.getType(tag.getString("loadingType"));
        }
    }

    private void writeFuelTypes(CompoundTag tag) {
        ListTag list = new ListTag();
        this.fuel.forEach(type -> list.add(StringTag.valueOf(type.toString())));
        tag.put("fuel", list);
    }

    private void readFuelTypes(CompoundTag tag) {
        if (tag.contains("fuel", Tag.TAG_LIST)) {
            this.fuel.clear();
            tag.getList("fuel", Tag.TAG_STRING).forEach(t ->
                    this.fuel.add(FuelType.getType(t.getAsString()))
            );
        }
    }

    private void writeFuelMax(CompoundTag tag) {
        CompoundTag fuelTag = new CompoundTag();
        this.maxFuel.forEach((key, value) -> fuelTag.putInt(key.toString(), value));
        tag.put("maxFuel", fuelTag);
    }

    // В методе deserializeNBT
    private void readFuelMax(CompoundTag tag) {
        if (tag.contains("maxFuel", Tag.TAG_COMPOUND)) {
            CompoundTag fuelTag = tag.getCompound("maxFuel");
            fuelTag.getAllKeys().forEach(key ->
                    this.maxFuel.put(
                            FuelType.getType(key),
                            fuelTag.getInt(key)
                    )
            );
        }
    }

    // Copy and JSON methods
    public Modifiers copy() {
        Modifiers copy = new Modifiers();
        // Existing fields
        copy.fireSoundVolume = this.fireSoundVolume;
        copy.fireSound = this.fireSound;
        copy.silencedFire = this.silencedFire;

        // Numeric fields
        copy.additionalDamage = this.additionalDamage;
        copy.damage = this.damage;
        copy.projectileSpeed = this.projectileSpeed;
        copy.spread = this.spread;
        copy.additionalProjectileGravity = this.additionalProjectileGravity;
        copy.projectileGravity = this.projectileGravity;
        copy.projectileLife = this.projectileLife;
        copy.recoilModifier = this.recoilModifier;
        copy.kickModifier = this.kickModifier;
        copy.muzzleFlashSize = this.muzzleFlashSize;
        copy.muzzleFlashScale = this.muzzleFlashScale;
        copy.aimDownSightSpeed = this.aimDownSightSpeed;
        copy.fireRate = this.fireRate;
        copy.criticalChance = this.criticalChance;
        copy.maxAmmo = this.maxAmmo;
        copy.projectileAmount = this.projectileAmount;
        copy.fireDelay = this.fireDelay;
        copy.reloadStart = this.reloadStart;
        copy.reloadTime = this.reloadTime;
        copy.reloadEnd = this.reloadEnd;
        copy.equipTime = this.equipTime;
        copy.ammoPerShot = this.ammoPerShot;
        copy.maxFuel = new HashMap<>(this.maxFuel);

        // Collections and enums
        copy.fireModes = new HashSet<>(this.fireModes);
        copy.gripType = this.gripType;
        copy.needsFullCharge = this.needsFullCharge;
        copy.oneTimeCharge = this.oneTimeCharge;
        copy.ammoItems = new HashSet<>(this.ammoItems);
        copy.autoReload = this.autoReload;
        copy.renderHud = this.renderHud;
        copy.loadingType = this.loadingType;
        copy.fuel = new HashSet<>(this.fuel);

        return copy;
    }

    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        // Existing fields
        json.addProperty("fireSoundVolume", this.fireSoundVolume);
        json.addProperty("fireSound", this.fireSound);
        json.addProperty("silencedFire", this.silencedFire);

        // Numeric fields
        json.addProperty("additionalDamage", this.additionalDamage);
        json.addProperty("damage", this.damage);
        json.addProperty("projectileSpeed", this.projectileSpeed);
        json.addProperty("spread", this.spread);
        json.addProperty("additionalProjectileGravity", this.additionalProjectileGravity);
        json.addProperty("projectileGravity", this.projectileGravity);
        json.addProperty("projectileLife", this.projectileLife);
        json.addProperty("recoilModifier", this.recoilModifier);
        json.addProperty("kickModifier", this.kickModifier);
        json.addProperty("muzzleFlashSize", this.muzzleFlashSize);
        json.addProperty("muzzleFlashScale", this.muzzleFlashScale);
        json.addProperty("aimDownSightSpeed", this.aimDownSightSpeed);
        json.addProperty("fireRate", this.fireRate);
        json.addProperty("criticalChance", this.criticalChance);
        json.addProperty("maxAmmo", this.maxAmmo);
        json.addProperty("projectileAmount", this.projectileAmount);
        json.addProperty("fireDelay", this.fireDelay);
        json.addProperty("reloadStart", this.reloadStart);
        json.addProperty("reloadTime", this.reloadTime);
        json.addProperty("reloadEnd", this.reloadEnd);
        json.addProperty("equipTime", this.equipTime);
        json.addProperty("ammoPerShot", this.ammoPerShot);
        JsonObject fuelJson = new JsonObject();
        this.maxFuel.forEach((key, value) ->
                fuelJson.addProperty(key.toString(), value)
        );

        json.add("maxFuel", fuelJson);
        var fireModesArray = new JsonArray();
        this.fireModes.forEach(mode -> fireModesArray.add(mode.toString()));
        json.add("fireModes", fireModesArray);

        return json;
    }

    @Override
    public float modifyFireSoundVolume(float volume, GunData data) {
        return IGunModifier.super.modifyFireSoundVolume(volume, data);
    }

    @Override
    public ResourceLocation modifyFireSound(ResourceLocation sound, GunData data) {
        return IGunModifier.super.modifyFireSound(sound, data);
    }

    @Override
    public double modifyFireSoundRadius(double radius, GunData data) {
        return IGunModifier.super.modifyFireSoundRadius(radius, data);
    }

    @Override
    public boolean silencedFire(GunData data) {
        return getBoolean(false, silencedFire);
    }

    @Override
    public boolean modifyNeedsFullCharge(boolean base, GunData data) {
        return getBoolean(base, this.needsFullCharge);
    }

    @Override
    public boolean modifyIsOneTimeCharge(boolean base, GunData data) {
        return getBoolean(base, oneTimeCharge);
    }

    @Override
    public boolean modifyShouldRenderHud(boolean base, GunData data) {
        return getBoolean(base, renderHud);
    }

    @Override
    public Set<ResourceLocation> modifyAmmoItems(Set<ResourceLocation> baseValue, GunData data) {
        return ammoItems != null && !ammoItems.isEmpty() ? this.ammoItems : baseValue;
    }

    @Override
    public boolean modifyAutoReloading(boolean base, GunData data) {
        return getBoolean(base, autoReload);
    }

    @Override
    public LoadingType modifyLoadingType(LoadingType baseValue, GunData data) {
        return loadingType != null ? this.loadingType : baseValue;
    }

    @Override
    public Set<FuelType> modifyFuel(Set<FuelType> baseValue, GunData data) {
        return fuel != null && !fuel.isEmpty() ? this.fuel : baseValue;
    }

    @Override
    public Set<FireMode> modifyFireModes(Set<FireMode> baseValue, GunData data) {
        return fireModes != null && !fireModes.isEmpty() ? this.fireModes : baseValue;
    }

    @Override
    public GripType modifyGripType(GripType baseValue, GunData data) {
        return this.gripType != null ? this.gripType :  baseValue;
    }

    // Calculation methods for each numeric property
    public float additionalDamage(GunData gunData) {
        return additionalDamage;
    }

    public float modifyDamage(float damage, GunData gunData) {
        return calculate(damage, this.damage);
    }

    public double modifyProjectileSpeed(double speed, GunData gunData) {
        return calculate((float) speed, projectileSpeed);
    }

    public float modifyProjectileSpread(float spread, GunData gunData) {
        return calculate(spread, this.spread);
    }

    public double additionalProjectileGravity(GunData gunData) {
        return additionalProjectileGravity;
    }

    public double modifyProjectileGravity(double gravity, GunData gunData) {
        return calculate((float) gravity, projectileGravity);
    }

    public int modifyProjectileLife(int life, GunData gunData) {
        return (int) calculate(life, projectileLife);
    }

    public float recoilModifier(GunData gunData) {
        return recoilModifier;
    }

    public float kickModifier(GunData gunData) {
        return kickModifier;
    }

    public double modifyMuzzleFlashSize(double size, GunData gunData) {
        return calculate((float) size, muzzleFlashSize);
    }

    public double modifyMuzzleFlashScale(double scale, GunData gunData) {
        return calculate((float) scale, muzzleFlashScale);
    }

    public double modifyAimDownSightSpeed(double speed, GunData gunData) {
        return calculate((float) speed, aimDownSightSpeed);
    }

    public int modifyFireRate(int rate, GunData gunData) {
        return (int) calculate(rate, fireRate);
    }

    public float criticalChance(GunData gunData) {
        return criticalChance;
    }

    public int modifyMaxAmmo(int maxAmmo, GunData gunData) {
        return (int) calculate(maxAmmo, this.maxAmmo);
    }

    public int modifyProjectileAmount(int amount, GunData gunData) {
        return (int) calculate(amount, projectileAmount);
    }

    public int modifyFireDelay(int delay, GunData gunData) {
        return (int) calculate(delay, fireDelay);
    }

    public int modifyReloadStart(int time, GunData gunData) {
        return (int) calculate(time, reloadStart);
    }

    public int modifyReloadTime(int time, GunData gunData) {
        return (int) calculate(time, reloadTime);
    }

    public int modifyReloadEnd(int time, GunData gunData) {
        return (int) calculate(time, reloadEnd);
    }

    public int modifyEquipTime(int time, GunData gunData) {
        return (int) calculate(time, equipTime);
    }

    public int modifyAmmoPerShot(int ammo, GunData gunData) {
        return (int) calculate(ammo, ammoPerShot);
    }

    public int modifyMaxFuel(int max, FuelType type, GunData gunData) {
        return maxFuel != null && maxFuel.get(type) != null ? maxFuel.get(type) : max;
    }

    public static boolean getBoolean(boolean base, String mod) {
        if (mod == null || mod.isEmpty()) {
            return base;
        }

        switch (mod){
            case "true" -> {
                return true;
            }
            case "false" -> {
                return false;
            }
            default -> {
                Ntgl.LOGGER.error("Invalid boolean  {}", mod);
                return base;
            }
        }
    }

    public static float calculate(float num, String operation) {
        if (operation == null || operation.isEmpty()) {
            return num;
        }

        try {
            char firstChar = operation.charAt(0);
            if (isOperator(firstChar)) {
                float operand = parseOperand(operation.substring(1));
                return applyOperation(num, firstChar, operand);
            } else {
                return parseOperand(operation);
            }
        } catch (NumberFormatException e) {
            var throwable = new IllegalArgumentException("Invalid number format in operation: " + operation, e);
            Ntgl.LOGGER.error("Invalid number format in operation: {}", operation, throwable);
            return num;
        }
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static float parseOperand(String numberStr) {
        return Float.parseFloat(numberStr);
    }

    private static float applyOperation(float num, char operator, float operand) {
        return switch (operator) {
            case '+' -> num + operand;
            case '-' -> num - operand;
            case '*' -> num * operand;
            case '/' -> {
                if (operand == 0.0) {
                    Ntgl.LOGGER.error("Division by zero", new ArithmeticException("Division by zero"));
                    yield num;
                }
                yield num / operand;
            }
            default -> {
                Ntgl.LOGGER.error("Unsupported operator: {}", operator, new IllegalArgumentException("Unsupported operator: " + operator));
                yield num / operand;
            }
        };
    }
}
