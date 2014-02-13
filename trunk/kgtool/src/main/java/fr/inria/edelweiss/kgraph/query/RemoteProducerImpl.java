/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.query;
//import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class RemoteProducerImpl 
//implements Producer 
{

//    private static Logger logger = Logger.getLogger(RemoteProducerImpl.class);
//    private RemoteProducer rp;
//
//    public RemoteProducerImpl(URL url) {
//        rp = RemoteProducerServiceClient.getPort(url);
//    }
//
//    /**
//     * nothing
//     */
//    @Override
//    public void init(int nbNodes, int nbEdges) {
////        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    /**
//     * propagate to the server ? nothing
//     */
//    public void setMode(int n) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * enum�rer la liste des uris de graphe nomm�s.
//     * select * where {graph ?g {}}
//     * @param gNode
//     * @param from
//     * @param env
//     * @return 
//     */
//    @Override
//    public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * test si l'uri de gNode est bien celle d'un graphe nomm�. 
//     * si gNode a une valeur g1 dans l'environnement, propager la requete
//     * ask {graph g1 {}} c�t� serveur
//     * 
//     * @param gNode
//     * @param from
//     * @param env
//     * @return 
//     */
//    @Override
//    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * Revoir prise en compte gNode, from et env
//     * @param gNode variable de graphe si elle existe, null sinon
//     * @param from liste des "from named <g>"
//     * @param qEdge arc recherch�
//     * @param env environnement qui contient �ventuellement contient la valeur de gNode
//     * @return 
//     */
//    @Override
//    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {
//
//        // si gNode != null et from non vide, alors "from named"
//        // si gNode == null et from non vide alors "from"
//
//        ArrayList<Entity> results = new ArrayList<Entity>();
//
//        List<String> sparqlQueries = getSparqlFromEdgeRequest(qEdge, env);
//        Graph g = Graph.create();
//
//
//        for (String query : sparqlQueries) {
//            logger.debug("sending query \n" + query + "\n");
//
//            try {
//                logger.debug("Pushing sparql to " + rp.getEndpoint());
//                logger.debug("Searching for edge " + qEdge.toString());
//
//                String sparqlRes = rp.getEdges(query);
//                if (sparqlRes != null) {
////                    logger.debug("Received result: \n" + sparqlRes);
//                    String subject = qEdge.getNode(0).getLabel();
//                    String object = qEdge.getNode(1).getLabel();
//                    String predicate = qEdge.getEdgeNode().getLabel();
//
//                    SparqlResultParser parser = new SparqlResultParser();
//                    List<HashMap<String, String>> res = parser.parse(sparqlRes);
//
//                    if (!res.isEmpty()) {
//                        String resAsUpdate = "insert data {\n";
//                        for (HashMap<String, String> r : res) {
//                            resAsUpdate += "\t " + r.get(subject) + " <" + predicate + "> " + r.get(object) + "\n";
//                        }
//                        resAsUpdate += "}";
//                        logger.debug("Result from " + rp.getEndpoint() + ": \n" + resAsUpdate);
//
//
//                        QueryProcess qp = QueryProcess.create(g);
//                        qp.query(resAsUpdate);
//
//                        Iterator it = g.getEdges().iterator();
//                        while (it.hasNext()) {
//                            results.add((Entity) it.next());
//                        }
////                        System.out.println("");
////                        System.out.println(RDFFormat.create(g));
////                        System.out.println("");
////                        System.out.println(results.size());
//                    }
//                }
//
//
//            } catch (EngineException ex) {
//                ex.printStackTrace();
//            } catch (WebServiceException ex) {
//                ex.printStackTrace();
//            }
//
//        }
//
//        return results;
//    }
//
//    /**
//     * UNUSED with kgram (pas de manipulation des sommets) 
//     * 
//     * @param gNode
//     * @param from
//     * @param qNode
//     * @param env
//     * @return 
//     */
//    @Override
//    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode, Environment env) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * a priori inutile de le propager cote serveur car il sera d�clenche par le moteur local (rien en pratique).
//     * @param qEdge
//     * @param index 
//     */
//    @Override
//    public void initPath(Edge qEdge, int index) {
//    }
//
//    /**
//     * sert au calcul de chemins : enumere les sommets d'un graph.
//     * a propager c�te serveur select ?x where {?x <p>{0} ?y}
//     * 
//     * @param gNode
//     * @param from
//     * @param env
//     * @param exp inutile dans ce cas l�
//     * @param index sert au sens du parcours
//     * @return 
//     */
//    @Override
//    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, List<Regex> exp, int index) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * Dans le cas des chemins
//     * @param gNode
//     * @param from
//     * @param qEdge
//     * @param env ne pas prendre en compte l'env dans le calcul des chemins
//     * @param exp
//     * @param src l'ensemble des 
//     * @param start noeud courant
//     * @param index sens du parcours
//     * @return 
//     */
//    @Override
//    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, Regex exp, Node src, Node start, int index) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * inutile car jamais appel� sur un remoteProducer
//     * @param value
//     * @return 
//     */
//    @Override
//    public Node getNode(Object value) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * inutile car jamais appel� sur un remoteProducer
//     * @param value
//     * @return 
//     */
//    @Override
//    public boolean isBindable(Node node) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * inutile car jamais appel� sur un remoteProducer
//     * @param value
//     * @return 
//     */
//    @Override
//    public List<Node> toNodeList(Object value) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    /**
//     * inutile car jamais appel� sur un remoteProducer
//     * @param value
//     * @return 
//     */
//    @Override
//    public Mappings map(List<Node> qNodes, Object object) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    private List<String> getSparqlFromEdgeRequest(Edge edge, Environment env) {
//
//        ArrayList<String> results = new ArrayList<String>();
//        String sEdge = edge.toString();
//
//        String filter = null;
//
//        //TODO environment handling
//        String sparqlPrefixes = "";
//        if (env.getQuery().getAST() instanceof ASTQuery) {
//            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
//            String source = "";
//            for (Exp exp : ast.getQuery().getBody()) {
//
//                if (exp.isFilter()) {
//                    filter = exp.toString();
//                }
//
//                if (exp.isBGP()) {
//                    System.out.println("");
//                    System.out.println("BGP : " + exp.toString());
//                    System.out.println("");
//                }
//
//                source += exp.toString() + "\n";
//            }
////            logger.debug("generating SPARQL request from: \n" + source);
//
//            //prefix handling
//            NSManager namespaceMgr = ast.getNSM();
//            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
//            while (prefixes.hasMoreElements()) {
//                String p = prefixes.nextElement();
//                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
//            }
//        }
//
//
//        boolean testEnvHandling = false;
//        if (testEnvHandling) {
//            if (env instanceof Memory) {
//                Memory mem = (Memory) env;
//                if (mem.current() != null) {
//                    Iterator<Mapping> mappingIt = mem.current().iterator();
//                    while (mappingIt.hasNext()) {
//                        Mapping mapping = mappingIt.next();
////                    System.out.println("Mapping: " + mapping.toString());
//
//                        String sparql = sparqlPrefixes;
//                        sparql += "select distinct * where { \n";
//                        sparql += "\t " + sEdge + "\n";
//                        if (filter != null) {
//                            sparql += "\t" + filter + "\n";
//                        }
//                        sparql += "}";
//
//                        // is a mapping always constructed as ?x ?e ?y ?
//                        for (int i = 0; i < mapping.getQueryNodes().length; i++) {
//                            String variable = mapping.getQueryNodes()[i].toString();
//                            String value = mapping.getNodes()[i].toString();
//                            logger.debug("Mapping \n[" + variable + ":" + value + "]");
//                            sparql = sparql.replaceAll("\\" + variable, value);
//                            results.add(sparql);
//                        }
//
////                    logger.debug("Handling mapping: \n"+sparql);
//
//                    }
//                }
//            }
//        }
//
//        if (results.isEmpty()) {
//            String sparql = sparqlPrefixes;
//            sparql += "select distinct * where { \n";
//            sparql += "\t " + sEdge + "\n";
////            if (filter != null) {
////                sparql += "\t" + filter + "\n";
////            }
//            sparql += "}";
//            results.add(sparql);
//        }
//
////        logger.debug("generated SPARQL request:");
////        logger.debug(sparql);
//        return results;
//    }
}
