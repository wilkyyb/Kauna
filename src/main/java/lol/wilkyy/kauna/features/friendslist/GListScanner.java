package lol.wilkyy.kauna.features.friendslist;

import lol.wilkyy.kauna.config.Colors;
import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GListScanner {
    private static final Pattern PROXY_PATTERN = Pattern.compile("\\[(\\w+)\\] \\(\\d+\\) » (.+)");
    private static final List<MutableComponent> foundFriends = new ArrayList<>();


    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();

            Matcher matcher = PROXY_PATTERN.matcher(content);
            if (matcher.find()) {
                String proxyName = matcher.group(1);
                String playersBlob = matcher.group(2);

                String[] onlinePlayers = playersBlob.split(", ");

                for (String friend : KaunaConfig.INSTANCE.friendsList) {
                    for (String onlinePlayer : onlinePlayers) {
                        String trimmed = onlinePlayer.trim().replace(".", "");
                        boolean isVanished = trimmed.startsWith("[V]");
                        String cleanName = trimmed.replaceAll("^\\[V\\]", "");

                        if (cleanName.equalsIgnoreCase(friend.trim())) {
                            MutableComponent entry = Component.literal("§8[")
                                    .append(Colors.getProxyName(proxyName))
                                    .append(Component.literal("§8] §7" + friend));

                            if (isVanished) {
                                entry.append(Component.literal(" §8[§4V§8]§7"));
                            }

                            foundFriends.add(entry);
                            break;
                        }
                    }
                }
            }

            if (content.contains("Pelaajia yhteensä:") && !foundFriends.isEmpty()) {
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("§2» §6Kavereita paikalla:"));
                    for (MutableComponent line : foundFriends) {
                        client.player.sendSystemMessage(Component.literal(" §7- ").append(line));
                    }
                    client.player.sendSystemMessage(Component.literal(""));
                }
                foundFriends.clear();
            }
        });
    }
}