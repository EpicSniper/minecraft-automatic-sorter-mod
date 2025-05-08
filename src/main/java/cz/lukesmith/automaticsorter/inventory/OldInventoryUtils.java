package cz.lukesmith.automaticsorter.inventory;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.InventoryAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class OldInventoryUtils {
/*
    @NotNull
    public static IInventoryAdapter getInventoryAdapter(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        AutomaticSorter.LOGGER.info(block.toString());
        if (blockEntity != null) {
            AutomaticSorter.LOGGER.info(blockEntity.toString());
        }


        if (block instanceof ChestBlock chestBlock) {
            return new InventoryAdapter(ChestBlock.getInventory(chestBlock, world.getBlockState(pos), world, pos, true));
        } else if (blockEntity instanceof Inventory inventory) {
            if (isExpandedStorageChest(block)) {
                return getExpandedStorageInventoryAdapter(world, pos, block, blockEntity);
            } else {
                return new InventoryAdapter(inventory);
            }
        } else {
            if (isAssortedStorageChest(blockEntity)) {
                return getAssortedStorageInventoryAdapter(blockEntity);
            } else if (isBasicStorageCrate(blockEntity)) {
                return getBasicStorageCrateInventoryAdapter(blockEntity);
            } else if (isBasicStorageStation(blockEntity)) {
                return getBasicStorageStationInventoryAdapter(blockEntity);
            }
        }

        return new NoInventoryAdapter();
    }

    // Basic Storage Mod

    private static String getBasicStorageStationClassName() {
        return "com.khazoda.basicstorage.block.entity.CrateStationBlockEntity";
    }

    private static boolean isBasicStorageStation(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageStationClass = Class.forName(getBasicStorageStationClassName());
            return basicStorageStationClass.isInstance(blockEntity);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    public static IInventoryAdapter getBasicStorageStationInventoryAdapter(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageStationClass = Class.forName(getBasicStorageStationClassName());
            if (basicStorageStationClass.isInstance(blockEntity)) {
                Object basicStorageStation = basicStorageStationClass.cast(blockEntity);
                Method getInventoryMethod = basicStorageStationClass.getMethod("getInventory");
                Inventory inventory = (Inventory) getInventoryMethod.invoke(basicStorageStation);
                return new InventoryAdapter(inventory);
            }
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException ignored) {

        }

        return new NoInventoryAdapter();
    }

    private static String getBasicStorageClassName() {
        return "com.khazoda.basicstorage.block.entity.CrateBlockEntity";
    }

    public static String getBasicStorageCrateSlotClassName() {
        return "com.khazoda.basicstorage.storage.CrateSlot";
    }

    private static boolean isBasicStorageCrate(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageChestClass = Class.forName(getBasicStorageClassName());
            return basicStorageChestClass.isInstance(blockEntity);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    public static IInventoryAdapter getBasicStorageCrateInventoryAdapter(BlockEntity blockEntity) {
        Object storage = getBasicCrateSlot(blockEntity);
        if (storage == null) {
            return new NoInventoryAdapter();
        }

        return new BasicCrateInventoryAdapter(storage);
    }

    public static Object getBasicCrateSlot(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageChestClass = Class.forName(getBasicStorageCrateSlotClassName());
            if (basicStorageChestClass.isInstance(blockEntity)) {
                Object basicStorageChest = basicStorageChestClass.cast(blockEntity);
                Field storageField = basicStorageChestClass.getField("storage");
                return storageField.get(basicStorageChest);
            }
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ignored) {

        }

        return null;
    }*/
}