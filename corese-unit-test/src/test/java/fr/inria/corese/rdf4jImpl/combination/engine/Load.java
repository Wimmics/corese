package fr.inria.corese.rdf4jImpl.combination.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.rdf4j.CoreseGraphModel;

public class Load {

    public static Graph coreseGraph(LoadableFile... load_files) {
        Graph graph = Graph.create();

        for (LoadableFile load_file : load_files) {
            fr.inria.corese.core.load.Load ld = fr.inria.corese.core.load.Load.create(graph);

            String input = load_file.getFilePath();
            int format = load_file.getFormatCorese();
            String[] contexts = load_file.getContextsCorese();

            for (String context : contexts) {
                try {
                    ld.parse(input, context, format);
                } catch (LoadException e) {
                    System.err.println("Error : Impossible to load " + load_file);
                    e.printStackTrace();
                }
            }
        }

        return graph;
    }

    private static Model rdf4jModel(Model model, LoadableFile... load_files) {
        for (LoadableFile load_file : load_files) {
            InputStream input = null;
            try {
                input = new FileInputStream(load_file.getFilePath());
            } catch (FileNotFoundException e1) {
                System.err.println("Error : File not found : " + load_file);
                e1.printStackTrace();
            }
            RDFFormat format = load_file.getFormatRdf4j();
            IRI[] contexts = load_file.getContextsRDF4J();

            try {
                model.addAll(Rio.parse(input, format, contexts));
            } catch (RDFParseException e) {
                System.err.println("Error : Impossible to parse " + load_file);
                e.printStackTrace();
            } catch (UnsupportedRDFormatException e) {
                System.err.println("Error : Unsupported RDF format " + load_file);
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error : Impossible to read " + load_file);
                e.printStackTrace();
            }
        }

        return model;
    }

    public static TreeModel treeModel(LoadableFile... load_files) {
        return (TreeModel) rdf4jModel(new TreeModel(), load_files);
    }

    public static CoreseGraphModel coreseModel(LoadableFile... load_files) {
        return (CoreseGraphModel) rdf4jModel(new CoreseGraphModel(), load_files);
    }

}
