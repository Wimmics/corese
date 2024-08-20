package fr.inria.corese.command.utils.loader.rdf;

import java.security.InvalidParameterException;

import fr.inria.corese.core.api.Loader;

/**
 * Enumeration of input RDF serialization formats.
 */
public enum EnumRdfInputFormat {

    // RdfXml
    RDFXML("rdfxml", Loader.RDFXML_FORMAT),
    APPLICATION_RDF_XML("application/rdf+xml", Loader.RDFXML_FORMAT),
    RDF("rdf", Loader.RDFXML_FORMAT),

    // Turtle
    TURTLE("turtle", Loader.TURTLE_FORMAT),
    TEXT_TURTLE("text/turtle", Loader.TURTLE_FORMAT),
    TTL("ttl", Loader.TURTLE_FORMAT),

    // Trig
    TRIG("trig", Loader.TRIG_FORMAT),
    APPLICATION_TRIG("application/trig", Loader.TRIG_FORMAT),

    // JsonLd
    JSONLD("jsonld", Loader.JSONLD_FORMAT),
    APPLICATION_LD_JSON("application/ld+json", Loader.JSONLD_FORMAT),

    // Ntriples
    NTRIPLES("ntriples", Loader.NT_FORMAT),
    APPLICATION_N_TRIPLES("application/n-triples", Loader.NT_FORMAT),
    NT("nt", Loader.NT_FORMAT),

    // Nquads
    NQUADS("nquads", Loader.NQUADS_FORMAT),
    APPLICATION_N_QUADS("application/n-quads", Loader.NQUADS_FORMAT),
    NQ("nq", Loader.NQUADS_FORMAT),

    // Rdfa
    RDFA("rdfa", Loader.RDFA_FORMAT),
    APPLICATION_XHTML_XML("application/xhtml+xml", Loader.RDFA_FORMAT),
    XHTML("xhtml", Loader.RDFA_FORMAT),
    HTML("html", Loader.RDFA_FORMAT);

    private final String name;
    private final int coreseCodeFormat;

    /**
     * Constructor.
     * 
     * @param name             The name of the format.
     * @param coreseCodeFormat The Corese code of the format.
     */
    private EnumRdfInputFormat(String name, int coreseCodeFormat) {
        this.name = name;
        this.coreseCodeFormat = coreseCodeFormat;
    }

    /**
     * Create an EnumInputFormat from a Corese code.
     */
    public static EnumRdfInputFormat create(int loaderFormat) {
        for (EnumRdfInputFormat format : EnumRdfInputFormat.values()) {
            if (format.coreseCodeFormat == loaderFormat) {
                return format;
            }
        }
        throw new InvalidParameterException(
                "Impossible to determine the input format, please specify it with the -f or -if or --input-format option.");
    }

    /**
     * Get the Corese code of the format.
     */
    public int getCoreseCode() {
        return this.coreseCodeFormat;
    }

    @Override
    public String toString() {
        return name;
    }
}
