package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import cz.lukesmith.automaticsorter.config.ModConfig;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryUtils.MainInventoryUtil;
import cz.lukesmith.automaticsorter.item.ModItems;
import cz.lukesmith.automaticsorter.screen.SorterControllerScreenHandler;
import net.minecraft.core.*;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SorterControllerBlockEntity extends BlockEntity implements MenuProvider {

    private int ticker = 0;
    private static final int MAX_TICKER = 5;
    private double overflow = 0;
    private final ItemStackHandler inventory = new ItemStackHandler(1);

    public SorterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static SorterControllerBlockEntity create(BlockPos pos, BlockState state) {
        return new SorterControllerBlockEntity(pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void loadAdditional(ValueInput pInput) {
        super.loadAdditional(pInput);
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(pInput, items);

        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, items.get(i));
        }

        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput pOutput) {
        super.saveAdditional(pOutput);
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSlots(); i++) {
            items.set(i, inventory.getStackInSlot(i));
        }

        ContainerHelper.saveAllItems(pOutput, items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.automaticsorter.sorter_controller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SorterControllerScreenHandler(pContainerId, pPlayerInventory, this);
    }

    private static int tryWhitelistMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter, IInventoryAdapter filterInventoryAdapter, int maxTransfer) {
        if (rootInventoryAdapter.isEmpty()) {
            return maxTransfer;
        }

        int transferLeft = maxTransfer;
        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            ItemStack containItemStack = filterInventoryAdapter.containsItem(rootInventoryItemStack);
            if (!containItemStack.isEmpty()) {
                int transferedInStack = chestInventoryAdapter.addItem(rootInventoryItemStack, transferLeft);
                transferLeft -= transferedInStack;
                if (transferedInStack > 0) {
                    rootInventoryAdapter.removeItem(i, transferedInStack);
                    if (transferLeft <= 0) {
                        return 0;
                    }
                }
            }
        }

        return transferLeft;
    }

    private static int tryInInventoryMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter, int maxTransfer) {
        if (rootInventoryAdapter.isEmpty()) {
            return maxTransfer;
        }

        int transferLeft = maxTransfer;

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            ItemStack containItemStack = chestInventoryAdapter.containsItem(rootInventoryItemStack);
            if (!containItemStack.isEmpty()) {
                int transferedInStack = chestInventoryAdapter.addItem(rootInventoryItemStack, transferLeft);
                transferLeft -= transferedInStack;
                if (transferedInStack > 0) {
                    rootInventoryAdapter.removeItem(i, transferedInStack);
                    if (transferLeft <= 0) {
                        return 0;
                    }
                }
            }
        }

        return transferLeft;
    }

    private static int tryRejectsMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter, int maxTransfer) {
        if (rootInventoryAdapter.isEmpty()) {
            return maxTransfer;
        }

        int transferLeft = maxTransfer;

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            int transferedInStack = chestInventoryAdapter.addItem(rootInventoryItemStack, transferLeft);
            transferLeft -= transferedInStack;
            if (transferedInStack > 0) {
                rootInventoryAdapter.removeItem(i, transferedInStack);
                if (transferLeft <= 0) {
                    return 0;
                }
            }
        }

        return transferLeft;
    }

    private static int tryToTransferItem(Level world, FilterBlockEntity filterBlockEntity, BlockPos filterPos, IInventoryAdapter rootInventoryAdapter, int maxTransfer) {
        Direction filterDirection = world.getBlockState(filterPos).getValue(FilterBlock.FACING);
        Vec3i filterOffset = filterDirection.getUnitVec3i();
        BlockPos chestPos = filterPos.offset(filterOffset);
        IInventoryAdapter chestInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, chestPos);
        if (chestInventoryAdapter instanceof NoInventoryAdapter) {
            return maxTransfer;
        }

        int filterType = filterBlockEntity.getFilterType();
        return switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
            case WHITELIST -> {
                IInventoryAdapter filterInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, filterPos);
                yield tryWhitelistMode(rootInventoryAdapter, chestInventoryAdapter, filterInventoryAdapter, maxTransfer);
            }
            case IN_INVENTORY -> tryInInventoryMode(rootInventoryAdapter, chestInventoryAdapter, maxTransfer);
            case REJECTS -> tryRejectsMode(rootInventoryAdapter, chestInventoryAdapter, maxTransfer);
            default -> maxTransfer;
        };

    }


    private int getAmplifierCount() {
        if (!this.inventory.getStackInSlot(0).isEmpty() && this.inventory.getStackInSlot(0).getItem().equals(ModItems.SORTER_AMPLIFIER)) {
            return this.inventory.getStackInSlot(0).getCount();
        }

        return 0;
    }

    public double getSpeedPerSecond() {
        if (ModConfig.get().instantSort) {
            return Double.MAX_VALUE;
        }

        double baseSpeed = ModConfig.get().baseSortingSpeed;
        double speedBoost = getAmplifierCount() * ModConfig.get().baseSpeedBoostPerUpgrade;

        return baseSpeed + speedBoost;
    }

    public double getSpeedPerTick() {
        return getSpeedPerSecond() / 20.0;
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (world.isClientSide) {
            return;
        }

        if (ticker > 0) {
            ticker--;
            return;
        }

        double speed = getSpeedPerTick() * MAX_TICKER;
        int maxTransfer = (int) Math.floor(speed + overflow);
        overflow = (speed + overflow) - maxTransfer;

        if (ModConfig.get().instantSort && overflow != 0) {
            overflow = 0;
        }

        Set<BlockPos> visited = new HashSet<>();
        Direction facing = world.getBlockState(pos).getValue(SorterControllerBlock.FACING);
        BlockPos nextPos = pos.offset(facing.getOpposite().getUnitVec3i());

        if ((world.getBlockState(nextPos).getBlock() instanceof PipeBlock)) {
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(nextPos);

            IInventoryAdapter rootInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, pos.offset(facing.getUnitVec3i()));
            boolean noInventoryRootChest = rootInventoryAdapter instanceof NoInventoryAdapter;
            int itemsLeftToTransfer = maxTransfer;
            ArrayList<FilterBlockEntity> rejectedFilters = new ArrayList<>();

            while (!queue.isEmpty() && (itemsLeftToTransfer >= 0) && !noInventoryRootChest) {
                BlockPos currentPos = queue.poll();
                if (visited.contains(currentPos)) {
                    continue;
                }

                visited.add(currentPos);
                for (Direction direction : Direction.values()) {
                    Vec3i offsetDirection = direction.getUnitVec3i();
                    BlockPos neighborPos = currentPos.offset(offsetDirection);
                    Block block = world.getBlockState(neighborPos).getBlock();
                    if (block instanceof PipeBlock) {
                        queue.add(neighborPos);
                    } else if (block instanceof FilterBlock) {
                        BlockEntity filterEntity = world.getBlockEntity(neighborPos);
                        if (filterEntity instanceof FilterBlockEntity filterBlockEntity) {
                            Direction filterFacing = world.getBlockState(neighborPos).getValue(FilterBlock.FACING);
                            if (direction != filterFacing) {
                                continue;
                            }

                            if (filterBlockEntity.getFilterType() == FilterBlockEntity.FilterTypeEnum.REJECTS.getValue()) {
                                rejectedFilters.add(filterBlockEntity);
                                continue;
                            }

                            itemsLeftToTransfer = tryToTransferItem(world, filterBlockEntity, neighborPos, rootInventoryAdapter, itemsLeftToTransfer);
                            if (itemsLeftToTransfer == 0) {
                                break;
                            }
                        }
                    }
                }
            }

            if (itemsLeftToTransfer >= 0) {
                for (FilterBlockEntity filterBlockEntity : rejectedFilters) {
                    itemsLeftToTransfer = tryToTransferItem(world, filterBlockEntity, filterBlockEntity.getBlockPos(), rootInventoryAdapter, itemsLeftToTransfer);
                    if (itemsLeftToTransfer >= 0) {
                        break;
                    }
                }
            }
        }

        ticker = MAX_TICKER;
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput data, HolderLookup.Provider lookup) {
        super.onDataPacket(connection, data, lookup);
    }
}