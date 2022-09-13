package fr.inria.corese.jenaImpl.combination.engine;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Quad;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;

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

    public static Dataset JenaDataset(LoadableFile... load_files) {
        Dataset dataset = DatasetFactory.create();

        for (LoadableFile load_file : load_files) {
            Model model = ModelFactory.createDefaultModel();

            String input = load_file.getFilePath();
            String[] contexts = load_file.getContextsCorese();

            model.read(input);

            for (String context : contexts) {
                if (context == "http://ns.inria.fr/corese/kgram/default") {
                    context = Quad.defaultGraphIRI.getURI();
                }
                dataset.addNamedModel(context, model);
            }
        }

        return dataset;
    }

}
