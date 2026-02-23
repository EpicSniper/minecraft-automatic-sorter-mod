package cz.lukesmith.automaticsorter.block;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import cz.lukesmith.automaticsorter.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import java.util.function.Function;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AutomaticSorter.MOD_ID);

    public static final RegistryObject<Block> PIPE_BLOCK = registerBlock("pipe",
            (properties) -> new PipeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "pipe")))
                    .strength(1.0f, 2.0f).noOcclusion()));

    public static final RegistryObject<Block> SORTER_CONTROLLER_BLOCK = registerBlock("sorter_controller",
            (properties) -> new SorterControllerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "sorter_controller")))
                    .strength(3.0f, 6.0f).noOcclusion()));

    public static final RegistryObject<Block> FILTER_BLOCK = registerBlock("filter",
            (properties) -> new FilterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "filter")))
                    .strength(3.0f, 6.0f).noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        RegistryObject<T> toReturn = BLOCKS.register(name,
                () -> function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, name)))));
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, name))).useBlockDescriptionPrefix()));
    }

    public static void register(BusGroup eventBus) {
        BLOCKS.register(eventBus);
    }
}
