package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.screen.FilterScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

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
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int filterType = FilterTypeEnum.IN_INVENTORY.getValue();

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
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        inventory.deserializeNBT(pRegistries, pTag.getCompound("FilterInventory"));
        filterType = pTag.getInt("FilterType");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.put("FilterInventory", inventory.serializeNBT(pRegistries));
        pTag.putInt("FilterType", filterType);
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
                case WHITELIST -> "Whitelist";
                case IN_INVENTORY -> "In Inventory";
                case REJECTS -> "Rejects";
            };
        }

        public static String getName(int value) {
            return getName(fromValue(value));
        }
    }

}
