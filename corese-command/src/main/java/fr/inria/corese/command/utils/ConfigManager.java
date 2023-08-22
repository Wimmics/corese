package fr.inria.corese.command.utils;

import java.nio.file.Path;

import fr.inria.corese.core.util.Property;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to manage configuration files.
 */
public class ConfigManager {

    /**
     * Load a configuration file.
     *
     * @param path    Path of the file to load.
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     */
    public static void loadFromFile(Path path, CommandSpec spec, boolean verbose) {

        try {
            Property.load(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open config file: " + path.toString(), e);
        }

        if (verbose) {
            spec.commandLine().getErr().println("Loaded config file: " + path.toString());
        }
    }

    /**
     * Load the default configuration file.
     *
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     */
    public static void loadDefaultConfig(CommandSpec spec, boolean verbose) {
        if (verbose) {
            spec.commandLine().getErr().println("Loaded default config");
        }
    }

}
