package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.ConfigHandler;
import lol.wilkyy.kauna.config.KaunaConfig;
import lol.wilkyy.kauna.features.friendslist.GListScanner;
import lol.wilkyy.kauna.features.kahakka.autogg.AutoGG;
import lol.wilkyy.kauna.features.kahakka.autoready.AutoReadyUp;
import lol.wilkyy.kauna.features.kahakka.autorequeue.AutoRequeue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class Kauna implements ModInitializer {

    private static boolean inKahakka = false;
    public static final String MOD_ID = "kauna";
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Override
    public void onInitialize() {
        GListScanner.init();
        AutoReadyUp.init();
        UpdateChecker.checkVersion();
        KaunaConfig.load();
        AutoGG.init();

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

                MutableComponent subtitleText = Component.literal("")
                        .append(Component.literal("ᴋ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xA01C3C)).withBold(true)))
                        .append(Component.literal("ᴀ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAA1F40)).withBold(true)))
                        .append(Component.literal("ɴ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xB52145)).withBold(true)))
                        .append(Component.literal("ɴ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBF2449)).withBold(true)))
                        .append(Component.literal("ᴀ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC9264D)).withBold(true)))
                        .append(Component.literal("ᴛ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD42952)).withBold(true)))

                        .append(Component.literal(" ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD42952))))

                        .append(Component.literal("ᴋ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD42952)).withBold(true)))
                        .append(Component.literal("ᴀ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC9264D)).withBold(true)))
                        .append(Component.literal("ᴜ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBF2449)).withBold(true)))
                        .append(Component.literal("ɴ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xB52145)).withBold(true)))
                        .append(Component.literal("ᴀ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAA1F40)).withBold(true)))
                        .append(Component.literal("ᴀ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xA01C3C)).withBold(true)));

                if (UpdateChecker.updateAvailable && KaunaConfig.INSTANCE.CheckForUpdates && client.player != null) {
                    client.gui.hud.setTitle(Component.literal(""));
                    scheduler.schedule(() -> {
                        client.execute(() -> {
                            if (client.player != null) {
                                client.player.sendSystemMessage(Component.literal("                                  ").append(ConfigHandler.getPrefix()));
                                client.player.sendSystemMessage(
                                        Component.literal("                            ")
                                                .append(Component.literal("Päivitys Saatavilla")
                                                .withStyle(style -> style
                                                        .withColor(ChatFormatting.GOLD)
                                                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://modrinth.com/mod/kauna")))
                                                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Avaa Modrinth")))
                                                        .withUnderlined(true))
                                ));
                                client.player.sendSystemMessage(
                                        Component.literal("                                  (klikkaa)").withStyle(ChatFormatting.GRAY));
                                client.player.sendSystemMessage(Component.literal(""));
                                client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                            }

                        });
                    }, 100, TimeUnit.MILLISECONDS);
                } else {
                    client.gui.hud.setTitle(Component.literal(""));
                }
                client.gui.hud.setSubtitle(subtitleText);
                client.gui.hud.setTimes(10, 40, 10);
            }
        });
    }

    public void kahakkaLeaveCheck() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inKahakka = false);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> inKahakka = false);
    }

    public static boolean inKahakka() { return inKahakka; }
}