package fr.inria.corese.sparql.triple.parser;

import java.util.List;

/**
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class RDFList extends And {

    Atom first;
    List<Atom> list;

    
    RDFList(){       
    }
    
    RDFList(Atom f, List<Atom> l){
        first = f;
        list = l;
    }
    
    public static RDFList create() {
        return new RDFList();
    }

    @Override
    public boolean isRDFList() {
        return true;
    }

    public Atom head() {
        return first;
    }

    void setHead(Atom e) {
        first = e;
    }

    void setList(List<Atom> l) {
        list = l;
    }

    public List<Atom> getList() {
        return list;
    }

   
}
