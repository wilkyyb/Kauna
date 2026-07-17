package lol.wilkyy.kauna.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
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
                .category(buildOtherCategory())
                .category(buildFriendsCategory())
                .category(buildSystemCategory())
                .save(KaunaConfig::save)
                .build()
                .generateScreen(parentScreen);
    }

    private static Option<Integer> buildIntOption(String name, String desc, int min, int max,
                                                  java.util.function.Supplier<Integer> getter, java.util.function.Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.createBuilder().text(Component.literal(desc)).build())
                .binding(getter.get(), getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1))
                .listener((opt, val) -> setter.accept(val))  // <-- fires on every slider change
                .build();
    }

    private static ConfigCategory buildKahakkaCategory() {
        Minecraft mc = Minecraft.getInstance();
        int maxX = mc.getWindow().getGuiScaledWidth();
        int maxY = mc.getWindow().getGuiScaledHeight();
        List<String> themeOptions = List.of("Rainbow", "Gay", "Lesbian", "Trans");

        return ConfigCategory.createBuilder()
                .name(Component.literal("Kahakka"))
                .group(dev.isxander.yacl3.api.OptionGroup.createBuilder()
                        .name(Component.literal("Kahakka").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Kahakka Asetukset")))

                        .option(buildBooleanOption("AutoGG", "Lähetä 'gg' pelin loppuessa",
                                () -> KaunaConfig.INSTANCE.autoGG, val -> KaunaConfig.INSTANCE.autoGG = val, null))
                        .option(buildBooleanOption("AutoReady", "Kyykkää automaattisesti erän alkaessa",
                                () -> KaunaConfig.INSTANCE.autoReady, val -> KaunaConfig.INSTANCE.autoReady = val, null))
                        .option(buildBooleanOption("Auto Requeue", "Liity automaattisesti jonoon siihen kittiin jota viimeksi pelasit",
                                () -> KaunaConfig.INSTANCE.autoRequeue, val -> KaunaConfig.INSTANCE.autoRequeue = val, null))
                        .build())
                .group(dev.isxander.yacl3.api.OptionGroup.createBuilder()
                        .name(Component.literal("Stats Display").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                        .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Näe pelisession statistiikat ruudullasi!")))

                        .option(buildBooleanOption("Stats Display", "Näytä kit-kohtaiset tai globaalit stats ruudulla",
                                () -> KaunaConfig.INSTANCE.statsHud, val -> KaunaConfig.INSTANCE.statsHud = val, null))
                        .option(buildBooleanOption("Global/Kit Stats", "Näytä kit-kohtaiset stats globaalien sijaan",
                                () -> KaunaConfig.INSTANCE.showKitStats, val -> KaunaConfig.INSTANCE.showKitStats = val, null))
                        .option(Option.<Float>createBuilder()
                                .name(Component.literal("Stats HUD Background Opacity"))
                                .description(OptionDescription.createBuilder().text(Component.literal("Taustan läpinäkyvyys")).build())
                                .binding(0.5f, () -> KaunaConfig.INSTANCE.statsHudBackgroundOpacity, val -> KaunaConfig.INSTANCE.statsHudBackgroundOpacity = val)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.05f)
                                        .valueFormatter(val -> Component.literal(String.format("%.0f%%", val * 100))))
                                .listener((opt, val) -> KaunaConfig.INSTANCE.statsHudBackgroundOpacity = val)
                                .build())
                        .option(buildIntOption("Stats HUD X", "Vaakasuuntainen sijainti",
                                0, maxX, () -> KaunaConfig.INSTANCE.statsHudX, val -> KaunaConfig.INSTANCE.statsHudX = val))
                        .option(buildIntOption("Stats HUD Y", "Pystysuuntainen sijainti",
                                0, maxY, () -> KaunaConfig.INSTANCE.statsHudY, val -> KaunaConfig.INSTANCE.statsHudY = val))
                        .build())

                .group(dev.isxander.yacl3.api.OptionGroup.createBuilder()
                        .name(Component.literal("Parkour Asetukset").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Parkour-moduulin lisäasetukset")))

                        .option(buildBooleanOption("Pysyvä Skip Indikaattori", "Näyttää koko skipattavan ajan indikaattorin siitä, että voi skipata",
                                () -> KaunaConfig.INSTANCE.stickySkipNotification, val -> KaunaConfig.INSTANCE.stickySkipNotification = val, null))

                        .option(buildBooleanOption("Maailman Ennätys Ajastin", "Näytä crosshairin alla maailman ennätyksen aika maailmanennätykseen",
                                () -> KaunaConfig.INSTANCE.worldRecordTimer, val -> KaunaConfig.INSTANCE.worldRecordTimer = val, null))

                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Maailman Ennätys Väriteema"))
                                .binding("Rainbow", () -> KaunaConfig.INSTANCE.wrColorTheme, val -> KaunaConfig.INSTANCE.wrColorTheme = val)
                                .controller(opt -> dev.isxander.yacl3.api.controller.CyclingListControllerBuilder.create(opt)
                                        .values(themeOptions)
                                        .valueFormatter(Colors::getFormattedThemeName))
                                .build())
                        .build())
                .build();
    }

    private static ConfigCategory buildOtherCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("Muuta"))
                .group(dev.isxander.yacl3.api.OptionGroup.createBuilder()
                        .name(Component.literal("Muuta").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD))
                        .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("Creative Asetukset")))

                        .option(buildBooleanOption("Creative Plot Title", "Vaihda plot title subtitlestä action baariin (ei niin tiellä)",
                                () -> KaunaConfig.INSTANCE.creativePlotTitle, val -> KaunaConfig.INSTANCE.creativePlotTitle = val, null))
                        .build())
                .build();
    }

    private static ConfigCategory buildFriendsCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("Kaverit"))
                .option(ListOption.<String>createBuilder()
                        .name(Component.literal("Kaverilista").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                        .description(OptionDescription.of(Component.literal("Hallitse tallennettuja ystäviäsi.")))
                        .binding(new ArrayList<>(), () -> KaunaConfig.INSTANCE.friendsList, val -> KaunaConfig.INSTANCE.friendsList = val)
                        .controller(StringControllerBuilder::create)
                        .initial("")
                        .build())
                .build();
    }

    private static ConfigCategory buildSystemCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("Järjestelmä"))
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
            Minecraft.getInstance().gui.setScreen(createConfigScreen(Minecraft.getInstance().gui.screen()));
        });
        return 1;
    }
}