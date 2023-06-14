package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

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
    APPLICATION_RDF_XML(1, "application/rdf+xml"),
    TEXT_TURTLE(2, "text/turtle"),
    APPLICATION_TRIG(3, "application/trig"),
    APPLICATION_LD_JSON(4, "application/ld+json"),
    APPLICATION_XML(11, "application/xml"),
    APPLICATION_JSON(13, "application/json"),
    TEXT_CSV(14, "text/csv"),
    TEXT_TSV(15, "text/tab-separated-values");

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
            case APPLICATION_XML:
                return "xml";
            case BIDING_JSON:
            case APPLICATION_JSON:
                return "json";
            case BIDING_CSV:
            case TEXT_CSV:
                return "csv";
            case BIDING_TSV:
            case TEXT_TSV:
                return "tsv";
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
