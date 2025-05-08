package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
    public static final ScreenHandlerType<FilterScreenHandler> FILTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(AutomaticSorter.MOD_ID, "filter"),
                    new ExtendedScreenHandlerType<>(FilterScreenHandler::new, BlockPos.PACKET_CODEC));

    public static final ScreenHandlerType<SorterControllerScreenHandler> SORTER_CONTROLLER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(AutomaticSorter.MOD_ID, "sorter_controller"),
                    new ExtendedScreenHandlerType<>(SorterControllerScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void registerScreenHandlers() {
        AutomaticSorter.LOGGER.info("Registering Screen Handlers for " + AutomaticSorter.MOD_ID);
    }

    public static void registerClientScreenHandlers() {
        AutomaticSorter.LOGGER.info("Registering Client Screen Handlers for " + AutomaticSorter.MOD_ID);
        HandledScreens.register(ModScreenHandlers.FILTER_SCREEN_HANDLER, FilterScreen::new);
        HandledScreens.register(ModScreenHandlers.SORTER_CONTROLLER_SCREEN_HANDLER, SorterControllerScreen::new);
    }
}
