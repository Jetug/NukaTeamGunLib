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

    public float getFullnessDisplay(ItemStack pStack) {
        return (float)getContentWeight(pStack) / 64.0F;
    }

    public boolean overrideStackedOnOther(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pStack.getCount() == 1 && pAction == ClickAction.SECONDARY) {
            var itemstack = pSlot.getItem();

            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(pPlayer);
                removeOne(pStack).ifPresent((p_150740_) -> {
                    add(pStack, pSlot.safeInsert(p_150740_));
                });
            } else if (itemstack.getItem().canFitInsideContainerItems()) {
                int i = (maxWeight - getContentWeight(pStack)) / getWeight(itemstack);
                int j = add(pStack, pSlot.safeTake(itemstack.getCount(), i, pPlayer));
                if (j > 0) {
                    this.playInsertSound(pPlayer);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pStack.getCount() != 1) {
            return false;
        } else if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            if (pOther.isEmpty()) {
                removeOne(pStack).ifPresent((p_186347_) -> {
                    this.playRemoveOneSound(pPlayer);
                    pAccess.set(p_186347_);
                });
            } else {
                int i = add(pStack, pOther);
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

    public boolean isBarVisible(ItemStack pStack) {
        return getContentWeight(pStack) > 0;
    }

    public int getBarWidth(ItemStack pStack) {
        return Math.min(1 + 12 * getContentWeight(pStack) / maxWeight, 13);
    }

    public int getBarColor(ItemStack pStack) {
        return BAR_COLOR;
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

    private static Optional<CompoundTag> getMatchingItem(ItemStack pStack, ListTag pList) {
        Optional var10000;
        if (pStack.is(Items.BUNDLE)) {
            var10000 = Optional.empty();
        } else {
            Stream<Tag> var2 = pList.stream();
            Objects.requireNonNull(CompoundTag.class);
            var2 = var2.filter(CompoundTag.class::isInstance);
            Objects.requireNonNull(CompoundTag.class);
            var10000 = var2.map(CompoundTag.class::cast).filter((p_186350_) -> ItemStack.isSameItemSameTags(ItemStack.of(p_186350_), pStack)).findFirst();
        }

        return var10000;
    }

    private int getWeight(ItemStack pStack) {
        return maxWeight / pStack.getMaxStackSize();
    }

    private int getContentWeight(ItemStack pStack) {
        return getContents(pStack).mapToInt((p_186356_) -> {
            return getWeight(p_186356_) * p_186356_.getCount();
        }).sum();
    }

    private Optional<ItemStack> removeOne(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getOrCreateTag();
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
                    pStack.removeTagKey(TAG_ITEMS);
                }

                return Optional.of(itemstack);
            }
        }
    }

    private boolean dropContents(ItemStack pStack, Player pPlayer) {
        CompoundTag compoundtag = pStack.getOrCreateTag();
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

            pStack.removeTagKey(TAG_ITEMS);
            return true;
        }
    }

    private static Stream<ItemStack> getContents(ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag == null) {
            return Stream.empty();
        } else {
            ListTag listtag = compoundtag.getList(TAG_ITEMS, 10);
            Stream<Tag> var10000 = listtag.stream();
            Objects.requireNonNull(CompoundTag.class);
            return var10000.map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }

    public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack pStack) {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        Stream<ItemStack> var10000 = getContents(pStack);
        Objects.requireNonNull(nonnulllist);
        var10000.forEach(nonnulllist::add);
        return Optional.of(new BundleTooltip(nonnulllist, getContentWeight(pStack)));
    }

    public void appendHoverText(@NotNull ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.minecraft.bundle.fullness", new Object[]{getContentWeight(pStack), 64}).withStyle(ChatFormatting.GRAY));
    }

    public void onDestroyed(@NotNull ItemEntity pItemEntity) {
        ItemUtils.onContainerDestroyed(pItemEntity, getContents(pItemEntity.getItem()));
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
