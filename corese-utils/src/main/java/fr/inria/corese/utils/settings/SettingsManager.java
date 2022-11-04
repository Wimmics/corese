package fr.inria.corese.utils.settings;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SettingsManager {

    private static Config config;
    private static Settings settings;

    private final static InputStream DEFAULT_CONFIG_PATH = SettingsManager.class
            .getResourceAsStream("/property/core-property.conf");

    /////////////////
    // Constructor //
    /////////////////

    private SettingsManager() {
    }

    //////////
    // Load //
    //////////

    public static void load() {
        SettingsManager.config = SettingsManager.loadDefaultConfig();
        SettingsManager.settings = new Settings(config);
    }

    public static void load(String userConfigFilePath) {

        Config userConfig = SettingsManager.loadUserConfig(userConfigFilePath);
        Config defaultConfig = SettingsManager.loadDefaultConfig();

        // Complete the default configuration with the user configuration
        SettingsManager.config = userConfig.withFallback(defaultConfig).resolve();
        SettingsManager.settings = new Settings(config);

        // Check if the user's configuration is valid compared to the default
        // configuration
        SettingsManager.config.checkValid(defaultConfig);
    }

    public static Config loadDefaultConfig() {
        return ConfigFactory.parseReader(new InputStreamReader(DEFAULT_CONFIG_PATH));
    }

    private static Config loadUserConfig(String userConfigFilePath) {
        File userConfigFile = new File(userConfigFilePath);
        return ConfigFactory.parseFile(userConfigFile);
    }

    //////////////////
    // Get property //
    //////////////////

    // TODO: Documentation
    public static Settings getSettings() {

        if (config == null || settings == null) {
            try {
                throw new Exception(
                        "You must load a configuration before using it. Call SettingsManager.load() to load the default configuration or SettingsManager(pathUserConfig) to load a custom configuration.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return settings;
    }

}
