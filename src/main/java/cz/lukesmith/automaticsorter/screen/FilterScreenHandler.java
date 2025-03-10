package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
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
        checkSize(((Inventory) blockEntity), 24);
        this.inventory = ((Inventory) blockEntity);
        inventory.onOpen(playerInventory.player);
        this.blockEntity = ((FilterBlockEntity) blockEntity);
        this.propertyDelegate = propertyDelegate;

        // Pridat sloty
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                this.addSlot(new Slot(this.inventory, j + i * 8, 26 + j * 18, 15 + i * 18));
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(propertyDelegate);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
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
