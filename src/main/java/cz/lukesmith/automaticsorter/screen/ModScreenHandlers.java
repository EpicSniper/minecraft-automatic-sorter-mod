package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModScreenHandlers {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AutomaticSorter.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<FilterScreenHandler>> FILTER_SCREEN_HANDLER =
            MENUS.register("filter_screen",
                    () -> IMenuTypeExtension.create(FilterScreenHandler::new));


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
