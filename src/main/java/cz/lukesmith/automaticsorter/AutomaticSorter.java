package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsorter.item.ModItemGroups;
import cz.lukesmith.automaticsorter.item.ModItems;
import cz.lukesmith.automaticsorter.screen.FilterScreenHandler;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
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
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "update_receive_items"), (server, player, handler, buf, responseSender) -> {
            int value = buf.readInt();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof FilterScreenHandler screenHandler) {
                    screenHandler.blockEntity.setFilterType(value);
                    screenHandler.blockEntity.markDirty();
                }
            });
        });
    }
}