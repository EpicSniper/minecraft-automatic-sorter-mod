package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.config.ModConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class NetworkHandler {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FilterTypePayload.ID, FilterTypePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FilterTypePayload.ID, FilterTypePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModConfigSyncPayload.ID, ModConfigSyncPayload.CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(FilterTypePayload.ID, (payload, context) -> {});
        ClientPlayNetworking.registerGlobalReceiver(ModConfigSyncPayload.ID, (payload, context) -> {
            payload.applyClient();
        });
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(FilterTypePayload.ID, (payload, context) -> {
            BlockPos blockPos = payload.blockPos();
            int filterType = payload.filterType();
            context.server().execute(() -> {
                if (context.player().getWorld().getBlockEntity(blockPos) instanceof FilterBlockEntity filterBlockEntity) {
                    filterBlockEntity.setFilterType(filterType);
                    filterBlockEntity.markDirty();
                }
            });
        });
    }

    public static void sendWhenJoin() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            NetworkHandler.sendConfigTo(handler.player);
        });
    }

    public static void sendConfigTo(ServerPlayerEntity player) {
        var config = ModConfig.get();
        var payload = new ModConfigSyncPayload(
                config.baseSortingSpeed,
                config.baseSpeedBoostPerUpgrade,
                config.instantSort
        );
        ServerPlayNetworking.send(player, payload);
    }
}
