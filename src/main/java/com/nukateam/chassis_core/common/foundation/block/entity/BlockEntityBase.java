package com.nukateam.chassis_core.common.foundation.block.entity;

//public abstract class BlockEntityBase extends BlockEntity {
//    public final ItemStackHandler itemHandler;
//    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
//
//    public BlockEntityBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int inventorySize) {
//        super(pType, pPos, pBlockState);
//
//        itemHandler = new ItemStackHandler(inventorySize) {
//            @Override
//            protected void onContentsChanged(int slot) {
//                setChanged();
//            }
//        };
//    }
//
//    @Override
//    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
//        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//            return lazyItemHandler.cast();
//        }
//
//        return super.getCapability(cap, side);
//    }
//
//    @Override
//    public void onLoad() {
//        super.onLoad();
//        lazyItemHandler = LazyOptional.of(() -> itemHandler);
//    }
//
//    @Override
//    public void invalidateCaps() {
//        super.invalidateCaps();
//        lazyItemHandler.invalidate();
//    }
//
//    @Override
//    protected void saveAdditional(@NotNull CompoundTag tag) {
//        tag.put("inventory", itemHandler.serializeNBT());
//        super.saveAdditional(tag);
//    }
//
//    @Override
//    public void load(CompoundTag nbt) {
//        super.load(nbt);
//        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
//    }
//
//    public void drops() {
//        var inventory = new SimpleContainer(itemHandler.getSlots());
//
//        for (int i = 0; i < itemHandler.getSlots(); i++)
//            inventory.setItem(i, itemHandler.getStackInSlot(i));
//
//        Containers.dropContents(this.level, this.worldPosition, inventory);
//    }
//}
