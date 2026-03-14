package com.nukateam.ntgl.modules.data;

import net.minecraft.world.entity.LivingEntity;
import java.util.HashMap;

public class DataKey {
    private final HashMap<Integer, DataEntry> data = new HashMap<>();

    private boolean defaultValue;

    public DataKey(boolean defaultValue){
        this.defaultValue = defaultValue;
    }

    public boolean getValue(LivingEntity entity){
        var entry = data.get(entity.getId());
        if (entry != null) {
            return entry.getValue();
        }
        return defaultValue;
    }

    public void setValue(LivingEntity entity, boolean value) {
        setValue(entity.getId(), value);
    }

    public void setValue(int id, boolean value) {
        if(data.containsKey(id)){
            data.get(id).setValue(value);
        }
        else {
            var dataEntry = new DataEntry();
            dataEntry.setValue(value);
            data.put(id, dataEntry);
        }
    }

    public HashMap<Integer, DataEntry> getData() {
        return data;
    }
}
