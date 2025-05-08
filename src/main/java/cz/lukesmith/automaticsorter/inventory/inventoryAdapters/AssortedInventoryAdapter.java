package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import cz.lukesmith.automaticsorter.inventory.inventoryUtils.AssortedInventoryUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class AssortedInventoryAdapter implements IInventoryAdapter {

    private final ArrayList<ItemStack> itemStacks;

    private final BlockEntity blockEntity;

    public AssortedInventoryAdapter(ArrayList<ItemStack> itemStacks, BlockEntity blockEntity) {
        this.itemStacks = itemStacks;
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (ItemStack stack : itemStacks) {
            if (compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        extractItem(index, amount);
    }

    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        int size = itemStacks.size();
        int toTransfer = Math.min(maxAmount, itemStack.getCount());
        int transferred = 0;
        for (int i = 0; i < size && transferred < toTransfer; i++) {
            ItemStack stack = itemStacks.get(i);
            if (canCombineStacks(stack, itemStack) && stack.getCount() < stack.getMaxCount()) {
                int canAdd = Math.min(toTransfer - transferred, stack.getMaxCount() - stack.getCount());
                ItemStack addStack = itemStack.copyWithCount(canAdd);
                insertItem(i, addStack);
                transferred += canAdd;
            } else if (stack.isEmpty()) {
                int put = toTransfer - transferred;
                ItemStack newStack = itemStack.copyWithCount(put);
                insertItem(i, newStack);
                transferred += put;
            }
        }
        return transferred;
    }


    @Override
    public ArrayList<ItemStack> getAllStacks() {
        return itemStacks;
    }

    @Override
    public int getSize() {
        return itemStacks.size();
    }

    private void extractItem(int index, int amount) {
        try {
            Object storageHandler = AssortedInventoryUtil.getAssortedStorageItemStackStorageHandler(this.blockEntity);
            Class<?> itemStackStorageHandlerClass = Class.forName(AssortedInventoryUtil.STORAGE_HANDLER_CLASSNAME);
            Method insertItemMethod = itemStackStorageHandlerClass.getDeclaredMethod("extractItem", int.class, int.class, boolean.class);
            insertItemMethod.invoke(storageHandler, index, amount, false);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertItem(int index, ItemStack itemstack) {
        try {
            Object storageHandler = AssortedInventoryUtil.getAssortedStorageItemStackStorageHandler(this.blockEntity);
            Class<?> itemStackStorageHandlerClass = Class.forName(AssortedInventoryUtil.STORAGE_HANDLER_CLASSNAME);
            Method insertItemMethod = itemStackStorageHandlerClass.getDeclaredMethod("insertItem", int.class, ItemStack.class, boolean.class);
            insertItemMethod.invoke(storageHandler, index, itemstack, false);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
