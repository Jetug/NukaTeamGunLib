package com.nukateam.chassis_core.client.utils;

import com.nukateam.chassis_core.common.input.InputKey;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.chassis_core.client.ClientConfig.OPTIONS;

@OnlyIn(Dist.CLIENT)
public class KeyUtils {
    @OnlyIn(Dist.CLIENT)
    public static Lazy<Map<Integer, InputKey>> keyMap = Lazy.of(() -> {
        var map = new HashMap<Integer, InputKey>();

        map.put(OPTIONS.keyUp.getKey().getValue()       , InputKey.UP       );
        map.put(OPTIONS.keyDown.getKey().getValue()     , InputKey.DOWN     );
        map.put(OPTIONS.keyLeft.getKey().getValue()     , InputKey.LEFT     );
        map.put(OPTIONS.keyRight.getKey().getValue()    , InputKey.RIGHT    );
        map.put(OPTIONS.keyJump.getKey().getValue()     , InputKey.JUMP     );
        map.put(NtglKeyBinds.LEAVE.getKey().getValue()   , InputKey.LEAVE    );
        map.put(OPTIONS.keyUse.getKey().getValue()      , InputKey.USE      );
        map.put(OPTIONS.keyAttack.getKey().getValue()   , InputKey.ATTACK   );

        return map;
    });

    @OnlyIn(Dist.CLIENT)
    public static InputKey getByKey(int key) {
        return keyMap.get().get(key);
    }
}
