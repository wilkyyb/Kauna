package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import lol.wilkyy.kauna.config.KaunaConfig; // normal import

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class autoChat implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Peli päättyi!") && !msg.contains("[") && KaunaConfig.INSTANCE.autoGG) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(KaunaConfig.INSTANCE.autoGGDelay);

                            String messageToSend;
                            if (KaunaConfig.INSTANCE.customAutoGG) {
                                messageToSend = KaunaConfig.INSTANCE.customAutoGGText;
                            } else {
                                messageToSend = "gg";
                            }

                            // Schedule back on client thread
                            client.execute(() -> {
                                if (client.player != null) {
                                    client.player.networkHandler.sendChatMessage(messageToSend);
                                    debugLog("AutoGG sent after delay: {}", messageToSend);
                                }
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, "AutoGG-Delay-Thread").start();
                }
                if (client.player != null) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(KaunaConfig.INSTANCE.autoGGDelay + 300);

                            if (KaunaConfig.INSTANCE.autoEz) {
                                if (client.player != null) {
                                    client.player.networkHandler.sendChatMessage("ez");
                                    debugLog("AutoEz sent after delay: {}");
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
        });
    }
}
