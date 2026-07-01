package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemGroups {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AutomaticSorter.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AUTOMATIC_SORTER_GROUP = CREATIVE_MODE_TABS.register("automatic_sorter_group",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.automatic_sorter_group"))
                    .icon(() -> new ItemStack(ModBlocks.PIPE_BLOCK.get()))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.PIPE_BLOCK.get());
                        output.accept(ModBlocks.SORTER_CONTROLLER_BLOCK.get());
                        output.accept(ModBlocks.FILTER_BLOCK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
