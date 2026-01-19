package lol.wilkyy.kauna.config;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.text.Text;

import static lol.wilkyy.kauna.config.KaunaConfig.debugLog;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Kauna Config"));

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            // ✅ Boolean toggle
            general.addEntry(builder.entryBuilder()
                    .startBooleanToggle(Text.literal("Testi 1"), KaunaConfig.INSTANCE.test1)
                    .setSaveConsumer(newValue -> {
                        KaunaConfig.INSTANCE.test1 = newValue;
                        KaunaConfig.save(); // persist immediately
                        debugLog("New value set for Testi 1: {}", newValue);
                    })
                    .setTooltip(Text.literal("Tää ei tee yhtään mitään, testausmielessä vaan."))
                    .build());

            // ✅ Float slider
            general.addEntry(builder.entryBuilder()
                    .startFloatField(Text.literal("Testi 2"), KaunaConfig.INSTANCE.test2)
                    .setSaveConsumer(newValue -> {
                        KaunaConfig.INSTANCE.test2 = newValue;
                        KaunaConfig.save(); // persist immediately
                        debugLog("New value set for Testi 2: {}", newValue);
                    })
                    .setMin(0.0F).setMax(1.0F)
                    .setTooltip(Text.literal("Tää ei tee yhtään mitään, testausmielessä vaan."))
                    .build());

            general.addEntry(builder.entryBuilder()
                    .startBooleanToggle(Text.literal("Debug Logging"), KaunaConfig.INSTANCE.debugLogging)
                    .setSaveConsumer(newValue -> {
                        KaunaConfig.INSTANCE.debugLogging = newValue;
                        KaunaConfig.save();
                    })
                    .setTooltip(Text.literal("Loggaa modin toiminnan tietoja konsoliin."))
                    .build());


            return builder.build();
        };
    }
}
