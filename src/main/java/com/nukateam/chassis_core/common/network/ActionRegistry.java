package com.nukateam.chassis_core.common.network;

import com.nukateam.ntgl.common.network.message.chassis.actions.Action;
import com.nukateam.ntgl.common.network.message.chassis.actions.InputAction;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class ActionRegistry {
    private static int currentId = 0;
    private static Map<Integer, Action> actions = new HashMap<>();
    private static Map<Class, Integer> actionsId = new HashMap<>();

    static {
        addAction(new InputAction());
    }

    public static Action getAction(int id) {
        return actions.get(id);
    }

    public static int getActionId(Class type) {
        return actionsId.get(type);
    }

    public static <T extends Action> void addAction(T action) {
        var id = currentId++;
        actions.put(id, action);
        actionsId.put(action.getClass(), id);
    }

}
