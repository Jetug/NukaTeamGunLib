package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponMode extends ResourceHolder {
    private static final Map<ResourceLocation, WeaponMode> loadingTypeMap = new HashMap<>();

    public static final WeaponMode PRIMARY     = new WeaponMode("primary"    , getAttackKey());
    public static final WeaponMode SECONDARY   = new WeaponMode("secondary"  , getKeyUseKey());
    public static final WeaponMode ADDITIONAL  = new WeaponMode("additional" , NtglKeyBinds.KEY_ADD_ATTACK);
    public static final WeaponMode ALTERNATIVE = new WeaponMode("alternative", NtglKeyBinds.KEY_ADD_ATTACK);

    private final KeyMapping key;

    static {
        registerType(PRIMARY    );
        registerType(SECONDARY  );
        registerType(ADDITIONAL );
        registerType(ALTERNATIVE);
    }

    private WeaponMode(String id, KeyMapping key) {
        super(ResourceLocation.tryBuild(Ntgl.MOD_ID, id));
        this.key = key;
    }

    public WeaponMode(ResourceLocation id, KeyMapping key) {
        super(id);
        this.key = key;
    }

    public KeyMapping getKeyMapping() {
        return key;
    }

    public static void registerType(WeaponMode mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static WeaponMode getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, PRIMARY);
    }

    public static WeaponMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }

    private static KeyMapping getAttackKey() {
        return Minecraft.getInstance().options.keyAttack;
    }

    private static KeyMapping getKeyUseKey() {
        return Minecraft.getInstance().options.keyUse;
    }
}
