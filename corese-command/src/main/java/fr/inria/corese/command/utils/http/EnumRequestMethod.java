package fr.inria.corese.command.utils.http;

/**
 * Enumeration of SPARQL request methods.
 */
public enum EnumRequestMethod {
    GET("get"),
    POST_URLENCODED("post-urlencoded"),
    POST_DIRECT("post-direct");

    private final String name;

    /**
     * Constructor.
     *
     * @param name The name of the SPARQL request method.
     */
    private EnumRequestMethod(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
