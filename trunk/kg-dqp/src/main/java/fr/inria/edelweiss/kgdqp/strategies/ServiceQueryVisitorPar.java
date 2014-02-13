/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.strategies;

import fr.inria.acacia.corese.triple.parser.*;
import fr.inria.edelweiss.kgdqp.core.CallableGetEdges;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author gaignard
 */
public class ServiceQueryVisitorPar implements QueryVisitor {

    private QueryProcessDQP execDQP;

    public ServiceQueryVisitorPar(QueryProcessDQP execDQP) {
        this.execDQP = execDQP;
    }

    @Override
    public void visit(Query query) {
    }

    @Override
    public void visit(ASTQuery ast) {
        HashMap<Triple, ArrayList<String>> indexEdgeSource = new HashMap<Triple, ArrayList<String>>();
        HashMap<String, ArrayList<Triple>> indexSourceEdge = new HashMap<String, ArrayList<Triple>>();
        ArrayList<Triple> orderedTPs = new ArrayList<Triple>();

        //index initialization
        Exp body = ast.getBody();
        buildIndex(body, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
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

        
        //dumpEdgeIndex(indexEdgeSource);
        
        //dumpSourceIndex(indexSourceEdge);
        

        //Query rewriting
        ArrayList<Exp> globalFilters = new ArrayList<Exp>();
        // !! not generalized to graph filters tht would be included inside union ..
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isFilter()) {
                globalFilters.add(exp);
            }
        }
        Exp rewriten = rewriteQueryWithServices(body, globalFilters, indexSourceEdge, orderedTPs);

        //TODO body.clear()
        for (int i = 0; i < body.size();) {
            body.remove(i);
        }
        for (int i = 0; i < rewriten.size(); i++) {
            body.add(rewriten.get(i));
        }

       
//        System.out.println(ast.toSparql());
    }

    // How to build an index from a "flat" query ? 
    public void buildIndex(Exp exp, HashMap<Triple, ArrayList<String>> indexEdgeSource, HashMap<String, ArrayList<Triple>> indexSourceEdge, ASTQuery ast, ArrayList<Triple> orderedTPs) {
        ExecutorService exec = Executors.newCachedThreadPool();
        List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isOptional()) {
                Option opt = (Option) subExp;
                for (int j = 0; j < opt.size(); j++) {
                    buildIndex(opt.get(j), indexEdgeSource, indexSourceEdge, ast, orderedTPs);
                }
            } else if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                buildIndex(arg0, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
                //recursion 2
                buildIndex(arg1, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
            } else if (subExp.isTriple() && (!subExp.isFilter())) {
                Producer mp = execDQP.getProducer();
                if (mp instanceof MetaProducer) {
                    for (Producer p : ((MetaProducer) mp).getProducers()) {
                        if (p instanceof RemoteProducerWSImpl) {
                            RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                            String url = rp.getEndpoint().getEndpoint();
//                            System.out.println("ASK (" + url + ") -> " + exp.toString());

                            Triple triple = subExp.getTriple();
                            if (!orderedTPs.contains(triple)) {
                                orderedTPs.add(triple);
                            }
                            //use cache
//                            boolean ask = SourceSelectorHTTP.ask(triple.getPredicate().toSparql(), rp, ast);
                            CallableAsk callableAsk = new CallableAsk(triple, rp, ast, indexEdgeSource, url);
                            results.add(exec.submit(callableAsk));
                        }
                    }
                }
            }
        }
        exec.shutdown();
        while (!exec.isTerminated()) {
        //synchronization barrier
        }
    }

    public Exp rewriteQueryWithServices(Exp exp, ArrayList<Exp> globalFilters, HashMap<String, ArrayList<Triple>> indexSourceEdge, ArrayList<Triple> orderedTPs) {

        ArrayList<Exp> toKeepInsideService = new ArrayList<Exp>();
        ArrayList<Exp> toKeepOutsideService = new ArrayList<Exp>();

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isOptional()) {
                Option opt = (Option) subExp;
                //TODO include optional elements into services ? 
                toKeepOutsideService.add(subExp);
            } else if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                union.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge, orderedTPs));
                //recursion 2
                union.set(1, rewriteQueryWithServices(arg1, globalFilters, indexSourceEdge, orderedTPs));
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
        
        
        //Re-ordering query plan with preservation of the initial TP orders
         ArrayList<Service> services = getSparqlServices(globalFilters, toKeepInsideService, indexSourceEdge, orderedTPs);
//        ArrayList<Service> sortedServices = new ArrayList<Service>();
        
        for (Triple t : orderedTPs) {
            for (Service s : services) {
                if (s.getBody().get(0).getBody().contains(t)) { // what about UNION/OPTIONAL in a service clause ?
                    //add service to the sorted list
                    if (!bgp.getBody().contains(s)) {
                        bgp.add(s);
                        break;
                    }
                }
            }
            for (Exp e : toKeepOutsideService) {
                if (e.equals(t)) { //what about UNION/OPTIONAL ?
                    bgp.add(e);
                    break;
                } 
            }
        }
        
        
        
//        //service treatment
//        if (!toKeepInsideService.isEmpty()) {
//            ArrayList<Service> services = getSparqlServices(globalFilters, toKeepInsideService, indexSourceEdge, orderedTPs);
//            for (Service s : services) {
//                bgp.add(s);
//            }
//        }
//        //non-service treatment
//        if (!toKeepOutsideService.isEmpty()) {
//            for (Exp e : toKeepOutsideService) {
//                bgp.add(e);
//            }
//        }
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

    public ArrayList<Service> getSparqlServices(ArrayList<Exp> globalFilters, ArrayList<Exp> toKeep, HashMap<String, ArrayList<Triple>> indexSourceEdge, ArrayList<Triple> orderedTPs) {
        ArrayList<Service> services = new ArrayList<Service>();
        for (String url : indexSourceEdge.keySet()) {
//            fr.inria.acacia.corese.triple.parser.Exp serv = 

            BasicGraphPattern unsortedBgp = BasicGraphPattern.create();
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                //todo check if toKeep is really needed ? 
                if (toKeep.contains(t)) {
                    unsortedBgp.add(t);
                }
            }

            //sorting triples with respect to the initial query (orderedTPs)
            BasicGraphPattern bgp = BasicGraphPattern.create();
            for (Triple t : orderedTPs) {
                if (unsortedBgp.getBody().contains(t)) {
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

    class CallableAsk implements Callable<Boolean> {

        Triple triple;
        RemoteProducerWSImpl rp;
        ASTQuery ast;
        HashMap<Triple, ArrayList<String>> indexEdgeSource;
        String url;

        public CallableAsk(Triple triple, RemoteProducerWSImpl rp, ASTQuery ast, HashMap<Triple, ArrayList<String>> indexEdgeSource, String url) {
            this.triple = triple;
            this.rp = rp;
            this.ast = ast;
            this.indexEdgeSource = indexEdgeSource;
            this.url = url;
        }

        @Override
        public Boolean call() throws Exception {
            Boolean ask = SourceSelectorWS.ask(triple.getPredicate().toSparql(), rp, ast);

            if (ask) {
                if (indexEdgeSource.get(triple) == null) {
                    ArrayList<String> urls = new ArrayList<String>();
                    urls.add(url);
                    synchronized (this) {
                        indexEdgeSource.put(triple, urls);
                    }
                } else {
                    ArrayList<String> urls = indexEdgeSource.get(triple);
                    urls.add(url);
                    synchronized (this) {
                        indexEdgeSource.put(triple, urls);
                    }
                }
            }

            return ask;
        }
    }
}
