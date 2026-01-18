package lol.wilkyy.kauna;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Kauna Config"));

            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            general.addEntry(builder.entryBuilder()
                    .startBooleanToggle(Text.literal("Enable Hype Song"), KaunaConfig.enableHypeSong)
                    .setSaveConsumer(newValue -> KaunaConfig.enableHypeSong = newValue)
                    .build());

            general.addEntry(builder.entryBuilder()
                    .startFloatField(Text.literal("Hype Song Volume"), KaunaConfig.hypeSongVolume)
                    .setSaveConsumer(newValue -> KaunaConfig.hypeSongVolume = newValue)
                    .setMin(0.0F).setMax(1.0F)
                    .build());

            return builder.build();
        };
    }
    public class KaunaConfig {
        public static boolean enableHypeSong = true;
        public static float hypeSongVolume = 1.0F;

    }
}
