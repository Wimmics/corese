package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumResultFormat {
    RDFXML(1),
    TURTLE(2),
    TRIG(3),
    JSONLD(4),
    CSV(14),
    TSV(15);

    private final int value;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     */
    EnumResultFormat(int value) {
        this.value = value;
    }

    /**
     * Get the value of the enum.
     * 
     * @param isSelect True if the query is a SELECT query.
     * @return The value of the enum.
     */
    public int getValue(boolean isSelect) {
        switch (this.value) {
            case 1:
                return isSelect ? 11 : 1;
            case 2:
                return isSelect ? 12 : 2;
            case 3:
                return isSelect ? 12 : 3;
            case 4:
                return isSelect ? 13 : 4;
            case 14:
                return 14;
            case 15:
                return 15;
            default:
                throw new InvalidParameterException("Output format " + this + " is unknow.");
        }
    }

    /**
     * Get the extension of the format.
     * 
     * @return The extension.
     */
    public String getExtention() {
        switch (this) {
            case RDFXML:
                return "xml";
            case TURTLE:
                return "ttl";
            case TRIG:
                return "trig";
            case JSONLD:
                return "jsonld";
            case CSV:
                return "csv";
            case TSV:
                return "tsv";
            default:
                throw new InvalidParameterException("Output format " + this + " is unknow.");
        }
    }

    /**
     * Get the enum from its value.
     * 
     * @param value The value of the enum.
     * @return The enum.
     */
    public static EnumResultFormat fromValue(int value) {
        switch (value) {
            case 1:
                return RDFXML;
            case 2:
                return TURTLE;
            case 3:
                return TRIG;
            case 4:
                return JSONLD;
            case 11:
                return RDFXML;
            case 12:
                return TURTLE;
            case 13:
                return JSONLD;
            case 14:
                return CSV;
            case 15:
                return TSV;
            default:
                throw new InvalidParameterException("Output format " + value + " is unknow.");
        }
    }

    /**
     * Convert to the corresponding OutputFormat.
     * 
     * @return The corresponding OutputFormat.
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
                return null;
        }
    }
}
