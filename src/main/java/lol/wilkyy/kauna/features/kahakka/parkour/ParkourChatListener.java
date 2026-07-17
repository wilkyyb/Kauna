package lol.wilkyy.kauna.features.kahakka.parkour;

import lol.wilkyy.kauna.Kauna;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class ParkourChatListener implements ClientModInitializer {

    public static double time = 0.0;
    public static double worldRecord = 0;
    public static double personalBest = 0;
    public static boolean statsUpdatedThisTick = false;
    public static double currentTime = 0.0;


    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();

            if (msg.contains("Aika:") && !msg.contains(" - PB:") && Kauna.isCurrentlyOnRealmi()) {
                if (!msg.contains("[")) {
                    try {
                        String numberPart = msg.substring(msg.indexOf("Aika:") + 5).trim();
                        time = Double.parseDouble(numberPart);
                        statsUpdatedThisTick = true; // always trigger splits display
                        debugLog("Updated Time: {}", time);
                    } catch (NumberFormatException e) {
                        debugLog("Failed to parse Time: {}", msg);
                    }
                }
            }

            if (msg.contains("Aikaisempi:") && Kauna.isCurrentlyOnRealmi()) {
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

            if (msg.contains("PB:") && Kauna.isCurrentlyOnRealmi()) {
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

            if (msg.contains("Paras aika:") && Kauna.isCurrentlyOnRealmi()) {
                if (!msg.contains("[")) {
                    try {
                        String numberPart = msg.substring(msg.lastIndexOf(":") + 1).trim();
                        worldRecord = Double.parseDouble(numberPart);
                        statsUpdatedThisTick = false; // reset for new duel
                        debugLog("Updated WR: {}", worldRecord);
                    } catch (Exception e) {
                        debugLog("Failed to parse WR: {}", msg);
                    }
                }
            }
        });
    }
}