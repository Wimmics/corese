package fr.inria.corese.command.utils.http;

/**
 * Enumeration of SPARQL request methods.
 */
public enum EnumRequestMethod {
    GET("GET"),
    POST_URLENCODED("POST-Encoded"),
    POST_DIRECT("POST-Direct");

    private final String name;
    private final boolean isPost;

    /**
     * Constructor.
     * 
     * @param name The name of the request method.
     */
    EnumRequestMethod(String name) {
        this.name = name;
        this.isPost = name.contains("POST");
    }

    /**
     * Get the name of the request method.
     * 
     * @return The name of the request method.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Check if the request method is POST or GET.
     * 
     * @return True if the request method is POST, false if it is GET.
     */
    public boolean isPost() {
        return this.isPost;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
