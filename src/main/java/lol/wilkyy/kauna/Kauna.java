package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class Kauna implements ModInitializer {

    private static boolean inKahakka = false;

    @Override
    public void onInitialize() {
        GListScanner.init();
        autoReadyUp.init();
        versionCheck.checkVersion();
        KaunaConfig.load();
        kahakkaJoinCheck();
        kahakkaLeaveCheck();
        debugLog("Kannat kaunaa... Mod initialized!");
    }

    public void kahakkaJoinCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Realmin Kahakkaan!") && !msg.contains("[")) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.inGameHud == null) return;

                inKahakka = true;


                // Create the primary subtitle: ᴋᴀɴɴᴀᴛ ᴋᴀᴜɴᴀᴀ
                MutableText subtitleText = Text.literal("ᴋᴀɴɴᴀᴛ ᴋᴀᴜɴᴀᴀ")
                        .setStyle(Style.EMPTY
                                .withColor(TextColor.fromRgb(0xDE2B56))
                                .withBold(true)
                                .withItalic(true));

                // If an update is found, append a new line with the warning
                if (versionCheck.updateAvailable && KaunaConfig.INSTANCE.CheckForUpdates) {
                    MutableText titleText = (Text.literal("Päivitys saatavilla!")
                            .setStyle(Style.EMPTY
                                    .withColor(Formatting.GOLD)
                                    .withBold(true)
                                    .withItalic(false)));
                    client.inGameHud.setTitle(titleText);
                } else {
                    client.inGameHud.setTitle(Text.literal(""));
                }

                client.inGameHud.setSubtitle(subtitleText);
                client.inGameHud.setTitleTicks(10, 40, 10); // Increased stay time to 40 ticks (2s) so player can read both lines

                debugLog("Player joined Kahakka" + (versionCheck.updateAvailable ? " (Update Notified)" : ""));
            }
        });
    }

    public void kahakkaLeaveCheck() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            inKahakka = false;
            debugLog("Player disconnected from Kahakka");
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            inKahakka = false;
            debugLog("Joined different server, reset inKahakka");
        });
    }

    public static boolean inKahakka() {
        return inKahakka;
    }

}