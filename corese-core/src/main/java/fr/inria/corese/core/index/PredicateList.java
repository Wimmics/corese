package fr.inria.corese.core.index;

import static fr.inria.corese.core.index.EdgeManagerIndexer.ITERATE_SUBLIST;
import static fr.inria.corese.core.index.EdgeManagerIndexer.RECORD_END;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * List of predicates of a given Node in NodeManager
 * List of positions of node in edge list of each predicate pi
 * (p1, .. pn) (i1, .. in)
 * in the edge list of predicate(j) = (t1 .. tn)
 * index(node) = position(j)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class PredicateList {
    boolean isPosition = true;
    
    // list of position(i) of given node in the list of edge of predicate(i)
    private ArrayList<Node> predicateList;
    private ArrayList<Integer> positionList;
    private ArrayList<Integer> endList;
    private ArrayList<Cursor> cursorList;

    PredicateList() {
        init();
    }
    
    PredicateList(boolean b) {
        this();
        //isPosition = b;
    }
       
    PredicateList(List<Node> l) {
        this();
        getPredicateList().addAll(l);
        //isPosition = false;
    }

    PredicateList(boolean b, int n) {
        this();
        //isPosition = b;       
    }
    
    void init() {
        setPredicateList(new ArrayList<>());
        if (isPosition) {
            setPositionList(new ArrayList<>());
            setEndList(new ArrayList<>());
            if (EdgeManagerIndexer.ITERATE_SUBLIST) {
                setCursorList(new ArrayList<>());
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(getPositionList());
        return sb.toString();
    }
    
    void trim() {
        getPredicateList().trimToSize();
        getPositionList().trimToSize();
        getEndList().trimToSize();
        if (ITERATE_SUBLIST) {
            getCursorList().trimToSize();
        }
    }
    
    public int getPosition(Node predicate) {
        return findPosition(predicate);
    }
    
    public int size() {
        return getPredicateList().size();
    }
    
    Node getPredicate(int i) {
        return getPredicateList().get(i);
    }
    
    int findPosition(Node predicate){
        int i = findPosition(predicate, 0, size());
        if (i >= 0 && i < size()){
            if (predicate.getIndex() == getPredicate(i).getIndex()) {
                return getPosition(i);
            }
        }
        return -1;
    }
    
    public Cursor getCursor(Node predicate){
        int i = findPosition(predicate, 0, size());
        if (i >= 0 && i < size()){
            if (predicate.getIndex() == getPredicate(i).getIndex()) {
                return getCursor(i);
            }
        }
        return null;
    }
    
    Cursor getCursor(int n) {
        //return new Cursor(getPosition(n), endList.get(n));
        return getCursorList().get(n);
    }
    
    int getPosition(int n) {
        if (isPosition){
            return getPositionList().get(n);
        }
        return -1;
    }
    
    int getEnd(int n) {
        if (n < getEndList().size()) {
            return getEndList().get(n);
        }
        return -1;
    }
        
    int getPositionBasic(Node predicate) {
        int i = 0;
        for (Node p : getPredicateList()) {
            if (predicate == p) {
                return getPosition(i);
            }
            i++;
        }
        return -3;
    }
    
    
    
    int findPosition(Node predicate, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = getPredicate(mid).compare(predicate); 
            if (res >= 0) {
                return findPosition(predicate, first, mid);
            } else {
                return findPosition(predicate, mid + 1, last);
            }
        }
    }
    
    
    void add(Node node, Node predicate, int begin, int end) {
        getPredicateList().add(predicate);
        if (isPosition) {
            if (node.getDatatypeValue().isNumber()
                    || node.getDatatypeValue().isBoolean()) {
                // for numbers we need to iterate all nodes with same value
                // not just this node, hence we will perform a dichotomy
                getPositionList().add(-1);
            } else {
                getPositionList().add(begin);
                if (RECORD_END) {
                    getEndList().add(end);
                }
                if (ITERATE_SUBLIST) {
                    getCursorList().add(new Cursor(begin, end));
                }
            }
        }
    }

    public void clear() {
        getPredicateList().clear();
        if (isPosition) {
            getPositionList().clear();
            getEndList().clear();
        }
    }

    public ArrayList<Node> getPredicateList() {
        return predicateList;
    }

    public void setPredicateList(ArrayList<Node> predicateList) {
        this.predicateList = predicateList;
    }

    public ArrayList<Integer> getEndList() {
        return endList;
    }

    public void setEndList(ArrayList<Integer> endList) {
        this.endList = endList;
    }

    public ArrayList<Integer> getPositionList() {
        return positionList;
    }

    public void setPositionList(ArrayList<Integer> positionList) {
        this.positionList = positionList;
    }

    public ArrayList<Cursor> getCursorList() {
        return cursorList;
    }

    public void setCursorList(ArrayList<Cursor> cursorList) {
        this.cursorList = cursorList;
    }
    
    public class Cursor {
        private int begin;
        private int end;
        
        Cursor(int b, int e) {
            begin = b;
            end = e;
        }

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }
}
