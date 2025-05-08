package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import cz.lukesmith.automaticsorter.inventory.inventoryUtils.BasicStorageInventoryUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BasicCrateInventoryAdapter implements IInventoryAdapter {

    private Object crateSlot;
    private Object component;

    public BasicCrateInventoryAdapter(Object crateSlot) {
        this.crateSlot = crateSlot;
        this.component = getComponent();
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (this.isComponentInicialized()) {
            try {
                Class<?> componentClass = this.component.getClass();
                Method itemMethod = componentClass.getMethod("item");
                Object itemComponent = itemMethod.invoke(this.component);
                if (itemComponent instanceof Item item) {
                    ItemStack itemStackToCheck = new ItemStack(item, itemStack.getCount());
                    if (ItemStack.areItemsAndComponentsEqual(itemStack, itemStackToCheck)) {
                        return itemStack;
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        if (this.isComponentInicialized()) {
            try (Transaction transaction = Transaction.openOuter()) {
                Class<?> componentClass = this.component.getClass();
                Method itemMethod = componentClass.getMethod("item");
                Object itemComponent = itemMethod.invoke(this.component);
                if (itemComponent instanceof Item item) {
                    ItemVariant itemVariant = ItemVariant.of(item);
                    Method extractMethod = this.crateSlot.getClass().getMethod("extract", ItemVariant.class, long.class, Transaction.class);
                    extractMethod.invoke(this.crateSlot, itemVariant, amount, transaction);
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }
    }

    // TODO: maxAmount a return
    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        if (this.isComponentInicialized()) {
            try {
                Class<?> componentClass = this.component.getClass();
                Method itemMethod = componentClass.getMethod("item");
                Object itemComponent = itemMethod.invoke(this.component);
                if (itemComponent instanceof Item item) {
                    ItemVariant itemVariant = ItemVariant.of(item);
                    try (Transaction transaction = Transaction.openOuter()) {
                        Method insertMethod = this.crateSlot.getClass().getMethod("insert", ItemVariant.class, long.class, Transaction.class);
                        long insertedAmount = (long) insertMethod.invoke(this.crateSlot, itemVariant, 1, transaction);
                        if (insertedAmount > 0) {
                            return 1;
                        }
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }

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

    private Object getComponent() {
        try {
            Class<?> basicStorageChestClass = Class.forName(BasicStorageInventoryUtil.CRATE_SLOT_CLASS_NAME);
            if (basicStorageChestClass.isInstance(this.crateSlot)) {
                Object basicStorageChest = basicStorageChestClass.cast(this.crateSlot);
                Method getComponentMethod = basicStorageChestClass.getMethod("toComponent");
                return getComponentMethod.invoke(basicStorageChest);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }
        return null;
    }

    private boolean isComponentInicialized() {
        return this.component != null;
    }
}
