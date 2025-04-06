package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.geo.interfaces.*;
import com.nukateam.ntgl.client.animators.*;
import com.nukateam.ntgl.client.event.ClientTickHandler;
import com.nukateam.ntgl.common.base.*;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.geo.render.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.renderers.gun.*;
import com.nukateam.ntgl.common.data.config.gun.*;
import com.nukateam.ntgl.common.util.interfaces.*;
import com.nukateam.ntgl.common.debug.*;
import com.nukateam.ntgl.common.foundation.enchantment.*;
import com.nukateam.ntgl.common.foundation.item.interfaces.*;
import mod.azure.azurelib.animatable.*;
import mod.azure.azurelib.core.animatable.instance.*;
import mod.azure.azurelib.core.animation.*;
import net.minecraft.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.common.util.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.*;

import javax.annotation.*;
import java.util.*;
import java.util.function.*;

import static com.nukateam.ntgl.common.data.constants.Tags.AMMO_COUNT;
import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class GunItem extends Item implements DynamicGeoItem, IColored, IMeta, IResourceProvider, IConfigConsumer<Gun>, IConfigProvider<Gun> {
    public static final String VARIANT = "variant";
    public static final Map<ItemStack, String> stackAnimations = new HashMap<>();
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    private final Lazy<String> name = Lazy.of(() -> ResourceUtils.getResourceName(getRegistryName()));
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private final Lazy<DefaultGunRendererGeo> GUN_RENDERER = Lazy.of(() -> new DefaultGunRendererGeo());
    private Gun gun = new Gun();

    @Nullable
    protected Supplier<DynamicGunModifier> modifierFactory = null;
    protected HashMap<MyPair<LivingEntity, HumanoidArm>, DynamicGunModifier> dynamicModifiers = new HashMap<>();

    @Nullable
    public DynamicGunModifier getGunModifier(ItemStack stack) {
        for(var value : dynamicModifiers.values()){
            if(value.getStack() == stack)
                return value;
        }

        return null;
    }

    public GunItem(Item.Properties properties) {
        super(properties);
        ClientTickHandler.addTicker(this, this::tick);
    }

    public void tick(TickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

        }
        else {
//            var forRemoval = new ArrayList<ItemStack>();
//            dynamycModifiers.forEach((k, v) -> {
//                if(!dynamycModifiersBuffer.containsKey(k)){
//                    forRemoval.add(k);
//                }
//            });
//
//            forRemoval.forEach((stack) ->{
//                dynamycModifiers.remove(stack);
//            });
//
//            dynamycModifiersBuffer.clear();
        }
    }

    public GunItem(Supplier<DynamicGunModifier> modifierFactory, Item.Properties properties) {
        this(properties);
        this.modifierFactory = modifierFactory;
    }

    @OnlyIn(Dist.CLIENT)
    public DynamicGeoItemRenderer getRenderer() {
        return GUN_RENDERER.get();
    }

    @Override
    public BiFunction<ItemDisplayContext, DynamicGeoItemRenderer<GunAnimator>, GunAnimator> getAnimatorFactory() {
        return GunAnimator::new;
    }

    @Override
    public void setConfig(NetworkManager.Supplier<Gun> supplier) {
        this.gun = supplier.getConfig();
        gun.onCreated(getName());
    }

    @Override
    public Gun getConfig() {
        return getGun();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getNamespace() {
        return getRegistryName().getNamespace();
    }

    public Gun getGun() {
        return this.gun;
    }

    public static String getVariant(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains(VARIANT, Tag.TAG_STRING)) {
            tag.putString(VARIANT, "default");
        }

        return tag.getString(VARIANT);
    }

    public void setDefaultTag(CompoundTag tag) {
        tag.putInt(AMMO_COUNT, GunModifierHelper.getMaxAmmo(new ItemStack(this)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (modifierFactory != null && entity instanceof LivingEntity livingEntity) {
            var arm = getGunHoldingArm(stack, livingEntity);
//            var modifier = modifierFactory.get();
            var key = new MyPair<>(livingEntity, arm);
//            var value = new MyPair<>(stack, modifier);

            if(arm == null)
                return;

            if (!dynamicModifiers.containsKey(key)) {
                dynamicModifiers.put(key, modifierFactory.get());
            }

            var modifier = dynamicModifiers.get(key);
            modifier.setEntity(livingEntity)
                    .setArm(arm);

            if(modifier.getStack() == null || !ItemStack.matches(modifier.getStack(), stack)){
//                var i = 1;
                modifier.setStack(stack);
            }

//            dynamicModifiers.put(key, value);
        }
    }

    private static @Nullable HumanoidArm getGunHoldingArm(ItemStack stack, LivingEntity livingEntity) {
        HumanoidArm arm = null;

        if(livingEntity.getMainHandItem() == stack){
            arm = HumanoidArm.RIGHT;
        }
        else if(livingEntity.getOffhandItem() == stack){
            arm = HumanoidArm.LEFT;
        }
        return arm;
    }

    //    @Override
//    public void createRenderer(Consumer<Object> consumer) {
//        consumer.accept(new RenderProvider() {
//            private ProxyItemRenderer renderer = null;
//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                if (renderer == null)
//                    return new ProxyItemRenderer((DynamicGunRenderer<GunAnimator>) getRenderer());
//                return this.renderer;
//            }
//        });
//    }

//    @Override
//    public void inventoryTick(ItemStack stack, Level pLevel, Entity entity, int pSlotId, boolean pIsSelected) {
//        checkAmmoCount(stack, entity);
//
//        super.inventoryTick(stack, pLevel, entity, pSlotId, pIsSelected);
//    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        var ammo = ForgeRegistries.ITEMS.getValue(GunModifierHelper.getCurrentAmmoId(stack));

        if (ammo != null) {
            tooltip.add(Component.translatable("info.ntgl.ammo_type", Component.translatable(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }

        var additionalDamageText = "";
        var tagCompound = stack.getTag();
        if (tagCompound != null) {
            if (tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
                float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                additionalDamage += GunModifierHelper.getAdditionalDamage(stack);

                if (additionalDamage > 0) {
                    additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                } else if (additionalDamage < 0) {
                    additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                }
            }
        }

        float damage = GunModifierHelper.getModifiedDamage(stack);
        damage = GunModifierHelper.getModifiedProjectileDamage(stack, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(stack, damage);
        tooltip.add(Component.translatable("info.ntgl.damage",
                ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));

        if (tagCompound != null) {
            if (tagCompound.getBoolean("IgnoreAmmo")) {
                tooltip.add(Component.translatable("info.ntgl.ignore_ammo").withStyle(ChatFormatting.AQUA));
            } else {
                int ammoCount = tagCompound.getInt(AMMO_COUNT);
                tooltip.add(Component.translatable("info.ntgl.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + GunEnchantmentHelper.getAmmoCapacity(stack)).withStyle(ChatFormatting.GRAY));
            }
        }
        //tooltip.add(Component.translatable("info.ntgl.attachment_help", new KeybindComponent("key.ntgl.attachments").getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
    }

//    @Override
//    public boolean isBarVisible(ItemStack stack) {
//        CompoundTag tagCompound = stack.getOrCreateTag();
//        Gun modifiedGun = this.getModifiedGun(stack);
//        return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt(Tags.AMMO_COUNT) != GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun);
//    }

//    @Override
//    public int getBarWidth(ItemStack stack) {
//        CompoundTag tagCompound = stack.getOrCreateTag();
//        Gun modifiedGun = this.getModifiedGun(stack);
//        return (int) (13.0 * (tagCompound.getInt(Tags.AMMO_COUNT) / (double) GunEnchantmentHelper.getAmmoCapacity(stack, modifiedGun)));
//    }

    public Gun getModifiedGun(ItemStack stack) {
        var tagCompound = stack.getTag();
        if (tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if (tagCompound.getBoolean("Custom")) {
                    var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    return Gun.create(key, tagCompound.getCompound("Gun"));
                } else {
                    var gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        if (Ntgl.isDebugging()) {
            return Debug.getGun(this);
        }

        return this.gun;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
            return GunModifierHelper.isAuto(stack);
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

//    @Override
//    public int getBarColor(ItemStack stack) {
//        return requireNonNull(ChatFormatting.YELLOW.getColor());
//    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.getMaxStackSize(stack) == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 5;
    }


    public static void doAnim(ItemStack stack, String animation) {
        stackAnimations.put(stack, animation);
    }

    public static void resetAnim(ItemStack stack) {
        stackAnimations.put(stack, null);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }
}
