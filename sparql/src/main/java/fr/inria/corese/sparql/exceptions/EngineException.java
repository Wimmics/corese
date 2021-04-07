package fr.inria.corese.sparql.exceptions;

import fr.inria.corese.kgram.api.query.ASTQ;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.URLServer;

/**
 * This class gathers all the exceptions that can be thrown in Corese
 *
 * @author Virginie Bottollier
 */
public class EngineException extends SparqlException {
    
    private String path;
    private URLServer url;
    private ASTQuery ast;
    private Object object;

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    protected EngineException() {
        super();
    }

    public EngineException(Exception e) {
        super(e);
    }
    
    public EngineException(Exception e, URLServer url) {
        super(e);
        setURL(url);
    }
    
    public EngineException(Exception e, String m) {
        super(e, m);
    }
    
    public EngineException(Error e) {
        super(e);
    }

    public EngineException(String message) {
        super(message);
    }

    public static EngineException cast(SparqlException e) {
        if (e instanceof EngineException) {
            return (EngineException) e;
        }
        return new EngineException(e);
    }
    
    public Exception getException() {
        if (getCause() instanceof Exception) {
            return (Exception) getCause();
        }
        return null;
    }

    public boolean isSafetyException() {
        return false;
    }

    public SafetyException getSafetyException() {
        return null;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    public IDatatype getDatatypeValue() {
        return null;
    }

    /**
     * @return the url
     */
    public URLServer getURL() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public EngineException setURL(URLServer url) {
        this.url = url;
        return this;
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public EngineException setObject(Object object) {
        this.object = object;
        return this;
    }

    /**
     * @return the ast
     */
    public ASTQuery getAST() {
        return ast;
    }

    /**
     * @param ast the ast to set
     */
    public EngineException setAST(ASTQuery ast) {
        this.ast = ast;
        return this;
    }

}
