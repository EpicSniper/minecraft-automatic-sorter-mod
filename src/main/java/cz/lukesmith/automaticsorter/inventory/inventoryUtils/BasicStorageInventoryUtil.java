package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.BasicCrateInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.InventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BasicStorageInventoryUtil implements IInventoryUtil {

    private static final String STATION_CLASS_NAME = "com.khazoda.basicstorage.block.entity.CrateStationBlockEntity";
    private static final String CRATE_CLASS_NAME = "com.khazoda.basicstorage.block.entity.CrateBlockEntity";
    public static final String CRATE_SLOT_CLASS_NAME = "com.khazoda.basicstorage.storage.CrateSlot";


    @Override
    public IInventoryAdapter getInventoryAdapter(World world, BlockPos pos, Block block, BlockEntity blockEntity) {
        if (isBasicStorageCrate(blockEntity)) {
            return getBasicStorageCrateInventoryAdapter(blockEntity);
        } else if (isBasicStorageStation(blockEntity)) {
            return getBasicStorageStationInventoryAdapter(blockEntity);
        }

        return null;
    }

    @Override
    public boolean isRelatedStorage(Block block, BlockEntity blockEntity) {
        return isBasicStorageCrate(blockEntity) || isBasicStorageStation(blockEntity);
    }

    private static boolean isBasicStorageStation(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageStationClass = Class.forName(STATION_CLASS_NAME);
            return basicStorageStationClass.isInstance(blockEntity);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    public static IInventoryAdapter getBasicStorageStationInventoryAdapter(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageStationClass = Class.forName(STATION_CLASS_NAME);
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

    private static boolean isBasicStorageCrate(BlockEntity blockEntity) {
        try {
            Class<?> basicStorageChestClass = Class.forName(CRATE_CLASS_NAME);
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
            Class<?> basicStorageChestClass = Class.forName(CRATE_SLOT_CLASS_NAME);
            if (basicStorageChestClass.isInstance(blockEntity)) {
                Object basicStorageChest = basicStorageChestClass.cast(blockEntity);
                Field storageField = basicStorageChestClass.getField("storage");
                return storageField.get(basicStorageChest);
            }
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ignored) {

        }

        return null;
    }
}
