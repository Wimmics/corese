package fr.inria.corese.kgram.sorter.impl.qpv1;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation for sorting the triple pattern Pure depth-first greedy
 * algorithm
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 19 mai 2014
 */
public class DepthFirstSearch implements ISort {

    private final List<QPGNode> visited, notVisited;
    private QPGraph g = null;
    private final boolean SORT_MODE_BY_EDGE = true;

    public DepthFirstSearch() {
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
            QPGNode first = findFirst(notVisited);
            route(first);
        }
        return visited;
    }

    //search in one connected graph, depth-first
    private void route(QPGNode previous) {
        if (previous == null) {
            return;
        }

        //1 get all the nodes linked to this node
        List<QPGNode> children;
        if (this.SORT_MODE_BY_EDGE) {
            children = this.getChildrenByEdge(previous);
        } else {
            children = this.getChildren(previous);
        }

        //2 while its chidlren (for one node) is not empty, continue search
        QPGNode next = null;
        while (!children.isEmpty() && ((next = findNext(children)) != null)) {
            route(next);
        }
    }

    //Find the bpg node with the smallest selectivity as starting routing point
    private QPGNode findFirst(List<QPGNode> lNodes) {
        if (lNodes == null || lNodes.isEmpty()) {
            return null;
        }

        QPGNode minNode = lNodes.get(0);
        double min = minNode.getCost();

        for (QPGNode node : lNodes) {
            double sel = node.getCost();
            if (sel != IEstimate.NA_COST && sel < min) {
                min = sel;
                minNode = node;
            }
        }
        this.queue(minNode);
        return minNode;
    }

    //find next node to be routed
    private QPGNode findNext(List<QPGNode> lNodes) {

        // if no nodes left, return null
        if (lNodes.isEmpty()) {
            return null;
        }

        //becaue the list of nodes has been sorted, so just return the toppest one
        QPGNode next = lNodes.get(0);

        //3 add to list
        this.queue(next);

        return next;
    }

    //get all the nodes linked to this node and sort by n.cost
    private List<QPGNode> getChildren(QPGNode n) {
        // get the linked nodes to a node and sort it (define the sequence of searching)
        List<QPGNode> lNodes = this.g.getLinkedNodes(n),
                children = new ArrayList<QPGNode>();
        //1 sort the list by selectivity of nodes
        children.addAll(lNodes);
        Collections.sort(children, new Comparator<QPGNode>() {
            @Override
            public int compare(QPGNode o1, QPGNode o2) {
                return Double.valueOf(o1.getCost()).compareTo(Double.valueOf(o2.getCost()));
            }
        });

        //1 remvoe visited nodes
        for (QPGNode node : visited) {
            if (children.contains(node)) {
                children.remove(node);
            }
        }

        return children;
    }

    //get all the nodes linked to this node and sort by e.cost * n.cost of nodes
    private List<QPGNode> getChildrenByEdge(QPGNode n) {
        List<QPGNode> children = new ArrayList<QPGNode>();

        List<QPGEdge> lEdges = this.g.getEdges(n, QPGEdge.BI_DIRECT);
        final Map<QPGNode, Double> weights = new HashMap<QPGNode, Double>();

        for (QPGEdge e : lEdges) {
            children.add(e.get(n));
            weights.put(e.get(n), e.getCost());
        }

        Collections.sort(children, new Comparator<QPGNode>() {
            @Override
            public int compare(QPGNode n1, QPGNode n2) {
                double d1 = n1.getCost() * weights.get(n1);
                double d2 = n2.getCost() * weights.get(n2);
                return Double.valueOf(d1).compareTo(Double.valueOf(d2));
            }
        });

        //1 remvoe visited nodes
        for (QPGNode node : visited) {
            if (children.contains(node)) {
                children.remove(node);
            }
        }

        return children;
    }

    private void queue(QPGNode minNode) {
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

    @Override
    public void rewrite(Exp exp, List<QPGNode> nodes, int start) {
        for (int i = 0; i < nodes.size(); i++) {
            QPGNode node = nodes.get(i);
            exp.set(start + i, node.getExp());
        }
    }
}
