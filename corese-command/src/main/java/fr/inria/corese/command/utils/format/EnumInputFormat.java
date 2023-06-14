package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.api.Loader;

/**
 * Enumeration of parsable RDF serialization formats.
 */
public enum EnumInputFormat {
    RDFXML(1, "rdfxml"),
    TURTLE(2, "turtle"),
    TRIG(3, "trig"),
    JSONLD(4, "jsonld"),
    N3(5, "n3"),
    NTRIPLES(6, "ntriples"),
    NQUADS(7, "nquads"),
    RDFA(8, "rdfa"),
    APPLICATION_RDF_XML(1, "application/rdf+xml"),
    TEXT_TURTLE(2, "text/turtle"),
    APPLICATION_TRIG(3, "application/trig"),
    APPLICATION_LD_JSON(4, "application/ld+json"),
    TEXT_N3(5, "text/n3"),
    APPLICATION_N_TRIPLES(6, "application/n-triples"),
    APPLICATION_N_QUADS(7, "application/n-quads"),
    APPLICATION_XHTML_XML(8, "application/xhtml+xml");

    private final int value;
    private final String name;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     * @param name  The name of the enum.
     */
    EnumInputFormat(int value, String name) {
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
     * Get the enum from its value.
     * 
     * @param value The value of the enum.
     * @return The enum.
     */
    public static EnumInputFormat fromValue(int value) {
        for (EnumInputFormat format : EnumInputFormat.values()) {
            if (format.getValue() == value) {
                return format;
            }
        }
        return null;
    }

    /**
     * Get the enum from its name.
     * 
     * @param name The name of the enum.
     * @return The enum.
     */
    public static EnumInputFormat fromName(String name) {
        for (EnumInputFormat format : EnumInputFormat.values()) {
            if (format.getName().equals(name)) {
                return format;
            }
        }
        return null;
    }

    /**
     * Convert {@code Loader} format value into {@code InputFormat} equivalent
     * 
     * @param loaderFormat The Loader format.
     * @return The corresponding InputFormat.
     */
    public static EnumInputFormat fromLoaderValue(int loaderFormat) {
        switch (loaderFormat) {
            case Loader.RDFXML_FORMAT:
            case Loader.XML_FORMAT:
                return EnumInputFormat.RDFXML;

            case Loader.TURTLE_FORMAT:
                return EnumInputFormat.TURTLE;

            case Loader.NT_FORMAT:
                return EnumInputFormat.NTRIPLES;

            case Loader.JSONLD_FORMAT:
            case Loader.JSON_FORMAT:
                return EnumInputFormat.JSONLD;

            case Loader.TRIG_FORMAT:
                return EnumInputFormat.TRIG;

            case Loader.NQUADS_FORMAT:
                return EnumInputFormat.NQUADS;

            case Loader.RDFA_FORMAT:
                return EnumInputFormat.RDFA;

            default:
                throw new InvalidParameterException("Loader format " + loaderFormat + " is unknown.");

        }
    }

    /**
     * Convert {@code OutputFormat} value to the corresponding {@code Loader}
     * format.
     * 
     * @param format The Loader format.
     * @return The corresponding Loader format.
     */
    public static int toLoaderValue(EnumInputFormat format) {
        switch (format) {
            case RDFXML:
            case APPLICATION_RDF_XML:
                return Loader.RDFXML_FORMAT;

            case TURTLE:
            case TEXT_TURTLE:
                return Loader.TURTLE_FORMAT;

            case NTRIPLES:
            case APPLICATION_N_TRIPLES:
                return Loader.NT_FORMAT;

            case JSONLD:
            case APPLICATION_LD_JSON:
                return Loader.JSONLD_FORMAT;

            case TRIG:
            case APPLICATION_TRIG:
                return Loader.TRIG_FORMAT;

            case NQUADS:
            case APPLICATION_N_QUADS:
                return Loader.NQUADS_FORMAT;

            case RDFA:
            case APPLICATION_XHTML_XML:
                return Loader.RDFA_FORMAT;

            default:
                throw new InvalidParameterException("Input format " + format + " is unknown.");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
