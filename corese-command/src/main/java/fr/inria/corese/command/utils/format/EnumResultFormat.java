package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumResultFormat {
    RDFXML,
    TURTLE,
    TRIG,
    JSONLD,
    BIDING_XML,
    BIDING_TURTLE,
    BIDING_TRIG,
    BIDING_JSON,
    BIDING_CSV,
    BIDING_TSV;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     */
    EnumResultFormat() {
    }

    /**
     * Get the corresponding ResultFormat.
     * 
     * @return The corresponding ResultFormat.
     */
    public int getResultFormatValue() {
        switch (this) {
            case RDFXML:
                return 1;
            case TURTLE:
                return 2;
            case TRIG:
                return 3;
            case JSONLD:
                return 4;
            case BIDING_XML:
                return 11;
            case BIDING_TURTLE:
            case BIDING_TRIG:
                return 12;
            case BIDING_JSON:
                return 13;
            case BIDING_CSV:
                return 14;
            case BIDING_TSV:
                return 15;
            default:
                throw new InvalidParameterException("Output format " + this + " is unknow.");
        }
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
                return "rdf";
            case TURTLE:
                return "ttl";
            case TRIG:
                return "trig";
            case JSONLD:
                return "jsonld";
            case BIDING_XML:
                return "xml";
            case BIDING_TURTLE:
                return "ttl";
            case BIDING_JSON:
                return "json";
            case BIDING_CSV:
                return "csv";
            case BIDING_TSV:
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
                return EnumOutputFormat.RDFXML;
            case TURTLE:
                return EnumOutputFormat.TURTLE;
            case TRIG:
                return EnumOutputFormat.TRIG;
            case JSONLD:
                return EnumOutputFormat.JSONLD;
            default:
                throw new InvalidParameterException("Output format " + this + " cannot be converted to OutputFormat.");
        }
    }
}
