package fr.inria.corese.command.utils.format;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumOutputFormat {
    RDFXML(1, "rdfxml", "rdf"),
    RDF(1, "rdf", "rdf"),
    APPLICATION_RDF_XML(1, "application/rdf+xml", "rdf"),

    TURTLE(2, "turtle", "ttl"),
    TTL(2, "ttl", "ttl"),
    TEXT_TURTLE(2, "text/turtle", "ttl"),

    TRIG(3, "trig", "trig"),
    APPLICATION_TRIG(3, "application/trig", "trig"),

    JSONLD(4, "jsonld", "jsonld"),
    APPLICATION_LD_JSON(4, "application/ld+json", "jsonld"),

    NTRIPLES(6, "ntriples", "nt"),
    NT(6, "nt", "nt"),
    APPLICATION_NTRIPLES(6, "application/n-triples", "nt"),

    NQUADS(7, "nquads", "nq"),
    NQ(7, "nq", "nq"),
    APPLICATION_NQUADS(7, "application/n-quads", "nq");

    private final int value;
    private final String name;
    private final String extention;

    /**
     * Constructor.
     * 
     * @param value     The value of the enum.
     * @param name      The name of the enum.
     * @param extention The extension of the format.
     */
    private EnumOutputFormat(int value, String name, String extention) {
        this.value = value;
        this.name = name;
        this.extention = extention;
    }

    /**
     * Get the value of the enum.
     * 
     * @return The value of the enum.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Get the name of the enum.
     * 
     * @return The name of the enum.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the extension of the format.
     * 
     * @return The extension.
     */
    public String getExtention() {
        return this.extention;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
