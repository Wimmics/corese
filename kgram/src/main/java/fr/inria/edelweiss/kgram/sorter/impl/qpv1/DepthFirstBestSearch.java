package fr.inria.edelweiss.kgram.sorter.impl.qpv1;

import fr.inria.edelweiss.kgram.sorter.core.QPGraph;
import fr.inria.edelweiss.kgram.sorter.core.QPGNode;
import fr.inria.edelweiss.kgram.sorter.core.ISort;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import static fr.inria.edelweiss.kgram.api.core.ExpType.FILTER;
import static fr.inria.edelweiss.kgram.api.core.ExpType.GRAPH;
import static fr.inria.edelweiss.kgram.api.core.ExpType.VALUES;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.sorter.core.QPGEdge;
import fr.inria.edelweiss.kgram.sorter.core.IEstimate;
import java.util.ArrayList;
import java.util.HashMap;
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
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 23 Oct. 2014
 */
public class DepthFirstBestSearch implements ISort {

    private final List<QPGNode> visited, notVisited;
    private QPGraph g = null;

    public DepthFirstBestSearch() {
        this.visited = new ArrayList<QPGNode>();
        this.notVisited = new ArrayList<QPGNode>();
    }

    @Override
    public List<QPGNode> sort(QPGraph graph) {
        this.g = graph;
        //EDGE GRAPH
        List<QPGNode> sortableNodes = g.getAllNodes(EDGE);
        sortableNodes.addAll(g.getAllNodes(GRAPH));
        notVisited.addAll(sortableNodes);

        //each loop is a sub graph
        while (!notVisited.isEmpty()) {
            Map<QPGNode, Double> nextNodesPool = new HashMap<QPGNode, Double>();
            QPGNode first = findFirst(notVisited, nextNodesPool);
            route(first, nextNodesPool);
        }
        return visited;
    }

    //search in one connected graph, depth-first
    private void route(QPGNode previous, Map<QPGNode, Double> nextNodesPool) {
        if (previous == null) return;
       
        // while there is still node not visited in the pool
        QPGNode next ;
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

            //if two nodes with same cost, on choose the one with more edges
            if ((cost < minNode.getCost()) ||
                    ((cost == minNode.getCost()) && (this.g.getEdges(node).size() > this.g.getEdges(minNode).size()))) {
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
            if(double1 < cost){
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
            //1 get the list of edges that filter (values, etc..) f links to
            List linkedNodes = this.g.getLinkedNodes(f);
            if (visited.contains(f)) {
                continue;
            }

            //2 check this list contains un visited node
            boolean flag = true;
            for (QPGNode unVistiedNode : notVisited) {
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
    private void addValues(QPGNode min) {
        List<QPGNode> values = this.g.getAllNodes(VALUES);

        for (QPGNode v : values) {
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

    @Override
    public void rewrite(Exp exp, List<QPGNode> nodes) {
        //the expression
        if (exp.size() == nodes.size()) {
            for (int i = 0; i < nodes.size(); i++) {
                QPGNode node = nodes.get(i);
                exp.set(i, node.getExp());
            }
        } else {
            //todo
        }
    }
}
