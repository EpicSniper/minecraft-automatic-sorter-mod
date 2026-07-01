package cz.lukesmith.automaticsorter;

import com.mojang.logging.LogUtils;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsorter.item.ModItemGroups;
import cz.lukesmith.automaticsorter.item.ModItems;
import cz.lukesmith.automaticsorter.network.NetworkHandler;
import cz.lukesmith.automaticsorter.screen.FilterScreen;
import cz.lukesmith.automaticsorter.screen.ModScreenHandlers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

@Mod(AutomaticSorter.MOD_ID)
public class AutomaticSorter {
    public static final String MOD_ID = "automaticsorter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AutomaticSorter(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(NetworkHandler::register);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItemGroups.register(modEventBus);
        ModScreenHandlers.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FILTER_BLOCK_ENTITY.get(),
                (FilterBlockEntity filterBlockEntity, net.minecraft.core.Direction side) -> filterBlockEntity.getInventory()
        );
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModScreenHandlers.FILTER_SCREEN_HANDLER.get(), FilterScreen::new);
        }
    }
}
