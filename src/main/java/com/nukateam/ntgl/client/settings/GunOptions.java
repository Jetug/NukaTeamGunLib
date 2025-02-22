package com.nukateam.ntgl.client.settings;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.nukateam.ntgl.Ntgl;
import mod.azure.azurelib.core.math.functions.limit.Min;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * Author: MrCrayfish
 */
public class GunOptions {
    private static GunOptions instance = null;
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private final File optionsFile;
    private double adsSensitivity = 0.75;
    private double gunVolume = 1.0;

    public static GunOptions getInstance(){
        if(instance == null)
            instance = new GunOptions(Minecraft.getInstance().gameDirectory);
        return instance;
    }

    protected GunOptions(File dataDir) {
        this.optionsFile = new File(dataDir, "cgs-options.txt");
        this.loadOptions();
    }

    /**
     * Gets the ads sensitivity
     */
    public double getAdsSensitivity() {
        return this.adsSensitivity;
    }

    /**
     * Gets the volume gun sounds
     */
    public double getGunVolume() {
        return this.gunVolume;
    }

    public void setAdsSensitivity(double adsSensitivity) {
        this.adsSensitivity = adsSensitivity;
    }

    public void setGunVolume(double gunVolume) {
        this.gunVolume = gunVolume;
    }

    public void saveOptions() {
        try (var writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
            writer.println("adsSensitivity:" + this.adsSensitivity);
            writer.println("gunVolume:" + this.gunVolume);
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
            case "adsSensitivity":
                this.adsSensitivity = Double.parseDouble(value);
                break;
            case "gunVolume":
                this.gunVolume = Double.parseDouble(value);
                break;
        }
    }
}