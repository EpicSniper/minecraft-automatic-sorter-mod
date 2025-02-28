package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.screen.FilterScreen;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AutomaticSorterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.FILTER_SCREEN_HANDLER, FilterScreen::new);
    }
}
