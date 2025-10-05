package com.nukateam.chassis_core.common.network.packet;

import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;

import static com.nukateam.chassis_core.common.util.helpers.PlayerUtils.isWearingChassis;

@SuppressWarnings("ConstantConditions")
public class C2SActionPacket extends PlayMessage<C2SActionPacket> {
    ActionType action = null;

    public C2SActionPacket(ActionType action) {
        this.action = action;
    }

    public C2SActionPacket() {}

    @Override
    public void encode(C2SActionPacket actionPacket, FriendlyByteBuf buffer) {
        buffer.writeByte(actionPacket.action.getId());
    }

    @Override
    public C2SActionPacket decode(FriendlyByteBuf buffer) {
        var action = ActionType.getById(buffer.readByte());
        return new C2SActionPacket(action);
    }

    @Override
    public void handle(C2SActionPacket message, MessageContext supplier) {
        supplier.execute((() ->
        {
            var player = supplier.getPlayer();
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