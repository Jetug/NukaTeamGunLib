package com.nukateam.ntgl.common.foundation.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class AmmoBoxItem extends Item {
    private static final String TAG_ITEMS = "Items";
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);
    private final int maxWeight;

    public AmmoBoxItem(Item.Properties pProperties, int maxWeight) {
        super(pProperties);
        this.maxWeight = maxWeight;
    }

    public float getFullnessDisplay(ItemStack stack) {
        return (float)getContentWeight(stack) / 64.0F;
    }



    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (stack.getCount() == 1 && pAction == ClickAction.SECONDARY) {
            var itemstack = pSlot.getItem();

            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(pPlayer);
                removeOne(stack).ifPresent((p_150740_) -> {
                    add(stack, pSlot.safeInsert(p_150740_));
                });
            } else if (itemstack.getItem().canFitInsideContainerItems()) {
                int i = (maxWeight - getContentWeight(stack)) / getWeight(itemstack);
                int j = add(stack, pSlot.safeTake(itemstack.getCount(), i, pPlayer));
                if (j > 0) {
                    this.playInsertSound(pPlayer);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (stack.getCount() != 1) {
            return false;
        } else if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            if (pOther.isEmpty()) {
                removeOne(stack).ifPresent((p_186347_) -> {
                    this.playRemoveOneSound(pPlayer);
                    pAccess.set(p_186347_);
                });
            } else {
                int i = add(stack, pOther);
                if (i > 0) {
                    this.playInsertSound(pPlayer);
                    pOther.shrink(i);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (dropContents(itemstack, pPlayer)) {
            this.playDropContentsSound(pPlayer);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getContentWeight(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + 12 * getContentWeight(stack) / maxWeight, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        var list = NonNullList.<ItemStack>create();
        var stream = getContents(stack);
        Objects.requireNonNull(list);
        stream.forEach(list::add);
        return Optional.of(new BundleTooltip(list, getContentWeight(stack)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.minecraft.bundle.fullness", new Object[]{getContentWeight(stack), 64}).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity pItemEntity) {
        ItemUtils.onContainerDestroyed(pItemEntity, getContents(pItemEntity.getItem()));
    }

//    public int getAmmoCount(GunData gunData, ItemStack ammoBox, Item item) {
//        var result = 0;
//        for(var projectile : getContents(ammoBox).toList()){
//            if(WeaponModifierHelper.isCurrentAmmo(gunData, item))
//                result += projectile.getCount();
//        }
//        return result;
//    }
//
    public int getAmmoCount(ItemStack ammoBox, Item item) {
        var result = 0;
        for(var ammo : getContents(ammoBox).toList()){
            if(ammo.getItem() == item)
                result += ammo.getCount();
        }
        return result;
    }

    private int add(ItemStack pBundleStack, ItemStack insertedStack) {
        if (!insertedStack.isEmpty() && insertedStack.getItem().canFitInsideContainerItems()) {
            var compoundtag = pBundleStack.getOrCreateTag();
            if (!compoundtag.contains(TAG_ITEMS)) {
                compoundtag.put(TAG_ITEMS, new ListTag());
            }

            int i = getContentWeight(pBundleStack);
            int j = getWeight(insertedStack);
            int k = Math.min(insertedStack.getCount(), (64 - i) / j);
            if (k == 0) {
                return 0;
            } else {
                var listtag = compoundtag.getList(TAG_ITEMS, 10);
                var optional = getMatchingItem(insertedStack, listtag);
                if (optional.isPresent()) {
                    var compoundtag1 = (CompoundTag)optional.get();
                    var itemstack = ItemStack.of(compoundtag1);
                    itemstack.grow(k);
                    itemstack.save(compoundtag1);
                    listtag.remove(compoundtag1);
                    listtag.add(0, compoundtag1);
                } else {
                    var itemstack1 = insertedStack.copyWithCount(k);
                    var compoundtag2 = new CompoundTag();
                    itemstack1.save(compoundtag2);
                    listtag.add(0, compoundtag2);
                }

                return k;
            }
        } else {
            return 0;
        }
    }

    private static Optional<CompoundTag> getMatchingItem(ItemStack stack, ListTag pList) {
        Optional var10000;
        if (stack.is(Items.BUNDLE)) {
            var10000 = Optional.empty();
        } else {
            Stream<Tag> var2 = pList.stream();
            Objects.requireNonNull(CompoundTag.class);
            var2 = var2.filter(CompoundTag.class::isInstance);
            Objects.requireNonNull(CompoundTag.class);
            var10000 = var2.map(CompoundTag.class::cast).filter((p_186350_) -> ItemStack.isSameItemSameTags(ItemStack.of(p_186350_), stack)).findFirst();
        }

        return var10000;
    }

    private int getWeight(ItemStack stack) {
        return maxWeight / stack.getMaxStackSize();
    }

    private int getContentWeight(ItemStack stack) {
        return getContents(stack).mapToInt((p_186356_) -> {
            return getWeight(p_186356_) * p_186356_.getCount();
        }).sum();
    }

    private Optional<ItemStack> removeOne(ItemStack stack) {
        CompoundTag compoundtag = stack.getOrCreateTag();
        if (!compoundtag.contains(TAG_ITEMS)) {
            return Optional.empty();
        } else {
            ListTag listtag = compoundtag.getList(TAG_ITEMS, 10);
            if (listtag.isEmpty()) {
                return Optional.empty();
            } else {
                boolean i = false;
                CompoundTag compoundtag1 = listtag.getCompound(0);
                ItemStack itemstack = ItemStack.of(compoundtag1);
                listtag.remove(0);
                if (listtag.isEmpty()) {
                    stack.removeTagKey(TAG_ITEMS);
                }

                return Optional.of(itemstack);
            }
        }
    }

    private boolean dropContents(ItemStack stack, Player pPlayer) {
        CompoundTag compoundtag = stack.getOrCreateTag();
        if (!compoundtag.contains(TAG_ITEMS)) {
            return false;
        } else {
            if (pPlayer instanceof ServerPlayer) {
                ListTag listtag = compoundtag.getList(TAG_ITEMS, 10);

                for(int i = 0; i < listtag.size(); ++i) {
                    CompoundTag compoundtag1 = listtag.getCompound(i);
                    ItemStack itemstack = ItemStack.of(compoundtag1);
                    pPlayer.drop(itemstack, true);
                }
            }

            stack.removeTagKey(TAG_ITEMS);
            return true;
        }
    }

    public static Stream<ItemStack> getContents(ItemStack stack) {
        var compoundtag = stack.getTag();
        if (compoundtag == null) {
            return Stream.empty();
        } else {
            var listTag = compoundtag.getList(TAG_ITEMS, 10);
            var tagStream = listTag.stream();
            Objects.requireNonNull(CompoundTag.class);
            return tagStream.map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }

    private void playRemoveOneSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
