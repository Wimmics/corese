package fr.inria.corese.command.programs;

import java.nio.file.Path;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.InputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "sparql", version = App.version, description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Sparql implements Runnable {

    @Parameters(paramLabel = "INPUT_FORMAT", description = "Input file format."
            + " Candidates: ${COMPLETION-CANDIDATES}")
    private InputFormat inputFormat;

    @Parameters(paramLabel = "INPUT", description = "Input file path.")
    private String intputPath;

    @Parameters(paramLabel = "QUERY", description = "Request to execute.")
    private String querySrting;

    private Graph graph;

    public Sparql() {
    }

    @Override
    public void run() {
        this.graph = GraphUtils.load(this.intputPath, this.inputFormat);
        this.execute();
    }

    private void execute() {
        QueryProcess exec = QueryProcess.create(graph);
        ASTQuery ast = null;
        Mappings map = null;

        // Execute query
        try {
            ast = exec.ast(this.querySrting);
            map = exec.query(ast);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Print results

        if (ast.isAsk()) {
            System.out.println(!map.isEmpty());
        } else if (ast.isConstruct()) {
            System.out.println(map.getGraph());
        } else if (ast.isSelect()) {
            for (Mapping m : map) {
                System.out.println(m);
            }
        }

    }

}
