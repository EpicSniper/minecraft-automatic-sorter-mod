package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.InventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MainInventoryUtil {

    @NotNull
    public static IInventoryAdapter getInventoryAdapter(World world, BlockPos pos) {

        IInventoryUtil expandedIU = new ExpandedInventoryUtil();
        IInventoryUtil assortedIU = new AssortedInventoryUtil();
        IInventoryUtil basicCreateIU = new BasicStorageInventoryUtil();

        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (block instanceof ChestBlock chestBlock) {
            return new InventoryAdapter(ChestBlock.getInventory(chestBlock, world.getBlockState(pos), world, pos, true));
        } else if (blockEntity instanceof Inventory inventory) {
            if (expandedIU.isRelatedStorage(block, blockEntity)) {
                return expandedIU.getInventoryAdapter(world, pos, block, blockEntity);
            } else {
                return new InventoryAdapter(inventory);
            }
        } else {
            if (assortedIU.isRelatedStorage(block, blockEntity)) {
                return assortedIU.getInventoryAdapter(world, pos, block, blockEntity);
            } else if (basicCreateIU.isRelatedStorage(block, blockEntity)) {
                IInventoryAdapter iu = basicCreateIU.getInventoryAdapter(world, pos, block, blockEntity);
                if (iu == null) {
                    return new NoInventoryAdapter();
                }

                return iu;
            }
        }

        return new NoInventoryAdapter();
    }
}