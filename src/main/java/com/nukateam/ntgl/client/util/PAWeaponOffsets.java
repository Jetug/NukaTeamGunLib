package com.nukateam.ntgl.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PAWeaponOffsets {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "pa_weapon_offsets.json");
    private static Map<String, Offset> offsets = new HashMap<>();

    static {
        load();
    }

    public static class Offset {
        public int x, y, z, rx, ry, rz;
        public int rArmX, rArmY, rArmZ, rArmRX, rArmRY, rArmRZ;
        public int lArmX, lArmY, lArmZ, lArmRX, lArmRY, lArmRZ;
        public int hX, hY, hZ, hRX, hRY, hRZ;
        public int emX, emY, emZ;
        public int hudX, hudY, hudZ;
        public int gpX, gpY, gpZ;
        public int mfX, mfY, mfZ;

        public Offset(int x, int y, int z, int rx, int ry, int rz,
                      int rX, int rY, int rZ, int rRx, int rRy, int rRz,
                      int lX, int lY, int lZ, int lRx, int lRy, int lRz,
                      int hX, int hY, int hZ, int hRX, int hRY, int hRZ,
                      int emX, int emY, int emZ,
                      int hudX, int hudY, int hudZ,
                      int gpX, int gpY, int gpZ,
                      int mfX, int mfY, int mfZ) {
            this.x = x; this.y = y; this.z = z; this.rx = rx; this.ry = ry; this.rz = rz;
            this.rArmX = rX; this.rArmY = rY; this.rArmZ = rZ; this.rArmRX = rRx; this.rArmRY = rRy; this.rArmRZ = rRz;
            this.lArmX = lX; this.lArmY = lY; this.lArmZ = lZ; this.lArmRX = lRx; this.lArmRY = lRy; this.lArmRZ = lRz;
            this.hX = hX; this.hY = hY; this.hZ = hZ; this.hRX = hRX; this.hRY = hRY; this.hRZ = hRZ;
            this.emX = emX; this.emY = emY; this.emZ = emZ;
            this.hudX = hudX; this.hudY = hudY; this.hudZ = hudZ;
            this.gpX = gpX; this.gpY = gpY; this.gpZ = gpZ;
            this.mfX = mfX; this.mfY = mfY; this.mfZ = mfZ;
        }
    }

    public static void load() {
        offsets.put("nukacraft:pipepistol", new Offset(2, 2, 3, -5, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:hammer", new Offset(2, 7, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:gatling_laser", new Offset(1, 0, 8, 29, -29, -1, -24, -3, -1, 28, -16, 0, 50, -11, 8, -1, -22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:global_offsets", new Offset(-10, -10, -13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:baseball_bat", new Offset(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:fireaxe", new Offset(0, -2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:pistol10mm", new Offset(2, 9, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:minigun", new Offset(1, 4, 5, 57, -52, 0, 1, 1, -16, 3, 5, 0, 16, 0, -38, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:broadsider", new Offset(2, 4, 7, 61, -49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:handmade_flamer", new Offset(3, 10, 15, 56, -44, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:grenade", new Offset(1, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:holy_grenade", new Offset(1, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:incendiary_grenade", new Offset(1, 3, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:flame_floater_grenade", new Offset(1, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:toxic_floater_grenade", new Offset(1, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:baseball_grenade", new Offset(1, 2, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:stun_grenade", new Offset(1, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:molotov", new Offset(1, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:dynamite_stick", new Offset(1, 1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:molotov_cola", new Offset(1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:goo", new Offset(5, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:komi", new Offset(0, -2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:security_baton", new Offset(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:shiv", new Offset(0, -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:poolcue", new Offset(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:rollpin", new Offset(0, -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:combat_knife", new Offset(0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:cane", new Offset(0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:golfdriver", new Offset(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:crowbar", new Offset(0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:cosmicknife", new Offset(0, -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:machete", new Offset(0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:pipe_wrench", new Offset(0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:hatchet", new Offset(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:classic10mm", new Offset(2, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:scout10mm", new Offset(2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:piperevolver", new Offset(2, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:fatman", new Offset(0, 1, 4, -19, -2, 0, 0, 0, 53, 9, -38, 0, 0, -1, -47, 16, -27, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:powdergun", new Offset(3, 3, 0, -14, -11, 0, 8, -4, -21, 27, -31, 0, 30, 7, -44, 46, -66, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:shotgun", new Offset(2, 0, 0, 0, -21, 0, -23, -3, 48, 15, -24, 0, 6, 4, -68, 22, -61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:laser_rifle", new Offset(3, 1, -1, -9, -31, 0, -33, 5, 20, 26, -15, 0, 49, 0, -44, 46, -70, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:laser_pistol", new Offset(2, 1, 1, 0, 0, 0, 0, 0, -11, 20, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:classic_laser_pistol", new Offset(2, 2, 4, 0, 0, 0, 0, -1, 1, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:flamer", new Offset(1, 0, 14, 28, -23, 0, -16, -3, 2, 37, -25, 0, 49, 5, -22, 0, -22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:missile_launcher", new Offset(0, -1, 4, -27, 0, 0, 0, -1, 56, 19, -34, 0, -8, 0, -32, 30, -40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        offsets.put("nukacraft:tesla_rifle", new Offset(4, 0, -1, -15, -26, 0, 0, -3, 12, 33, -16, 0, 42, 1, -28, 41, -56, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                Type type = new TypeToken<Map<String, Offset>>(){}.getType();
                Map<String, Offset> jsonOffsets = GSON.fromJson(reader, type);
                if (jsonOffsets != null) {
                    offsets.putAll(jsonOffsets);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(ResourceLocation id, int x, int y, int z, int rx, int ry, int rz,
                            int rX, int rY, int rZ, int rRx, int rRy, int rRz,
                            int lX, int lY, int lZ, int lRx, int lRy, int lRz,
                            int hX, int hY, int hZ, int hRX, int hRY, int hRZ,
                            int emX, int emY, int emZ,
                            int hudX, int hudY, int hudZ,
                            int gpX, int gpY, int gpZ,
                            int mfX, int mfY, int mfZ) {
        offsets.put(id.toString(), new Offset(x, y, z, rx, ry, rz, rX, rY, rZ, rRx, rRy, rRz, lX, lY, lZ, lRx, lRy, lRz, hX, hY, hZ, hRX, hRY, hRZ, emX, emY, emZ, hudX, hudY, hudZ, gpX, gpY, gpZ, mfX, mfY, mfZ));
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(offsets, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Offset get(ResourceLocation id) {
        return offsets.get(id.toString());
    }

    public static void updateInMemory(ResourceLocation id, int x, int y, int z, int rx, int ry, int rz,
                                      int rX, int rY, int rZ, int rRx, int rRy, int rRz,
                                      int lX, int lY, int lZ, int lRx, int lRy, int lRz,
                                      int hX, int hY, int hZ, int hRX, int hRY, int hRZ,
                                      int emX, int emY, int emZ,
                                      int hudX, int hudY, int hudZ,
                                      int gpX, int gpY, int gpZ,
                                      int mfX, int mfY, int mfZ) {
        offsets.put(id.toString(), new Offset(x, y, z, rx, ry, rz, rX, rY, rZ, rRx, rRy, rRz, lX, lY, lZ, lRx, lRy, lRz, hX, hY, hZ, hRX, hRY, hRZ, emX, emY, emZ, hudX, hudY, hudZ, gpX, gpY, gpZ, mfX, mfY, mfZ));
    }

    public static Offset getGlobal() {
        return offsets.computeIfAbsent("nukacraft:global_offsets", k -> new Offset(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }
}
