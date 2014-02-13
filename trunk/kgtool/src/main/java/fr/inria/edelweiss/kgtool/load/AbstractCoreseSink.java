package fr.inria.edelweiss.kgtool.load;

import org.semarglproject.rdf.ParseException;
import org.semarglproject.sink.TripleSink;

/**
 * Implements the interface TripleSink (from semargl) in order to add the
 * triples (that are parsed by parser of semargl) to the graph of corese system
 *
 * @author Fuqi Song, wimmics inria i3s
 * @date Jan 2014 new
 */
public abstract class AbstractCoreseSink implements TripleSink {

    //define the type of literals
    public static final int NON_LITERAL = 11;
    public static final int PLAIN_LITERAL = 12;
    public static final int TYPED_LITERAL = 13;


    @Override
    public void addNonLiteral(String subject, String predicate, String object) {
        addTriple(subject, predicate, object, null, null, NON_LITERAL);
    }

    @Override
    public void addPlainLiteral(String subject, String predicate, String content, String lang) {
        addTriple(subject, predicate, content, lang, null, PLAIN_LITERAL);
    }

    @Override
    public void addTypedLiteral(String subject, String predicate, String content, String type) {
        addTriple(subject, predicate, content, null, type, TYPED_LITERAL);
    }

    /**
     * Callback method for handling triples.
     *
     * @param subj triple's subject
     * @param pred triple's predicate
     * @param obj triple's object
     * @param lang language
     * @param type data type
     * @param literalType type of literal
     */
    protected abstract void addTriple(String subj, String pred, String obj, String lang, String type, int literalType);

    @Override
    public void setBaseUri(String string) {
        //nothing
    }

    @Override
    public void startStream() throws ParseException {
        //nothing
    }

    @Override
    public void endStream() throws ParseException {
        //nothing
    }

    @Override
    public boolean setProperty(String string, Object o) {
       return false;
    }

}
