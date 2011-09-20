package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
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
public class Mappings extends ArrayList<Mapping> 
implements Comparator<Mapping>
{
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
	Query query;
	Group group, distinct;
	Node fake;
	Object object;
	private Object graph;

	EventManager manager;
	
	// SPARQL: -1 (unbound first)
	// Corese order: 1 (unbound last)
	int unbound = -1;
	


	public Mappings(){

	}

	Mappings(Query q){
		query = q;
	}

	void setEventManager(EventManager man){
		manager = man;
		hasEvent = true;
	}

	public static Mappings create(Query q){
		Mappings lMap = new Mappings(q); 
		lMap.isDistinct = q.isDistinct();
		lMap.isListGroup = q.isListGroup();
		lMap.setSelect(q.getSelect());
		if (lMap.isDistinct){
			lMap.distinct = lMap.group(q.getSelectFun());
			lMap.distinct.setDistinct(true);
			lMap.distinct.setDuplicate(q.isDistribute());
		}
		return lMap;
	}
	
	public Query getQuery(){
		return query;
	}
	
	public void setObject(Object o){
		object = o;
	}
	
	public Object getObject(){
		return object;
	}

	public String toString(){
		if (select == null) return super.toString();
		
		String str = "";
		int i = 1;
		for (Mapping map : this){
			str += ((i<10)?"0":"") + i + " ";
			for (Node qNode : select){
				Node node = map.getNode(qNode);
				if (node!=null){
					str += qNode + " = " + node + "; ";
				}
			}
			i++;
			str += "\n";
		}
		return str;
	}

	public List<Node> getSelect(){
		return select;
	}
	
	public Object getValue(Node qNode){
		return getValue(qNode.getLabel());
	}
	
	public Object getValue(String var){
		if (size() == 0) return null;
		Mapping map = get(0);
		Node node = map.getNode(var);
		if (node == null) return null;
		return node.getValue();
	}

	void setSelect(List<Node> nodes){
		select = nodes;
	}

	void submit(Mapping a){
		if (accept(a)){
			add(a);
		}
	}

	boolean accept(Node node){
		return distinct.accept(node);
	}

	boolean accept(Mapping r){
		if (select.size()==0) return true;

		if (isDistinct){

			if (true) return distinct.add(r);

			boolean same;
			for (Mapping res : this){
				same = true;
				for (Node qnode : select){
					if (! same(res.getNode(qnode), r.getNode(qnode))){
						same = false;
						break;
					}
				}
				if (same) return false;
			}
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


	void sort(){
		Collections.sort(this, this);
	}


	public int compare(Mapping r1, Mapping r2) {
		Node[] order1 = r1.getOrderBy();
		Node[] order2 = r2.getOrderBy();

		//boolean reverse[]=query.getReverse(); // sort in reverse order
		List<Exp> orderBy = query.getOrderBy();

		int res = 0;
		
		for (int i = 0; i < order1.length && i < order2.length && res == 0; i++) {

			if (order1[i] != null && order2[i] != null) { // sort ?x
				res = order1[i].compare(order2[i]);
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

			if (orderBy.get(i).status()){ 
				switch (res) {
				case -1 : res = 1; break;
				case 1 : res = -1; break;
				}
			}

		}
		return res;
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
	void complete(){
		if (query.getOrderBy().size()>0){
			sort();
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
	void aggregate(Evaluator evaluator, Memory memory){
		if (size() == 0) return ;
		Query qq = query;
		boolean isEvent = hasEvent;
		Exp num = null;
		// select count(?n) as ?count
		for (Exp exp : qq.getSelectFun()){
			Filter f = exp.getFilter();
			if (f!=null){
				if (f.isRecAggregate()){
					// perform group by and then aggregate
					if (isEvent){
						Event event = EventImpl.create(Event.AGG, exp);
						manager.send(event);
					}
					eval(evaluator, exp, memory, SELECT);
				}
				else if (f.getExp().oper() == ExprType.NUMBER){
					num = exp;
				}
			}
		}

		// order by ?count
		int n = 0;
		for (Exp exp : qq.getOrderBy()){
			Filter f = exp.getFilter();
			if (f!=null && f.isRecAggregate()){
				// perform group by and then aggregate
				if (isEvent){
					Event event = EventImpl.create(Event.AGG, exp);
					manager.send(event);
				}
				eval(evaluator, exp, memory, n++);
			}
		}

		if (qq.getHaving() != null){
			if (isEvent){
				Event event = EventImpl.create(Event.AGG, query.getHaving());
				manager.send(event);
			}
			eval(evaluator, qq.getHaving(), memory, HAVING);
		}

		if ((qq.isGroupBy() || qq.isConnect()) && qq.isConstruct()){
			if (group==null) group = createGroup();
		}

		if ((qq.isGroupBy() || qq.isConnect())&& ! qq.isConstruct()){ // && query.isAggregate
			// after group by (and aggregate), leave one Mapping for each group
			// with result of the group
			cut();
		}
		else if (qq.getHaving() != null){
			// clause 'having' with no group by
			// select (max(?x) as ?max where {}
			// having(?max > 100)
			if ( isValid()){
				clean();
			}
			else {
				clear();
			}
		}
		else if (query.isAggregate() && ! qq.isConstruct()){
			clean();
		}

		//having(evaluator, memory);
	}

	void clean(){
		if (size()>1){
			Mapping map = get(0);
			clear();
			add(map);
		}
	}

	/** 
	 * select count(?doc) as ?count
	 * group by ?person ?date
	 * order by ?count
	 */

	private	void eval(Evaluator eval, Exp exp, Memory mem, int n){
		if (query.isGroupBy() || query.isConnect()){
			// perform group by and then aggregate
			aggregate(eval, exp, mem, n);
		}
		else {
			apply(eval, exp, mem, n);
		}
	}



	/**
	 * Compute aggregate (e.g. count() max()) and having
	 * on one group or on whole result (in both case: this Mappings)
	 * in order to be able to compute both count(?doc) and ?count
	 * we bind Mapping into memory
	 */
	private	boolean apply(Evaluator eval, Exp exp, Memory memory, int n){
		int select = SELECT;
		// get first Mapping in current group
		Mapping firstMap = get(0);
		// bind the Mapping in memory to retrieve group by variables
		//if (n != select) 
			memory.push(firstMap, -1);
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
			// TODO: group by should be bound ??
			Node node = eval.eval(exp.getFilter(), memory);
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
	private	void aggregate(Evaluator eval, Exp exp, Memory mem, int n){
		if (group == null) group = createGroup();

		for (List<Mappings> lGroup :  group.values()){
			// lGroup is a list of groups (that share same value for first groupBy variable)

			for (Mappings group : lGroup){
				// eval aggregate filter for each group 
				// set memory current group
				// filter (e.g. count()) will consider this group
				if (hasEvent) group.setEventManager(manager);
				mem.setGroup(group);
				group.apply(eval, exp, mem, n);
				mem.setGroup(null);
			}
		}
	}


	/** 
	 * process group by
	 * leave one Mapping within each group
	 */
	private	void cut(){
		if (group == null) group = createGroup();
		// clear the current list
		clear();
		for (List<Mappings> ll :  group.values()){
			for (Mappings lMap : ll){
				int start = 0;
				if (lMap.isValid()){
					// clause 'having' may have tagged first mapping as not valid
					start = 1;
					Mapping map = lMap.get(0);
					if (isListGroup && map != null){
						map.setMappings(lMap);
					}
					// add one element for current group
					add(map);
				}
			}
		}
		
	}


	/**
	 * remove Mapping which do not verify having filter
	 * having(?count > 50)
	 * @deprecated
	 */
	private	void having(Evaluator evaluator, Memory memory){
		if (query.getHaving()!=null){
			int i = 0;
			while (i < size()){
				Mapping map = get(i);
				if (evaluator.test(query.getHaving().getFilter(), map)){
					i++;
				}
				else {
					remove(map);
				}
			}
		}
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
			Group group = new Group(query.getGroupBy());
			group.setDuplicate(query.isDistribute());

			for (Mapping map : this){
				group.add(map);
			}
			return group;
		}
	}

	
	/**
	 * 	for select distinct
	 */
	Group group(List<Exp> list){
		Group group = new Group(list);
		return group;
	}

	
	
	public Group getGroup(){
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
	void process(Evaluator eval, Filter f){
		for (Mapping map : this){
			eval.eval(f, map);
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






}
