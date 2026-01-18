package lol.wilkyy.kauna;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Kauna implements ModInitializer {

    private static boolean inKahakka = false;
    public static final Logger LOGGER = LogManager.getLogger("Kauna");


    public void onInitialize() {
        kahakkaJoinCheck();
        kahakkaLeaveCheck();
        LOGGER.info("Kannat kaunaa... Mod initialized!");
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
                LOGGER.info("Player joined Kahakka");
            }
        });
    }


    public void kahakkaLeaveCheck() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            inKahakka = false;
            LOGGER.info("Player disconnected from Kahakka");
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            inKahakka = false;
            LOGGER.info("Joined different server, reset inKahakka");
        });
    }

    public static boolean inKahakka() {
        return inKahakka;
    }
}