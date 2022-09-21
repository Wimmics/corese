package fr.inria.corese.cli;

import fr.inria.corese.cli.programs.Convert;
import fr.inria.corese.cli.programs.Profile;
import fr.inria.corese.cli.programs.Sparql;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Hello world!
 */
@Command(name = "Corese-CLI", version = "4.3.0", mixinStandardHelpOptions = true, subcommands = {
        Convert.class, Sparql.class, Profile.class,
})
public final class App implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Print usage
        CommandLine.usage(new App(), System.out);
        ;
    }
}