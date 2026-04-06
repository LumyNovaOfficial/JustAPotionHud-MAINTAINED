package dev.lumyrix.potionhud.render;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PotionHudRenderer {
    private static final int BASE_ICON    = 18;
    private static final int BASE_PAD     = 4;
    private static final int COLOR_WARN   = 0xFFDD4949;
    private static final int FLICKER_START = 119;

    private PotionHudRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        if (!cfg.isEnabled()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        List<MobEffectInstance> effects = new ArrayList<>(client.player.getActiveEffects());
        if (effects.isEmpty()) return;

        effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration));

        float scale  = cfg.getScale();
        int iconSize = Math.max(1, (int)(BASE_ICON * scale));
        int pad      = Math.max(1, (int)(BASE_PAD  * scale));

        int screenW = client.getWindow().getGuiScaledWidth();
        int screenH = client.getWindow().getGuiScaledHeight();
        int x = (int)(cfg.getXFrac() * screenW);
        int y = (int)(cfg.getYFrac() * screenH);

        for (MobEffectInstance inst : effects) {
            Holder<MobEffect> holder = inst.getEffect();
            MobEffect effect = holder.value();

            String name = effect.getDisplayName().getString() + romanLevel(inst.getAmplifier());
            String time = formatTicks(inst.getDuration());

            int ticks     = inst.getDuration();
            boolean isWarn    = ticks != Integer.MAX_VALUE && ticks <= 319;
            boolean isFlicker = cfg.isFlicker() && ticks != Integer.MAX_VALUE && ticks <= FLICKER_START;

            float alpha = 1.0f;
            if (isFlicker) {
                // elapsed começa em 0 quando ticks=FLICKER_START → cos(0)=1 → alpha máximo
                int elapsed = FLICKER_START - ticks;

                // período em ticks: 5-3s normal (8 ticks), 3-2s 25% mais rápido, <2s mais 5%
                float periodTicks;
                if (ticks > 60) {
                    periodTicks = 8f;
                } else if (ticks > 40) {
                    periodTicks = 8f / 1.25f;
                } else {
                    periodTicks = (8f / 1.25f) / 1.05f;
                }

                alpha = 0.15f + 0.85f * (float)(0.5 + 0.5 * Math.cos(2 * Math.PI * elapsed / periodTicks));
            }

            int timeColor = isWarn
                ? ((int)(alpha * 255) << 24 | (COLOR_WARN & 0x00FFFFFF))
                : 0xFFFFFFFF;
            int nameColor = isFlicker
                ? ((int)(alpha * 255) << 24 | 0x00FFFFFF)
                : 0xFFFFFFFF;

            final int fx = x, fy = y, fSize = iconSize;
            final float fa = alpha;
            holder.unwrapKey().ifPresent(key -> {
                Identifier id = key.identifier();
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    Identifier.fromNamespaceAndPath(id.getNamespace(), "mob_effect/" + id.getPath()),
                    fx, fy, fSize, fSize, fa);
            });

            int textX = x + iconSize + pad;
            int lineH  = client.font.lineHeight + 1;
            int textY  = y + (iconSize - lineH * 2) / 2;

            graphics.pose().pushMatrix();
            graphics.pose().translate((float) textX, (float) textY);
            graphics.pose().scale(scale, scale);
            graphics.drawString(client.font, name, 0, 0,     nameColor, false);
            graphics.drawString(client.font, time, 0, lineH, timeColor, false);
            graphics.pose().popMatrix();

            y += iconSize + pad;
        }
    }

    private static String formatTicks(int ticks) {
        if (ticks == Integer.MAX_VALUE) return "**:**";
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static String romanLevel(int amp) {
        return switch (amp) {
            case 0  -> "";
            case 1  -> " II";
            case 2  -> " III";
            case 3  -> " IV";
            case 4  -> " V";
            default -> " " + (amp + 1);
        };
    }
}
