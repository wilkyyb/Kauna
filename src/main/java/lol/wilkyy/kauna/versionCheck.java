package lol.wilkyy.kauna;

import lol.wilkyy.kauna.config.KaunaConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class versionCheck {
    private static final String VERSION_URL = "https://raw.githubusercontent.com/wilkyyb/Kauna/refs/heads/master/version.txt";
    private static final String CURRENT_VERSION = "0.6.0";
    public static boolean updateAvailable = false;

    public static void checkVersion() {
        if (KaunaConfig.INSTANCE.CheckForUpdates) {
            new Thread(() -> {
                try {
                    URL url = URI.create(VERSION_URL).toURL();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        String latestVersion = reader.readLine();
                        if (latestVersion != null && !latestVersion.trim().equals(CURRENT_VERSION)) {
                            updateAvailable = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[Kauna] Version check failed: " + e.getMessage());
                }
            }).start();
        }
    }
}