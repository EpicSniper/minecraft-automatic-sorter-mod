package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
    public static final ScreenHandlerType<FilterScreenHandler> FILTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(AutomaticSorter.MOD_ID, "gem_polishing"),
                    new ExtendedScreenHandlerType<>(FilterScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void registerScreenHandlers() {
        AutomaticSorter.LOGGER.info("Registering Screen Handlers for " + AutomaticSorter.MOD_ID);
    }
}
