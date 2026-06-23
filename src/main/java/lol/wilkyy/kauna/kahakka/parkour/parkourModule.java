package lol.wilkyy.kauna.kahakka.parkour;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.config.Colors;
import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

import java.text.DecimalFormat;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;
import static lol.wilkyy.kauna.kahakka.inDuelChecks.inParkourDuel;
import static lol.wilkyy.kauna.kahakka.parkour.parkourChatListener.*;

public class parkourModule implements ClientModInitializer {

    private static boolean rainbowRunning = false;
    private static int rainbowTick = 0;
    private static final String worldRecordText = "Maailman Ennätys";

    private static double pbDiff = 0.0;
    private static double wrDiff = 0.0;
    private static boolean pendingDisplay = false;

    private static final DecimalFormat threeDecimals = new DecimalFormat("0.000");

    int otherTextColor = 0xbebebe;
    int timeColor = 0xFFFFFF;
    int dividerColor = 0x545454;
    int starColor = 0xFDD000;

    // Dynamically retrieve the colors array based on config selection
    private static int[] getCurrentColors() {
        if (KaunaConfig.INSTANCE.wrColorTheme == null) return Colors.RAINBOW_THEME;
        return switch (KaunaConfig.INSTANCE.wrColorTheme) {
            case "Gay" -> Colors.GAY_THEME;
            case "Lesbian" -> Colors.LESBIAN_THEME;
            case "Trans" -> Colors.TRANS_THEME;
            default -> Colors.RAINBOW_THEME;
        };
    }

    @Override
    public void onInitializeClient() {
        parkourDuelWinCheck();
        playerSkipCheck();
        countdown();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (pendingDisplay && parkourChatListener.statsUpdatedThisTick) {
                calculateAndShowSplits(mc);
                pendingDisplay = false;
                parkourChatListener.statsUpdatedThisTick = false;
            }

            if (!rainbowRunning) return;

            if (mc.level != null && mc.level.getGameTime() % 2 == 0) {
                rainbowTick++;
            }

            int wrColor = (wrDiff < 0) ? 0x00FF00 : 0xfc5454;
            int pbColor = (pbDiff < 0) ? 0x00FF00 : 0xfc5454;

            int[] targetColors = getCurrentColors();

            MutableComponent rainbowText = Component.empty();
            for (int i = 0; i < worldRecordText.length(); i++) {
                int colorIndex = (i - rainbowTick) % targetColors.length;
                if (colorIndex < 0) colorIndex += targetColors.length;
                int color = targetColors[colorIndex];
                rainbowText.append(Component.literal(String.valueOf(worldRecordText.charAt(i)))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(true)));
            }

            MutableComponent title = Component.literal("⭐ ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                    .append(rainbowText)
                    .append(Component.literal(" ⭐")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

            mc.gui.setTimes(0, 2, 20);
            mc.gui.setTitle(title);
            mc.gui.setSubtitle(generateSubtitle(wrColor, pbColor));

            if (rainbowTick > 80) {
                rainbowRunning = false;
                mc.gui.setTitle(Component.literal(""));
                mc.gui.setSubtitle(Component.literal(""));
            }
        });
    }

    public static void showParkourTitle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) return;
        if (inParkourDuel) return;

        // Set the timings once (Fade-in ticks, Display duration ticks, Fade-out ticks)
        mc.gui.setTimes(10, 40, 10);

        // Push the title text once. Minecraft handles its lifecycle automatically.
        mc.gui.setTitle(Component.literal("Parkour")
                .withStyle(ChatFormatting.BLUE)
                .withStyle(style -> style.withBold(true)));
    }

    private void calculateAndShowSplits(Minecraft client) {
        wrDiff = time - worldRecord;
        pbDiff = time - personalBest;

        int wrColor = (wrDiff < 0) ? 0x00FF00 : 0xfc5454;
        int pbColor = (pbDiff < 0) ? 0x00FF00 : 0xfc5454;

        if (time < worldRecord && worldRecord != 0) { // World Record
            rainbowRunning = true;
            rainbowTick = 0;
            if (client.player != null) {
                client.player.playSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), 1.0f, 1.0f);
            }
            debugLog("Got WR, animation started");

        } else if (pbDiff < 0) { // Personal best
            MutableComponent title = Component.literal("⭐ ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                    .append(Component.literal("O").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x52A4E5)).withBold(true)))
                    .append(Component.literal("m").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x56A9E4)).withBold(true)))
                    .append(Component.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x5BAEE2)).withBold(true)))
                    .append(Component.literal(" "))
                    .append(Component.literal("E").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x64B8DF)).withBold(true)))
                    .append(Component.literal("n").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x68BDDE)).withBold(true)))
                    .append(Component.literal("n").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x64B8DF)).withBold(true)))
                    .append(Component.literal("ä").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x5064B8DF)).withBold(true)))
                    .append(Component.literal("t").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x5BAEE2)).withBold(true)))
                    .append(Component.literal("y").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x56A9E4)).withBold(true)))
                    .append(Component.literal("s").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x52A4E5)).withBold(true)))
                    .append(Component.literal(" ⭐")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

            client.gui.setTimes(0, 100, 20);
            client.gui.setTitle(title);
            client.gui.setSubtitle(generateSubtitle(wrColor, pbColor));
            debugLog("Got PB, splits displayed");

        } else { // No record
            MutableComponent title = Component.literal("⭐ ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true))
                    .append(Component.literal("V").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFAB500)).withBold(true)))
                    .append(Component.literal("o").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBC117)).withBold(true)))
                    .append(Component.literal("i").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBCD2E)).withBold(true)))
                    .append(Component.literal("t").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFCD845)).withBold(true)))
                    .append(Component.literal("t").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFCE45C)).withBold(true)))
                    .append(Component.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFDD63D)).withBold(true)))
                    .append(Component.literal("j").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFEC71F)).withBold(true)))
                    .append(Component.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFB900)).withBold(true)))
                    .append(Component.literal(" ⭐")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(starColor)).withBold(true)));

            client.gui.setTimes(0, 100, 20);
            client.gui.setTitle(title);
            client.gui.setSubtitle(generateSubtitle(wrColor, pbColor));
            debugLog("No WR or PB, showing standard splits");
        }
    }

    private Component generateSubtitle(int wrColor, int pbColor) {
        return Component.empty()
                .append(Component.literal("WR ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(otherTextColor))))
                .append(Component.literal(formatDiff(wrDiff))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(wrColor))))
                .append(Component.literal(" | ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                .append(Component.literal(threeDecimals.format(time))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(timeColor))))
                .append(Component.literal(" | ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(dividerColor))))
                .append(Component.literal("PB ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(otherTextColor))))
                .append(Component.literal(formatDiff(pbDiff))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pbColor))));
    }

    private static String formatDiff(double value) {
        String formatted = threeDecimals.format(value);
        return (value > 0) ? "+" + formatted : formatted;
    }

    public void parkourDuelWinCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Aika:") && !msg.contains("[") && Kauna.isCurrentlyOnRealmi()) {
                pendingDisplay = true;
            }
        });
    }

    public void playerSkipCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            Minecraft client = Minecraft.getInstance();
            if (client.player == null ) return;

            if (msg.contains("ehdotti kartan ohitusta!") && Kauna.isCurrentlyOnRealmi()) {
                if (KaunaConfig.INSTANCE.stickySkipNotification) {
                    Component subtitle = Component.empty()
                            .append(Component.literal("⌚")
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xbebebe))));
                    client.gui.setTimes(0, 280, 20);
                    client.gui.setTitle(Component.literal(""));
                    client.gui.setSubtitle(subtitle);
                } else {
                    Component mainTitle = Component.literal("");
                    Component subtitle = Component.literal("Vastustaja haluaa ohittaa kartan")
                            .withStyle(ChatFormatting.RED);
                    client.gui.setTimes(2, 30, 20);
                    client.gui.setTitle(mainTitle);
                    client.gui.setSubtitle(subtitle);
                }
                debugLog("Skip-ilmoitus näytetty.");
            }

            if ((msg.contains("hyväksyi kartan ohituksen!") || msg.contains("Peli alkoi!")) && !msg.contains("[") && Kauna.isCurrentlyOnRealmi()) {
                client.gui.setTimes(0, 0, 0);
                client.gui.setTitle(Component.literal(""));
                client.gui.setSubtitle(Component.literal(""));
            }
        });
    }

    public void countdown() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();
            if (content.contains("Kierroksen alkuun") && Kauna.isCurrentlyOnRealmi()) {
                String number = content.replaceAll("[^0-9]", "");
                if (!number.isEmpty()) {
                    Minecraft client = Minecraft.getInstance();
                    Component countdownTitle = Component.literal(number)
                            .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD));
                    client.gui.setTimes(0, 20, 5);
                    client.gui.setTitle(countdownTitle);
                }
            }
        });
    }
}