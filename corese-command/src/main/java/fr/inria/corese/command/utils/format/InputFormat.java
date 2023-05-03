package fr.inria.corese.command.utils.format;

import java.security.InvalidParameterException;

import fr.inria.corese.core.api.Loader;

/**
 * Enumeration of parsable RDF serialization formats.
 */
public enum InputFormat {
    RDFXML(1),
    TURTLE(2),
    TRIG(3),
    JSONLD(4),
    N3(5),
    NTRIPLES(6),
    NQUADS(7),
    RDFA(8);

    private final int value;

    /**
     * Constructor.
     * 
     * @param value The value of the enum.
     */
    InputFormat(int value) {
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
    public static InputFormat fromValue(int value) {
        for (InputFormat format : InputFormat.values()) {
            if (format.getValue() == value) {
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
    public static InputFormat fromLoaderValue(int loaderFormat) {
        switch (loaderFormat) {
            case Loader.RDFXML_FORMAT:
                return InputFormat.RDFXML;

            case Loader.TURTLE_FORMAT:
                return InputFormat.TURTLE;

            case Loader.NT_FORMAT:
                return InputFormat.NTRIPLES;

            case Loader.JSONLD_FORMAT:
                return InputFormat.JSONLD;

            case Loader.TRIG_FORMAT:
                return InputFormat.TRIG;

            case Loader.NQUADS_FORMAT:
                return InputFormat.NQUADS;

            case Loader.RDFA_FORMAT:
                return InputFormat.RDFA;

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
    public static int toLoaderValue(InputFormat format) {
        switch (format) {
            case RDFXML:
                return Loader.RDFXML_FORMAT;

            case TURTLE:
                return Loader.TURTLE_FORMAT;

            case NTRIPLES:
                return Loader.NT_FORMAT;

            case JSONLD:
                return Loader.JSONLD_FORMAT;

            case TRIG:
                return Loader.TRIG_FORMAT;

            case NQUADS:
                return Loader.NQUADS_FORMAT;

            case RDFA:
                return Loader.RDFA_FORMAT;

            default:
                throw new InvalidParameterException("Input format " + format + " is unknown.");
        }
    }
}
