package com.nukateam.chassis_core.common.config;

import com.nukateam.chassis_core.common.data.enums.AttachmentMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class EquipmentAttachment implements INBTSerializable<CompoundTag> {
    public AttachmentMode mode = AttachmentMode.ADD;
    public String frame;
    public String armor;

    public static EquipmentAttachment create(CompoundTag tag) {
        var config = new EquipmentAttachment();
        config.deserializeNBT(tag);
        return config;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt("mode", this.mode.ordinal());
        tag.putString("frame", frame);
        tag.putString("armor", armor);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        var modeId = tag.getInt("mode");
        if(modeId < AttachmentMode.values().length) {
            mode = AttachmentMode.values()[modeId];
        }
        frame = tag.getString("frame");
        armor = tag.getString("armor");
    }
}
