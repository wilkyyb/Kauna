package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class autoReadyUp implements ClientModInitializer {

    private static boolean shoulderCrouchPending = false;
    private static int crouchTimer = -1;
    private static int cooldownTimer = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            if (cooldownTimer > 0) cooldownTimer--;

            // 1. Execute Crouch
            if (shoulderCrouchPending) {
                client.options.sneakKey.setPressed(true);
                shoulderCrouchPending = false;
                crouchTimer = 1;
                debugLog("Auto-crouch executed via Mixin trigger");
                return;
            }

            // 2. Release Crouch
            if (crouchTimer > 0) {
                crouchTimer--;
                if (crouchTimer == 0) {
                    client.options.sneakKey.setPressed(false);
                    crouchTimer = -1;
                }
            }
        });
    }

    // This method is called by the Mixin
    public static void handleActionbarText(String content) {
        // Check if the feature is enabled in the config
        if (!KaunaConfig.INSTANCE.autoReadyUp) return;

        // DUMP TO CONSOLE for debugging
        System.out.println("[Actionbar Mixin] " + content);

        if (content.contains("(kyykkää)")) {
            if (crouchTimer == -1 && cooldownTimer <= 0) {
                shoulderCrouchPending = true;
                cooldownTimer = 60; // 3 second cooldown
            }
        }
    }
}