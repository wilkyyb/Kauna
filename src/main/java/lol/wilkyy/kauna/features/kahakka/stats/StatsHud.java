package lol.wilkyy.kauna.features.kahakka.stats;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.features.chat.PlayerNameResolver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class StatsHud implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(Kauna.MOD_ID, "stats_hud"),
                StatsHud::render
        );
        StatsListener.register();
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        renderStats(graphics, client.font);
    }

    private static void renderStats(GuiGraphicsExtractor graphics, Font font) {
        int x = KaunaConfig.INSTANCE.statsHudX;
        int y = KaunaConfig.INSTANCE.statsHudY;
        int rowHeight = 12;

        boolean kitMode = KaunaConfig.INSTANCE.showKitStats;
        String currentKit = StatsManager.getCurrentKit();

        int wins, losses, currentWinstreak;
        String winLossRatio;

        if (kitMode) {
            int[] ks = StatsManager.getKitStats(currentKit);
            wins = ks[0];
            losses = ks[1];
            currentWinstreak = ks[3]; // Kit winstreak
            winLossRatio = losses == 0
                    ? (wins > 0 ? String.valueOf(wins) : "0.0")
                    : String.format("%.2f", (double) wins / losses);
        } else {
            wins = StatsManager.getWins();
            losses = StatsManager.getLosses();
            currentWinstreak = StatsManager.getGlobalWinstreak(); // Global winstreak
            winLossRatio = StatsManager.getWinLossRatio();
        }

        Component title = Component.literal("Session Stats")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true));

        net.minecraft.network.chat.TextColor kitColor = StatsManager.getCurrentKitColor();

        Component scopeLabel = kitMode
                ? Component.literal("⚔ " + StatsManager.getCurrentKit() + " ⚔")
                .setStyle(Style.EMPTY
                        .withColor(kitColor != null ? kitColor : net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.WHITE))
                        .withBold(true))
                : Component.literal("◈ Global ◈")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true));

        Component winsText = Component.literal("Voitot: ")
                .append(Component.literal(String.valueOf(wins)).withStyle(ChatFormatting.GREEN));

        Component lossesText = Component.literal("Häviöt: ")
                .append(Component.literal(String.valueOf(losses)).withStyle(ChatFormatting.RED));

        Component wlText = Component.literal("W/L: ")
                .append(Component.literal(winLossRatio).withStyle(ChatFormatting.DARK_GREEN));

        Component winstreakText = Component.literal("WS: ")
                .append(Component.literal(String.valueOf(currentWinstreak)).withStyle(ChatFormatting.YELLOW));



        if (KaunaConfig.INSTANCE.statsHud && Kauna.inKahakka()) {
            // Calculate background dimensions
            int padding = 4;
            int bgWidth = 90; // wide enough for all rows
            int rowCount = (currentWinstreak > 0) ? 6 : 5; // title + scope + wins + losses + wl + optional ws
            int bgHeight = rowHeight * rowCount + padding;

            int bgX1 = x - bgWidth / 2 - padding;
            int bgY1 = y - padding;
            int bgX2 = x + bgWidth / 2 + padding;
            int bgY2 = y + bgHeight;

            // Build ARGB color: alpha from config, black background
            int alpha = (int) (KaunaConfig.INSTANCE.statsHudBackgroundOpacity * 255);
            int bgColor = (alpha << 24) | 0x000000;

            graphics.fill(bgX1, bgY1, bgX2, bgY2, bgColor);

            // Then render text on top as before
            graphics.text(font, title,      x - font.width(title.getVisualOrderText()) / 2,      y,              0xFFFFFF00, true);
            graphics.text(font, scopeLabel, x - font.width(scopeLabel.getVisualOrderText()) / 2, y + rowHeight,  0xFFFFFFFF, true);
            graphics.text(font, winsText,   x - font.width(winsText.getVisualOrderText()) / 2,   y + rowHeight*2, 0xFFFFFFFF, true);
            graphics.text(font, lossesText, x - font.width(lossesText.getVisualOrderText()) / 2, y + rowHeight*3, 0xFFFFFFFF, true);
            graphics.text(font, wlText,     x - font.width(wlText.getVisualOrderText()) / 2,     y + rowHeight*4, 0xFFFFFFFF, true);

            if (currentWinstreak > 0) {
                graphics.text(font, winstreakText, x - font.width(winstreakText.getVisualOrderText()) / 2, y + rowHeight*5, 0xFFFFFFFF, true);
            }
        }
    }
}