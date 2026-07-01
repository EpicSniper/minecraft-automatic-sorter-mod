package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.InventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.ItemHandlerAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MainInventoryUtil {

    @NotNull
    public static IInventoryAdapter getInventoryAdapter(Level world, BlockPos pos) {

        IInventoryUtil expandedIU = new ExpandedInventoryUtil();
        IInventoryUtil assortedIU = new AssortedInventoryUtil();

        IItemHandler itemHandler = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (itemHandler != null) {
            return new ItemHandlerAdapter(itemHandler);
        }

        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (block instanceof ChestBlock chestBlock) {
            return new InventoryAdapter(ChestBlock.getContainer(chestBlock, world.getBlockState(pos), world, pos, true));
        } else if (blockEntity instanceof Container inventory) {
            if (expandedIU.isRelatedStorage(block, blockEntity)) {
                return expandedIU.getInventoryAdapter(world, pos, block, blockEntity);
            } else {
                return new InventoryAdapter(inventory);
            }
        } else if (blockEntity instanceof FilterBlockEntity filterBlockEntity) {
            return new InventoryAdapter(filterBlockEntity.getContainer());
        } else {
            if (assortedIU.isRelatedStorage(block, blockEntity)) {
                return assortedIU.getInventoryAdapter(world, pos, block, blockEntity);
            }
        }

        return new NoInventoryAdapter();
    }
}
