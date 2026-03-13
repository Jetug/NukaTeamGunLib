package com.nukateam.ntgl.modules.data;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.modules.data.message.C2SMessageUpdateEntityData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class DataKeyManager {
    private static DataKeyManager instance = null;
    private final HashMap<Integer, DataKey> dataKeys = new HashMap<>();
    private int id = 0;

    private DataKeyManager(){}

    public static DataKeyManager getInstance(){
        if(instance == null)
            instance = new DataKeyManager();
        return instance;
    }

    public void registerKey(DataKey dataKey){
        dataKeys.put(id++, dataKey);
    }

    public void setData(Boolean data, int dataKeyId, int entityId){
        if(dataKeys.containsKey(dataKeyId)) {
            dataKeys.get(dataKeyId).setValue(entityId, data);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            var entries = new HashMap<DataEntry, C2SMessageUpdateEntityData.EntityData>();

            DataKeyManager.getInstance().dataKeys.forEach((dataKeyId, dataKey) -> {
                dataKey.getData().forEach((entityId, dataEntry) -> {
                    if (dataEntry.isPendingSync()) {
                        dataEntry.setPendingSync(false);
                        entries.put(dataEntry, new C2SMessageUpdateEntityData.EntityData(entityId, dataKeyId));
                    }
                });
            });

            if (!entries.isEmpty()) {
                PacketHandler.getPlayChannel().sendToAll(new C2SMessageUpdateEntityData());
            }
        }
    }
}
