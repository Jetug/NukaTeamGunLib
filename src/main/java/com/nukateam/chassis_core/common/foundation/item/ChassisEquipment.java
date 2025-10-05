package com.nukateam.chassis_core.common.foundation.item;

import com.nukateam.chassis_core.client.render.utils.ResourceHelper;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.item.IChassisEquipment;
import com.nukateam.chassis_core.common.network.managers.ConfigSupplier;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.nukateam.chassis_core.common.foundation.item.StackUtils.DEFAULT;
import static com.nukateam.chassis_core.common.foundation.item.StackUtils.getVariant;
import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class ChassisEquipment extends Item implements IChassisEquipment, GeoItem {
    public final ChassisPart part;
    private final AnimatableInstanceCache cache = createInstanceCache(this);
    private final Lazy<String> name = Lazy.of(() -> ResourceHelper.getResourceName(ForgeRegistries.ITEMS.getKey(this)));
//    private final Lazy<EquipmentConfig> config = Lazy.of(() -> modResourceManager.getEquipmentConfig(getName()));
    private  EquipmentConfig config = new EquipmentConfig();
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public ChassisEquipment(Properties pProperties, ChassisPart part) {
        super(pProperties);
        this.part = part;
    }

    @Nullable
    public ResourceLocation getTexture(ItemStack stack) {
        var variant = getVariant(stack);
        var value = getConfig().getTexture(variant);
        return value != null ? value : getConfig().getTexture(DEFAULT);
    }

    @Nullable
    public EquipmentConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ConfigSupplier<EquipmentConfig> config) {
        this.config = config.getConfig();
    }

    public String getName() {
        return name.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {}

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }
}
