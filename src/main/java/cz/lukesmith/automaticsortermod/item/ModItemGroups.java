package cz.lukesmith.automaticsortermod.item;

import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup AUTOMATIC_SORTER_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(AutomaticSorterMod.MOD_ID, "automatic_sorter_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.automatic_sorter_group"))
                    .icon(() -> new ItemStack(ModItems.TEST_ITEM)).entries((displayContext, entries) -> {
                        entries.add(ModItems.TEST_ITEM);
                    }).build());

    public static void registerItemGroups() {
        AutomaticSorterMod.LOGGER.info("Registering item groups for " + AutomaticSorterMod.MOD_ID);
    }
}
