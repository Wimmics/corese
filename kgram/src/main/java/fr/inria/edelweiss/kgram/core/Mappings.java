package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.core.Loopable;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.event.EventManager;

/*
 * Manage list of Mapping, result of a query
 * 
 * process select distinct
 * process group by, order by, limit offset, aggregates, having(?count>50)
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 */
public class Mappings  extends PointerObject
implements Comparator<Mapping> , Iterable<Mapping> , Loopable
{
	private static final String NL = System.getProperty("line.separator");;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int SELECT = -1;
	private static int HAVING  = -2;



	List<Node> select;
	boolean isDistinct = false,
	isValid = true,
	hasEvent = false,
	// if true, store all Mapping of the group
	isListGroup = false;
        boolean sortWithDesc = true;
	Query query;
	List<Mapping>  list, reject;
	private List<Entity> insert;
	private List<Entity> delete;
	Group group, distinct;
	Node fake;
	Object object;
        Eval eval;
	private Object graph;
	private int nbsolutions = 0;

	EventManager manager;
	
	// SPARQL: -1 (unbound first)
	// Corese order: 1 (unbound last)
	int unbound = -1;
        int count = 0;
	private int nbDelete = 0;
	private int nbInsert = 0;
	private Node templateResult;
        private boolean isFake = false;
	


	public Mappings(){
		list = new ArrayList<Mapping> ();
	}
	
	Mappings(Mapping map){
		this();
		add(map);
	}

	Mappings(Query q){
		this();
		query = q;
	}

	void setEventManager(EventManager man){
		manager = man;
		hasEvent = true;
	}
	
	public static Mappings create(Query q){
		return create(q, false);
	}
        
        public static Mappings create(Query q, boolean subEval){
		Mappings lMap = new Mappings(q); 
		lMap.init(q, subEval);
		return lMap;
	}
        
        @Override
        public Iterable getLoop(){
            return this;
        }
	
	void init(Query q, boolean subEval){
		isDistinct  = ! subEval && q.isDistinct();
		isListGroup = q.isListGroup();
		setSelect(q.getSelect());
                
		if (isDistinct){
			distinct = group(q.getSelectFun());
			distinct.setDistinct(true);
			distinct.setDuplicate(q.isDistribute());
		}
//                else if (q.isRule() 
//                        && q.getConstructNodes() != null
//                        && q.getConstructNodes().size() > 0){
//                    // construct where
//                    // simulate distinct * on construct variables
//                    isDistinct = true;
//                    setSelect(q.getConstructNodes());
//                    distinct = Group.create(q.getConstructNodes());
//                    distinct.setDistinct(true);
//                    distinct.setDuplicate(q.isDistribute());
//                }
	}
        
        int count(){
            return count;
        }
        
        void setCount(int n){
            count = n;
        }
	
	public void add(Mapping m){
		list.add(m);
	}
        
        public void reject(Mapping m){
            if (reject == null){
                reject = new ArrayList();
            }
            reject.add(m);
        }
        
        void complete(){
            if (reject != null){
                for (Mapping m : reject){
                    list.remove(m);
                }
            }
        }
	
	List<Mapping> getList(){
		return list;
	}
	
	void setList(List<Mapping> l){
		list = l;
	}
	
	public void add(Mappings lm){
		list.addAll(lm.getList());
	}
	
	public Iterator<Mapping> iterator(){
		return list.iterator();
	}
	
	public int size(){
		return list.size();
	}
	
	public Mapping get(int n){
		return list.get(n);
	}
	
	void remove(int n){
		list.remove(n);
	}
	
	public void clear(){
		list.clear();
	}
	
	public Query getQuery(){
		return query;
	}
	
	public void setQuery(Query q){
		query = q;
	}
	
	public void setObject(Object o){
		object = o;
	}
	
	public Object getObject(){
		return object;
	}

	public String toString(){
		return toString(false);
	}
	
	public String toString(boolean all){
		StringBuffer sb = new StringBuffer();
		int i = 1;
		boolean isSelect = select != null && ! all;
		for (Mapping map : this){
			String str = ((i < 10) ? "0" : "") + i + " ";
			sb.append(str);
			
			if (isSelect){
				for (Node qNode : select){
					print(map, qNode, sb);
				}
			}
			else {
				for (Node qNode : map.getQueryNodes()){
					print(map, qNode, sb);
				}
			}
			
			i++;
			sb.append(NL);
		}
		return sb.toString();
	}
	
	
	void print(Mapping map, Node qNode, StringBuffer sb){
		Node node = map.getNode(qNode);
		if (node != null){
			sb.append(qNode);
			sb.append(" = ");
			sb.append(node);
                        if (node.getObject() != null && node.getObject() != this &&
                                (node.getObject() instanceof Mappings || node.getObject() instanceof Mapping)){
                            sb.append(" : ");
                            sb.append(node.getObject().toString());
                        }
			sb.append("; ");
		}
	}

	public List<Node> getSelect(){
		return select;
	}
	
	public Object getValue(Node qNode){
            if (size() == 0) {
                return null;
            }
            Mapping map = get(0);
            return map.getValue(qNode);
	}
	
	public Node getNode(String var){
		if (size() == 0) {
                    return null;
                }
		Mapping map = get(0);
		return map.getNode(var);
	}
        
        public Object getNodeObject(String var){
            if (size() == 0) {
                return null;
            }
            return get(0).getNodeObject(var);
        }
        
        public Node getNode(Node var){
		if (size() == 0) {
                    return null;
                }
		Mapping map = get(0);
		return map.getNode(var);
	}
        
        public Node getQueryNode(String var){
		if (size() == 0){
                    return null;
                }
		Mapping map = get(0);
		return map.getQueryNode(var);
	}
	
	public Object getValue(String var){
		Node node = getNode(var);
		if (node == null){
                    return null;
                }
		return node.getValue();
	}
        
        public Object getValue(String var, int n){
            return getValue(var);
        }

	void setSelect(List<Node> nodes){
		select = nodes;
	}
        
        public void setSelect(Node node){
            select = new ArrayList<Node>(1);
            select.add(node);
	}
        
        /**
         * use case:
         * bind(sparql('select ?x ?y where { ... }') as (?z, ?t))
         * rename ?x as ?z and ?y as ?t in all Mapping
         * as well as in Mappings select
         * 
         */
        public void setNodes(List<Node> nodes){
            if (getSelect() != null){
                for (Mapping map : this) {
                    map.rename(getSelect(), nodes);
                }
                setSelect(nodes);
            }
            else  for (Mapping map : this) {
                map.setNodes(nodes);
            }
        }
        
        public void fixQueryNodes(Query q){
            for (Mapping m : this){
                m.fixQueryNodes(q);
            }
        }

	/**
	 * select distinct 
	 * in case of aggregates, accept Mapping now, distinct will be computed below
	 */
	void submit(Mapping a){
		if (a == null){
			return;
		}
		if (query.isAggregate() || accept(a)){
			add(a);
		}
	}
	
	/**
	 * Used for distinct on aggregates
	 */
	void submit2(Mapping a){
		if (query.isAggregate()){
			 if (accept(a)){
				 add(a);
			 }
		}
		else {
			add(a);
		}
	}

	boolean accept(Node node){
		return (distinct == null) ? true : distinct.accept(node);
	}

	boolean accept(Mapping r){
		if (select.size()==0) return true;
		if (isDistinct){
			return distinct.isDistinct(r);
		}
		return true;
	}

	void setValid(boolean b){
		isValid = b;
	}

	boolean isValid(){
		return isValid;
	}


	boolean same(Node n1, Node n2){
		if (n1 == null){
			return n2 == null;
		}
		else if (n2 == null){
			return false;
		}
		else return n1.same(n2);
	}

        void sort(Eval eval){
            this.eval = eval;
            Collections.sort(list, this);
            this.eval = null;
	}
        
	void sort(){
		Collections.sort(list, this);
	}

        /**
         * 
         * Sort according to node
         */
        void sort(Eval eval, Node node){ 
            this.eval = eval;
            sortWithDesc = false;
            for (Mapping m : this){
                m.setOrderBy(m.getNode(node));
            }
            sort();
            this.eval = null;
        }
        
        int find(Node node, Node qnode){
            return find(node, qnode, 0, size()-1);
        }
        
        int find(Node n2, Node qnode, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
                        Node n1 = list.get(mid).getNode(qnode);
			int res = compare(n1, n2); 
			if (res >= 0) {
				return find(n2, qnode, first, mid);
			}
			else {
				return find(n2, qnode, mid+1, last); 
			}
		}		
	}
        
      int compare(Node n1, Node n2) {
        int res = 0;
        if (n1 != null && n2 != null) { // sort ?x
            res = n1.compare(n2);
        } //      unbound 
        else if (n1 == null) { // unbound var
            if (n2 == null) {
                res = 0;
            } else {
                res = unbound;
            }
        } else if (n2 == null) {
            res = -unbound;
        } else {
            res = 0;
        }
        return res;
    }
        
      int comparator(Node n1, Node n2){
          if (eval != null){
              return eval.compare(n1, n2);
          }
          return n1.compare(n2);
      }

	public int compare(Mapping r1, Mapping r2) {
		Node[] order1 = r1.getOrderBy();
		Node[] order2 = r2.getOrderBy();

		List<Exp> orderBy = query.getOrderBy();

		int res = 0;
		for (int i = 0; i < order1.length && i < order2.length && res == 0; i++) {

			if (order1[i] != null && order2[i] != null) { // sort ?x
				res = comparator(order1[i], order2[i]);
			}
			//      unbound 
			else if (order1[i] == null) { // unbound var
				if (order2[i] == null){
					res = 0;
				}
				else {
					res = unbound;
				}
			}
			else if (order2[i] == null){
				res = - unbound;
			}
			else {
				res = 0;
			}
			
			if (! orderBy.isEmpty() && orderBy.get(i).status() && sortWithDesc){ 
				res = desc(res);
			}
                        
		}
		return res;
	}

	int desc(int i){
		if (i == 0) return 0;
		else if (i < 0) return +1;
		else return -1;
	}
	
	/***********************************************************
	 * 
	 * 	Aggregates
	 * 
	 * 1. select [distinct] var where
	 * 2. group by
	 * 3. count/min/max as var
	 * 4. order by 
	 * 5. limit offset
	 * 
	 * group by with aggregate return one Mapping per group 
	 * where the mapping hold the result of the aggregate
	 * 
	 */



	/**
	 *  order by
	 *  offset
	 */
	void complete(Eval eval){
		if (query.getOrderBy().size()>0){
			sort(eval);
		}
		if (query.getOffset() > 0){
			// skip offset
			// TODO: optimize this
			for (int i=0; i<query.getOffset() && size()>0; i++){
				remove(0);
			}
		}
		while (size() > query.getLimit()){
			remove(size()-1);
		}             
	}




	/** 
	 * select count(?doc) as ?count
	 * group by ?person ?date
	 * order by ?count
	 * having(?count > 100)
	 * TODO:
	 * optimize this because we enumerate all Mappings for each kind of aggregate
	 * we could enumerate Mappings once and compute all aggregates for each map
	 */
	void aggregate(Evaluator evaluator, Memory memory, Producer p){
		aggregate(query, evaluator, memory, p, true);
	}
	
	public void aggregate(Query qq, Evaluator evaluator, Memory memory, Producer p){
		aggregate(qq, evaluator, memory, p, false);
	}
	
	void aggregate(Query qq, Evaluator evaluator, Memory memory, Producer p, boolean isFinish){
		
		if (size() == 0){
			if (qq.isAggregate()){
				// SPARQL test cases requires that aggregate on empty result set return one empty result ...
				add(new Mapping());
                                isFake = true;
			}
			return ;
		}
		
		boolean isEvent = hasEvent;

		// select (count(?n) as ?count)
		aggregate(evaluator, memory, p, qq.getSelectFun(), true);
				
		// order by count(?n)
		aggregate(evaluator, memory, p, qq.getOrderBy(), false);
	
		if (qq.getHaving() != null){
			if (isEvent){
				Event event = EventImpl.create(Event.AGG, query.getHaving());
				manager.send(event);
			}
			eval(evaluator, qq.getHaving(), memory, p, HAVING);
		}

		finish(qq);
		
		//template(evaluator, qq, memory);
		
	}
		

	
	void finish(Query qq){
		setNbsolutions(size());
		if (qq.hasGroupBy() && ! qq.isConstruct()){ 
			// after group by (and aggregate), leave one Mapping for each group
			// with result of the group
			groupBy();
		}
		else if (qq.getHaving() != null){
			// clause 'having' with no group by
			// select (max(?x) as ?max where {}
			// having(?max > 100)
			having();
		}
		else if (qq.isAggregate() && ! qq.isConstruct()){
			clean();
		}
	}
	
	
	void having(){
		if (isValid()){
			clean();
		}
		else {
			clear();
		}
	}

	void clean(){
		if (size()>1){
			Mapping map = get(0);
			clear();
			add(map);
		}
	}
	
	void aggregate(Evaluator evaluator, Memory memory, Producer p, List<Exp> list, boolean isSelect){
		int n = 0;
		for (Exp exp : list){
			if (exp.isAggregate()){
				// perform group by and then aggregate
				if (hasEvent){
					Event event = EventImpl.create(Event.AGG, exp);
					manager.send(event);
				}
				eval(evaluator, exp, memory, p, (isSelect)?SELECT:n++);
			}
		}
	}
	

	/** 
	 * select count(?doc) as ?count
	 * group by ?person ?date
	 * order by ?count
	 */

	private	void eval(Evaluator eval, Exp exp, Memory mem, Producer p, int n){
		if (exp.isExpGroupBy()){
			// min(?l, groupBy(?x, ?y)) as ?min
			Group g = createGroup(exp);
			aggregate(g, eval, exp, mem, p, n);
			if (exp.isHaving()){
				// min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min
				having(eval, exp, mem, g);
				// remove global group if any 
				// may be recomputed with new Mapping list
				setGroup(null);
			}
		}
		else if (query.hasGroupBy()){
			// perform group by and then aggregate
			aggregate(getCreateGroup(), eval, exp, mem, p, n);
		}
		else {
			apply(eval, exp, mem, p, n);
		}
	}

	
	
	/**
	 * exp : min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min)
	 * test the filter, remove Mappping that fail
	 */
	void having(Evaluator eval, Exp exp, Memory mem, Group g){
		Filter f = exp.getHavingFilter();
		clear();
		for (Mappings lm : g.getValues()){
			for (Mapping map : lm){
				mem.push(map, -1);
				if (eval.test(f, mem)){
					add(map);
				}
				mem.pop(map);
			}
		}
	}
	
	
	/**
	 * Eliminate all Mapping that do not match filter
	 */
	void filter(Evaluator eval, Filter f, Memory mem){
		ArrayList<Mapping> l = new ArrayList<Mapping>();
		for (Mapping map : getList()){
			mem.push(map, -1);
			if (eval.test(f, mem)){
				l.add(map);
			}
			mem.pop(map);
		}
		setList(l);
	}
	
	
	
	/**
	 * Template perform additionnal group_concat(?out)
	 */
	void template(Evaluator eval, Memory mem, Producer p){
            template(eval, query, mem, p);
        }
        
        void template(Evaluator eval, Query q, Memory mem, Producer p){
		if (size() > 0 && ! isFake && q.isTemplate()){
                    setTemplateResult(apply(eval, q.getTemplateGroup(), mem, p));
		}
	}
   
	/**
	 * Template perform additionnal group_concat(?out)
	 */
	private	Node apply(Evaluator eval, Exp exp, Memory memory, Producer p){
		Mapping firstMap = get(0);
		// bind the Mapping in memory to retrieve group by variables
		memory.aggregate(firstMap);
		if (size() == 1){
                    // memory.getNode(?out)
                    Node node = memory.getNode(exp.getFilter().getExp().getExp(0));
                    //if (node == null || ! node.isFuture()){
                    if (node != null && !node.isFuture()) {
                        // if (node == null) go to aggregate below because we want it to be uniform
                        // whether there is one or several results
                        return node;
                    }
		}
                
		Node node = eval.eval(exp.getFilter(), memory, p);
		memory.pop(firstMap);
		return node;
	}

	
	/**
	 * Compute aggregate (e.g. count() max()) and having
	 * on one group or on whole result (in both case: this Mappings)
	 * in order to be able to compute both count(?doc) and ?count
	 * we bind Mapping into memory
	 */
	private	boolean apply(Evaluator eval, Exp exp, Memory memory, Producer p, int n){
		int select = SELECT;
		// get first Mapping in current group
		Mapping firstMap = get(0);
		// bind the Mapping in memory to retrieve group by variables
		memory.aggregate(firstMap);
		boolean res = true;

		if (n == HAVING){
			res = eval.test(exp.getFilter(), memory);
			
			if (hasEvent){
				Event event = EventImpl.create(Event.FILTER, exp, res);
				manager.send(event);
			}
			setValid(res);
		}
		else {
			Node node = null;
			
			if (exp.getFilter() == null){
				// order by ?count
				node = memory.getNode(exp.getNode());			
			}
			else {
				node = eval.eval(exp.getFilter(), memory, p);
			}
			
			if (hasEvent){
				Event event = EventImpl.create(Event.FILTER, exp, node);
				manager.send(event);
			}
			
			for (Mapping map : this){

				if (n == select){
					map.setNode(exp.getNode(), node);
				}
				else {
					map.setOrderBy(n,  node);
				}
			}
		}

		//if (n != SELECT) 
			memory.pop(firstMap);
		return res;
	}


	/**
	 * Process aggregate for each group
	 * select, order by, having
	 */
	private	void aggregate(Group group, Evaluator eval, Exp exp, Memory mem, Producer p, int n){
		//if (group == null) group = createGroup();
            int count = 0;
		for (Mappings map : group.getValues()){
			// eval aggregate filter for each group 
			// set memory current group
			// filter (e.g. count()) will consider this group
			if (hasEvent){
                            map.setEventManager(manager);
                        }
                        map.setCount(count++);
			mem.setGroup(map);
			map.apply(eval, exp, mem, p, n);
			mem.setGroup(null);
		}
	}
	



	/** 
	 * process group by
	 * leave one Mapping within each group
	 */
	public	void groupBy(){
		// clear the current list
		groupBy(getCreateGroup());
	}
		
	
	public Mappings groupBy(List<Exp> list){
		Group group = createGroup(list);
		groupBy(group);
		return this;
	}
	
	
	/**
	 * Generate the Mapping list according to the group
	 * PRAGMA: replace the original list by the group list
	 */
	public void groupBy(Group group){
		clear();
		for (Mappings lMap : group.getValues()){
			int start = 0;
			if (lMap.isValid()){
				// clause 'having' may have tagged first mapping as not valid
				start = 1;
				Mapping map = lMap.get(0);
				if (map != null){
					if (isListGroup){
						map.setMappings(lMap);
					}
					else {
						// it may have been set by aggregate (see process)
						map.setMappings(null);
					}
				}
				// add one element for current group
				// check distinct if any
				submit2(map);
			}
		}
	}
	

	
	/**
	 * Project on select variables of query 
	 * Modify all the Mapping
	 */
	public Mappings project(){
		for (Mapping map : this){
			map.project(query);
		}
		return this;
	}



	/**
	 * for group by ?o1 .. ?on
	 */
	private	Group createGroup(){
		if (query.isConnect()){
			// group by any
			Merge group = new Merge(this);
			group.merge();
			return group;
		}
		else {
			Group group = createGroup(query.getGroupBy());
			return group;
		}
	}
	
	private Group getCreateGroup(){
		if (group == null){
			 group = createGroup();
		}
		return group;
	}
	
	private Group getGroup(){
		return group;
	}
	
	private void setGroup(Group g){
		group = g;
	}
	
	/**
	 * Generate a group by list of variables
	 */
	public Group defineGroup(List<String> list){
		ArrayList<Exp> el = new ArrayList<Exp>();
		for (String name : list){
			el.add(query.getSelectExp(name));
		}
		return createGroup(el);
	}
	
	
	/**
	 * group by
	 */
	Group createGroup(List<Exp> list){
		return createGroup(list, false);
	}
	
	Group createGroup(Exp exp){
		return createGroup(exp.getExpGroupBy(), true);
	}
	
	Group createGroup(List<Exp> list, boolean extend){
		Group group = new Group(list);
		group.setDuplicate(query.isDistribute());
		group.setExtend(extend);

		for (Mapping map : this){
			group.add(map);
		}
		return group;
	}

	
	/**
	 * 	for select distinct
	 */
	Group group(List<Exp> list){
		Group group = new Group(list);
		return group;
	}

	public Node max(Node qNode){
		Node node = minmax(qNode, true);
		return node;
	}

	public Node min(Node qNode){
		return minmax(qNode, false);
	}


	private	Node minmax(Node qNode, boolean isMax){
		Node res = null ;
		for (Mapping map : this){
			Node node = map.getNode(qNode);
			if (res == null){
				res = node;
			}
			else if (node != null){
				if (isMax){
					if (node.compare(res) > 0){
						res = node;
					}
				}
				else if (node.compare(res) < 0){
					res = node;
				}
			}

		}
		return res;	
	}

	/**
	 * Generic aggregate
	 * eval is Walker
	 * it applies the aggregate f (e.g. sum(?x)) on the list of Mapping
	 * with Mapping as environment to get variable binding
	 */
	void process(Evaluator eval, Filter f, Environment env, Producer p){
            int n = 0;
		for (Mapping map : this){
                    this.setCount(n++);
			// in case there is a nested aggregate, map will be an Environment
			// it must implement aggregate() and hence must know current Mappings group
			map.setMappings(this);
			map.setQuery(env.getQuery());
			// share same bnode table in all Mapping of current group solution
			map.setMap(env.getMap());
			eval.eval(f, map, p);
		}
	}

	/*********************************************************************
	 * 
	 * Pipeline Solutions implementation
	 * These operations use the select nodes if any and otherwise the query nodes
	 * 
	 * 
	 *********************************************************************/

	public Mappings union(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m : this){
			res.add(m);
		}
		for (Mapping m : lm){
			res.add(m);
		}
		return res;
	}
	
	public Mappings and(Mappings lm){
		return join(lm);
	}
	
	public Mappings join(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m1 : this){
			for (Mapping m2 : lm){
				Mapping map = m1.join(m2);
				if (map != null){
					res.add(map);
				}
			}
		}

		return res;
	}

	public Mappings minus(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m1 : this){
			boolean ok = true;
			for (Mapping m2 : lm){
				if (m1.compatible(m2)){
					ok = false;
					break;
				}
			}
			if (ok){
				res.add(m1);
			}
		}
		return res;
	}

	public Mappings optional(Mappings lm){
		return option(lm);
	}

	public Mappings option(Mappings lm){
		Mappings res = new Mappings();
		for (Mapping m1 : this){
			boolean ok = false;
			for (Mapping m2 : lm){
				Mapping map = m1.join(m2);
				if (map != null){
					ok = true;
					res.add(map);
				}
			}
			if (! ok){
				res.add(m1);
			}
		}

		return res;
	}

	public Mappings project(List<Exp> lExp){
		Mappings res = new Mappings();

		return res;
	}

	public Mappings rename(List<Exp> lExp){
		Mappings res = new Mappings();
		for (Mapping m : this){
			res.add(m.rename(lExp));
		}

		return res;
	}

	/**
	 * Assign select nodes to all Mapping
	 */
	public void finalize(){
		if (getSelect() != null){
			Node[] nodes = new Node[getSelect().size()];
			int i = 0;
			for (Node node : getSelect()){
				nodes[i++] = node;
			}

			for (Mapping map : this){
				map.setSelect(nodes);
			}
		}
	}

	public void setGraph(Object graph) {
		this.graph = graph;
	}

	public Object getGraph() {
		return graph;
	}
	
	public int nbUpdate() {
		return nbDelete + nbInsert;
	}

	public int nbDelete() {
		return nbDelete;
	}

	public void setNbDelete(int nbDelete) {
		this.nbDelete = nbDelete;
	}

	public int nbInsert() {
		return nbInsert;
	}

	public void setNbInsert(int nbInsert) {
		this.nbInsert = nbInsert;
	}

	public List<Entity> getInsert() {
		return insert;
	}

	public void setInsert(List<Entity> lInsert) {
		this.insert = lInsert;
	}

	public List<Entity> getDelete() {
		return delete;
	}

	public void setDelete(List<Entity> lDelete) {
		this.delete = lDelete;
	}

	public int nbSolutions() {
		return nbsolutions;
	}

	void setNbsolutions(int nbsolutions) {
		this.nbsolutions = nbsolutions;
	}

	public Node getTemplateResult() {
		return templateResult;
	}
        
        public String getTemplateStringResult() {
		if (templateResult == null){
                    return null;
                }
                 return templateResult.getLabel();
	}

	private void setTemplateResult(Node templateResult) {
		this.templateResult = templateResult;
	}
        
        @Override
        public int pointerType(){
            return MAPPINGS;
        }
        
        @Override
        public Mappings getMappings(){
            return this;
        }

//
//	public Solutions union(Solutions s){
//		return union((Mappings) s);
//	}
//
//	public Solutions join(Solutions s){
//		return join((Mappings) s);
//	}
//
//	public Solutions minus(Solutions s){
//		return minus((Mappings) s);
//	}
//
//	public Solutions option(Solutions s){
//		return option((Mappings) s);
//	}



//	@Deprecated
//	private	void aggregate2(Group group, Evaluator eval, Exp exp, Memory mem, int n){
//		if (Group.test){
//			aggregate2(group, eval, exp, mem, n);
//			return;
//		}
//
//		for (List<Mappings> lGroup :  group.values()){
//			// lGroup is a list of groups (that share same value for first groupBy variable)
//
//			for (Mappings maps : lGroup){
//				// eval aggregate filter for each group 
//				// set memory current group
//				// filter (e.g. count()) will consider this group
//				if (hasEvent) maps.setEventManager(manager);
//				mem.setGroup(maps);
//				maps.apply(eval, exp, mem, n);
//				mem.setGroup(null);
//			}
//		}
//	}
	
//	@Deprecated
//	public void groupBy2(Group group){
//		if (Group.test){
//			groupBy2(group);
//			return;
//		}
//		
//		clear();
//		for (List<Mappings> ll :  group.values()){
//			for (Mappings lMap : ll){
//				int start = 0;
//				if (lMap.isValid()){
//					// clause 'having' may have tagged first mapping as not valid
//					start = 1;
//					Mapping map = lMap.get(0);
//					if (isListGroup && map != null){
//						map.setMappings(lMap);
//					}
//					// add one element for current group
//					add(map);
//				}
//			}
//		}
//	}

}
