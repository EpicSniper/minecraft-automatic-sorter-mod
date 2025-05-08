package cz.lukesmith.automaticsorter.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.block.entity.SorterControllerBlockEntity;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryUtils.MainInventoryUtil;
import cz.lukesmith.automaticsorter.network.ModConfigSyncPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import cz.lukesmith.automaticsorter.config.ModConfig;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModCommands {

    private static final File EXPORT_FILE = new File("config/automatic_sorter_export.json");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("as_basespeed")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                    .executes(ctx -> {
                        double value = DoubleArgumentType.getDouble(ctx, "value");
                        ModConfig.get().baseSortingSpeed = value;
                        ModConfig.save();
                        ctx.getSource().sendFeedback(() -> Text.translatable("message.automaticsorter.basespeed_set", value), true);
                        sendSyncConfigPacketToAllPlayers(ctx);

                        return Command.SINGLE_SUCCESS;
                    })));

            dispatcher.register(CommandManager.literal("as_basespeedboostperupgrade")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                    .executes(ctx -> {
                        double value = DoubleArgumentType.getDouble(ctx, "value");
                        ModConfig.get().baseSpeedBoostPerUpgrade = value;
                        ModConfig.save();
                        ctx.getSource().sendFeedback(() -> Text.translatable("message.automaticsorter.basespeedboost_set", value), true);
                        sendSyncConfigPacketToAllPlayers(ctx);

                        return Command.SINGLE_SUCCESS;
                    })));

            dispatcher.register(CommandManager.literal("as_instantsort")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                    .executes(ctx -> {
                        boolean value = BoolArgumentType.getBool(ctx, "value");
                        ModConfig.get().instantSort = value;
                        ModConfig.save();
                        ctx.getSource().sendFeedback(() -> Text.translatable("message.automaticsorter.instantsort_set", value ? "true" : "false"), true);
                        sendSyncConfigPacketToAllPlayers(ctx);

                        return Command.SINGLE_SUCCESS;
                    })));

            dispatcher.register(CommandManager.literal("as_exportModBlocks")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("radius", IntegerArgumentType.integer())
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        ServerWorld world = source.getWorld();
                        BlockPos origin = source.getPlayer().getBlockPos();
                        int radius = Math.min(IntegerArgumentType.getInteger(ctx, "radius"), 1024);
                        JsonArray blocks = new JsonArray();
                        int objectsDetected = 0;
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    BlockPos pos = origin.add(x, y, z);
                                    BlockState state = world.getBlockState(pos);
                                    Block block = state.getBlock();
                                    Identifier id = Registries.BLOCK.getId(block);
                                    BlockEntity blockEntity = world.getBlockEntity(pos);
                                    if (id.getNamespace().equals("automaticsorter")) { // tvůj mod
                                        JsonObject obj = new JsonObject();
                                        obj.addProperty("x", x);
                                        obj.addProperty("y", y);
                                        obj.addProperty("z", z);
                                        obj.addProperty("block", id.toString());
                                        obj.addProperty("properties", state.toString());

                                        if (blockEntity instanceof SorterControllerBlockEntity || blockEntity instanceof FilterBlockEntity) {
                                            JsonArray inventory = new JsonArray();
                                            Inventory inv = (Inventory) blockEntity;
                                            for (int i = 0; i < inv.size(); i++) {
                                                ItemStack stack = inv.getStack(i);
                                                if (!stack.isEmpty()) {
                                                    JsonObject itemObj = new JsonObject();
                                                    itemObj.addProperty("slot", i);
                                                    itemObj.addProperty("item", Registries.ITEM.getId(stack.getItem()).toString());
                                                    itemObj.addProperty("count", stack.getCount());
                                                    inventory.add(itemObj);
                                                }
                                            }

                                            if (blockEntity instanceof FilterBlockEntity filterBlockEntity) {
                                                obj.addProperty("filterType", filterBlockEntity.getFilterType());
                                            }

                                            obj.add("inventory", inventory);
                                            if (block instanceof SorterControllerBlock) {
                                                Direction direction = world.getBlockState(pos).get(SorterControllerBlock.FACING);
                                                pos = pos.offset(direction);
                                            } else if (block instanceof FilterBlock) {
                                                pos = pos.offset(world.getBlockState(pos).get(FilterBlock.FACING));
                                            }

                                            IInventoryAdapter inventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, pos);

                                            JsonObject jsonIAdapter = new JsonObject();
                                            state = world.getBlockState(pos);
                                            jsonIAdapter.addProperty("block", Registries.BLOCK.getId(state.getBlock()).toString());
                                            jsonIAdapter.addProperty("properties", state.toString());

                                            JsonArray adapterInventory = new JsonArray();
                                            for (int i = 0; i < inventoryAdapter.getAllStacks().size(); i++) {
                                                ItemStack stack = inventoryAdapter.getAllStacks().get(i);
                                                if (!stack.isEmpty()) {
                                                    JsonObject itemObj = new JsonObject();
                                                    itemObj.addProperty("slot", i);
                                                    itemObj.addProperty("item", Registries.ITEM.getId(stack.getItem()).toString());
                                                    itemObj.addProperty("count", stack.getCount());
                                                    adapterInventory.add(itemObj);
                                                }
                                            }

                                            jsonIAdapter.add("inventory", adapterInventory);
                                            obj.add("inventoryAdapter", jsonIAdapter);
                                        }

                                        blocks.add(obj);

                                        objectsDetected++;
                                    }
                                }
                            }
                        }
                        JsonObject root = new JsonObject();
                        root.add("blocks", blocks);
                        Path file = EXPORT_FILE.toPath();
                        try {
                            Files.writeString(file, root.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        int finalObjectsDetected = objectsDetected;
                        source.sendFeedback(() -> Text.translatable("message.automaticsorter.objects_detected", finalObjectsDetected), false);

                        return Command.SINGLE_SUCCESS;
                })));

            if (AutomaticSorter.isDevEnvironment()) {
                dispatcher.register(CommandManager.literal("as_importModBlocks")
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        ServerWorld world = source.getWorld();
                        BlockPos origin = source.getPlayer().getBlockPos();
                        Path file = EXPORT_FILE.toPath();
                        JsonObject root = null;
                        try {
                            root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        JsonArray blocks = root.getAsJsonArray("blocks");

                        for (JsonElement el : blocks) {
                            JsonObject obj = el.getAsJsonObject();
                            int x = obj.get("x").getAsInt();
                            int y = obj.get("y").getAsInt();
                            int z = obj.get("z").getAsInt();
                            BlockPos pos = origin.add(x, y, z);

                            Identifier id = Identifier.of(obj.get("block").getAsString());
                            Block block = Registries.BLOCK.get(id);
                            BlockState state = parseBlockState(block, obj.get("properties").getAsString());
                            world.setBlockState(pos, state);

                            BlockEntity be = world.getBlockEntity(pos);
                            if (be instanceof Inventory && obj.has("inventory")) {
                                Inventory inv = (Inventory) be;
                                JsonArray items = obj.getAsJsonArray("inventory");
                                int slot = 0;
                                for (JsonElement itemEl : items) {
                                    JsonObject itemObj = itemEl.getAsJsonObject();
                                    ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(itemObj.get("item").getAsString())), itemObj.get("count").getAsInt());
                                    if (slot < inv.size()) inv.setStack(slot++, stack);
                                }
                            }
                            if (be instanceof FilterBlockEntity && obj.has("filterType")) {
                                ((FilterBlockEntity) be).setFilterType(obj.get("filterType").getAsInt());
                            }

                            if (obj.has("inventoryAdapter")) {
                                JsonObject adapter = obj.getAsJsonObject("inventoryAdapter");
                                // inventory adapter má jenom Filter a SorterController
                                // pozice je vždy facing Filteru nebo Sorteru
                                BlockPos adapterPos;
                                if (block instanceof SorterControllerBlock) {
                                    Direction direction = world.getBlockState(pos).get(SorterControllerBlock.FACING);
                                    adapterPos = pos.offset(direction);
                                } else if (block instanceof FilterBlock) {
                                    Direction direction = world.getBlockState(pos).get(FilterBlock.FACING);
                                    adapterPos = pos.offset(direction);
                                } else {
                                    continue; // nemá smysl pokračovat
                                }

                                Identifier adapterId = Identifier.of(adapter.get("block").getAsString());
                                Block adapterBlock = Registries.BLOCK.get(adapterId);
                                BlockState adapterState = parseBlockState(adapterBlock, adapter.get("properties").getAsString());
                                world.setBlockState(adapterPos, adapterState);

                                // naplnit inventář adapteru
                                BlockEntity adapterBe = world.getBlockEntity(adapterPos);
                                if (adapterBe instanceof Inventory && adapter.has("inventory")) {
                                    Inventory inv = (Inventory) adapterBe;
                                    JsonArray items = adapter.getAsJsonArray("inventory");
                                    int slot = 0;
                                    for (JsonElement itemEl : items) {
                                        JsonObject itemObj = itemEl.getAsJsonObject();
                                        ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(itemObj.get("item").getAsString())), itemObj.get("count").getAsInt());
                                        if (slot < inv.size()) inv.setStack(slot++, stack);
                                    }
                                }
                            }
                        }
                        source.sendFeedback(() -> Text.literal("Import hotov!"), false);
                        return Command.SINGLE_SUCCESS;
                    })
                );
            }
        });
    }

    private static BlockState parseBlockState(Block block, String propertiesString) {
        BlockState state = block.getDefaultState();
        // Najdi část v hranatých závorkách
        int start = propertiesString.indexOf('[');
        int end = propertiesString.indexOf(']');
        if (start != -1 && end != -1 && end > start) {
            String props = propertiesString.substring(start + 1, end);
            String[] pairs = props.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length != 2) continue;
                String key = kv[0].trim();
                String value = kv[1].trim();
                // Nastav property podle typu
                if (state.getProperties().stream().anyMatch(p -> p.getName().equals(key))) {
                    var property = state.getProperties().stream().filter(p -> p.getName().equals(key)).findFirst().get();
                    if (property.getType() == Boolean.class) {
                        state = state.with((net.minecraft.state.property.Property<Boolean>) property, Boolean.parseBoolean(value));
                    } else if (property.getType() == Direction.class) {
                        state = state.with((net.minecraft.state.property.Property<Direction>) property, Direction.valueOf(value.toUpperCase()));
                    } else if (property.getType() == Integer.class) {
                        state = state.with((net.minecraft.state.property.Property<Integer>) property, Integer.parseInt(value));
                    }
                }
            }
        }
        return state;
    }


    private static void sendSyncConfigPacketToAllPlayers(CommandContext<ServerCommandSource> ctx) {
        ModConfig config = ModConfig.get();
        ModConfigSyncPayload payload = new ModConfigSyncPayload(
                config.baseSortingSpeed,
                config.baseSpeedBoostPerUpgrade,
                config.instantSort
        );

        for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
