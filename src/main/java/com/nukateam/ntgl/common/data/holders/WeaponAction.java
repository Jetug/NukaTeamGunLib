package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponAction extends ResourceHolder {
    public static final WeaponAction NONE = new WeaponAction(ResourceLocation.tryBuild(Ntgl.MOD_ID, "none"));
    public static final WeaponAction SHOT = new WeaponAction(ResourceLocation.tryBuild(Ntgl.MOD_ID, "shot"));
    public static final WeaponAction MELEE = new WeaponAction(ResourceLocation.tryBuild(Ntgl.MOD_ID, "melee"));
    public static final WeaponAction THROW = new WeaponAction(ResourceLocation.tryBuild(Ntgl.MOD_ID, "throw"));
    public static final WeaponAction SCOPE = new WeaponAction(ResourceLocation.tryBuild(Ntgl.MOD_ID, "scope"));
    public static final WeaponAction CUSTOM = new WeaponAction(ResourceLocation.tryBuild(Ntgl.MOD_ID, "custom"));

    private static final Map<ResourceLocation, WeaponAction> loadingTypeMap = new HashMap<>();

    static {
        registerType(NONE);
        registerType(SHOT);
        registerType(MELEE);
        registerType(THROW);
        registerType(SCOPE);
        registerType(CUSTOM);
    }

    public WeaponAction(ResourceLocation id) {
        super(id);
    }

    public ResourceLocation getIcon() {
        assert id != null;
        return ResourceLocation.tryBuild(id.getNamespace(), "textures/hud/weapon_action/" + id.getPath() + ".png");
    }

    public static void registerType(WeaponAction mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static WeaponAction getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, SHOT);
    }

    public static WeaponAction getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
