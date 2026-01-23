package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GListScanner {
    // Matches patterns like "[hub2] (7) » Player1, Player2"
    private static final Pattern PROXY_PATTERN = Pattern.compile("\\[(\\w+)\\] \\(\\d+\\) » (.+)");
    private static final List<String> foundFriends = new ArrayList<>();

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();

            // 1. Detect individual proxy lines
            Matcher matcher = PROXY_PATTERN.matcher(content);
            if (matcher.find()) {
                String proxyName = matcher.group(1);
                String playersBlob = matcher.group(2);

                // Split the long string of players into individual names
                // We split by ", " because the server uses that to separate them
                String[] onlinePlayers = playersBlob.split(", ");

                for (String friend : KaunaConfig.INSTANCE.friendsList) {
                    for (String onlinePlayer : onlinePlayers) {
                        // Check for an EXACT match (ignoring dots if the server adds them for bedrock)
                        // We trim() just in case there are trailing spaces
                        String cleanName = onlinePlayer.trim().replace(".", "");

                        if (cleanName.equalsIgnoreCase(friend.trim())) {
                            foundFriends.add("§b" + friend + " §8-> §a" + proxyName);
                            break; // Move to next friend once found in this proxy
                        }
                    }
                }
            }

            // 2. Detect the end of the command output to print results
            if (content.contains("Pelaajia yhteensä:") && !foundFriends.isEmpty()) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("\n§c§l» §6§lKavereita paikalla:"), false);
                    for (String line : foundFriends) {
                        client.player.sendMessage(Text.literal(" §7- " + line), false);
                    }
                    client.player.sendMessage(Text.literal(""), false);
                }
                foundFriends.clear();
            }
        });
    }
}