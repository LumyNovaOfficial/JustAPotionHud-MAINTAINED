package dev.lumyrix.potionhud.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lumyrix.potionhud.config.PotionHudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
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
                int elapsed = FLICKER_START - ticks;
                float periodTicks = ticks > 60 ? 8f : ticks > 40 ? 8f / 1.25f : (8f / 1.25f) / 1.05f;
                alpha = 0.15f + 0.85f * (float)(0.5 + 0.5 * Math.cos(2 * Math.PI * elapsed / periodTicks));
            }

            int timeColor = isWarn ? ((int)(alpha * 255) << 24 | (COLOR_WARN & 0x00FFFFFF)) : 0xFFFFFFFF;
            int nameColor = isFlicker ? ((int)(alpha * 255) << 24 | 0x00FFFFFF) : 0xFFFFFFFF;

            final int fx = x, fy = y, fSize = iconSize;
            TextureAtlasSprite sprite = client.getMobEffectTextures().get(holder);
            graphics.flush();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            graphics.blitSprite(RenderType::guiTextured, sprite, fx, fy, fSize, fSize);
            graphics.flush();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            int textX = x + iconSize + pad;
            int lineH  = client.font.lineHeight + 1;
            int textY  = y + (iconSize - lineH * 2) / 2;

            graphics.pose().pushPose();
            graphics.pose().translate((float) textX, (float) textY, 0.0f);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(client.font, name, 0, 0,     nameColor, false);
            graphics.drawString(client.font, time, 0, lineH, timeColor,  false);
            graphics.pose().popPose();

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
