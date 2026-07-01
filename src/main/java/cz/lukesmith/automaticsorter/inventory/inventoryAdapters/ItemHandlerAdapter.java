package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;

public class ItemHandlerAdapter implements IInventoryAdapter {
    private final IItemHandler itemHandler;

    public ItemHandlerAdapter(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        itemHandler.extractItem(index, amount, false);
    }

    @Override
    public boolean addItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }

        ItemStack transferStack = itemStack.copyWithCount(1);
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack remainder = itemHandler.insertItem(i, transferStack.copy(), false);
            if (remainder.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            stacks.add(itemHandler.getStackInSlot(i));
        }

        return stacks;
    }

    @Override
    public int getSize() {
        return itemHandler.getSlots();
    }
}
