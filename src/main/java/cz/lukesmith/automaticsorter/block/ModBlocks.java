package cz.lukesmith.automaticsorter.block;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block PIPE_BLOCK = registerBlock("pipe",
            new PipeBlock(Block.Settings.copy(Blocks.COPPER_BLOCK)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, "pipe")))
                    .requiresTool().strength(1.0f, 2.0f).nonOpaque()
    ));
    public static final Block SORTER_CONTROLLER_BLOCK = registerBlock("sorter_controller",
            new SorterControllerBlock(Block.Settings.copy(Blocks.COPPER_BLOCK)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, "sorter_controller")))
                    .requiresTool().strength(3.0f, 6.0f).nonOpaque()
    ));
    public static final Block FILTER_BLOCK = registerBlock("filter",
            new FilterBlock(Block.Settings.copy(Blocks.COPPER_BLOCK)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, "filter")))
                    .requiresTool().strength(3.0f, 6.0f).nonOpaque()
    ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(AutomaticSorter.MOD_ID, name),
                new BlockItem(block, new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(AutomaticSorter.MOD_ID, name))).useBlockPrefixedTranslationKey()));
    }

    public static void registerModBlocks() {
        AutomaticSorter.LOGGER.info("Registering ModBlocks for " + AutomaticSorter.MOD_ID);
    }
}
