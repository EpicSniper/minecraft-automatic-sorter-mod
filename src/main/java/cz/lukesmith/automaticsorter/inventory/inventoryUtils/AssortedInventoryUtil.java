package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.AssortedInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class AssortedInventoryUtil implements IInventoryUtil {

    private static final String MAIN_CLASSNANE = "com.grim3212.assorted.storage.common.block.blockentity.BaseStorageBlockEntity";
    public static final String STORAGE_HANDLER_CLASSNAME = "com.grim3212.assorted.lib.core.inventory.impl.ItemStackStorageHandler";

    public IInventoryAdapter getInventoryAdapter(World world, BlockPos pos, Block block, BlockEntity blockEntity) {
        try {
            Class<?> itemStackStorageHandlerClass = Class.forName(STORAGE_HANDLER_CLASSNAME);
            Object itemStackStorageHandler = getAssortedStorageItemStackStorageHandler(blockEntity);
            if (itemStackStorageHandler == null) {
                return new NoInventoryAdapter();
            }
            Method getSlotsMethod = itemStackStorageHandlerClass.getMethod("getSlots");
            int slots = (int) getSlotsMethod.invoke(itemStackStorageHandler);
            Method getStackMethod = itemStackStorageHandlerClass.getMethod("getStackInSlot", int.class);
            ArrayList<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < slots; i++) {
                ItemStack itemStack = (ItemStack) getStackMethod.invoke(itemStackStorageHandler, i);
                itemStacks.add(itemStack);
            }

            return new AssortedInventoryAdapter(itemStacks, blockEntity);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException ignored) {

        }

        return new NoInventoryAdapter();
    }

    public boolean isRelatedStorage(Block block, BlockEntity blockEntity) {
        try {
            Class<?> chestBlockClass = Class.forName(MAIN_CLASSNANE);
            return chestBlockClass.isInstance(blockEntity);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    public static Object getAssortedStorageItemStackStorageHandler(BlockEntity blockEntity) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> chestBlockClass = Class.forName(MAIN_CLASSNANE);
        if (chestBlockClass.isInstance(blockEntity)) {
            Object chestBlockEntity = chestBlockClass.cast(blockEntity);
            Method isLockedMethod = chestBlockClass.getMethod("isLocked");
            boolean isLocked = (boolean) isLockedMethod.invoke(chestBlockEntity);
            if (isLocked) {
                return null;
            }

            Method getContainerMethod = chestBlockClass.getMethod("getStorageHandler");
            Object lockedItemStackStorageHandler = getContainerMethod.invoke(chestBlockEntity);
            Class<?> itemStackStorageHandlerClass = Class.forName(STORAGE_HANDLER_CLASSNAME);
            Class<?> fabricPlatformInvClass = Class.forName("com.grim3212.assorted.lib.inventory.FabricPlatformInventoryStorageHandlerUnsided");
            if (fabricPlatformInvClass.isInstance(lockedItemStackStorageHandler)) {
                Object fabricPlatformInventory = fabricPlatformInvClass.cast(lockedItemStackStorageHandler);
                Method getItemStorageHandlerMethod = fabricPlatformInvClass.getDeclaredMethod("getItemStorageHandler", Direction.class);
                Object storageHandler = getItemStorageHandlerMethod.invoke(fabricPlatformInventory, (Direction) null);

                return itemStackStorageHandlerClass.cast(storageHandler);
            }
        }

        throw new ClassNotFoundException("Class not found: " + STORAGE_HANDLER_CLASSNAME);
    }
}
