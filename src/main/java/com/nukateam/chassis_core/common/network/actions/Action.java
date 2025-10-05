package com.nukateam.chassis_core.common.network.actions;

import com.nukateam.chassis_core.common.network.ActionRegistry;
import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.FriendlyByteBuf;

public abstract class Action<T extends Action<T>> {
    private int id = -1;

    public static Action<?> getClassByName(String name) {
        try {
            var act = Class.forName(name);
            var o = (Object) act;
            var action = (Action<?>) o;

            return action;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return ActionRegistry.getActionId(this.getClass());
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.getClass().getCanonicalName();
    }

    public void doServerAction(T message, MessageContext context, int entityId) {
    }

    public void doClientAction(T message, MessageContext context, int entityId) {
    }

    public abstract void write(FriendlyByteBuf buffer);

    public abstract T read(FriendlyByteBuf buffer);


}
