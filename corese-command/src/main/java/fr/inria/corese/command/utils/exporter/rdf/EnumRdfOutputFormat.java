package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.sparql.api.ResultFormatDef;

/**
 * Enumeration of output RDF serialization formats.
 */
public enum EnumRdfOutputFormat {
    // RdfXml
    RDFXML("rdfxml", ResultFormatDef.RDF_XML_FORMAT, "rdf"),
    APPLICATION_RDF_XML("application/rdf+xml", ResultFormatDef.RDF_XML_FORMAT, "rdf"),
    RDF("rdf", ResultFormatDef.RDF_XML_FORMAT, "rdf"),

    // Turtle
    TURTLE("turtle", ResultFormatDef.TURTLE_FORMAT, "ttl"),
    TEXT_TURTLE("text/turtle", ResultFormatDef.TURTLE_FORMAT, "ttl"),
    TTL("ttl", ResultFormatDef.TURTLE_FORMAT, "ttl"),

    // Trig
    TRIG("trig", ResultFormatDef.TRIG_FORMAT, "trig"),
    APPLICATION_TRIG("application/trig", ResultFormatDef.TRIG_FORMAT, "trig"),

    // JsonLd
    JSONLD("jsonld", ResultFormatDef.JSONLD_FORMAT, "jsonld"),
    APPLICATION_LD_JSON("application/ld+json", ResultFormatDef.JSONLD_FORMAT, "jsonld"),

    // Ntriples
    NTRIPLES("ntriples", ResultFormatDef.NTRIPLES_FORMAT, "nt"),
    APPLICATION_N_TRIPLES("application/n-triples", ResultFormatDef.NTRIPLES_FORMAT, "nt"),
    NT("nt", ResultFormatDef.NTRIPLES_FORMAT, "nt"),

    // Nquads
    NQUADS("nquads", ResultFormatDef.NQUADS_FORMAT, "nq"),
    APPLICATION_N_QUADS("application/n-quads", ResultFormatDef.NQUADS_FORMAT, "nq"),
    NQ("nq", ResultFormatDef.NQUADS_FORMAT, "nq"),

    // Rdfc-1.0-sha256
    RDFC10("rdfc-1.0", ResultFormatDef.RDFC10_FORMAT, "nq"),
    RDFC10SHA256("rdfc-1.0-sha256", ResultFormatDef.RDFC10_FORMAT, "nq"),

    // Rdfc-1.0-sha384
    RDFC10SHA384("rdfc-1.0-sha384", ResultFormatDef.RDFC10_SHA384_FORMAT, "nq");

    private final String name;
    private final int coreseCodeFormat;
    private final String extention;

    /**
     * Constructor.
     * 
     * @param name             The name of the format.
     * @param coreseCodeFormat The Corese code of the format.
     * @param extention        The extension file for the format.
     */
    private EnumRdfOutputFormat(String name, int coreseCodeFormat, String extention) {
        this.name = name;
        this.coreseCodeFormat = coreseCodeFormat;
        this.extention = extention;
    }

    /**
     * Get the Corese code of the format.
     * 
     * @return The Corese code of the format.
     */
    public int getCoreseCodeFormat() {
        return this.coreseCodeFormat;
    }

    /**
     * Get the extension of the file format associated with the format.
     * 
     * @return The extension of the file format associated with the format.
     */
    public String getExtention() {
        return this.extention;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
