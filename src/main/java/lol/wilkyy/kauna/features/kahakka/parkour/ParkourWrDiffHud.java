package lol.wilkyy.kauna.features.kahakka.parkour;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

public class ParkourWrDiffHud implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(Kauna.MOD_ID, "parkour_wr_diff"),
                ParkourWrDiffHud::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (ParkourChatListener.currentTime <= 0) return;
        if (ParkourChatListener.worldRecord <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double diff = ParkourChatListener.currentTime - ParkourChatListener.worldRecord;
        boolean ahead = diff < 0;
        int color = ahead ? 0xFF00FF00 : 0xFFfc5454;
        String sign = ahead ? "" : "+";
        String text = String.format("%s%.3f", sign, diff);

        Component diffComponent = Component.literal(text)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color & 0xFFFFFF)).withBold(true));

        int x = graphics.guiWidth() / 2 - mc.font.width(diffComponent.getVisualOrderText()) / 2;
        int y = graphics.guiHeight() / 2 + 10;

        if (KaunaConfig.worldRecordTimer) {
            graphics.text(mc.font, diffComponent, x, y, color, true);
        }
    }
}