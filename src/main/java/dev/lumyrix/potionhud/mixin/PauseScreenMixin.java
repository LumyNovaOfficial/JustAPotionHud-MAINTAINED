package dev.lumyrix.potionhud.mixin;

import dev.lumyrix.potionhud.screen.PotionHudConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends net.minecraft.client.gui.screens.Screen {

    protected PauseScreenMixin() {
        super(Component.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void potionhud$addConfigButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(
                Component.literal("Potion HUD"),
                btn -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new PotionHudConfigScreen(this));
                })
                .pos(this.width - 106, 6)
                .size(100, 16)
                .build());
    }
}
