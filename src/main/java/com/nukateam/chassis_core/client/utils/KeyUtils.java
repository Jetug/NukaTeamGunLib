package com.nukateam.chassis_core.client.utils;

import com.nukateam.chassis_core.common.input.InputKey;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.Lazy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class KeyUtils {
    @OnlyIn(Dist.CLIENT)
    public static Lazy<Map<Integer, InputKey>> keyMap = Lazy.of(() -> {
        var map = new HashMap<Integer, InputKey>();
        var options = Minecraft.getInstance().options;

        map.put(options.keyUp.getKey().getValue()       , InputKey.UP       );
        map.put(options.keyDown.getKey().getValue()     , InputKey.DOWN     );
        map.put(options.keyLeft.getKey().getValue()     , InputKey.LEFT     );
        map.put(options.keyRight.getKey().getValue()    , InputKey.RIGHT    );
        map.put(options.keyJump.getKey().getValue()     , InputKey.JUMP     );
        map.put(NtglKeyBinds.LEAVE.getKey().getValue()   , InputKey.LEAVE    );
        map.put(options.keyUse.getKey().getValue()      , InputKey.USE      );
        map.put(options.keyAttack.getKey().getValue()   , InputKey.ATTACK   );

        return map;
    });

    @OnlyIn(Dist.CLIENT)
    public static InputKey getByKey(int key) {
        return keyMap.get().get(key);
    }
}
