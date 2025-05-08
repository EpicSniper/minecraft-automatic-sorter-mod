package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public interface IInventoryAdapter {

    ItemStack containsItem(ItemStack itemStack);

    void removeItem(int index, int amount);

    int addItem(ItemStack itemStack, int maxAmount);

    ArrayList<ItemStack> getAllStacks();

    int getSize();

    default boolean isEmpty() {
        return getAllStacks().isEmpty();
    }

    default boolean compareStacks(ItemStack insertingItem, ItemStack compareItem) {
        if (insertingItem.isEnchantable()) {
            return ItemStack.areItemsEqual(insertingItem, compareItem);
        }

        return ItemStack.areItemsAndComponentsEqual(insertingItem, compareItem);
    }

    default boolean canCombineStacks(ItemStack insertingItem, ItemStack compareItem) {
        return ItemStack.areItemsAndComponentsEqual(insertingItem, compareItem);
    }
}
