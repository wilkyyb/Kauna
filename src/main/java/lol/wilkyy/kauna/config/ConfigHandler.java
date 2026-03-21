package lol.wilkyy.kauna.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ConfigHandler {

    public static MutableText getPrefix() {
        return Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
                .append(Text.literal("K").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFEBA17)).withBold(true)))
                .append(Text.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFEC833)).withBold(true)))
                .append(Text.literal("u").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD54E)).withBold(true)))
                .append(Text.literal("n").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFE36A)).withBold(true)))
                .append(Text.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFF085)).withBold(true)))
                .append(Text.literal("] ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("kauna")
                .then(literal("config")
                        // AutoGG
                        .then(literal("AutoGG")
                                .executes(ctx -> showStatus(ctx, "AutoGG", KaunaConfig.INSTANCE.autoGG))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "AutoGG", "autoGG"))))

                        // AutoGG Text
                        .then(literal("AutoGGText")
                                .executes(ctx -> showStatus(ctx, "AutoGG Viesti", KaunaConfig.INSTANCE.AutoGGText))
                                .then(argument("msg", StringArgumentType.greedyString()).executes(ctx -> {
                                    KaunaConfig.INSTANCE.AutoGGText = StringArgumentType.getString(ctx, "msg");
                                    return notify(ctx, "AutoGG Viesti", KaunaConfig.INSTANCE.AutoGGText);
                                })))

                        // AutoGG Delay
                        .then(literal("AutoGGDelay")
                                .executes(ctx -> showStatus(ctx, "AutoGG Viive", KaunaConfig.INSTANCE.autoGGDelay + "ms"))
                                .then(argument("ms", IntegerArgumentType.integer(0, 1000)).executes(ctx -> {
                                    KaunaConfig.INSTANCE.autoGGDelay = IntegerArgumentType.getInteger(ctx, "ms");
                                    return notify(ctx, "AutoGG Viive", KaunaConfig.INSTANCE.autoGGDelay + "ms");
                                })))

                        // AutoEz
                        .then(literal("AutoEz")
                                .executes(ctx -> showStatus(ctx, "AutoEz", KaunaConfig.INSTANCE.autoEz))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "AutoEz", "autoEz"))))

                        // AutoReady
                        .then(literal("AutoReadyUp")
                                .executes(ctx -> showStatus(ctx, "Auto ReadyUp", KaunaConfig.INSTANCE.autoReadyUp))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "Auto ReadyUp", "autoReadyUp"))))

                        // AutoStats
                        .then(literal("AutoLookupDuelStats")
                                .executes(ctx -> showStatus(ctx, "Stats Lookup", KaunaConfig.INSTANCE.autoStatsLookup))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "Stats Lookup", "autoStatsLookup"))))

                        // StickySkip
                        .then(literal("StickySkip")
                                .executes(ctx -> showStatus(ctx, "Pysyvä Skip-ilmoitus", KaunaConfig.INSTANCE.stickySkipNotification))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "Sticky Skip", "stickySkipNotification"))))
                )
                // Friend management remains the same...
                .then(literal("friend")
                        .then(literal("list").executes(ctx -> {
                            ctx.getSource().sendFeedback(getPrefix()
                                    .append(Text.literal("Kaverit:\n §8- §a").formatted(Formatting.GRAY)
                                    .append(Text.literal(String.join("\n §8-§a ", KaunaConfig.INSTANCE.friendsList)).formatted(Formatting.GREEN))));
                            return 1;
                        }))
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            MinecraftClient client = MinecraftClient.getInstance();
                                            if (client.getNetworkHandler() != null) {
                                                return CommandSource.suggestMatching(
                                                        client.getNetworkHandler().getPlayerList().stream()
                                                                .map(entry -> entry.getProfile().name()),
                                                        builder
                                                );
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            if (!KaunaConfig.INSTANCE.friendsList.contains(name)) {
                                                KaunaConfig.INSTANCE.friendsList.add(name);
                                                KaunaConfig.save();
                                                ctx.getSource().sendFeedback(getPrefix()
                                                .append(Text.literal(name).formatted(Formatting.WHITE))
                                                .append(Text.literal(" lisätty kaveriksi!").formatted(Formatting.GRAY)));
                                            }
                                            return 1;
                                        })))
                        .then(literal("remove")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(KaunaConfig.INSTANCE.friendsList, builder))
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            if (KaunaConfig.INSTANCE.friendsList.remove(name)) {
                                                KaunaConfig.save();

                                                ctx.getSource().sendFeedback(getPrefix()
                                                .append(Text.literal(name).formatted(Formatting.WHITE))
                                                .append(Text.literal(" poistettu kavereista.").formatted(Formatting.GRAY)));
                                            }
                                            return 1;
                                        })))
                )
        );
    }

    // Helper for Boolean Status Display
    private static int showStatus(CommandContext<FabricClientCommandSource> ctx, String name, boolean val) {
        ctx.getSource().sendFeedback(getPrefix().append(Text.literal(name)
                .formatted(Formatting.WHITE)
                .append(Text.literal(" on tällä hetkellä: ").formatted(Formatting.GRAY)))
                .append(Text.literal(val ? "PÄÄLLÄ" : "POIS").formatted(val ? Formatting.GREEN : Formatting.RED)));
        return 1;
    }

    // Helper for String/Object Status Display
    private static int showStatus(CommandContext<FabricClientCommandSource> ctx, String name, Object val) {
        ctx.getSource().sendFeedback(Text.literal(name + " on tällä hetkellä: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(val)).formatted(Formatting.YELLOW)));
        return 1;
    }

    private static int setBool(CommandContext<FabricClientCommandSource> ctx, String displayName, String fieldName) {
        boolean val = BoolArgumentType.getBool(ctx, "enabled");
        try {
            KaunaConfig.class.getField(fieldName).set(KaunaConfig.INSTANCE, val);
        } catch (Exception ignored) {
            // Manual fallbacks if reflection fails or field names differ
            if (fieldName.equals("inRealmiCheck")) KaunaConfig.INSTANCE.inRealmiCheck = val;
            if (fieldName.equals("autoGG")) KaunaConfig.INSTANCE.autoGG = val;
            if (fieldName.equals("autoEz")) KaunaConfig.INSTANCE.autoEz = val;
            if (fieldName.equals("autoReadyUp")) KaunaConfig.INSTANCE.autoReadyUp = val;
            if (fieldName.equals("autoStatsLookup")) KaunaConfig.INSTANCE.autoStatsLookup = val;
            if (fieldName.equals("stickySkipNotification")) KaunaConfig.INSTANCE.stickySkipNotification = val;
        }
        return notify(ctx, displayName, val ? "PÄÄLLÄ" : "POIS");
    }

    private static int notify(CommandContext<FabricClientCommandSource> ctx, String name, String status) {
        KaunaConfig.save();

        // Determine color based on the status string
        Formatting statusColor = Formatting.YELLOW; // Default for non-boolean (like text or delay)
        if (status.equalsIgnoreCase("PÄÄLLÄ")) statusColor = Formatting.GREEN;
        if (status.equalsIgnoreCase("POIS")) statusColor = Formatting.RED;

        ctx.getSource().sendFeedback(getPrefix()
                .append(Text.literal(name).formatted(Formatting.WHITE)
                        .append(Text.literal(" asetettiin: ").formatted(Formatting.GRAY)))
                .append(Text.literal(status).formatted(statusColor)));
        return 1;
    }
}