package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.ConfigHandler;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.kahakka.autoReadyUp;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class Kauna implements ModInitializer {

    private static boolean inKahakka = false;

    @Override
    public void onInitialize() {
        GListScanner.init();
        autoReadyUp.init();
        versionCheck.checkVersion();
        KaunaConfig.load();

        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ConfigHandler.register(dispatcher);
        });

        kahakkaJoinCheck();
        kahakkaLeaveCheck();
        debugLog("Kannat kaunaa... Mod initialized!");
    }

    public static boolean isCurrentlyOnRealmi() {
        if (!KaunaConfig.INSTANCE.inRealmiCheck) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address.toLowerCase().contains("realmi.fi");
        }
        return false;
    }

    public void kahakkaJoinCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Realmin Kahakkaan!") && !msg.contains("[") && Kauna.isCurrentlyOnRealmi()) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.inGameHud == null) return;
                inKahakka = true;

                MutableText subtitleText = Text.literal("ᴋᴀɴɴᴀᴛ ᴋᴀᴜɴᴀᴀ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDE2B56)).withBold(true).withItalic(true));

                if (versionCheck.updateAvailable && KaunaConfig.INSTANCE.CheckForUpdates) {
                    client.inGameHud.setTitle(Text.literal("Päivitys saatavilla!").formatted(Formatting.GOLD).copy().append(""));
                } else {
                    client.inGameHud.setTitle(Text.literal(""));
                }

                client.inGameHud.setSubtitle(subtitleText);
                client.inGameHud.setTitleTicks(10, 40, 10);
            }
        });
    }

    public void kahakkaLeaveCheck() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inKahakka = false);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> inKahakka = false);
    }

    public static boolean inKahakka() { return inKahakka; }
}