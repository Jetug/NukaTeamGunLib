package com.nukateam.gunscore.client.data.util.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSkins {
    private static final HashMap<String, GameProfile> players = new HashMap();

    public PlayerSkins() {
    }

    public static ResourceLocation getSkin(UUID uuid, String name) {
        return getSkin(getGameProfile(uuid, name));
    }

    public static ResourceLocation getSkin(Player player) {
        return getSkin(player.getGameProfile());
    }

    public static ResourceLocation getSkin(GameProfile gameProfile) {
        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
        return map.containsKey(MinecraftProfileTexture.Type.SKIN) ? minecraft.getSkinManager().registerTexture((MinecraftProfileTexture)map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) : DefaultPlayerSkin.getDefaultSkin(gameProfile.getId());
    }

    public static GameProfile getGameProfile(UUID uuid, String name) {
        if (players.containsKey(uuid.toString())) {
            return (GameProfile)players.get(uuid.toString());
        } else {
            GameProfile profile = new GameProfile(uuid, name);
            SkullBlockEntity.updateGameprofile(profile, (gameProfile) -> {
                players.put(uuid.toString(), gameProfile);
            });
            return profile;
        }
    }
}