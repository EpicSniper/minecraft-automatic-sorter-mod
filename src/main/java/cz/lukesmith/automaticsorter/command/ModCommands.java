package cz.lukesmith.automaticsorter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import cz.lukesmith.automaticsorter.config.ModConfig;
import cz.lukesmith.automaticsorter.network.NetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("as_basespeed")
                //.requires(src -> src.permissions(2))
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                        .executes(ctx -> {
                            double value = DoubleArgumentType.getDouble(ctx, "value");
                            ModConfig config = ModConfig.get();
                            config.baseSortingSpeed = value;
                            ModConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.translatable("message.automaticsorter.basespeed_set", value), true);
                            sendSyncConfigPacketToAllPlayers(ctx);
                            return Command.SINGLE_SUCCESS;
                        })));

        event.getDispatcher().register(Commands.literal("as_basespeedboostperupgrade")
                //.requires(src -> src.hasPermission(2))
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                        .executes(ctx -> {
                            double value = DoubleArgumentType.getDouble(ctx, "value");
                            ModConfig config = ModConfig.get();
                            config.baseSpeedBoostPerUpgrade = value;
                            ModConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.translatable("message.automaticsorter.basespeedboost_set", value), true);
                            sendSyncConfigPacketToAllPlayers(ctx);
                            return Command.SINGLE_SUCCESS;
                        })));

        event.getDispatcher().register(Commands.literal("as_instantsort")
                //.requires(src -> src.hasPermission(2))
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean value = BoolArgumentType.getBool(ctx, "value");
                            ModConfig config = ModConfig.get();
                            config.instantSort = value;
                            ModConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.translatable("message.automaticsorter.instantsort_set", value ? "true" : "false"), true);
                            sendSyncConfigPacketToAllPlayers(ctx);
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private static void sendSyncConfigPacketToAllPlayers(CommandContext<CommandSourceStack> ctx) {
        for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
            NetworkHandler.sendConfigTo(player);
        }
    }
}
