package lol.wilkyy.kauna.kahakka;

import lol.wilkyy.kauna.Kauna;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import lol.wilkyy.kauna.config.KaunaConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class AutoGG {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String msg = message.getString();
            Minecraft client = Minecraft.getInstance();

            if (client.player == null || !msg.contains("Peli päättyi!") || msg.contains("[")) {
                return;
            }

            //
            if (KaunaConfig.INSTANCE.autoGG && Kauna.isCurrentlyOnRealmi()) {
                scheduler.schedule(() -> {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.connection.sendChat("gg");
                            debugLog("AutoGG sent: {}", "gg");
                        }
                    });
                }, 300, TimeUnit.MILLISECONDS);
            }
        });
    }
}