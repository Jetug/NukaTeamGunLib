package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.geo.interfaces.DynamicGeoItem;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.ntgl.client.animators.GrenadeAnimator;
import com.nukateam.ntgl.client.model.gun.GeoGrenadeModel;
import com.nukateam.ntgl.client.render.renderers.gun.DynamicGrenadeRenderer;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.common.foundation.init.ModSounds;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.common.foundation.entity.ThrowableGrenadeEntity;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class GrenadeItem extends Item implements DynamicGeoItem, IThrowable {
    protected int maxCookTime;
    private ThrowableConfig projectile = new ThrowableConfig();
    private final Lazy<DynamicGrenadeRenderer> RENDERER = Lazy.of(() -> new DynamicGrenadeRenderer(new GeoGrenadeModel()));
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    private boolean isPreparing = false;
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public GrenadeItem(Item.Properties properties, int maxCookTime) {
        super(properties);
        this.maxCookTime = maxCookTime;
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
    public boolean isPreparing(){
        return this.isPreparing;
    }

    @Override
    public int getPrepareTime() {
        return 10;
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
    public BiFunction<ItemDisplayContext, DynamicGrenadeRenderer<GrenadeAnimator>, GrenadeAnimator> getAnimatorFactory() {
        return GrenadeAnimator::new;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.maxCookTime;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (!this.canCook()) return;

        int duration = this.getUseDuration(stack) - count;
        if(duration < getPrepareTime()) {
            isPreparing = true;
        }
        else {
            isPreparing = false;
        }
        if (duration == getPrepareTime())
            player.level().playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.ITEM_GRENADE_PIN.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
        var stack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        isPreparing = false;

        if (this.canCook() && !level.isClientSide()) {
            if (!(entityLiving instanceof Player player) || !player.isCreative()) {
                stack.shrink(1);
            }

            var grenade = this.create(level, entityLiving, 0);
            grenade.onDeath();
            if (entityLiving instanceof Player) {
                ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
            }
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        isPreparing = false;

        if (!level.isClientSide()) {
            throwItem(stack, level, entityLiving, timeLeft);
        }
    }

    private void throwItem(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        int duration = this.getUseDuration(stack) - timeLeft;
        if (duration >= getPrepareTime()) {
            if (!(entityLiving instanceof Player player) || !player.isCreative()) {
                stack.shrink(1);
            }

            var grenade = this.create(level, entityLiving, this.maxCookTime - duration);
            grenade.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, Math.min(1.0F, duration / 20F), 1.0F);
            level.addFreshEntity(grenade);
            this.onThrown(level, grenade);

            if (entityLiving instanceof Player) {
                ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft) {
        return new ThrowableGrenadeEntity(world, entity, getConfig().getProjectile(), timeLeft);
    }

    public boolean canCook() {
        return true;
    }

    protected void onThrown(Level world, ThrowableGrenadeEntity entity) {}
}
