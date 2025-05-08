package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class MultiInventoryAdapter implements IInventoryAdapter {

    private ArrayList<IInventoryAdapter> inventoryAdapters;

    public MultiInventoryAdapter(IInventoryAdapter inventoryAdapters) {
        this.inventoryAdapters = new ArrayList<>();
        this.inventoryAdapters.add(inventoryAdapters);
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (IInventoryAdapter inventoryAdapter : inventoryAdapters) {
            ItemStack stack = inventoryAdapter.containsItem(itemStack);
            if (!stack.isEmpty()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        int i = 0;
        for (IInventoryAdapter inventoryAdapter : inventoryAdapters) {
            if (index < i + inventoryAdapter.getSize()) {
                inventoryAdapter.removeItem(index - i, amount);
                return;
            }

            i += inventoryAdapter.getSize();
        }
    }

    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        int toTransfer = Math.min(maxAmount, itemStack.getCount());
        int transferred = 0;
        for (IInventoryAdapter inventoryAdapter : inventoryAdapters) {
            int added = inventoryAdapter.addItem(itemStack, toTransfer - transferred);
            transferred += added;
            if (transferred >= toTransfer) {
                break;
            }
        }

        return transferred;
    }

    public void addInventoryAdapter(IInventoryAdapter inventoryAdapter) {
        inventoryAdapters.add(inventoryAdapter);
    }

    public void addInventoryAdapterAsFirst(IInventoryAdapter inventoryAdapter) {
        inventoryAdapters.add(0, inventoryAdapter);
    }

    public void setInventoryAdapters(ArrayList<IInventoryAdapter> inventoryAdapters) {
        this.inventoryAdapters = inventoryAdapters;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (IInventoryAdapter inventoryAdapter : inventoryAdapters) {
            stacks.addAll(inventoryAdapter.getAllStacks());
        }

        return stacks;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (IInventoryAdapter inventoryAdapter : inventoryAdapters) {
            size += inventoryAdapter.getSize();
        }
        return size;
    }
}
