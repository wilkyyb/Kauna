package lol.wilkyy.kauna.kahakka;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.kahakka.parkour.parkourModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class inDuelChecks implements ClientModInitializer {

    private static boolean inDuel = false;
    public static boolean inParkourDuel = false;

    private static int duelEndTimer = -1; // -1 = not running

    public static boolean duelStarted = false;

    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (duelEndTimer > 0) {
                duelEndTimer--;

                if (duelEndTimer == 0) {
                    inDuel = false;
                    inParkourDuel = false;

                    if (client.gui != null) {
                        client.gui.setTitle(Component.literal(""));
                        client.gui.setSubtitle(Component.literal(""));
                        client.gui.setTimes(0, 0, 0);
                    }

                    debugLog("Duel state and HUD reset.");
                    duelEndTimer = -1;
                }
            }
        });

        // Detect Duel Start
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Aloitetaan kaksintaistoa..") && !msg.contains("[") && Kauna.isCurrentlyOnRealmi()) {
                inDuel = true;
                debugLog("Duel started");
            }
        });

        // Detect Parkour Duel Start
        Minecraft clientt = Minecraft.getInstance();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Ohita kartta:") && !msg.contains("[") && !(clientt.player != null && clientt.gameMode != null && clientt.gameMode.getPlayerMode() == GameType.SPECTATOR && Kauna.isCurrentlyOnRealmi())) {
                parkourModule.showParkourTitle();
                inParkourDuel = true;
                debugLog("Parkour Duel started");
            }
        });

        // Detect Duel End / Spectator Exit
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            // Check for game end OR leaving spectator mode
            if ((msg.contains("Peli päättyi!") && !msg.contains("["))&& Kauna.isCurrentlyOnRealmi()) {
                duelEndTimer = 80; // Triggers the 4-second reset countdown
                debugLog("Duel end message detected, starting reset timer.");
                AutoReadyUp.isSearchingForReady = false;
            }
        });
        // Detect Duel End / Spectator Exit
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Poistuit kaksintaiston katselemisesta!")) {
                inDuel = false;
                inParkourDuel = false;
                debugLog("Exited duel spectator mode");
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                    if (client.gui != null) {
                        client.gui.setTitle(Component.literal(""));
                        client.gui.setSubtitle(Component.literal(""));
                        client.gui.setTimes(0, 0, 0);
                    }
                });
            }
        });

        // Round AutoReadyUp Reset
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Voitti kierroksen!")) {
                AutoReadyUp.isSearchingForReady = false;
            }
        });

        // Handle Disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            inDuel = false;
            inParkourDuel = false;
            client.gui.setTitle(Component.literal(""));
            client.gui.setSubtitle(Component.literal(""));
            client.gui.setTimes(0, 0, 0);
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