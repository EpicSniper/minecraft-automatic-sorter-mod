package cz.lukesmith.automaticsortermod.screen;

import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<FilterScreenHandler> FILTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(AutomaticSorterMod.MOD_ID, "gem_polishing"),
                    new ExtendedScreenHandlerType<>(FilterScreenHandler::new));

    public static void registerScreenHandlers() {
        AutomaticSorterMod.LOGGER.info("Registering Screen Handlers for " + AutomaticSorterMod.MOD_ID);
    }
}
