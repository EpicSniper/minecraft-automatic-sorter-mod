package cz.lukesmith.automaticsorter;

import com.mojang.logging.LogUtils;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsorter.command.ModCommands;
import cz.lukesmith.automaticsorter.item.ModItemGroups;
import cz.lukesmith.automaticsorter.item.ModItems;
import cz.lukesmith.automaticsorter.network.NetworkHandler;
import cz.lukesmith.automaticsorter.screen.FilterScreen;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import cz.lukesmith.automaticsorter.screen.SorterControllerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AutomaticSorter.MOD_ID)
public class AutomaticSorter {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "automaticsorter";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public AutomaticSorter(FMLJavaModLoadingContext context) {
        BusGroup modEventBus = context.getModBusGroup();

        // Common setup
        FMLCommonSetupEvent.getBus(modEventBus).addListener(this::commonSetup);

        // Creative tab population
        BuildCreativeModeTabContentsEvent.BUS.addListener(AutomaticSorter::addCreative);

        // ServerStartingEvent registration
        ServerStartingEvent.BUS.addListener(this::onServerStarting);

        // Mod registrations
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItemGroups.register(modEventBus);
        ModScreenHandlers.register(modEventBus);

        // Networking
        NetworkHandler.register();

        // Config
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModScreenHandlers.FILTER_SCREEN_HANDLER.get(), FilterScreen::new);
            MenuScreens.register(ModScreenHandlers.SORTER_CONTROLLER_SCREEN_HANDLER.get(), SorterControllerScreen::new);
            NetworkHandler.registerClient();
        }
    }
}
