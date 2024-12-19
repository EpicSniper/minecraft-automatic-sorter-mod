package cz.lukesmith.automaticsortermod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TestEntityBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public TestEntityBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEST_ENTITY_BLOCK_ENTITY, pos, state);
    }

    public static TestEntityBlockEntity create(BlockPos pos, BlockState state) {
        return new TestEntityBlockEntity(pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {

    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;    // 22:37
    }

    public static void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return; // Tick metoda se neprovádí na klientské straně
        }

        // Detekuj blok nad a pod tímto blokem
        BlockPos abovePos = pos.up(); // Blok nad
        BlockPos belowPos = pos.down(); // Blok pod

        // Získání entity bloku nad a pod
        BlockEntity aboveEntity = world.getBlockEntity(abovePos);
        BlockEntity belowEntity = world.getBlockEntity(belowPos);

        // Zkontroluj, zda nad je inventář
        if (aboveEntity instanceof Inventory aboveInventory) {
            // Zkontroluj, zda pod je inventář
            if (belowEntity instanceof Inventory belowInventory) {
                // Přesuň itemy z horního inventáře do spodního
                transferItems(aboveInventory, belowInventory);
            }
        }
    }

    private static void transferItems(Inventory from, Inventory to) {
        // Prochází všechny sloty v horním inventáři
        for (int i = 0; i < from.size(); i++) {
            ItemStack stack = from.getStack(i);
            if (!stack.isEmpty()) {
                // Přidej stack do dolního inventáře
                ItemStack remaining = addToInventory(to, stack);
                from.setStack(i, remaining); // Aktualizuj zbývající stack
            }
        }
    }

    private static ItemStack addToInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (slotStack.isEmpty()) {
                // Přidej celý stack do prázdného slotu
                inventory.setStack(i, stack);
                return ItemStack.EMPTY;
            } else if (ItemStack.canCombine(slotStack, stack)) {
                // Přidej k existujícímu stacku
                int maxInsert = Math.min(stack.getCount(), slotStack.getMaxCount() - slotStack.getCount());
                slotStack.increment(maxInsert);
                stack.decrement(maxInsert);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack; // Zbývající itemy, pokud nebylo místo
    }
}
