package dev.lumyrix.potionhud;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import dev.lumyrix.potionhud.render.PotionHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class JustAPotionHudClient implements ClientModInitializer {
    public static final String MOD_ID = "justapotionhud";

    @Override
    public void onInitializeClient() {
        PotionHudConfig.load();

        HudElementRegistry.replaceElement(VanillaHudElements.MOB_EFFECTS,
            original -> (graphics, delta) -> {
                if (!PotionHudConfig.getInstance().isHideVanillaHud()) {
                    original.extractRenderState(graphics, delta);
                }
            }
        );

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.MOB_EFFECTS,
            Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
            (graphics, delta) -> PotionHudRenderer.render(graphics, delta)
        );
    }
}
