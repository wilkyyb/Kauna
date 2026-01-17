package lol.wilkyy.kauna;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Kauna implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("Kauna");

	@Override
	public void onInitialize() {
		kahakkaJoinCheck();
		LOGGER.info("Kauna initialized");
	}
	public void kahakkaJoinCheck() {
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			String msg = message.getString();
			if (msg.contains("Realmin Kahakkaan!") && !msg.contains("[")) {
				MinecraftClient client = MinecraftClient.getInstance();
				client.inGameHud.setTitle(Text.literal(""));
				client.inGameHud.setSubtitle(
						Text.literal("ᴋᴀɴɴᴀᴛ ᴋᴀᴜɴᴀᴀ")
								.setStyle(Style.EMPTY
										.withColor(TextColor.fromRgb(0xDE2B56))
										.withBold(true)
										.withItalic(true))
				);
				client.inGameHud.setTitleTicks(10, 15, 10);
				LOGGER.info("Liittyi Kahakkaan");
			}
		});
	}
}