package lol.wilkyy.kauna.mixin.features.other;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(Hud.class)
public class PlotTitleMixin {

    private static final Pattern PLOT_SUBTITLE = Pattern.compile(
            "^(\\w{1,16}) (-?\\d+;-?\\d+)$"
    );

    @Inject(method = "setSubtitle", at = @At("HEAD"), cancellable = true)
    private void kauna$redirectPlotSubtitle(Component subtitle, CallbackInfo ci) {
        if (!KaunaConfig.INSTANCE.creativePlotTitle) {return;}
        String text = subtitle.getString();

        if (PLOT_SUBTITLE.matcher(text).matches()) {
            Hud self = (Hud) (Object) this;
            self.setOverlayMessage((MutableComponent) subtitle, false);
            ci.cancel();
        }
    }
}