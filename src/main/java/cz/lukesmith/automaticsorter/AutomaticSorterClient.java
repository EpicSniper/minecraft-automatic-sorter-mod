package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.network.NetworkHandler;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;

public class AutomaticSorterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModScreenHandlers.registerClientScreenHandlers();
        NetworkHandler.registerClient();
    }
}
