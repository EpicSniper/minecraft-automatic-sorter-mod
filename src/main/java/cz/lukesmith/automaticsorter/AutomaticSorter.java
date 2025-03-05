package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsorter.item.ModItemGroups;
import cz.lukesmith.automaticsorter.item.ModItems;
import cz.lukesmith.automaticsorter.network.FilterTypePayload;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticSorter implements ModInitializer {
    public static final String MOD_ID = "automaticsorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();

        ModItems.registerItems();
        ModBlocks.registerModBlocks();

        ModBlockEntities.registerModBlocksEntities();
        ModScreenHandlers.registerScreenHandlers();

        PayloadTypeRegistry.playC2S().register(FilterTypePayload.ID, FilterTypePayload.CODEC);

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
}