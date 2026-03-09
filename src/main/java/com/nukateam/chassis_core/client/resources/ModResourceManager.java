package com.nukateam.chassis_core.client.resources;

import com.nukateam.chassis_core.common.data.json.EquipmentConfig;
import com.nukateam.chassis_core.common.data.json.ModelConfigBase;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import static com.nukateam.chassis_core.client.render.utils.ResourceHelper.getResourceName;
import static com.nukateam.ntgl.common.data.json.JsonDeserializers.GSON_INSTANCE;

public class ModResourceManager {
    private static final String CONFIG_DIR = "config/model/";
    private static final String EQUIPMENT_DIR = CONFIG_DIR + "equipment";
    private static final String FRAME_DIR = CONFIG_DIR + "chassis";
    private static final String ITEM_DIR = CONFIG_DIR + "item";

    private final Map<String, EquipmentConfig> equipmentConfig = new HashMap<>();

    private static boolean isEmptyArray(Object obj) {
        if (obj.getClass().isArray())
            return 0 == Array.getLength(obj);
        return false;
    }

    public static boolean isNotEmpty(String string) {
        return string != null && !string.equals("");
    }

    private static  Map<ResourceLocation, Resource> getJsonResources(String path) {
        return Minecraft.getInstance().getResourceManager()
                .listResources(path, fileName -> fileName.getPath().endsWith(".json"));
    }

    private static BufferedReader getBufferedReader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream));
    }

    @Nullable
    public EquipmentConfig getEquipmentConfig(String itemId) {
        return equipmentConfig.get(itemId);
    }

    public void loadConfigs() {
        loadEquipment();
    }

    private void loadEquipment() {
        for (var file : getJsonResources(EQUIPMENT_DIR).keySet()) {
            var config = getConfig(file, EquipmentConfig.class);
            if (config == null) continue;

            if (isNotEmpty(config.parent)) {
                var parent = getConfig(ResourceLocation.tryParse(config.parent), EquipmentConfig.class);

                try {
                    var fields = config.getClass().getFields();
                    for (var field : fields) {
                        var obj = field.get(config);

                        var isEmptyString = (obj instanceof String str && str.equals(""));

                        if (obj == null || isEmptyString || isEmptyArray(obj)) {
                            field.set(config, parent.getClass().getField(field.getName()).get(parent));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            config.onFinishLoading();
            equipmentConfig.put(config.name, config);
        }
    }

    @Nullable
    private <T extends ModelConfigBase> T getConfig(ResourceLocation resourceLocation, Class<T> classOfT) {
        try {
            var readIn = getBufferedReader(resourceLocation);
            var settings = GSON_INSTANCE.fromJson(readIn, classOfT);
            settings.name = getResourceName(resourceLocation);
            return settings;
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedReader getBufferedReader(ResourceLocation resourceLocation) throws IOException {
        return getBufferedReader(Minecraft.getInstance().getResourceManager().getResource(resourceLocation).get().open());
    }
}
