package cz.lukesmith.automaticsortermod;

import cz.lukesmith.automaticsortermod.block.ModBlocks;
import cz.lukesmith.automaticsortermod.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsortermod.item.ModItemGroups;
import cz.lukesmith.automaticsortermod.item.ModItems;
import cz.lukesmith.automaticsortermod.screen.FilterScreenHandler;
import cz.lukesmith.automaticsortermod.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticSorterMod implements ModInitializer {
    public static final String MOD_ID = "automaticsortermod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();

        ModItems.registerItems();
        ModBlocks.registerModBlocks();

        ModBlockEntities.registerModBlocksEntities();
        ModScreenHandlers.registerScreenHandlers();
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("automaticsortermod", "update_receive_items"), (server, player, handler, buf, responseSender) -> {
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