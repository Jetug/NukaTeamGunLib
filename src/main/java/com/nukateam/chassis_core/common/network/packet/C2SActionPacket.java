package com.nukateam.chassis_core.common.network.packet;

import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.isWearingChassis;

@SuppressWarnings("ConstantConditions")
public class C2SActionPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SActionPacket> STREAM_CODEC = StreamCodec.of(
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

    public static void handle(C2SActionPacket message, MessageContext supplier) {
        supplier.execute((() ->
        {
            var player = supplier.getPlayer().get();
            if (!isWearingChassis(player)) return;
            var armor = (WearableChassis) player.getVehicle();

            switch (message.action) {
                case DISMOUNT -> armor.exitArmor();
                case OPEN_GUI -> armor.openGUI(player);
            }

        }));
        supplier.setHandled(true);


    }
}