package fr.inria.corese.core.load;

import fr.inria.corese.kgram.api.core.Node;

/**
 * Interface that needs to be implemented when loading serialization(mainly 
 * for adding triples) (ex. json-ld, RDFa.) to corese graph
 * 
 * @author Fuqi Song wimmics inria i3s
 */
public interface ILoadSerialization {

    public static final int LITERAL = 10;
    public static final int NON_LITERAL = 20;

    /**
     * Add triples from parser to corese graph
     * 
     * @param subj subject
     * @param pred predicate
     * @param obj object
     * @param lang language tag
     * @param type data type
     * @param literalType literal type (LITERAL | NON_LITERAL)
     * @param source
     */
    public void addTriple(String subj, String pred, String obj, String lang, String type, int literalType, Node source);
}
