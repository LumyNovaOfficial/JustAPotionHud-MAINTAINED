package dev.lumyrix.potionhud.render;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import dev.lumyrix.potionhud.screen.HudPositionScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PotionHudRenderer {
    private static final int   BASE_ICON        = 18;
    private static final int   BASE_PAD         = 4;
    private static final int   COLOR_WARN       = 0xFFDD4949;
    private static final int   FLICKER_START    = 119;
    private static final int   BG_PAD           = 5;
    private static final int   CORNER_R         = 5;
    private static final float SHADOW_INTENSITY = 0.160f;

    private static int     cachedR = -1, cachedBaseA = -1, cachedRgb = -1;
    private static int[][] cornerCache = null;

    private static final float MARQUEE_SPEED = 27.6f;
    private static long lastMarqueeTime = 0;
    private static final float[] marqueeOffsets = new float[32];

    private static final Map<Holder<MobEffect>, Long> LOW_DURATION_START = new HashMap<>();
    // Cache de larguras de texto
    private static final java.util.Map<String, Integer> TEXT_WIDTH_CACHE = new java.util.HashMap<>();
    private static int cachedScale = -1;
    private static Minecraft cachedMc = null;


    private PotionHudRenderer() {}

    public record EffectEntry(String name, String level, String time, boolean warn,
                              float textAlpha, Holder<MobEffect> holder, boolean infinite) {}

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof HudPositionScreen) return;
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        if (!cfg.isEnabled() || mc.player == null) return;

        if (cfg.isPreviewMode()) {
            int sw = mc.getWindow().getGuiScaledWidth();
            Component warn = Component.translatable("potionhud.preview.warning")
                .withStyle(ChatFormatting.BOLD);
            int tw = mc.font.width(warn);
            graphics.drawString(mc.font, warn, sw / 2 - tw / 2 + 1, 5, 0xFFAA0000, false);
            graphics.drawString(mc.font, warn, sw / 2 - tw / 2,     5, 0xFFFF3333, false);
        }

        List<EffectEntry> entries = buildEntries(cfg, mc);
        if (entries.isEmpty()) return;

        long now = System.currentTimeMillis();
        float dt = lastMarqueeTime == 0 ? 0 : (now - lastMarqueeTime) / 1000.0f;
        lastMarqueeTime = now;
        for (int i = 0; i < marqueeOffsets.length; i++) marqueeOffsets[i] += MARQUEE_SPEED * dt;

        renderEntries(graphics, cfg, mc,
            mc.getWindow().getGuiScaledWidth(),
            mc.getWindow().getGuiScaledHeight(), entries);
    }

    public static int[] renderPreviewAndGetSize(GuiGraphics graphics, int screenW, int screenH) {
        return renderEntries(graphics, PotionHudConfig.getInstance(),
            Minecraft.getInstance(), screenW, screenH, buildFakeEntries());
    }

    private static List<EffectEntry> buildEntries(PotionHudConfig cfg, Minecraft mc) {
        if (cfg.isPreviewMode()) return buildFakeEntries();
        if (mc.player == null) return List.of();
        List<MobEffectInstance> real = new ArrayList<>(mc.player.getActiveEffects());
        if (real.isEmpty()) return List.of();
        real.sort(Comparator.comparingInt(MobEffectInstance::getDuration));
        List<EffectEntry> out = new ArrayList<>();
        for (MobEffectInstance inst : real) {
            int ticks = inst.getDuration();
            boolean warn    = ticks != Integer.MAX_VALUE && ticks <= 319;
            boolean flicker = cfg.isFlicker() && ticks != Integer.MAX_VALUE && ticks <= FLICKER_START;
            float textAlpha = 1.0f;
            boolean infinite = false;

            if (ticks != Integer.MAX_VALUE && ticks <= 20) {
                long now = System.currentTimeMillis();
                Holder<MobEffect> holder = inst.getEffect();
                Long start = LOW_DURATION_START.get(holder);
                if (start == null) {
                    LOW_DURATION_START.put(holder, now);
                } else if (now - start > 3500) {
                    infinite = true;
                    warn = true;
                    flicker = false;
                    textAlpha = 1.0f;
                }
            } else if (ticks > 20) {
                LOW_DURATION_START.remove(inst.getEffect());
            }

            if (flicker) {
                int elapsed = FLICKER_START - ticks;
                float period = ticks > 60 ? 8f : ticks > 40 ? 8f / 1.25f : (8f / 1.25f) / 1.05f;
                textAlpha = 0.15f + 0.85f * (float)(0.5 + 0.5 * Math.cos(2 * Math.PI * elapsed / period));
            }
            String lvl = (cfg.isHideLevel1() && inst.getAmplifier() == 0) ? "" : romanLevel(inst.getAmplifier());
            out.add(new EffectEntry(
                inst.getEffect().value().getDisplayName().getString(),
                lvl,
                formatTicks(ticks), warn, textAlpha, inst.getEffect(), infinite));
        }
        return out;
    }

    private static List<EffectEntry> buildFakeEntries() {
        return List.of(
            new EffectEntry("Speed",        " II",  "--:--", false, 1f, MobEffects.SPEED, false),
            new EffectEntry("Regeneration", " I",   "--:--", false, 1f, MobEffects.REGENERATION, false),
            new EffectEntry("Strength",     " III", "--:--", false, 1f, MobEffects.STRENGTH, false),
            new EffectEntry("Jump Boost",   " II",  "--:--", false, 1f, MobEffects.JUMP_BOOST, false),
            new EffectEntry("Resistance",   " I",   "--:--", false, 1f, MobEffects.RESISTANCE, false)
        );
    }

    private static int cachedWidth(String text, float scale) {
        if (cachedMc == null || scale != cachedScale) {
            TEXT_WIDTH_CACHE.clear();
            cachedScale = (int)(scale * 1000);
        }
        String key = text + "@" + cachedScale;
        Integer w = TEXT_WIDTH_CACHE.get(key);
        if (w == null) {
            w = (int)(cachedMc.font.width(text) * scale);
            TEXT_WIDTH_CACHE.put(key, w);
        }
        return w;
    }

    private static int[] renderEntries(GuiGraphics g, PotionHudConfig cfg,
                                        Minecraft mc, int screenW, int screenH,
                                        List<EffectEntry> entries) {
        if (cachedMc != mc) {
            TEXT_WIDTH_CACHE.clear();
            cachedMc = mc;
        }
        float scale  = cfg.getScale();
        int iconSize = Math.max(1, (int)(BASE_ICON * scale));
        int pad      = Math.max(1, (int)(BASE_PAD  * scale));
        int lineH    = (int)((mc.font.lineHeight + 1) * scale);

        int maxByHeight = Math.max(1, (int)((screenH * cfg.getMaxHudHeightFrac() + pad) / (iconSize + pad)));
        int maxDisplay  = cfg.getMaxEffectsOverride() > 0 ? cfg.getMaxEffectsOverride() : maxByHeight;
        int displayCount = Math.min(entries.size(), maxDisplay);
        boolean hasOverflow = entries.size() > maxDisplay;
        int overflowCount   = entries.size() - displayCount;

        int maxTextW = 0;
        for (EffectEntry e : entries) {
            String timeStr = e.infinite() ? "∞" : e.time();
            int tw = Math.max(cachedWidth(e.name() + e.level(), scale),
                              cachedWidth(timeStr, scale));
            if (tw > maxTextW) maxTextW = tw;
        }

        int hudW = iconSize + pad + maxTextW;
        int visibleRows = displayCount + (hasOverflow ? 1 : 0);
        int hudH = Math.max(1, visibleRows) * iconSize + (Math.max(1, visibleRows) - 1) * pad;

        int startX, startY;
        if (cfg.isUseAltPos()) {
            startX = (int)(cfg.getPosXFrac() * screenW);
            startY = (int)(cfg.getPosYFrac() * screenH);
        } else {
            int[] pos = cfg.resolvePosition(screenW, screenH, hudW, hudH);
            startX = pos[0]; startY = pos[1];
        }

        int bgX = startX - BG_PAD, bgY = startY - BG_PAD;
        int bgW = hudW + BG_PAD * 2, bgH = hudH + BG_PAD * 2;

        if (cfg.isBgVisible() && cfg.isShadows()) {
            int[] offs = {3, 2, 1};
            float[] af  = {0.25f, 0.40f, 0.55f};
            for (int i = 0; i < offs.length; i++) {
                int o  = offs[i];
                int sa = Math.min(255, (int)(af[i] * SHADOW_INTENSITY * 255));
                g.fill(bgX + o, bgY + o, bgX + bgW + o, bgY + bgH + o, sa << 24);
            }
        }

        if (cfg.isBgVisible()) {
            int bgArgb = cfg.getBgArgb();
            if (cfg.isBgRounded()) drawRoundedRectAA(g, bgX, bgY, bgW, bgH, CORNER_R, bgArgb);
            else g.fill(bgX, bgY, bgX + bgW, bgY + bgH, bgArgb);
        }

        int y = startY;
        for (int i = 0; i < displayCount; i++) {
            EffectEntry e = entries.get(i);
            int iconX = cfg.isIconRight() ? startX + maxTextW + pad : startX;
            int textX = cfg.isIconRight() ? startX : startX + iconSize + pad;
            int textY = y + (iconSize - lineH * 2) / 2;

            final int rowY = y;
            if (e.holder() != null) {
                e.holder().unwrapKey().ifPresent(key -> {
                    Identifier id = key.identifier();
                    g.blitSprite(RenderPipelines.GUI_TEXTURED,
                        Identifier.fromNamespaceAndPath(id.getNamespace(), "mob_effect/" + id.getPath()),
                        iconX, rowY, iconSize, iconSize, 1.0f);
                });
            }

            int nameColor = e.warn() ? applyAlpha(COLOR_WARN, e.textAlpha()) : applyAlpha(0xFFFFFFFF, e.textAlpha());
            String nameStr = e.name() + e.level();
            String timeStr = e.infinite() ? "\u221E" : e.time();
            int timeColor = e.infinite() ? applyAlpha(COLOR_WARN, 0.85f) : nameColor;
            float mq = i < marqueeOffsets.length ? marqueeOffsets[i] : 0;

            if (cfg.isShadows()) {
                float si = e.infinite() ? SHADOW_INTENSITY : SHADOW_INTENSITY * e.textAlpha();
                int ts1 = Math.min(255, (int)(si * 0.55f * 255)) << 24;
                int ts2 = Math.min(255, (int)(si * 0.30f * 255)) << 24;
                int ts3 = Math.min(255, (int)(si * 0.12f * 255)) << 24;
                drawScrollingText(g, mc, nameStr, textX+3, textY+3,        ts3, scale, maxTextW, mq);
                drawScrollingText(g, mc, timeStr, textX+3, textY+lineH+3, ts3, scale, maxTextW, 0);
                drawScrollingText(g, mc, nameStr, textX+2, textY+2,        ts2, scale, maxTextW, mq);
                drawScrollingText(g, mc, timeStr, textX+2, textY+lineH+2, ts2, scale, maxTextW, 0);
                drawScrollingText(g, mc, nameStr, textX+1, textY+1,        ts1, scale, maxTextW, mq);
                drawScrollingText(g, mc, timeStr, textX+1, textY+lineH+1, ts1, scale, maxTextW, 0);
            }
            drawScrollingText(g, mc, nameStr, textX, textY,        nameColor, scale, maxTextW, mq);
            drawScrollingText(g, mc, timeStr, textX, textY+lineH, timeColor, scale, maxTextW, 0);

            y += iconSize + pad;
        }

        if (hasOverflow) {
            List<EffectEntry> overflow = entries.subList(displayCount, entries.size());
            int iconX = cfg.isIconRight() ? startX + maxTextW + pad : startX;
            int textX = cfg.isIconRight() ? startX : startX + iconSize + pad;
            int miniSize = Math.max(4, iconSize / 2 - 1);
            int col = 0, row = 0;
            for (int i = 0; i < Math.min(overflow.size(), 4); i++) {
                EffectEntry e = overflow.get(i);
                if (e.holder() != null) {
                    final int mx = iconX + col * (miniSize + 1);
                    final int my = y     + row * (miniSize + 1);
                    e.holder().unwrapKey().ifPresent(key -> {
                        Identifier id = key.identifier();
                        g.blitSprite(RenderPipelines.GUI_TEXTURED,
                            Identifier.fromNamespaceAndPath(id.getNamespace(), "mob_effect/" + id.getPath()),
                            mx, my, miniSize, miniSize, 0.85f);
                    });
                }
                col++; if (col >= 2) { col = 0; row++; }
            }
            String lbl = "+" + overflowCount + " more";
            int labelY = y + (iconSize - lineH) / 2;
            if (cfg.isShadows()) {
                float si = SHADOW_INTENSITY;
                drawScaledText(g, mc, lbl, textX+2, labelY+2, (Math.min(255,(int)(si*0.20f*255))<<24), scale);
                drawScaledText(g, mc, lbl, textX+1, labelY+1, (Math.min(255,(int)(si*0.45f*255))<<24), scale);
            }
            drawScaledText(g, mc, lbl, textX, labelY, 0xFFAAAAAA, scale);
        }

        return new int[]{hudW, hudH};
    }

    private static void drawScrollingText(GuiGraphics g, Minecraft mc, String text,
                                           int x, int y, int color, float scale,
                                           int maxW, float scrollOffset) {
        int textW = cachedWidth(text, scale);
        if (textW <= maxW) { drawScaledText(g, mc, text, x, y, color, scale); return; }
        int gap   = (int)(12 * scale);
        int cycle = textW + gap;
        int off   = (int)(scrollOffset % cycle);
        int sh    = (int)(mc.font.lineHeight * scale) + 4;
        g.enableScissor(x, y - 2, x + maxW, y + sh);
        drawScaledText(g, mc, text, x - off,           y, color, scale);
        if (off > 0) drawScaledText(g, mc, text, x - off + cycle, y, color, scale);
        g.disableScissor();
    }

    private static void drawScaledText(GuiGraphics g, Minecraft mc,
                                        String text, int x, int y, int color, float scale) {
        if (scale == 1.0f) { g.drawString(mc.font, text, x, y, color, false); return; }
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawString(mc.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    private static void rebuildCornerCache(int r, int baseA, int rgb) {
        cachedR = r; cachedBaseA = baseA; cachedRgb = rgb;
        cornerCache = new int[r][r];
        for (int row = 0; row < r; row++) {
            for (int col = 0; col < r; col++) {
                double cx = r - col - 0.5, cy2 = r - row - 0.5;
                double dist = Math.sqrt(cx * cx + cy2 * cy2);
                double cov;
                if (dist <= r - 1.0) cov = 1.0;
                else if (dist >= r + 1.0) cov = 0.0;
                else {
                    int ins = 0;
                    int total = 4 * 4;
                    double sampleStep = 1.0 / 4.0;
                    for (int sy = 0; sy < 4; sy++)
                        for (int sx = 0; sx < 4; sx++) {
                            double px = (r - col - 1.0) + (sx + 0.5) * sampleStep;
                            double py = (r - row - 1.0) + (sy + 0.5) * sampleStep;
                            if (px * px + py * py <= (double) r * r) ins++;
                        }
                    cov = (double) ins / total;
                }
                if (cov <= 0.0) { cornerCache[row][col] = 0; continue; }
                int a = Math.min(255, (int)(baseA * cov));
                cornerCache[row][col] = (a << 24) | rgb;
            }
        }
    }

    public static void drawRoundedRectAA(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w / 2, h / 2));
        if (r <= 0) { g.fill(x, y, x + w, y + h, color); return; }
        int baseA = (color >> 24) & 0xFF, rgb = color & 0x00FFFFFF;
        g.fill(x + r, y,     x + w - r, y + h,     color);
        g.fill(x,     y + r, x + r,     y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);
        if (cornerCache == null || cachedR != r || cachedBaseA != baseA || cachedRgb != rgb)
            rebuildCornerCache(r, baseA, rgb);
        for (int row = 0; row < r; row++)
            for (int col = 0; col < r; col++) {
                int pc = cornerCache[row][col]; if (pc == 0) continue;
                g.fill(x + col,       y + row,       x + col + 1,   y + row + 1,   pc);
                g.fill(x + w - col-1, y + row,       x + w - col,   y + row + 1,   pc);
                g.fill(x + col,       y + h - row-1, x + col + 1,   y + h - row,   pc);
                g.fill(x + w - col-1, y + h - row-1, x + w - col,   y + h - row,   pc);
            }
    }

    private static int applyAlpha(int argb, float alpha) {
        return ((int)(((argb >> 24) & 0xFF) * alpha) << 24) | (argb & 0x00FFFFFF);
    }
    private static String formatTicks(int ticks) {
        if (ticks == Integer.MAX_VALUE) return "**:**";
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
    private static String romanLevel(int amp) {
        return switch (amp) {
            case 0 -> " I"; case 1 -> " II"; case 2 -> " III";
            case 3 -> " IV"; case 4 -> " V"; default -> " " + (amp + 1);
        };
    }
}
