package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IInventoryUtil {

    IInventoryAdapter getInventoryAdapter(World world, BlockPos pos, Block block, BlockEntity blockEntity);

    boolean isRelatedStorage(Block block, BlockEntity blockEntity);
}
