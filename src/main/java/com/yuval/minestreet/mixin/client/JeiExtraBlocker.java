package com.yuval.minestreet.mixin.client;

import com.yuval.minestreet.client.ModHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mezz.jei.gui.overlay.GuiPropertiesCache", remap = false)
public class JeiExtraBlocker {

    @Inject(
            method = "hasValidScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disable(CallbackInfoReturnable<Boolean> cir) {
        if (ModHelper.isTradingStation())
            cir.setReturnValue(false);
    }
}
