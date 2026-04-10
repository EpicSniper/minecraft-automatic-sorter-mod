package cz.lukesmith.automaticsorter.inventory;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public interface IInventoryAdapter {

    ItemStack containsItem(ItemStack itemStack);

    void removeItem(int index, int amount);

    boolean addItem(ItemStack itemStack);

    ArrayList<ItemStack> getAllStacks();

    int getSize();

    default boolean isEmpty() {
        for (ItemStack stack : getAllStacks()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
