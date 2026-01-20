package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class Kauna implements ModInitializer {

    private static boolean inKahakka = false;


    public void onInitialize() {
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

                inKahakka = true;

                client.inGameHud.setTitle(Text.literal(""));
                client.inGameHud.setSubtitle(
                        Text.literal("ᴋᴀɴɴᴀᴛ ᴋᴀᴜɴᴀᴀ")
                                .setStyle(Style.EMPTY
                                        .withColor(TextColor.fromRgb(0xDE2B56))
                                        .withBold(true)
                                        .withItalic(true))
                );
                client.inGameHud.setTitleTicks(10, 15, 10);
                debugLog("Player joined Kahakka");
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