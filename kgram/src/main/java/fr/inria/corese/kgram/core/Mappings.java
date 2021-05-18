package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.kgram.event.EventImpl;
import fr.inria.corese.kgram.event.EventManager;
import java.util.HashMap;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.MAPPINGS;
import fr.inria.corese.kgram.api.query.Binder;
import java.util.Map;

/*
 * Manage list of Mapping, result of a query
 * 
 * process select distinct
 * process group by, order by, limit offset, aggregates, having(?count>50)
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 */
public class Mappings extends PointerObject
        implements Comparator<Mapping>, Iterable<Mapping> {
 
    private static final String NL = System.getProperty("line.separator");
    private static final String AGGREGATE_LOCAL = "@local";
    private static final long serialVersionUID = 1L;
    private static int SELECT = -1;
    private static int HAVING = -2;
    List<Node> select;
    boolean isDistinct = false,
            isValid = true,
            hasEvent = false,
            // if true, store all Mapping of the group
            isListGroup = false;
    boolean sortWithDesc = true;
    Query query;
    List<Mapping> list, reject;
    private List<Edge> insert;
    private List<Edge> delete;
    private List<Node> nodeList;
    Group group, distinct;
    Node fake;
    Object object;
    private Eval eval;
    private TripleStore graph;
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
    private boolean error = false;
    private Mapping sm1;
    private Mapping sm2;
    private Node result;
    private Binder binding;
    private Object provenance;
    private List<String> link;
    private int length = 0;
    private int queryLength = 0;
    private int display = Integer.MAX_VALUE;

    public Mappings() {
        list = new ArrayList<>();
        link = new ArrayList<>();
    }

    Mappings(Mapping map) {
        this();
        add(map);
    }

    Mappings(Query q) {
        this();
        query = q;
    }

    void setEventManager(EventManager man) {
        manager = man;
        hasEvent = true;
    }

    public static Mappings create(Query q) {
        return create(q, false);
    }

    public static Mappings create(Query q, boolean subEval) {
        Mappings lMap = new Mappings(q);
        lMap.init(q, subEval);
        return lMap;
    }

    @Override
    public Iterable getLoop() {
        return this;
    }
    
    @Override
    public String getDatatypeLabel() {
       return String.format("[Mappings: size=%s]", size());
    }

    public void init(Query q) {
        init(q, false);
    }

    void init(Query q, boolean subEval) {
        initiate(q, !subEval && q.isDistinct());
    }
    
    void initiate(Query q,  boolean b){
        initiate(q, b, false);
    }
    
    void initiate(Query q,  boolean b, boolean all){
        this.query = q;
        isDistinct = b;
        isListGroup = q.isListGroup();
        setSelect(q.getSelect());
        if (isDistinct) {
            if (all) {
                List<Node> list = q.getSelectNodes();
                if (list.isEmpty()){
                    distinct = group(q.getSelectFun()); 
                }
                else {
                   distinct = group(q.toExp(list)); 
                }
            }
            else {
                distinct = group(q.getSelectFun());
            }
            distinct.setDistinct(true);
            distinct.setDuplicate(q.isDistribute());
        }
    }
    
    public Mappings distinct() {
        Mappings res = new Mappings();
        res.initiate(getQuery(), true, true);
        for (Mapping m : this) {
            res.submit(m);
        }
        return res;
    }
    
    public Mappings distinctAll() {
        List<Node> list = query.getSelectNodes();
        if (list.isEmpty()) {
            list = query.getSelect();
        }
        if (list.isEmpty()) {
            list = getSelect();
        }
        return distinct(list, list);
    }

    
    public Mappings distinct(List<Node> list) {
        Mappings map = distinct(query.getSelect(), list);
        //map.setNodeList(list);
        return map;
    }
    
    public Mappings distinct(List<Node> selectList, List<Node> distinctList) {
        Mappings map = new Mappings(query);
        map.setSelect(selectList);
        Group group = Group.create(distinctList);
        group.setDistinct(true);
        for (Mapping m : this) {
            if (group.isDistinct(m)) {
                map.add(m);
            }
        }
        return map;
    }
    
    
    
    public boolean isDistinct(){
        return isDistinct;
    }

    int count() {
        return count;
    }

    void setCount(int n) {
        count = n;
    }

    public void add(Mapping m) {
        list.add(m);
    }

    public void reject(Mapping m) {
        if (reject == null) {
            reject = new ArrayList<>();
        }
        reject.add(m);
    }

    void complete() {
        if (reject != null) {
            for (Mapping m : reject) {
                list.remove(m);
            }
        }
    }

    List<Mapping> getList() {
        return list;
    }

    void setList(List<Mapping> l) {
        list = l;
    }

    public void add(Mappings lm) {
        list.addAll(lm.getList());
    }

    @Override
    public Iterator<Mapping> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }

    public Mapping get(int n) {
        return list.get(n);
    }
    
    public Mapping set(int n, Mapping m) {
        return list.set(n, m);
    }

    void remove(int n) {
        list.remove(n);
    }

    public void clear() {
        list.clear();
    }

    @Override
    public Query getQuery() {
        return query;
    }

    public Object getAST() {
        if (getQuery() == null) {
            return null;
        }
        return getQuery().getAST();
    }

    public Object getContext() {
        if (getQuery() == null) {
            return null;
        }
        return getQuery().getContext();
    }

    public void setQuery(Query q) {
        query = q;
    }

//    public void setObject(Object o) {
//        object = o;
//    }
//
//    public Object getObject() {
//        return object;
//    }

    @Override
    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean all) {
        return toString(all, false, size());     
    }

    public String toString(boolean all, boolean ptr, int max) {
        StringBuffer sb = new StringBuffer();
        int i = 1;
        boolean isSelect = select != null && !all;
        for (Mapping map : this) {
            if (i > max) {
                sb.append(String.format("# size = %s, stop after: %s" , size(), (i-1)));
                sb.append(NL);
                break;
            }
            String str = ((i < 10) ? "0" : "") + i + " ";
            sb.append(str);

            if (isSelect) {
                for (Node qNode : select) {
                    print(map, qNode, sb, ptr);
                }
            } else {
                for (Node qNode : map.getQueryNodes()) {
                    print(map, qNode, sb, ptr);
                }
            }

            i++;
            sb.append(NL);
        }
        return sb.toString();
    }

    void print(Mapping map, Node qNode, StringBuffer sb, boolean ptr) {
        Node node = map.getNode(qNode);
        if (node != null) {
            sb.append(qNode);
            //sb.append("[").append(qNode.getIndex()).append("]"); 
            sb.append(" = ").append(node);
            Object obj = node.getObject();
            if (ptr && obj != null
                    && obj != this
                    && obj instanceof PointerObject) {
                sb.append(" : \n");
                sb.append(obj.toString());
            }
            sb.append("; ");
        }
    }

    public List<Node> getSelect() {
        return select;
    }

    public DatatypeValue getValue(Node qNode) {
        if (size() == 0) {
            return null;
        }
        Mapping map = get(0);
        return map.getValue(qNode);
    }

    public Node getNode(String var) {
        if (size() == 0) {
            return null;
        }
        Mapping map = get(0);
        return map.getNode(var);
    }

    public Object getNodeObject(String var) {
        if (size() == 0) {
            return null;
        }
        return get(0).getNodeObject(var);
    }

    public Node getNode(Node var) {
        if (size() == 0) {
            return null;
        }
        Mapping map = get(0);
        return map.getNode(var);
    }

    public Node getQueryNode(String var) {
        if (size() == 0) {
            return null;
        }
        Mapping map = get(0);
        return map.getQueryNode(var);
    }

    public DatatypeValue getValue(String var) {
        Node node = getNode(var);
        if (node == null) {
            return null;
        }
        return node.getDatatypeValue();
    }

    @Override
    // PRAGMA: Do **not** take var into account
    public Object getValue(String var, int n) {
        if (n >= size()) return null;
        return get(n);
    }
    
    public Object getValue2(String var, int n) {
        return getValue(var);
    }

    void setSelect(List<Node> nodes) {
        select = nodes;
    }

    public void setSelect(Node node) {
        select = new ArrayList<>(1);
        select.add(node);
    }

    /**
     * use case: bind(sparql('select ?x ?y where { ... }') as (?z, ?t)) rename
     * ?x as ?z and ?y as ?t in all Mapping as well as in Mappings select
     *
     */
    public void setNodes(List<Node> nodes) {
        if (getSelect() != null) {
            for (Mapping map : this) {
                map.rename(getSelect(), nodes);
            }
            setSelect(nodes);
        } else {
            for (Mapping map : this) {
                map.setNodes(nodes);
            }
        }
    }

    public void fixQueryNodes(Query q) {
        for (Mapping m : this) {
            m.fixQueryNodes(q);
        }
    }

    /**
     * select distinct in case of aggregates, accept Mapping now, distinct will
     * be computed below
     */
    public void submit(Mapping a) {
        if (a == null) {
            return;
        }
        if (acceptable(a)) {
            add(a);
        }
    }
    
    boolean acceptable(Mapping m) {
        return query.isAggregate() || accept(m);
    }

    /**
     * Used for distinct on aggregates
     */
    void submit2(Mapping a) {
        if (query.isAggregate()) {
            if (accept(a)) {
                add(a);
            }
        } else {
            add(a);
        }
    }

    boolean accept(Node node) {
        return (distinct == null) ? true : distinct.accept(node);
    }

    // TODO: check select == null
    public boolean accept(Mapping r) {
        if (select == null || select.isEmpty()) {
            return true;
        }
        if (isDistinct) {
            return distinct.isDistinct(r);
        }
        return true;
    }

    void setValid(boolean b) {
        isValid = b;
    }

    boolean isValid() {
        return isValid;
    }

    @Deprecated
    boolean same(Node n1, Node n2) {
        if (n1 == null) {
            return n2 == null;
        } else if (n2 == null) {
            return false;
        } else {
            return n1.same(n2);
        }
    }

    void sort(Eval eval) {
        this.setEval(eval);
        Collections.sort(list, this);
        this.setEval(null);
    }

    void sort() {
        Collections.sort(list, this);
    }
    
    public void genericSort() {
        Collections.sort(list, new MappingSorter());
    }
    
    class MappingSorter implements Comparator<Mapping> {
            
        @Override
        public int compare(Mapping m1, Mapping m2){
            int res = 0;
            for (int i = 0; i < getSelect().size() && res == 0; i++) {
                Node n  = getSelect().get(i);
                Node n1 = m1.getNodeValue(n);
                Node n2 = m2.getNodeValue(n);
                res = genCompare(n1, n2);
            }
            return res;
        }
    
    }
    
    public void sort(List<String> varList) {
        Collections.sort(list, new VariableSorter(varList));
    }
    
    VariableSorter getVariableSorter(List<String> varList) {
        return new VariableSorter(varList);
    }
    
    class VariableSorter implements Comparator<Mapping> {
        
        List<String> varList;
        
        VariableSorter(List<String> list) {
            this.varList = list;
        }
            
        @Override
        public int compare(Mapping m1, Mapping m2){
            int res = 0;
            for (int i = 0; i < varList.size() && res == 0; i++) {
                Node n1 = m1.getNodeValue(varList.get(i));
                Node n2 = m2.getNodeValue(varList.get(i));
                res = genCompare(n1, n2);
            }
            return res;
        }
    
    }
    

    /**
     *
     * Sort according to node
     */
    void sort(Eval eval, Node node) {
        this.setEval(eval);
        sortWithDesc = false;
        for (Mapping m : this) {
            m.setOrderBy(m.getNode(node));
        }
        sort();
        this.setEval(null);
    }

    int find(Node node, Node qnode) {
        return find(node, qnode, 0, size() - 1);
    }

    int find(Node n2, Node qnode, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            Node n1 = list.get(mid).getNodeValue(qnode);
            int res = compare(n1, n2);
            if (res >= 0) {
                return find(n2, qnode, first, mid);
            } else {
                return find(n2, qnode, mid + 1, last);
            }
        }
    }
    
     int find(Mapping m, VariableSorter vs, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            Mapping mm = get(mid);
            int res = vs.compare(mm, m);
            if (res >= 0) {
                return find(m, vs, first, mid);
            } else {
                return find(m, vs, mid + 1, last);
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
    
    int genCompare(Node n1, Node n2) {
        return compare(n1, n2);
    }
    
    int comparator(Node n1, Node n2) {       
        if (getEval() != null) {
            return getEval().getVisitor().compare(getEval(), n1.compare(n2), n1.getDatatypeValue(), n2.getDatatypeValue());
        }
        return n1.compare(n2);
    }
    
    
//    int comparator2(Node n1, Node n2) {       
//        if (getEval() != null) {
//            return getEval().compare(n1, n2);
//        }
//        return n1.compare(n2);
//    }

    @Override
    public int compare(Mapping m1, Mapping m2) {
        sm1 = m1;
        sm2 = m2;
        Node[] order1 = m1.getOrderBy();
        Node[] order2 = m2.getOrderBy();
        
//        for (Node n : order1) { System.out.print(n + " " );}
//        System.out.println();
//        for (Node n : order2) { System.out.print(n + " " );}
//        System.out.println();
        
        List<Exp> orderBy = query.getOrderBy();

        int res = 0;
        for (int i = 0; i < order1.length && i < order2.length && res == 0; i++) {

            if (order1[i] != null && order2[i] != null) { // sort ?x
                res = comparator(order1[i], order2[i]);
            } //      unbound 
            else if (order1[i] == null) { // unbound var
                if (order2[i] == null) {
                    res = 0;
                } else {
                    res = unbound;
                }
            } else if (order2[i] == null) {
                res = -unbound;
            } else {
                res = 0;
            }

            if (!orderBy.isEmpty() && orderBy.get(i).status() && sortWithDesc) {
                res = desc(res);
            }

        }
        return res;
    }
    
    int desc(int i) {
        if (i == 0) {
            return 0;
        } else if (i < 0) {
            return +1;
        } else {
            return -1;
        }
    }

    /**
     * *********************************************************
     *
     * Aggregates
     *
     * 1. select [distinct] var where 2. group by 3. count/min/max as var 4.
     * order by 5. limit offset
     *
     * group by with aggregate return one Mapping per group where the mapping
     * hold the result of the aggregate
     *
     */
    /**
     * order by offset
     */
    void complete(Eval eval) {
        if (query.getOrderBy().size() > 0) {
            sort(eval);
        }
        if (query.getOffset() > 0) {
            // skip offset
            // TODO: optimize this
            for (int i = 0; i < query.getOffset() && size() > 0; i++) {
                remove(0);
            }
        }
        while (size() > query.getLimit()) {
            remove(size() - 1);
        }
    }

    /**
     * select count(?doc) as ?count group by ?person ?date order by ?count
     * having(?count > 100) TODO: optimize this because we enumerate all
     * Mappings for each kind of aggregate we could enumerate Mappings once and
     * compute all aggregates for each map
     */
    void aggregate(Evaluator evaluator, Memory memory, Producer p) throws SparqlException {
        aggregate(query, evaluator, memory, p, true);
    }

    public void aggregate(Query qq, Evaluator evaluator, Memory memory, Producer p) throws SparqlException {
        aggregate(qq, evaluator, memory, p, false);
    }

    void aggregate(Query qq, Evaluator evaluator, Memory memory, Producer p, boolean isFinish) throws SparqlException {

        if (size() == 0) {
            if (qq.isAggregate()) {
                // SPARQL test cases requires that aggregate on empty result set return one empty result
                // and count() return 0
                add(Mapping.fake(qq));
                setFake(true);  
            } else {
                return;
            }
        }

        boolean isEvent = hasEvent;

        // select (count(?n) as ?count)
        aggregate(evaluator, memory, p, qq.getSelectFun(), true);

        // order by count(?n)
        aggregate(evaluator, memory, p, qq.getOrderBy(), false);

        if (qq.getHaving() != null) {
            if (isEvent) {
                Event event = EventImpl.create(Event.AGG, query.getHaving());
                manager.send(event);
            }
            eval(evaluator, qq.getHaving(), memory, p, HAVING);
        }

        finish(qq);

    }

    void finish(Query qq) {
        setNbsolutions(size());
        if (qq.getAST().hasMetadata(AGGREGATE_LOCAL)) {
            // keep results as is
        }
        else if (qq.hasGroupBy() && !qq.isConstruct()) {
            // after group by (and aggregate), leave one Mapping for each group
            // with result of the group
            groupBy();
        } else if (qq.getHaving() != null) {
            // clause 'having' with no group by
            // select (max(?x) as ?max where {}
            // having(?max > 100)
            having();
        } else if (qq.isAggregate() && !qq.isConstruct()) {
            clean();
        }
    }

    void having() {
        if (isValid()) {
            clean();
        } else {
            clear();
        }
    }

    void clean() {
        if (size() > 1) {
            Mapping map = get(0);
            clear();
            add(map);
        }
    }

    void aggregate(Evaluator evaluator, Memory memory, Producer p, List<Exp> list, boolean isSelect) throws SparqlException {
        int n = 0;
        for (Exp exp : list) {
            if (exp.isAggregate()) {
                // perform group by and then aggregate
                if (hasEvent) {
                    Event event = EventImpl.create(Event.AGG, exp);
                    manager.send(event);
                }
                eval(evaluator, exp, memory, p, (isSelect) ? SELECT : n);
            }
            if (!isSelect)  {
                n++;
            }
        }
    }

    /**
     * select count(?doc) as ?count group by ?person ?date order by ?count
     */
    private void eval(Evaluator eval, Exp exp, Memory mem, Producer p, int n) throws SparqlException {
        if (exp.isExpGroupBy()) {
            // min(?l, groupBy(?x, ?y)) as ?min
            Group g = createGroup(exp);
            aggregate(g, eval, exp, mem, p, n);
            if (exp.isHaving()) {
                // min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min
                having(eval, exp, mem, g);
                // remove global group if any 
                // may be recomputed with new Mapping list
                setGroup(null);
            }
        } else if (query.hasGroupBy()) {
            // perform group by and then aggregate
            aggregate(getCreateGroup(), eval, exp, mem, p, n);
        } else {
            apply(eval, exp, mem, p, n);
        }
    }

    /**
     * exp : min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min) test the filter,
     * remove Mappping that fail
     */
    void having(Evaluator eval, Exp exp, Memory mem, Group g) throws SparqlException {
        Filter f = exp.getHavingFilter();
        clear();
        for (Mappings lm : g.getValues()) {
            for (Mapping map : lm) {
                mem.push(map, -1);
                if (eval.test(f, mem)) {
                        add(map);
                }
                mem.pop(map);
            }
        }
    }

    /**
     * Eliminate all Mapping that do not match filter
     */
    void filter(Evaluator eval, Filter f, Memory mem) throws SparqlException {
        ArrayList<Mapping> l = new ArrayList<Mapping>();
        for (Mapping map : getList()) {
            mem.push(map, -1);
            if (eval.test(f, mem)) {
                    l.add(map);
            }
            mem.pop(map);
        }
        setList(l);
    }

    /**
     * Template perform additionnal group_concat(?out)
     */
    void template(Evaluator eval, Memory mem, Producer p) throws SparqlException {
        template(eval, query, mem, p);
    }

    void template(Evaluator eval, Query q, Memory mem, Producer p) throws SparqlException {
        if (q.isTemplate() && size() > 0 && ! (isFake() && q.isTransformationTemplate()) ) {
            // fake in transformation template -> fail
            // fake in query template -> not fail
            setTemplateResult(apply(eval, q.getTemplateGroup(), mem, p));
        }
    }

    /**
     * Template perform additionnal group_concat(?out)
     */
    public Node apply(Evaluator eval, Exp exp, Memory memory, Producer p) throws SparqlException {
        Mapping firstMap = get(0);
        // bind the Mapping in memory to retrieve group by variables
        memory.aggregate(firstMap);
        if (size() == 1) {
            // memory.getNode(?out)
            //Node node = memory.getNode(exp.getFilter().getExp().getExp(0));
            Node node = eval.eval(exp.getFilter().getExp().getExp(0).getFilter(), memory, p);       
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
     * Compute aggregate (e.g. count() max()) and having on one group or on
     * whole result (in both case: this Mappings) in order to be able to compute
     * both count(?doc) and ?count we bind Mapping into memory
     */
    private boolean apply(Evaluator eval, Exp exp, Memory memory, Producer p, int n) throws SparqlException {
        int iselect = SELECT;
        // get first Mapping in current group
        Mapping firstMap = get(0);
        // bind the Mapping in memory to retrieve group by variables
        memory.aggregate(firstMap);
        boolean res = true;
        Eval ev = memory.getEval();
        if (n == HAVING) {
            res = eval.test(exp.getFilter(), memory);
            if (ev != null) {
                ev.getVisitor().having(ev, exp.getFilter().getExp(), res);
            }
            if (hasEvent) {
                Event event = EventImpl.create(Event.FILTER, exp, res);
                manager.send(event);
            }
            setValid(res);
        } else {
            Node node;
            if (exp.getFilter() == null) {
                // order by ?count
                node = memory.getNode(exp.getNode());
            } else {
                node = eval.eval(exp.getFilter(), memory, p);
                if (ev != null) {
                    ev.getVisitor()
                        .aggregate(ev, exp.getFilter().getExp(), 
                                (node == null) ? null : node.getDatatypeValue());
                }
            }
            
            if (hasEvent) {
                Event event = EventImpl.create(Event.FILTER, exp, node);
                manager.send(event);
            }

            for (Mapping map : this) {

                if (n == iselect) {
                    map.setNode(exp.getNode(), node);
                } else {
                    map.setOrderBy(n, node);
                }
            }
        }

        //if (n != SELECT) 
        memory.pop(firstMap);
        return res;
    }

    /**
     * Process aggregate for each group select, order by, having
     */
    private void aggregate(Group group, Evaluator eval, Exp exp, Memory mem, Producer p, int n) throws SparqlException {
        int count = 0;
        for (Mappings map : group.getValues()) {
            // eval aggregate filter for each group 
            // set memory current group
            // filter (e.g. count()) will consider this group
            if (hasEvent) {
                map.setEventManager(manager);
            }
            map.setCount(count++);
            mem.setGroup(map);
            map.apply(eval, exp, mem, p, n);
            mem.setGroup(null);
        }
    }
    
    
    public void aggregate(Mapping map, Query q, Map<String, DatatypeValue> bn, int n) {
        setCount(n);
        // in case there is a nested aggregate, map will be an Environment
        // it must implement aggregate() and hence must know current Mappings group
        map.setMappings(this);
        map.setQuery(q);
        // share same bnode table in all Mapping of current group solution
        map.setMap(bn);
    }

    /**
     * process group by leave one Mapping within each group
     */
    public void groupBy() {
        // clear the current list
        groupBy(getCreateGroup());
    }

    public Mappings groupBy(List<Exp> list) {
        Group group = createGroup(list);
        groupBy(group);
        return this;
    }

    /**
     * Generate the Mapping list according to the group PRAGMA: replace the
     * original list by the group list
     */
    public void groupBy(Group group) {
        clear();
        for (Mappings lMap : group.getValues()) {
            int start = 0;
            if (lMap.isValid()) {
                // clause 'having' may have tagged first mapping as not valid
                start = 1;
                Mapping map = lMap.get(0);
                if (map != null) {
                    if (isListGroup) {
                        map.setMappings(lMap);
                    } else {
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
     * Project on select variables of query Modify all the Mapping
     */
    public Mappings project() {
        for (Mapping map : this) {
            map.project(query);
        }
        return this;
    }
    
    public Mappings project(Node q){
        Mappings map = create(query);
        for (Mapping m : this){
            Mapping res = m.project(q);
            if (res != null){
                map.add(res);
            }
        }
        return map;
    }

    /**
     * for group by ?o1 .. ?on
     */
    private Group createGroup() {
        if (query.isConnect()) {
            // group by any
            Merge group = new Merge(this);
            group.merge();
            return group;
        } else {
            Group group = createGroup(query.getGroupBy());
            return group;
        }
    }

    private Group getCreateGroup() {
        if (group == null) {
            group = createGroup();
        }
        return group;
    }

    private Group getGroup() {
        return group;
    }

    private void setGroup(Group g) {
        group = g;
    }

    /**
     * Generate a group by list of variables
     */
    public Group defineGroup(List<String> list) {
        ArrayList<Exp> el = new ArrayList<Exp>();
        for (String name : list) {
            el.add(query.getSelectExp(name));
        }
        return createGroup(el);
    }

    /**
     * group by
     */
    Group createGroup(List<Exp> list) {
        return createGroup(list, false);
    }

    Group createGroup(Exp exp) {
        return createGroup(exp.getExpGroupBy(), true);
    }

    Group createGroup(List<Exp> list, boolean extend) {
        Group gp =  Group.createFromExp(list);
        gp.setDuplicate(query.isDistribute());
        gp.setExtend(extend);
        gp.setFake(isFake());

        for (Mapping map : this) {
            gp.add(map);
        }
        return gp;
    }

    /**
     * for select distinct
     */
    Group group(List<Exp> list) {
        Group group =  Group.createFromExp(list);
        return group;
    }

    public Node max(Node qNode) {
        Node node = minmax(qNode, true);
        return node;
    }

    public Node min(Node qNode) {
        return minmax(qNode, false);
    }

    private Node minmax(Node qNode, boolean isMax) {
        Node res = null;
        for (Mapping map : this) {
            Node node = map.getNode(qNode);
            if (res == null) {
                res = node;
            } else if (node != null) {
                if (isMax) {
                    if (node.compare(res) > 0) {
                        res = node;
                    }
                } else if (node.compare(res) < 0) {
                    res = node;
                }
            }

        }
        return res;
    }

    /**
     * Generic aggregate eval is Walker it applies the aggregate f (e.g.
     * sum(?x)) on the list of Mapping with Mapping as environment to get
     * variable binding
     */
//    void aggregate(Evaluator eval, Filter f, Environment env, Producer p) {
//        if (isFake()) {
//            // fake Mappings because there were no result
//            return;
//        }
//        int n = 0;
//        for (Mapping map : this) {
//            this.setCount(n++);
//            // in case there is a nested aggregate, map will be an Environment
//            // it must implement aggregate() and hence must know current Mappings group
//            map.setMappings(this);
//            map.setQuery(env.getQuery());
//            // share same bnode table in all Mapping of current group solution
//            map.setMap(env.getMap());
//            eval.eval(f, map, p);
//        }
//    }
       
    /**
     * *******************************************************************
     *
     * Pipeline Solutions implementation These operations use the select nodes
     * if any and otherwise the query nodes
     *
     *
     ********************************************************************
     */
    
    Node getCommonNode(Mappings map) {
        if (isEmpty() || map.isEmpty()) {
            return null;
        }
        return get(0).getCommonNode(map.get(0));       
    }
    
    List<String> getCommonVariables(Mappings map) {
        HashMap<String, String> t1 = unionVariable();
        HashMap<String, String> t2 = map.unionVariable();
        return intersectionVariable(t1, t2);
    }
    
    List<String> intersectionVariable(HashMap<String, String> t1, HashMap<String, String> t2) {
        ArrayList<String> varList = new ArrayList<>();
        for (String var : t1.keySet()) {
            if (t2.containsKey(var)) {
                varList.add(var);
            }
        }
        return varList;
    }
    
    HashMap<String, String> unionVariable() { 
       HashMap<String, String> union = new HashMap<>();
       for (Mapping m : this) {
            for (String var : m.getVariableNames()) {
                union.put(var, var);
            }
        } 
       return union;
    }
    
    /**
     * Is there a Mapping compatible with m
     * @param m
     * @return 
     */
    
    int  find(Mapping m, List<String> list) {
        return find(m, getVariableSorter(list), 0, size()-1);
    }
    
    boolean minusCompatible(Mapping m, List<String> list) {
        int n = find(m, getVariableSorter(list), 0, size()-1);
        if (n >= 0 && n < size()) {
            Mapping mm = get(n);
            return m.minusCompatible(mm,list);
        }
        return false;
    }
    
    public Mappings union(Mappings lm) {
        Mappings res = (getQuery() == lm.getQuery()) ? Mappings.create(getQuery()) : new Mappings();
        for (Mapping m : this) {
            res.add(m);
        }
        for (Mapping m : lm) {
            res.add(m);
        }
        return res;
    }

    public Mappings and(Mappings lm) {
        return join(lm);
    }

    public Mappings join(Mappings lm) {
        Mappings res =  Mappings.create(getQuery());
        for (Mapping m1 : this) {
            for (Mapping m2 : lm) {
                Mapping map = m1.join(m2);
                if (map != null) {
                    res.add(map);
                }
            }
        }

        return res;
    }
    
     public Mappings joiner(Mappings lm) {
        Mappings res =  Mappings.create(getQuery());
        for (Mapping m1 : this) {
            for (Mapping m2 : lm) {
                Mapping map = m1.merge(m2);
                if (map != null) {
                    res.add(map);
                }
            }
        }
        return res;
    }
     
     // join with cmn common variable, map2 is sorted on cmn, null value first
     public Mappings joiner(Mappings map2, Node cmn) {
        Mappings map1 = this;
        Mappings res =  Mappings.create(map1.getQuery());
        
        for (Mapping m1 : map1){
            Node val = m1.getNodeValue(cmn);
            if (val == null){
                // common unbound in m1
                for (Mapping m2 : map2){
                    Mapping m = m1.merge(m2);
                    if (m != null){
                        res.add(m);
                    }
                }
            }
            else {
                for (Mapping m2 : map2){
                    Node val2 = m2.getNodeValue(cmn);
                    if (val2 == null){
                        // common unbound in m2
                         Mapping m = m1.merge(m2);
                         if (m != null){
                             res.add(m);
                         }                         
                    }
                    else {
                        break;
                    }
                }
                
                // index of common value in map2
                int index = map2.find(val, cmn);

                if (index >= 0 && index < map2.size()) {

                    for (int i = index; i < map2.size(); i++) {

                        // get value of common in map2
                        Mapping m2 = map2.get(i);
                        Node n2 = m2.getNodeValue(cmn);

                        if (n2 == null || !val.match(n2)) { // was equal
                            break;
                        }
                        Mapping m = m1.merge(m2);
                        if (m != null){
                             res.add(m);
                         }   
                    }
                }                
            }
        }
        return res;
    }

    public Mappings minus(Mappings lm) {
        Mappings res = new Mappings();
        for (Mapping m1 : this) {
            boolean ok = true;
            for (Mapping m2 : lm) {
                if (m1.compatible(m2)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                res.add(m1);
            }
        }
        return res;
    }

    public Mappings optional(Mappings lm) {
        return option(lm);
    }

    public Mappings option(Mappings lm) {
        Mappings res = new Mappings();
        for (Mapping m1 : this) {
            boolean ok = false;
            for (Mapping m2 : lm) {
                Mapping map = m1.join(m2);
                if (map != null) {
                    ok = true;
                    res.add(map);
                }
            }
            if (!ok) {
                res.add(m1);
            }
        }

        return res;
    }

    public Mappings project(List<Exp> lExp) {
        Mappings res = new Mappings();

        return res;
    }

    public Mappings rename(List<Exp> lExp) {
        Mappings res = new Mappings();
        for (Mapping m : this) {
            res.add(m.rename(lExp));
        }

        return res;
    }
    
    /**
     * Join (var = val) to each Mapping, remove those where var = something else
     * Use case: service ?s { BGP }
     */
    public void join(Node var, Node val) {
        if (!getSelect().contains(var)) {
            getSelect().add(var);
        }
        for (int i = 0; i < size();) {
            Mapping m = list.get(i);
            Node node = m.getNodeValue(var);
            if (node == null) {
                m.addNode(var, val);
                i++;
            } else if (node.equals(val)) {
                i++;
            } else {
                list.remove(m);
            }
        }
    }
    
    public boolean inScope(Node var) {
        if (getNodeList() != null) {
            return getNodeList().contains(var);
        }
        if (getSelect() != null) {  
            return getSelect().contains(var);
        }
        return true; 
    }
    
    public List<Node> aggregate(Node var) {
        List<Node> list = new ArrayList<>();       
        for (Mapping m : this) {
            Node val = m.getNodeValue(var);
            if (val != null && !list.contains(val)) {
                list.add(val);
            }
        }
        return list;
    }
    
    public Mappings limit(int n) {
        while (size()>n) {
            remove(size()-1);
        }
        return this;
    }
    
    public Mappings getMappings(Query q, Node var, Node val) {
        Mappings map = create(q); 
        for (Mapping m : this) {
            Node node = m.getNodeValue(var);
            if (node != null && node.equals(val)) {
                map.add(m);
            }
        }
        Mappings res = map.distinct();
        return res;
    }
    
    public Mappings getMappings(Query q) {
        Mappings map = create(q); 
        for (Mapping m : this) {
             map.add(m);
        }
        Mappings res = map.distinct();
        return res;
    }
    
    /**
     * Check if all values of a given variable are in same namespace
     */
    void select() {
        if (size() > 0) {
            Mapping m = get(0);
            if (m.size() > 0) {
                Node var = m.getQueryNode(0);
                if (var.isVariable()) {
                    DatatypeValue value = m.getValue(var);
                    if (value.isURI()) {
                        String ns = value.stringValue();
                        boolean check = check(var, ns);
                        if (check) {
                            ArrayList<Object> list = new ArrayList<>();
                            list.add(var);
                            list.add(ns);
                        }
                    }
                }
            }
        }
    }
    
    boolean check(Node var, String ns) {
        for (Mapping m : this) {
            if (m.size() > 0) {
                DatatypeValue value = m.getValue(var);
                if (!(value != null && value.isURI() && value.stringValue().startsWith(ns))) {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return true;
    }
    

    /**
     * Assign select nodes to all Mapping
     */
    public void finish() {
        if (getSelect() != null) {
            Node[] nodes = new Node[getSelect().size()];
            int i = 0;
            for (Node node : getSelect()) {
                nodes[i++] = node;
            }

            for (Mapping map : this) {
                map.setSelect(nodes);
            }
        }
    }

    public void setGraph(TripleStore graph) {
        this.graph = graph;
    }

    @Override
    public TripleStore getTripleStore() {
        return graph;
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

    public List<Edge> getInsert() {
        return insert;
    }

    public void setInsert(List<Edge> lInsert) {
        this.insert = lInsert;
    }

    public List<Edge> getDelete() {
        return delete;
    }

    public void setDelete(List<Edge> lDelete) {
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
        if (templateResult == null) {
            return null;
        }
        return templateResult.getLabel();
    }

    private void setTemplateResult(Node templateResult) {
        this.templateResult = templateResult;
    }

    @Override
    public PointerType pointerType() {
        return MAPPINGS;
    }

    @Override
    public Mappings getMappings() {
        return this;
    }

    /**
     * @return the eval
     */
    public Eval getEval() {
        return eval;
    }

    /**
     * @param eval the eval to set
     */
    public void setEval(Eval eval) {
        this.eval = eval;
    }

    /**
     * @return the isFake
     */
    public boolean isFake() {
        return isFake;
    }

    /**
     * @param isFake the isFake to set
     */
    public void setFake(boolean isFake) {
        this.isFake = isFake;
    }
    
    boolean isNodeList() {
        return size() != 0 && getNodeList() != null && ! getNodeList().isEmpty();
    }
    
        /**
     * @return the nodeList
     */
    public List<Node> getNodeList() {
        return nodeList;
    }
    
    /**
     * Generate nodeList for values clause for this Mappings
     */
    public List<Node> getNodeListValues() {
        if (isEmpty()) {
            return new ArrayList<>();
        }
        return get(0).getQueryNodeList();
    }
    
    public List<Node> getQueryNodeList() {
        return getNodeListValues();
    }

    /**
     * @param nodeList the nodeList to set
     */
    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }
    
    /**
     * @return the result
     */
    public Node getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Node result) {
        this.result = result;
    }
    
        /**
     * @return the binding
     */
    public Binder getBinding() {
        return binding;
    }

    /**
     * @param binding the binding to set
     */
    public void setBinding(Binder binding) {
        this.binding = binding;
    }
 
    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }
    
        /**
     * @return the provenance
     */
    public Object getProvenance() {
        return provenance;
    }

    /**
     * @param provenance the provenance to set
     */
    public void setProvenance(Object provenance) {
        this.provenance = provenance;
    }
    
    void setNamedGraph(Node node) {
        for (Mapping m : this) {
            m.setNamedGraph(node);
        }
    }

    public String getLink() {
        if (getLinkList().isEmpty()) {
            return null;
        }
        return getLinkList().get(0);
    }

    public void setLink(String link) {
        addLink(link);
    }
    
    public void addLink(String link) {
        getLinkList().add(link);
    }
    
    public List<String> getLinkList() {
        return link;
    }

    public void setLinkList(List<String> link) {
        this.link = link;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getQueryLength() {
        return queryLength;
    }

    public void setQueryLength(int queryLength) {
        this.queryLength = queryLength;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

  

}
