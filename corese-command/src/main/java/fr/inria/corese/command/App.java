package fr.inria.corese.command;

import fr.inria.corese.command.programs.Canonicalize;
import fr.inria.corese.command.programs.Convert;
import fr.inria.corese.command.programs.RemoteSparql;
import fr.inria.corese.command.programs.Shacl;
import fr.inria.corese.command.programs.Sparql;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;

@Command(name = "Corese-command", version = App.version, mixinStandardHelpOptions = true, subcommands = {
        Convert.class, Sparql.class, Shacl.class, RemoteSparql.class, Canonicalize.class, GenerateCompletion.class
})

public final class App implements Runnable {

    public final static String version = "4.5.1";

    public static void main(String[] args) {
        // Define the color scheme
        ColorScheme colorScheme = new ColorScheme.Builder()
                .commands(Style.bold) // Commands in blue
                .options(Style.fg_yellow) // Options in yellow
                .parameters(Style.fg_cyan, Style.bold) // Parameters in cyan and bold
                .optionParams(Style.italic, Style.fg_cyan) // Option parameters in italic
                .errors(Style.fg_red, Style.bold) // Errors in red and bold
                .stackTraces(Style.italic) // Stack traces in italic
                .applySystemProperties() // Apply system properties for colors
                .build();

        CommandLine commandLine = new CommandLine(new App()).setColorScheme(colorScheme);

        // Hide the generate-completion command
        CommandLine gen = commandLine.getSubcommands().get("generate-completion");
        gen.getCommandSpec().usageMessage().hidden(true);

        // Execute the command
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Print usage
        CommandLine.usage(new App(), System.out);
    }
}