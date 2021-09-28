package fr.inria.corese.kgram.core;

/**
 *
 * @author corby
 */
public class SparqlException extends Exception {
    private boolean stop = false;

    public SparqlException() {
    }

    public SparqlException(String message) {
        super(message);
    }

    public SparqlException(Exception e) {
        super(e);
    }
    
     public SparqlException(Exception e, String m) {
        super(m, e);
    }
    
    public SparqlException(Error e) {
        super(e);
    }
    
    // isStop true means stop query processing, perform aggregate etc. and return partial result
    // isStop false means this is an exception
    // see LDScriptException in sparql
 
    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

}
