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
    @Optional boolean silencedFire = false;
    @Optional String additionalDamage = "";
    @Optional String modifyDamage = "";
    @Optional String modifyProjectileSpeed = "";
    @Optional String modifyProjectileSpread = "";
    @Optional String additionalProjectileGravity = "";
    @Optional String modifyProjectileGravity = "";
    @Optional String modifyProjectileLife = "";
    @Optional float recoilModifier = 1;
    @Optional float kickModifier = 1;
    @Optional String modifyMuzzleFlashSize = "";
    @Optional String modifyMuzzleFlashScale = "";
    @Optional String modifyAimDownSightSpeed = "";
    @Optional String modifyFireRate = "";
    @Optional float criticalChance = 0;
    @Optional String modifyMaxAmmo = "";
    @Optional String modifyProjectileAmount = "";
    @Optional String modifyFireDelay = "";
    @Optional String modifyReloadStart = "";
    @Optional String modifyReloadTime = "";
    @Optional String modifyReloadEnd = "";
    @Optional String modifyEquipTime = "";
    @Optional String modifyAmmoPerShot = "";
    @Optional HashMap<FuelType, Integer> modifyMaxFuel = new HashMap<>();

    // Collections and enums
    @Optional Set<FireMode> fireModes = new HashSet<>();
    @Optional
    GripType gripType = null;
    @Optional boolean modifyNeedsFullCharge = false;
    @Optional boolean modifyIsOneTimeCharge = false;
    @Optional
    Set<ResourceLocation> ammoItems = new HashSet<>();
    @Optional boolean modifyAutoReloading = false;
    @Optional boolean modifyShouldRenderHud = false;
    @Optional
    LoadingType loadingType = null;
    @Optional Set<FuelType> modifyFuel = new HashSet<>();

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        // Existing serialization
        tag.putString("fireSoundVolume", this.fireSoundVolume);
        tag.putString("fireSound", this.fireSound);
        tag.putBoolean("silencedFire", this.silencedFire);

        // Numeric fields
        tag.putString("additionalDamage", this.additionalDamage);
        tag.putString("modifyDamage", this.modifyDamage);
        tag.putString("modifyProjectileSpeed", this.modifyProjectileSpeed);
        tag.putString("modifyProjectileSpread", this.modifyProjectileSpread);
        tag.putString("additionalProjectileGravity", this.additionalProjectileGravity);
        tag.putString("modifyProjectileGravity", this.modifyProjectileGravity);
        tag.putString("modifyProjectileLife", this.modifyProjectileLife);
        tag.putFloat("recoilModifier", this.recoilModifier);
        tag.putFloat("kickModifier", this.kickModifier);
        tag.putString("modifyMuzzleFlashSize", this.modifyMuzzleFlashSize);
        tag.putString("modifyMuzzleFlashScale", this.modifyMuzzleFlashScale);
        tag.putString("modifyAimDownSightSpeed", this.modifyAimDownSightSpeed);
        tag.putString("modifyFireRate", this.modifyFireRate);
        tag.putFloat("criticalChance", this.criticalChance);
        tag.putString("modifyMaxAmmo", this.modifyMaxAmmo);
        tag.putString("modifyProjectileAmount", this.modifyProjectileAmount);
        tag.putString("modifyFireDelay", this.modifyFireDelay);
        tag.putString("modifyReloadStart", this.modifyReloadStart);
        tag.putString("modifyReloadTime", this.modifyReloadTime);
        tag.putString("modifyReloadEnd", this.modifyReloadEnd);
        tag.putString("modifyEquipTime", this.modifyEquipTime);
        tag.putString("modifyAmmoPerShot", this.modifyAmmoPerShot);

        // Collections and enums
        writeFireModes(tag);
        writeGripType(tag);
        tag.putBoolean("modifyNeedsFullCharge", this.modifyNeedsFullCharge);
        tag.putBoolean("modifyIsOneTimeCharge", this.modifyIsOneTimeCharge);
        writeAmmoItems(tag);
        tag.putBoolean("modifyAutoReloading", this.modifyAutoReloading);
        tag.putBoolean("modifyShouldRenderHud", this.modifyShouldRenderHud);
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
        if (tag.contains("silencedFire", Tag.TAG_BYTE)) this.silencedFire = tag.getBoolean("silencedFire");

        // Numeric fields
        if (tag.contains("additionalDamage", Tag.TAG_STRING)) this.additionalDamage = tag.getString("additionalDamage");
        if (tag.contains("modifyDamage", Tag.TAG_STRING)) this.modifyDamage = tag.getString("modifyDamage");
        if (tag.contains("modifyProjectileSpeed", Tag.TAG_STRING)) this.modifyProjectileSpeed = tag.getString("modifyProjectileSpeed");
        if (tag.contains("modifyProjectileSpread", Tag.TAG_STRING)) this.modifyProjectileSpread = tag.getString("modifyProjectileSpread");
        if (tag.contains("additionalProjectileGravity", Tag.TAG_STRING)) this.additionalProjectileGravity = tag.getString("additionalProjectileGravity");
        if (tag.contains("modifyProjectileGravity", Tag.TAG_STRING)) this.modifyProjectileGravity = tag.getString("modifyProjectileGravity");
        if (tag.contains("modifyProjectileLife", Tag.TAG_STRING)) this.modifyProjectileLife = tag.getString("modifyProjectileLife");
        if (tag.contains("recoilModifier", Tag.TAG_FLOAT)) this.recoilModifier = tag.getFloat("recoilModifier");
        if (tag.contains("kickModifier", Tag.TAG_FLOAT)) this.kickModifier = tag.getFloat("kickModifier");
        if (tag.contains("modifyMuzzleFlashSize", Tag.TAG_STRING)) this.modifyMuzzleFlashSize = tag.getString("modifyMuzzleFlashSize");
        if (tag.contains("modifyMuzzleFlashScale", Tag.TAG_STRING)) this.modifyMuzzleFlashScale = tag.getString("modifyMuzzleFlashScale");
        if (tag.contains("modifyAimDownSightSpeed", Tag.TAG_STRING)) this.modifyAimDownSightSpeed = tag.getString("modifyAimDownSightSpeed");
        if (tag.contains("modifyFireRate", Tag.TAG_STRING)) this.modifyFireRate = tag.getString("modifyFireRate");
        if (tag.contains("criticalChance", Tag.TAG_FLOAT)) this.criticalChance = tag.getFloat("criticalChance");
        if (tag.contains("modifyMaxAmmo", Tag.TAG_STRING)) this.modifyMaxAmmo = tag.getString("modifyMaxAmmo");
        if (tag.contains("modifyProjectileAmount", Tag.TAG_STRING)) this.modifyProjectileAmount = tag.getString("modifyProjectileAmount");
        if (tag.contains("modifyFireDelay", Tag.TAG_STRING)) this.modifyFireDelay = tag.getString("modifyFireDelay");
        if (tag.contains("modifyReloadStart", Tag.TAG_STRING)) this.modifyReloadStart = tag.getString("modifyReloadStart");
        if (tag.contains("modifyReloadTime", Tag.TAG_STRING)) this.modifyReloadTime = tag.getString("modifyReloadTime");
        if (tag.contains("modifyReloadEnd", Tag.TAG_STRING)) this.modifyReloadEnd = tag.getString("modifyReloadEnd");
        if (tag.contains("modifyEquipTime", Tag.TAG_STRING)) this.modifyEquipTime = tag.getString("modifyEquipTime");
        if (tag.contains("modifyAmmoPerShot", Tag.TAG_STRING)) this.modifyAmmoPerShot = tag.getString("modifyAmmoPerShot");

        // Collections and enums
        readFireModes(tag);
        readGripType(tag);
        if (tag.contains("modifyNeedsFullCharge", Tag.TAG_BYTE)) this.modifyNeedsFullCharge = tag.getBoolean("modifyNeedsFullCharge");
        if (tag.contains("modifyIsOneTimeCharge", Tag.TAG_BYTE)) this.modifyIsOneTimeCharge = tag.getBoolean("modifyIsOneTimeCharge");
        readAmmoItems(tag);
        if (tag.contains("modifyAutoReloading", Tag.TAG_BYTE)) this.modifyAutoReloading = tag.getBoolean("modifyAutoReloading");
        if (tag.contains("modifyShouldRenderHud", Tag.TAG_BYTE)) this.modifyShouldRenderHud = tag.getBoolean("modifyShouldRenderHud");
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
        this.modifyFuel.forEach(type -> list.add(StringTag.valueOf(type.toString())));
        tag.put("modifyFuel", list);
    }

    private void readFuelTypes(CompoundTag tag) {
        if (tag.contains("modifyFuel", Tag.TAG_LIST)) {
            this.modifyFuel.clear();
            tag.getList("modifyFuel", Tag.TAG_STRING).forEach(t ->
                    this.modifyFuel.add(FuelType.getType(t.getAsString()))
            );
        }
    }

    private void writeFuelMax(CompoundTag tag) {
        CompoundTag fuelTag = new CompoundTag();
        this.modifyMaxFuel.forEach((key, value) -> fuelTag.putInt(key.toString(), value));
        tag.put("modifyMaxFuel", fuelTag);
    }

    // В методе deserializeNBT
    private void readFuelMax(CompoundTag tag) {
        if (tag.contains("modifyMaxFuel", Tag.TAG_COMPOUND)) {
            CompoundTag fuelTag = tag.getCompound("modifyMaxFuel");
            fuelTag.getAllKeys().forEach(key ->
                    this.modifyMaxFuel.put(
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
        copy.modifyDamage = this.modifyDamage;
        copy.modifyProjectileSpeed = this.modifyProjectileSpeed;
        copy.modifyProjectileSpread = this.modifyProjectileSpread;
        copy.additionalProjectileGravity = this.additionalProjectileGravity;
        copy.modifyProjectileGravity = this.modifyProjectileGravity;
        copy.modifyProjectileLife = this.modifyProjectileLife;
        copy.recoilModifier = this.recoilModifier;
        copy.kickModifier = this.kickModifier;
        copy.modifyMuzzleFlashSize = this.modifyMuzzleFlashSize;
        copy.modifyMuzzleFlashScale = this.modifyMuzzleFlashScale;
        copy.modifyAimDownSightSpeed = this.modifyAimDownSightSpeed;
        copy.modifyFireRate = this.modifyFireRate;
        copy.criticalChance = this.criticalChance;
        copy.modifyMaxAmmo = this.modifyMaxAmmo;
        copy.modifyProjectileAmount = this.modifyProjectileAmount;
        copy.modifyFireDelay = this.modifyFireDelay;
        copy.modifyReloadStart = this.modifyReloadStart;
        copy.modifyReloadTime = this.modifyReloadTime;
        copy.modifyReloadEnd = this.modifyReloadEnd;
        copy.modifyEquipTime = this.modifyEquipTime;
        copy.modifyAmmoPerShot = this.modifyAmmoPerShot;
        copy.modifyMaxFuel = new HashMap<>(this.modifyMaxFuel);

        // Collections and enums
        copy.fireModes = new HashSet<>(this.fireModes);
        copy.gripType = this.gripType;
        copy.modifyNeedsFullCharge = this.modifyNeedsFullCharge;
        copy.modifyIsOneTimeCharge = this.modifyIsOneTimeCharge;
        copy.ammoItems = new HashSet<>(this.ammoItems);
        copy.modifyAutoReloading = this.modifyAutoReloading;
        copy.modifyShouldRenderHud = this.modifyShouldRenderHud;
        copy.loadingType = this.loadingType;
        copy.modifyFuel = new HashSet<>(this.modifyFuel);

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
        json.addProperty("modifyDamage", this.modifyDamage);
        json.addProperty("modifyProjectileSpeed", this.modifyProjectileSpeed);
        json.addProperty("modifyProjectileSpread", this.modifyProjectileSpread);
        json.addProperty("additionalProjectileGravity", this.additionalProjectileGravity);
        json.addProperty("modifyProjectileGravity", this.modifyProjectileGravity);
        json.addProperty("modifyProjectileLife", this.modifyProjectileLife);
        json.addProperty("recoilModifier", this.recoilModifier);
        json.addProperty("kickModifier", this.kickModifier);
        json.addProperty("modifyMuzzleFlashSize", this.modifyMuzzleFlashSize);
        json.addProperty("modifyMuzzleFlashScale", this.modifyMuzzleFlashScale);
        json.addProperty("modifyAimDownSightSpeed", this.modifyAimDownSightSpeed);
        json.addProperty("modifyFireRate", this.modifyFireRate);
        json.addProperty("criticalChance", this.criticalChance);
        json.addProperty("modifyMaxAmmo", this.modifyMaxAmmo);
        json.addProperty("modifyProjectileAmount", this.modifyProjectileAmount);
        json.addProperty("modifyFireDelay", this.modifyFireDelay);
        json.addProperty("modifyReloadStart", this.modifyReloadStart);
        json.addProperty("modifyReloadTime", this.modifyReloadTime);
        json.addProperty("modifyReloadEnd", this.modifyReloadEnd);
        json.addProperty("modifyEquipTime", this.modifyEquipTime);
        json.addProperty("modifyAmmoPerShot", this.modifyAmmoPerShot);
        JsonObject fuelJson = new JsonObject();
        this.modifyMaxFuel.forEach((key, value) ->
                fuelJson.addProperty(key.toString(), value)
        );

        json.add("modifyMaxFuel", fuelJson);
        var fireModesArray = new JsonArray();
        this.fireModes.forEach(mode -> fireModesArray.add(mode.toString()));
        json.add("fireModes", fireModesArray);

        return json;
    }

    // Calculation methods for each numeric property
    public float additionalDamage(GunData gunData) {
        return  parseOperand(additionalDamage);
    }

    public float modifyDamage(float damage, GunData gunData) {
        return calculate(damage, modifyDamage);
    }

    public double modifyProjectileSpeed(double speed, GunData gunData) {
        return calculate((float) speed, modifyProjectileSpeed);
    }

    public float modifyProjectileSpread(float spread, GunData gunData) {
        return calculate(spread, modifyProjectileSpread);
    }

    public double getAdditionalProjectileGravity(double base, GunData gunData) {
        return calculate((float) base, additionalProjectileGravity);
    }

    public double modifyProjectileGravity(double gravity, GunData gunData) {
        return calculate((float) gravity, modifyProjectileGravity);
    }

    public int modifyProjectileLife(int life, GunData gunData) {
        return (int) calculate(life, modifyProjectileLife);
    }

    public float recoilModifier(GunData gunData) {
        return recoilModifier;
    }

    public float kickModifier(GunData gunData) {
        return kickModifier;
    }

    public double modifyMuzzleFlashSize(double size, GunData gunData) {
        return calculate((float) size, modifyMuzzleFlashSize);
    }

    public double modifyMuzzleFlashScale(double scale, GunData gunData) {
        return calculate((float) scale, modifyMuzzleFlashScale);
    }

    public double modifyAimDownSightSpeed(double speed, GunData gunData) {
        return calculate((float) speed, modifyAimDownSightSpeed);
    }

    public int modifyFireRate(int rate, GunData gunData) {
        return (int) calculate(rate, modifyFireRate);
    }

    public float criticalChance(GunData gunData) {
        return criticalChance;
    }

    public int modifyMaxAmmo(int maxAmmo, GunData gunData) {
        return (int) calculate(maxAmmo, modifyMaxAmmo);
    }

    public int modifyProjectileAmount(int amount, GunData gunData) {
        return (int) calculate(amount, modifyProjectileAmount);
    }

    public int modifyFireDelay(int delay, GunData gunData) {
        return (int) calculate(delay, modifyFireDelay);
    }

    public int modifyReloadStart(int time, GunData gunData) {
        return (int) calculate(time, modifyReloadStart);
    }

    public int modifyReloadTime(int time, GunData gunData) {
        return (int) calculate(time, modifyReloadTime);
    }

    public int modifyReloadEnd(int time, GunData gunData) {
        return (int) calculate(time, modifyReloadEnd);
    }

    public int modifyEquipTime(int time, GunData gunData) {
        return (int) calculate(time, modifyEquipTime);
    }

    public int modifyAmmoPerShot(int ammo, GunData gunData) {
        return (int) calculate(ammo, modifyAmmoPerShot);
    }

    public int modifyMaxFuel(int max, FuelType type, GunData gunData) {
        return modifyMaxFuel.get(type);
    }

    public static float calculate(float num, String operation) {
        if (operation == null || operation.isEmpty()) {
            var throwable = new IllegalArgumentException("Operation string cannot be null or empty");
            Ntgl.LOGGER.error("Operation string cannot be null or empty", throwable);
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
                Ntgl.LOGGER.error("Division by zero", new IllegalArgumentException("Unsupported operator: " + operator));
                yield num / operand;
            }
        };
    }
}
