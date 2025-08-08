package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.geo.interfaces.DynamicGeoItem;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.client.animators.ThrowableAnimator;
import com.nukateam.ntgl.client.model.gun.ThrowableItemModel;
import com.nukateam.ntgl.client.render.renderers.weapon.ThrowableItemRenderer;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class ThrowableItem extends Item implements DynamicGeoItem, IThrowable {
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    private final Lazy<ThrowableItemRenderer<?>> RENDERER = Lazy.of(() ->
            new ThrowableItemRenderer<ThrowableAnimator>(new ThrowableItemModel()));
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private ThrowableConfig projectile = new ThrowableConfig();

    public ThrowableItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableConfig getConfig() {
        return projectile;
    }

    @Override
    public void setConfig(ConfigSupplier<ThrowableConfig> supplier) {
        projectile = supplier.getConfig();
    }

    @Override
    public BiFunction<ItemDisplayContext, ThrowableItemRenderer<ThrowableAnimator>, ThrowableAnimator> getAnimatorFactory() {
        return ThrowableAnimator::new;
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @OnlyIn(Dist.CLIENT)
    public DynamicGeoItemRenderer getRenderer() {
        return RENDERER.get();
    }

    @Override
    public void expire(LivingEntity entityLiving) {
        var throwableEntity = this.create(entityLiving.level(), entityLiving, 0);
        throwableEntity.onDeath();
    }

    @Override
    public void throwItem(ItemStack stack, LivingEntity entityLiving, int timeLeft) {
        var level = entityLiving.level();

        if (!(entityLiving instanceof Player player) || !player.isCreative()) {
            stack.shrink(1);
        }

        var grenade = this.create(level, entityLiving, timeLeft);
        grenade.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, Math.min(1.0F, timeLeft / 20F), 1.0F);
        level.addFreshEntity(grenade);
        this.onThrown(level, grenade);

        if (entityLiving instanceof Player) {
            ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    public ThrowableItemEntity create(Level world, LivingEntity entity, int timeLeft) {


        return ProjectileManager.getInstance()
                .getFactory(getConfig().getGeneral().getProjectileType())
                .create(world, entity, getConfig().getProjectile(), timeLeft);
    }

    protected void onThrown(Level world, ThrowableItemEntity entity) {}
}
