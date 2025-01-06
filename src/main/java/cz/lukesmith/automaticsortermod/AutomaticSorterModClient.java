package cz.lukesmith.automaticsortermod;

import cz.lukesmith.automaticsortermod.screen.FilterScreen;
import cz.lukesmith.automaticsortermod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AutomaticSorterModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.FILTER_SCREEN_HANDLER, FilterScreen::new);
    }
}
