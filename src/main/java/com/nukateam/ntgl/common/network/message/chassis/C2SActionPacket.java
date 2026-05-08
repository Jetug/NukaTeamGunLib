package com.nukateam.ntgl.common.network.message.chassis;

import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.isWearingChassis;

@SuppressWarnings("ConstantConditions")
public class C2SActionPacket implements CustomPacketPayload {
    public static final Type<C2SActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_action_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SActionPacket> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    ActionType action = null;

    public C2SActionPacket(ActionType action) {
        this.action = action;
    }

    public C2SActionPacket() {}

    public static void encode(C2SActionPacket actionPacket, FriendlyByteBuf buffer) {
        buffer.writeByte(actionPacket.action.getId());
    }

    public static C2SActionPacket decode(FriendlyByteBuf buffer) {
        var action = ActionType.getById(buffer.readByte());
        return new C2SActionPacket(action);
    }

    public static void handle(C2SActionPacket message, IPayloadContext supplier) {
        supplier.enqueueWork((() ->
        {
            var player = supplier.player();
            if (!isWearingChassis(player)) return;
            var armor = (WearableChassis) player.getVehicle();

            switch (message.action) {
                case DISMOUNT -> armor.exitArmor();
                case OPEN_GUI -> armor.openGUI(player);
            }
        }));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}