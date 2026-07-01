package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AutomaticSorter.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SorterControllerBlockEntity>> SORTER_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sorter_controller_be",
                    () -> BlockEntityType.Builder.of(SorterControllerBlockEntity::new, ModBlocks.SORTER_CONTROLLER_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterBlockEntity>> FILTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("filter_be",
                    () -> BlockEntityType.Builder.of(FilterBlockEntity::new, ModBlocks.FILTER_BLOCK.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
