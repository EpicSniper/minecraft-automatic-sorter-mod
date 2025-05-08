package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.block.entity.SorterControllerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class SorterControllerScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    public final SorterControllerBlockEntity blockEntity;
    private final int inventorySize = 1;


    public SorterControllerScreenHandler(int syncId, PlayerInventory inventory, BlockPos pos) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(pos), new ArrayPropertyDelegate(1));
    }

    public SorterControllerScreenHandler(int syncId, PlayerInventory playerInventory,
                                         BlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.SORTER_CONTROLLER_SCREEN_HANDLER, syncId);
        checkSize(((Inventory) blockEntity), inventorySize);
        this.inventory = ((Inventory) blockEntity);
        inventory.onOpen(playerInventory.player);
        this.blockEntity = ((SorterControllerBlockEntity) blockEntity);

        this.addSlot(new Slot(this.inventory, 0, 80, 33));

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

    public String getSpeedBoostText() {
        double speed = blockEntity.getSpeedPerSecond();
        if (speed == Double.MAX_VALUE) {
            return "∞";
        }

        return String.format("%.2f", speed);
    }
}
