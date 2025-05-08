package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup AUTOMATIC_SORTER_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(AutomaticSorter.MOD_ID, "automatic_sorter_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.automatic_sorter_group"))
                    .icon(() -> new ItemStack(ModBlocks.PIPE_BLOCK)).entries((displayContext, entries) -> {
                        entries.add(ModBlocks.PIPE_BLOCK);
                        entries.add(ModBlocks.SORTER_CONTROLLER_BLOCK);
                        entries.add(ModBlocks.FILTER_BLOCK);
                        entries.add(ModItems.SORTER_AMPLIFIER);
                    }).build());

    public static void registerItemGroups() {
        AutomaticSorter.LOGGER.info("Registering item groups for " + AutomaticSorter.MOD_ID);
    }
}
