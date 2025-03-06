package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
public class ModBlockEntities {
    public static final BlockEntityType<SorterControllerBlockEntity> SORTER_CONTROLLER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(AutomaticSorter.MOD_ID, "sorter_controller_be"),
                    FabricBlockEntityTypeBuilder.create(SorterControllerBlockEntity::create, ModBlocks.SORTER_CONTROLLER_BLOCK).build(null));

    public static final BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(AutomaticSorter.MOD_ID, "filter_be"),
                    FabricBlockEntityTypeBuilder.create(FilterBlockEntity::create, ModBlocks.FILTER_BLOCK).build(null));

    public static void registerModBlocksEntities() {
        AutomaticSorter.LOGGER.info("Registering ModBlocksEntities for " + AutomaticSorter.MOD_ID);
    }
}