package lol.wilkyy.kauna.kahakka.StatsDisplay;

import net.minecraft.network.chat.TextColor;
import java.util.HashMap;
import java.util.Map;

public class StatsManager {
    private static int wins = 0;
    private static int losses = 0;
    private static int globalWinstreak = 0; // Separate global tracking

    public static int getWins() { return wins; }
    public static int getLosses() { return losses; }

    public static int getGlobalWinstreak() { return globalWinstreak; }
    public static void setGlobalWinstreak(int streak) { globalWinstreak = streak; }

    public static void addWin() { wins++; globalWinstreak++; }
    public static void addLoss() { losses++; globalWinstreak = 0; }

    public static void resetSession() {
        wins = 0;
        losses = 0;
        globalWinstreak = 0;
    }

    public static String getWinLossRatio() {
        if (losses == 0) {
            return wins > 0 ? String.valueOf(wins) : "0.0";
        }
        return String.format("%.2f", (double) wins / losses);
    }

    private static String currentKit = "Pelaa Kittiä";
    private static TextColor currentKitColor = null;

    public static String getCurrentKit() { return currentKit; }
    public static TextColor getCurrentKitColor() { return currentKitColor; }

    public static void setCurrentKit(String kit, TextColor color) {
        currentKit = kit;
        currentKitColor = color;
    }

    // int[] layout: { wins, losses, kills, winstreak }
    private static final Map<String, int[]> kitStats = new HashMap<>();

    public static void addKitWin(String kit) {
        int[] stats = kitStats.computeIfAbsent(kit, k -> new int[4]);
        stats[0]++; // Wins
        stats[3]++; // Winstreak
    }

    public static void addKitLoss(String kit) {
        int[] stats = kitStats.computeIfAbsent(kit, k -> new int[4]);
        stats[1]++; // Losses
        stats[3] = 0; // Reset streak
    }

    public static int getKitWinstreak(String kit) {
        return kitStats.getOrDefault(kit, new int[4])[3];
    }

    public static void setKitWinstreak(String kit, int streak) {
        kitStats.computeIfAbsent(kit, k -> new int[4])[3] = streak;
    }

    public static int[] getKitStats(String kit) {
        return kitStats.getOrDefault(kit, new int[4]);
    }

    public static Map<String, int[]> getAllKitStats() {
        return kitStats;
    }
}