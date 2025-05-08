package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SorterControllerScreen extends HandledScreen<SorterControllerScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of(AutomaticSorter.MOD_ID, "textures/gui/sorter_controller.png");

    public SorterControllerScreen(SorterControllerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight, 176, 166);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);

        String upgradeText = Text.translatable("automaticsorter.screen_text.sorting_speed").getString() + ": " + handler.getSpeedBoostText();
        int textWidth = textRenderer.getWidth(upgradeText);
        int x = (backgroundWidth - textWidth) / 2;
        context.drawText(this.textRenderer, upgradeText, x, 20, -12566464, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}