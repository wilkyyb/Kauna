package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.Logger;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.apache.logging.log4j.LogManager;


public class parkourChatListener implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("KaunaParkour");

    public static double time = 0.0;
    public static double worldRecord = 0;
    public static double personalBest = 0;

    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();

            if (msg.contains("Aika:")) {
                if (!msg.contains("CHAT")) {
                    try {
                        // Extract everything after "Aika:"
                        String numberPart = msg.substring(msg.indexOf("Aika:") + 5).trim();
                        time = Double.parseDouble(numberPart);
                        LOGGER.info("Updated Time: {}", time);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Failed to parse Time: {}", msg);
                    }
                }
                LOGGER.info("Received Time: {}", msg);
            }

            if (msg.contains("Aikaisempi:")) {
                if (!msg.contains("[")) { // checkaa ettei pelaaja lähettäny (prefixistä)
                    try {
                        String numberPart = msg.substring(msg.indexOf("Aikaisempi:") + 11).trim();
                        personalBest = Double.parseDouble(numberPart);
                        LOGGER.info("Updated PersonalBest (aikaisempi): {}", personalBest);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Failed to parse PB (aikaisempi): {}", msg);
                    }
                }
                LOGGER.info("Received PB: {}", msg);
            }


            if (msg.contains("PB:")) {
                if (!msg.contains("[")) { // checkaa ettei pelaaja lähettäny (prefixistä)
                    try {
                        String numberPart = msg.substring(msg.indexOf("PB:") + 3).trim();
                        personalBest = Double.parseDouble(numberPart);
                        LOGGER.info("Updated PersonalBest (PB): {}", personalBest);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Failed to parse PB (PB): {}", msg);
                    }
                }
                LOGGER.info("Received PB: {}", msg);
            }

            if (msg.contains("Paras aika:")) {
                if (!msg.contains("[")) { // checkaa ettei pelaaja lähettäny (prefixistä)
                    try {
                        // Take everything after the last colon
                        String numberPart = msg.substring(msg.lastIndexOf(":") + 1).trim();
                        worldRecord = Double.parseDouble(numberPart);
                        LOGGER.info("Updated WorldRecord: {}", worldRecord);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to parse WR: {}", msg);
                    }

                }
            }
        });
    }
}
