package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.config.ModConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

@Mod.EventBusSubscriber
public class NetworkHandler {
    private static final String CHANNEL_NAME = "main";
    public static final SimpleChannel CHANNEL = ChannelBuilder.named(Identifier.tryBuild(AutomaticSorter.MOD_ID, CHANNEL_NAME))
            .simpleChannel();

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(FilterTypePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FilterTypePacket::encode)
                .decoder(FilterTypePacket::decode)
                .consumer(FilterTypePacket::handle)
                .add();

        CHANNEL.messageBuilder(ModConfigSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ModConfigSyncPacket::encode)
                .decoder(ModConfigSyncPacket::decode)
                .consumerMainThread(ModConfigSyncPacket::handle)
                .add();
    }

    public static void registerClient() {
        /*CHANNEL.messageBuilder(ModConfigSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ModConfigSyncPacket::encode)
                .decoder(ModConfigSyncPacket::decode)
                .consumerMainThread(ModConfigSyncPacket::handle)
                .add();*/
    }

    public static void sendConfigTo(ServerPlayer player) {
        var config = ModConfig.get();
        var packet = new ModConfigSyncPacket(
                config.baseSortingSpeed,
                config.baseSpeedBoostPerUpgrade,
                config.instantSort
        );

        System.out.println("Sending config to player " + player.getName().getString() +
                ": baseSortingSpeed=" + config.baseSortingSpeed +
                ", baseSpeedBoostPerUpgrade=" + config.baseSpeedBoostPerUpgrade +
                ", instantSort=" + config.instantSort);
        NetworkHandler.CHANNEL.send(packet, player.connection.getConnection());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendConfigTo(player);
        }
    }
}
