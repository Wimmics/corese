/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.*;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.RemoteProducerWSImpl;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author gaignard
 */
@Deprecated
public class ServiceQueryVisitor implements QueryVisitor {

    private QueryProcessDQP execDQP;

    public ServiceQueryVisitor(QueryProcessDQP execDQP) {
        this.execDQP = execDQP;
    }

    @Override
    public void visit(Query query) {
    }

    // essayer de ne faire qu'une fois select distinct ?p where { ?p rdf:type rdf:Property } !! necessite les entailments
    // sinon plus couteux : select distinct ?p where { ?x ?p ?y}
    @Override
    public void visit(ASTQuery ast) {
        HashMap<Triple, ArrayList<String>> indexEdgeSource = new HashMap<Triple, ArrayList<String>>();
        HashMap<String, ArrayList<Triple>> indexSourceEdge = new HashMap<String, ArrayList<Triple>>();

        //index initialization
        Exp body = ast.getBody();
        buildIndex(body, indexEdgeSource, indexSourceEdge, ast);
        // Source -> property index initialization
        for (Triple t : indexEdgeSource.keySet()) {
            if (indexEdgeSource.get(t).size() == 1) {
                String url = indexEdgeSource.get(t).get(0);
                if (indexSourceEdge.containsKey(url)) {
                    ArrayList<Triple> triples = indexSourceEdge.get(url);
                    triples.add(t);
                } else {
                    ArrayList<Triple> triples = new ArrayList<Triple>();
                    triples.add(t);
                    indexSourceEdge.put(url, triples);
                }
            }
        }
//        System.out.println("");
//        dumpEdgeIndex(indexEdgeSource);
//        System.out.println("");

        System.out.println("");
        dumpSourceIndex(indexSourceEdge);
        System.out.println("");

        //Query rewriting
        ArrayList<Exp> excludeFromServices = new ArrayList<Exp>();
        ArrayList<Exp> globalFilters = new ArrayList<Exp>();
        // !! not generalized to graph filters tht would be included inside union ..
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isFilter()) {
                globalFilters.add(exp);
            }
        }
        Exp rewriten = rewriteQueryWithServices(body, globalFilters, indexSourceEdge);

        //TODO body.clear()
        for (int i = 0; i < body.size();) {
            body.remove(i);
        }
        for (int i = 0; i < rewriten.size(); i++) {
            body.add(rewriten.get(i));
        }

        System.out.println("");
        System.out.println("Transformed AST");
        System.out.println(ast.toSparql());
    }

    // How to build an index from a "flat" query ? 
    //TODO parallelise the ASKs
    public void buildIndex(Exp exp, HashMap<Triple, ArrayList<String>> indexEdgeSource, HashMap<String, ArrayList<Triple>> indexSourceEdge, ASTQuery ast) {
        ExecutorService exec = Executors.newCachedThreadPool();
        List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                buildIndex(arg0, indexEdgeSource, indexSourceEdge, ast);
                //recursion 2
                buildIndex(arg1, indexEdgeSource, indexSourceEdge, ast);
            } else if (subExp.isTriple() && (!subExp.isFilter())) {
                Producer mp = execDQP.getProducer();
                if (mp instanceof MetaProducer) {
                    for (Producer p : ((MetaProducer) mp).getProducers()) {
                        // !!! HTTPimpl
//                        if (p instanceof RemoteProducerHTTPImpl) {
//                            RemoteProducerHTTPImpl rp = (RemoteProducerHTTPImpl) p;
//                            String url = rp.getEndpoint().getEndpoint();
                        // !!! WSimpl
                        if (p instanceof RemoteProducerWSImpl) {
                            RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                            String url = rp.getEndpoint().getEndpoint();
//                            System.out.println("ASK (" + url + ") -> " + exp.toString());

                            Triple triple = subExp.getTriple();
                            //use cache
//                            boolean ask = SourceSelectorHTTP.ask(triple.getPredicate().toSparql(), rp, ast);
                            boolean ask = SourceSelectorWS.ask(triple.getPredicate().toSparql(), rp, ast);
                            if (ask) {
                                if (indexEdgeSource.get(triple) == null) {
                                    ArrayList<String> urls = new ArrayList<String>();
                                    urls.add(url);
                                    indexEdgeSource.put(triple, urls);
                                } else {
                                    ArrayList<String> urls = indexEdgeSource.get(triple);
                                    urls.add(url);
                                    indexEdgeSource.put(triple, urls);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public Exp rewriteQueryWithServices(Exp exp, ArrayList<Exp> globalFilters, HashMap<String, ArrayList<Triple>> indexSourceEdge) {

        ArrayList<Exp> toKeepInsideService = new ArrayList<Exp>();
        ArrayList<Exp> toKeepOutsideService = new ArrayList<Exp>();

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                union.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge));
                //recursion 2
                union.set(1, rewriteQueryWithServices(arg1, globalFilters, indexSourceEdge));
                toKeepOutsideService.add(subExp);

            } else if (subExp.isTriple() && (!subExp.isFilter())) {
                if (!existsInSourceIndex(subExp.getTriple(), indexSourceEdge)) {
                    toKeepOutsideService.add(subExp);
                } else {
                    toKeepInsideService.add(subExp);
                }
            }
        }
        BasicGraphPattern bgp = new BasicGraphPattern();
        if (!toKeepInsideService.isEmpty()) {
            ArrayList<Exp> services = getSparqlServices(globalFilters, toKeepInsideService, indexSourceEdge);
            for (Exp s : services) {
                bgp.add(s);
            }
        }
        if (!toKeepOutsideService.isEmpty()) {
            for (Exp e : toKeepOutsideService) {
                bgp.add(e);
            }
        }
        return bgp;
    }

    public void dumpEdgeIndex(HashMap<Triple, ArrayList<String>> indexEdgeSource) {
        for (Triple t : indexEdgeSource.keySet()) {
            System.out.println(t.getPredicate());
            ArrayList<String> urls = indexEdgeSource.get(t);
            for (String url : urls) {
                System.out.println("\t->" + url);
            }
        }
    }

    public void dumpSourceIndex(HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        for (String url : indexSourceEdge.keySet()) {
            System.out.println(url);
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                System.out.println("\t->" + t.getPredicate());
            }
        }
    }

    public ArrayList<Exp> getSparqlServices(ArrayList<Exp> globalFilters, ArrayList<Exp> toKeep, HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        ArrayList<Exp> services = new ArrayList<Exp>();
        for (String url : indexSourceEdge.keySet()) {
//            fr.inria.acacia.corese.triple.parser.Exp serv = 

            BasicGraphPattern bgp = BasicGraphPattern.create();
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                if (toKeep.contains(t)) {
                    bgp.add(t);
                }
            }

            // Include filters
            // !! attention cas particulier du filtre !exist()
            List<Exp> applicableFilters = Util.getApplicableFilter(globalFilters, bgp);
            for (Exp f : applicableFilters) {
                bgp.append(f);
            }

            if (bgp.size() != 0) {
                services.add(Service.create(Constant.create(url), bgp));
            }
        }
        return services;
    }

    public boolean existsInSourceIndex(Triple t, HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        for (String url : indexSourceEdge.keySet()) {
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            if (triples.contains(t)) {
                return true;
            }
        }
        return false;
    }
}
