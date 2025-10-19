package xyz.meowing.vexel.mixins;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.meowing.knit.api.events.EventBus;
import xyz.meowing.vexel.events.GuiEvent;

//#if MC > 1.20.1
import net.minecraft.client.render.RenderTickCounter;
//#endif

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    //#if MC >= 1.21.6
    //$$ @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V", shift = At.Shift.AFTER), cancellable = true)
    //#else
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.AFTER), cancellable = true)
    //#endif

    //#if MC > 1.20.1
    public void hookRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
    //#elseif MC == 1.20.1
    //$$ public void hookRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
    //#endif
        if (EventBus.INSTANCE.post(new GuiEvent.Render()))
            ci.cancel();
    }
}
