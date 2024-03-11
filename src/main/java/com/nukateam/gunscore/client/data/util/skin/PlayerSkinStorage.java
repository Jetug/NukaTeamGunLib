package com.nukateam.gunscore.client.data.util.skin;

import com.ibm.icu.impl.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.UUID;

public class PlayerSkinStorage {
    public static final PlayerSkinStorage INSTANCE = new PlayerSkinStorage();
    public final HashMap<Pair<UUID, Pair<Integer, Integer>>, ResourceLocation> playerSkins = new HashMap();

    private PlayerSkinStorage() {
    }

    public void createSkin(Player player, Pair<Integer, Integer> size, BufferedImage image) {
        UUID tag = player.getUUID();
        String path = "player_" + size.first + "_" + size.second + "_" + tag;
        this.playerSkins.put(Pair.of(tag, size), TextureHelper.createResource(image, path));
    }

    public ResourceLocation getSkin(Player player, Pair<Integer, Integer> size) {
        if (!this.playerSkins.containsKey(Pair.of(player.getUUID(), size))) {
            Thread thread = new Thread(() -> {
                try {
                    BufferedImage image = TextureHelper.getPlayerSkinImage(player);
                    image = BufferedImageHelper.extendImage(image, (Integer)size.first, (Integer)size.second);
                    this.createSkin(player, size, image);
                } catch (Exception var4) {
                    this.createTemplate(player, size);
                }

            });
            thread.start();
            this.createTemplate(player, size);
        }

        return (ResourceLocation)this.playerSkins.get(Pair.of(player.getUUID(), size));
    }

    private void createTemplate(Player player, Pair<Integer, Integer> size) {
        BufferedImage image = TextureHelper.getDefaultPlayerSkinImage(player);
        image = BufferedImageHelper.extendImage(image, (Integer)size.first, (Integer)size.second);
        UUID tag = player.getUUID();
        String path = "player_temp_" + size.first + "_" + size.second + "_" + tag;
        this.playerSkins.put(Pair.of(tag, size), TextureHelper.createResource(image, path));
    }
}