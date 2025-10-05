
package com.nukateam.chassis_core.common.data.holders;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ChassisPart extends ResourceHolder {
    public static final ChassisPart HELMET          = new ChassisPart("head"      );
    public static final ChassisPart BODY_ARMOR      = new ChassisPart("body"      );
    public static final ChassisPart LEFT_ARM_ARMOR  = new ChassisPart("left_arm"  );
    public static final ChassisPart RIGHT_ARM_ARMOR = new ChassisPart("right_arm" );
    public static final ChassisPart LEFT_LEG_ARMOR  = new ChassisPart("left_leg"  ); //5
    public static final ChassisPart RIGHT_LEG_ARMOR = new ChassisPart("right_leg" );
    public static final ChassisPart ENGINE          = new ChassisPart("engine");
    public static final ChassisPart BACK            = new ChassisPart("back");
    public static final ChassisPart COOLING         = new ChassisPart("cooling");
    public static final ChassisPart BODY_FRAME      = new ChassisPart("body_frame"); //10
    public static final ChassisPart LEFT_ARM_FRAME  = new ChassisPart("left_arm_frame");
    public static final ChassisPart RIGHT_ARM_FRAME = new ChassisPart("right_arm_frame");
    public static final ChassisPart LEFT_LEG_FRAME  = new ChassisPart("left_leg_frame");
    public static final ChassisPart RIGHT_LEG_FRAME = new ChassisPart("right_leg_frame");
    public static final ChassisPart LEFT_HAND       = new ChassisPart("left_hand");
    public static final ChassisPart RIGHT_HAND      = new ChassisPart("right_hand"); //16

    private static final Map<ResourceLocation, ChassisPart> typeMap = new HashMap<>();
    
    static {
        registerType(HELMET         );
        registerType(BODY_ARMOR     );
        registerType(LEFT_ARM_ARMOR );
        registerType(RIGHT_ARM_ARMOR);
        registerType(LEFT_LEG_ARMOR );
        registerType(RIGHT_LEG_ARMOR);
        registerType(ENGINE         );
        registerType(BACK           );
        registerType(COOLING        );
        registerType(BODY_FRAME     );
        registerType(LEFT_ARM_FRAME );
        registerType(RIGHT_ARM_FRAME);
        registerType(LEFT_LEG_FRAME );
        registerType(RIGHT_LEG_FRAME);
        registerType(LEFT_HAND      );
        registerType(RIGHT_HAND     );
    }

    public ChassisPart(ResourceLocation id) {
        super(id);
    }

    public ChassisPart(String name) {
        super(name);
    }

    public static void registerType(ChassisPart mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static ChassisPart getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, HELMET);
    }

    public static ChassisPart getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }
}
