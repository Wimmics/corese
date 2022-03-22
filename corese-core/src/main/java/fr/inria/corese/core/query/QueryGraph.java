package fr.inria.corese.core.query;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.compiler.parser.Transformer;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.QueryGraphVisitor;
import fr.inria.corese.core.EdgeFactory;
import fr.inria.corese.core.Graph;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;

/**
 * Translate a Graph into a Query Use case: let g = eval(construct {} where {})
 * let q = query(g) eval(q)
 *
 * The target Query may itself be a construct
 *
 * There may be a Visitor that rewrites the Graph on the Fly, e.g. : - bnode to
 * variable - constant to variable - generalize type - eliminate edge
 * 
 */
@Deprecated
public class QueryGraph implements QueryGraphVisitor {

    boolean isDebug = false,
            isConstruct = false;

    Graph graph;
    QueryGraphVisitor visitor;
    EdgeFactory fac;

    QueryGraph(Graph g) {
        graph = g;
        fac = new EdgeFactory(g);
        visitor = this;
    }

    public static QueryGraph create(Graph g) {
        return new QueryGraph(g);
    }

    public void setVisitor(QueryGraphVisitor vis) {
        visitor = vis;
    }

    /**
     * Compile Graph into a BGP Generate a Query
     */
    public Query getQuery() throws EngineException {
        Transformer t = Transformer.create();
        ASTQuery ast = ASTQuery.create();
        ast.setSelectAll(true);
        ast.setBody(BasicGraphPattern.create());
        ast = visitor.visit(ast);
        graph = visitor.visit(graph);

        Exp exp = getExp(graph);
        Query q = Query.create(exp);
        q.setAST(ast);
        q = t.transform(q, ast);
        q.setDebug(isDebug);
        q = visitor.visit(q);

        if (isConstruct()) {
            // TODO: blanks in construct should be renamed
            q.setConstruct(q.getBody());
            q.setConstruct(true);
        }

        return q;
    }

    /**
     * The query is construct {graph} where {graph}
     */
    public void setConstruct(boolean b) {
        isConstruct = b;
    }

    public boolean isConstruct() {
        return isConstruct;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    Exp getExp(Graph g) {
        Exp exp = Exp.create(Exp.AND);

        for (Edge ent : g.getEdges()) {
            Edge e = visitor.visit(ent);
            if (e != null) {
                init(e);
                exp.add(fac.queryEdge(e));
                //exp.add(e);
            }
        }
        return exp;
    }

    /**
     * Set the index of Node to -1 Just in case the graph has already been used
     * as a Query
     */
    void init(Edge edge) {

        for (int i = 0; i < edge.nbNode(); i++) {
            edge.getNode(i).setIndex(-1);
        }

        edge.getEdgeNode().setIndex(-1);
        Node var = edge.getEdgeVariable();

        if (var != null) {
            var.setIndex(-1);
        }
    }

    /**
     * *****************************
     * Visitor
     */
    @Override
    public Query visit(Query q) {
        return q;
    }

    @Override
    public Graph visit(Graph g) {
        return g;
    }

    @Override
    public ASTQuery visit(ASTQuery ast) {
        return ast;
    }

    @Override
    public Edge visit(Edge ent) {
        return ent;
    }

}
