package com.nukateam.chassis_core.common.network.actions;

import com.nukateam.chassis_core.common.network.ActionRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public abstract class Action<T extends Action<T>> {
    public int getId() {
        return ActionRegistry.getActionId(this.getClass());
    }

    public String getName() {
        return this.getClass().getCanonicalName();
    }

    public void doServerAction(T message, NetworkEvent.Context context, int entityId) {
    }

    public void doClientAction(T message, NetworkEvent.Context context, int entityId) {
    }

    public abstract void write(FriendlyByteBuf buffer);

    public abstract T read(FriendlyByteBuf buffer);
}
