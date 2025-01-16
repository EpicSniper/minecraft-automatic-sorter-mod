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

public class FilterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    private int canReceiveItems = 0;

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
        nbt.putInt("CanReceiveItems", canReceiveItems);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        canReceiveItems = nbt.getInt("CanReceiveItems");
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
                    return FilterBlockEntity.this.canReceiveItems;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    FilterBlockEntity.this.canReceiveItems = value;
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

    public int getCanReceiveItems() {
        return canReceiveItems;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public void setCanReceiveItems(int canReceiveItems) {
        this.canReceiveItems = canReceiveItems;
    }
}
