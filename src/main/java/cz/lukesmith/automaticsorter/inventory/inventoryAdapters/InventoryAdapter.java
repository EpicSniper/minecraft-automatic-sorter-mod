package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class InventoryAdapter implements IInventoryAdapter {

    private final Inventory inventory;

    public InventoryAdapter(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int size = inventory.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (this.compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        inventory.removeStack(index, amount);
    }

    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        int size = inventory.size();
        int toTransfer = Math.min(maxAmount, itemStack.getCount());
        int transferred = 0;
        for (int i = 0; i < size && transferred < toTransfer; i++) {
            ItemStack stack = inventory.getStack(i);
            if (this.canCombineStacks(stack, itemStack) && stack.getCount() < stack.getMaxCount()) {
                int canAdd = Math.min(toTransfer - transferred, stack.getMaxCount() - stack.getCount());
                stack.increment(canAdd);
                transferred += canAdd;
                inventory.markDirty();
            } else if (stack.isEmpty()) {
                int put = toTransfer - transferred;
                ItemStack newStack = itemStack.copyWithCount(put);
                inventory.setStack(i, newStack);
                transferred += put;
                inventory.markDirty();
            }
        }
        return transferred;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int size = inventory.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            stacks.add(stack);
        }

        return stacks;
    }

    @Override
    public int getSize() {
        return inventory.size();
    }
}
