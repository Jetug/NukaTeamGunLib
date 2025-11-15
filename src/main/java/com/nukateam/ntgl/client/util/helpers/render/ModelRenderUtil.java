package com.nukateam.ntgl.client.util.helpers.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

import static net.minecraft.world.item.ItemDisplayContext.*;

public class ModelRenderUtil {
    public static void scissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        GL11.glScissor(
                x * scale,
                mc.getWindow().getScreenHeight() - y * scale - height * scale,
                Math.max(0, width * scale), Math.max(0, height * scale));
    }

    public static BakedModel getModel(Item item) {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(item));
    }

    public static BakedModel getModel(ItemStack item) {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);
    }

    public static void applyTransformType(ItemStack stack, PoseStack poseStack, ItemDisplayContext transformType, @Nullable LivingEntity entity) {
        var model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity != null ? entity.level() : null, entity, 0);
        var leftHanded = transformType == FIRST_PERSON_LEFT_HAND || transformType == THIRD_PERSON_LEFT_HAND;
        model.applyTransform(transformType, poseStack, leftHanded);

        /* Flips the model and normals if left handed. */
        if (leftHanded) {
            var scale = new Matrix4f().scale(-1, 1, 1);
            var normal = new Matrix3f(scale);
            poseStack.last().pose().mul(scale);
            poseStack.last().normal().mul(normal);
        }
    }

    public static boolean isMouseWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}