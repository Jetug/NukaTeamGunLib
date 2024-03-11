package com.nukateam.gunscore.client.data.util.skin;

import com.mojang.blaze3d.platform.NativeImage;
import com.nukateam.gunscore.GunMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BufferedImageHelper {
    public BufferedImageHelper() {
    }

    @Nullable
    public static BufferedImage resourceToBufferedImage(ResourceLocation resourceLocation) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
            NativeImage nativeImage = NativeImage.read(resource.getInputStream());
            byte[] imageArr = nativeImage.asByteArray();
            return getImage(imageArr);
        } catch (IOException var4) {
            GunMod.LOGGER.error(var4.getMessage(), var4);
            return null;
        }
    }

    public static NativeImage getNativeImage(BufferedImage img) {
        NativeImage nativeImage = new NativeImage(img.getWidth(), img.getHeight(), true);

        for(int x = 0; x < img.getWidth(); ++x) {
            for(int y = 0; y < img.getHeight(); ++y) {
                int clr = img.getRGB(x, y);
                int alpha = (clr & -16777216) >> 24;
                int red = (clr & 16711680) >> 16;
                int green = (clr & '\uff00') >> 8;
                int blue = clr & 255;
                int rgb = (alpha << 8) + blue;
                rgb = (rgb << 8) + green;
                rgb = (rgb << 8) + red;
                nativeImage.setPixelRGBA(x, y, rgb);
            }
        }

        return nativeImage;
    }

    public static BufferedImage extendImage(BufferedImage image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, 2);
        addImage(scaledImage, image, 0, 0);
        return scaledImage;
    }

    public static void cropZone(BufferedImage image, int x, int y, int width, int height) {
        for(int i = 0; i < image.getWidth(); ++i) {
            for(int j = 0; j < image.getHeight(); ++j) {
                if (i < x || i >= x + width || j < y || j >= y + height) {
                    int rgba = image.getRGB(i, j) & 16777215;
                    image.setRGB(i, j, rgba);
                }
            }
        }

    }

    public static void cropImage(BufferedImage img, int xPos, int yPos) {
        for(int x = 0; x < img.getWidth(); ++x) {
            for(int y = 0; y < img.getHeight(); ++y) {
                if (x >= xPos || y >= yPos) {
                    img.setRGB(x, y, (new Color(0.0F, 0.0F, 0.0F, 0.0F)).getRGB());
                }
            }
        }

    }

    private static void addImage(BufferedImage buff1, BufferedImage buff2, int x, int y) {
        Graphics2D g2d = buff1.createGraphics();
        g2d.drawImage(buff2, x, y, (ImageObserver)null);
        g2d.dispose();
    }

    public static BufferedImage getImage(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);

        try {
            return ImageIO.read(bais);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }
}