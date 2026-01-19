package lol.wilkyy.kauna.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KaunaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE =
            new File(MinecraftClient.getInstance().runDirectory, "config/kauna.json");

    public static final Logger LOGGER = LogManager.getLogger("Kauna");

    // ✅ Your config options
    public boolean test1 = true;
    public float test2 = 1.0F;
    public boolean debugLogging = false;

    // Singleton instance
    public static KaunaConfig INSTANCE = new KaunaConfig();

    // Load config from disk
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, KaunaConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save(); // create default file
        }
    }

    // Save config to disk
    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void debugLog(String format, Object... args) {
        if (KaunaConfig.INSTANCE.debugLogging) {
            LOGGER.info(format, args); // Log4j supports {} placeholders
        }
    }

}
