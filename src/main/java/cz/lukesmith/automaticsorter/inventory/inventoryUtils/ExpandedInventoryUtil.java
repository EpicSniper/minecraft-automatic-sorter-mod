package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.InventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.MultiInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExpandedInventoryUtil implements IInventoryUtil {

    private static final String MAIN_CLASSNAME = "dev.compasses.expandedstorage.block.AbstractChestBlock";
    private static final String CHEST_BLOCK_ENTITY_CLASSNAME = "dev.compasses.expandedstorage.block.entity.ChestBlockEntity";

    public IInventoryAdapter getInventoryAdapter(World world, BlockPos pos, Block block, BlockEntity blockEntity) {
        try {
            Class<?> chestBlockClass = Class.forName(MAIN_CLASSNAME);
            if (chestBlockClass.isInstance(block)) {
                MultiInventoryAdapter multiInventoryAdapter = new MultiInventoryAdapter(new InventoryAdapter(getExpandedStorageInventory(blockEntity)));
                try {
                    Object chestBlock = chestBlockClass.cast(block);
                    Method getContainerMethod = chestBlockClass.getMethod("getDirectionToAttached", BlockState.class);
                    Direction direction = (Direction) getContainerMethod.invoke(chestBlock, world.getBlockState(pos));
                    BlockPos secondPos = pos.offset(direction);
                    BlockEntity secondBlockEntity = world.getBlockEntity(secondPos);
                    Direction facing = world.getBlockState(pos).get(ChestBlock.FACING);
                    InventoryAdapter secondInventoryAdapter = new InventoryAdapter(getExpandedStorageInventory(secondBlockEntity));
                    if ((facing == Direction.EAST && direction == Direction.SOUTH)
                            || (facing == Direction.SOUTH && direction == Direction.WEST)
                            || (facing == Direction.WEST && direction == Direction.NORTH)
                            || (facing == Direction.NORTH && direction == Direction.EAST)
                            || (direction == Direction.UP)) {
                        multiInventoryAdapter.addInventoryAdapterAsFirst(secondInventoryAdapter);
                    } else {
                        multiInventoryAdapter.addInventoryAdapter(secondInventoryAdapter);
                    }

                    return multiInventoryAdapter;
                } catch (InvocationTargetException ignored) {

                }

                return multiInventoryAdapter;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {

        }

        return new NoInventoryAdapter();
    }


    public boolean isRelatedStorage(Block block, BlockEntity blockEntity) {
        try {
            Class<?> chestBlockClass = Class.forName(MAIN_CLASSNAME);
            return chestBlockClass.isInstance(block);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    private static Inventory getExpandedStorageInventory(BlockEntity blockEntity) {
        try {
            Class<?> chestBlockEntityClass = Class.forName(CHEST_BLOCK_ENTITY_CLASSNAME);
            if (chestBlockEntityClass.isInstance(blockEntity)) {
                Object chestBlockEntity = chestBlockEntityClass.cast(blockEntity);
                Method getInventory = chestBlockEntityClass.getMethod("getInventory");
                if (getInventory.invoke(chestBlockEntity) instanceof Inventory inventory) {
                    return inventory;
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException ignored) {

        }

        return null;
    }
}
