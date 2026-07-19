package lol.wilkyy.kauna.features.kahakka.autorequeue;

import lol.wilkyy.kauna.Kauna;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.features.kahakka.stats.StatsManager;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class AutoRequeue {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long DEFAULT_DELAY_MS = 4250;

    public static void requeue() {
        requeue(StatsManager.getCurrentKit(), DEFAULT_DELAY_MS);
    }

    public static void requeue(String kit, long delayMillis) {
        if (!KaunaConfig.INSTANCE.autoRequeue || !Kauna.isCurrentlyOnRealmi()) {
            debugLog("AutoRequeue skipped: disabled or not on Realmi.");
            return;
        }

        if (kit == null || kit.isBlank() || kit.equals("Pelaa Kittiä")) {
            debugLog("AutoRequeue skipped: no current kit detected.");
            return;
        }

        Minecraft client = Minecraft.getInstance();

        scheduler.schedule(() -> {
            client.execute(() -> {
                if (client.player != null) {
                    String command = "duelqueue " + kit;

                    if(KaunaConfig.INSTANCE.autoRequeue) {
                        client.player.connection.sendCommand(command);
                    }

                    debugLog("AutoRequeue sent: /{}", command);
                }
            });
        }, delayMillis, TimeUnit.MILLISECONDS);
    }
}