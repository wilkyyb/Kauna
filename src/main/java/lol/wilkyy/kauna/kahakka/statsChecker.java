package lol.wilkyy.kauna.kahakka;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class statsChecker implements ClientModInitializer {

    private static final String TARGET_TITLE = "kaksintaistotilastot";
    private boolean hasScraped = false;
    private int scanDelayTicks = 0;
    private int crouchTicks = 0;

    private static int statsDelayTicks = -1;
    public static String duelName = "";
    public static String targetOpponent = "";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (statsDelayTicks > 0) {
                statsDelayTicks--;
                if (statsDelayTicks == 0) {
                    if (client.player != null && !targetOpponent.isEmpty()) {
                        client.player.connection.sendCommand("stats " + targetOpponent);
                    }
                    statsDelayTicks = -1;
                }
            }

            if (crouchTicks > 0) {
                if (client.options != null) {
                    client.options.keyShift.setDown(true);
                }
                crouchTicks--;

                if (crouchTicks == 0) {
                    if (client.options != null) {
                        client.options.keyShift.setDown(false);
                    }
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!KaunaConfig.INSTANCE.autoStatsLookup) return;

            String content = message.getString();
            Pattern pattern = Pattern.compile("Realmi » Valmistaudu! Vastustaja: (\\w+)");
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                targetOpponent = matcher.group(1);

                if (!duelName.isEmpty()) {
                    triggerStatsLookup(duelName, targetOpponent);
                }
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            hasScraped = false;
            scanDelayTicks = 0;

            ScreenEvents.beforeRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
                if (screen1 instanceof AbstractContainerScreen<?> handledScreen) {
                    if (screen1.getTitle().getString().contains(TARGET_TITLE) && !hasScraped && !duelName.isEmpty()) {
                        if (scanDelayTicks > 0) {
                            scanDelayTicks--;
                            return;
                        }

                        if (isContainerPopulated(handledScreen)) {
                            processStats(handledScreen);
                        }
                    }
                }
            });
        });
    }

    public static void triggerStatsLookup(String dName, String pName) {
        duelName = dName;
        targetOpponent = pName;
        statsDelayTicks = 10;
    }

    private void processStats(AbstractContainerScreen<?> screen) {
        var handler = screen.getMenu();
        Minecraft client = Minecraft.getInstance();
        Slot nextPageSlot = null;
        boolean found = false;

        for (Slot slot : handler.slots) {
            if (slot.container instanceof net.minecraft.world.entity.player.Inventory) continue;

            ItemStack stack = slot.getItem();
            String name = stack.getHoverName().getString();

            if (name.equalsIgnoreCase(duelName)) {
                displayStatsLocally(stack);
                hasScraped = true;

                client.execute(() -> {
                    client.player.closeContainer();
                    if (KaunaConfig.INSTANCE.autoReadyUp && !statsChecker.duelName.contains("Parkour")) {
                        this.crouchTicks = 5;
                    }
                });

                resetState();
                found = true;
                break;
            }

            if (name.contains("Seuraava sivu")) {
                nextPageSlot = slot;
            }
        }

        if (!found && !hasScraped) {
            if (nextPageSlot != null) {
                client.gameMode.handleInventoryMouseClick(handler.containerId, nextPageSlot.index, 0, ClickType.PICKUP, client.player);
                scanDelayTicks = 5;
            } else {
                hasScraped = true;
                client.execute(() -> {
                    client.player.closeContainer();
                    if (!statsChecker.duelName.contains("Parkour")) {
                        crouchTicks = 2;
                    }
                });
                resetState();
            }
        }
    }

    private void displayStatsLocally(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        List<Component> lines = stack.getTooltipLines(
                Item.TooltipContext.EMPTY,
                client.player,
                TooltipFlag.NORMAL
        );

        int wins = 0, pelattu = 0;
        for (Component line : lines) {
            String text = line.getString().toLowerCase();
            if (text.contains("voittoja:")) wins = parseNumber(text);
            if (text.contains("pelattu:")) pelattu = parseNumber(text);
        }

        int lost = pelattu - wins;
        double winloseratio = (pelattu > 0) ? (lost <= 0 ? (double) wins : (double) wins / lost) : 0.0;
        String formattedRatio = String.format("%.2f", winloseratio);

        ChatFormatting ratioColor;

        if (winloseratio >= 200) ratioColor = ChatFormatting.DARK_PURPLE;
        else if (winloseratio >= 150) ratioColor = ChatFormatting.LIGHT_PURPLE;
        else if (winloseratio >= 100) ratioColor = ChatFormatting.DARK_BLUE;
        else if (winloseratio >= 50) ratioColor = ChatFormatting.BLUE;
        else if (winloseratio >= 25) ratioColor = ChatFormatting.YELLOW;
        else if (winloseratio >= 10) ratioColor = ChatFormatting.AQUA;
        else if (winloseratio >= 5.0) ratioColor = ChatFormatting.DARK_AQUA;
        else if (winloseratio >= 2.0) ratioColor = ChatFormatting.DARK_GREEN;
        else if (winloseratio >= 1.0) ratioColor = ChatFormatting.GREEN;
        else ratioColor = ChatFormatting.GRAY;

        ChatFormatting winColor;

        if (wins >= 5000) winColor = ChatFormatting.DARK_PURPLE;
        else if (wins >= 3500) winColor = ChatFormatting.LIGHT_PURPLE;
        else if (wins >= 2000) winColor = ChatFormatting.DARK_BLUE;
        else if (wins >= 1000) winColor = ChatFormatting.BLUE;
        else if (wins >= 750)  winColor = ChatFormatting.YELLOW;
        else if (wins >= 500)  winColor = ChatFormatting.AQUA;
        else if (wins >= 300)  winColor = ChatFormatting.DARK_AQUA;
        else if (wins >= 200)  winColor = ChatFormatting.DARK_GREEN;
        else if (wins >= 100)  winColor = ChatFormatting.GREEN;
        else winColor = ChatFormatting.GRAY;

        Component message = Component.empty()
                .append(Component.literal(targetOpponent).setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)))
                .append(Component.literal(" - ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(duelName).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\n"))
                // Wins Section
                .append(Component.literal("🏆 ").setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true)))
                .append(Component.literal(String.valueOf(wins)).setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(winColor)))
                .append(Component.literal(" ☠ ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(lost)).withStyle((ChatFormatting.RED)))
                // Ratio Section
                .append(Component.literal(" W/L: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(formattedRatio).setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(ratioColor)));

        client.player.displayClientMessage(message, false);
    }

    private void resetState() {
        duelName = "";
        targetOpponent = "";
        scanDelayTicks = 0;
    }

    private int parseNumber(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isContainerPopulated(AbstractContainerScreen<?> screen) {
        var handler = screen.getMenu();
        for (Slot slot : handler.slots) {
            if (!(slot.container instanceof net.minecraft.world.entity.player.Inventory)) {
                if (!slot.getItem().isEmpty()) return true;
            }
        }
        return false;
    }
}