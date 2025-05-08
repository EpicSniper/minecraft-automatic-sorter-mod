package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.config.ModConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Synchronizuje config ze serveru na klienta.
 */
public record ModConfigSyncPayload(double baseSortingSpeed, double baseSpeedBoostPerUpgrade, boolean instantSort)
        implements CustomPayload {

    public static final String NAME = "config_sync";
    public static final CustomPayload.Id<ModConfigSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(AutomaticSorter.MOD_ID, NAME));

    public static final PacketCodec<PacketByteBuf, ModConfigSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, ModConfigSyncPayload::baseSortingSpeed,
            PacketCodecs.DOUBLE, ModConfigSyncPayload::baseSpeedBoostPerUpgrade,
            PacketCodecs.BOOLEAN, ModConfigSyncPayload::instantSort,
            ModConfigSyncPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void applyClient() {
        ModConfig config = ModConfig.get();
        config.baseSortingSpeed = this.baseSortingSpeed;
        config.baseSpeedBoostPerUpgrade = this.baseSpeedBoostPerUpgrade;
        config.instantSort = this.instantSort;
    }
}
