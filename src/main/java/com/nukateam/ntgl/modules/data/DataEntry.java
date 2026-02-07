package com.nukateam.ntgl.modules.data;

public class DataEntry {
    private boolean value;
    private boolean pendingSync;

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
        pendingSync = true;
    }

    public boolean isPendingSync() {
        return pendingSync;
    }

    public void setPendingSync(boolean pendingSync) {
        this.pendingSync = pendingSync;
    }
}
