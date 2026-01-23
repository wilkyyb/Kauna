package lol.wilkyy.kauna.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KaunaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE =
            new File(MinecraftClient.getInstance().runDirectory, "config/kauna.json");

    public static final Logger LOGGER = LogManager.getLogger("Kauna");

    public boolean debugLogging = false;

    public boolean autoGG = true;              // normal AutoGG toggle
    public int autoGGDelay = 0;                  // delay in seconds before sending AutoGG
    public String AutoGGText = "gg";     // default custom message

    public boolean autoEz = false;
    public boolean autoReadyUp = false;

    public boolean CheckForUpdates = true;


    // Initialize with the default name
    public List<String> friendsList = new ArrayList<>(List.of("hullu"));

    // Singleton instance
    public static KaunaConfig INSTANCE = new KaunaConfig();

    // Load config from disk
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, KaunaConfig.class);

                if (INSTANCE == null) {
                    // Defensive: if Gson returns null
                    LOGGER.warn("Config file empty or invalid, resetting to defaults.");
                    INSTANCE = new KaunaConfig();
                    save();
                }
            } catch (Exception e) {
                // Catch *any* error (syntax, type mismatch, IO)
                LOGGER.error("Failed to load config, resetting to defaults.", e);
                INSTANCE = new KaunaConfig();
                save();
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
