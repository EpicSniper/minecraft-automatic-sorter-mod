package cz.lukesmith.automaticsortermod.block.entity;

import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import cz.lukesmith.automaticsortermod.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<SorterControllerBlockEntity> SORTER_CONTROLLER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(AutomaticSorterMod.MOD_ID, "sorter_controller_be"),
                    BlockEntityType.Builder.create(SorterControllerBlockEntity::create, ModBlocks.SORTER_CONTROLLER_BLOCK).build(null));

    public static final BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(AutomaticSorterMod.MOD_ID, "filter_be"),
                    BlockEntityType.Builder.create(FilterBlockEntity::create, ModBlocks.FILTER_BLOCK).build(null));

    public static void registerModBlocksEntities() {
        AutomaticSorterMod.LOGGER.info("Registering ModBlocksEntities for " + AutomaticSorterMod.MOD_ID);
    }
}