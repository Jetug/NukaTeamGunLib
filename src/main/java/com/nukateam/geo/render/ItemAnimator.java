package com.nukateam.geo.render;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.geo.interfaces.IItemAnimator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;

import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;


public abstract class ItemAnimator implements GeoEntity, IItemAnimator, IResourceProvider {
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected final ItemDisplayContext transformType;
    protected ItemStack itemStack;
    private final Lazy<String> name = Lazy.of(this::crateName);
    private final Lazy<String> namespace = Lazy.of(this::createNamespace);

    public ItemAnimator(ItemDisplayContext transformType) {
        this.transformType = transformType;
    }

    @Override
    public ItemStack getStack() {
        return itemStack;
    }

    @Override
    public void setStack(ItemStack stack){
        this.itemStack = stack;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ItemDisplayContext getTransformType() {
        return transformType;
    }

    @Override
    public String getName() {
        return name.get();
    }
    
    @Override
    public String getNamespace() {
        return namespace.get();
    }

    private String crateName() {
        var item = getStack().getItem();
        if (item instanceof IResourceProvider provider) {
            return provider.getName();
        } else {
            var registryName = getRegistryKey(item);
            return getResourceName(registryName);
        }
    }

    public static String getResourceName(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        return FilenameUtils.removeExtension(FilenameUtils.getName(path));
    }

    private String createNamespace() {
        var item = getStack().getItem();
        if (item instanceof IResourceProvider provider) {
            return provider.getNamespace();
        }
        else {
            var registryName = getRegistryKey(item);
            return registryName.getNamespace();
        }
    }

    @Nullable
    private static ResourceLocation getRegistryKey(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }
}
