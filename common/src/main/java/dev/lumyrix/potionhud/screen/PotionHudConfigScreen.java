package dev.lumyrix.potionhud.screen;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import dev.lumyrix.potionhud.preset.PresetManager;
import dev.lumyrix.potionhud.render.PotionHudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

public class PotionHudConfigScreen extends Screen {

    private final Screen parent;
    private static final int W     = 220;
    private static final int BW    = 106;
    private static final int H     = 20;
    private static final int TAB_H = 22;

    // 0=General  1=HUD  2=Presets
    private int     activeTab              = 0;
    private boolean showAdvancedHudOptions = false;

    private boolean showColorPicker = false;
    private boolean showSaveScreen  = false;
    private boolean showLoadScreen  = false;

    private EditBox hexInput;
    private EditBox presetNameInput;
    private EditBox maxEffectsInput;
    private int     colorPreview  = 0xFF000000;
    private String  feedbackMsg   = "";
    private long    feedbackUntil = 0;

    public PotionHudConfigScreen(Screen parent) {
        super(Component.translatable("potionhud.screen.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (showColorPicker) { initColorPicker(); return; }
        if (showSaveScreen)  { initSaveScreen();  return; }
        if (showLoadScreen)  { initLoadScreen();  return; }
        initTabbed();
    }

    private void initTabbed() {
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        int cx  = width  / 2 - W / 2;
        int top = height / 2 - 100;

        String[] tabKeys = {
            "potionhud.tab.general",
            "potionhud.tab.hud",
            "potionhud.tab.presets"
        };
        int tabW = W / 3;
        for (int i = 0; i < 3; i++) {
            final int ti = i;
            addRenderableWidget(Button.builder(Component.translatable(tabKeys[i]),
                btn -> { activeTab = ti; rebuildWidgets(); })
                .pos(cx + i * tabW, top - TAB_H - 2).size(tabW, TAB_H).build());
        }

        switch (activeTab) {
            case 0 -> initTabGeneral(cfg, cx, top);
            case 1 -> initTabHud(cfg, cx, top);
            case 2 -> initTabPresets(cfg, cx, top);
        }

        addRenderableWidget(Button.builder(
                Component.translatable("potionhud.button.apply_leave"), btn -> onClose())
            .pos(width - 82, height - 22).size(78, 18).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.apply_leave")));
    }

    // ── GENERAL ──────────────────────────────────────────────────────────────
    private void initTabGeneral(PotionHudConfig cfg, int cx, int top) {
        addRenderableWidget(new AbstractSliderButton(cx, top, W, H,
                scaleLabel(cfg.getScale()), (cfg.getScale() - 0.5f) / 2.5f) {
            @Override protected void updateMessage() { setMessage(scaleLabel(toScale(value))); }
            @Override protected void applyValue()    { markCustom(); PotionHudConfig.getInstance().setScale(toScale(value)); }
        }).setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.scale")));

        addRenderableWidget(Button.builder(
                Component.translatable(cfg.isEnabled() ? "potionhud.option.enabled" : "potionhud.option.disabled"),
                btn -> {
                    boolean v = !PotionHudConfig.getInstance().isEnabled();
                    PotionHudConfig.getInstance().setEnabled(v);
                    btn.setMessage(Component.translatable(v ? "potionhud.option.enabled" : "potionhud.option.disabled"));
                }).pos(cx, top + 25).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.enabled")));

        addRenderableWidget(Button.builder(Component.literal("\u25C4"),
            btn -> { PotionHudConfig.getInstance().setAnchor(PotionHudConfig.getInstance().getAnchor().prev()); markCustom(); rebuildWidgets(); })
            .pos(cx, top + 50).size(20, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.position_prev")));
        addRenderableWidget(Button.builder(Component.literal("\u25BA"),
            btn -> { PotionHudConfig.getInstance().setAnchor(PotionHudConfig.getInstance().getAnchor().next()); markCustom(); rebuildWidgets(); })
            .pos(cx + W - 20, top + 50).size(20, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.position_next")));

        addRenderableWidget(Button.builder(
                Component.translatable(cfg.isFlicker() ? "potionhud.option.flicker_on" : "potionhud.option.flicker_off"),
                btn -> {
                    boolean v = !PotionHudConfig.getInstance().isFlicker();
                    PotionHudConfig.getInstance().setFlicker(v);
                    btn.setMessage(Component.translatable(v ? "potionhud.option.flicker_on" : "potionhud.option.flicker_off"));
                    markCustom();
                }).pos(cx, top + 75).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.flicker")));

        addRenderableWidget(Button.builder(
                Component.translatable(cfg.isIconRight() ? "potionhud.option.icon_right" : "potionhud.option.icon_left"),
                btn -> {
                    boolean v = !PotionHudConfig.getInstance().isIconRight();
                    PotionHudConfig.getInstance().setIconRight(v);
                    btn.setMessage(Component.translatable(v ? "potionhud.option.icon_right" : "potionhud.option.icon_left"));
                    markCustom();
                }).pos(cx, top + 100).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.icon_side")));

        addRenderableWidget(Button.builder(
                Component.translatable("potionhud.button.reset"),
                btn -> { PotionHudConfig.getInstance().resetToDefaults(); PotionHudConfig.save(); rebuildWidgets(); })
            .pos(cx, top + 125).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.reset")));


        addRenderableWidget(Button.builder(
                Component.translatable(cfg.isPreviewMode() ? "potionhud.option.preview_on" : "potionhud.option.preview_off"),
                btn -> {
                    boolean v = !PotionHudConfig.getInstance().isPreviewMode();
                    PotionHudConfig.getInstance().setPreviewMode(v);
                    PotionHudConfig.save();
                    btn.setMessage(Component.translatable(v ? "potionhud.option.preview_on" : "potionhud.option.preview_off"));
                }).pos(cx + W + 8, top).size(90, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.preview")));
    }

    // ── HUD ──────────────────────────────────────────────────────────────────
    private void initTabHud(PotionHudConfig cfg, int cx, int top) {
        addRenderableWidget(new AbstractSliderButton(cx, top, W, H,
                transparencyLabel(cfg.getBgTransparency()), cfg.getBgTransparency()) {
            @Override protected void updateMessage() { setMessage(transparencyLabel((float) value)); }
            @Override protected void applyValue()    { markCustom(); PotionHudConfig.getInstance().setBgTransparency((float) value); }
        }).setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.transparency")));

        addRenderableWidget(Button.builder(
                Component.translatable(cfg.isBgRounded() ? "potionhud.option.corners_rounded" : "potionhud.option.corners_square"),
                btn -> {
                    boolean v = !PotionHudConfig.getInstance().isBgRounded();
                    PotionHudConfig.getInstance().setBgRounded(v);
                    btn.setMessage(Component.translatable(v ? "potionhud.option.corners_rounded" : "potionhud.option.corners_square"));
                    markCustom();
                }).pos(cx, top + 25).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.corners")));

        addRenderableWidget(Button.builder(
                Component.translatable("potionhud.option.bg_color", cfg.getBgColor()),
                btn -> { showColorPicker = true; rebuildWidgets(); })
            .pos(cx, top + 50).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.bg_color")));

        addRenderableWidget(Button.builder(
                Component.translatable(cfg.isHideVanillaHud()
                    ? "potionhud.option.hide_vanilla_hud_yes"
                    : "potionhud.option.hide_vanilla_hud_no"),
                btn -> {
                    boolean v = !PotionHudConfig.getInstance().isHideVanillaHud();
                    PotionHudConfig.getInstance().setHideVanillaHud(v);
                    PotionHudConfig.save();
                    btn.setMessage(Component.translatable(v
                        ? "potionhud.option.hide_vanilla_hud_yes"
                        : "potionhud.option.hide_vanilla_hud_no"));
                }).pos(cx, top + 75).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.hide_vanilla_hud")));

        addRenderableWidget(Button.builder(
                Component.literal("Advanced Options " + (showAdvancedHudOptions ? "\u25B2" : "\u25BC")),
                btn -> { showAdvancedHudOptions = !showAdvancedHudOptions; rebuildWidgets(); })
            .pos(cx, top + 100).size(W, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.advanced_options")));

        if (showAdvancedHudOptions) {
            addRenderableWidget(new AbstractSliderButton(cx, top + 125, W, H,
                    maxHeightLabel(cfg.getMaxHudHeightFrac()), cfg.getMaxHudHeightFrac()) {
                @Override protected void updateMessage() { setMessage(maxHeightLabel((float) value)); }
                @Override protected void applyValue()    { markCustom(); PotionHudConfig.getInstance().setMaxHudHeightFrac((float) value); }
            }).setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.max_height")));

            maxEffectsInput = new EditBox(font, cx, top + 150, W, H,
                Component.translatable("potionhud.option.max_effects_input"));
            maxEffectsInput.setMaxLength(3);
            int cur = cfg.getMaxEffectsOverride();
            maxEffectsInput.setValue(cur > 0 ? String.valueOf(cur) : "");
            maxEffectsInput.setHint(Component.translatable("potionhud.option.max_effects_hint"));
            maxEffectsInput.setResponder(val -> {
                try { PotionHudConfig.getInstance().setMaxEffectsOverride(Math.max(1, Integer.parseInt(val.trim()))); }
                catch (NumberFormatException e) { PotionHudConfig.getInstance().setMaxEffectsOverride(-1); }
            });
            maxEffectsInput.setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.max_effects_input")));
            addRenderableWidget(maxEffectsInput);
        }
    }

    // ── PRESETS ───────────────────────────────────────────────────────────────
    private void initTabPresets(PotionHudConfig cfg, int cx, int top) {
        addRenderableWidget(Button.builder(Component.literal("OG"),
            btn -> { PotionHudConfig.getInstance().applyPreset("og"); PotionHudConfig.save(); rebuildWidgets(); })
            .pos(cx, top).size(68, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.preset_og")));
        addRenderableWidget(Button.builder(Component.literal("Feather"),
            btn -> { PotionHudConfig.getInstance().applyPreset("feather"); PotionHudConfig.save(); rebuildWidgets(); })
            .pos(cx + 76, top).size(68, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.preset_feather")));
        addRenderableWidget(Button.builder(Component.literal("Lunar"),
            btn -> { PotionHudConfig.getInstance().applyPreset("lunar"); PotionHudConfig.save(); rebuildWidgets(); })
            .pos(cx + 152, top).size(68, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.preset_lunar")));

        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.save_preset"),
            btn -> { showSaveScreen = true; rebuildWidgets(); })
            .pos(cx, top + 30).size(BW, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.save_preset")));
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.load_preset"),
            btn -> { showLoadScreen = true; rebuildWidgets(); })
            .pos(cx + BW + 8, top + 30).size(BW, H).build())
            .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.load_preset")));
    }

    // ── COLOR PICKER ──────────────────────────────────────────────────────────
    private void initColorPicker() {
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        int leftCx = width / 4 - 100;
        int cy = height / 2 - 40;
        hexInput = new EditBox(font, leftCx, cy, 200, H, Component.literal("Hex Color"));
        hexInput.setMaxLength(7); hexInput.setValue(cfg.getBgColor());
        hexInput.setResponder(val -> colorPreview = parseHexPreview(val));
        colorPreview = parseHexPreview(cfg.getBgColor());
        addRenderableWidget(hexInput);
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.apply"), btn -> {
            String val = hexInput.getValue().trim();
            if (isValidHex(val)) {
                markCustom(); PotionHudConfig.getInstance().setBgColor(val.startsWith("#") ? val : "#" + val);
                PotionHudConfig.save(); showColorPicker = false; rebuildWidgets();
            }
        }).pos(leftCx, cy + 30).size(96, H).build())
        .setTooltip(Tooltip.create(Component.translatable("potionhud.tooltip.color_apply")));
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.cancel"),
            btn -> { showColorPicker = false; rebuildWidgets(); })
            .pos(leftCx + 104, cy + 30).size(96, H).build());
    }

    // ── SAVE / LOAD ───────────────────────────────────────────────────────────
    private void initSaveScreen() {
        int cx = width / 2 - 100, cy = height / 2 - 40;
        presetNameInput = new EditBox(font, cx, cy + 25, 200, H,
            Component.translatable("potionhud.option.preset_name"));
        presetNameInput.setMaxLength(64); presetNameInput.setValue("my_preset");
        addRenderableWidget(presetNameInput);
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.save"), btn -> {
            String name = presetNameInput.getValue().trim();
            if (!name.isBlank()) {
                PotionHudConfig cfg = PotionHudConfig.getInstance();
                PresetManager.savePreset(PresetManager.snapshot(cfg.getBgTransparency(), cfg.isBgRounded(),
                    cfg.getBgColor(), cfg.getScale(), cfg.isFlicker()), name, null);
                feedbackMsg = "Saved as " + name + ".json!";
                feedbackUntil = System.currentTimeMillis() + 3000;
                showSaveScreen = false; rebuildWidgets();
            }
        }).pos(cx, cy + 55).size(96, H).build());
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.cancel"),
            btn -> { showSaveScreen = false; rebuildWidgets(); })
            .pos(cx + 104, cy + 55).size(96, H).build());
    }

    private void initLoadScreen() {
        int cx = width / 2 - 100, cy = height / 2 - 40;
        presetNameInput = new EditBox(font, cx, cy + 25, 200, H,
            Component.translatable("potionhud.option.preset_filename"));
        presetNameInput.setMaxLength(80); presetNameInput.setValue("my_preset");
        addRenderableWidget(presetNameInput);
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.load"), btn -> {
            String name = presetNameInput.getValue().trim();
            File file = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
                .resolve(name.endsWith(".json") ? name : name + ".json").toFile();
            PresetManager.CustomPreset loaded = PresetManager.loadPreset(file);
            if (loaded != null) {
                PotionHudConfig cfg = PotionHudConfig.getInstance();
                cfg.setBgTransparency(loaded.bgTransparency); cfg.setBgRounded(loaded.bgRounded);
                cfg.setBgColor(loaded.bgColor); cfg.setScale(loaded.scale); cfg.setFlicker(loaded.flicker);
                cfg.setPreset("custom"); PotionHudConfig.save();
                feedbackMsg = "Loaded " + name + "!";
                feedbackUntil = System.currentTimeMillis() + 3000;
                showLoadScreen = false; rebuildWidgets();
            } else {
                feedbackMsg = "File not found or invalid!";
                feedbackUntil = System.currentTimeMillis() + 3000;
            }
        }).pos(cx, cy + 55).size(96, H).build());
        addRenderableWidget(Button.builder(Component.translatable("potionhud.button.cancel"),
            btn -> { showLoadScreen = false; rebuildWidgets(); })
            .pos(cx + 104, cy + 55).size(96, H).build());
    }

    // ── RENDER ────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        if (showColorPicker) {
            int leftCx = width / 4 - 100;
            int cy = height / 2 - 40;
            g.drawCenteredString(font, Component.translatable("potionhud.screen.color_picker"),
                width / 4, cy - 20, 0xFFFFFFFF);
            g.drawCenteredString(font, "#RRGGBB", width / 4, cy - 10, 0xFFAAAAAA);
            int previewX = width * 3 / 4 - 50;
            int previewY = height / 2 - 50;
            int previewSize = 100;
            g.fill(previewX - 2, previewY - 2, previewX + previewSize + 2, previewY + previewSize + 2, 0xFF555555);
            g.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, colorPreview | 0xFF000000);
            g.drawCenteredString(font, "Preview", width * 3 / 4, previewY + previewSize + 5, 0xFFAAAAAA);
            return;
        }
        if (showSaveScreen) {
            int cy = height / 2 - 40;
            g.drawCenteredString(font, Component.translatable("potionhud.screen.save_preset"), width/2, cy, 0xFFFFFFFF);
            g.drawCenteredString(font, Component.translatable("potionhud.hint.save_preset"), width/2, cy+10, 0xFFAAAAAA);
            g.drawCenteredString(font, Component.translatable("potionhud.hint.config_folder"), width/2, cy+80, 0xFF888888);
            return;
        }
        if (showLoadScreen) {
            int cy = height / 2 - 40;
            g.drawCenteredString(font, Component.translatable("potionhud.screen.load_preset"), width/2, cy, 0xFFFFFFFF);
            g.drawCenteredString(font, Component.translatable("potionhud.hint.load_preset"), width/2, cy+10, 0xFFAAAAAA);
            g.drawCenteredString(font, Component.translatable("potionhud.hint.config_folder"), width/2, cy+80, 0xFF888888);
            return;
        }

        int cx  = width / 2 - W / 2;
        int top = height / 2 - 100;

        g.drawCenteredString(font, this.title, width/2, top - TAB_H - 42, 0xFFFFFFFF);
        g.fill(cx + activeTab * (W/3), top - TAB_H - 2, cx + activeTab * (W/3) + W/3, top - 2, 0x44FFFFFF);

        if (activeTab == 0) {
            g.drawCenteredString(font,
                Component.literal("Position: " + PotionHudConfig.getInstance().getAnchor().label()),
                width/2, top + 58, 0xFFCCCCCC);
        }
        if (activeTab == 1 && showAdvancedHudOptions) {
            g.drawString(font, Component.translatable("potionhud.option.max_effects_label"),
                cx, top + 148, 0xFF888888, false);
        }

        g.drawCenteredString(font,
            Component.translatable("potionhud.label.preset", PotionHudConfig.getInstance().getPreset().toUpperCase()),
            width/2, top + 240, 0xFFAAAAAA);

        if (System.currentTimeMillis() < feedbackUntil)
            g.drawCenteredString(font, feedbackMsg, width/2, top + 255, 0xFF88FF88);

        PotionHudRenderer.render(g, DeltaTracker.ONE);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x88000000);
    }

    private static void markCustom()                  { PotionHudConfig.getInstance().setPreset("custom"); }
    private static float toScale(double v)            { return Math.round((0.5f + (float)v * 2.5f) * 100) / 100.0f; }
    private static Component scaleLabel(float s)      { return Component.translatable("potionhud.option.scale", String.format("%.2f", s)); }
    private static Component maxHeightLabel(float v)  { return Component.translatable("potionhud.option.max_height", String.format("%.0f%%", v*100)); }
    private static Component transparencyLabel(float v) {
        if (v >= 1.0f) return Component.translatable("potionhud.option.transparency_off");
        return Component.translatable("potionhud.option.transparency", String.format("%.0f%%", v*100));
    }
    private static boolean isValidHex(String s) {
        if (s == null) return false;
        String h = s.startsWith("#") ? s.substring(1) : s;
        return h.length() == 6 && h.matches("[0-9a-fA-F]+");
    }
    private static int parseHexPreview(String s) {
        try { String h = s.startsWith("#") ? s.substring(1) : s; if (h.length() == 6) return 0xFF000000 | Integer.parseInt(h, 16); }
        catch (NumberFormatException ignored) {}
        return 0xFF000000;
    }

    @Override public void onClose() { PotionHudConfig.save(); assert minecraft != null; minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
