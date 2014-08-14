package fr.inria.edelweiss.kgram.sorter.impl;

import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An implementation for sorting the triple pattern based on selectivity
 *
 * TODO: depth-first search and breadth-first search using edge weight
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class SortBySelectivity implements ISort {

    List<BPGNode> visited = new ArrayList<BPGNode>(),
            notVisited = new ArrayList<BPGNode>();

    @Override
    public List<BPGNode> sort(BPGraph g) {
        List<BPGNode> allEdgeNodes = g.getNodeList(EDGE);
        notVisited.addAll(allEdgeNodes);

        //each loop is a sub graph
        while (!notVisited.isEmpty()) {
            BPGNode minNode = findMinimum(notVisited, g);
            route(g, minNode);
        }
        return visited;
    }

    @Override
    public void rewrite(Exp exp, List<BPGNode> nodes) {
        //the expression
        if (exp.size() == nodes.size()) {
            for (int i = 0; i < nodes.size(); i++) {
                BPGNode node = nodes.get(i);
                exp.set(i, node.getExp());
            }
        }else{
            //todo
        }
    }

    //search in one connected graph
    private void route(BPGraph g, BPGNode n) {
        if (n == null) {
            return;
        }

        List<BPGNode> lNodes = g.getNodeList(n),
                notVisitedLoc = new ArrayList<BPGNode>();
        //1 sort the list by selectivity of nodes
        notVisitedLoc.addAll(lNodes);
        Collections.sort(notVisitedLoc, new Comparator<BPGNode>() {
            @Override
            public int compare(BPGNode o1, BPGNode o2) {
                return Double.valueOf(o1.getSelectivity()).compareTo(Double.valueOf(o2.getSelectivity()));
            }
        });

        //2 while the local not visited list (for one node) is not empty, continue search
        BPGNode min = null;
        while (!notVisitedLoc.isEmpty() && ((min = findMinimum(notVisitedLoc, g)) != null)) {
            route(g, min);
        }
    }

    //find node with minimum selectivity
    private BPGNode findMinimum(List<BPGNode> lNodes, BPGraph g) {
        //1 remvoe visited nodes
        for (BPGNode node : visited) {
            if (lNodes.contains(node)) {
                lNodes.remove(node);
            }
        }

        if (lNodes.isEmpty()) {
            return null;
        }

        //2 find the node with minimum selectivity in the list
        BPGNode minNode = lNodes.get(0);
        double min = minNode.getSelectivity();

        for (BPGNode node : lNodes) {
            double sel = node.getSelectivity();
            if (sel != IEstimate.NULL_SEL && sel < min) {
                min = sel;
                minNode = node;
            }
        }

        //3.0 check VALUES, if linked to this node, put it before
        this.addValues(g, minNode);

        //3 add found node to list
        visited.add(minNode);
        notVisited.remove(minNode);

        //4 check (and add) filter
        this.addFilters(g);
        return minNode;
    }

    //for a filter, if all linked vairables have been visited, 
    //then add this filter just after these triple patterns
    private void addFilters(BPGraph g) {
        List<BPGNode> filters = g.getNodeList(FILTER);
        //others.addAll(g.getNodeList(VALUES));

        for (BPGNode f : filters) {
            //1 get the list of edges that filter (values, etc..) f links to
            List linkedNodes = g.getNodeList(f);
            if (visited.contains(f)) {
                continue;
            }

            //2 check this list contains un visited node
            boolean flag = true;
            for (BPGNode unVistiedNode : notVisited) {
                if (linkedNodes.contains(unVistiedNode)) {
                    flag = false;
                    break;
                }
            }

            //3 if flag=true, means there are no edges left linking to the filter (values, etc...)
            //so add this filter to the sequence, just after the triples patterns
            if (flag) {
                visited.add(f);
            }
        }
    }

    //for VALUES, put it just before the vairables being used 
    private void addValues(BPGraph g, BPGNode min) {
        List<BPGNode> values = g.getNodeList(VALUES);

        for (BPGNode v : values) {
            if (visited.contains(v)) {
                continue;
            }

            //1 get the list of edges that VALUES v links to
            List linkedNodes = g.getNodeList(v);

            //2 if the VALUES use this (first) triple pattern, then add it to
            //visited list, before all linked nodes
            if (linkedNodes.contains(min)) {
                visited.add(v);
            }
        }
    }

}
