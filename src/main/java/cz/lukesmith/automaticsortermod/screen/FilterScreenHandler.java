package cz.lukesmith.automaticsortermod.screen;

import cz.lukesmith.automaticsortermod.block.entity.FilterBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FilterScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    public final FilterBlockEntity blockEntity;
    private final PropertyDelegate propertyDelegate;


    public FilterScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), new ArrayPropertyDelegate(1));
    }

    public FilterScreenHandler(int syncId, PlayerInventory playerInventory,
                               BlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.FILTER_SCREEN_HANDLER, syncId);
        checkSize(((Inventory) blockEntity), 9);
        this.inventory = ((Inventory) blockEntity);
        inventory.onOpen(playerInventory.player);
        this.blockEntity = ((FilterBlockEntity) blockEntity);
        this.propertyDelegate = propertyDelegate;

        // Pridat sloty
        this.addSlot(new Slot(inventory, 0, 62, 15));
        this.addSlot(new Slot(inventory, 1, 80, 15));
        this.addSlot(new Slot(inventory, 2, 98, 15));
        this.addSlot(new Slot(inventory, 3, 62, 33));
        this.addSlot(new Slot(inventory, 4, 80, 33));
        this.addSlot(new Slot(inventory, 5, 98, 33));
        this.addSlot(new Slot(inventory, 6, 62, 51));
        this.addSlot(new Slot(inventory, 7, 80, 51));
        this.addSlot(new Slot(inventory, 8, 98, 51));


        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(propertyDelegate);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getFilterType() {
        return propertyDelegate.get(0);
    }

    public int toggleFilterType() {
        int value = FilterBlockEntity.FilterTypeEnum.nextValue(this.getFilterType());
        propertyDelegate.set(0, value);
        return value;
    }
}
