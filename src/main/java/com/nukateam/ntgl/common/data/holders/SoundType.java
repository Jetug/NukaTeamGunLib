//package com.nukateam.ntgl.common.data.holders;
//
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class SoundType extends ResourceHolder {
//    public static SoundType FIRE            = new SoundType("fire");
//    public static SoundType SILENCED_FIRE   = new SoundType("silencedFire");
//    public static SoundType ENCHANTED_FIRE  = new SoundType("enchantedFire");
//    public static SoundType PRE_FIRE        = new SoundType("preFire");
//    public static SoundType RELOAD          = new SoundType("reload");
//    public static SoundType COCK            = new SoundType("cock");
//
//    private static final Map<String, SoundType> typeMap = new HashMap<>();
//    protected final String id;
//
//    static {
//        registerType(FIRE);
//        registerType(SILENCED_FIRE );
//        registerType(ENCHANTED_FIRE);
//        registerType(PRE_FIRE      );
//        registerType(RELOAD        );
//        registerType(COCK          );
//    }
//
//    public SoundType(ResourceLocation id) {
//        super(id);
//    }
//
//    public SoundType(String name) {
//        super(name);
//    }
//
//    public static void registerType(SoundType mode) {
//        typeMap.putIfAbsent(mode.getId(), mode);
//    }
//
//    public static SoundType getType(ResourceLocation id) {
//        return typeMap.getOrDefault(id, createDefault(id));
//    }
//
//    public static SoundType getType(String path) {
//        var id = ResourceLocation.tryParse(path);
//        return getType(id);
//    }
//
//    private static SoundType createDefault(ResourceLocation id){
//        var type = new SoundType(id);
//        registerType(type);
//        return type;
//    }
//
//    public ResourceLocation getId() {
//        return this.id;
//    }
//
//    public boolean equals(ResourceLocation obj) {
//        return this.id.equals(obj);
//    }
//
//    @Override
//    public int hashCode() {
//        return id.hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return id.toString();
//    }
//}
