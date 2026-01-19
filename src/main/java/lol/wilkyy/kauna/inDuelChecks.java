package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class inDuelChecks implements ClientModInitializer {

    private static boolean inDuel = false;
    private static boolean inParkourDuel = false;

    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Peli päättyi!") && !msg.contains("[")) {
                new Thread(() -> {
                    try {
                        MinecraftClient client = MinecraftClient.getInstance();
                        Thread.sleep(4000);
                        inDuel = false;
                        inParkourDuel = false;
                        client.inGameHud.setTitle(Text.literal(""));
                        client.inGameHud.setSubtitle(Text.literal(""));
                        client.inGameHud.setTitleTicks(0, 0, 0);
                        debugLog("Duel ended");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Aloitetaan kaksintaistoa..") && !msg.contains("[")) {
                inDuel=true;
                debugLog("Duel started");
            }
        });
        MinecraftClient clientt = MinecraftClient.getInstance();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Ohita kartta:") && !msg.contains("[") && !(clientt.player != null && clientt.interactionManager != null && clientt.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)) {
                inParkourDuel=true;
                debugLog("Parkour Duel started");
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            inDuel = false;
            inParkourDuel = false;
            client.inGameHud.setTitle(Text.literal(""));
            client.inGameHud.setSubtitle(Text.literal(""));
            client.inGameHud.setTitleTicks(0, 0, 0);
            debugLog("Duel ended due to disconnect");
        });
    }
    public static boolean inDuel() {
        return inDuel;
    }
    public static boolean inParkourDuel() {
        return inParkourDuel;
    }
}
