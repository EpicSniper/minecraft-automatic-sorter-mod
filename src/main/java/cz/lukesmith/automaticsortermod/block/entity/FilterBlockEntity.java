package cz.lukesmith.automaticsortermod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_BLOCK_ENTITY, pos, state);
    }

    public static FilterBlockEntity create(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {

    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;    // 22:37
    }

    public static void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return; // Tick metoda se neprovádí na klientské straně
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return DefaultedList.ofSize(1, ItemStack.EMPTY);
    }
}
