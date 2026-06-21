package lol.wilkyy.kauna.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ConfigHandler {

    public static Screen createConfigScreen(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Kauna Config").withStyle(ChatFormatting.BOLD))
                .category(buildKahakkaCategory())
                .category(buildYstavatCategory())
                .category(buildSystemCategory())
                .save(KaunaConfig::save)
                .build()
                .generateScreen(parentScreen);
    }

    private static ConfigCategory buildKahakkaCategory() {
        List<String> themeOptions = List.of("Rainbow", "Gay", "Lesbian", "Trans");

        return ConfigCategory.createBuilder()
                .name(Component.literal("Kahakka"))

                // First Section (Example)
                .option(buildBooleanOption("AutoGG", "Lähetä 'gg' pelin loppuessa",
                        () -> KaunaConfig.INSTANCE.autoGG, val -> KaunaConfig.INSTANCE.autoGG = val, null))
                .option(buildBooleanOption("AutoReady", "Kyykkää automaattisesti erän alkaessa",
                        () -> KaunaConfig.INSTANCE.autoReady, val -> KaunaConfig.INSTANCE.autoReady = val, null))

                // --- THIS ACTS AS YOUR DIVIDER TITLE ---
                .group(dev.isxander.yacl3.api.OptionGroup.createBuilder()
                        .name(Component.literal("Parkour Asetukset").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Parkour-moduulin lisäasetukset")))

                        // Options placed inside this group will appear under the divider title
                        .option(buildBooleanOption("Pysyvä Skip Indikaattori", "Näyttää koko skipattavan ajan indikaattorin siitä, että voi skipata",
                                () -> KaunaConfig.INSTANCE.stickySkipNotification, val -> KaunaConfig.INSTANCE.stickySkipNotification = val, null))

                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Maailman Ennätys Väriteema"))
                                .binding("Rainbow", () -> KaunaConfig.INSTANCE.wrColorTheme, val -> KaunaConfig.INSTANCE.wrColorTheme = val)
                                .controller(opt -> dev.isxander.yacl3.api.controller.CyclingListControllerBuilder.create(opt)
                                        .values(themeOptions)
                                        .valueFormatter(Colors::getFormattedThemeName))
                                .build())
                        .build()) // Ends the group section
                .build();
    }

    private static ConfigCategory buildYstavatCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("Ystävät"))
                .option(ListOption.<String>createBuilder()
                        .name(Component.literal("Kaverilista"))
                        .description(OptionDescription.of(Component.literal("Hallitse tallennettuja ystäviäsi.")))
                        .binding(new ArrayList<>(), () -> KaunaConfig.INSTANCE.friendsList, val -> KaunaConfig.INSTANCE.friendsList = val)
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildSystemCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("System"))
                .option(buildBooleanOption("Varmista Realmi.fi Palvelin", "Varmista, että pelaaja on Realmi.fi palvelimella tehdessään mitään. !! Modi saattaa rikkoutua, jos laitat tämän pois.",
                        () -> KaunaConfig.INSTANCE.inRealmiCheck, val -> KaunaConfig.INSTANCE.inRealmiCheck = val, null))
                .option(buildBooleanOption("Tarkista Päivitykset", "Tarkista onko päivityksiä saatavilla",
                        () -> KaunaConfig.INSTANCE.CheckForUpdates, val -> KaunaConfig.INSTANCE.CheckForUpdates = val, null))
                .option(buildBooleanOption("Debug", "Kirjaa loki-tiedostoihin debuggaamiseen liittyviä asioita.",
                        () -> KaunaConfig.INSTANCE.debugLogging, val -> KaunaConfig.INSTANCE.debugLogging = val, null))
                .build();
    }

    private static Option<Boolean> buildBooleanOption(String name, String desc, java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter, String imageFileName) {
        OptionDescription.Builder descBuilder = OptionDescription.createBuilder().text(Component.literal(desc));

        return Option.<Boolean>createBuilder()
                .name(Component.literal(name))
                .description(descBuilder.build())
                .binding(true, getter, setter)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .valueFormatter(val -> val ? Component.literal("Päällä") : Component.literal("Pois Päältä"))
                        .coloured(true))
                .build();
    }

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
                        .executes(ctx -> openGuiScreen())
                        .then(literal("config")
                                .executes(ctx -> openGuiScreen())
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
        dispatcher.register(literal("k").redirect(kaunaCommand));
        dispatcher.register(literal("kc").executes(ctx -> openGuiScreen()));
    }

    private static int openGuiScreen() {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreen(createConfigScreen(Minecraft.getInstance().screen));
        });
        return 1;
    }
}