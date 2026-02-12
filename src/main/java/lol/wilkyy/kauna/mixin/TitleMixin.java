package lol.wilkyy.kauna.mixin;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.autoReadyUp;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.inDuelChecks;
import lol.wilkyy.kauna.statsChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

import static lol.wilkyy.kauna.statsChecker.duelName;


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
        if (title == null &&  Kauna.isCurrentlyOnRealmi()) return;
        String titleText = title.getString();

        for (String type : DUEL_TYPES) {
            if (titleText.equalsIgnoreCase(type)) {
                // Check if we already found the opponent from chat
                if (!statsChecker.targetOpponent.isEmpty()) {
                    statsChecker.triggerStatsLookup(type, statsChecker.targetOpponent);
                } else {
                    // If title came first, just save the duel name and wait for chat
                    duelName = type;
                }
                break;
            }
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message == null && Kauna.isCurrentlyOnRealmi()) return;
        String content = message.getString();

        if (content.contains("Kierroksen alkuun")) {
            String number = content.replaceAll("[^0-9]", "");

            if (!number.isEmpty()) {
                MinecraftClient client = MinecraftClient.getInstance();

                inDuelChecks.duelStarted = false;
                // Handle the specific cases for 5 and 4
                if (number.equals("5")) {
                } else if (number.equals("4")) {
                } else {
                    // Standard 3, 2, 1 logic
                    Formatting color = switch (number) {
                        case "3" -> Formatting.GREEN;
                        case "2" -> Formatting.YELLOW;
                        case "1" -> Formatting.RED;
                        default -> Formatting.GOLD;
                    };

                    Text countdownTitle = Text.literal(number)
                            .setStyle(net.minecraft.text.Style.EMPTY.withBold(true).withColor(color));

                    client.inGameHud.setTitleTicks(0, 30, 5);
                    client.inGameHud.setTitle(countdownTitle);
                    client.inGameHud.setSubtitle(Text.literal(""));
                }
                return;
            }
        }

        if (content.contains("PB") && !inDuelChecks.duelStarted && Kauna.isCurrentlyOnRealmi()) {
            MinecraftClient client = MinecraftClient.getInstance();

            client.inGameHud.setTitleTicks(0, 20, 5);
            client.inGameHud.setTitle(Text.literal(""));
            client.inGameHud.setSubtitle(Text.literal("GLHF!").formatted(Formatting.GOLD));
            inDuelChecks.duelStarted = true;
        }

        // Existing autoReadyUp kyykkää logic
        if (content.contains("(kyykkää)") && Kauna.isCurrentlyOnRealmi()) {
            if (KaunaConfig.INSTANCE.autoReadyUp && !statsChecker.duelName.contains("Parkour")) {
                lol.wilkyy.kauna.autoReadyUp.startCrouch(2);
            }
        }
    }
}
