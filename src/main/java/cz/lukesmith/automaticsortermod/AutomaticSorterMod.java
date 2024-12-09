package cz.lukesmith.automaticsortermod;

import cz.lukesmith.automaticsortermod.item.ModItemGroups;
import cz.lukesmith.automaticsortermod.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticSorterMod implements ModInitializer {
	public static final String MOD_ID = "automatic-sorter-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerItems();
		ModItemGroups.registerItemGroups();
	}
}