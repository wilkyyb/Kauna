package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.ConfigHandler;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.kahakka.autoReadyUp;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

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
        Minecraft client = Minecraft.getInstance();
        if (client.getCurrentServer() != null) {
            return client.getCurrentServer().ip.toLowerCase().contains("realmi.fi");
        }
        return false;
    }

    public void kahakkaJoinCheck() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            if (msg.contains("Realmin Kahakkaan!") && !msg.contains("[") && Kauna.isCurrentlyOnRealmi()) {
                Minecraft client = Minecraft.getInstance();
                if (client.gui == null) return;
                inKahakka = true;

                MutableComponent subtitleText = Component.literal("ᴋᴀɴɴᴀᴛ ᴋᴀᴜɴᴀᴀ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDE2B56)).withBold(true).withItalic(true));

                if (versionCheck.updateAvailable && KaunaConfig.INSTANCE.CheckForUpdates) {
                    client.gui.setTitle(Component.literal("Päivitys saatavilla!").withStyle(ChatFormatting.GOLD).copy().append(""));
                } else {
                    client.gui.setTitle(Component.literal(""));
                }

                client.gui.setSubtitle(subtitleText);
                client.gui.setTimes(10, 40, 10);
            }
        });
    }

    public void kahakkaLeaveCheck() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inKahakka = false);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> inKahakka = false);
    }

    public static boolean inKahakka() { return inKahakka; }
}