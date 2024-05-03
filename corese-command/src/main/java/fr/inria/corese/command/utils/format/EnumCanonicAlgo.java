package fr.inria.corese.command.utils.format;

/**
 * Enumeration of exportable RDF serialization formats.
 */
public enum EnumCanonicAlgo {
    RDFC10(1, "rdfc-1.0", "nq"),
    RDFC10SHA256(1, "rdfc-1.0-sha256", "nq"),

    RDFC10SHA384(2, "rdfc-1.0-sha384", "nq");

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
    private EnumCanonicAlgo(int value, String name, String extention) {
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
     * Get the name of the canonic algorithm.
     * 
     * @return The name of the canonic algorithm.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the extension of the file format associated with the canonic algorithm.
     * 
     * @return The extension of the file format associated with the canonic
     *         algorithm.
     */
    public String getExtention() {
        return this.extention;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
