package cz.lukesmith.automaticsortermod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import cz.lukesmith.automaticsortermod.AutomaticSorterMod;
import cz.lukesmith.automaticsortermod.block.entity.FilterBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FilterScreen extends HandledScreen<FilterScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(AutomaticSorterMod.MOD_ID, "textures/gui/filter.png");
    private ButtonWidget receiveItemsButton;

    public FilterScreen(FilterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;

        receiveItemsButton = ButtonWidget.builder(Text.of(""), button -> {
            int value = handler.toggleFilterType();
            sendFilterTypeUpdate(value);
        }).dimensions(this.x + 6, this.y + 14, 17, 17).build();

        this.addDrawableChild(receiveItemsButton);
    }

    private Text getButtonText() {
        return Text.of(FilterBlockEntity.FilterTypeEnum.getName(handler.getFilterType()));
    }

    private void sendFilterTypeUpdate(int value) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(value);
        ClientPlayNetworking.send(new Identifier("automaticsortermod", "update_receive_items"), buf);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawTooltip(textRenderer, getButtonText(), mouseX, mouseY);
        }

        if (!receiveItemsButton.isMouseOver(mouseX, mouseY)) {
            receiveItemsButton.setFocused(false);
        }
    }
}