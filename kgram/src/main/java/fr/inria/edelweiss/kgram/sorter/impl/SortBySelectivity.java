package fr.inria.edelweiss.kgram.sorter.impl;

import fr.inria.edelweiss.kgram.sorter.core.BPGraph;
import fr.inria.edelweiss.kgram.sorter.core.BPGNode;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.BPGEdge;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation for sorting the triple pattern based on selectivity
 *
 * TODO: depth-first search and breadth-first search using edge weight
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class SortBySelectivity implements ISort {

    private final List<BPGNode> visited, notVisited;
    private BPGraph g = null;
    private final boolean SORT_MODE_BY_EDGE = true;

    public SortBySelectivity() {
        this.visited = new ArrayList<BPGNode>();
        this.notVisited = new ArrayList<BPGNode>();
    }

    @Override
    public List<BPGNode> sort(BPGraph graph) {
        this.g = graph;
        //EDGE GRAPH
        List<BPGNode> sortableNodes = g.getAllNodes(EDGE);
        sortableNodes.addAll(g.getAllNodes(GRAPH));
        notVisited.addAll(sortableNodes);

        //each loop is a sub graph
        while (!notVisited.isEmpty()) {
            BPGNode first = findFirst(notVisited);
            route(first);
        }
        return visited;
    }

    //search in one connected graph, depth-first
    private void route(BPGNode previous) {
        if (previous == null) {
            return;
        }

        //1 get all the nodes linked to this node
        List<BPGNode> children;
        if(this.SORT_MODE_BY_EDGE){
            children = this.getChildrenByEdge(previous);
        }else{
            children = this.getChildren(previous);
        }

        //2 while its chidlren (for one node) is not empty, continue search
        BPGNode next = null;
        while (!children.isEmpty() && ((next = findNext(children)) != null)) {
            route(next);
        }
    }

    //Find the bpg node with the smallest selectivity as starting routing point
    private BPGNode findFirst(List<BPGNode> lNodes) {
        if (lNodes == null || lNodes.isEmpty()) {
            return null;
        }

        BPGNode minNode = lNodes.get(0);
        double min = minNode.getSelectivity();

        for (BPGNode node : lNodes) {
            double sel = node.getSelectivity();
            if (sel != IEstimate.NULL_SEL && sel < min) {
                min = sel;
                minNode = node;
            }
        }
        this.queue(minNode);
        return minNode;
    }

    //find next node to be routed
    private BPGNode findNext(List<BPGNode> lNodes) {
//        //1 remvoe visited nodes
        for (BPGNode node : visited) {
            if (lNodes.contains(node)) {
                lNodes.remove(node);
            }
        }

        // if no nodes left, return null
        if (lNodes.isEmpty()) {
            return null;
        }

        //becaue the list of nodes has been sorted, so just return the toppest one
        BPGNode next = lNodes.get(0);
//        double min = minNode.getSelectivity();
//
//        for (BPGNode node : lNodes) {
//            double sel = node.getSelectivity();
//            if (sel != IEstimate.NULL_SEL && sel < min) {
//                min = sel;
//                minNode = node;
//            }
//        }

        //3 add to list
        this.queue(next);

        return next;
    }

    //get all the nodes linked to this node and sort by SELECTIVITY of node
    private List<BPGNode> getChildren(BPGNode n) {
        // get the linked nodes to a node and sort it (define the sequence of searching)
        List<BPGNode> lNodes = this.g.getLinkedNodes(n),
                children = new ArrayList<BPGNode>();
        //1 sort the list by selectivity of nodes
        children.addAll(lNodes);
        Collections.sort(children, new Comparator<BPGNode>() {
            @Override
            public int compare(BPGNode o1, BPGNode o2) {
                return Double.valueOf(o1.getSelectivity()).compareTo(Double.valueOf(o2.getSelectivity()));
            }
        });

        return children;
    }

    //get all the nodes linked to this node and sort by WEIGHTS * SELECTIVTY of nodes
    private List<BPGNode> getChildrenByEdge(BPGNode n) {
        List<BPGNode> children = new ArrayList<BPGNode>();

        List<BPGEdge> lEdges = this.g.getEdges(n);
        final Map<BPGNode, Double> weights = new HashMap<BPGNode, Double>();

        for (BPGEdge e : lEdges) {
            children.add(e.get(n));
            weights.put(e.get(n), e.getWeight());
        }

        Collections.sort(children, new Comparator<BPGNode>() {
            @Override
            public int compare(BPGNode n1, BPGNode n2) {
                double d1 = n1.getSelectivity() * weights.get(n1);
                double d2 = n2.getSelectivity() * weights.get(n2);
                return Double.valueOf(d1).compareTo(Double.valueOf(d2));
            }
        });

        return children;
    }

    private void queue(BPGNode minNode) {
        //3.0 check VALUES, if linked to this node, put it before
        this.addValues(minNode);

        //3 add found node to list
        visited.add(minNode);
        notVisited.remove(minNode);

        //4 check (and add) filter
        this.addFilters();
    }

    //for a filter, if all linked vairables have been visited, 
    //then add this filter just after these triple patterns
    private void addFilters() {
        List<BPGNode> filters = this.g.getAllNodes(FILTER);
        //others.addAll(g.getNodeList(VALUES));

        for (BPGNode f : filters) {
            //1 get the list of edges that filter (values, etc..) f links to
            List linkedNodes = this.g.getLinkedNodes(f);
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
    private void addValues(BPGNode min) {
        List<BPGNode> values = this.g.getAllNodes(VALUES);

        for (BPGNode v : values) {
            if (visited.contains(v)) {
                continue;
            }

            //1 get the list of edges that VALUES v links to
            List linkedNodes = this.g.getLinkedNodes(v);

            //2 if the VALUES use this (first) triple pattern, then add it to
            //visited list, before all linked nodes
            if (linkedNodes.contains(min)) {
                visited.add(v);
            }
        }
    }

    @Override
    public void rewrite(Exp exp, List<BPGNode> nodes) {
        //the expression
        if (exp.size() == nodes.size()) {
            for (int i = 0; i < nodes.size(); i++) {
                BPGNode node = nodes.get(i);
                exp.set(i, node.getExp());
            }
        } else {
            //todo
        }
    }
}
