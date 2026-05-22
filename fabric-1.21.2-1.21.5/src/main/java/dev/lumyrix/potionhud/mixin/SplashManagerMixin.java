package dev.lumyrix.potionhud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.resource.SplashTextResourceSupplier")
public class SplashManagerMixin {

    private static final double SPLASH_CHANCE = 0.055;
    private static final String JESUS_SPLASH  = "Jesus Loves You!! If You repent from your sins and sinful lifestyle, He can save you! You're NOT Too far gone.";

    @Inject(method = "get", at = @At("HEAD"), cancellable = true, require = 1)
    private void potionhud$JesusGlimmer(CallbackInfoReturnable<String> cir) {
        if (Math.random() < SPLASH_CHANCE) {
            cir.setReturnValue(JESUS_SPLASH);
        }
    }
}
