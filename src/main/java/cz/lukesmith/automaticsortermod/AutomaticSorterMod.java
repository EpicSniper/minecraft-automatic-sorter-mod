package cz.lukesmith.automaticsortermod;

import cz.lukesmith.automaticsortermod.block.ModBlocks;
import cz.lukesmith.automaticsortermod.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsortermod.item.ModItemGroups;
import cz.lukesmith.automaticsortermod.item.ModItems;
import net.fabricmc.api.ModInitializer;

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
	}
}