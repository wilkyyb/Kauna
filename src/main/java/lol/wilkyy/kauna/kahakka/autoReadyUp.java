package lol.wilkyy.kauna.kahakka;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class autoReadyUp {
    private static int crouchTimer = 0;
    private static int cooldownTimer = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (crouchTimer > 0) {
                if (client.options != null) {
                    client.options.keyShift.setDown(true);
                }
                crouchTimer--;

                if (crouchTimer == 0) {
                    if (client.options != null) {
                        client.options.keyShift.setDown(false);
                    }
                }
            }

            if (cooldownTimer > 0) {
                cooldownTimer--;
            }
        });
    }

    public static void startCrouch(int ticks) {
        if (cooldownTimer <= 0) {
            crouchTimer = ticks;
            cooldownTimer = 100;
        }
    }
}