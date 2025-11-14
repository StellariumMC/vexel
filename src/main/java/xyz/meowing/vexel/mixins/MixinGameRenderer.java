package xyz.meowing.vexel.mixins;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.knit.api.render.KnitResolution;
import xyz.meowing.vexel.utils.render.NVGRenderer;
import xyz.meowing.vexel.events.GuiEvent;

import static xyz.meowing.vexel.Vexel.getEventBus;

//#if MC > 1.20.1
import net.minecraft.client.render.RenderTickCounter;
//#endif

//#if MC >= 1.21.5 && FABRIC
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
//#endif

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    //#if MC >= 1.21.6
    //$$ @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V", shift = At.Shift.AFTER), cancellable = true)
    //#else
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.AFTER), cancellable = true)
    //#endif

    //#if MC > 1.20.1
    public void hookRender(
            RenderTickCounter tickCounter,
            boolean tick,
            CallbackInfo ci
    ) {
    //#elseif MC == 1.20.1
    //$$ public void hookRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
    //#endif

        NVGRenderer.INSTANCE.beginFrame(KnitResolution.getWindowWidth(), KnitResolution.getWindowHeight());
        if (
                getEventBus().post(
                        new GuiEvent.Render(),
                        false
                )
        ) ci.cancel();
        NVGRenderer.INSTANCE.endFrame();
    }
}