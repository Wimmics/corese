package fr.inria.corese.command.programs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.sparql.api.IDatatype;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "ldscript", version = "4.4.0", description = "Run an LDSCRIPT file.", mixinStandardHelpOptions = true)
public class LDScript implements Runnable {

    @Parameters(paramLabel = "INPUT", description = "LDScript file to run.")
    private Path intputPath;

    @Override
    public void run() {

        // Open LDScript file
        String ldScript = "";
        try {
            ldScript = Files.readString(intputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Compile LDScript
        QueryProcess exec = QueryProcess.create();
        try {
            exec.compile(ldScript);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Execute program
        String name = "http://ns.inria.fr/main";
        try {
            IDatatype dt = exec.funcall(name);
            System.out.println(dt);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}
