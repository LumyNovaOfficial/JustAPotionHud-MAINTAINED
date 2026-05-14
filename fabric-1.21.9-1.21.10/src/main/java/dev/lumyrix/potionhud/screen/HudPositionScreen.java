package dev.lumyrix.potionhud.screen;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import dev.lumyrix.potionhud.render.PotionHudRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudPositionScreen extends Screen {
    private final Screen parent;
    private boolean showGuideLines = true;
    private boolean isDragging = false;
    private float dragOffsetX = 0, dragOffsetY = 0;
    private int cachedHudW = 80, cachedHudH = 36;
    private static final int SNAP = 8, LINE_COL = 0x55FFFFFF, LINE_SNAP = 0xAAFFFF44;

    public HudPositionScreen(Screen parent) {
        super(Component.translatable("potionhud.screen.position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("←"), btn -> onClose())
            .pos(3, 3).size(19, 19).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.back")));
        addRenderableWidget(Button.builder(
                Component.literal(showGuideLines ? "‖" : "—"),
                btn -> {
                    showGuideLines = !showGuideLines;
                    btn.setMessage(Component.literal(showGuideLines ? "‖" : "—"));
                })
            .pos(this.width - 22, 3).size(19, 19).build());
        addRenderableWidget(Button.builder(
                Component.translatable("potionhud.button.apply_leave"),
                btn -> {
                    PotionHudConfig.save();
                    onClose();
                })
            .pos(this.width / 2 - 50, this.height - 24).size(100, 18).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x44000000);

        if (showGuideLines) {
            PotionHudConfig cfg = PotionHudConfig.getInstance();
            float cx = cfg.getPosXFrac() * this.width + cachedHudW / 2.0f;
            float cy = cfg.getPosYFrac() * this.height + cachedHudH / 2.0f;
            boolean sh = Math.abs(cy - this.height / 2.0f) <= SNAP;
            boolean sv = Math.abs(cx - this.width / 2.0f) <= SNAP;
            g.fill(0, this.height / 2, this.width, this.height / 2 + 1, sh ? LINE_SNAP : LINE_COL);
            g.fill(this.width / 2, 0, this.width / 2 + 1, this.height, sv ? LINE_SNAP : LINE_COL);
        }

        int[] size = PotionHudRenderer.renderPreviewAndGetSize(g, this.width, this.height);
        if (size != null) { cachedHudW = size[0]; cachedHudH = size[1]; }

        g.drawCenteredString(this.font, Component.translatable("potionhud.position.hint"),
            this.width / 2, this.height - 40, 0xFFAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (super.mouseClicked(event, consumed)) return true;
        if (event.button() == 0) {
            isDragging = true;
            PotionHudConfig cfg = PotionHudConfig.getInstance();
            dragOffsetX = (float)(event.x() - cfg.getPosXFrac() * this.width);
            dragOffsetY = (float)(event.y() - cfg.getPosYFrac() * this.height);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (event.button() == 0 && isDragging) {
            move(event.x(), event.y());
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && isDragging) {
            move(event.x(), event.y());
            isDragging = false;
            PotionHudConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    private void move(double mx, double my) {
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        float nx = (float)(mx - dragOffsetX);
        float ny = (float)(my - dragOffsetY);
        if (showGuideLines) {
            float cx = nx + cachedHudW / 2.0f;
            float cy = ny + cachedHudH / 2.0f;
            if (Math.abs(cx - this.width / 2.0f) <= SNAP) nx = this.width / 2.0f - cachedHudW / 2.0f;
            if (Math.abs(cy - this.height / 2.0f) <= SNAP) ny = this.height / 2.0f - cachedHudH / 2.0f;
        }
        cfg.setPosXFrac(nx / this.width);
        cfg.setPosYFrac(ny / this.height);
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}
    @Override public void onClose() { assert minecraft != null; minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
