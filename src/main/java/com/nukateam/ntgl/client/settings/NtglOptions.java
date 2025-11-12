package com.nukateam.ntgl.client.settings;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Author: MrCrayfish
 */
public class NtglOptions {
    private static final String ADS_SENSITIVITY = "adsSensitivity";
    private static final String GUN_VOLUME = "gunVolume";
    private static final String SHOW_TIPS = "showTips";
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static NtglOptions instance = null;
    private final File optionsFile;
    private double adsSensitivity = 0.75;
    private double gunVolume = 1.0;
    private boolean showTips = true;

    public static NtglOptions getInstance(){
        if(instance == null)
            instance = new NtglOptions(Minecraft.getInstance().gameDirectory);
        return instance;
    }

    protected NtglOptions(File dataDir) {
        this.optionsFile = new File(dataDir, "cgs-options.txt");
        this.loadOptions();
    }

    public double getAdsSensitivity() {
        return this.adsSensitivity;
    }

    public double getGunVolume() {
        return this.gunVolume;
    }

    public boolean isShowTips() {
        return showTips;
    }

    public void setShowTips(boolean showTips) {
        this.showTips = showTips;
    }

    public void setAdsSensitivity(double adsSensitivity) {
        this.adsSensitivity = adsSensitivity;
    }

    public void setGunVolume(double gunVolume) {
        this.gunVolume = gunVolume;
    }

    public void saveOptions() {
        try (var writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
            writer.println(ADS_SENSITIVITY + ":" + this.adsSensitivity);
            writer.println(GUN_VOLUME + ":" + this.gunVolume);
            writer.println(SHOW_TIPS + ":" + this.showTips);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadOptions() {
        try {
            if (!this.optionsFile.exists())
                return;

            var lines = IOUtils.readLines(new FileInputStream(this.optionsFile), Charsets.UTF_8);
            var compound = new CompoundTag();

            for (var line : lines) {
                try {
                    var iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(line).iterator();
                    compound.putString(iterator.next(), iterator.next());
                } catch (Exception var10) {
                    Ntgl.LOGGER.warn("Skipping bad option: {}", line);
                }
            }

            for (var key : compound.getAllKeys()) {
                var value = compound.getString(key);
                try {
                    readOption(key, value);
                } catch (Exception e) {
                    Ntgl.LOGGER.warn("Skipping bad option: {}:{}", key, value);
                }
            }
        } catch (Exception e) {
            Ntgl.LOGGER.error("Failed to load options", e);
        }
    }

    private void readOption(String key, String value) {
        switch (key) {
            case ADS_SENSITIVITY:
                this.adsSensitivity = Double.parseDouble(value);
                break;
            case GUN_VOLUME:
                this.gunVolume = Double.parseDouble(value);
                break;
            case SHOW_TIPS:
                this.showTips = Boolean.parseBoolean(value);
                break;
        }
    }
}