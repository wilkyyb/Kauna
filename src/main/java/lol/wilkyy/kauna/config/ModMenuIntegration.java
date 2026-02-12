package lol.wilkyy.kauna.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Kauna Configuration"))
                    .setSavingRunnable(KaunaConfig::save);

            ConfigEntryBuilder eb = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            // --- SECTION: AUTO GG ---
            general.addEntry(eb.startTextDescription(Text.literal("AutoGG Asetukset")
                            .setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN)))
                    .build());

            general.addEntry(eb.startBooleanToggle(Text.literal("AutoGG"), KaunaConfig.INSTANCE.autoGG)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.autoGG = val)
                    .setDefaultValue(true)
                    .build());

            general.addEntry(eb.startStrField(Text.literal("AutoGG Viesti"), KaunaConfig.INSTANCE.AutoGGText)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.AutoGGText = val)
                    .setDefaultValue("gg")
                    .build());

            general.addEntry(eb.startIntSlider(Text.literal("AutoGG Viive (ms)"), KaunaConfig.INSTANCE.autoGGDelay, 0, 1000)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.autoGGDelay = val)
                    .setDefaultValue(250)
                    .build());

            // --- SECTION: AUTOMATION ---
            general.addEntry(eb.startTextDescription(Text.literal("Muut Automaattiset")
                            .setStyle(Style.EMPTY.withBold(true).withColor(Formatting.AQUA)))
                    .build());

            general.addEntry(eb.startBooleanToggle(Text.literal("AutoEz (3s viive)"), KaunaConfig.INSTANCE.autoEz)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.autoEz = val)
                    .build());

            general.addEntry(eb.startBooleanToggle(Text.literal("Auto ReadyUp"), KaunaConfig.INSTANCE.autoReadyUp)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.autoReadyUp = val)
                    .setDefaultValue(false)
                    .build());

            general.addEntry(eb.startBooleanToggle(Text.literal("Stats Lookup"), KaunaConfig.INSTANCE.autoStatsLookup)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.autoStatsLookup = val)
                    .setDefaultValue(true)
                    .build());

            // --- SECTION: PARKOUR ---
            general.addEntry(eb.startTextDescription(Text.literal("Parkour Duel")
                            .setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN)))
                    .build());
            general.addEntry(eb.startBooleanToggle(Text.literal("Pysyvä ohitus-ilmoitus"), KaunaConfig.INSTANCE.stickySkipNotification)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.stickySkipNotification = val)
                    .setDefaultValue(true)
                    .build());

            // --- SECTION: FRIENDS  ---
            general.addEntry(eb.startTextDescription(Text.literal("Kaverit")
                            .setStyle(Style.EMPTY.withBold(true).withColor(Formatting.DARK_AQUA)))
                    .build());

            general.addEntry(eb.startStrList(Text.literal("Kaverilista"), KaunaConfig.INSTANCE.friendsList)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.friendsList = val)
                    .setDefaultValue(new java.util.ArrayList<>(java.util.List.of("hullu")))
                    .build());

            // --- SECTION: SYSTEM ---
            general.addEntry(eb.startTextDescription(Text.literal("Dev Asetukset")
                            .setStyle(Style.EMPTY.withBold(true).withColor(Formatting.YELLOW)))
                    .build());

            general.addEntry(eb.startBooleanToggle(Text.literal("Debug Logging"), KaunaConfig.INSTANCE.debugLogging)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.debugLogging = val)
                    .setDefaultValue(false)
                    .build());

            general.addEntry(eb.startBooleanToggle(Text.literal("Tarkista Päivitykset"), KaunaConfig.INSTANCE.CheckForUpdates)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.CheckForUpdates = val)
                    .setDefaultValue(true)
                    .build());

            // Add this in the "General" category or a new "Server" category
            general.addEntry(eb.startBooleanToggle(Text.literal("Vain Realmi.fi"), KaunaConfig.INSTANCE.inRealmiCheck)
                    .setSaveConsumer(val -> KaunaConfig.INSTANCE.inRealmiCheck = val)
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Modin toiminnot ovat päällä vain Realmi.fi -palvelimella"))
                    .build());

            return builder.build();
        };
    }
}