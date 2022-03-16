package fr.inria.corese.core.index;

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
public class PredicateList 
        // list of predicate Node of a given node (in NodeManager)
        extends ArrayList<Node> {
    boolean isPosition = false;
    
    // list of position(i) of given node in the list of edge of predicate(i) 
    ArrayList<Integer> positionList;

    PredicateList() {
        init();
    }
    
    PredicateList(boolean b) {
        isPosition = b;
        init();
    }
    
    void init() {
        if (isPosition) {
            positionList = new ArrayList<>();
        }
    }
    
    PredicateList(List<Node> l) {
        addAll(l);
        isPosition = false;
    }

    PredicateList(boolean b, int n) {
        super(n);
        isPosition = b;
        if (isPosition) {
            positionList = new ArrayList<>(n);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(positionList);
        return sb.toString();
    }

    int getPosition(int n) {
        if (isPosition){
            return positionList.get(n);
        }
        return -1;
    }
    
    int getPosition(Node predicate) {
        return findPosition(predicate);
    }

    
    int getPositionBasic(Node predicate) {
        int i = 0;
        for (Node p : this) {
            if (predicate == p) {
                return getPosition(i);
            }
            i++;
        }
        return -3;
    }
    
    int findPosition(Node predicate){
        int i = findPosition(predicate, 0, size());
        if (i >= 0 && i < size()){
            if (predicate.getIndex() == get(i).getIndex()) {
                return getPosition(i);
            }
        }
        return -1;
    }
    
    int findPosition(Node predicate, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = get(mid).compare(predicate); 
            if (res >= 0) {
                return findPosition(predicate, first, mid);
            } else {
                return findPosition(predicate, mid + 1, last);
            }
        }
    }
    
    
    void add(Node node, Node predicate, int n) {
        add(predicate);
        if (isPosition) {
            if (node.getDatatypeValue().isNumber() ||
                node.getDatatypeValue().isBoolean()){
                // for numbers we need to iterate all nodes with same value
                // not just this node, hence we will perform a dichotomy
                positionList.add(-1);
            }
            else {
                positionList.add(n);
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        if (isPosition) {
            positionList.clear();
        }
    }
}
