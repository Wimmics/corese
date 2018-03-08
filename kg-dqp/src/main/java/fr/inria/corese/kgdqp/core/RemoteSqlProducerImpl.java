/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.core;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.kgram.api.core.*;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.edge.EdgeImpl;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.RDFFormat;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Ad-hoc mediator handling mappings from some OntoNeuroLOG properties to the
 * SQL databases implementing the NeuroLOG federated SQL schema.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteSqlProducerImpl implements Producer {

    private static Logger logger = LogManager.getLogger(RemoteSqlProducerImpl.class);
    private String url, driver, login, password;
    private Map<String, String> rdfSqlMappings = new HashMap<String, String>();
    private final String sqlHasForName = "{ "
            + "     select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', \n "
            + "         \" SELECT DATASET.DATASET_ID, DATASET.NAME FROM DATASET %VALUE-CONSTRAINTS% \")\n"
            //            + "         \" SELECT Dataset.dataset_id, Dataset.name FROM Dataset %VALUE-CONSTRAINTS% \")\n"
            + "         as (?x, %OBJECT%)) where {} "
            + "} .\n"
            + "{ "
            + "     select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#dataset-IRISA-SS-\",?x)) as %SUBJECT%) where {}"
            //            + "     select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#dataset-\",?x)) as %SUBJECT%) where {}"
            + "}";
    private final String sqlIsReferredToBy = "{ "
            + "     select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', \n"
            + "        \" SELECT DATASET.SUBJECT_ID, DATASET.DATASET_ID FROM DATASET %VALUE-CONSTRAINTS% \")\n"
            //            + "        \" SELECT Dataset.Subject_subject_id, Dataset.dataset_id FROM Dataset %VALUE-CONSTRAINTS% \")\n"
            + "         as (?x, ?y)) where {} "
            + "} .\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-IRISA-SS-\",?x)) as %SUBJECT%) where {} }\n"
            //            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-\",?x)) as %SUBJECT%) where {} }\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#dataset-IRISA-SS-\",?y)) as %OBJECT%) where {} }";
//            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#dataset-\",?y)) as %OBJECT%) where {} }";
    private final String sqlHasForSubjectIdentifier = "{ "
            + "     select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', \n"
            + "        \" SELECT SUBJECT.SUBJECT_ID, SUBJECT.NAME FROM SUBJECT %VALUE-CONSTRAINTS% \")\n"
            //            + "        \" SELECT Subject.subject_id, Subject.subject_common_identifier FROM Subject %VALUE-CONSTRAINTS% \")\n"
            + "         as (?x, ?y)) where {} "
            + "} .\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-IRISA-SS-\",?x)) as %SUBJECT%) where {} }\n"
            //            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-\",?x)) as %SUBJECT%) where {} }\n"
            + "{ select(?y as %OBJECT%) where {} }";
    private final String sqlInvolvesHasPatient = "{ "
            + "     select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', \n"
            + "        \" SELECT REL_SUBJECT_STUDY.STUDY_ID, REL_SUBJECT_STUDY.SUBJECT_ID FROM REL_SUBJECT_STUDY %VALUE-CONSTRAINTS% \")\n"
            //            + "        \" SELECT Subject.subject_id, Subject.subject_common_identifier FROM Subject %VALUE-CONSTRAINTS% \")\n"
            + "         as (?x, ?y)) where {} "
            + "} .\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#study-IRISA-SS-\",?x)) as %SUBJECT%) where {} }\n"
            //            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#study-\",?x)) as %SUBJECT%) where {} }\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-IRISA-SS-\",?y)) as %OBJECT%) where {} }";
//            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-\",?y)) as %OBJECT%) where {} }";

    public RemoteSqlProducerImpl(String url, String driver, String login, String password) {
        this.url = url;
        this.driver = driver;
        this.login = login;
        this.password = password;

        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name", sqlHasForName);
        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#is-referred-to-by", sqlIsReferredToBy);
        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#has-for-subject-identifier", sqlHasForSubjectIdentifier);
        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#involves-as-patient", sqlInvolvesHasPatient);

        for (Entry<String, String> entry : rdfSqlMappings.entrySet()) {
            String sql = entry.getValue();
            sql = sql.replaceAll("db:%DATABASE%", "<" + this.url + ">");
            sql = sql.replaceAll("%DRIVER%", "<" + this.driver + ">");
            sql = sql.replaceAll("%LOGIN%", this.login);
            sql = sql.replaceAll("%PASSWORD%", this.password);

            entry.setValue(sql);
        }

    }


    @Override
    /**
     * propagate to the server ? nothing
     */
    public void setMode(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return
     */
    @Override
    public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return
     */
    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Transforms an EDGE request into a simple SPARQL query pushed to the
     * remote producer. Results are returned through standard web services
     * protocol.
     *
     * @param gNode graph variable if it exists, null otherwise
     * @param from "from named <g>" list
     * @param qEdge edge searched for
     * @param env query execution context (current variable values, etc.)
     * @return an iterator over graph entities
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {
        // si gNode != null et from non vide, alors "from named"
        // si gNode == null et from non vide alors "from"

        String query = getSparqlQuery(qEdge, env);
        Graph resGraph = Graph.create();
        Graph g = Graph.create();

        StopWatch sw = new StopWatch();
        sw.start();

        InputStream is = null;
        try {
            QueryProcess exec = QueryProcess.create(resGraph);

            if (query != null) {
                Mappings map = exec.query(query);

//            logger.info("Received results in " + sw.getTime());

                String sparqlRes = RDFFormat.create(map).toString();
//            System.out.println(XMLFormat.create(map));

                if (sparqlRes != null) {
                    Load l = Load.create(g);
                    is = new ByteArrayInputStream(sparqlRes.getBytes());
                    l.load(is);
//                logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
                }
            }

        } catch (LoadException ex) {
            ex.printStackTrace();
        } catch (EngineException ex) {
            ex.printStackTrace();
        }
//        for (Iterator<Entity> it = g.getEdges().iterator(); it.hasNext();) {
//            Edge e = (Edge) it.next();
//            System.out.println(e);
//        }
//        
        return g.getEdges();
    }

    /**
     * UNUSED with kgram (pas de manipulation des sommets)
     *
     * @param gNode
     * @param from
     * @param qNode
     * @param env
     * @return
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * a priori inutile de le propager cote serveur car il sera declenche par le
     * moteur local (rien en pratique).
     *
     * @param qEdge
     * @param index
     */
    @Override
    public void initPath(Edge qEdge,
            int index) {
    }

    /**
     *
     * @param gNode
     * @param from
     * @param env
     * @param exp inutile dans ce cas la
     * @param index sert au sens du parcours
     * @return
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, List<Regex> exp,
            int index) {
        Graph resGraph = Graph.create();
        return resGraph.getEdges();
    }

    /**
     * Dans le cas des chemins
     *
     * @param gNode
     * @param from
     * @param qEdge
     * @param env ne pas prendre en compte l'env dans le calcul des chemins
     * @param exp
     * @param src l'ensemble des
     * @param start noeud courant
     * @param index sens du parcours
     * @return
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, Regex exp, Node src, Node start, int index) {
        Graph resGraph = Graph.create();
        return resGraph.getEdges();
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public Node getNode(Object value) {
        return null;
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public boolean isBindable(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public List<Node> toNodeList(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public Mappings map(List<Node> qNodes, Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * ?x p ?y FILTER ((?x > 10) && (?z > 10))
     *
     */
    public boolean bound(Edge edge, Filter filter) {
        List<String> vars = new ArrayList<String>();
        if (edge.getNode(0).isVariable()) {
            vars.add(edge.getNode(0).toString());
        }
        if (edge.getNode(1).isVariable()) {
            vars.add(edge.getNode(1).toString());
        }

        List<String> varsFilter = filter.getVariables();
        for (String var : varsFilter) {
            if (!vars.contains(var)) {
                return false;
            }
        }
        return true;
    }

    public String getSparqlQuery(Edge edge, Environment env) {

        //prefix handling
        String sparqlPrefixes = "";
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            for (String p : namespaceMgr.getPrefixes()) {
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        //filter handling
        ArrayList<String> filters = new ArrayList<String>();
        for (Exp exp : env.getQuery().getBody()) {
            if (exp.isFilter()) {
                Filter kgFilter = exp.getFilter();
                if (bound(edge, kgFilter)) {
                    filters.add(((Term) kgFilter).toSparql());
                }
            }
        }

        //binding handling
        Node subject = env.getNode(edge.getNode(0));
        Node object = env.getNode(edge.getNode(1));
        Node predicate = null;

        if (edge.getEdgeVariable() != null) {
            predicate = env.getNode(edge.getEdgeVariable());
        }

        //   
        if (subject == null) {
            subject = edge.getNode(0);
        }
        if (object == null) {
            object = edge.getNode(1);
        }
        if (predicate == null) {
            predicate = edge.getEdgeNode();
        }

        EdgeImpl bEdge = new EdgeImpl();
        bEdge.setEdgeNode(predicate);
        bEdge.setNode(0, subject);
        bEdge.setNode(1, object);

//        logger.info("Bind join ? " + edge.toString() + " -> (" + subject + ", " + predicate + ", " + object + ")");

        String property = edge.getLabel();
        String sql = rdfSqlMappings.get(property);

        if (sql != null) {
            sql = sql.replaceAll("%SUBJECT%", edge.getNode(0).toString());
            sql = sql.replaceAll("%OBJECT%", edge.getNode(1).toString());
        } else {
            return null;
        }

        String valueConstraints = processValueConstraints(edge, bEdge);
        sql = sql.replaceAll("%VALUE-CONSTRAINTS%", valueConstraints);

        String sparql = sparqlPrefixes;
        sparql += "construct  { " + edge.getNode(0).toString() + " <" + property + "> " + edge.getNode(1).toString() + " } \n where { \n"
                + sql;

        if (filters.size() > 0) {
            sparql += "\t  FILTER (\n";
            int i = 0;
            for (String filter : filters) {
                if (i == (filters.size() - 1)) {
                    sparql += "\t\t " + filter + "\n";
                } else {
                    sparql += "\t\t " + filter + "&&\n";
                }
                i++;
            }
            sparql += "\t  )\n";
        }
        sparql += " }";

        return sparql;
    }

    public String processValueConstraints(Edge qEdge, Edge bEdge) {
        String where = "WHERE ";

        // Handling BINDINGS
        if (bEdge.getNode(0).isVariable() && bEdge.getNode(1).isVariable()) {
            return "";
        } else {
            String prop = qEdge.getLabel();
            //+ "         \" SELECT Dataset.dataset_id, Dataset.name FROM Dataset %VALUE-CONSTRAINTS% \")\n"
            if (prop.equals("http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#is-referred-to-by")) {
                if (!bEdge.getNode(0).isVariable()) {
                    where += "DATASET.SUBJECT_ID LIKE '" + extractSubjectId(bEdge.getNode(0)) + "' AND\n";
//                    where += "Dataset.Subject_subject_id LIKE '" + extractSubjectId(bEdge.getNode(0)) + "' AND\n";
                }
                if (!bEdge.getNode(1).isVariable()) {
                    where += "DATASET.DATASET_ID LIKE '" + extractDatasetId(bEdge.getNode(1)) + "' AND\n";
//                    where += "Dataset.dataset_id LIKE '" + extractDatasetId(bEdge.getNode(1)) + "' AND\n";
                }
                //+ "         \" SELECT Dataset.dataset_id, Dataset.name FROM Dataset %VALUE-CONSTRAINTS% \")\n"
            } else if (prop.equals("http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name")) {
                if (!bEdge.getNode(0).isVariable()) {
                    where += "DATASET.DATASET_ID LIKE '" + extractDatasetId(bEdge.getNode(0)) + "' AND\n";
//                    where += "Dataset.dataset_id LIKE '" + extractDatasetId(bEdge.getNode(0)) + "' AND\n";
                }
                if (!bEdge.getNode(1).isVariable()) {
                    where += "DATASET.NAME LIKE '" + bEdge.getNode(1).getValue().toString() + "' AND\n";
//                    where += "Dataset.name LIKE '" + bEdge.getNode(1).getValue().toString() + "' AND\n";
                }
                // + "        \" SELECT Subject.subject_id, Subject.subject_common_identifier FROM Subject %VALUE-CONSTRAINTS% \")\n"
            } else if (prop.equals("http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#has-for-subject-identifier")) {
                if (!bEdge.getNode(0).isVariable()) {
                    where += "SUBJECT.SUBJECT_ID LIKE '" + extractSubjectId(bEdge.getNode(0)) + "' AND\n";
//                    where += "Subject.subject_id LIKE '" + extractSubjectId(bEdge.getNode(0)) + "' AND\n";
                }
                if (!bEdge.getNode(1).isVariable()) {
                    where += "SUBJECT.NAME LIKE '" + bEdge.getNode(1).getValue().toString() + "' AND\n";
//                    where += "Subject.subject_common_identifier LIKE '" + bEdge.getNode(1).getValue().toString() + "' AND\n";
                }
            } else if (prop.equals("http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#involves-as-patient")) {
                if (!bEdge.getNode(0).isVariable()) {
                    where += "REL_SUBJECT_STUDY.STUDY_ID LIKE '" + extractStudyId(bEdge.getNode(0)) + "' AND\n";
//                    where += "Subject.subject_id LIKE '" + extractSubjectId(bEdge.getNode(0)) + "' AND\n";
                }
                if (!bEdge.getNode(1).isVariable()) {
                    where += "REL_SUBJECT_STUDY.SUBJECT_ID LIKE '" + extractSubjectId(bEdge.getNode(1)) + "' AND\n";
//                    where += "Subject.subject_common_identifier LIKE '" + bEdge.getNode(1).getValue().toString() + "' AND\n";
                }

            } else {
                logger.warn("Unsupporting bind join for " + bEdge);
            }
        }

        // Handling FILTERS

        //post-processing the SQL WHERE clause
        if (where.equals("WHERE ")) {
            return "";
        } else if (where.endsWith("AND\n")) {
            where = where.substring(0, where.lastIndexOf("AND\n"));
//            logger.info("generated where clause \n" + where);
            return where;
        }
        logger.info("generated where clause \n" + where);
        return where;
    }

    public String extractDatasetId(Node n) {
//        String url = "<http://neurolog.techlog.anr.fr/data.rdf#dataset-GIN-SS-122>";
//        try {
//            URL testUrl = new URL(n.getValue().toString());
//        } catch (MalformedURLException e) {
//            return n.getValue().toString();
//        }
        String nodeUrl = n.getValue().toString();
//        System.out.println(nodeUrl);
        nodeUrl = nodeUrl.substring(nodeUrl.lastIndexOf("-") + 1);
        nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 1);
//        nodeUrl = nodeUrl.substring(nodeUrl.lastIndexOf("#dataset-") + 9);
        return nodeUrl;
    }

    public String extractSubjectId(Node n) {
//        String url = "<http://neurolog.techlog.anr.fr/data.rdf#dataset-GIN-SS-122>";
//        try {
//            URL testUrl = new URL(n.getValue().toString());
//        } catch (MalformedURLException e) {
//            return n.getValue().toString();
//        }
        String nodeUrl = n.getValue().toString();
//        System.out.println(nodeUrl);
        nodeUrl = nodeUrl.substring(nodeUrl.lastIndexOf("-") + 1);
        nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 1);
//        nodeUrl = nodeUrl.substring(nodeUrl.lastIndexOf("#subject-") + 9);
        return nodeUrl;
    }

    public String extractStudyId(Node n) {
//        String url = "<http://neurolog.techlog.anr.fr/data.rdf#dataset-GIN-SS-122>";
//        try {
//            URL testUrl = new URL(n.getValue().toString());
//        } catch (MalformedURLException e) {
//            return n.getValue().toString();
//        }
        String nodeUrl = n.getValue().toString();
//        System.out.println(nodeUrl);
        nodeUrl = nodeUrl.substring(nodeUrl.lastIndexOf("-") + 1);
        nodeUrl = nodeUrl.substring(0, nodeUrl.length() - 1);
//        nodeUrl = nodeUrl.substring(nodeUrl.lastIndexOf("#subject-") + 9);
        return nodeUrl;
    }

    @Override
    public boolean isProducer(Node node) {
        return false;
    }

 

    @Override
    public Object getGraph() {
       return null;
    }

     @Override
    public int getMode() {
        return Producer.DEFAULT;
    }

    @Override
    public void setGraphNode(Node n) {
    }

    @Override
    public Node getGraphNode() {
        return null;
    }

    @Override
    public Producer getProducer(Node node, Environment env) {
        return null;
    }

    @Override
    public Query getQuery() {
        return null;
    }

    @Override
    public Object getValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatatypeValue getDatatypeValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity copy(Entity ent) {
        return ent;
    }

    @Override
    public void close() {

    }
}
