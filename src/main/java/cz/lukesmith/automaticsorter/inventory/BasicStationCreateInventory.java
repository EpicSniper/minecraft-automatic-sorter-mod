package cz.lukesmith.automaticsorter.inventory;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class BasicStationCreateInventory implements IInventoryAdapter {
    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        return null;
    }

    @Override
    public void removeItem(int index, int amount) {

    }

    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        return 0;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return IInventoryAdapter.super.isEmpty();
    }

    @Override
    public boolean compareStacks(ItemStack insertingItem, ItemStack compareItem) {
        return IInventoryAdapter.super.compareStacks(insertingItem, compareItem);
    }

    @Override
    public boolean canCombineStacks(ItemStack insertingItem, ItemStack compareItem) {
        return IInventoryAdapter.super.canCombineStacks(insertingItem, compareItem);
    }
}
