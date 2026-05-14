package dev.lumyrix.potionhud;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import dev.lumyrix.potionhud.render.PotionHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class JustAPotionHudClient implements ClientModInitializer {

    public static final String MOD_ID = "justapotionhud";

    @Override
    public void onInitializeClient() {
        PotionHudConfig.load();
        HudRenderCallback.EVENT.register(PotionHudRenderer::render);
    }
}
