package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;
import static lol.wilkyy.kauna.parkourChatListener.*;

public class parkourModule implements ClientModInitializer {

    private static boolean rainbowRunning = false;
    private static int rainbowTick = 0;
    private static final String worldRecordText = "Maailman Ennätys";
    private static final int[] rainbowColors = {
            0xFF5656, 0xFFAA00, 0xFFFF55,
            0x57FF57, 0x55FFFF, 0xAA00AA, 0xFF55FF
    };

    private static double pbDiff = 0.0;
    private static double wrDiff = 0.0;
    private static boolean pendingDisplay = false;

    // Formatter for always three decimals
    private static final DecimalFormat threeDecimals = new DecimalFormat("0.000");

    // Colors
    int otherTextColor = 0xbebebe;
    int timeColor = 0xFFFFFF;
    int dividerColor = 0x545454;
    int starColor = 0xFDD000;

    @Override
    public void onInitializeClient() {
        parkourDuelWinCheck();
        playerSkipCheck();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // 1. Check if we need to calculate and show splits (waits for ChatListener to finish)
            if (pendingDisplay && parkourChatListener.statsUpdatedThisTick) {
                calculateAndShowSplits(mc);
                pendingDisplay = false;
                parkourChatListener.statsUpdatedThisTick = false;
            }

            // 2. Rainbow Title Animation Logic
            if (!rainbowRunning) return;

            if (mc.world != null && mc.world.getTime() % 2 == 0) {
                rainbowTick++;
            }

            int wrColor = (wrDiff < 0) ? 0x00FF00 : 0xfc5454;
            int pbColor = (pbDiff < 0) ? 0x00FF00 : 0xfc5454;

            MutableText rainbowText = Text.empty();
            for (int i = 0; i < worldRecordText.length(); i++) {
                int colorIndex = (i - rainbowTick) % rainbowColors.length;
                if (colorIndex < 0) colorIndex += rainbowColors.length;
                int color = rainbowColors[colorIndex];
                rainbowText.append(Text.literal(String.valueOf(worldRecordText.charAt(i)))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(true)));
            }

            MutableText title = Text.literal("⭐ ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                    .append(rainbowText)
                    .append(Text.literal(" ⭐")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

            mc.inGameHud.setTitleTicks(0, 2, 20);
            mc.inGameHud.setTitle(title);
            mc.inGameHud.setSubtitle(generateSubtitle(wrColor, pbColor));

            if (rainbowTick > 80) {
                rainbowRunning = false;
                mc.inGameHud.setTitle(Text.literal(""));
                mc.inGameHud.setSubtitle(Text.literal(""));
            }
        });
    }

    private void calculateAndShowSplits(MinecraftClient client) {
        wrDiff = time - worldRecord;
        pbDiff = time - personalBest;

        int pbTitleColor = 0x61c6ee;
        int noRecordTitleColor = 0xf9d301;

        int wrColor = (wrDiff < 0) ? 0x00FF00 : 0xfc5454;
        int pbColor = (pbDiff < 0) ? 0x00FF00 : 0xfc5454;

        if (time <= worldRecord && worldRecord != 0) {
            rainbowRunning = true;
            rainbowTick = 0;
            // ... rest of logic
            if (client.player != null) {
                client.player.playSound(SoundEvents.GOAT_HORN_SOUNDS.get(0).value(), 1.0f, 1.0f);
            }
            debugLog("Got WR, rainbow animation started");

        } else if (pbDiff < 0) { // Personal best
            MutableText title = Text.literal("⭐ ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                    .append(Text.literal("Oma Ennätys")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbTitleColor)).withBold(true)))
                    .append(Text.literal(" ⭐")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

            client.inGameHud.setTitleTicks(0, 100, 20);
            client.inGameHud.setTitle(title);
            client.inGameHud.setSubtitle(generateSubtitle(wrColor, pbColor));
            debugLog("Got PB, splits displayed");

        } else { // No record
            MutableText title = Text.literal("⭐ ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                    .append(Text.literal("Voittaja")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(noRecordTitleColor)).withBold(true)))
                    .append(Text.literal(" ⭐")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

            client.inGameHud.setTitleTicks(0, 100, 20);
            client.inGameHud.setTitle(title);
            client.inGameHud.setSubtitle(generateSubtitle(wrColor, pbColor));
            debugLog("No WR or PB, showing standard splits");
        }
    }

    private Text generateSubtitle(int wrColor, int pbColor) {
        return Text.empty()
                .append(Text.literal("WR ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(otherTextColor))))
                .append(Text.literal(formatDiff(wrDiff))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(wrColor))))
                .append(Text.literal(" | ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                .append(Text.literal(threeDecimals.format(time))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(timeColor))))
                .append(Text.literal(" | ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                .append(Text.literal("PB ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(otherTextColor))))
                .append(Text.literal(formatDiff(pbDiff))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbColor))));
    }

    private static String formatDiff(double value) {
        String formatted = threeDecimals.format(value);
        return (value > 0) ? "+" + formatted : formatted;
    }

    public void parkourDuelWinCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            // Just trigger the pending flag
            if (msg.contains("Aika:") && !msg.contains("[")) {
                pendingDisplay = true;
            }
        });
    }

    public void playerSkipCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();
            String playerName = client.player != null ? client.player.getName().getString() : "";

            if (msg.contains("ehdotti kartan ohitusta!") && !msg.contains(playerName) && !msg.contains("[")) {
                Text subtitle = Text.empty()
                        .append(Text.literal("⌚")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xbebebe))));

                client.inGameHud.setTitleTicks(0, 72000, 20);
                client.inGameHud.setTitle(Text.literal(""));
                client.inGameHud.setSubtitle(subtitle);
                debugLog("Vastustaja haluaa skipata!");
            }

            if (msg.contains("hyväksyi kartan ohituksen!") && !msg.contains("[")) {
                client.inGameHud.setTitleTicks(0, 0, 0);
                client.inGameHud.setTitle(Text.literal(""));
                client.inGameHud.setSubtitle(Text.literal(""));
            }
        });
    }
}