package lol.wilkyy.kauna;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class autoReadyUp {
    private static int crouchTimer = 0;
    private static int cooldownTimer = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle the active crouching logic
            if (crouchTimer > 0) {
                if (client.options != null) {
                    client.options.sneakKey.setPressed(true);
                }
                crouchTimer--;

                if (crouchTimer == 0) {
                    if (client.options != null) {
                        client.options.sneakKey.setPressed(false);
                    }
                }
            }

            // Handle the cooldown logic
            if (cooldownTimer > 0) {
                cooldownTimer--;
            }
        });
    }

    public static void startCrouch(int ticks) {
        // Only start a crouch if we are not currently on cooldown
        if (cooldownTimer <= 0) {
            crouchTimer = ticks;
            // Set cooldown to 100 ticks (5 seconds) to prevent spamming
            // during the same pregame countdown
            cooldownTimer = 100;
        }
    }
}