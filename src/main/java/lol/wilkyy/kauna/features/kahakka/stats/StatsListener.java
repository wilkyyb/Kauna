package lol.wilkyy.kauna.features.kahakka.stats;

import lol.wilkyy.kauna.features.chat.PlayerNameResolver;
import lol.wilkyy.kauna.features.kahakka.parkour.ParkourChatListener;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

public class StatsListener {
    private static boolean readingWinstreakLine = false;

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;

            String text = message.getString().trim();

            // 1. Capture Multi-line winstreak block from server
            if (text.contains("voittoputki:")) {
                readingWinstreakLine = true;
                return;
            }

            if (readingWinstreakLine) {
                if (text.contains("Nykyinen")) {
                    // Split cleanly by the colon to separate the labels from the number
                    String[] parts = text.split(":", 2);
                    if (parts.length == 2) {
                        // Isolate the numeric characters on the right side of the colon
                        String numericPart = parts[1].replaceAll("[^0-9]", "").trim();

                        try {
                            int currentStreak = Integer.parseInt(numericPart);
                            StatsManager.setKitWinstreak(StatsManager.getCurrentKit(), currentStreak);
                        } catch (NumberFormatException ignored) {}
                    }

                    // If this line contains 'ennätys', we know we can stop tracking the block
                    if (text.toLowerCase().contains("ennätys")) {
                        readingWinstreakLine = false;
                    }
                    return;
                } else if (text.contains("Ennätys:")) {
                    readingWinstreakLine = false;
                    return;
                } else {
                    readingWinstreakLine = false;
                }
            }

            // 2. Local VS calculations (No longer blocked out permanently)
            if (!text.contains("⚔") || !text.contains("VS")) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            String playerName = PlayerNameResolver.getPlayerName();
            String stripped = text.replace("⚔", "").trim();

            String[] sides = stripped.split(" VS ", 2);
            if (sides.length != 2) return;

            String leftSide  = sides[0].trim();
            String rightSide = sides[1].trim();
            String rightName = rightSide.split("[\\(\\s]")[0].trim();

            if (leftSide.equalsIgnoreCase(playerName)) {
                StatsManager.addWin();
                StatsManager.addKitWin(StatsManager.getCurrentKit());
                ParkourChatListener.currentTime = 0.0;
            } else if (rightName.equalsIgnoreCase(playerName)) {
                StatsManager.addLoss();
                StatsManager.addKitLoss(StatsManager.getCurrentKit());
                ParkourChatListener.currentTime = 0.0;
            }
        });
    }
}