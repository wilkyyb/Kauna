package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GListScanner implements ClientModInitializer {
    private static final Pattern PROXY_PATTERN = Pattern.compile("\\[(\\w+)\\] \\(\\d+\\) » (.+)");
    private static final List<String> foundFriends = new ArrayList<>();

    private static String searchTarget = null;
    private static String searchResultProxy = null;
    private static boolean isManualSearch = false;

    public static void setSearchTarget(String name) {
        searchTarget = name;
        searchResultProxy = null;
        isManualSearch = true;
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("glist")
                    .then(ClientCommandManager.literal("find")
                            .then(ClientCommandManager.argument("playername", StringArgumentType.string())
                                    .executes(context -> {
                                        String target = StringArgumentType.getString(context, "playername");
                                        GListScanner.setSearchTarget(target);
                                        if (context.getSource().getClient().player != null) {
                                            context.getSource().getClient().player.networkHandler.sendChatCommand("glist");
                                        }
                                        return 1;
                                    })
                            )
                    )
            );
        });
    }

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String content = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();

            boolean isHeader = content.contains("» Realmi «");
            boolean isFooter = content.contains("Pelaajia yhteensä:");
            Matcher matcher = PROXY_PATTERN.matcher(content);
            boolean isProxyLine = matcher.find();

            if (isProxyLine) {
                String proxyName = matcher.group(1);
                String[] onlinePlayers = matcher.group(2).split(", ");

                if (searchTarget != null) {
                    for (String p : onlinePlayers) {
                        if (p.trim().replace(".", "").equalsIgnoreCase(searchTarget)) {
                            searchResultProxy = proxyName;
                        }
                    }
                }

                for (String friend : KaunaConfig.INSTANCE.friendsList) {
                    for (String p : onlinePlayers) {
                        if (p.trim().replace(".", "").equalsIgnoreCase(friend.trim())) {
                            foundFriends.add("§b" + friend + " §8-> §a" + proxyName);
                            break;
                        }
                    }
                }
            }

            if (isFooter) {
                if (client.player != null) {
                    if (isManualSearch && searchTarget != null) {
                        if (searchResultProxy != null) {
                            client.player.sendMessage(Text.literal(" §7- §b" + searchTarget + " §8-> §a" + searchResultProxy), false);
                        } else {
                            client.player.sendMessage(Text.literal(" §7- §c" + searchTarget + " (Ei paikalla)"), false);
                        }
                    }

                    client.player.sendMessage(Text.literal("§c§l» §6§lKavereita paikalla:"), false);


                    if (!foundFriends.isEmpty()) {
                        for (String line : foundFriends) {
                            client.player.sendMessage(Text.literal(" §7- " + line), false);
                        }
                    } else {
                        client.player.sendMessage(Text.literal(" §7- §oEi ketään online."), false);
                    }


                    client.player.sendMessage(Text.literal(""), false);
                }

                foundFriends.clear();
                isManualSearch = false;
                searchTarget = null;
                return true;
            }

            // Return true for everything else so the original glist still displays
            return true;
        });
    }
}