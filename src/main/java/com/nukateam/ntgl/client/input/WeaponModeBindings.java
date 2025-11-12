package com.nukateam.ntgl.client.input;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class WeaponModeBindings {
    private static Map<WeaponMode, KeyMapping> keys = Map.of(
            WeaponMode.PRIMARY, getAttackKey(),
            WeaponMode.SECONDARY, getKeyUseKey(),
            WeaponMode.ADDITIONAL, NtglKeyBinds.KEY_ADD_ATTACK,
            WeaponMode.ALTERNATIVE, NtglKeyBinds.KEY_ALT_ATTACK
    );

    public static KeyMapping getKey(WeaponMode mode){
        return keys.get(mode);
    }

    private static KeyMapping getAttackKey() {
        return Minecraft.getInstance().options.keyAttack;
    }

    private static KeyMapping getKeyUseKey() {
        return Minecraft.getInstance().options.keyUse;
    }
}
