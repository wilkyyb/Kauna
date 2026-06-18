package lol.wilkyy.kauna.mixin;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.kahakka.inDuelChecks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;


@Mixin(Gui.class)
public class TitleMixin {

    private static final List<String> DUEL_TYPES = Arrays.asList(
            "Mace", "Crystal", "Spear (Mace)", "Spear (Elytra)", "Sword", "Axe",
            "Realistic", "Parkour", "Diamond SMP", "Creeper", "SMP", "Netherite Potion",
            "Sumo", "UHC", "Diamond Potion", "Cart", "Spleef", "Bed", "Diamond Mace",
            "OG Vanilla", "Spear", "Hoplite", "Archer", "OneShot", "Ghast",
            "Tavallinen", "Boxing", "Soppa", "Speed", "Trident", "Elytra", "Cart (HT)",
            "Netherite Potion (Vanha)", "Diamond Potion (Vanha)", "SMP (Vanha)",
            "UHC (Uusi)", "Mace (Vanha)", "Assembly", "Paukutus", "TNT-Sota", "Arrow Toss"
    );


    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void onSetOverlayMessage(Component message, boolean tinted, CallbackInfo ci) {
        if (message == null && Kauna.isCurrentlyOnRealmi()) return;
        String content = message.getString();

        if (content.contains("Kierroksen alkuun")) {
            String number = content.replaceAll("[^0-9]", "");

            if (!number.isEmpty()) {
                Minecraft client = Minecraft.getInstance();

                inDuelChecks.duelStarted = false;
                if (number.equals("5")) {
                } else if (number.equals("4")) {
                } else {
                    ChatFormatting color = switch (number) {
                        case "3" -> ChatFormatting.GREEN;
                        case "2" -> ChatFormatting.YELLOW;
                        case "1" -> ChatFormatting.RED;
                        default -> ChatFormatting.GOLD;
                    };

                    Component countdownTitle = Component.literal(number)
                            .setStyle(net.minecraft.network.chat.Style.EMPTY.withBold(true).withColor(color));

                    client.gui.setTimes(0, 30, 5);
                    client.gui.setTitle(countdownTitle);
                    client.gui.setSubtitle(Component.literal(""));
                }
                return;
            }
        }

        if (content.contains("PB") && !inDuelChecks.duelStarted && Kauna.isCurrentlyOnRealmi()) {
            Minecraft client = Minecraft.getInstance();

            client.gui.setTimes(0, 20, 5);
            client.gui.setTitle(Component.literal(""));
            client.gui.setSubtitle(Component.literal("GLHF!").withStyle(ChatFormatting.GOLD));
            inDuelChecks.duelStarted = true;
        }
    }
}
