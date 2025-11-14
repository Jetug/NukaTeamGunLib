package com.nukateam.ntgl.common.network.message.chassis.actions;

import com.nukateam.chassis_core.common.input.InputKey;
import com.nukateam.chassis_core.common.input.KeyAction;
import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.FriendlyByteBuf;

import static com.nukateam.chassis_core.common.input.CommonInputHandler.onKeyInput;

@SuppressWarnings("ConstantConditions")
public class InputAction extends Action<InputAction> {
    private InputKey key;
    private KeyAction action;

    public InputAction() {
    }

    public InputAction(InputKey key, KeyAction action) {
        this.key = key;
        this.action = action;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(key);
        buffer.writeEnum(action);

    }

    @Override
    public InputAction read(FriendlyByteBuf buffer) {
        return new InputAction(
                buffer.readEnum(InputKey.class),
                buffer.readEnum(KeyAction.class));
    }

    @Override
    public void doServerAction(InputAction message, MessageContext context, int entityId) {
        var player = context.getPlayer();
        onKeyInput(message.key, message.action, player);
    }
}
