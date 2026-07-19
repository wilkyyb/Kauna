package lol.wilkyy.kauna.mixin.features.kahakka.parkour;

import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.features.kahakka.autoready.AutoReadyUp;
import lol.wilkyy.kauna.features.kahakka.stats.StatsManager;
import lol.wilkyy.kauna.features.kahakka.duel.DuelManager;
import lol.wilkyy.kauna.features.kahakka.parkour.ParkourChatListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

@Mixin(Hud.class)
public class TitleMixin {

    private static final List<String> KITS = List.of(
            "Mace", "Crystal", "Sword", "Axe", "Smp", "Netherite Potion", "UHC",
            "Diamond Potion", "Diamond SMP", "Creeper", "Cart", "Bed", "OG Vanilla",
            "Archer", "Speed", "Trident", "Elytra", "Cart (HT)", "Parkour", "Sumo",
            "Spleef", "OneShot", "Ghast", "Boxing", "Soppa", "Paukutus", "TNT-Sota",
            "Arrow Toss", "Spear (Mace)", "Spear (Elytra)", "Spear", "Realistic",
            "Diamond Mace", "Assembly", "Hoplite", "Tavallinen", "UHC (Vanha)"
    );

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Component message, CallbackInfo ci) {
        if (message == null) return;
        String content = message.getString();
        if (content == null) return;

        for (String kit : KITS) {
            if (content.equalsIgnoreCase(kit)) {
                // Extract the TextColor object directly
                net.minecraft.network.chat.TextColor textColor = message.getStyle().getColor();

                // Pass both the name and color to the manager
                StatsManager.setCurrentKit(kit, textColor);

                String colorHex = (textColor != null) ? String.format("0x%08X", textColor.getValue()) : "None/Default";
                debugLog("Kit detected: " + kit + " (Color: " + colorHex + ")");
                break;
            }
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Component message, boolean tinted, CallbackInfo ci) {

        String content = message.getString();

        if (content != null) {
            if (content.contains("kyykkää") || KaunaConfig.autoReady) {
                AutoReadyUp.tryTriggerCrouch();
            }
        }

        if (content != null) {
            if (content.contains("↑") || content.contains("←") || content.contains("↓") || content.contains("→")) {
                AutoReadyUp.isSearchingForReady = false;
            }
        }

        // Parkour stuff
        if (content.contains("Kierroksen alkuun")) {
            String number = content.replaceAll("[^0-9]", "");

            if (!number.isEmpty()) {
                Minecraft client = Minecraft.getInstance();
                DuelManager.duelStarted = false;

                if (!number.equals("5") && !number.equals("4")) {
                    ChatFormatting color = switch (number) {
                        case "3" -> ChatFormatting.GREEN;
                        case "2" -> ChatFormatting.YELLOW;
                        case "1" -> ChatFormatting.RED;
                        default -> ChatFormatting.GOLD;
                    };

                    Component countdownTitle = Component.literal(number)
                            .withStyle(Style.EMPTY.withBold(true).withColor(color));

                    client.gui.hud.setTimes(0, 30, 5);
                    client.gui.hud.setTitle(countdownTitle);
                    client.gui.hud.setSubtitle(Component.literal(""));
                }
                return;
            }
        }

        if (content.contains("Aika:")) {
            try {
                String afterAika = content.substring(content.indexOf("Aika:") + 5).trim();

                // If there is a suffix (like " - PB: 18.000"), isolate the number before it
                if (afterAika.contains(" - ")) {
                    afterAika = afterAika.substring(0, afterAika.indexOf(" - ")).trim();
                } else {
                    // If it contains trailing spaces or extra characters, isolate just the number string safely
                    afterAika = afterAika.split("\\s+")[0].trim();
                }

                ParkourChatListener.currentTime = Double.parseDouble(afterAika);
            } catch (NumberFormatException | IndexOutOfBoundsException ignored) {}
        }

        if (content.contains("Peli päättyi")) {
            DuelManager.inDuel = false;
            DuelManager.inParkourDuel = false;
            DuelManager.duelEndTimer = -1;
            DuelManager.duelStarted = false;
            ParkourChatListener.time = 0.0;
            ParkourChatListener.worldRecord = 0;
            ParkourChatListener.personalBest = 0;
            ParkourChatListener.statsUpdatedThisTick = false;
            ParkourChatListener.currentTime = 0.0;
        }

        if (content.contains("PB") && !DuelManager.duelStarted) {
            Minecraft client = Minecraft.getInstance();

            client.gui.hud.setTimes(0, 20, 5);
            client.gui.hud.setTitle(Component.literal(""));
            client.gui.hud.setSubtitle(Component.literal("GLHF!").withStyle(ChatFormatting.GOLD));
            DuelManager.duelStarted = true;
        }
    }
}