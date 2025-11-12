package com.nukateam.chassis_core.common.data.json;

import com.google.gson.annotations.SerializedName;

public enum AttachmentMode {
    @SerializedName("add")
    ADD(),
    @SerializedName("replace")
    REPLACE();
}