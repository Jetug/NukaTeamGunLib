package com.nukateam.ntgl.common.network.message.chassis.actions;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.chassis_core.common.network.ActionRegistry;
import net.minecraft.network.FriendlyByteBuf;

public abstract class Action<T extends Action<T>> {
    public int getId() {
        return ActionRegistry.getActionId(this.getClass());
    }

    public String getName() {
        return this.getClass().getCanonicalName();
    }

    public void doServerAction(T message, IPayloadContext context, int entityId) {
    }

    public void doClientAction(T message, IPayloadContext context, int entityId) {
    }

    public abstract void write(FriendlyByteBuf buffer);

    public abstract T read(FriendlyByteBuf buffer);
}
