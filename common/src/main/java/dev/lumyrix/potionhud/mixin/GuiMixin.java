package dev.lumyrix.potionhud.mixin;

import dev.lumyrix.potionhud.config.PotionHudConfig;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void potionhud$noOp(CallbackInfo ci) {
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true, require = 0)
    private void potionhud$hideVanillaEffects(CallbackInfo ci) {
        if (PotionHudConfig.getInstance().isHideVanillaHud()) {
            ci.cancel();
        }
    }
}
