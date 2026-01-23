package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class parkourChatListener implements ClientModInitializer {

    public static double time = 0.0;
    public static double worldRecord = 0;
    public static double personalBest = 0;
    // FLAG: Tells the module that parsing for this race is complete
    public static boolean statsUpdatedThisTick = false;

    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();

            if (msg.contains("Aika:")) {
                if (!msg.contains("[")) {
                    try {
                        String numberPart = msg.substring(msg.indexOf("Aika:") + 5).trim();
                        time = Double.parseDouble(numberPart);
                        debugLog("Updated Time: {}", time);
                    } catch (NumberFormatException e) {
                        debugLog("Failed to parse Time: {}", msg);
                    }
                }
            }

            // Set the flag true ONLY when the PB/Previous time arrives,
            // as this is usually the last message in the sequence
            if (msg.contains("Aikaisempi:")) {
                if (!msg.contains("[")) {
                    try {
                        String numberPart = msg.substring(msg.indexOf("Aikaisempi:") + 11).trim();
                        personalBest = Double.parseDouble(numberPart);
                        statsUpdatedThisTick = true;
                        debugLog("Updated PersonalBest (aikaisempi): {}", personalBest);
                    } catch (NumberFormatException e) {
                        debugLog("Failed to parse PB (aikaisempi): {}", msg);
                    }
                }
            }

            if (msg.contains("PB:")) {
                if (!msg.contains("[")) {
                    try {
                        String numberPart = msg.substring(msg.indexOf("PB:") + 3).trim();
                        personalBest = Double.parseDouble(numberPart);
                        statsUpdatedThisTick = true;
                        debugLog("Updated PersonalBest (PB): {}", personalBest);
                    } catch (NumberFormatException e) {
                        debugLog("Failed to parse PB (PB): {}", msg);
                    }
                }
            }

// Inside parkourChatListener.java
            if (msg.contains("Paras aika:")) {
                if (!msg.contains("[")) {
                    try {
                        String numberPart = msg.substring(msg.lastIndexOf(":") + 1).trim();
                        worldRecord = Double.parseDouble(numberPart);
                        // ADD THIS: In case WR is the last message sent by the server
                        statsUpdatedThisTick = true;
                        debugLog("Updated WR: {}", worldRecord);
                    } catch (Exception e) {
                        debugLog("Failed to parse WR: {}", msg);
                    }
                }
            }
        });
    }
}