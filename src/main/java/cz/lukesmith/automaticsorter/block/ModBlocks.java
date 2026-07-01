package cz.lukesmith.automaticsorter.block;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import cz.lukesmith.automaticsorter.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AutomaticSorter.MOD_ID);

    public static final DeferredHolder<Block, Block> PIPE_BLOCK = registerBlock("pipe",
            (properties) -> new PipeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .strength(1.0f, 2.0f).noOcclusion()));

    public static final DeferredHolder<Block, Block> SORTER_CONTROLLER_BLOCK = registerBlock("sorter_controller",
            (properties) -> new SorterControllerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .strength(3.0f, 6.0f).noOcclusion()));

    public static final DeferredHolder<Block, Block> FILTER_BLOCK = registerBlock("filter",
            (properties) -> new FilterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .strength(3.0f, 6.0f).noOcclusion()));


    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Function<BlockBehaviour.Properties, T> blockSupplier) {
        DeferredHolder<Block, T> toReturn = BLOCKS.register(name, () -> blockSupplier.apply(BlockBehaviour.Properties.of()));
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
