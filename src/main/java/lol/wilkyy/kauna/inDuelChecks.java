package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class inDuelChecks implements ClientModInitializer {

    private static boolean inDuel = false;
    private static boolean inParkourDuel = false;

    private static int duelEndTimer = -1; // -1 means timer is not running

    public static boolean duelStarted = false;

    public void onInitializeClient() {
        // Handle the countdown and HUD clearing
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (duelEndTimer > 0) {
                duelEndTimer--;

                if (duelEndTimer == 0) {
                    inDuel = false;
                    inParkourDuel = false;

                    if (client.inGameHud != null) {
                        client.inGameHud.setTitle(Text.literal(""));
                        client.inGameHud.setSubtitle(Text.literal(""));
                        client.inGameHud.setTitleTicks(0, 0, 0);
                    }

                    debugLog("Duel state and HUD reset.");
                    duelEndTimer = -1;
                }
            }
        });

        // Detect Duel Start
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Aloitetaan kaksintaistoa..") && !msg.contains("[")) {
                inDuel = true;
                debugLog("Duel started");
            }
        });

        // Detect Parkour Duel Start
        MinecraftClient clientt = MinecraftClient.getInstance();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Ohita kartta:") && !msg.contains("[") && !(clientt.player != null && clientt.interactionManager != null && clientt.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)) {
                inParkourDuel = true;
                debugLog("Parkour Duel started");
            }
        });

        // NEW: Detect Duel End / Spectator Exit
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            // Check for game end OR leaving spectator mode
            if ((msg.contains("Peli päättyi!") && !msg.contains("["))) {
                duelEndTimer = 80; // Triggers the 4-second reset countdown
                debugLog("Duel end message detected, starting reset timer.");
            }
        });
        // NEW: Detect Duel End / Spectator Exit
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Poistuit kaksintaiston katselemisesta!")) {
                inDuel = false;
                inParkourDuel = false;
                debugLog("Exited duel spectator mode, starting reset timer.");
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                    if (client.inGameHud != null) {
                        client.inGameHud.setTitle(Text.literal(""));
                        client.inGameHud.setSubtitle(Text.literal(""));
                        client.inGameHud.setTitleTicks(0, 0, 0);
                    }
                });
            }
        });

        // Handle Disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            inDuel = false;
            inParkourDuel = false;
            client.inGameHud.setTitle(Text.literal(""));
            client.inGameHud.setSubtitle(Text.literal(""));
            client.inGameHud.setTitleTicks(0, 0, 0);
            duelEndTimer = -1;
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