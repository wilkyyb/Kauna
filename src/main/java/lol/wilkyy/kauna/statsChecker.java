package lol.wilkyy.kauna;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class statsChecker implements ClientModInitializer {

    private static final String TARGET_TITLE = "kaksintaistotilastot";
    private boolean hasScraped = false;
    private int scanDelayTicks = 0; // The timer for page switching

    public static String duelName = "";
    public static String targetOpponent = "";

    @Override
    public void onInitializeClient() {
        // 1. Chat Listener
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
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

        // 2. GUI Logic
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            hasScraped = false;
            scanDelayTicks = 0;

            ScreenEvents.beforeRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
                if (screen1 instanceof HandledScreen<?> handledScreen) {
                    // Only scan if we have a name to look for and haven't finished
                    if (screen1.getTitle().getString().contains(TARGET_TITLE) && !hasScraped && !duelName.isEmpty()) {

                        // Wait for the timer to reach 0 before scanning (important for page turns)
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand("stats " + pName);
        }
    }

    private void processStats(HandledScreen<?> screen) {
        var handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        Slot nextPageSlot = null;
        boolean found = false;

        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof net.minecraft.entity.player.PlayerInventory) continue;

            ItemStack stack = slot.getStack();
            String name = stack.getName().getString();

            // Check if matches the kit we are looking for
            if (name.equalsIgnoreCase(duelName)) {
                displayStatsLocally(stack);
                hasScraped = true;
                client.execute(() -> client.player.closeHandledScreen());
                resetState();
                found = true;
                break;
            }

            // Look for the next page button
            if (name.contains("Seuraava sivu")) {
                nextPageSlot = slot;
            }
        }

        // If not found on the current page
        if (!found && !hasScraped) {
            if (nextPageSlot != null) {
                // Click Next Page and start a 5-tick delay
                client.interactionManager.clickSlot(handler.syncId, nextPageSlot.id, 0, SlotActionType.PICKUP, client.player);
                scanDelayTicks = 5;
                System.out.println("Switching to next page...");
            } else {
                // No more pages, give up
                System.out.println("Could not find " + duelName + " in stats.");
                hasScraped = true;
                resetState();
            }
        }
    }

    private void displayStatsLocally(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Use Item.TooltipContext.DEFAULT for 1.21.1
        List<Text> lines = stack.getTooltip(
                Item.TooltipContext.DEFAULT,
                client.player,
                TooltipType.BASIC
        );

        int v = 0, p = 0;
        for (Text line : lines) {
            String text = line.getString().toLowerCase();
            // Check for the Finnish keywords in the lore
            if (text.contains("voittoja:")) v = parseNumber(text);
            if (text.contains("pelattu:")) p = parseNumber(text);
        }

        // Local chat feedback
        client.player.sendMessage(Text.literal("--- " + targetOpponent + " [" + duelName + "] ---").formatted(Formatting.GOLD), false);
        client.player.sendMessage(Text.literal("Voittoja: ").formatted(Formatting.GRAY).append(Text.literal(String.valueOf(v)).formatted(Formatting.WHITE)), false);
        client.player.sendMessage(Text.literal("Pelattu: ").formatted(Formatting.GRAY).append(Text.literal(String.valueOf(p)).formatted(Formatting.WHITE)), false);
    }

    private void resetState() {
        duelName = "";
        targetOpponent = "";
        scanDelayTicks = 0;
    }

    private int parseNumber(String text) {
        try { return Integer.parseInt(text.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0; }
    }

    private boolean isContainerPopulated(HandledScreen<?> screen) {
        var handler = screen.getScreenHandler();
        for (Slot slot : handler.slots) {
            if (!(slot.inventory instanceof net.minecraft.entity.player.PlayerInventory)) {
                if (!slot.getStack().isEmpty()) return true;
            }
        }
        return false;
    }
}