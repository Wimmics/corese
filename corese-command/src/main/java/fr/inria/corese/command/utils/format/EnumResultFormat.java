package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.print.ResultFormat;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumResultFormat {
    RDFXML(1, "rdfxml", "rdf", true),
    RDF(1, "rdf", "rdf", true),
    APPLICATION_RDF_XML(1, "application/rdf+xml", "rdf", true),

    TURTLE(2, "turtle", "ttl", true),
    TTL(2, "ttl", "ttl", true),
    TEXT_TURTLE(2, "text/turtle", "ttl", true),

    TRIG(3, "trig", "trig", true),
    APPLICATION_TRIG(3, "application/trig", "trig", true),

    JSONLD(4, "jsonld", "jsonld", true),
    APPLICATION_LD_JSON(4, "application/ld+json", "jsonld", true),

    NTRIPLES(6, "ntriples", "nt", true),
    NT(6, "nt", "nt", true),
    APPLICATION_NTRIPLES(6, "application/n-triples", "nt", true),

    NQUADS(7, "nquads", "nq", true),
    NQ(7, "nq", "nq", true),
    APPLICATION_NQUADS(7, "application/n-quads", "nq", true),

    BIDING_XML(11, "xml", "srx", false),
    SRX(11, "srx", "srx", false),
    APPLICATION_SPARQL_RESULTS_XML(11, ResultFormat.SPARQL_RESULTS_XML, "srx", false),

    BIDING_JSON(13, "json", "srj", false),
    SRJ(13, "srj", "srj", false),
    APPLICATION_SPARQL_RESULTS_JSON(13, ResultFormat.SPARQL_RESULTS_JSON, "srj", false),

    BIDING_CSV(14, "csv", "csv", false),
    TEXT_CSV(14, ResultFormat.SPARQL_RESULTS_CSV, "csv", false),

    BIDING_TSV(15, "tsv", "tsv", false),
    TEXT_TSV(15, ResultFormat.SPARQL_RESULTS_TSV, "tsv", false),

    BIDING_MARKDOWN(16, "markdown", "md", false),
    BIDING_MD(16, "md", "md", false),
    TEXT_MARKDOWN(16, ResultFormat.SPARQL_RESULTS_MD, "md", false);

    private final int value;
    private final String name;
    private final String extention;
    private final boolean isRDFFormat;

    /**
     * Constructor.
     * 
     * @param value     The value of the enum.
     * @param name      The name of the enum.
     * @param extention The extension of the format.
     */
    private EnumResultFormat(int value, String name, String extention, boolean isRDFFormat) {
        this.value = value;
        this.name = name;
        this.extention = extention;
        this.isRDFFormat = isRDFFormat;
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
     * @throws InvalidParameterException If the format is unknow.
     */
    public String getExtention() {
        return this.extention;
    }

    /**
     * Get if the format is a RDF format.
     * 
     * @return True if the format is a RDF format, false otherwise.
     */
    public boolean isRDFFormat() {
        return this.isRDFFormat;
    }

    /**
     * Convert to the corresponding OutputFormat.
     * 
     * @return The corresponding OutputFormat.
     * @throws InvalidParameterException If the format is unknow.
     */
    public EnumOutputFormat convertToOutputFormat() {
        switch (this) {
            case RDFXML:
            case RDF:
            case APPLICATION_RDF_XML:
                return EnumOutputFormat.RDFXML;
            case TURTLE:
            case TTL:
            case TEXT_TURTLE:
                return EnumOutputFormat.TURTLE;
            case TRIG:
            case APPLICATION_TRIG:
                return EnumOutputFormat.TRIG;
            case JSONLD:
            case APPLICATION_LD_JSON:
                return EnumOutputFormat.JSONLD;
            case NTRIPLES:
            case NT:
            case APPLICATION_NTRIPLES:
                return EnumOutputFormat.NTRIPLES;
            case NQUADS:
            case NQ:
            case APPLICATION_NQUADS:
                return EnumOutputFormat.NQUADS;

            default:
                throw new InvalidParameterException("Output format " + this + " cannot be converted to OutputFormat.");
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
