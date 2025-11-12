package com.nukateam.chassis_core.common.data.json;

import com.nukateam.chassis_core.common.data.json.EquipmentAttachment;
import com.nukateam.chassis_core.common.data.json.ModelConfigBase;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class EquipmentConfig extends ModelConfigBase {
    public String parent;
    public ResourceLocation model;
    public HashMap<String, ResourceLocation> texture;
    public int[] uv;
    public String part;
    public String[] hide = new String[0];
    public EquipmentAttachment[] attachments;
    public String[] mods = new String[0];

    @Nullable
    public Collection<String> getArmorBone(String chassisBone) {
        var result = new ArrayList<String>();
        for (var attachment : attachments) {
            if (Objects.equals(attachment.frame, chassisBone))
                result.add(attachment.armor);
        }
        return result;
    }

    public void onFinishLoading(){
    }

    public Collection<String> getAllVariants() {
        return texture.keySet();
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getTexture(String tag) {
        return texture.get(tag);
    }
}
