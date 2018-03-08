package fr.inria.corese.kgram.sorter.impl.qpv1;

import static fr.inria.corese.kgram.api.core.ExpType.BIND;
import fr.inria.corese.kgram.sorter.core.QPGraph;
import fr.inria.corese.kgram.sorter.core.QPGNode;
import fr.inria.corese.kgram.sorter.core.ISort;
import static fr.inria.corese.kgram.api.core.ExpType.EDGE;
import static fr.inria.corese.kgram.api.core.ExpType.FILTER;
import static fr.inria.corese.kgram.api.core.ExpType.GRAPH;
import static fr.inria.corese.kgram.api.core.ExpType.VALUES;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.sorter.core.QPGEdge;
import fr.inria.corese.kgram.sorter.core.IEstimate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation for sorting the triple pattern depth-first & best greedy
 * algorithm
 *
 * 1 start with the one with least cost, if there are more than 2 with same cost
 * then choose the one with more edges, if still the same, then just pick one
 *
 * 2 search next node by considering all the adjacent nodes of the visited nodes
 *
 * 3 recursively search until all nodes have been visited
 *
 * 4 every time one node is added to visited, check FILTER & VALUES
 * ++ 4.1 now also check BIND 20Jan2015
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 Oct. 2014
 */
public class DepthFirstBestSearch implements ISort {

    private final List<QPGNode> visited, notVisited, binds;
    private QPGraph g = null;

    public DepthFirstBestSearch() {
        this.visited = new ArrayList<QPGNode>();
        this.notVisited = new ArrayList<QPGNode>();
        this.binds = new ArrayList<QPGNode>();
    }

    @Override
    public List<QPGNode> sort(QPGraph graph) {
        this.g = graph;
        //EDGE GRAPH
        List<QPGNode> sortableNodes = g.getAllNodes(EDGE);
        sortableNodes.addAll(g.getAllNodes(GRAPH));
        notVisited.addAll(sortableNodes);
        binds.addAll(g.getAllNodes(BIND));

        //each loop is a sub graph
        while (!notVisited.isEmpty()) {
            Map<QPGNode, Double> nextNodesPool = new LinkedHashMap<QPGNode, Double>();
            QPGNode first = findFirst(notVisited, nextNodesPool);
            route(first, nextNodesPool);
        }

        //for other left nodes, ex. filter/values/bind not related to any vairables, optional
        //just add them at the end of visited list
        for (QPGNode n : g.getAllNodes()) {
            if (!visited.contains(n)) {
                visited.add(n);
            }
        }
        return visited;
    }

    //search in one connected graph, depth-first
    private void route(QPGNode previous, Map<QPGNode, Double> nextNodesPool) {
        if (previous == null) return;

        // while there is still node not visited in the pool
        QPGNode next;
        while ((next = findNext(nextNodesPool)) != null) {
            route(next, nextNodesPool);
        }
    }

    //Find the bpg node with the smallest selectivity as starting routing point
    private QPGNode findFirst(List<QPGNode> lNodes, Map<QPGNode, Double> pool) {
        if (lNodes == null || lNodes.isEmpty()) return null;

        QPGNode minNode = lNodes.get(0);
        for (QPGNode node : lNodes) {
            double cost = node.getCost();
            if (cost == IEstimate.NA_COST) continue;
            //number of linked edges
            int es1 = this.g.getEdges(node) == null ? 0 : this.g.getEdges(node).size();
            int es2 = this.g.getEdges(minNode) == null ? 0 : this.g.getEdges(minNode).size();

            //if two nodes with same cost, on choose the one with more edges
            if ((cost < minNode.getCost())
                    || (cost == minNode.getCost() && es1 > es2)) {
                minNode = node;
            }
        }
        this.queue(minNode, pool);
        return minNode;
    }

    //find next node to be routed
    private QPGNode findNext(Map<QPGNode, Double> pool) {
        // 1. if no nodes left, return null
        if (pool.isEmpty()) return null;

        // 2. find the node with smallest cost
        QPGNode next = null;
        double cost = Double.MAX_VALUE;
        for (Map.Entry<QPGNode, Double> entry : pool.entrySet()) {
            QPGNode qPGNode = entry.getKey();
            Double double1 = entry.getValue();
            if (double1 < cost) {
                cost = double1;
                next = qPGNode;
            }
        }

        //3 add to list
        this.queue(next, pool);

        return next;
    }

    private void queue(QPGNode minNode, Map<QPGNode, Double> pool) {
        //1.0 check VALUES, if linked to this node, put it before
        this.addValues(minNode);

        //2 add found node to list
        visited.add(minNode);
        //2.1 remove from relevant list
        notVisited.remove(minNode);
        pool.remove(minNode);
        
        //2.5 add binds
        addBinds(minNode);
        //3 check (and add) filter
        this.addFilters();

        //4. find adjacent nodes
        findAdjacentNodes(minNode, pool);
    }

    //for a filter, if all linked vairables have been visited, 
    //then add this filter just after these triple patterns
    private void addFilters() {
        List<QPGNode> filters = this.g.getAllNodes(FILTER);
        //others.addAll(g.getNodeList(VALUES));

        for (QPGNode f : filters) {
            //if there are no edges left linking to the filter (values, etc...)
            //add this filter to the sequence, just after the triples patterns
            if (!visited.contains(f) && !intersect(this.g.getLinkedNodes(f), notVisited, binds)) {
                visited.add(f);
            }
        }
    }

    //for VALUES, put it just before the vairables being used 
    private void addValues(QPGNode min) {
        List<QPGNode> values = this.g.getAllNodes(VALUES);

        for (QPGNode v : values) {
            //if the VALUES use this (first) triple pattern, then add it to
            //visited list, before all linked nodes
            if (!visited.contains(v) && g.getLinkedNodes(v).contains(min)) {
                visited.add(v);
            }
        }
    }

    //Find the adjacent nodes linked to a given node and add them to a pool
    private void findAdjacentNodes(QPGNode minNode, Map<QPGNode, Double> pool) {
        //add adjacent unvisted nodes to a pool 
        List<QPGEdge> lEdges = this.g.getEdges(minNode, QPGEdge.BI_DIRECT);
        //final Map<QPGNode, Double> weights = new HashMap<QPGNode, Double>();

        for (QPGEdge e : lEdges) {
            //pool.add(e.get(minNode));
            // get the linked node
            QPGNode linkedNode = e.get(minNode);
            //get the cost of the linked node
            double cost = e.getCost() * linkedNode.getCost();

            //check the cost between the linked node and existing visited node
            for (QPGNode vn : this.visited) {
                //if there is edge between vn and node
                QPGEdge linkedEdge = this.g.getEdge(minNode, vn);
                if (linkedEdge != null) {
                    double cost2 = linkedEdge.getCost() * vn.getCost();
                    if (cost2 < cost) {
                        cost = cost2;
                    }
                }
            }

            if (!visited.contains(linkedNode)) {
                pool.put(linkedNode, cost);
            }
        }
    }

    //Check BIND expressions
    private void addBinds(QPGNode min) {
        for (QPGNode q : g.getLinkedNodes(min)) {
            List<QPGNode> linkedBinds = g.getLinkedNodes(q, true, true);

            //for BIND, it also can have FILTERs and VALUES, and also it can depends on the
            //other BINDs expressions, so need to check recusively
            while (q.getType()== BIND && !visited.contains(q) && !intersect(linkedBinds, notVisited, binds)) {
                this.addValues(q);

                visited.add(q);
                binds.remove(q);
                addBinds(q);

                this.addFilters();
            }
        }
    }

    /**
     * Check if the first list has same nodes with the other two lists of nodes
     * @param list
     * @param notVisited
     * @param binds
     * @return 
     */
    private boolean intersect(List<QPGNode> list, List<QPGNode> notVisited, List<QPGNode> binds) {
        for (QPGNode q : list) {
            if (notVisited.contains(q) || binds.contains(q)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void rewrite(Exp exp, List<QPGNode> nodes, int start) {
        for (int i = 0; i < nodes.size(); i++) {
            QPGNode node = nodes.get(i);
            exp.set(start + i, node.getExp());
        }
    }
}
