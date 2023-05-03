package fr.inria.corese.command.utils.format;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum ResultFormat {
    RDFXML(1),
    TURTLE(2),
    TRIG(3),
    JSONLD(4),
    RESULT_XML(11),
    RESULT_RDF(12),
    RESULT_JSON(13),
    RESULT_CSV(14),
    RESULT_TSV(15),
    RESULT_HTML(20);

    private final int value;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     */
    ResultFormat(int value) {
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
    public static ResultFormat fromValue(int value) {
        for (ResultFormat format : ResultFormat.values()) {
            if (format.getValue() == value) {
                return format;
            }
        }
        return null;
    }

    /**
     * Convert to the corresponding OutputFormat.
     * 
     * @return The corresponding OutputFormat.
     */
    public OutputFormat convertToOutputFormat() {
        switch (this) {
            case RDFXML:
                return OutputFormat.RDFXML;
            case TURTLE:
                return OutputFormat.TURTLE;
            case TRIG:
                return OutputFormat.TRIG;
            case JSONLD:
                return OutputFormat.JSONLD;
            default:
                return null;
        }
    }
}
