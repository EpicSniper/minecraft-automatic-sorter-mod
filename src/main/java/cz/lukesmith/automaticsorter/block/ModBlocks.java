package cz.lukesmith.automaticsorter.block;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block PIPE_BLOCK = registerBlock("pipe", new PipeBlock(
            FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool().strength(1.0f, 2.0f).nonOpaque()
    ));
    public static final Block SORTER_CONTROLLER_BLOCK = registerBlock("sorter_controller", new SorterControllerBlock(
            FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool().strength(3.0f, 6.0f).nonOpaque()
    ));
    public static final Block FILTER_BLOCK = registerBlock("filter", new FilterBlock(
            FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool().strength(3.0f, 6.0f).nonOpaque()
    ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(AutomaticSorter.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(AutomaticSorter.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        AutomaticSorter.LOGGER.info("Registering ModBlocks for " + AutomaticSorter.MOD_ID);
    }
}
