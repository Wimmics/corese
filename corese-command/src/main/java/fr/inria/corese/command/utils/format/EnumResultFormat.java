package fr.inria.corese.command.utils.format;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumResultFormat {
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
    EnumResultFormat(int value) {
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
    public static EnumResultFormat fromValue(int value) {
        for (EnumResultFormat format : EnumResultFormat.values()) {
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
