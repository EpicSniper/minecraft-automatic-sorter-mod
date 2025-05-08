package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class NoInventoryAdapter implements IInventoryAdapter {
    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        // No operation
    }

    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        return 0;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        return new ArrayList<>();
    }

    @Override
    public int getSize() {
        return 0;
    }
}
