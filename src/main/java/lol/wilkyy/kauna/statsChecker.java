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
                    if (KaunaConfig.INSTANCE.autoReadyUp) {
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
                    crouchTicks = 2;
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

        // 1. Calculate Ratio as a double to avoid the "0" integer issue
        // We check if pelattu > 0 to avoid division by zero crashes
        double lost = pelattu - voittoja;
        double ratio = (pelattu > 0) ? (double) voittoja / lost : 0.0;

        // 2. Format the ratio to 2 decimal places (e.g., 0.50)
        String formattedRatio = String.format("%.2f", ratio);

        // 3. Determine color: Green if ratio >= 1.0 (positive performance), Red if below
        // (Or stick to your preference: Green if > 0)
        Formatting ratioColor = (ratio >= 1.0) ? Formatting.GREEN : Formatting.RED;

        // Build the formatted message
        Text message = Text.empty()
                .append(Text.literal("|").formatted(Formatting.BOLD))
                .append(Text.literal(" " + targetOpponent + " ").setStyle(net.minecraft.text.Style.EMPTY.withBold(true).withColor(Formatting.GOLD)))
                .append(Text.literal("- ").formatted(Formatting.GRAY))
                .append(Text.literal(duelName + " statistiikat").formatted(Formatting.AQUA))
                .append(Text.literal("\n"))
                .append(Text.literal("| ").setStyle(net.minecraft.text.Style.EMPTY.withBold(true).withColor(Formatting.WHITE)))
                .append(Text.literal("Voitot: ").formatted(Formatting.WHITE))
                .append(Text.literal(voittoja + " ").formatted(Formatting.GREEN))
                .append(Text.literal("Pelattu: ").formatted(Formatting.WHITE))
                .append(Text.literal(pelattu + " ").formatted(Formatting.DARK_AQUA))
                .append(Text.literal("- ").formatted(Formatting.GRAY))
                .append(Text.literal("W/L: ").formatted(Formatting.WHITE))
                .append(Text.literal(formattedRatio).formatted(ratioColor)); // Use formatted string here

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