package fr.inria.corese.command.programs;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.exporter.rdf.RdfDataExporter;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * Abstract class for all commands.
 * 
 * This class provides common options and methods for all commands.
 */
@Command(version = App.version)
public abstract class AbstractCommand implements Callable<Integer> {

    ///////////////
    // Constants //
    ///////////////

    // Exit codes
    protected final int ERROR_EXIT_CODE_SUCCESS = 0;
    protected final int ERROR_EXIT_CODE_ERROR = 1;

    /////////////
    // Options //
    /////////////

    @Option(names = { "-o",
            "--output-data" }, description = "Specifies the output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = RdfDataExporter.DEFAULT_OUTPUT)
    protected Path output;

    @Option(names = { "-c", "--config",
            "--init" }, description = "Specifies the path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private Path configFilePath;

    @Option(names = { "-v",
            "--verbose" }, description = "Enables verbose mode, printing more information about the execution of the command.")
    protected boolean verbose = false;

    @Option(names = { "-w",
            "--no-owl-import" }, description = "Disables the automatic importation of ontologies specified in 'owl:imports' statements. When this flag is set, the application will not fetch and include referenced ontologies.", required = false, defaultValue = "false")
    private boolean noOwlImport;

    ////////////////
    // Properties //
    ////////////////

    // Command specification
    @Spec
    protected CommandSpec spec;

    // Output
    protected Boolean outputToFileIsDefault = false;

    /////////////
    // Methods //
    /////////////

    @Override
    public Integer call() {

        // Load configuration file
        Optional<Path> configFilePath = Optional.ofNullable(this.configFilePath);
        if (configFilePath.isPresent()) {
            ConfigManager.loadFromFile(configFilePath.get(), this.spec, this.verbose);
        } else {
            ConfigManager.loadDefaultConfig(this.spec, this.verbose);
        }

        // Set owl import
        Property.set(Value.DISABLE_OWL_AUTO_IMPORT, this.noOwlImport);

        return 0;
    }

}
