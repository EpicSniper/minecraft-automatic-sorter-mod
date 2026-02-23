package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SorterControllerScreen extends AbstractContainerScreen<SorterControllerScreenHandler> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "textures/gui/sorter_controller.png");

    public SorterControllerScreen(SorterControllerScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelY = 1000;
        inventoryLabelY = 1000;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 176, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        String upgradeText = Component.translatable("automaticsorter.screen_text.sorting_speed")
                .getString() + ": " + menu.getSpeedBoostText();

        int textWidth = this.font.width(upgradeText);
        int x = (this.imageWidth - textWidth) / 2;

        // Pozor: souřadnice jsou relativní vůči levému hornímu rohu GUI (0,0)
        guiGraphics.drawString(this.font, upgradeText, x, 20, -12566464, false);
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}