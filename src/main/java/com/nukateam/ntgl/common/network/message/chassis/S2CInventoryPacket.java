package com.nukateam.ntgl.common.network.message.chassis;

import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class S2CInventoryPacket implements CustomPacketPayload {
    public static final Type<S2CInventoryPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_inventory_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CInventoryPacket> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    public int chassisId = -1;
    public ListTag inventory;
    public static final String INVENTORY = "inventory";

    public S2CInventoryPacket() {}

    public S2CInventoryPacket(int chassisId, ListTag inventory) {
        this.chassisId = chassisId;
        this.inventory = inventory;
    }

    public static void encode(S2CInventoryPacket message, FriendlyByteBuf buffer) {
        var nbt = new CompoundTag();
        nbt.put(INVENTORY, message.inventory);
        buffer.writeInt(message.chassisId);
        buffer.writeNbt(nbt);
    }

    public static S2CInventoryPacket decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var inventory = (ListTag) buffer.readNbt().get(INVENTORY);
        return new S2CInventoryPacket(entityId, inventory);
    }

    public static void handle(S2CInventoryPacket message, IPayloadContext supplier) {
        supplier.enqueueWork((() ->
        {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var entity = player.level().getEntity(message.chassisId);

                if (entity instanceof WearableChassis powerArmor)
                    powerArmor.setArmorData(message.inventory);
            }
        }));

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}