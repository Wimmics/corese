package fr.inria.corese.command.programs;

import java.nio.file.Path;

import picocli.CommandLine.Option;

public abstract class AbstractInputCommand extends AbstractCommand {

    @Option(names = { "-i",
            "--input-data" }, description = "Specifies the path or URL of the input RDF data. Multiple values are allowed.", arity = "1...")
    protected String[] inputsRdfData;

    @Option(names = { "-R",
            "--recursive" }, description = "If set to true and an input is a directory, all files in the directory will be loaded recursively. Default value: ${DEFAULT-VALUE}.", defaultValue = "false")
    protected boolean recursive = false;

    @Override
    public Integer call() {
        super.call();

        // Check input values
        this.checkInputValues();

        return 0;
    }

    /**
     * Check if the input values are correct.
     *
     * @throws IllegalArgumentException if input path is same as output path.
     */
    private void checkInputValues() throws IllegalArgumentException {
        if (this.inputsRdfData != null && this.output != null) {
            for (String input : this.inputsRdfData) {
                if (Path.of(input).compareTo(this.output) == 0) {
                    throw new IllegalArgumentException("Input path cannot be same as output path: " + input);
                }
            }
        }
    }

}