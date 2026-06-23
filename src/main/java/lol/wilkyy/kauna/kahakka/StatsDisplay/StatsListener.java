package lol.wilkyy.kauna.kahakka.StatsDisplay;

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
                if (text.contains("Nykyinen:")) {
                    String parts = text.split("Nykyinen:", 2)[1].trim();
                    String numericPart = parts.split("[\\s\\(]")[0].trim();

                    try {
                        int currentStreak = Integer.parseInt(numericPart);
                        // Updates the kit-specific winstreak slot!
                        StatsManager.setKitWinstreak(StatsManager.getCurrentKit(), currentStreak);
                    } catch (NumberFormatException ignored) {}

                    if (text.contains("ennätys")) {
                        readingWinstreakLine = false;
                    }
                } else if (text.contains("Ennätys:")) {
                    readingWinstreakLine = false;
                }
                return;
            }

            // 2. Local VS calculations
            if (!text.contains("⚔") || !text.contains("VS")) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            String playerName = mc.player.getName().getString();
            String stripped = text.replace("⚔", "").trim();

            String[] sides = stripped.split(" VS ", 2);
            if (sides.length != 2) return;

            String leftSide  = sides[0].trim();
            String rightSide = sides[1].trim();
            String rightName = rightSide.split("[\\(\\s]")[0].trim();

            if (leftSide.equalsIgnoreCase(playerName)) {
                StatsManager.addWin(); // Handles global winstreak + global wins
                StatsManager.addKitWin(StatsManager.getCurrentKit()); // Handles kit winstreak + kit wins
            } else if (rightName.equalsIgnoreCase(playerName)) {
                StatsManager.addLoss(); // Resets global winstreak
                StatsManager.addKitLoss(StatsManager.getCurrentKit()); // Resets kit winstreak
            }
        });
    }
}