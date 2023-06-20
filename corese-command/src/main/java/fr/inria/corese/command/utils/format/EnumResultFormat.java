package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.print.ResultFormat;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumResultFormat {
    RDFXML(1, "rdfxml"),
    TURTLE(2, "turtle"),
    TRIG(3, "trig"),
    JSONLD(4, "jsonld"),
    BIDING_XML(11, "xml"),
    BIDING_JSON(13, "json"),
    BIDING_CSV(14, "csv"),
    BIDING_TSV(15, "tsv"),
    BIDING_MARKDOWN(16, "markdown"),
    BIDING_MD(16, "md"),
    APPLICATION_RDF_XML(1, ResultFormat.RDF_XML),
    TEXT_TURTLE(2, ResultFormat.TURTLE_TEXT),
    APPLICATION_TRIG(3, ResultFormat.TRIG),
    APPLICATION_LD_JSON(4, ResultFormat.JSON_LD),
    APPLICATION_SPARQL_RESULTS_XML(11, ResultFormat.SPARQL_RESULTS_XML),
    APPLICATION_SPARQL_RESULTS_JSON(13, ResultFormat.SPARQL_RESULTS_JSON),
    TEXT_CSV(14, ResultFormat.SPARQL_RESULTS_CSV),
    TEXT_TSV(15, ResultFormat.SPARQL_RESULTS_TSV),
    TEXT_MARKDOWN(16, ResultFormat.SPARQL_RESULTS_MD);

    private final int value;
    private final String name;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     * @param name  The name of the enum.
     */
    EnumResultFormat(int value, String name) {
        this.value = value;
        this.name = name;
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
        switch (this) {
            case RDFXML:
            case APPLICATION_RDF_XML:
                return "rdf";
            case TURTLE:
            case TEXT_TURTLE:
                return "ttl";
            case TRIG:
            case APPLICATION_TRIG:
                return "trig";
            case JSONLD:
            case APPLICATION_LD_JSON:
                return "jsonld";
            case BIDING_XML:
            case APPLICATION_SPARQL_RESULTS_XML:
                return "srx";
            case BIDING_JSON:
            case APPLICATION_SPARQL_RESULTS_JSON:
                return "srj";
            case BIDING_CSV:
            case TEXT_CSV:
                return "csv";
            case BIDING_TSV:
            case TEXT_TSV:
                return "tsv";
            case BIDING_MD:
            case BIDING_MARKDOWN:
            case TEXT_MARKDOWN:
                return "md";
            default:
                throw new InvalidParameterException("Output format " + this + " is unknow.");
        }
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
            case APPLICATION_RDF_XML:
                return EnumOutputFormat.RDFXML;
            case TURTLE:
            case TEXT_TURTLE:
                return EnumOutputFormat.TURTLE;
            case TRIG:
            case APPLICATION_TRIG:
                return EnumOutputFormat.TRIG;
            case JSONLD:
            case APPLICATION_LD_JSON:
                return EnumOutputFormat.JSONLD;
            default:
                throw new InvalidParameterException("Output format " + this + " cannot be converted to OutputFormat.");
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}
