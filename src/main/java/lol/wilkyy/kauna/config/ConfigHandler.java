package lol.wilkyy.kauna.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ConfigHandler {

    public static MutableComponent getPrefix() {
        return Component.literal("[").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))
                .append(Component.literal("K").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFEBA17)).withBold(true)))
                .append(Component.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFEC833)).withBold(true)))
                .append(Component.literal("u").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD54E)).withBold(true)))
                .append(Component.literal("n").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFE36A)).withBold(true)))
                .append(Component.literal("a").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFF085)).withBold(true)))
                .append(Component.literal("] ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
    }
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var kaunaCommand = dispatcher.register(
                literal("kauna")
                .then(literal("config")
                        // AutoGG
                        .then(literal("AutoGG")
                                .executes(ctx -> showStatus(ctx, "AutoGG", KaunaConfig.INSTANCE.autoGG))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "AutoGG", "autoGG"))))

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

                        .then(literal("inRealmiCheck")
                                .executes(ctx -> showStatus(ctx, "Check if server is Realmi", KaunaConfig.INSTANCE.inRealmiCheck))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "inRealmiCheck", "inRealmiCheck"))))


                        .then(literal("CheckForUpdates")
                                .executes(ctx -> showStatus(ctx, "Check if update available", KaunaConfig.INSTANCE.CheckForUpdates))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "CheckForUpdates", "CheckForUpdates"))))


                        .then(literal("DebugLogging")
                                .executes(ctx -> showStatus(ctx, "Enable Debug Logging", KaunaConfig.INSTANCE.debugLogging))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(ctx -> setBool(ctx, "DebugLogging", "debugLogging"))))

                )

                .then(literal("friend")
                        .then(literal("list").executes(ctx -> {
                            ctx.getSource().sendFeedback(getPrefix()
                                    .append(Component.literal("Kaverit:\n §8- §a").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(String.join("\n §8-§a ", KaunaConfig.INSTANCE.friendsList)).withStyle(ChatFormatting.GREEN))));
                            return 1;
                        }))
                        .then(literal("add")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            Minecraft client = Minecraft.getInstance();
                                            if (client.getConnection() != null) {
                                                return SharedSuggestionProvider.suggest(
                                                        client.getConnection().getOnlinePlayers().stream()
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
                                                .append(Component.literal(name).withStyle(ChatFormatting.WHITE))
                                                .append(Component.literal(" lisätty kaveriksi!").withStyle(ChatFormatting.GRAY)));
                                            }
                                            return 1;
                                        })))
                        .then(literal("remove")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(KaunaConfig.INSTANCE.friendsList, builder))
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            if (KaunaConfig.INSTANCE.friendsList.remove(name)) {
                                                KaunaConfig.save();

                                                ctx.getSource().sendFeedback(getPrefix()
                                                .append(Component.literal(name).withStyle(ChatFormatting.WHITE))
                                                .append(Component.literal(" poistettu kavereista.").withStyle(ChatFormatting.GRAY)));
                                            }
                                            return 1;
                                        })))
                )
        );
        dispatcher.register(
                literal("k").redirect(kaunaCommand)
        );
        dispatcher.register(
                literal("kc").redirect(kaunaCommand.getChild("config"))
        );
    }

    // Helper for Boolean Status Display
    private static int showStatus(CommandContext<FabricClientCommandSource> ctx, String name, boolean val) {
        ctx.getSource().sendFeedback(getPrefix().append(Component.literal(name)
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" on tällä hetkellä: ").withStyle(ChatFormatting.GRAY)))
                .append(Component.literal(val ? "PÄÄLLÄ" : "POIS").withStyle(val ? ChatFormatting.GREEN : ChatFormatting.RED)));
        return 1;
    }

    // Helper for String/Object Status Display
    private static int showStatus(CommandContext<FabricClientCommandSource> ctx, String name, Object val) {
        ctx.getSource().sendFeedback(Component.literal(name + " on tällä hetkellä: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(val)).withStyle(ChatFormatting.YELLOW)));
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
            if (fieldName.equals("autoReadyUp")) KaunaConfig.INSTANCE.autoReadyUp = val;
            if (fieldName.equals("autoStatsLookup")) KaunaConfig.INSTANCE.autoStatsLookup = val;
            if (fieldName.equals("stickySkipNotification")) KaunaConfig.INSTANCE.stickySkipNotification = val;
        }
        return notify(ctx, displayName, val ? "PÄÄLLÄ" : "POIS");
    }

    private static int notify(CommandContext<FabricClientCommandSource> ctx, String name, String status) {
        KaunaConfig.save();

        ChatFormatting statusColor = ChatFormatting.YELLOW; // Default for non-boolean (like text or delay)
        if (status.equalsIgnoreCase("PÄÄLLÄ")) statusColor = ChatFormatting.GREEN;
        if (status.equalsIgnoreCase("POIS")) statusColor = ChatFormatting.RED;

        ctx.getSource().sendFeedback(getPrefix()
                .append(Component.literal(name).withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(" asetettiin: ").withStyle(ChatFormatting.GRAY)))
                .append(Component.literal(status).withStyle(statusColor)));
        return 1;
    }
}