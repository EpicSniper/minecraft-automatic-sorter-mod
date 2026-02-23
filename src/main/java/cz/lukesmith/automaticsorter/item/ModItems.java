package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AutomaticSorter.MOD_ID);

    public static final RegistryObject<Item> SORTER_AMPLIFIER = registerItem("sorter_amplifier", SorterAmplifierItem::new);

    public static RegistryObject<Item> registerItem(String name, Function<Item.Properties, Item> function) {
        return ModItems.ITEMS.register(name, () -> function.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, name)))));
    }

    public static void register(BusGroup eventBus) {
        ITEMS.register(eventBus);
    }
}
