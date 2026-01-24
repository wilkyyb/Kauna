package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
    private int scanDelayTicks = 0;
    private int crouchTicks = 0;

    // Timing logic
    private static int statsDelayTicks = -1;
    public static String duelName = "";
    public static String targetOpponent = "";

    @Override
    public void onInitializeClient() {
        // 1. Tick Listener for Delayed Command
        // This prevents the stats menu from opening instantly and blocking the first-round crouch.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (statsDelayTicks > 0) {
                statsDelayTicks--;
                if (statsDelayTicks == 0) {
                    if (client.player != null && !targetOpponent.isEmpty()) {
                        client.player.networkHandler.sendChatCommand("stats " + targetOpponent);
                    }
                    statsDelayTicks = -1;
                }
            }

            if (crouchTicks > 0) {
                if (client.options != null) {
                    // Force the key to be "down" every tick the counter is active
                    client.options.sneakKey.setPressed(true);
                }
                crouchTicks--;

                // Once the timer expires, release the key
                if (crouchTicks == 0) {
                    if (client.options != null) {
                        client.options.sneakKey.setPressed(false);
                    }
                }
            }
        });

        // 2. Chat Listener
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

        // 3. GUI Scanning Logic
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            hasScraped = false;
            scanDelayTicks = 0;

            ScreenEvents.beforeRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
                if (screen1 instanceof HandledScreen<?> handledScreen) {
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
        // Wait 5 ticks (250ms) to ensure autoReadyUp finishes its crouch packet first.
        statsDelayTicks = 10;
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

            if (name.equalsIgnoreCase(duelName)) {
                displayStatsLocally(stack);
                hasScraped = true;

                client.execute(() -> {
                    client.player.closeHandledScreen();
                    // 5 ticks (approx 250ms) is the reliable standard for macro-crouching
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
                client.interactionManager.clickSlot(handler.syncId, nextPageSlot.id, 0, SlotActionType.PICKUP, client.player);
                scanDelayTicks = 5;
            } else {
                hasScraped = true;
                client.execute(() -> {
                    client.player.closeHandledScreen();
                    if (!statsChecker.duelName.contains("Parkour")) {
                        crouchTicks = 2;
                    }
                });
                resetState();
            }
        }
    }

    private void displayStatsLocally(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        List<Text> lines = stack.getTooltip(
                Item.TooltipContext.DEFAULT,
                client.player,
                TooltipType.BASIC
        );

        int voittoja = 0, pelattu = 0;
        for (Text line : lines) {
            String text = line.getString().toLowerCase();
            if (text.contains("voittoja:")) voittoja = parseNumber(text);
            if (text.contains("pelattu:")) pelattu = parseNumber(text);
        }

        // 1. Calculate Ratio
        double lost = pelattu - voittoja;
        double winloseratio = (pelattu > 0) ? (lost <= 0 ? (double) voittoja : (double) voittoja / lost) : 0.0;
        String formattedRatio = String.format("%.2f", winloseratio);

        // 2. Win/Loss Ratio Colors
        Formatting ratioColor;
        boolean boldRatio = false;

        if (winloseratio >= 200) { ratioColor = Formatting.DARK_PURPLE; boldRatio = true; }
        else if (winloseratio >= 150) ratioColor = Formatting.LIGHT_PURPLE;
        else if (winloseratio >= 100) ratioColor = Formatting.DARK_BLUE;
        else if (winloseratio >= 50) ratioColor = Formatting.BLUE;
        else if (winloseratio >= 25) ratioColor = Formatting.YELLOW;
        else if (winloseratio >= 10) ratioColor = Formatting.AQUA;
        else if (winloseratio >= 5.0) ratioColor = Formatting.DARK_AQUA;
        else if (winloseratio >= 2.0) ratioColor = Formatting.DARK_GREEN;
        else if (winloseratio >= 1.0) ratioColor = Formatting.GREEN;
        else ratioColor = Formatting.GRAY;

        // 3. Wins Color Logic (Mapped to same colors as Ratio)
        Formatting winColor;
        boolean boldWins = false;

        if (voittoja >= 5000) { winColor = Formatting.DARK_PURPLE; boldWins = true; }
        else if (voittoja >= 3500) winColor = Formatting.LIGHT_PURPLE;
        else if (voittoja >= 2000) winColor = Formatting.DARK_BLUE;
        else if (voittoja >= 1000) winColor = Formatting.BLUE;
        else if (voittoja >= 750)  winColor = Formatting.YELLOW;
        else if (voittoja >= 500)  winColor = Formatting.AQUA;
        else if (voittoja >= 300)  winColor = Formatting.DARK_AQUA;
        else if (voittoja >= 200)  winColor = Formatting.DARK_GREEN;
        else if (voittoja >= 100)  winColor = Formatting.GREEN;
        else winColor = Formatting.GRAY;

        // 4. Build the Message
        Text message = Text.empty()
                .append(Text.literal("|").formatted(Formatting.BOLD))
                .append(Text.literal(" " + targetOpponent + " ").setStyle(net.minecraft.text.Style.EMPTY.withBold(true).withColor(Formatting.GOLD)))
                .append(Text.literal("- ").formatted(Formatting.GRAY))
                .append(Text.literal(duelName + " stats").formatted(Formatting.AQUA))
                .append(Text.literal("\n"))
                .append(Text.literal("| ").setStyle(net.minecraft.text.Style.EMPTY.withBold(true).withColor(Formatting.WHITE)))
                // Wins Section
                .append(Text.literal("Voitot: ").formatted(Formatting.WHITE))
                .append(Text.literal(String.valueOf(voittoja)).setStyle(net.minecraft.text.Style.EMPTY.withColor(winColor).withBold(boldWins)))
                .append(Text.literal("  - ").formatted(Formatting.GRAY))
                // Ratio Section
                .append(Text.literal("W/L: ").formatted(Formatting.WHITE))
                .append(Text.literal(formattedRatio).setStyle(net.minecraft.text.Style.EMPTY.withColor(ratioColor).withBold(boldRatio)));

        client.player.sendMessage(message, false);
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