package xyz.meowing.vexel.mixins;

//#if MC >= 1.21
import com.mojang.blaze3d.opengl.GlStateManager;
//#else
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.vexel.utils.render.TextureTracker;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
    @Inject(method = "_bindTexture", at = @At("HEAD"), remap = false)
    private static void onBindTexture(int texture, CallbackInfo ci) {
        TextureTracker.setPreviousBoundTexture(texture);
    }

    @Inject(method = "_activeTexture", at = @At("HEAD"), remap = false)
    private static void onActiveTexture(int texture, CallbackInfo ci) {
        TextureTracker.setPreviousActiveTexture(texture);
    }
}