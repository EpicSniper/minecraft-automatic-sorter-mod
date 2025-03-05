package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SorterControllerBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    private int ticker = 0;
    private static final int MAX_TICKER = 5;

    public SorterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    public static SorterControllerBlockEntity create(BlockPos pos, BlockState state) {
        return new SorterControllerBlockEntity(pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return;
        }

        if (ticker > 0) {
            ticker--;
            return;
        }

        Set<BlockPos> connectedPipes = findConnectedPipes(world, pos);
        Set<BlockPos> connectedFilters = findConnectedFilters(world, connectedPipes);

        BlockPos rootChestPos = pos.up();

        Inventory rootChestInventory = getInventoryFromPosition(world, rootChestPos);
        if (rootChestInventory != null) {
            for (BlockPos filterPos : connectedFilters) {
                Direction filterDirection = world.getBlockState(filterPos).get(FilterBlock.FACING);
                BlockPos chestPos = filterPos.offset(filterDirection);
                Inventory inventory = getInventoryFromPosition(world, chestPos);
                if (inventory != null) {
                    BlockEntity filterEntity = world.getBlockEntity(filterPos);
                    if (filterEntity instanceof FilterBlockEntity filterBlockEntity) {
                        int filterType = filterBlockEntity.getFilterType();
                        boolean itemTransfered;
                        switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
                            case WHITELIST:
                                itemTransfered = transferWhitelistItem(rootChestInventory, inventory, filterBlockEntity);
                                break;
                            case IN_INVENTORY:
                                itemTransfered = transferCommonItem(rootChestInventory, inventory);
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + FilterBlockEntity.FilterTypeEnum.fromValue(filterType));
                        }

                        if (itemTransfered) {
                            break;
                        }
                    }
                }
            }
        }

        ticker = MAX_TICKER;
    }

    @Nullable
    private static Inventory getInventoryFromPosition(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (block instanceof ChestBlock chestBlock) {
            return ChestBlock.getInventory(chestBlock, world.getBlockState(pos), world, pos, true);
        } else if (blockEntity instanceof BarrelBlockEntity barrelBlockEntity) {
            return barrelBlockEntity;
        }

        return null;
    }

    private static boolean transferWhitelistItem(Inventory from, Inventory to, FilterBlockEntity filterBlockEntity) {
        for (int i = 0; i < from.size(); i++) {
            ItemStack stack = from.getStack(i);
            if (!stack.isEmpty()) {
                ItemStack singleItem = stack.split(1); // Remove one item from the current stack
                if (filterBlockEntity.isItemInInventory(singleItem)) {
                    ItemStack remaining = transferItem(to, singleItem);

                    if (remaining.isEmpty()) {
                        from.setStack(i, stack);
                        return true; // Item was successfully transferred
                    } else {
                        stack.increment(1); // Revert the split if the item was not transferred
                        from.setStack(i, stack);
                    }
                } else {
                    stack.increment(1); // Revert the split if the item is not in the whitelist
                    from.setStack(i, stack);
                }
            }
        }

        return false;
    }

    public static boolean transferCommonItem(Inventory from, Inventory to) {
        for (int i = 0; i < from.size(); i++) {
            ItemStack stack = from.getStack(i);
            if (!stack.isEmpty() && containsItem(to, stack)) {
                ItemStack singleItem = stack.split(1); // Remove one item from the current stack
                ItemStack remaining = transferItem(to, singleItem);

                if (remaining.isEmpty()) {
                    from.setStack(i, stack);
                    return true; // Item was successfully transferred
                } else {
                    stack.increment(1); // Revert the split if the item was not transferred
                    from.setStack(i, stack);
                }
            }
        }
        return false;
    }

    private static boolean containsItem(Inventory inventory, ItemStack item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (ItemStack.areItemsEqual(inventory.getStack(i), item)) {
                return true;
            }
        }

        return false;
    }

    private static ItemStack transferItem(Inventory to, ItemStack item) {
        to.size();
        for (int i = 0; i < to.size(); i++) {
            ItemStack stackInSlot = to.getStack(i);
            if (stackInSlot.isEmpty()) {
                to.setStack(i, item);
                return ItemStack.EMPTY;
            } else if (ItemStack.areItemsEqual(stackInSlot, item)) {
                int transferAmount = Math.min(item.getCount(), stackInSlot.getMaxCount() - stackInSlot.getCount());
                stackInSlot.increment(transferAmount);
                item.decrement(transferAmount);
                if (item.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return item;
    }

    private static Set<BlockPos> findConnectedPipes(World world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        BlockPos belowPos = startPos.down();

        if (!(world.getBlockState(belowPos).getBlock() instanceof PipeBlock)) {
            return visited;
        }

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(belowPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            if (!visited.contains(currentPos)) {
                visited.add(currentPos);

                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.offset(direction);
                    if (world.getBlockState(neighborPos).getBlock() instanceof PipeBlock) {
                        queue.add(neighborPos);
                    }
                }
            }
        }

        return visited;
    }

    private static Set<BlockPos> findConnectedFilters(World world, Set<BlockPos> pipePositions) {
        Set<BlockPos> filterPositions = new HashSet<>();

        for (BlockPos pipePos : pipePositions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pipePos.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof FilterBlock) {
                    Direction filterFacing = neighborState.get(FilterBlock.FACING);
                    if (direction == filterFacing) {
                        filterPositions.add(neighborPos);
                    }
                }
            }
        }

        return filterPositions;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return this.pos;
    }
}