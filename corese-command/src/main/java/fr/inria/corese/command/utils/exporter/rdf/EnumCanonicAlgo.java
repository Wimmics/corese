package fr.inria.corese.command.utils.exporter.rdf;

import fr.inria.corese.sparql.api.ResultFormatDef;

/**
 * Enumeration of canonic algorithms.
 */
public enum EnumCanonicAlgo {

    // Rdfc-1.0-sha256
    RDFC_1_0("rdfc-1.0", ResultFormatDef.RDFC10_FORMAT, "nq"),
    RDFC_1_0_SHA256("rdfc-1.0-sha256", ResultFormatDef.RDFC10_FORMAT, "nq"),

    // Rdfc-1.0-sha384
    RDFC_1_0_SHA384("rdfc-1.0-sha384", ResultFormatDef.RDFC10_SHA384_FORMAT, "nq");

    private final String name;
    private final int coreseCodeFormat;
    private final String extention;

    /**
     * Constructor.
     * 
     * @param name             The name of the canonic algorithm.
     * @param coreseCodeFormat The Corese code of the format.
     * @param extention        The extension of the file format associated with the
     *                         canonic algorithm.
     */
    private EnumCanonicAlgo(String name, int coreseCodeFormat, String extention) {
        this.name = name;
        this.coreseCodeFormat = coreseCodeFormat;
        this.extention = extention;
    }

    /**
     * Get the Corese code of the format.
     * 
     * @return The Corese code of the format.
     */
    public int getCoreseCodeFormat() {
        return this.coreseCodeFormat;
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
