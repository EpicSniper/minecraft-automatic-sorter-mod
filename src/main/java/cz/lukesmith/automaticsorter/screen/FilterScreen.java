package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.network.FilterTypePacket;
import cz.lukesmith.automaticsorter.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collections;
import java.util.List;

public class FilterScreen extends AbstractContainerScreen<FilterScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "textures/gui/filter.png");
    private Button receiveItemsButton;
    private static final ItemStack CHEST_BLOCK = new ItemStack(Blocks.CHEST);
    private static final ItemStack FILTER_BLOCK = new ItemStack(ModBlocks.FILTER_BLOCK.get());
    private static final ItemStack REJECTS = new ItemStack(Blocks.BARRIER);


    public FilterScreen(FilterScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    private Component getButtonText() {
        return Component.translatable(FilterBlockEntity.FilterTypeEnum.getName(menu.getFilterType()));
    }

    private List<FormattedText> getButtonTextList() {
        return List.of(getButtonText());
    }

    @Override
    protected void init() {
        super.init();
        inventoryLabelY = 1000;
        titleLabelY = 1000;

        receiveItemsButton = new Button.Builder(Component.literal(""), button -> {
            int value = menu.toggleFilterType();
            BlockPos blockPos = menu.getBlockPos();
            FilterTypePacket payload = new FilterTypePacket(blockPos, value);
            NetworkHandler.CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
        }).pos(this.leftPos + 6, this.topPos + 14).size(18, 18).build();

        this.addRenderableWidget(receiveItemsButton);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
        if (this.receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            context.renderComponentTooltip(getFont(), getButtonTextList(), mouseX, mouseY, ItemStack.EMPTY);
        }

        if (!this.receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            this.receiveItemsButton.setFocused(false);
        }

        int filterType = menu.getFilterType();
        ItemStack renderButtonBlock = getDisplayedItemStack();
        context.renderItem(renderButtonBlock, this.leftPos + 7, this.topPos + 15);

        switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
            case REJECTS:
            case IN_INVENTORY:
                int x = this.leftPos + 25;
                int y = this.topPos + 14;
                int width = 8 * 18;
                int height = 3 * 18;
                int color = 0x80000000;
                context.fill(x, y, x + width, y + height, color);
                break;
            default:
                break;
        }
    }

    private ItemStack getDisplayedItemStack() {
        int filterType = menu.getFilterType();
        if (filterType == FilterBlockEntity.FilterTypeEnum.WHITELIST.getValue()) {
            return FILTER_BLOCK;
        } else if (filterType == FilterBlockEntity.FilterTypeEnum.IN_INVENTORY.getValue()) {
            return CHEST_BLOCK;
        } else {
            return REJECTS;
        }
    }
}