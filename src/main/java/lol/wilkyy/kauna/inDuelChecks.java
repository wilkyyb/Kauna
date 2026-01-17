package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class inDuelChecks implements ClientModInitializer {

    private static boolean inDuel = false;

    public static final Logger LOGGER = LogManager.getLogger("KaunaInDuelChecks");

    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Peli päättyi!") && !msg.contains("[")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(4000);
                        inDuel = false;
                        LOGGER.info("Duel ended");
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
                LOGGER.info("Duel started");
            }
        });
    }
    public static boolean inDuel() {
        return inDuel;
    }
}
