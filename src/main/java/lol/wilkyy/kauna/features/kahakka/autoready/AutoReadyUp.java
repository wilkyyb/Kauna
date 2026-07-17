package lol.wilkyy.kauna.features.kahakka.autoready;

import com.mojang.blaze3d.platform.InputConstants;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.mixin.features.kahakka.parkour.KeyMappingAccessor; // Import your accessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class AutoReadyUp {

    public static boolean isSearchingForReady = false;
    private static boolean wasForcedByMod = false;
    private static long cooldownEndTime = 0;
    private static final long COOLDOWN_MS = 7000;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            String playerName = client.player.getGameProfile().name();
            String chatText = ChatFormatting.stripFormatting(message.getString());

            if (chatText.contains("on valmis!") || KaunaConfig.autoReady) {
                debugLog("Detected readiness chat message: '" + chatText + "'");

                if (chatText.contains(playerName)) {
                    debugLog("Match found for player target: " + playerName);
                    isSearchingForReady = false;
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            KeyMapping sneakKey = client.options.keyShift;

            if (isSearchingForReady) {
                sneakKey.setDown(true);
                wasForcedByMod = true;
            } else if (wasForcedByMod) {
                wasForcedByMod = false;

                InputConstants.Key boundKey = ((KeyMappingAccessor) sneakKey).getBoundKey();
                int currentBindingCode = boundKey.getValue();

                if (!InputConstants.isKeyDown(client.getWindow(), currentBindingCode)) {
                    sneakKey.setDown(false);
                }
            }
        });
    }

    public static void tryTriggerCrouch() {
        long currentTime = System.currentTimeMillis();

        if (isSearchingForReady) {
            return;
        }

        if (currentTime >= cooldownEndTime) {
            isSearchingForReady = true;
            cooldownEndTime = currentTime + COOLDOWN_MS;
            debugLog("Auto crouched");
        }
    }
}