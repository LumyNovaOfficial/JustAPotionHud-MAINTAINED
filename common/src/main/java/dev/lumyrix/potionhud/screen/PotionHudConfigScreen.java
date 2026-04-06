package dev.lumyrix.potionhud.screen;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PotionHudConfigScreen extends Screen {

    private final Screen parent;
    private static final int W = 200;
    private static final int H = 20;

    public PotionHudConfigScreen(Screen parent) {
        super(Component.literal("Potion HUD Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        int cx = this.width / 2 - W / 2;
        int cy = this.height / 2 - 75;

        addRenderableWidget(new AbstractSliderButton(cx, cy, W, H,
                scaleLabel(cfg.getScale()), (cfg.getScale() - 0.5f) / 1.5f) {
            @Override protected void updateMessage() { setMessage(scaleLabel(toScale(this.value))); }
            @Override protected void applyValue()    { PotionHudConfig.getInstance().setScale(toScale(this.value)); }
        });

        addRenderableWidget(Button.builder(
                Component.literal(cfg.isEnabled() ? "Enabled" : "Disabled"),
                btn -> {
                    boolean now = !PotionHudConfig.getInstance().isEnabled();
                    PotionHudConfig.getInstance().setEnabled(now);
                    btn.setMessage(Component.literal(now ? "Enabled" : "Disabled"));
                }).pos(cx, cy + 25).size(W, H).build());

        addRenderableWidget(new AbstractSliderButton(cx, cy + 50, W, H,
                xLabel(cfg.getXFrac()), cfg.getXFrac()) {
            @Override protected void updateMessage() { setMessage(xLabel((float) this.value)); }
            @Override protected void applyValue()    { PotionHudConfig.getInstance().setXFrac((float) this.value); }
        });

        addRenderableWidget(new AbstractSliderButton(cx, cy + 75, W, H,
                yLabel(cfg.getYFrac()), cfg.getYFrac()) {
            @Override protected void updateMessage() { setMessage(yLabel((float) this.value)); }
            @Override protected void applyValue()    { PotionHudConfig.getInstance().setYFrac((float) this.value); }
        });

        addRenderableWidget(Button.builder(
                Component.literal("Flicker <6s: " + (cfg.isFlicker() ? "ON" : "OFF")),
                btn -> {
                    boolean now = !PotionHudConfig.getInstance().isFlicker();
                    PotionHudConfig.getInstance().setFlicker(now);
                    btn.setMessage(Component.literal("Flicker <6s: " + (now ? "ON" : "OFF")));
                }).pos(cx, cy + 100).size(W, H).build());

        addRenderableWidget(Button.builder(
                Component.literal("Reset to Defaults"),
                btn -> {
                    PotionHudConfig.getInstance().resetToDefaults();
                    PotionHudConfig.save();
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new PotionHudConfigScreen(this.parent));
                }).pos(cx, cy + 130).size(W, H).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                btn -> this.onClose())
                .pos(cx, cy + 155).size(W, H).build());
    }

    private static float toScale(double v) { return Math.round((0.5f + (float)v * 1.5f) * 100) / 100.0f; }
    private static Component scaleLabel(float s) { return Component.literal("Scale: " + String.format("%.2f", s)); }
    private static Component xLabel(float v) { return Component.literal("X: " + String.format("%.0f%%", v * 100)); }
    private static Component yLabel(float v) { return Component.literal("Y: " + String.format("%.0f%%", v * 100)); }

    @Override
    public void onClose() {
        PotionHudConfig.save();
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 95, 0xFFFFFFFF);
    }
}
