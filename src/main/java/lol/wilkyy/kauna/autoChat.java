package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import lol.wilkyy.kauna.config.KaunaConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class autoChat implements ClientModInitializer {

    // Using a thread pool to manage delays without blocking the game
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();

            // Guard clause: ensure player exists and message matches "Peli päättyi!" (and isn't a chat message with [)
            if (client.player == null || !msg.contains("Peli päättyi!") || msg.contains("[")) {
                return;
            }

            // Handle AutoGG - Now always uses AutoGGText
            if (KaunaConfig.INSTANCE.autoGG && Kauna.isCurrentlyOnRealmi()) {
                String messageToSend = KaunaConfig.INSTANCE.AutoGGText;

                scheduler.schedule(() -> {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage(messageToSend);
                            debugLog("AutoGG sent: {}", messageToSend);
                        }
                    });
                }, KaunaConfig.INSTANCE.autoGGDelay, TimeUnit.MILLISECONDS);
            }

            // Handle AutoEz
            if (KaunaConfig.INSTANCE.autoEz && Kauna.isCurrentlyOnRealmi()) {
                // Scheduled for the GG delay + 3100ms
                long ezDelay = KaunaConfig.INSTANCE.autoGGDelay + 3100;

                scheduler.schedule(() -> {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage("ez");
                            debugLog("AutoEz sent after {}ms", ezDelay);
                        }
                    });
                }, ezDelay, TimeUnit.MILLISECONDS);
            }
        });
    }
}