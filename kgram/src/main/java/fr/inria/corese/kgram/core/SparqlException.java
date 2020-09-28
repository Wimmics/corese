package fr.inria.corese.kgram.core;

/**
 *
 * @author corby
 */
public class SparqlException extends Exception {

    public SparqlException() {
    }

    public SparqlException(String message) {
        super(message);
    }

    public SparqlException(Exception e) {
        super(e);
    }

}
