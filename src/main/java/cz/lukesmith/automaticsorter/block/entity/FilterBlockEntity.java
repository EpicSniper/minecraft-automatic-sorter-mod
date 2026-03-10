package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.screen.FilterScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;

public class FilterBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler inventory = new ItemStackHandler(24) {
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    };

    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);

    private int filterType = FilterTypeEnum.IN_INVENTORY.getValue();

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCap.invalidate(); // nutné pro uvolnění paměti při zničení bloku
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public Container getContainer() {
        return new Container() {
            @Override
            public int getContainerSize() {
                return inventory.getSlots();
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < getContainerSize(); i++) {
                    if (!getItem(i).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public @NotNull ItemStack getItem(int index) {
                return inventory.getStackInSlot(index);
            }

            @Override
            public @NotNull ItemStack removeItem(int index, int count) {
                return inventory.extractItem(index, count, false);
            }

            @Override
            public @NotNull ItemStack removeItemNoUpdate(int pSlot) {
                ItemStack stack = inventory.getStackInSlot(pSlot);
                if (!stack.isEmpty()) {
                    inventory.setStackInSlot(pSlot, ItemStack.EMPTY);
                    return stack;
                }
                return ItemStack.EMPTY;
            }

            @Override
            public void setItem(int index, @NotNull ItemStack stack) {
                inventory.setStackInSlot(index, stack);
            }

            @Override
            public void setChanged() {

            }

            @Override
            public boolean stillValid(@NotNull Player pPlayer) {
                return false;
            }

            @Override
            public void clearContent() {
                inventory.setSize(0);
            }
        };
    }

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput pInput) {
        super.loadAdditional(pInput);
        this.loadAdditionalInventory(pInput);

        filterType = pInput.getInt("FilterType").orElse(0);
        this.setChanged();
    }

    private void loadAdditionalInventory(ValueInput pInput) {
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(pInput, items);
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, items.get(i));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput pOutput) {
        super.saveAdditional(pOutput);
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSlots(); i++) {
            items.set(i, inventory.getStackInSlot(i));
        }

        ContainerHelper.saveAllItems(pOutput, items);
        pOutput.putInt("FilterType", filterType);
    }

    public static FilterBlockEntity create(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("automaticsorter.filter_block_entity");
    }

    public int getFilterType() {
        return filterType;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.setBlock(getBlockPos(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
        this.setChanged();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FilterScreenHandler(pContainerId, pPlayerInventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return FilterBlockEntity.this.filterType;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    FilterBlockEntity.this.filterType = value;
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput data, HolderLookup.Provider lookup) {
        super.onDataPacket(connection, data, lookup);
        loadAdditional(data);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider lookup) {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", inventory.serializeNBT(lookup));
        tag.putInt("FilterType", filterType);
        return tag;
    }

    @Override
    public void handleUpdateTag(ValueInput tag, HolderLookup.Provider holders) {
        extracted(tag, holders);
        ValueInput tagInput = tag.childOrEmpty("inventory");
        this.loadAdditionalInventory(tagInput);

        this.filterType = tag.getIntOr("FilterType", 0);
    }

    private void extracted(ValueInput tag, HolderLookup.Provider holders) {
        super.handleUpdateTag(tag, holders);
    }

    public enum FilterTypeEnum {
        WHITELIST(0),
        IN_INVENTORY(1),
        REJECTS(2);

        private final int value;


        FilterTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static FilterTypeEnum fromValue(int value) {
            for (FilterTypeEnum type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }

        public static int nextValue(int number) {
            return switch (FilterTypeEnum.fromValue(number)) {
                case WHITELIST -> FilterTypeEnum.IN_INVENTORY.getValue();
                case IN_INVENTORY -> FilterTypeEnum.REJECTS.getValue();
                case REJECTS -> FilterTypeEnum.WHITELIST.getValue();
            };
        }

        public static String getName(FilterTypeEnum type) {
            return switch (type) {
                case WHITELIST -> "automaticsroter.filter_mode.whitelist";
                case IN_INVENTORY -> "automaticsroter.filter_mode.in_inventory";
                case REJECTS -> "automaticsorter.filter_mode.rejects";
            };
        }

        public static String getName(int value) {
            return getName(fromValue(value));
        }
    }

}
