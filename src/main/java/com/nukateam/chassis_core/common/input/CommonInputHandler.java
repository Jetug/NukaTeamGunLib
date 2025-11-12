package com.nukateam.chassis_core.common.input;

import com.nukateam.chassis_core.common.events.CommonInputEvent;
import com.nukateam.chassis_core.common.input.InputKey;
import com.nukateam.chassis_core.common.input.KeyAction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import static com.nukateam.chassis_core.common.input.InputKey.JUMP;
import static com.nukateam.chassis_core.common.input.KeyAction.PRESS;
import static com.nukateam.chassis_core.common.input.KeyAction.REPEAT;
import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.getEntityChassis;
import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.isWearingChassis;

@SuppressWarnings("ConstantConditions")
public class CommonInputHandler {
    public static void onKeyInput(com.nukateam.chassis_core.common.input.InputKey key, KeyAction action, Player player) {
        MinecraftForge.EVENT_BUS.post(new CommonInputEvent(key, action, player));

        if (!isWearingChassis(player) || key == null) return;

        if ((action == PRESS || action == REPEAT) && key == JUMP)
            getEntityChassis(player).jump();

        switch (action) {
            case PRESS -> onPress(key, player);
            case RELEASE -> onRelease(key, player);
            case DOUBLE_CLICK -> onDoubleClick(key, player);
            case LONG_PRESS -> onLongPress(key, player);
        }
    }

    public static void onPress(com.nukateam.chassis_core.common.input.InputKey key, Player player) {
//        if (key == InputKey.LEAVE)
//            stopWearingArmor(player);
    }

    public static void onRelease(com.nukateam.chassis_core.common.input.InputKey key, Player player) {
        if (!isWearingChassis(player)) return;
//        if(key == ATTACK) getLocalPlayerChassis(player).powerPunch();
//        if(key == USE) getLocalPlayerChassis(player).resetAttackCharge();
    }

    public static void onDoubleClick(com.nukateam.chassis_core.common.input.InputKey key, Player player) {
        if (!isWearingChassis(player)) return;

//        DashDirection direction = switch (key){
//            case UP    -> DashDirection.FORWARD;
//            case DOWN  -> DashDirection.BACK;
//            case LEFT  -> DashDirection.LEFT;
//            case RIGHT -> DashDirection.RIGHT;
//            case JUMP  -> DashDirection.UP;
//            default -> null;
//        };
//
//        if(direction == null) return;
//        getLocalPlayerChassis(player).dash(direction);
    }

    public static void onLongPress(InputKey key, Player player) {
//        if (!isLocalWearingChassis(player)) return;
//        var bool = key == USE;
//        if(bool){
//            getLocalPlayerChassis(player).addAttackCharge();
//        }
    }
}
