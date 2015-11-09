/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import static fr.inria.edelweiss.kgram.api.core.ExpType.AND;
import static fr.inria.edelweiss.kgram.api.core.ExpType.BGP;
import static fr.inria.edelweiss.kgram.api.core.ExpType.JOIN;
import static fr.inria.edelweiss.kgram.api.core.ExpType.UNION;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author macina
 */
public class GenerateBGP extends Sorter {

    private Logger logger = Logger.getLogger(GenerateBGP.class);

    private Exp exp;

    private boolean horizontal = false;
    private boolean vertical = false;
    private HashMap<Edge, ArrayList<Producer>> indexEdgeProducers;
    private List<Producer> producers;

    public GenerateBGP() {
        this.producers = new ArrayList<Producer>();
        this.indexEdgeProducers = new HashMap<Edge, ArrayList<Producer>>();
    }

    public boolean isHorizontal() {
        Edge e;
        boolean result = true;
        for (int i = 0; i < exp.getExpList().size() && result; i++) {
            e = exp.getExpList().get(i).getEdge();
            if (indexEdgeProducers.get(e).size() < 2) {
                result = false;
            }
        }
        horizontal = result;
        return horizontal;
    }

    public boolean isVertical() {
        Edge e;
        boolean result = true;
        for (int i = 0; i < exp.getExpList().size() && result; i++) {
            e = exp.getExpList().get(i).getEdge();
            if ((indexEdgeProducers.get(e) != null) && (indexEdgeProducers.get(e).size() > 1)) {
                result = false;
            }
        }
        vertical = result;
        return vertical;
    }

    public Exp buildBGP() {
        //To do : process on indexEdgeProducers and generate BGP
//        Exp results = Exp.create(UNION);
        if (isVertical()) {
//            logger.info("Vertical");
            exp.setType(BGP);
            return exp;
        }
        if (isHorizontal()) {
//            logger.info("Horizontal");
            return createBGP();
        }
        return exp;
    }

    public Exp createUnion(ArrayList<Edge> edges) {
        Exp results = Exp.create(UNION);

        Exp bgp = new Exp(BGP);

        Exp andLock = new Exp(AND);
        for (Edge e : edges) {
            bgp.add(e);
            andLock.add(e);
        }

        andLock.setLock(true);
        results.add(bgp);
        results.add(andLock);

//            logger.info("BGP "+bgp+ "  AND LOCK "+exp);
//        logger.info("Horizontal " + results.toString());
        return results;
    }

    public Exp createBGP() {
        HashMap<Producer, ArrayList<Edge>> indexProducerEdges = buildIndexProducerEdges();
        Exp joinBGP = Exp.create(JOIN);
        ArrayList<Edge> edges;
        //all edges might be found in all producers
        if (compareAllProducers()) {
            edges = indexProducerEdges.get(producers.get(0));
            return createUnion(edges);
        } else {
            //draft
            Exp tmp1 = createUnion(indexProducerEdges.get(producers.get(0)));
            Exp tmp2 = createUnion(indexProducerEdges.get(producers.get(1)));
            Exp join = new Exp(JOIN, tmp1, tmp2);
            for (int i = 2; i < producers.size(); i++) {
                Producer p = producers.get(i);
                edges = indexProducerEdges.get(p);
                if (i == 2) {
                    joinBGP = new Exp(JOIN,join,createUnion(edges));
                } else {
                    joinBGP = new Exp(JOIN, joinBGP, createUnion(edges));
                }
            }
            return joinBGP;
        }
    }

    public boolean compareAllProducers() {
        boolean res = true;
        for (int i = 0; i + 1 < exp.getExpList().size() && res; i++) {
            Edge e1 = exp.getExpList().get(i).getEdge();
            Edge e2 = exp.getExpList().get(i + 1).getEdge();
            res = compare2Producers(indexEdgeProducers.get(e1), indexEdgeProducers.get(e2));
        }
        return res;
    }

    public boolean compare2Producers(ArrayList<Producer> p1, ArrayList<Producer> p2) {
        ArrayList<Producer> tmp1 = (ArrayList<Producer>) p1.clone();
        ArrayList<Producer> tmp2 = (ArrayList<Producer>) p2.clone();

        tmp1.removeAll(p2);
        tmp2.removeAll(p1);

        return (tmp1.size() == tmp2.size()) && (tmp1.isEmpty());

    }

    public HashMap<Producer, ArrayList<Edge>> buildIndexProducerEdges() {
        Edge e;
        HashMap<Producer, ArrayList<Edge>> indexProducerEdges = new HashMap<Producer, ArrayList<Edge>>();
        ArrayList<Producer> producersList;
        ArrayList<Edge> edges;

        for (int i = 0; i < exp.getExpList().size(); i++) {
            e = exp.getExpList().get(i).getEdge();
            producersList = indexEdgeProducers.get(e);

            for (Producer p : producersList) {
                if (!producers.contains(p)) {
                    producers.add(p);
                }
                if (indexProducerEdges.containsKey(p)) {
                    edges = indexProducerEdges.get(p);
                    edges.add(e);
                    indexProducerEdges.put(p, edges);
                } else {
                    edges = new ArrayList<Edge>();
                    edges.add(e);
                    indexProducerEdges.put(p, edges);
                }
            }
        }
//        logger.info("LIST OF PRODUCERS "+producers.size());
        return indexProducerEdges;
    }

    void setExp(Exp exp) {
        this.exp = exp;
    }

    public HashMap<Edge, ArrayList<Producer>> getIndexEdgeProducers() {
        return indexEdgeProducers;
    }

    public void setIndexEdgeProducers(HashMap<Edge, ArrayList<Producer>> indexEdgeProducers) {
        this.indexEdgeProducers = indexEdgeProducers;
    }
}
