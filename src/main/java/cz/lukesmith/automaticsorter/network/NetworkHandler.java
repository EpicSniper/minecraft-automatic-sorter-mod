package cz.lukesmith.automaticsorter.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(FilterTypePayload.TYPE, FilterTypePayload.STREAM_CODEC, FilterTypePayload::handle);
    }
}
