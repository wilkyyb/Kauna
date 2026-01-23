package lol.wilkyy.kauna.mixin;

import lol.wilkyy.kauna.autoReadyUp;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.statsChecker;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;


@Mixin(InGameHud.class)
public class TitleMixin {


    // The list of all duel types you want to track
    private static final List<String> DUEL_TYPES = Arrays.asList(
            "Mace", "Crystal", "Spear (Mace)", "Spear (Elytra)", "Sword", "Axe",
            "Realistic", "Parkour", "Diamond SMP", "Creeper", "SMP", "Netherite Potion",
            "Sumo", "UHC", "Diamond Potion", "Cart", "Spleef", "Bed", "Diamond Mace",
            "OG Vanilla", "Spear", "Shieldless UHC", "Archer", "OneShot", "Ghast",
            "Tavallinen", "Boxing", "Soppa", "Speed", "Trident", "Elytra", "Cart (HT)",
            "Netherite Potion (Vanha)", "Diamond Potion (Vanha)", "SMP (Vanha)",
            "UHC (Uusi)", "Mace (Vanha)"
    );

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Text title, CallbackInfo ci) {
        if (title == null) return;
        String titleText = title.getString();

        for (String type : DUEL_TYPES) {
            if (titleText.equalsIgnoreCase(type)) {
                // Check if we already found the opponent from chat
                if (!statsChecker.targetOpponent.isEmpty()) {
                    statsChecker.triggerStatsLookup(type, statsChecker.targetOpponent);
                } else {
                    // If title came first, just save the duel name and wait for chat
                    statsChecker.duelName = type;
                }
                break;
            }
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message != null && message.getString().contains("(kyykkää)")) {
            if (KaunaConfig.INSTANCE.autoReadyUp) {
                autoReadyUp.startCrouch(2);
            }
        }
    }
}
