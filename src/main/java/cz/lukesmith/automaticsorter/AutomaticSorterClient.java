package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.network.FilterTypePayload;
import cz.lukesmith.automaticsorter.screen.FilterScreen;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AutomaticSorterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.FILTER_SCREEN_HANDLER, FilterScreen::new);

        PayloadTypeRegistry.playS2C().register(FilterTypePayload.ID, FilterTypePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(FilterTypePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                AutomaticSorter.LOGGER.info("Received packet");
            });
        });
    }
}
