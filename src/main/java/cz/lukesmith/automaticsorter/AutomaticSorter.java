package cz.lukesmith.automaticsorter;

import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsorter.command.ModCommands;
import cz.lukesmith.automaticsorter.item.ModItemGroups;
import cz.lukesmith.automaticsorter.item.ModItems;
import cz.lukesmith.automaticsorter.network.NetworkHandler;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
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
        ModCommands.register();
        NetworkHandler.register();
        NetworkHandler.registerServer();
        NetworkHandler.sendWhenJoin();
    }

    public static boolean isDevEnvironment() {
        try {
            String env = System.getProperty("fabric.development");
            return env != null && env.equals("true");
        } catch (Exception e) {
            return false;
        }
    }
}