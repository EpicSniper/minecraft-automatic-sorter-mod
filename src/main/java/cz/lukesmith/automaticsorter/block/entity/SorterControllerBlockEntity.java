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
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SorterControllerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    private int ticker = 0;
    private static final int MAX_TICKER = 5;
    private double overflow = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public SorterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    public static SorterControllerBlockEntity create(BlockPos pos, BlockState state) {
        return new SorterControllerBlockEntity(pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, this.inventory);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return this.pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.automaticsorter.sorter_controller");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SorterControllerScreenHandler(syncId, playerInventory, this.pos);
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

    private static int tryToTransferItem(World world, FilterBlockEntity filterBlockEntity, BlockPos filterPos, IInventoryAdapter rootInventoryAdapter, int maxTransfer) {
        Direction filterDirection = world.getBlockState(filterPos).get(FilterBlock.FACING);
        BlockPos chestPos = filterPos.offset(filterDirection);
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
        if (!this.getStack(0).isEmpty() && this.getStack(0).getItem().equals(ModItems.SORTER_AMPLIFIER)) {
            return this.getStack(0).getCount();
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

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
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
        Direction facing = world.getBlockState(pos).get(SorterControllerBlock.FACING);
        BlockPos nextPos = pos.offset(facing.getOpposite());

        if ((world.getBlockState(nextPos).getBlock() instanceof PipeBlock)) {
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(nextPos);

            IInventoryAdapter rootInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, pos.offset(facing));
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
                    BlockPos neighborPos = currentPos.offset(direction);
                    Block block = world.getBlockState(neighborPos).getBlock();
                    if (block instanceof PipeBlock) {
                        queue.add(neighborPos);
                    } else if (block instanceof FilterBlock) {
                        BlockEntity filterEntity = world.getBlockEntity(neighborPos);
                        if (filterEntity instanceof FilterBlockEntity filterBlockEntity) {
                            Direction filterFacing = world.getBlockState(neighborPos).get(FilterBlock.FACING);
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
                    itemsLeftToTransfer = tryToTransferItem(world, filterBlockEntity, filterBlockEntity.getPos(), rootInventoryAdapter, itemsLeftToTransfer);
                    if (itemsLeftToTransfer >= 0) {
                        break;
                    }
                }
            }
        }

        ticker = MAX_TICKER;
    }
}