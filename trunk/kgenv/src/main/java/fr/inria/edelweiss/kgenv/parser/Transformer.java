package fr.inria.edelweiss.kgenv.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.*;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.api.core.*;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgram.tool.Message;


/**
 * Compiler of SPARQL AST to KGRAM Exp Query
 * Use Corese SPARQL parser
 * Use an abstract compiler to generate target edge/node/filter implementations
 * 
 * sub query compiled as distinct edge/node to avoid inappropriate 
 * type inference on nodes
 * 
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class Transformer implements ExpType {	
	private static Logger logger = Logger.getLogger(Transformer.class);

	public static final String ROOT = "?_kgram_";
	public static final String THIS = "?this";

	int count = 0;

	CompilerFactory fac;
	Compiler compiler;
	List<QueryVisitor> visit;
	Sorter sort;
	//Table table;
	ASTQuery ast;
	Checker check;
	Hashtable<Edge, Query> table;  

	int ncount = 0, rcount = 0;
	boolean fail = false,
	isSPARQLCompliant = true,
	isSPARQL1 = true;
	String namespaces, base;
        private Dataset dataset;
	BasicGraphPattern pragma;
	
	Transformer(){
		table = new Hashtable<Edge, Query>();
		// new
		fac = new CompilerFacKgram();
		compiler = fac.newInstance();
	}

	Transformer(CompilerFactory f){
		this();
		fac = f;
		compiler = fac.newInstance();
	}

	public static Transformer create(CompilerFactory f){
		return new Transformer(f);
	}
	
	public static Transformer create(){
		return new Transformer();
	}
	
	public void set(Dataset ds){
		if (ds!=null){
                    dataset = ds;
		}
	}
	
	public void set(Sorter s){
		sort = s;
	}
	
	public void add(QueryVisitor v){
		if (visit == null){
			visit = new ArrayList<QueryVisitor>();
		}
		visit.add(v);
	}
	
	public void add(List<QueryVisitor> v){
		if (visit == null){
			visit = new ArrayList<QueryVisitor>();
		}
		visit.addAll(v);
	}
	
	public Query transform(String squery) throws EngineException{
		return transform(squery, false);
	}

	public Query transform(String squery, boolean isRule) throws EngineException{
		
		ast = ASTQuery.create(squery);
		ast.setRule(isRule);
		ast.setDefaultNamespaces(namespaces);
		ast.setDefaultBase(base);
		ast.setSPARQLCompliant(isSPARQLCompliant);
                
                if (dataset != null){
                    ast.setDefaultDataset(dataset);
                }

		ParserSparql1.create(ast).parse();
						
		Query q = transform(ast);
		
		return q;
		
	}
	
	/**
	 * Transform for a (outer) query (not a subquery)
	 */
	public Query transform (ASTQuery ast){
		this.ast = ast;
		ast.setSPARQLCompliant(isSPARQLCompliant);
                if (isSPARQLCompliant){
                    ast.getDataset().complete();                    
                }
		//new
		compiler.setAST(ast);
		
		Pragma p = new Pragma(this, ast);
		if (ast.getPragma() != null){
			p.compile();
		}
		if (pragma != null){
			p.compile(pragma);
		}
		
		// compile describe
		ast.compile();
		
		// type check:
		// check scope for bind()
		ast.validate();
				
		if (visit!=null){
			for (QueryVisitor v : visit){
				v.visit(ast);
			}
		}
		
                template(ast);
                
		Query q = compile(ast);
		q.setRule(ast.isRule());
		
		template(q, ast);
				
		q = transform(q, ast);
		return q;
	}
	
        /**
         * Optimize values in template
         * Insert values in template body in case the ?in variable is bound
         * hence it is more efficient to verify values according to ?in binding
         * instead of testing values blindly 
         * use case:
         * template where { ?in a ?t EXP } values ?t { list of values }
         * rewritten as:
         * template where { ?in a ?t values ?t { list of values } EXP } 
         */
	void template(ASTQuery ast){
            if (! ast.isTemplate()){
               return; 
            }
            fr.inria.acacia.corese.triple.parser.Exp body = ast.getBody();
            if (ast.getValues() != null && 
                    body.size() > 0 && 
                    body instanceof BasicGraphPattern &&
                    body.get(0).isTriple()){
                
                Triple t = body.get(0).getTriple();
                
                if (bound(ast.getValues(), t)){
                    body.add(1, ast.getValues());
                    ast.getValues().setMoved(true);
                }
            }
        }
        
        boolean bound(Values values, Triple t){
            if (! t.getArg(1).isVariable()){
                return false;
            }
            for (Variable var : values.getVariables()){
                if (var.equals(t.getArg(1))){
                    return true;
                }
            }
            return false;
        }
        
	private void template(Query q, ASTQuery ast) {
		if (ast.isTemplate()){
			q.setTemplate(true);
			q.setAllResult(ast.isAllResult());
			if (ast.isTurtle()){
				q.setPragma(Pragma.TURTLE, Pragma.TURTLE);
			}
			if (ast.getSeparator()!=null){
				q.setPragma(Pragma.SEPARATOR, ast.getSeparator());
			}
			if (ast.getName() != null) {
				q.setPragma(Pragma.NAME, ast.getName());
			}
			ast.getTemplateGroup().compile(ast);
			q.setTemplateGroup(Exp.create(FILTER, ast.getTemplateGroup()));
		}
	}
	

	/**
	 * Also used by QueryGraph to compile RDF Graph as a Query
	 */
	public Query transform(Query q, ASTQuery ast){
		//new
		compiler.setAST(ast);
		if (ast.isConstruct() || ast.isDescribe()){
			validate(ast.getInsert(), ast);
			Exp cons = construct(ast);
			q.setConstruct(cons);
			q.setConstruct(true);
		}

		if (ast.isDelete()){
			validate(ast.getDelete(), ast);
			Exp del = delete(ast);
			q.setDelete(del);
			q.setDelete(true);
		}
		
		if (ast.isUpdate()){
			q.setUpdate(true);
		}

		// retrieve select nodes for query:
		complete(q, ast);

		having(q, ast);

		if (ast.isRule()){
			// rule need predicate nodes
			List<Node> list = visit(q.getBody());
			q.setNodeList(list);
		}

		if (compiler.isFail() || fail){
			q.setFail(true);
		}	

		q.setSort(ast.isSorted());
		q.setDebug(ast.isDebug());
		q.setCheck(ast.isCheck());
		q.setRelax(ast.isMore());

		for (Edge edge : table.keySet()){
			q.set(edge, table.get(edge));
		}

		filters(q);
		relax(q);

		if (visit != null){
			for (QueryVisitor v : visit){
				v.visit(q);
			}	
		}

		return q;
	}
	
	
	/**
	 * Generate a new compiler for each (sub) query in order to get fresh new nodes
	 */
	Query compile(ASTQuery ast){		
		Exp ee = compile(ast.getExtBody(), false);
		Query q = Query.create(ee);
		q.setAST(ast);
		// use same compiler
		bindings(q, ast);
		path(q, ast);
		
		return q; 
		
	}
	

	
	/**
	 * subquery is compiled using a new compiler to get fresh new nodes
	 * to prevent type inference on nodes between outer and sub queries
	 */
	Query compileQuery(ASTQuery ast){
		// new
		Compiler save = compiler;
		compiler = fac.newInstance();
		compiler.setAST(ast);
		
		Query q = compile(ast);
		// complete select, order by, group by
		complete(q, ast);
		having(q, ast);

		// bind is compiled as subquery
		q.setBind(ast.isBind());
		q.setRelax(ast.isMore());
		
		if (save != null){
			compiler = save;
		}

		return q;
	}
	
	
	
	/**
	 * Delete/Insert/Construct
	 */
	Exp  compile(ASTQuery ast, fr.inria.acacia.corese.triple.parser.Exp exp){
		Compiler save = compiler;
		compiler = fac.newInstance();
		compiler.setAST(ast);
		Exp ee = compile(exp, false);
		
		if (save != null){
			compiler = save;
		}
		return ee;
	}
	

	

	
	
	/**
	 * Compile service  as a subquery if it is a pattern
	 * and as a subquery if it already is one
	 */
	Exp compileService(Service service){
		Node src = compile(service.getService());
		Exp node = Exp.create(NODE, src);
		
		fr.inria.acacia.corese.triple.parser.Exp body = service.get(0);
		ASTQuery aa;
		
		if (body.isBGP() && body.size()==1 && body.get(0).isQuery()){
			// service body is a subquery
			aa = body.get(0).getQuery();
		}
		else {
			// service body is a pattern
			aa = ast.subCreate();
			aa.setSelectAll(true);
			aa.setBody(body);
		}
		
		Query q = compileQuery(aa);
		q.setService(true);
		q.setSilent(service.isSilent());
		
		Exp exp = Exp.create(SERVICE, node, q);
		exp.setSilent(service.isSilent());
		return exp;
	}
	
	
	Query create(Exp exp){
		Query q = Query.create(exp);
		if (sort!=null){
			q.set(sort);
		}
		return q;
	}


	public boolean isSPARQLCompliant(){
		return isSPARQLCompliant;
	}

	public void setSPARQLCompliant(boolean b){
		isSPARQLCompliant = b;
	}

	public void setNamespaces(String ns){
		namespaces = ns;
	}
	
	public void setPragma(BasicGraphPattern p){
		pragma = p;
	}
	
	public void setBase(String ns){
		base = ns;
	}

	public void setSPARQL1(boolean b){
		isSPARQL1 = b;
	}
	
	@Deprecated
	void bind(ASTQuery ast){
		if (ast.getVariableBindings()!=null){
			Expression exp = ast.bind();
			if (exp == null) return;
			Triple triple = Triple.create(exp);
			ast.getBody().add(triple);
		}
	}
	
	void bindings(Query q, ASTQuery ast){
		
		if (ast.getValues() == null){
			return;
		}

		Exp bind = bindings(ast.getValues());
                
		if (bind != null){
                    if (ast.getValues().isMoved()){
                        q.setTemplateMappings(bind.getMappings());
                    }
                    else {
			q.setMappings(bind.getMappings());
			q.setBindingNodes(bind.getNodeList());
                    }
		}
		else {
			q.setCorrect(false);
			q.addError("Value Bindings: ", "#values != #variables");
		}
	}
	
	Exp bindings(Values values){		   
		List<Node> lNode = bind(values);
		Node[] nodes = getNodes(lNode);

		Mappings lMap = new Mappings();

		for (List<Constant> lVal :  values.getValues()){
			if (values.getVariables().size() != lVal.size()){
				// error: not right number of values
				return null;
			}
			else {
				List<Node> list = bind(lVal);
				Mapping map = create(nodes, list);
				lMap.add(map);
			}
		}
		
		Exp bind = Exp.create(VALUES);
		bind.setNodeList(lNode);
		bind.setMappings(lMap);
		return bind;
	}

	
	List<Node> bind(Values values){

		List<Node> lNode = new ArrayList<Node>();

		for (Variable var : values.getVariables()){
			Node qNode = compiler.createNode(var);
			lNode.add(qNode);
		}
		
		return lNode;
	}
	
	Node[] getNodes(List<Node> lNode){
		Node[] nodes = new Node[lNode.size()];
		int i = 0;
		for (Node node : lNode){
			nodes[i++] = node;
		}
		return nodes;
	}
	
	
	List<Node> bind(List<Constant> lVal){
		List<Node> lNode = new ArrayList<Node>();
		for (Constant val : lVal){
			Node node = null;
			if (val != null) {
				node = compiler.createNode(val);
			}
			lNode.add(node);
		}
		return lNode;
	}

	Mapping create(Node[] lNode, List<Node> lVal){
		Node[] nodes = new Node[lVal.size()];
		int i = 0;
		for (Node node : lVal){
			nodes[i++] = node;
		}
		return Mapping.create(lNode, nodes);
	}
	

	
	Exp  construct(ASTQuery ast){
		return compile(ast, ast.getInsert());
	}
	
	Exp  delete(ASTQuery ast){
		return compile(ast, ast.getDelete());
	}

	public ASTQuery getAST(){
		return ast;
	}

	public Compiler getCompiler(){
		return compiler;
	}


	void complete(Query qCurrent, ASTQuery ast){	
		qCurrent.collect();
		//qCurrent.setSelectFun(select(qCurrent, ast));
		select(qCurrent, ast);		
		qCurrent.setOrderBy(orderBy(qCurrent, ast));
		qCurrent.setGroupBy(groupBy(qCurrent, ast));
		
		qCurrent.setDistinct(ast.isDistinct());
                // generate a DISTINCT(?x) for distinct ?x
		qCurrent.distinct();
		qCurrent.setFrom (nodes(ast.getActualFrom()));
		qCurrent.setNamed(nodes(ast.getActualNamed()));
		
		// sort from uri to speed up verification at query time 
		// Producer may use dichotomy
		qCurrent.setFrom (sort(qCurrent.getFrom()));
		qCurrent.setNamed(sort(qCurrent.getNamed()));
				
		qCurrent.setLimit(Math.min(ast.getMaxResult(), ast.getMaxProjection()));
		qCurrent.setOffset(ast.getOffset());

		qCurrent.setGraphNode(createNode());
		
		if (qCurrent.isCorrect()){
			// check semantics of select vs aggregates and group by
			boolean correct = qCurrent.check();
			if (! correct){
				qCurrent.setCorrect(false);
			}
			else {
				qCurrent.setCorrect(ast.isCorrect());
			}
		}

	}

	void path(Query q, ASTQuery ast){
		if (ast.getRegexTest().size()>0){
			Node node = compiler.createNode(Variable.create(THIS));
			q.setPathNode(node);
		}
		for (Expression test : ast.getRegexTest()){
			// ?x c:isMemberOf[?this != <inria>] + ?y
			Filter f = compile(test);
			q.addPathFilter(f);
		}
	}

	void having(Query q, ASTQuery ast){
		if (ast.getHaving()!=null){
			//Filter having = compile(ast.getHaving());			
			Filter having = compileSelect(ast.getHaving(), ast);			
			q.setHaving(Exp.create(FILTER, having));
		}
	}


	/**
	 * Retrieve/Compute the nodes for the select of qCurrent Query
	 * Nodes may be std Node or  select fun() as ?var node
	 * in this last case we may create a node from scratch for ?var
	 * This function is called 
	 * - once for each subquery 
	 * - once for the global query  
	 */
	List<Exp> select(Query qCurrent, ASTQuery ast){
		List<Exp> select    = new ArrayList<Exp>();
		// list of query nodes created for variables in filter that need
		// an outer node value
		List<Node> lNodes = new ArrayList<Node>();

		if (ast.isSelectAll() || ast.isConstruct()){
			// select *
			// get nodes from query nodes and edges
			select = qCurrent.getSelectNodesExp();
		}

		qCurrent.setSelectFun(select);
		
		for (Variable var : ast.getSelectVar()){	
			// retrieve var node from query
			String varName = var.getName();
			Node node = getNode(qCurrent, var);
			Exp exp = Exp.create(NODE, node);
			
			// process filter if any
			Expression ee = ast.getExpression(varName);
			
			if (ee != null){
				// select fun() as var
				Filter f = compileSelect(ee, ast);

				if (f != null){
					// select fun() as var
					exp.setFilter(f);
					checkFilterVariables(qCurrent, f, select, lNodes);
					function(qCurrent, exp, var);
					aggregate(qCurrent, exp, ee, select);					
				}
			}

			// TODO: check var in select * to avoid duplicates
			
			//select.add(exp);
			add(select, exp);
			
			if (lNodes.contains(exp.getNode())){
				// undef variable of former exp is current exp as var
				lNodes.remove(exp.getNode());
			}
		}	

		for (Node node : lNodes){
			// additional variables for exp in select (exp as var)
			Exp exp = Exp.create(NODE, node);
			exp.status(true);
			select.add(exp);
		}
		
		qCurrent.setSelectWithExp(select);
		return select;
	}
	
	/**
	 * select * (exp as var)
	 * if var is already in select *, add exp to var
	 */
	void add(List<Exp> select, Exp exp){
		boolean found = false;
		
		for (Exp e : select){
			if (e.getNode().same(exp.getNode())){
				if (exp.getFilter() != null){
					e.setFilter(exp.getFilter());
				}
				found = true;
				break;
			}
		}
		
		if (! found){
			select.add(exp);
		}
	}
	
	
	void aggregate(Query qCurrent, Exp exp, Expression ee, List<Exp> list){
		if (exp.isAggregate()){
			// process  min(?l, groupBy(?x, ?y))
			extendAggregate(qCurrent, exp, ee);
		}
		else {
			// check if exp has a variable that is computed by a previous aggregate
			// if yes, exp is also considered as an aggregate
			checkAggregate(exp, list);
		}
	}
	
	
	/**
	 * use case: select (count(?x) as ?c) (?c + ?c as ?d)
	 * check that ?c is an aggregate variable
	 * set ?c + ?c as aggregate
	 */
	void checkAggregate(Exp exp, List<Exp> select){
		List<String> list = exp.getFilter().getVariables();
		
		for (Exp ee : select){
			if (ee.isAggregate()){
				String name = ee.getNode().getLabel();
				if (list.contains(name)){
					exp.setAggregate(true);
					break;
				}
			}
		}
	}
	
	
	/**
	 * min(?l, groupBy(?x, ?y))
	 */
	void extendAggregate(Query qCurrent, Exp exp, Expression ee){
		if (ee.isAggregate() && ee.arity()>1){
			Expression g = ee.getArg(1);
			if (g.oper() == ExprType.GROUPBY){
				List<Exp> ob = orderBy(qCurrent, g.getArgs(), ast);
				exp.setExpGroupBy(ob);
			}
		}
	}

	
	Node getNode(Query qCurrent, Variable var){
		Node node = getProperAndSubSelectNode(qCurrent, var.getName());
		if (node == null){
			node = compiler.createNode(var);
		}
		else {
			ASTQuery ast = getAST(qCurrent);
			//ast.addError("Variable already defined: ", var);
		}
		return node;
	}
	
	ASTQuery getAST(Query q){
		return (ASTQuery) q.getAST();
	}

	Node getProperAndSubSelectNode(Query q, String name){
		Node node;
		if (Query.test) node = q.getSelectNodes(name);
		else node = q.getProperAndSubSelectNode(name);
		return node;
	}

	/**
	 * If filter isFunctionnal()
	 * create it's query node list
	 */
	void function(Query qCurrent, Exp exp, Variable var){
		if (exp.getFilter().isFunctional()){
			if (var.getVariableList()!=null){
				// sql() as (?x, ?y)
				for (Variable vv : var.getVariableList()){
					Node qNode = getNode(qCurrent, vv);
					exp.addNode(qNode);
				}
			}
			else {
				exp.addNode(exp.getNode());
			}
		}
	}

	/**
	 *  Check that variables in filter have corresponding proper node 
	 *  otherwise create a Node to import value from outer query
	 * @param query
	 * @param f
	 */
	void checkFilterVariables(Query query, Filter f, List<Exp> select, List<Node> lNodes){
		List<String> lVar = f.getVariables();
		for (String name : lVar){
			Node node = getProperAndSubSelectNode(query, name);
			if (node == null){
				if (! containsExp(select, name) && ! containsNode(lNodes, name)){
					node = compiler.createNode(name);
					lNodes.add(node);
				}
			}
		}
	}



	boolean containsExp(List<Exp> lExp, String name){
		for (Exp exp : lExp){
			if (exp.getNode().getLabel().equals(name)){
				return true;
			}		
		}
		return false;
	}

	boolean containsNode(List<Node> lNode, String name){
		for (Node node : lNode){
			if (node.getLabel().equals(name)){
				return true;
			}		
		}
		return false;
	}


	List<Exp> orderBy(Query qCurrent, ASTQuery ast){
		List<Exp> order = orderBy(qCurrent, ast.getSort(), ast);
		if (order.size()>0){
			int n = 0;
			for (boolean b : ast.getReverse()){
				order.get(n).status(b);
				n++;
			}
		}
		return order;
	}


	List<Exp> groupBy(Query qCurrent, ASTQuery ast){
		List<Exp> list = orderBy(qCurrent,  ast.getGroupBy(), ast);
		qCurrent.setConnect(ast.isConnex());
		return list;
	}


	List<Exp> orderBy(Query qCurrent,  List<Expression> input, ASTQuery ast){
		List<Exp> list = new ArrayList<Exp>();
		
		for (Expression ee : input){
			if (ee.isVariable()){
				Exp exp = qCurrent.getSelectExp(ee.getName());
				Node node;
				
				if (exp != null){
					node = exp.getNode();
				}
				else {
					node = getProperAndSubSelectNode(qCurrent, ee.getName());
				}

				if (node == null){
					ast.addError("OrderBy GroupBy Undefined exp: ", ee);
					node = compiler.createNode(ee.getName());
				}
				Exp e = Exp.create(NODE, node);

				if (exp!=null  && exp.isAggregate()){
					// order by ?count
					e.setAggregate(true);
				}
				list.add(e);
				//}
			}
			else {
				// order by fun(?x)
				// TODO: check rewrite fun() as var
				Filter f = compile(ee);
				Node node = createNode(); 
				Exp exp = Exp.create(NODE, node);
				exp.setFilter(f);
				list.add(exp);
			}
		}
		return list;
	}

	/**
	 * Create a fake query node 
	 */
	Node createNode(){
		String name = ROOT + count++;
		Node node = compiler.createNode(name);
		return node;
	}

	List<Node> nodes(List<Constant> from){
		List<Node> nodes = new ArrayList<Node>();
		for (Constant cst : from){
			nodes.add(new NodeImpl(cst));
		}
		return nodes;
	}
	
	List<Node> sort(List<Node> list){
		Collections.sort(list, new Comparator<Node>(){
			public int compare(Node o1, Node o2) {
				return o1.compare(o2);			
			}
		});
		return list;
	}


	/**
	 * Compile AST statements into KGRAM statements
	 * Compile triple into Edge, filter into Filter
	 */
	Exp compile(fr.inria.acacia.corese.triple.parser.Exp query, boolean opt){
		return compile(query, opt, 0);
	}	
	
	
	Exp compile(fr.inria.acacia.corese.triple.parser.Exp query, boolean opt, int level){

		Exp exp = null;
		int type = getType(query);
		opt = opt || isOption(type);

		switch(type){

		case FILTER:
			exp = compileFilter((Triple) query, opt);
			break;

		case EDGE:
			exp = compileEdge((Triple) query, opt);
			break;

		case QUERY:
			exp = compileQuery(query.getQuery());
			break;

		case SERVICE: 
			exp = compileService((Service) query);		
			break;

		case VALUES:			
			exp = bindings((Values) query);
			if (exp == null){
				// TODO:
				logger.error("** Value Bindings: #values != #variables");
				return null;
			}
			break;

		default:

			/**************************
			 * 
			 * Compile Body
			 * 
			 **************************/

			exp = Exp.create(cpType(type));

			boolean hasBind = false;

			for (fr.inria.acacia.corese.triple.parser.Exp ee : query.getBody()){

				Exp tmp = compile(ee, opt, level+1);

				if (tmp != null){				

					if (tmp.isQuery() && tmp.getQuery().isBind()){
						hasBind = true;
					}

					if (ee.isScope()){								
						// add AND as a whole
						exp.add(tmp);

					}
                                        else if (isJoinable(ee)){
                                            exp.join(tmp);
                                        }
					else {
						// add elements of AND one by one
						exp.insert(tmp);
					}
				}
			}

			// PRAGMA: do it after loop above to have filter compiled
			query.validateBlank(ast);		

			exp = complete(exp, query, opt);
		}

		return exp;

	}
	

	Exp compileEdge(Triple tt, boolean opt){
		Edge r = compiler.compile(tt);
		Exp exp = Exp.create(EDGE, r);

		if (tt.isXPath()){
			// deprecated ?x xpath() ?y
			exp.setType(EVAL);
			Filter xpath = compiler.compile(tt.getXPath());
			exp.setFilter(xpath);
		}
		else if (tt.isPath()){
			exp.setType(PATH);
			Expression regex = tt.getRegex();
			if (regex == null){
				// deprecated: there may be a match($path, regex)
			}
			else {
				regex.compile(ast);
				exp.setRegex(regex);
			}
			exp.setObject(tt.getMode());
		}
		else if (ast.isCheck()) {
			check(tt, r);
		}

		return exp;
	}

	
	/**
	 * Complete compilation
	 */
	Exp complete(Exp exp, fr.inria.acacia.corese.triple.parser.Exp query, boolean opt){
		// complete path (deprecated)
		path(exp);

		switch (getType(query)){

		case MINUS:
			// add a fake graph node 
			// use case:
			// graph ?g {PAT minus {PAT}}
			exp.setNode(createNode());
			break;

		case GRAPH:
			// bind the graph variable/uri
			// use case: graph ?g {}
			// no edge in graph pattern

			Source srcexp = (Source) query;
			Node src = compile(srcexp.getSource());
			// create a NODE kgram expression for graph ?g
			Exp node = Exp.create(NODE, src);
			Exp gnode = Exp.create(GRAPHNODE, node);
			exp.add(0, gnode);
			break;


		case NOT:
			exp = Exp.create(NOT, exp);
			break;

		case FORALL:
			Exp first = Exp.create(AND);
			Forall fa = (Forall) query;

			for (fr.inria.acacia.corese.triple.parser.Exp ee : fa.getFirst().getBody()){
				Exp tmp = compile(ee, opt);
				first.add(tmp);
			}

			exp = Exp.create(FORALL, first, exp);
			break;	

		case IF:
			IfThenElse ee = (IfThenElse) query;
			Exp e1 = compile(ee.getIf(), opt);
			Exp e2 = compile(ee.getThen(), opt);
			Exp e3 = null;
			if (ee.getElse()!=null){
				e3 = compile(ee.getElse(), opt);
			}
			exp.add(e1);
			exp.add(e2);
			if (e3 !=null){
				exp.add(e3);
			}
			break;
		}
		
		return exp;
	}

	
	Exp compileFilter(Triple triple, boolean opt){
		List<Filter> qvec = compiler.compileFilter(triple);
		Exp exp;
		
		if (qvec.size()==1){
			exp = Exp.create(FILTER, qvec.get(0));
			compileExist(qvec.get(0).getExp(), opt);
		}
		else {
			exp =  Exp.create(AND);
			for (Filter qm : qvec){
				Exp f = Exp.create(FILTER, qm);
				compileExist(qm.getExp(), opt);
				exp.add(f);
			}
		}
		return exp;
	}
	
	
	Node compile(Atom at){
		return compiler.createNode(at);
	}
		
	
	void pop(Exp exp){
		List<Exp> list = new ArrayList<Exp>();
		for (Exp ee : exp){
			if (ee.isQuery() && ee.getQuery().isBind()){
				list.add(Exp.create(POP, ee));
			}
		}
		for (Exp ee : list){
			exp.insert(ee);
		}
	}

	
	
	/**
	 * Rewrite fun() as ?var in exp
	 * Compile exists {}
	 */
	Filter compile(Expression exp){
		Filter f = compiler.compile(exp);
		compileExist(f.getExp(), false);
		return f;
	}
	
	/**
	 * Do not rewrite fun() as var
	 */
	Filter compileSelect(Expression exp, ASTQuery ast){
		Filter f = exp.compile(ast);
		compileExist(f.getExp(), false);
		return f;
	}
	

	/**
	 * filter(exist {PAT})
	 */
	void compileExist(Expr exp, boolean opt){
		if (exp.oper() == ExprType.EXIST){
			Term term = (Term) exp;
			Exp pat = compile(term.getExist(), opt);
			term.setPattern(pat);
		}
		else {
			for (Expr ee : exp.getExpList()){
				compileExist(ee, opt);
			}
		}
	}


	/**
	 * Assign  pathLength($path) <= 10 to its path
	 */
	void path(Exp exp){		
		for (Exp ee : exp){
			if (ee.isPath()){
				for (Exp ff : exp){
					if (ff.isFilter()){
						processPath(ee, ff);
					}
				}
				if (ee.getRegex() == null){
					String name = ee.getEdge().getLabel();
					Term star = Term.function(Term.STAR, Constant.create(name));
					star.compile(ast);
					ee.setRegex(star);
				}
			}
		}
	}




	/**
	 * Check if filter f concerns path e
	 * for regex, mode, min, max
	 * store them in Exp e
	 */
	void processPath(Exp exp, Exp ef){
		Filter f = ef.getFilter();
		Edge e = exp.getEdge();
		Node n = e.getEdgeVariable();

		List<String> lVar = f.getVariables();
		if (lVar.size()==0) return ;
		if (n==null) return;
		if (! n.getLabel().equals(lVar.get(0))) return ;

		Regex regex = compiler.getRegex(f);

		if (regex != null && exp.getRegex()==null){
			// mode: i d s
			String mode = compiler.getMode(f);
			if (mode != null){
				exp.setObject(mode);
				if (mode.indexOf("i")!=-1){
					regex = Term.function(Term.SEINV, ((Expression)regex));
				}
			}
			((Expression)regex).compile(ast);
			exp.setRegex(regex);
		}
		else {
			if (compiler.getMin(f) != -1){
				exp.setMin(compiler.getMin(f));
			}
			if (compiler.getMax(f) != -1){
				exp.setMax(compiler.getMax(f));
			}
		}
	}

	boolean isOption(int type){
		switch (type){
		case OPTION:
		case OPTIONAL:
		case UNION:
		case MINUS: return true;
		
		default: return false;
		}
	}
	
	int getType(fr.inria.acacia.corese.triple.parser.Exp query){
		if (query.isFilter()){
			return FILTER;
		}
		else if (query.isTriple()){
			return EDGE;
		}
		else if (query.isUnion()){
			return UNION;
		}
		else if (query.isJoin()){
			return JOIN;
		}
		else if (query.isOption()){
			return OPTION;
		} 
                else if (query.isOptional()){
			return OPTIONAL;
		} 
		else if (query.isMinus()){
			return MINUS;
		} 
		else if (query.isGraph()){
			return GRAPH;
		} 
		else if (query.isService()){
			return SERVICE;
		} 
		else if (query.isQuery()){
			return QUERY;
		} 
		else if (query.isExist()){
			return EXIST;
		} 
		else if (query.isForall()){
			return FORALL;
		} 
		else if (query.isIfThenElse()){
			return IF;
		} 
		else if (query.isNegation()){
			return NOT;
		} 
		else if (query.isValues()){
			return VALUES;
		} 
		else if (query.isAnd()){
			return AND;
		} 
		else return EMPTY;
	}

	int cpType(int type){
		switch (type){
		case FORALL:
		case NOT: return AND;
		default: return  type;
		}
	}


	/***************************************/

	
	/**
	 * Generate a complementary Query that checks:
	 * definition of class/property
	 */
	void check(Triple tt, Edge edge){
		ASTQuery aa = new Checker(ast).check(tt);
		if (aa != null){
			Transformer tr = Transformer.create();
			Query qq = tr.transform(aa);
			add(edge, qq);
		}
	}
	
	void add(Edge edge, Query query){
		table.put(edge, query);
	}
	
	/**
	 * Generate predefined system filters that may be used by kgram
	 * Filters are stored in a table, we can have several predefined filters
	 * pathNode() generate a blank node for each path (PathFinder)
	 */
	void filters(Query q){
		ASTQuery ast = (ASTQuery) q.getAST();
		
		Term t = Term.function(Processor.PATHNODE);
		q.setFilter(Query.PATHNODE, t.compile(ast));
	}
	
	void relax(Query q){
		ASTQuery ast = (ASTQuery) q.getAST();
		for (Expression exp : ast.getRelax()){
			if (exp.isConstant()){
				Constant p = exp.getConstant();
				Node n = compiler.createNode(p);
				q.addRelax(n);
			}
		}
	}
	
	/*********************************************
	 * 
	 * Get Predicate Nodes for Rule
	 * 
	 ********************************************/
	
	List<Node> visit(Exp exp){
		ArrayList<Node> list  = new ArrayList<Node>();
		visit(exp, list);
		//System.out.println("** T: " + list);
		return list;
	}

	/**
	 * Return predicate nodes of this exp:
	 * edge, path regex and constraints, filter exists, query select having
	 * TODO: query order|group by exists
	 */
	void visit(Exp exp, List<Node> list){
		switch (exp.type()){
		
		case EDGE: 
			Node pred = exp.getEdge().getEdgeNode();
			if (! list.contains(pred)) {
				list.add(exp.getEdge().getEdgeNode());
			}
			break;

		case PATH: 
			visitRegex((Expression) exp.getRegex(), list);
			break;
			
		case FILTER:
			visit(exp.getFilter().getExp(), list);
			break;
				
			
		case QUERY:
			Query q = exp.getQuery();
			
			for (Exp ee : q.getSelectFun()){
				if (ee.getFilter()!=null){
					visit(ee.getFilter().getExp(), list);
				}
			}
			
			if (q.getHaving()!=null){
				visit(q.getHaving().getFilter().getExp(), list);
			}			
			// continue
			
		default: 
			for (Exp ee : exp.getExpList()){
				visit(ee, list);
			}
			
		}
	}
	
	
	/**
	 * exp is a Regex
	 * return its predicates
	 */
	void visitRegex(Expression exp, List<Node> list){
		if (exp.isConstant()){
			Node node = compiler.createNode(exp.getConstant());
			list.add(node);
		}
		else if (exp.isTerm() && exp.isTest()){
			// path @[ a foaf:Person ]
			Expression ee = exp.getExpr();
			visit(ee, list);
		}
		else if (exp.isTerm() && exp.isNot()){
			// ! p is a problem because we do not know the predicate nodes ...
			// let's return top level property, it subsumes all properties
			list.clear();
			Node node = compiler.createNode(ASTQuery.getRootPropertyURI());
			list.add(node);	
		}
		else {
			for (Expression ee : exp.getArgs()){
				visitRegex(ee, list);
			}
		}
	}
	
	/**
	 * Filter: check exists {}
	 */
	void visit(Expr exp, List<Node> list){
		for (Expr ee : exp.getExpList()){
			visit(ee, list);
		}
		if (exp.oper() == ExprType.EXIST){
			visit((Exp) exp.getPattern(), list);
		}
	}
	
	
	
	/*************************************************************/
	
	
	
	/**
	 * check unbound variable in construct/insert/delete
	 */
	boolean validate(fr.inria.acacia.corese.triple.parser.Exp exp, ASTQuery ast){
		boolean suc = true;
		
		for (fr.inria.acacia.corese.triple.parser.Exp ee : exp.getBody()){
			boolean b = true;
			
			if (ee.isTriple()){
				b = validate(ee.getTriple(), ast);
			}
			else if (ee.isGraph()){
				b = validate((Source) ee, ast);
			}
			else {
				b = validate(ee, ast);
			}
			
			suc = suc && b;
		}
		
		return suc;
	}
	
	
	boolean validate(Source exp, ASTQuery ast){
		boolean suc = validate(exp.getSource(), ast);
		
		for (fr.inria.acacia.corese.triple.parser.Exp ee : exp.getBody()){
			suc =  validate(ee, ast) && suc;
		}
		
		return suc;
	}
	
	
	boolean validate(Atom at, ASTQuery ast){
		if (at.isVariable() && 
				! at.isBlankNode() &&
				! ast.isSelectAllVar(at.getVariable())){
			ast.addError(Message.get(Message.UNDEF_VAR) + 
					ast.getUpdateTitle() + ": " , at.getLabel());
			return false;
		}

		return true;
	}
	
	
	boolean validate(Triple t, ASTQuery ast){
		boolean suc = validate(t.getSubject(), ast) ;
		suc =  validate(t.getObject(), ast) && suc;
		
		Variable var = t.getVariable();
		if (var != null){
			suc = validate(var, ast) && suc;
		}
		
		return suc;
	}

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    private boolean isJoinable(fr.inria.acacia.corese.triple.parser.Exp ee) {
        return ee.isBGP() || ee.isUnion() ;
    }

	

}
