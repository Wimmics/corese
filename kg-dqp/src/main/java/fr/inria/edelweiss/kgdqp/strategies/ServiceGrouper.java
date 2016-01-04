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
import fr.inria.edelweiss.kgenv.parser.EdgeImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.BgpGenerator;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * A class aiming at grouping, if possible, consecutive tripple into SPARQL
 * SERVICE clauses.
 *
 * 
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 * @author Abdoul Macina <macina@i3s.unice.fr>
 */
public class ServiceGrouper implements QueryVisitor {

    Logger logger = Logger.getLogger(ServiceGrouper.class);
    private QueryProcessDQP execDQP;

    private HashMap<Triple, ArrayList<Producer>> indexEdgeProducers;

    public ServiceGrouper(QueryProcessDQP execDQP) {
        this.indexEdgeProducers = new HashMap<Triple, ArrayList<Producer>>();
        this.execDQP = execDQP;
    }

    /**
     * Entry point to navigate trough a Query expression.
     * This visitor will build indexEdgeSources, indexEdgeVaribles and listEdges for a query
     * 
     * @param query 
     */
    @Override
    public void visit(Query query) {
        if (execDQP.getPlanProfile() == Query.QP_BGP) {
            logger.info("Building predicates indices for "+query.getBody());
            int i = 0;
            for (fr.inria.edelweiss.kgram.core.Exp e : query.getExpList()) {
                if (e.isBGPAnd()) {
                    buildGeneratedBGPIndices(query, e, i);
                    i++;
                }
            }
            logger.info("END of building index!");
        }
    }

    /**
     * Entry point to navigate through a query abstract syntax tree (AST). This
     * vistor transforms a query to possibly group tripple patterns into SERVICE
     * clauses.
     *
     * @param ast The input query AST.
     */
    @Override
    public void visit(ASTQuery ast) {
        HashMap<Triple, ArrayList<String>> indexEdgeSource = new HashMap<Triple, ArrayList<String>>();
        HashMap<String, ArrayList<Triple>> indexSourceEdge = new HashMap<String, ArrayList<Triple>>();
        ArrayList<Triple> orderedTPs = new ArrayList<Triple>();

        //index initialization
        Exp body = ast.getBody();
        logger.info("Building indices for ");
        logger.info(body.toSparql());

        buildIndex(body, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
        // Source -> property index initialization
        for (Triple t : indexEdgeSource.keySet()) {
            if (indexEdgeSource.get(t).size() > 0) {
                for (String url : indexEdgeSource.get(t)) {
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
        }

        dumpEdgeIndex(indexEdgeSource);
        dumpSourceIndex(indexSourceEdge);

        //Query rewriting
        ArrayList<Exp> globalFilters = new ArrayList<Exp>();
        // !! not generalized to graph filters tht would be included inside union ..
        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isFilter()) {
                globalFilters.add(exp);
            }
        }

        rewriteQueryWithServices(body, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs);
        logger.info("Final rewritten query");
        logger.info(ast.toSparql());
    }

    /**
     * This methods builds two indices. The first one annotates triple patterns
     * with data source possibly hosting candidates. The second one give
     * information, for each data source, on the triple pattern candidates.
     *
     * @param exp a KGRAM expression containing triple patterns.
     * @param indexEdgeSource the first index "TP -> data source" as output.
     * @param indexSourceEdge the second index "data source -> TP" as output.
     * @param ast the input AST query.
     * @param orderedTPs accumulated triple patterns appearing in the initial
     * query. They are reused when rewriting the query.
     */
    // How to build an index from a "flat" query ? 
    public void buildIndex(Exp exp, HashMap<Triple, ArrayList<String>> indexEdgeSource, HashMap<String, ArrayList<Triple>> indexSourceEdge, ASTQuery ast, ArrayList<Triple> orderedTPs) {

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

                            Boolean ask = SourceSelectorWS.ask(triple.getPredicate().toSparql(), rp, ast);
                            if (ask) {
                                if (indexEdgeSource.get(triple) == null) {
                                    ArrayList<String> urls = new ArrayList<String>();
                                    urls.add(url);
                                    indexEdgeSource.put(triple, urls);

                                    ArrayList<Producer> producersbis = new ArrayList<Producer>();
                                    producersbis.add(p);
                                    indexEdgeProducers.put(triple, producersbis);

                                } else {
                                    ArrayList<String> urls = indexEdgeSource.get(triple);
                                    urls.add(url);
                                    indexEdgeSource.put(triple, urls);

                                    ArrayList<Producer> producersbis = indexEdgeProducers.get(triple);
                                    producersbis.add(p);
                                    indexEdgeProducers.put(triple, producersbis);
                                }
                            }
                        }
                    }
                }

            } 
            else {
                //
                if (subExp.isAnd() ||subExp.isQuery() ) {
                    buildIndex(subExp, indexEdgeSource, indexSourceEdge, ast, orderedTPs);
                }
            }
        }
    }

    /**
     * Core of query rewriting. Takes as input the two indices, and the flat
     * list of triple patterns and possibly generates SERVICE clauses. SERVICES
     * are sorted based on the number of free variables. If they have the same
     * number of free variables, the initial TPs order is preserved.
     *
     * @param exp the KGRAM expression represinting the input query.
     * @param globalFilters some of the query filters. To be refined.
     * @param indexSourceEdge the data source -> triple pattern index.
     * @param indexEdgeSource thet triple pattern -> data source index.
     * @param orderedTPs the flat list of TPs appearing in the inout query.
     * @return a rewriten expression with possibly SERVICE clauses.
     */
    public Exp rewriteQueryWithServices(Exp exp, ArrayList<Exp> globalFilters,
            HashMap<String, ArrayList<Triple>> indexSourceEdge,
            HashMap<Triple, ArrayList<String>> indexEdgeSource,
            ArrayList<Triple> orderedTPs) {

        ArrayList<Exp> consecutiveTP = new ArrayList<Exp>();
        ArrayList<Exp> tpToBeRemoved = new ArrayList<Exp>();

        for (int i = 0; i < exp.size(); i++) {
            Exp subExp = exp.get(i);
            if (subExp.isBGP()) {
                BasicGraphPattern bgp = (BasicGraphPattern) subExp;
                Exp arg0 = bgp.get(0);
                bgp.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
            } else if (subExp.isOptional()) {
                Option opt = (Option) subExp;
                //TODO include optional elements into services ? 
                Exp arg0 = opt.get(0);
                Exp arg1 = opt.get(1);
                //recursion 1
                opt.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
                //recursion 2
                opt.set(1, rewriteQueryWithServices(arg1, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
            } else if (subExp.isUnion()) {
                Or union = (Or) subExp;
                Exp arg0 = union.get(0);
                Exp arg1 = union.get(1);
                //recursion 1
                union.set(0, rewriteQueryWithServices(arg0, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
                //recursion 2
                union.set(1, rewriteQueryWithServices(arg1, globalFilters, indexSourceEdge, indexEdgeSource, orderedTPs));
//            } else if (subExp.isAnd()) {
//               
//            } else if (subExp.isGraph()) {

            } else if (subExp.isTriple() && (!subExp.isFilter())) {
                consecutiveTP.add(subExp);
                tpToBeRemoved.add(subExp);
            }
        }

        Exp services = null;
        /// Grouping of consecutive TPs into SERVICE clauses
        if (consecutiveTP.size() > 0) {
            logger.info("Found consecutive triple patterns into " + exp.toSparql());
            // SERVICE clause generation
            services = getSparqlServices(globalFilters, consecutiveTP, indexSourceEdge, indexEdgeSource, orderedTPs);
            exp.getBody().removeAll(tpToBeRemoved);
        } else {
            logger.info("No consecutive triple patterns into " + exp.toSparql());
            return exp;
        }

        // RANKING consecutive SERVICE clauses
        HashMap<Exp, Integer> rankedExpressions = new HashMap<Exp, Integer>();

        logger.info("Ranking : ");
        logger.info(services.toSparql());
        for (Exp e : services.getBody()) {
//            if (e instanceof Service) {
//                Service s = (Service) e;
            int freeVars = countFreeVars(e);
            logger.info(freeVars + " free variables for " + e.toSparql());
            rankedExpressions.put(e, freeVars);
//            } else if (e instanceof Service) {
//                //TODO What if e is a TP (rdf:type or sameas predicates !!
//                
//            }
        }

        // REORDERING consecutive SERVICE clauses
        logger.info("Sorting services possibly with the same score");
        ArrayList<Map.Entry<Exp, Integer>> serviceList = new ArrayList<Map.Entry<Exp, Integer>>();
        for (Map.Entry<Exp, Integer> e : rankedExpressions.entrySet()) {
            serviceList.add(e);
        }

        final ArrayList<Exp> initTps = consecutiveTP;
        Collections.sort(serviceList, new Comparator<Map.Entry<Exp, Integer>>() {
            @Override
            public int compare(Map.Entry<Exp, Integer> o1, Map.Entry<Exp, Integer> o2) {
                if (o1.getValue() == o2.getValue()) {
                    Exp e1 = o1.getKey();
                    Exp e2 = o2.getKey();

                    Exp firstE1 = null;
                    Exp firstE2 = null;

                    if (e1 instanceof Service) {
                        firstE1 = e1.getBody().get(0).get(0);
                    } else if (e1 instanceof Triple) {
                        firstE1 = e1;
                    } else {
                        logger.warn("!!!!!!"); //TODO
                    }
                    if (e2 instanceof Service) {
                        firstE2 = e2.getBody().get(0).get(0);
                    } else if (e2 instanceof Triple) {
                        firstE2 = e2;
                    } else {
                        logger.warn("!!!!!!"); //TODO
                    }

                    Integer initRankeE1 = getInitialRank(firstE1, initTps);
                    Integer initRankeE2 = getInitialRank(firstE2, initTps);

                    if (initRankeE1 == -1) {
                        logger.warn(firstE1.toSparql() + " was not found in " + initTps);
                    }
                    if (initRankeE2 == -1) {
                        logger.warn(firstE2.toSparql() + " was not found in " + initTps);
                    }

                    logger.info("Comparing : " + firstE1.toSparql() + " with " + firstE2.toSparql());
                    return initRankeE1.compareTo(initRankeE2);
                } else {
                    return o1.getValue().compareTo(o2.getValue());
                }
            }

        });

        for (Map.Entry<Exp, Integer> e : serviceList) {
            exp.add(e.getKey());
        }
        logger.info(exp.toSparql());
        return exp;
    }

    /**
     * Get the rank of a triple pattern in a list.
     *
     * @param exp the input triple pattern.
     * @param consecutiveTP the lists of consecutive triple patterns.
     * @return the rank of the expression in the list of consecutive triple
     * patterns, -1 if the triple pattern is not found.
     */
    private int getInitialRank(Exp exp, ArrayList<Exp> consecutiveTP) {
        logger.warn("Searching for " + exp.toSparql() + " in " + consecutiveTP);
        int r = 1;
        for (Exp e : consecutiveTP) {
            if (e.equals(exp)) {
                return r;
            }
            r++;
        }
        return -1;
    }

    /**
     * Counts free variables in an expression.
     *
     * @param s an input expression.
     * @return the number of free variables.
     */
    private int countFreeVars(Exp s) {
        int count = 0;
        ArrayList<Atom> freeVars = new ArrayList<Atom>();
        if (s instanceof Triple) {
            Triple t = (Triple) s;
            if (t.getSubject().isVariable() && !freeVars.contains(t.getSubject())) {
                freeVars.add(t.getSubject());
            }
            if (t.getObject().isVariable() && !freeVars.contains(t.getObject())) {
                freeVars.add(t.getObject());
            }
            if (t.getPredicate().isVariable() && !freeVars.contains(t.getPredicate())) {
                freeVars.add(t.getPredicate());
            }
            count += freeVars.size();
            return count;
        } else {
            for (Exp e : s.getBody()) {
                if (e.isBGP()) {
                    count += countFreeVars(e);
                } else if (e.isTriple()) {
                    Triple t = (Triple) e;
                    if (t.getSubject().isVariable() && !freeVars.contains(t.getSubject())) {
                        freeVars.add(t.getSubject());
                    }
                    if (t.getObject().isVariable() && !freeVars.contains(t.getObject())) {
                        freeVars.add(t.getObject());
                    }
                    if (t.getPredicate().isVariable() && !freeVars.contains(t.getPredicate())) {
                        freeVars.add(t.getPredicate());
                    }
                }
            }
            count += freeVars.size();
            return count;
        }
    }

    /**
     * Transdorms a flat list of consecutive triple patterns into possibly a set
     * of SERVICE clauses. If TP candidates are hosted in more than one single
     * data source, they are excluded from SERVICE clauses.
     *
     * @param globalFilters some of the query filters. To be refined.
     * @param consecutiveTPs the list of consecutive triple paterns to be
     * possibly grouped.
     * @param indexSourceEdge the data source -> triple pattern index.
     * @param indexEdgeSource the triple pattern -> data source index.
     * @param orderedTPs the flat list of TPs appearing in the inout query.
     * @return an expression containing triple patterns possibly grouped.
     */
    public Exp getSparqlServices(ArrayList<Exp> globalFilters, ArrayList<Exp> consecutiveTPs,
            HashMap<String, ArrayList<Triple>> indexSourceEdge,
            HashMap<Triple, ArrayList<String>> indexEdgeSource,
            ArrayList<Triple> orderedTPs) {

        logger.info("Processing\n" + consecutiveTPs.toString());
        ArrayList<Service> services = new ArrayList<Service>();
        BasicGraphPattern outOfServicesBgp = BasicGraphPattern.create();

        for (String url : indexSourceEdge.keySet()) {
            BasicGraphPattern unsortedBgp = BasicGraphPattern.create();
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                //todo check if toKeep is really needed ? 
                if (consecutiveTPs.contains(t)) {
                    unsortedBgp.add(t);
                }
            }

            //sorting triples with respect to the initial query (orderedTPs)
            BasicGraphPattern servBgp = BasicGraphPattern.create();
            for (Triple t : orderedTPs) {
                if (unsortedBgp.getBody().contains(t)) {
                    //checking if the TP also appears in another source, if so, adding it into an OPTIONAL clause
                    if (indexEdgeSource.get(t).size() > 1) {
                        logger.warn("Triple pattern " + t.toSparql() + " appears in more than one data source, excluded from SERVICE.");
//                        Option opt = Option.create(BasicGraphPattern.create(t));
//                        servBgp.add(opt);
                        if (!outOfServicesBgp.getBody().contains(t)) {
                            outOfServicesBgp.add(t);
                        }
                    } else {
                        servBgp.add(t);
                    }
                }
            }

            // Include filters
            // !! attention cas particulier du filtre !exist()
            List<Exp> applicableFilters = Util.getApplicableFilter(globalFilters, servBgp);
            for (Exp f : applicableFilters) {
                servBgp.append(f);
            }

            if (servBgp.size() != 0) {
                outOfServicesBgp.add(Service.create(Constant.create(url), servBgp));
            }

        }
        return outOfServicesBgp;
    }

    /**
     * Transdorms a flat list of consecutive triple patterns into possibly a set
     * of SERVICE clauses. If TP candidates are hosted in more than one single
     * data source, they are included into SERVICE clauses through OPTIONAL
     * statements.
     *
     * @param globalFilters some of the query filters. To be refined.
     * @param consecutiveTPs the list of consecutive triple paterns to be
     * possibly grouped.
     * @param indexSourceEdge the data source -> triple pattern index.
     * @param indexEdgeSource the triple pattern -> data source index.
     * @param orderedTPs the flat list of TPs appearing in the inout query.
     * @return an expression containing triple patterns possibly grouped.
     */
    public Exp getSparqlServicesWithOptionals(ArrayList<Exp> globalFilters, ArrayList<Exp> consecutiveTPs,
            HashMap<String, ArrayList<Triple>> indexSourceEdge,
            HashMap<Triple, ArrayList<String>> indexEdgeSource,
            ArrayList<Triple> orderedTPs) {

        logger.info("Processing\n" + consecutiveTPs.toString());

        BasicGraphPattern bgpWithServices = BasicGraphPattern.create();

        for (String url : indexSourceEdge.keySet()) {
            BasicGraphPattern unsortedBgp = BasicGraphPattern.create();
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                //todo check if toKeep is really needed ? 
                if (consecutiveTPs.contains(t)) {
                    unsortedBgp.add(t);
                }
            }

            //sorting triples with respect to the initial query (orderedTPs)
            BasicGraphPattern servBgp = BasicGraphPattern.create();
            for (Triple t : orderedTPs) {
                if (unsortedBgp.getBody().contains(t)) {
                    //checking if the TP also appears in another source, if so, adding it into an OPTIONAL clause
                    if (indexEdgeSource.get(t).size() > 1) {
                        logger.warn("Triple pattern " + t.toSparql() + " appears in more than one data source, included in OPTIONAL.");
                        Option opt = Option.create(BasicGraphPattern.create(t));
                        servBgp.add(opt);
                    } else {
                        servBgp.add(t);
                    }
                }
            }

            // Include filters
            // !! be careful with !exist()
            List<Exp> applicableFilters = Util.getApplicableFilter(globalFilters, servBgp);
            for (Exp f : applicableFilters) {
                servBgp.append(f);
            }

            if (servBgp.size() != 0) {
                bgpWithServices.add(Service.create(Constant.create(url), servBgp));
            }

        }
        return bgpWithServices;
    }

    /**
     * Checks if a triple pattern exists in the source -> triple pattern index.
     *
     * @param t the triple patterns.
     * @param indexSourceEdge the source -> triple pattern index.
     * @return true if it exists in the index, false otherwise.
     */
    public boolean existsInSourceIndex(Triple t, HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        for (String url : indexSourceEdge.keySet()) {
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            if (triples.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints the content of the triple pattern -> data source index.
     *
     * @param indexEdgeSource the triple pattern -> data source index.
     */
    private void dumpEdgeIndex(HashMap<Triple, ArrayList<String>> indexEdgeSource) {
        logger.info("Edge index");
        for (Triple t : indexEdgeSource.keySet()) {
            System.out.println(t.getPredicate());
            ArrayList<String> urls = indexEdgeSource.get(t);
            for (String url : urls) {
                System.out.println("\t->" + url);
            }
        }
    }

    /**
     * Prints the content of the data source -> triple pattern.
     *
     * @param indexSourceEdge the data source -> triple pattern.
     */
    private void dumpSourceIndex(HashMap<String, ArrayList<Triple>> indexSourceEdge) {
        logger.info("Source index");
        for (String url : indexSourceEdge.keySet()) {
            System.out.println(url);
            ArrayList<Triple> triples = indexSourceEdge.get(url);
            for (Triple t : triples) {
                System.out.println("\t->" + t.getPredicate());
            }
        }
    }

    /**
     *
     * @param query
     * @param exp
     */
    private void buildGeneratedBGPIndices(Query query, fr.inria.edelweiss.kgram.core.Exp exp, int n) {

        List<Edge> tmpEdges = query.getQueryEdgeList();
        HashMap<Edge, ArrayList<Producer>> tmpEdgeProducers = query.getBgpGenerator().getIndexEdgeProducers();
        HashMap<Edge, ArrayList<Node>> tmpEdgeVariables = query.getBgpGenerator().getIndexEdgeVariables();
        HashMap<Edge, ArrayList<Filter>> tmpEdgeFilters = query.getBgpGenerator().getIndexEdgeFilters();
        
        List<Filter> tmpFilters = new ArrayList<Filter>();
        HashMap<Filter, List<String>> tmpFilterVariables =  new HashMap<Filter, List<String>>();
        
        int i = 0;
        for (fr.inria.edelweiss.kgram.core.Exp e : exp) {
            //get Filters
            if(e.isFilter()){
                tmpFilters.add(e.getFilter());
                tmpFilterVariables.put(e.getFilter(), e.getFilter().getVariables());
            }
            
            //process Edges
            if (e.isEdge()) {
                
                    //build indexEdgeProducers
                    tmpEdges.add(e.getEdge());
                    EdgeImpl ee = (EdgeImpl) e.getEdge();
                    tmpEdgeProducers.put(e.getEdge(), indexEdgeProducers.get(ee.getTriple()));

                    //build indexEdgesVariables
                    ArrayList<Node> tmpNodes = new ArrayList<Node>();
                    for (int l=0; l<ee.nbNode(); l++){
                        if(e.getEdge().getNode(l).isVariable()){
                            tmpNodes.add(e.getEdge().getNode(l));
                            
                            //build indexEdgeFilters
                            for(Filter f: tmpFilters){
                                if(tmpFilterVariables.get(f).contains(e.getEdge().getNode(l).toString())){
                                    if(tmpEdgeFilters.get(e.getEdge())!=null){
                                        ArrayList<Filter> filters =  tmpEdgeFilters.get(e.getEdge());
                                        filters.add(f);
                                        tmpEdgeFilters.put(e.getEdge(), filters);
                                    }
                                    else {
                                        ArrayList<Filter> filters =  new ArrayList<Filter>();
                                        filters.add(f);
                                        tmpEdgeFilters.put(e.getEdge(), filters);
                                    }
                                }
                            }
                        }
                    }
                    
                    tmpEdgeVariables.put(e.getEdge(), tmpNodes);
                    i++;
            } else {
                //apply visit to subquery
                if (e.type() == Query.SERVICE) {
                    visit((Query) e.rest());
                }
                //other expressions like UNION SELECT etc.
                else {
                        buildGeneratedBGPIndices(query, e, i);
                }
            }
        }

//        logger.info("EP "+tmpEdgeProducers);
//        logger.info("EV "+tmpEdgeVariables);
//        logger.info("EF "+tmpEdgeFilters);
//        logger.info("E "+tmpEdges);
        //Update BgpGenerator for the Query
        BgpGenerator gBGP = query.getBgpGenerator();
        gBGP.setIndexEdgeProducers(tmpEdgeProducers);
        gBGP.setIndexEdgeVariables(tmpEdgeVariables);
        gBGP.setIndexEdgeFilters(tmpEdgeFilters);
        query.setQueryEdgeList(tmpEdges);
        query.setBgpGenerator(gBGP);
    }

//    //draft
//    private void putFreeEdgesInBGP(Exp body) {
//
//        BasicGraphPattern bgp = new BasicGraphPattern();
//        int index =-1;
//        for (int i = 0; i < body.size(); i++){
//            if(body.get(i).isTriple()){
//                
//                bgp.add(body.get(i));
//                if(index!=-1){
//                    body.remove(index);
//                    index =i-1;
//                }
//                else{
//                    index = i;
//                }
//            }
//        }
//        
//        body.set(index, bgp);
//        System.out.println(" RES "+body);
//    }
}
