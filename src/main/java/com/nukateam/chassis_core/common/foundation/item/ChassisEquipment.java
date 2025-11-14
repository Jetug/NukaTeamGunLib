package com.nukateam.chassis_core.common.foundation.item;

import com.nukateam.chassis_core.client.render.utils.ResourceHelper;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;

import static com.nukateam.chassis_core.common.foundation.item.StackUtils.DEFAULT;
import static com.nukateam.chassis_core.common.foundation.item.StackUtils.getVariant;
import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;

public class ChassisEquipment extends Item implements IChassisEquipment, GeoItem {
    public final ChassisPart part;
    private final AnimatableInstanceCache cache = createInstanceCache(this);
    private final Lazy<String> name = Lazy.of(() -> ResourceHelper.getResourceName(BuiltInRegistries.ITEM.getKey(this)));
    private  EquipmentConfig config = new EquipmentConfig();

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
        this.config = config.config();
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
}
