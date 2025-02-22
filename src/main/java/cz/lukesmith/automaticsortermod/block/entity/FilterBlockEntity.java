package cz.lukesmith.automaticsortermod.block.entity;

import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import cz.lukesmith.automaticsortermod.screen.FilterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, AbstractInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(24, ItemStack.EMPTY);

    private int filterType = FilterTypeEnum.IN_INVENTORY.getValue();

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_BLOCK_ENTITY, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("FilterType", filterType);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        filterType = nbt.getInt("FilterType");
    }

    public static FilterBlockEntity create(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(AutomaticSorterMod.MOD_ID + ".filter_block_entity");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FilterScreenHandler(syncId, playerInventory, this, new PropertyDelegate() {
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
            public int size() {
                return 1;
            }
        });
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return; // Tick metoda se neprovádí na klientské straně
        }
    }

    public int getFilterType() {
        return filterType;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public boolean isItemInInventory(ItemStack singleItem) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && ItemStack.canCombine(stack, singleItem)) {
                return true;
            }
        }
        return false;
    }

    public enum FilterTypeEnum {
        WHITELIST(0),
        IN_INVENTORY(1);

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
            return (number + 1) % values().length;
        }

        public static String getName(FilterTypeEnum type) {
            return switch (type) {
                case WHITELIST -> "Whitelist";
                case IN_INVENTORY -> "In Inventory";
            };
        }

        public static String getName(int value) {
            return getName(fromValue(value));
        }
    }
}
