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
            if (client.player == null) return true;

            Matcher matcher = PROXY_PATTERN.matcher(content);
            boolean isProxyLine = matcher.find();
            boolean isFooter = content.contains("Pelaajia yhteensä:");

            if (isProxyLine) {
                String proxyName = matcher.group(1);
                String rawPlayers = matcher.group(2);
                String[] onlinePlayers = rawPlayers.split(", ");

                // We will build a new formatted string for this line
                StringBuilder highlightedLine = new StringBuilder("§8[§6" + proxyName + "§8] §8(§6" + onlinePlayers.length + "§8) §2» ");

                for (int i = 0; i < onlinePlayers.length; i++) {
                    String p = onlinePlayers[i].trim();
                    String cleanName = p.replace(".", "");
                    boolean isFriend = false;

                    // Check if this player is in the config friends list
                    for (String friend : KaunaConfig.INSTANCE.friendsList) {
                        if (cleanName.equalsIgnoreCase(friend.trim())) {
                            isFriend = true;
                            foundFriends.add("§8[§a" + proxyName + "§8] "  + "§7" + friend);
                            break;
                        }
                    }

                    // Highlight friend with Aqua (&b), others stay Gray (&7)
                    if (isFriend) {
                        highlightedLine.append("§a").append(p);
                    } else {
                        highlightedLine.append("§7").append(p);
                    }

                    // Add comma if not the last player
                    if (i < onlinePlayers.length - 1) {
                        highlightedLine.append("§8, ");
                    }

                    // Manual search logic
                    if (searchTarget != null && cleanName.equalsIgnoreCase(searchTarget)) {
                        searchResultProxy = proxyName;
                    }
                }

                // Send the highlighted line and CANCEL the original server message
                client.player.sendMessage(Text.literal(highlightedLine.toString()), false);
                return false; // This cancels the original gray server message
            }

            if (isFooter) {
                // Your existing summary logic...
                if (isManualSearch && searchTarget != null) {
                    client.player.sendMessage(Text.literal(" §7- " + (searchResultProxy != null ? "§b" + searchTarget + " §8-> §a" + searchResultProxy : "§c" + searchTarget + " (Ei paikalla)")), false);
                }

                client.player.sendMessage(Text.literal("§2» §6Kavereita paikalla:"), false);

                if (!foundFriends.isEmpty()) {
                    for (String line : foundFriends) client.player.sendMessage(Text.literal(" §7- " + line), false);
                } else {
                    client.player.sendMessage(Text.literal(" §7- §oEi ketään online."), false);
                }

                client.player.sendMessage(Text.literal(""), false);
                foundFriends.clear();
                isManualSearch = false;
                searchTarget = null;
                return true; // Let the footer show
            }

            return true;
        });
    }
}