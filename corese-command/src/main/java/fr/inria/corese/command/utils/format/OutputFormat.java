package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.transform.Transformer;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum OutputFormat {
    RDFXML(1),
    TURTLE(2),
    TRIG(3),
    JSONLD(4);

    private final int value;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     */
    OutputFormat(int value) {
        this.value = value;
    }

    /**
     * Get the value of the enum.
     * 
     * @return The value of the enum.
     */
    public int getValue() {
        return value;
    }

    /**
     * Get the enum from its value.
     * 
     * @param value The value of the enum.
     * @return The enum.
     */
    public static OutputFormat fromValue(int value) {
        for (OutputFormat format : OutputFormat.values()) {
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
    public static String convertToTransformer(OutputFormat outputFormat) {
        switch (outputFormat) {
            case RDFXML:
                return Transformer.RDFXML;

            case TURTLE:
                return Transformer.TURTLE;

            case JSONLD:
                return Transformer.JSON;

            case TRIG:
                return Transformer.TRIG;

            default:
                throw new InvalidParameterException("Output format " + outputFormat + " is unknow.");
        }
    }

}
