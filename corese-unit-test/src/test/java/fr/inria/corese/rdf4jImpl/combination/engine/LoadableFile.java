package fr.inria.corese.rdf4jImpl.combination.engine;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import fr.inria.corese.kgram.api.core.ExpType;

public class LoadableFile {

    private String file_name;
    private String file_path;
    private RdfFormat format;
    private String[] contexts;

    public LoadableFile(String file, RdfFormat format, String... contexts) {
        this.file_name = file;
        this.file_path = LoadableFile.class.getResource(file_name).getPath();
        this.format = format;
        this.contexts = contexts;
    }

    public String getFilePath() {
        return this.file_path;
    }

    public int getFormatCorese() {
        switch (this.format) {
            case RDFXML:
                return 0;

            case TURTLE:
                return 2;

            case N3:
            case NTRIPLES:
                return 3;

            case JSONLD:
                return 4;

            case TRIG:
                return 8;

            default:
                throw new IllegalArgumentException("Unknown format");
        }
    }

    public RDFFormat getFormatRdf4j() {
        switch (this.format) {
            case RDFXML:
                return RDFFormat.RDFXML;

            case TURTLE:
                return RDFFormat.TURTLE;

            case N3:
                return RDFFormat.N3;

            case NTRIPLES:
                return RDFFormat.NTRIPLES;

            case JSONLD:
                return RDFFormat.JSONLD;

            case TRIG:
                return RDFFormat.TRIG;

            default:
                throw new IllegalArgumentException("Unknown format");
        }
    }

    public String[] getContextsCorese() {
        if (this.contexts.length == 0) {
            return new String[] { ExpType.DEFAULT_GRAPH };
        } else {
            return this.contexts;
        }
    }

    public IRI[] getContextsRDF4J() {
        SimpleValueFactory vf = SimpleValueFactory.getInstance();

        IRI[] IRIs = new IRI[contexts.length];
        for (int i = 0; i < contexts.length; i++) {
            IRIs[i] = vf.createIRI(contexts[i]);
        }

        return IRIs;
    }

    @Override
    public String toString() {
        String result = "";
        result += "(";
        result += this.file_name + ", ";
        result += this.format + ", ";

        result += "[";
        for (String context : this.contexts) {
            result += context;
        }
        result += "]";

        result += ")";

        return result;
    }

}
