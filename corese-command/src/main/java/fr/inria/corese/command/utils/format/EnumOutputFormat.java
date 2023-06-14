package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.transform.Transformer;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumOutputFormat {
    RDFXML(1, "rdfxml"),
    TURTLE(2, "turtle"),
    TRIG(3, "trig"),
    JSONLD(4, "jsonld"),
    APPLICATION_RDF_XML(1, "application/rdf+xml"),
    TEXT_TURTLE(2, "text/turtle"),
    APPLICATION_TRIG(3, "application/trig"),
    APPLICATION_LD_JSON(4, "application/ld+json");

    private final int value;
    private final String name;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     * @param name  The name of the enum.
     */
    EnumOutputFormat(int value, String name) {
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
     */
    public String getExtention() {
        switch (this) {
            case RDFXML:
            case APPLICATION_RDF_XML:
                return "xml";

            case TURTLE:
            case TEXT_TURTLE:
                return "ttl";

            case TRIG:
            case APPLICATION_TRIG:
                return "trig";

            case JSONLD:
            case APPLICATION_LD_JSON:
                return "jsonld";

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
    public static EnumOutputFormat fromValue(int value) {
        for (EnumOutputFormat format : EnumOutputFormat.values()) {
            if (format.getValue() == value) {
                return format;
            }
        }
        return null;
    }

    /**
     * Convert {@code OutputFormat} value into {@code Transformer} equivalent
     * value.
     *
     * @param outputFormat Value to convert.
     * @return Converted value.
     */
    public static String convertToTransformer(EnumOutputFormat outputFormat) {
        switch (outputFormat) {
            case RDFXML:
            case APPLICATION_RDF_XML:
                return Transformer.RDFXML;

            case TURTLE:
            case TEXT_TURTLE:
                return Transformer.TURTLE;

            case JSONLD:
            case APPLICATION_LD_JSON:
                return Transformer.JSON;

            case TRIG:
            case APPLICATION_TRIG:
                return Transformer.TRIG;

            default:
                throw new InvalidParameterException("Output format " + outputFormat + " is unknow.");
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

}
