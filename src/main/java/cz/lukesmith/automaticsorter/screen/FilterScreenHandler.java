package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FilterScreenHandler extends AbstractContainerMenu {

    private final FilterBlockEntity blockEntity;
    private final Level level;
    private final ContainerData propertyDelegate;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int FILTER_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int FILTER_INVENTORY_SLOT_COUNT = 24;

    public FilterScreenHandler(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    public FilterScreenHandler(int containerId, Inventory playerInventory, BlockEntity be, ContainerData data) {
        super(ModScreenHandlers.FILTER_SCREEN_HANDLER.get(), containerId);

        if (!(be instanceof FilterBlockEntity filterBe))
            throw new IllegalStateException("Invalid block entity at container open");

        this.blockEntity = filterBe;
        this.level = playerInventory.player.level();
        this.propertyDelegate = data;

        // Přidej sloty z block entity
        ItemStackHandler inventory = blockEntity.getInventory(); // přístup k ItemStackHandler

        // Příklad – 3 řádky po 8 slotech = 24 slotů
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                int slot = j + i * 8;
                int x = 26 + j * 18;
                int y = 15 + i * 18;
                this.addSlot(new SlotItemHandler(inventory, slot, x, y));
            }
        }

        // Přidej hráčský inventář
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        this.addDataSlots(data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.FILTER_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index < VANILLA_SLOT_COUNT) {
            // Z hráče do blockEntity
            if (!moveItemStackTo(sourceStack, FILTER_INVENTORY_FIRST_SLOT_INDEX, FILTER_INVENTORY_FIRST_SLOT_INDEX + FILTER_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < FILTER_INVENTORY_FIRST_SLOT_INDEX + FILTER_INVENTORY_SLOT_COUNT) {
            // Z blockEntity do hráče
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    public int getFilterType() {
        return propertyDelegate.get(0);
    }

    public int toggleFilterType() {
        int value = FilterBlockEntity.FilterTypeEnum.nextValue(this.getFilterType());
        propertyDelegate.set(0, value);
        blockEntity.setChanged();
        return value;
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }
}
