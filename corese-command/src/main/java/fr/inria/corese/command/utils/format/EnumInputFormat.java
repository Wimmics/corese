package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.api.Loader;

/**
 * Enumeration of parsable RDF serialization formats.
 */
public enum EnumInputFormat {

    RDFXML(1, "rdfxml"),
    RDF(1, "rdf"),
    APPLICATION_RDF_XML(1, "application/rdf+xml"),

    TURTLE(2, "turtle"),
    TTL(2, "ttl"),
    TEXT_TURTLE(2, "text/turtle"),

    TRIG(3, "trig"),
    APPLICATION_TRIG(3, "application/trig"),

    JSONLD(4, "jsonld"),
    APPLICATION_LD_JSON(4, "application/ld+json"),

    NTRIPLES(6, "ntriples"),
    NT(6, "nt"),
    APPLICATION_NTRIPLES(6, "application/n-triples"),

    NQUADS(7, "nquads"),
    NQ(7, "nq"),
    APPLICATION_NQUADS(7, "application/n-quads"),

    RDFA(8, "rdfa"),
    HTML(8, "html"),
    APPLICATION_XHTML_XML(8, "application/xhtml+xml");

    private final int value;
    private final String name;

    /**
     * Constructor.
     * 
     * @param value     The value of the enum.
     * @param name      The name of the enum.
     * @param extention The extension of the format.
     */
    private EnumInputFormat(int value, String name) {
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
     * Convert {@code Loader} format value into {@code InputFormat} equivalent
     * 
     * @param loaderFormat The Loader format.
     * @return The corresponding InputFormat.
     */
    public static EnumInputFormat fromLoaderValue(int loaderFormat) {
        switch (loaderFormat) {
            case Loader.RDFXML_FORMAT:
                return EnumInputFormat.RDFXML;

            case Loader.TURTLE_FORMAT:
                return EnumInputFormat.TURTLE;

            case Loader.TRIG_FORMAT:
                return EnumInputFormat.TRIG;

            case Loader.JSONLD_FORMAT:
                return EnumInputFormat.JSONLD;

            case Loader.NT_FORMAT:
                return EnumInputFormat.NTRIPLES;

            case Loader.NQUADS_FORMAT:
                return EnumInputFormat.NQUADS;

            case Loader.RDFA_FORMAT:
                return EnumInputFormat.RDFA;

            default:
                throw new InvalidParameterException(
                        "Impossible to determine the input format, please specify it with the -f or -if or --input-format option.");
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
            case RDF:
            case APPLICATION_RDF_XML:
                return Loader.RDFXML_FORMAT;

            case TURTLE:
            case TTL:
            case TEXT_TURTLE:
                return Loader.TURTLE_FORMAT;

            case TRIG:
            case APPLICATION_TRIG:
                return Loader.TRIG_FORMAT;

            case JSONLD:
            case APPLICATION_LD_JSON:
                return Loader.JSONLD_FORMAT;

            case NTRIPLES:
            case NT:
            case APPLICATION_NTRIPLES:
                return Loader.NT_FORMAT;

            case NQUADS:
            case NQ:
            case APPLICATION_NQUADS:
                return Loader.NQUADS_FORMAT;

            case RDFA:
            case HTML:
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
