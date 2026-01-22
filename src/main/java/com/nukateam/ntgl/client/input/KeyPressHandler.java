package com.nukateam.ntgl.client.input;

import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.modules.wheel.ActionWheel;
import com.nukateam.ntgl.modules.wheel.ActionWheelManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.util.handler.ClientShootingHandler.isInGame;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyPressHandler {
    private static final ArrayList<KeyCommand> commands = new ArrayList<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        var player = Minecraft.getInstance().player;
        if (event.phase == TickEvent.Phase.END && isInGame() && player != null) {
            for (var command : commands){
                if (command.getKey().isDown()){
                    if(!command.keyPressed) {
                        command.keyPressed = true;
                        command.onPress();
                    }
                }
                else {
                    if (command.keyPressed){
                        command.keyPressed = false;
                        command.onRelease();
                    }
                }
            }
        }
    }

    public static void addCommand(KeyCommand command){
        commands.add(command);
    }
}
