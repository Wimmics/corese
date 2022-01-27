package fr.inria.corese.compiler.parser;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Triple;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Compiler API for Transformer.
 * Generate target Edge/Node/Filter
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 */

public interface Compiler {

  void setAST(ASTQuery ast);

  boolean isFail();

  Node createNode(String name);

  Node createNode(Atom at);
  
  Node createNode(Atom at, boolean reuse);

  Node getNode();

  Edge getEdge();

  List<Filter> getFilters();

  Collection<Node> getVariables();

  Filter compile(Expression exp) throws EngineException;

  Edge compile(Triple t, boolean insertData);
  // rec==true when compile nested triple in values clause
  Edge compile(Triple t, boolean insertData, boolean rec);

  List<Filter> compileFilter(Expression exp) throws EngineException;

  Regex getRegex(Filter f) throws EngineException ;

  String getMode(Filter f);

  int getMin(Filter f);

  int getMax(Filter f);

  HashMap<String, Node> getVarTable();
  
  void share (Compiler cp);

}
