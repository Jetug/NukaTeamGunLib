package com.nukateam.ntgl.client.data.util.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.impl.Pair;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class TextureHelper {
    public static final String MINECRAFT_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public TextureHelper() {
    }

    public static ResourceLocation createResource(String name, AbstractTexture abstractTexture) {
        return createResource("ntgl", name, abstractTexture);
    }

    public static ResourceLocation createResource(String namespace, String name, AbstractTexture abstractTexture) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        ResourceLocation headTextureLocation = new ResourceLocation(namespace, name);
        textureManager.register(headTextureLocation, abstractTexture);
        return headTextureLocation;
    }

    @Nullable
    public static AbstractTexture cropTexture(ResourceLocation resourceLocation, int x, int y, int width, int height) {
        BufferedImage image = BufferedImageHelper.resourceToBufferedImage(resourceLocation);
        if (image == null) {
            return null;
        } else {
            BufferedImageHelper.cropZone(image, x, y, width, height);
            return new DynamicTexture(BufferedImageHelper.getNativeImage(image));
        }
    }

    @Nullable
    public static BufferedImage getPlayerImage(Player clientPlayer) {
        try {
            return getPlayerSkinImage(clientPlayer);
        } catch (Exception var3) {
            ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(clientPlayer.getGameProfile().getId());
            return BufferedImageHelper.resourceToBufferedImage(skin);
        }
    }

    @Nullable
    public static BufferedImage getPlayerHeadImage(Player clientPlayer) {
        try {
            BufferedImage playerSkin = getPlayerSkinImage(clientPlayer);
            if (playerSkin == null) {
                return null;
            } else {
                BufferedImageHelper.cropImage(playerSkin, 64, 16);
                return playerSkin;
            }
        } catch (Exception var4) {
            ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(clientPlayer.getGameProfile().getId());
            BufferedImage playerSkin = BufferedImageHelper.resourceToBufferedImage(skin);
            if (playerSkin == null) {
                return null;
            } else {
                BufferedImageHelper.cropImage(playerSkin, 64, 16);
                return playerSkin;
            }
        }
    }

    @Nullable
    public static AbstractTexture getDefaultResizedHeadTexture(Player clientPlayer, int width, int height) {
        BufferedImage playerHead = getDefaultPlayerHeadImage(clientPlayer);
        if (playerHead == null) {
            return null;
        } else {
            playerHead = BufferedImageHelper.extendImage(playerHead, width, height);
            return new DynamicTexture(BufferedImageHelper.getNativeImage(playerHead));
        }
    }

    @Nullable
    public static BufferedImage getDefaultPlayerHeadImage(Player clientPlayer) {
        ResourceLocation skin = PlayerSkins.getSkin(clientPlayer);
        BufferedImage playerSkin = BufferedImageHelper.resourceToBufferedImage(skin);
        if (playerSkin == null) {
            return null;
        } else {
            BufferedImageHelper.cropImage(playerSkin, 64, 16);
            return playerSkin;
        }
    }

    private static void print(BufferedImage image) {
        File outputFile = new File("C:/Users/Jetug/Desktop/test/output.png");

        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException var3) {
        }

    }

    public static ResourceLocation getPlayerSkinLocation(AbstractClientPlayer player) {
        GameProfile gameProfile = player.getGameProfile();
        PropertyMap propertyMap = gameProfile.getProperties();
        if (!propertyMap.containsKey("textures")) {
            return player.getSkinTextureLocation();
        } else {
            Property property = (Property) propertyMap.get("textures").iterator().next();
            String textureValue = property.getValue();
            ResourceLocation skinLocation = new ResourceLocation("textures/" + textureValue);
            return skinLocation;
        }
    }

    @Nullable
    public static BufferedImage getPlayerSkinImage(Player clientPlayer) {
        BufferedImage skin = skinRequest(clientPlayer.getUUID());
        return (BufferedImage) Objects.requireNonNullElse(skin, BufferedImageHelper.resourceToBufferedImage(PlayerSkins.getSkin(clientPlayer)));
    }

    public static BufferedImage getDefaultPlayerSkinImage(Player clientPlayer) {
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(clientPlayer.getGameProfile().getId());
        return BufferedImageHelper.resourceToBufferedImage(skin);
    }

    @Nullable
    public static BufferedImage skinRequest(UUID uuid) {
        String var10000 = uuid.toString();
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + var10000.replace("-", "");

        try {
            String response = getHTML(url);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            String base64 = json.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            String skinData = decodeBase64(base64);
            json = JsonParser.parseString(skinData).getAsJsonObject();
            String jsonUrl = json.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
            return ImageIO.read(new URL(jsonUrl));
        } catch (Exception var7) {
            if (!(var7 instanceof IOException) && !(var7 instanceof IllegalStateException)) {
                Ntgl.LOGGER.error(var7.getMessage(), var7);
            }

            return null;
        }
    }

    public static ResourceLocation createResource(BufferedImage image, String name) {
        return createResource(image, Ntgl.MOD_ID, name);
    }

    public static ResourceLocation createResource(BufferedImage image, String namespace, String name) {
        Minecraft minecraft = Minecraft.getInstance();
        NativeImage nativeImage = BufferedImageHelper.getNativeImage(image);
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation resourceLocation = new ResourceLocation(namespace, name);
        minecraft.getTextureManager().register(resourceLocation, texture);
        return resourceLocation;
    }

    public static Pair<Integer, Integer> getTextureSize(ResourceLocation resourceLocation) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).get();
            NativeImage nativeImage = NativeImage.read(resource.open());
            return Pair.of(nativeImage.getWidth(), nativeImage.getHeight());
        } catch (IOException var3) {
            return Pair.of(0, 0);
        }
    }

    @Nullable
    public static AbstractTexture getResizedHeadTexture(Player clientPlayer, int width, int height) {
        BufferedImage playerHead = getPlayerHeadImage(clientPlayer);
        if (playerHead == null) {
            return null;
        } else {
            playerHead = BufferedImageHelper.extendImage(playerHead, width, height);
            return new DynamicTexture(BufferedImageHelper.getNativeImage(playerHead));
        }
    }

    private static String decodeBase64(String base64) {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private static String getHTML(String urlToRead) {
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String res = conn.getResponseMessage();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                line = result.toString();
            } catch (Throwable var9) {
                try {
                    reader.close();
                } catch (Throwable var8) {
                    var9.addSuppressed(var8);
                }

                throw var9;
            }

            reader.close();
            return line;
        } catch (Exception var10) {
            return "";
        }
    }
}
