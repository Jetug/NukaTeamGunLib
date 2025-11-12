package com.nukateam.chassis_core.common.data.enums;

public enum ActionType {
    DISMOUNT,
    OPEN_GUI,
    ADD_ATTACK_CHARGE,
    RESET_ATTACK_CHARGE;

    private static final ActionType[] values = ActionType.values();

    public static ActionType getById(int id) {
        return values[id];
    }

    public int getId() {
        return ordinal();
    }
}
