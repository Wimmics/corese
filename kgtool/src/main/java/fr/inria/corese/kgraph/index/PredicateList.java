package fr.inria.corese.kgraph.index;

import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * List of predicates of a Node
 * List of position of node in edge list of each Pi
 * (P1, .. Pn) (I1, .. In)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class PredicateList extends ArrayList<Node> {
    boolean isPosition = false;
    
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

    int getPosition(int n) {
        if (isPosition){
            return positionList.get(n);
        }
        return -1;
    }

    void add(Node node, Node predicate, int n) {
        add(predicate);
        if (isPosition) {
            if (node.getDatatypeValue().isNumber()){
                // for numbers we need to iterate all nodes with same value
                // not just this node, hence we will compute the index of the 
                // first node with same value 
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
