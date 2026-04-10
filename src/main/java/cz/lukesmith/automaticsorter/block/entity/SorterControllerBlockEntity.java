package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.inventory.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.InventoryUtils;
import cz.lukesmith.automaticsorter.inventory.NoInventoryAdapter;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SorterControllerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

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
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return this.pos;
    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;
    }

    private static boolean tryWhitelistMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter, IInventoryAdapter filterInventoryAdapter) {
        if (rootInventoryAdapter.isEmpty()) {
            return false;
        }

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            ItemStack containItemStack = filterInventoryAdapter.containsItem(rootInventoryItemStack);
            if (!containItemStack.isEmpty()) {
                if (chestInventoryAdapter.addItem(rootInventoryItemStack)) {
                    rootInventoryAdapter.removeItem(i, 1);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryInInventoryMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter) {
        if (rootInventoryAdapter.isEmpty()) {
            return false;
        }

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            ItemStack containItemStack = chestInventoryAdapter.containsItem(rootInventoryItemStack);
            if (!containItemStack.isEmpty()) {
                if (chestInventoryAdapter.addItem(rootInventoryItemStack)) {
                    rootInventoryAdapter.removeItem(i, 1);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryRejectsMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter) {
        if (rootInventoryAdapter.isEmpty()) {
            return false;
        }

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            if (chestInventoryAdapter.addItem(rootInventoryItemStack)) {
                rootInventoryAdapter.removeItem(i, 1);
                return true;
            }
        }

        return false;
    }

    private static boolean tryToTransferItem(World world, FilterBlockEntity filterBlockEntity, BlockPos filterPos, IInventoryAdapter rootInventoryAdapter) {
        Direction filterDirection = world.getBlockState(filterPos).get(FilterBlock.FACING);
        BlockPos chestPos = filterPos.offset(filterDirection);
        IInventoryAdapter chestInventoryAdapter = InventoryUtils.getInventoryAdapter(world, chestPos);
        if (chestInventoryAdapter instanceof NoInventoryAdapter) {
            return false;
        }

        int filterType = filterBlockEntity.getFilterType();
        return switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
            case WHITELIST -> {
                IInventoryAdapter filterInventoryAdapter = InventoryUtils.getInventoryAdapter(world, filterPos);
                yield tryWhitelistMode(rootInventoryAdapter, chestInventoryAdapter, filterInventoryAdapter);
            }
            case IN_INVENTORY -> tryInInventoryMode(rootInventoryAdapter, chestInventoryAdapter);
            case REJECTS -> tryRejectsMode(rootInventoryAdapter, chestInventoryAdapter);
            default -> false;
        };

    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return;
        }

        if (ticker > 0) {
            ticker--;
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        BlockPos belowPos = pos.down();

        if ((world.getBlockState(belowPos).getBlock() instanceof PipeBlock)) {
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(belowPos);

            IInventoryAdapter rootInventoryAdapter = InventoryUtils.getInventoryAdapter(world, pos.up());
            boolean noInventoryRootChest = rootInventoryAdapter instanceof NoInventoryAdapter;
            boolean itemTransfered = false;
            ArrayList<FilterBlockEntity> rejectedFilters = new ArrayList<>();
            boolean isRootInventoryEmpty = rootInventoryAdapter.isEmpty();

            while (!queue.isEmpty() && !itemTransfered && !noInventoryRootChest && !isRootInventoryEmpty) {
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

                            if (tryToTransferItem(world, filterBlockEntity, neighborPos, rootInventoryAdapter)) {
                                itemTransfered = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!itemTransfered) {
                for (FilterBlockEntity filterBlockEntity : rejectedFilters) {
                    if (tryToTransferItem(world, filterBlockEntity, filterBlockEntity.getPos(), rootInventoryAdapter)) {
                        break;
                    }
                }
            }
        }

        ticker = MAX_TICKER;
    }
}