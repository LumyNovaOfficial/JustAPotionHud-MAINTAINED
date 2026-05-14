package dev.lumyrix.potionhud.render;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import dev.lumyrix.potionhud.screen.HudPositionScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PotionHudRenderer {

    private static final int BASE_ICON = 18;
    private static final int BASE_PAD = 4;

    private PotionHudRenderer() {}

    public record EffectEntry(String name, String level, String time, boolean warn,
                              float textAlpha, Holder<MobEffect> holder) {}

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof HudPositionScreen) return;

        PotionHudConfig cfg = PotionHudConfig.getInstance();
        if (!cfg.isEnabled() || mc.player == null) return;

        List<EffectEntry> entries = buildEntries(cfg, mc);
        if (entries.isEmpty()) return;

        renderEntries(graphics, cfg, mc,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                entries);
    }

    // ✅ FIX PRINCIPAL: método que estava faltando
    public static int[] renderPreviewAndGetSize(GuiGraphics graphics, int screenW, int screenH) {
        PotionHudConfig cfg = PotionHudConfig.getInstance();
        Minecraft mc = Minecraft.getInstance();

        List<EffectEntry> entries = buildFakeEntries();

        return renderEntries(graphics, cfg, mc, screenW, screenH, entries);
    }

    private static List<EffectEntry> buildEntries(PotionHudConfig cfg, Minecraft mc) {
        if (mc.player == null) return List.of();

        List<MobEffectInstance> real = new ArrayList<>(mc.player.getActiveEffects());
        real.sort(Comparator.comparingInt(MobEffectInstance::getDuration));

        List<EffectEntry> out = new ArrayList<>();
        for (MobEffectInstance inst : real) {
            out.add(new EffectEntry(
                    inst.getEffect().value().getDisplayName().getString(),
                    "",
                    "",
                    false,
                    1f,
                    inst.getEffect()
            ));
        }
        return out;
    }

    private static List<EffectEntry> buildFakeEntries() {
        return List.of(
                new EffectEntry("Speed", " II", "--:--", false, 1f,
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.SPEED.value())),
                new EffectEntry("Strength", " I", "--:--", false, 1f,
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.STRENGTH.value()))
        );
    }

    private static int[] renderEntries(GuiGraphics graphics,
                                       PotionHudConfig cfg,
                                       Minecraft mc,
                                       int screenW,
                                       int screenH,
                                       List<EffectEntry> entries) {

        int x = 20;
        int y = 20;

        int maxW = 0;

        for (EffectEntry e : entries) {
            int w = mc.font.width(e.name());
            if (w > maxW) maxW = w;
        }

        int iconSize = BASE_ICON;
        int pad = BASE_PAD;

        int height = entries.size() * (iconSize + pad);

        int drawY = y;

        for (EffectEntry e : entries) {

            graphics.drawString(mc.font, e.name(), x + iconSize + 4, drawY, 0xFFFFFF, false);

            drawEffectIcon(graphics, e, x, drawY, iconSize);

            drawY += iconSize + pad;
        }

        return new int[]{maxW + iconSize, height};
    }

    private static void drawEffectIcon(GuiGraphics graphics, EffectEntry e, int x, int y, int size) {
        if (e.holder() == null) return;

        e.holder().unwrapKey().ifPresent(key -> {
            ResourceLocation id = key.location();
            graphics.blitSprite(
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "mob_effect/" + id.getPath()),
                    x, y, size, size
            );
        });
    }
}
