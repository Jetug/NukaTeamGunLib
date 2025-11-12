package com.nukateam.ntgl.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.nukateam.chassis_core.common.foundation.item.StackUtils;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.geometry.*;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class GunIconModels implements IUnbakedGeometry<GunIconModels> {
    @Nonnull
    private final ItemStack stack;

    public GunIconModels(ItemStack stack) {
        this.stack = stack;
    }

    public GunIconModels withStack(ItemStack stack) {
        return new GunIconModels(stack);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker modelBaker,
                           Function<Material, TextureAtlasSprite> function,
                           ModelState modelState,
                           ItemOverrides itemOverrides, ResourceLocation resourceLocation)
    {
        var particleLocation = getMaterial(context, "particle");
        var particleSprite = particleLocation != null ? function.apply(particleLocation) : null;

        var itemContext = StandaloneGeometryBakingContext.builder(context)
                .withGui3d(false).withUseBlockLight(false).build(resourceLocation);

        var builder = CompositeModel.Baked.builder(itemContext, particleSprite,
                new GunIconModels.ItemOverrideHandler(itemOverrides, modelBaker, itemContext, this),
                context.getTransforms());

        var skin = StackUtils.getVariant(stack);
        var texture = getTexture(resourceLocation.getNamespace(), getItemName(stack), skin);
        var baseMaterial = new Material(InventoryMenu.BLOCK_ATLAS, texture);
        var baseLocation = getMaterial(context, "base");
        var sprite = !stack.isEmpty() ?
                function.apply(baseMaterial) :
                function.apply(baseLocation);

        var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, sprite.contents());
        var quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> sprite, modelState, resourceLocation);

        builder.addQuads(getLayerRenderTypes(), quads);

        builder.setParticle(particleSprite);
        return builder.build();
    }

    private static String getItemName(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();
    }

    private static ResourceLocation getTexture(String namespace, String nameItem, String skin) {
        return new ResourceLocation(namespace, "item/dynamic/" + nameItem + "/" + nameItem + "_" + skin);
    }

    public static RenderTypeGroup getLayerRenderTypes() {
        return new RenderTypeGroup(RenderType.translucent(), ForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
    }

    public enum Loader implements IGeometryLoader<GunIconModels> {
        INSTANCE;

        @Override
        public GunIconModels read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            return new GunIconModels(ItemStack.EMPTY);
        }
    }

    @Nullable
    private static Material getMaterial(IGeometryBakingContext context, String base) {
        return context.hasMaterial(base) ? context.getMaterial(base) : null;
    }

    private static final class ItemOverrideHandler extends ItemOverrides {
        private final ItemOverrides nested;
        private final ModelBaker baker;
        private final IGeometryBakingContext owner;
        private final GunIconModels parent;

        private ItemOverrideHandler(ItemOverrides nested, ModelBaker baker, IGeometryBakingContext owner, GunIconModels parent) {
            this.nested = nested;
            this.baker = baker;
            this.owner = owner;
            this.parent = parent;
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity, int integer) {
            var overriden = nested.resolve(originalModel, stack, level, entity, integer);
            if (overriden != originalModel) return overriden;
            if (!StackUtils.getVariant(stack).equals("default")) {
                var unbaked = this.parent.withStack(stack);
                var bakedModel = unbaked.bake(owner, baker, Material::sprite, BlockModelRotation.X0_Y0, this,
                        new ResourceLocation(Ntgl.MOD_ID, "gun_icon_override"));
                return bakedModel;
            }
            return originalModel;
        }
    }
}