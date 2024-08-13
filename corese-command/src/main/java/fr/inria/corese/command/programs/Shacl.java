package fr.inria.corese.command.programs;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.TestType;
import fr.inria.corese.command.utils.exporter.rdf.EnumRdfOutputFormat;
import fr.inria.corese.command.utils.exporter.rdf.RdfDataExporter;
import fr.inria.corese.command.utils.loader.rdf.EnumRdfInputFormat;
import fr.inria.corese.command.utils.loader.rdf.RdfDataLoader;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "shacl", version = App.version, description = "Run SHACL validation on a RDF dataset.", mixinStandardHelpOptions = true)
public class Shacl extends AbstractInputCommand {

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "Specifies the RDF serialization format of the input file. Possible values are: :@|fg(225) ${COMPLETION-CANDIDATES}|@.")
    private EnumRdfInputFormat inputFormat = null;

    @Option(names = { "-a", "-sf",
            "--shapes-format" }, description = "Specifies the serialization format of the SHACL shapes. Possible values are: :@|fg(225) ${COMPLETION-CANDIDATES}|@.)")
    private EnumRdfInputFormat reportFormat = null;

    @Option(names = { "-s",
            "--shapes" }, description = "Specifies the path or URL of the file containing the SHACL shapes.", arity = "1...", required = true)
    private String[] shaclShapes;

    @Option(names = { "-r", "-of",
            "--output-format" }, description = "Specifies the serialization format of the validation report. Possible values are: :@|fg(225) ${COMPLETION-CANDIDATES}|@. Default value: ${DEFAULT-VALUE}.", defaultValue = "TURTLE")
    private EnumRdfOutputFormat outputFormat = null;

    public Integer call() {

        super.call();

        try {
            // Load input file(s)
            RdfDataLoader loader = new RdfDataLoader(this.spec, this.verbose);
            Graph dataGraph = loader.load(this.inputsRdfData, this.inputFormat, this.recursive);

            // Load shapes file(s)
            Graph shapesGraph = loader.load(this.shaclShapes, this.reportFormat, this.recursive);

            // Check if shapes graph contains SHACL shapes
            if (!TestType.isShacl(shapesGraph)) {
                throw new IllegalArgumentException("No SHACL shapes found in the input file(s).");
            }

            // Evaluation of SHACL shapes
            Graph reportGraph = this.evaluateSHACLShapes(dataGraph, shapesGraph);

            // Export the report graph
            RdfDataExporter rdfExporter = new RdfDataExporter(this.spec, this.verbose, this.output);
            rdfExporter.export(reportGraph, this.outputFormat);

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (Exception e) {
            this.spec.commandLine().getErr().println("Error: " + e.getMessage());
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Evaluate SHACL shapes.
     * 
     * @param dataGraph   The data graph.
     * @param shapesGraph The shapes graph.
     * @return The report graph.
     * @throws Exception If an error occurs while evaluating SHACL shapes.
     */
    private Graph evaluateSHACLShapes(Graph dataGraph, Graph shapesGraph) throws Exception {

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Evaluating SHACL shapes...");
        }

        fr.inria.corese.core.shacl.Shacl shacl = new fr.inria.corese.core.shacl.Shacl(dataGraph, shapesGraph);
        try {
            return shacl.eval();
        } catch (Exception e) {
            throw new Exception("Error while evaluating SHACL shapes: " + e.getMessage(), e);
        }
    }

}
