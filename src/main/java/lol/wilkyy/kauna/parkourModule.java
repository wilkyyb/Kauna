package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.Logger;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.apache.logging.log4j.LogManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.MutableText;

import java.text.DecimalFormat;

import static lol.wilkyy.kauna.parkourChatListener.*;

public class parkourModule implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("KaunaParkour");

    private static boolean rainbowRunning = false;
    private static int rainbowTick = 0;
    private static final String worldRecordText = "Maailman Ennätys";
    private static final int[] rainbowColors = {
            0xFF5656, 0xFFAA00, 0xFFFF55,
            0x57FF57, 0x55FFFF, 0xAA00AA, 0xFF55FF
    };

    private static double pbDiff = 0.0;
    private static double wrDiff = 0.0;

    // formatter for always three decimals
    private static final DecimalFormat threeDecimals = new DecimalFormat("0.000");


    @Override
    public void onInitializeClient() {
        LOGGER.info(">>> parkourModule initialized <<<");
        parkourDuelEnd();

        // Rainbow animation tick handler (only for WR case)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!rainbowRunning) return;
            rainbowTick++;

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

            mc.inGameHud.setTitle(title);
            mc.inGameHud.setTitleTicks(0, 2, 20);

            Text subtitle = Text.empty()
                    .append(Text.literal(formatDiff(wrDiff))
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(wrColor))))
                    .append(Text.literal(" | ")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                    .append(Text.literal(threeDecimals.format(time))
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(timeColor))))
                    .append(Text.literal(" | ")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                    .append(Text.literal(formatDiff(pbDiff))
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbColor))));

            mc.inGameHud.setSubtitle(subtitle);

            if (rainbowTick > 200) {
                rainbowRunning = false;
                mc.inGameHud.setTitle(Text.literal(""));
                mc.inGameHud.setSubtitle(Text.literal(""));
            }
        });
    }


    // helper method
    private static String formatDiff(double value) {
        String formatted = threeDecimals.format(value);
        if (value > 0) {
            return "+" + formatted;
        }
        return formatted;
    }

    int wrColor = (wrDiff < 0) ? 0x00FF00 : 0xfc5454;
    int pbColor = (pbDiff < 0) ? 0x00FF00 : 0xfc5454;
    int timeColor = 0xFFFFFF;
    int dividerColor = 0x545454;
    int starColor = 0xFDD000;

    public void parkourDuelEnd() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Aika:") && !msg.contains("[")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1);
                        wrDiff = time - worldRecord;
                        pbDiff = time - personalBest;

                        int pbTitleColor = 0x61c6ee;
                        int noRecordTitleColor = 0xf9d301;

                        if (wrDiff < 0) { // World record
                            rainbowRunning = true;
                            rainbowTick = 0;
                            LOGGER.info("Got WR, rainbow animation started");
                        } else if (pbDiff < 0) { // Personal best
                            MinecraftClient client = MinecraftClient.getInstance();

                            MutableText title = Text.literal("⭐ ")
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                                    .append(Text.literal("Oma Ennätys")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbTitleColor)).withBold(true)))
                                    .append(Text.literal(" ⭐")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

                            Text subtitle = Text.empty()
                                    .append(Text.literal(formatDiff(wrDiff))
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(wrColor))))
                                    .append(Text.literal(" | ")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                                    .append(Text.literal(threeDecimals.format(time))
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(timeColor))))
                                    .append(Text.literal(" | ")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                                    .append(Text.literal(formatDiff(pbDiff))
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbColor))));

                            client.inGameHud.setTitle(title);
                            client.inGameHud.setSubtitle(subtitle);
                            client.inGameHud.setTitleTicks(0, 100, 20);

                            LOGGER.info("Got PB, splits displayed without server title");
                        } else { // No record
                            MinecraftClient client = MinecraftClient.getInstance();

                            MutableText title = Text.literal("⭐ ")
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                                    .append(Text.literal("Voittaja")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(noRecordTitleColor)).withBold(true)))
                                    .append(Text.literal(" ⭐")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

                            Text subtitle = Text.empty()
                                    .append(Text.literal(formatDiff(wrDiff))
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(wrColor))))
                                    .append(Text.literal(" | ")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                                    .append(Text.literal(threeDecimals.format(time))
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(timeColor))))
                                    .append(Text.literal(" | ")
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                                    .append(Text.literal(formatDiff(pbDiff))
                                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbColor))));

                            client.inGameHud.setTitle(title);
                            client.inGameHud.setSubtitle(subtitle);
                            client.inGameHud.setTitleTicks(0, 100, 20);

                            LOGGER.info("No WR or PB, splits displayed without server title");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
    }
}
