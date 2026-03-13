package com.nukateam.ntgl.modules.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IMessage<T>
{
    void encode(T message, FriendlyByteBuf buffer);

    T decode(FriendlyByteBuf buffer);

    void handle(T message, NetworkEvent.Context context);
}