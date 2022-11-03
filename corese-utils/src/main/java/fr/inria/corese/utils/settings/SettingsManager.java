package fr.inria.corese.utils.settings;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SettingsManager {

    private static Config config;
    private static Settings settings;

    private final static String DEFAULT_CONFIG_PATH = SettingsManager.class
            .getResource("/property/core-property.conf").getPath();

    /////////////////
    // Constructor //
    /////////////////

    private SettingsManager() {
    }

    //////////
    // Load //
    //////////

    public static void load() {
        SettingsManager.config = SettingsManager.loadDefaultConfig().resolve();
    }

    public static void load(String userConfigFilePath) {

        Config userConfig = SettingsManager.loadUserConfig(userConfigFilePath);
        Config defaultConfig = SettingsManager.loadDefaultConfig();

        // Complete the default configuration with the user configuration
        SettingsManager.config = userConfig.withFallback(defaultConfig).resolve();

        // Check if the user's configuration is valid compared to the default
        // configuration
        SettingsManager.config.checkValid(defaultConfig);
    }

    public static Config loadDefaultConfig() {
        return ConfigFactory.parseFile(new File(DEFAULT_CONFIG_PATH));
    }

    private static Config loadUserConfig(String userConfigFilePath) {
        File userConfigFile = new File(userConfigFilePath);
        return ConfigFactory.parseFile(userConfigFile);
    }

    //////////////////
    // Get property //
    //////////////////

    public static Settings getSettings() {

        if (config == null) {
            SettingsManager.config = loadDefaultConfig();
        }

        if (settings == null) {
            SettingsManager.settings = new Settings(config);
        }

        return settings;
    }

}
