package lol.wilkyy.kauna.features.chat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class PlayerNameResolver implements ClientModInitializer {

    private static boolean inDisguise = false;
    private static String disguiseName = null;

    private static int nameCheckDelayTicks = -1;
    private static final int NAME_CHECK_DELAY = 20;

    public static boolean isInDisguise() {
        return inDisguise;
    }

    public static String getPlayerName() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return "";

        if (inDisguise && disguiseName != null) {
            return disguiseName;
        }

        return client.player.getGameProfile().name();
    }

    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();

            if (msg.contains("Realmi » Olet naamioitunut") && !msg.contains("[")) {
                inDisguise = true;
                disguiseName = null; // clear stale name until re-captured
                nameCheckDelayTicks = NAME_CHECK_DELAY;
                debugLog("inDisguise = True");
            }

            if (msg.contains("Olet nyt oma itsesi") && !msg.contains("[")) {
                inDisguise = false;
                disguiseName = null;
                nameCheckDelayTicks = -1;
                debugLog("inDisguise = False");
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            inDisguise = false;
            disguiseName = null;
            nameCheckDelayTicks = -1;
            debugLog("inDisguise = False");
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (nameCheckDelayTicks < 0) return;

            nameCheckDelayTicks--;
            if (nameCheckDelayTicks == 0) {
                captureDisguiseName(client);
            }
        });
    }

    private static void captureDisguiseName(Minecraft client) {
        if (client.player == null || client.getConnection() == null) return;

        var info = client.getConnection().getPlayerInfo(client.player.getGameProfile().id());
        if (info != null && info.getProfile() != null && info.getProfile().name() != null) {
            disguiseName = info.getProfile().name();
            debugLog("Captured disguise name: " + disguiseName);
        } else {
            debugLog("Failed to capture disguise name — profile was null");
        }
    }
}