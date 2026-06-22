package lol.wilkyy.kauna.mixin;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.kahakka.AutoReadyUp;
import lol.wilkyy.kauna.kahakka.inDuelChecks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

@Mixin(Gui.class)
public class TitleMixin {

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
                inDuelChecks.duelStarted = false;

                if (!number.equals("5") && !number.equals("4")) {
                    ChatFormatting color = switch (number) {
                        case "3" -> ChatFormatting.GREEN;
                        case "2" -> ChatFormatting.YELLOW;
                        case "1" -> ChatFormatting.RED;
                        default -> ChatFormatting.GOLD;
                    };

                    Component countdownTitle = Component.literal(number)
                            .withStyle(Style.EMPTY.withBold(true).withColor(color));

                    client.gui.setTimes(0, 30, 5);
                    client.gui.setTitle(countdownTitle);
                    client.gui.setSubtitle(Component.literal(""));
                }
                return;
            }
        }

        if (content.contains("PB") && !inDuelChecks.duelStarted) {
            Minecraft client = Minecraft.getInstance();

            client.gui.setTimes(0, 20, 5);
            client.gui.setTitle(Component.literal(""));
            client.gui.setSubtitle(Component.literal("GLHF!").withStyle(ChatFormatting.GOLD));
            inDuelChecks.duelStarted = true;
        }
    }
}