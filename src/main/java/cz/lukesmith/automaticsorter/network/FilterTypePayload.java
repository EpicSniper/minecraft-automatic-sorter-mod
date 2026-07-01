package cz.lukesmith.automaticsorter.network;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FilterTypePayload(BlockPos blockPos, int filterType) implements CustomPacketPayload {
    public static final Type<FilterTypePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "filter_type_change"));

    public static final StreamCodec<ByteBuf, FilterTypePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FilterTypePayload::blockPos,
            ByteBufCodecs.VAR_INT,
            FilterTypePayload::filterType,
            FilterTypePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FilterTypePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            if (level.getBlockEntity(payload.blockPos()) instanceof FilterBlockEntity filterBlockEntity) {
                filterBlockEntity.setFilterType(payload.filterType());
                filterBlockEntity.setChanged();
            }
        });
    }
}
