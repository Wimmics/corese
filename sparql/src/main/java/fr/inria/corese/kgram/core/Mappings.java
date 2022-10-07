package fr.inria.corese.kgram.core;

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
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.MAPPINGS;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(Mappings.class);

    private static final String NL = System.getProperty("line.separator");
    private static final String AGGREGATE_LOCAL = "@local";
    private static final long serialVersionUID = 1L;
    private static int SELECT = -1;
    private static int HAVING = -2;
    // SPARQL: -1 (unbound first)
    // Corese order: 1 (unbound last)
    public static int unbound = -1;
    List<Node> select;
    boolean isDistinct = false,
            // statisfy having(test)
            isValid = true,
            hasEvent = false,
            // if true, store all Mapping of the group
            isListGroup = false;
    boolean sortWithDesc = true;
    private Query query;
    private List<Mapping> list;
    List<Mapping> reject;
    // Original join Mappings
    // use case: union manage its own way
    private Mappings joinMappings;
    // Update result
    private List<Edge> insert;
    private List<Edge> delete;
    // in-scope node list 
    private List<Node> nodeList;
    private Group group;
    private Group distinct;
    private Eval eval;
    // service report if Mappings from service
    // json object
    private IDatatype detail;
    // construct where result graph
    private TripleStore graph;
    private int nbsolutions = 0;
    EventManager manager;
    int count = 0;
    private int nbDelete = 0;
    private int nbInsert = 0;
    // result of query as a template
    private Node templateResult;
    // fake result in case of aggregate without result
    private boolean isFake = false;
    // parse error in service result
    private boolean error = false;
    //private Node result;
    // return Binding stack as part of result to share it
    private Binding binding;
    // Federate Service manage provenance
    private Object provenance;
    // Linked Result URL List
    private List<String> link;
    // service result log
    private int length = 0;
    private int queryLength = 0;
    // limit number of results to be displayed
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

    void initiate(Query q, boolean b) {
        initiate(q, b, false);
    }

    void initiate(Query q, boolean b, boolean all) {
        this.setQuery(q);
        isDistinct = b;
        isListGroup = q.isListGroup();
        setSelect(q.getSelect());
        
        if (isDistinct) {
            if (all) {
                setDistinct(group(getAllExpList(q)));
            } else {
                setDistinct(group(q.getSelectFun()));
            }
            getDistinct().setDistinct(true);
            getDistinct().setDuplicate(q.isDistribute());
        }
    }
    
    // use case: service require distinct mappings for bindings
    // for variables in body of q, not for select (exp as var)
    List<Exp> getAllExpList(Query q) {
        // variables in body of q
        List<Node> list = q.selectNodesFromPattern();
        if (list.isEmpty()) {
            return q.getSelectFun();
        } else {
            return q.toExp(list);
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

    public Mappings distinct(List<Node> list) {
        Mappings map = distinct(getQuery().getSelect(), list);
        return map;
    }

    public Mappings distinct(List<Node> selectList, List<Node> distinctList) {
        Mappings map = new Mappings(getQuery());
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

    public boolean isDistinct() {
        return isDistinct;
    }

    int count() {
        return count;
    }

    void setCount(int n) {
        count = n;
    }

    public Mappings add(Mapping m) {
        getMappingList().add(m);
        return this;
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
                getMappingList().remove(m);
            }
        }
    }

    List<Mapping> getList() {
        return getMappingList();
    }

    void setList(List<Mapping> l) {
        setMappingList(l);
    }

    public void add(Mappings lm) {
        getMappingList().addAll(lm.getMappingList());
    }

    @Override
    public Iterator<Mapping> iterator() {
        return getMappingList().iterator();
    }

    @Override
    public int size() {
        return getMappingList().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Mapping get(int n) {
        return getMappingList().get(n);
    }

    public Mapping set(int n, Mapping m) {
        return getMappingList().set(n, m);
    }

    void remove(int n) {
        getMappingList().remove(n);
    }

    public void clear() {
        getMappingList().clear();
    }
    
    public void cleanIndex() {
        for (Node node : getSelect()) {
            if (node != null) {
                node.setIndex(-1);
            }
        }
    }

    @Override
    public Query getQuery() {
        return query;
    }

    public ASTQuery getAST() {
        if (getQuery() == null) {
            return null;
        }
        return getQuery().getAST();
    }

    public Context getContext() {
        if (getQuery() == null) {
            return null;
        }
        return getQuery().getContext();
    }

    public Mappings setQuery(Query q) {
        query = q;
        return this;
    }
    
    public Mappings initQuery(Query q) {
        setQuery(q);
        init(q);
        return this;
    }

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
        ArrayList<Node> alist = new ArrayList<>();
        
        for (Mapping map : this) {
            if (i > max) {
                sb.append(String.format("# size = %s, stop after: %s", size(), (i - 1)));
                sb.append(NL);
                break;
            }
            String str = ((i < 10) ? "0" : "") + i + " ";
            sb.append(str);

            if (isSelect) {
                for (Node qNode : select) {
                    print(map, qNode, sb, alist, ptr);
                    
                }
            } else {
                for (Node qNode : map.getQueryNodes()) {
                    print(map, qNode, sb, alist, ptr);
                }
            }

            i++;
            sb.append(NL);
//            for (Node n : alist) {
//                sb.append(n).append(" : ").append(n.getEdge()).append(NL);
//            }
//            alist.clear();
        }
        return sb.toString();
    }

    void print(Mapping map, Node qNode, StringBuffer sb, List<Node> list, boolean ptr) {
        Node node = map.getNode(qNode);
        if (node != null) {
            sb.append(qNode).append(" = ").append(node);
//            if (node.isTripleWithEdge()) {
//                list.add(node);
//            }
            Object obj = node.getNodeObject();
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

    public IDatatype getValue(Node qNode) {
        if (size() == 0) {
            return null;
        }
        Mapping map = get(0);
        return map.getValue(qNode);
    }
    
    public List<Node> getNodeValueList(String var) {
        List<Node> alist = new ArrayList<>();
        for (Mapping m : this) {
            Node n = m.getNode(var);
            if (n != null) {
                alist.add(n);
            }
        }
        return alist;
    }
    
    public List<String> getStringValueList(String var) {
        List<String> alist = new ArrayList<>();
        for (Mapping m : this) {
            IDatatype dt = m.getValue(var);
            if (dt != null) {
                alist.add(dt.getLabel());
            }
        }
        return alist;
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

    public IDatatype getValue(String var) {
        Node node = getNode(var);
        if (node == null) {
            return null;
        }
        return node.getDatatypeValue();
    }

    @Override
    // PRAGMA: Do **not** take var into account
    public Object getValue(String var, int n) {
        if (n >= size()) {
            return null;
        }
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
        return getQuery().isAggregate() || accept(m);
    }

    /**
     * Used for distinct on aggregates
     */
    void submit2(Mapping a) {
        if (getQuery().isAggregate()) {
            if (accept(a)) {
                add(a);
            }
        } else {
            add(a);
        }
    }
    
    public void modifyDistinct() {
        // select distinct + group by is already done by submit2
        if (isDistinct() && getQuery().getGroupBy().isEmpty()) {
            ArrayList<Mapping> alist = new ArrayList<>();
            for (Mapping m : this) {
                if (accept(m)) {
                    alist.add(m);
                }
            }
            setMappingList(alist);
        }
    }

    boolean accept(Node node) {
        return (getDistinct() == null) ? true : getDistinct().accept(node);
    }

    // TODO: check select == null
    public boolean accept(Mapping r) {
        if (select == null || select.isEmpty()) {
            return true;
        }
        if (isDistinct) {
            return getDistinct().isDistinct(r);
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


    /**
     * Query with new modifier
     * Prepare Mappings:
     * Mapping orderBy groupBy
     */
    public void modify(Query q) {
        setGroup(null);  
        setDistinct(null);
        init(q);
        
        if (! q.getOrderBy().isEmpty() || !q.getGroupBy().isEmpty()) {
            for (Mapping m : this) {
                m.prepareModify(q);
            }
        }
    }
    
    /**
     * New select (exp as var)
     */
    public void modifySelect(Eval eval, Query q) {
        for (Exp exp : q.getSelectFun()) {
            if (exp.getFilter() != null && !exp.isAggregate()) {
                Node node = exp.getNode();

                for (Mapping m : this) {
                    //if (m.getNode(node) == null) {
                    // @todo: bnode ?
                    m.setBind(eval.getEnvironment().getBind());
                    m.setEval(eval);
                    m.setQuery(q);
                    try {
                        Node value = eval.eval(exp.getFilter(), m, eval.getProducer());
                        if (value != null) {
                            m.setNode(node, value);
                            addSelectVariable(node);
                        }
                    } catch (SparqlException ex) {
                        logger.error(ex.getMessage());
                    }
                    //}
                }
            }
        }
    }
        
    void addSelectVariable(Node node) {
        if (! getSelect().contains(node)) {
            getSelect().add(node);
        }
    }

    /**
     * New order by on this Mappings, after query processing
     */
    public void modifyOrderBy() {
        if (getEval() != null) {
            modifyOrderBy(getEval(), getQuery());
        }
    }
    
    public void modifyOrderBy(Eval eval, Query q) {
        if (! q.getOrderBy().isEmpty()) {
            setOrderBy(eval, q);
            sort();
        }
    }

    /**
     * Compute order by array again and set it in every Mapping
     */
    void setOrderBy(Eval eval, Query q) {
        if (q.isDebug()) {
            System.out.println("Order By: " + this.toString(true));
        }
        for (Mapping m : this) {
            int i = 0;
                        
            for (Exp exp : q.getOrderBy()) {
                Node node = null;
                if (exp.getFilter() == null) {
                    node = m.getNode(exp.getNode());
                } else {
                    try {
                        // @todo: complete Mapping m with Binding, etc.
                        m.setBind(eval.getEnvironment().getBind());
                        node = eval.eval(null, exp.getFilter(), m, eval.getProducer());
                        if (q.isDebug()) {
                            System.out.println("Order By eval: " + exp);
                            System.out.println(m);
                        }
                    } catch (SparqlException ex) {
                        Eval.logger.error("Order By error: " + ex);
                    }
                }
                if (q.isDebug()) {
                    System.out.println("Order By Result: " + exp + " " + node);
                    System.out.println("__");
                }
                // order by array was reset by prepareModify()
                m.getOrderBy()[i++] = node;
            }
        }
    }
    
    
    public static void setOrderUnboundFirst(boolean b) {
        unbound = (b) ? -1 : 1;
    }
    
    
    
    
    
    /*********************************
     * 
     * MappingSet sort
     * 
     ********************************/


    public void sort(List<String> varList) {
        Collections.sort(getMappingList(), new VariableSorter(varList));
    }
    
    
   /**
     * Is there a Mapping compatible with m
     *
     */
    int find(Mapping m, List<String> list) {
        return find(m, getVariableSorter(list), 0, size() - 1);
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
        public int compare(Mapping m1, Mapping m2) {
            int res = 0;
            for (int i = 0; i < varList.size() && res == 0; i++) {
                Node n1 = m1.getNodeValue(varList.get(i));
                Node n2 = m2.getNodeValue(varList.get(i));
                res = genCompare(n1, n2);
            }
            return res;
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
    
    boolean minusCompatible(Mapping m, List<String> list) {
        int n = find(m, getVariableSorter(list), 0, size() - 1);
        if (n >= 0 && n < size()) {
            Mapping mm = get(n);
            return m.minusCompatible(mm, list);
        }
        return false;
    }    

    
    /*****************************************
     * 
     * Standard sort
     * order by
     * join()
     * 
     *****************************************/
    
    void sort(Eval eval) {
        this.setEval(eval);
        sort();
        this.setEval(null);
    }

    void sort() {
        Collections.sort(getMappingList(), this);
    }
    
    /**
     *
     * Sort according to node
     * use case: join(exp, exp)
     */
    void sort(Eval eval, Node node) {
        prepare(node);
        sort(eval);
    }
    
    void sort(Node node) {
        prepare(node);
        sort();
    }
    
    void prepare(Node node) {
        sortWithDesc = false;
        for (Mapping m : this) {
            m.setOrderBy(m.getNode(node));
        }
    }
    
        
    // find index of node where qnode=node
    // with standard sort
    int find(Node node, Node qnode) {
        return find(node, qnode, 0, size() - 1);
    }

    
    /**
     * comparator of Node for standard sort
     * use IDatatype compareTo()
     * compare with sameTerm semantics: order 1 and 01 in deterministic way
     * authorize overload of comparator for specific datatypes using a Visitor
     */ 
    int comparator(Node n1, Node n2) {
        if (getEval() != null) {
            return getEval().getVisitor().compare(getEval(), n1.compare(n2), n1.getDatatypeValue(), n2.getDatatypeValue());
        }
        return n1.compare(n2);
    }
    
    
    // comparator of Mapping for standard sort
    @Override
    public int compare(Mapping m1, Mapping m2) {
        Node[] order1 = m1.getOrderBy();
        Node[] order2 = m2.getOrderBy();
        List<Exp> orderBy = getQuery().getOrderBy();
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
            if (sortWithDesc && !orderBy.isEmpty() && orderBy.get(i).status() ) {
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
    

    // find index of node where qnode=node
    int find(Node node, Node qnode, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            Node n1 = getMappingList().get(mid).getNodeValue(qnode);
            int res = compare(n1, node);
            if (res >= 0) {
                return find(node, qnode, first, mid);
            } else {
                return find(node, qnode, mid + 1, last);
            }
        }
    }

    // standard node comparator where argument may be null
    int compare(Node n1, Node n2) {
        int res = 0;
        if (n1 != null && n2 != null) { // sort ?x
            res = comparator(n1, n2); //n1.compare(n2);
        } //      unbound 
        else if (n1 == null) { // unbound var
            if (n2 == null) {
                res = 0;
            } else {
                res = unbound;
            }
        } else if (n2 == null) {
            res = -unbound;
        } 
//        else {
//            res = 0;
//        }
        return res;
    }

     int genCompare(Node n1, Node n2) {
        return compare(n1, n2);
     }

    

    /**************************
     * Alternative sorter
     * use case: gui
     * 
     **************************/
    

    public void genericSort() {
        Collections.sort(getMappingList(), new MappingSorter());
    }
    

    class MappingSorter implements Comparator<Mapping> {

        @Override
        public int compare(Mapping m1, Mapping m2) {
            int res = 0;
            for (int i = 0; i < getSelect().size() && res == 0; i++) {
                Node n = getSelect().get(i);
                Node n1 = m1.getNodeValue(n);
                Node n2 = m2.getNodeValue(n);
                res = genCompare(n1, n2);
            }
            return res;
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
     * order by limit offset
     */
    public void complete(Eval eval) {
        if (getQuery().getOrderBy().size() > 0) {
            sort(eval);
        }  
        limitOffset();
    }
    
    void limitOffset() {
        if (getQuery().getOffset() > 0) {
            // skip offset
            // TODO: optimize this
            for (int i = 0; i < getQuery().getOffset() && size() > 0; i++) {
                remove(0);
            }
        }
        while (size() > getQuery().getLimit()) {
            remove(size() - 1);
        }
    }

    /**
     * select count(?doc) as ?count group by ?person ?date order by ?count
     * having(?count > 100) TODO: optimize this because we enumerate all
     * Mappings for each kind of aggregate we could enumerate Mappings once and
     * compute all aggregates for each map
     */
    public void aggregate(Query q, Evaluator evaluator, Environment env, Producer p) throws SparqlException {
        if (env instanceof Memory) {
            aggregate(q, evaluator, (Memory)env, p);
        }
    }
    
    public void aggregate(Evaluator evaluator, Memory memory, Producer p) throws SparqlException {
        aggregate(getQuery(), evaluator, memory, p);
    }
    
    // new aggregate on former Mappings
    public void modifyAggregate(Query q, Evaluator evaluator, Memory memory, Producer p) throws SparqlException {
        aggregate(q, evaluator, memory, p);
    }
    
    public void modifyLimitOffset() {
        limitOffset();
    }
    
    public Mappings modifyValues(Query q) {
        if (q.getValues() != null) {
            return join(q.getValues().getMappings());
        }
        return this;
    }
    
    public void aggregate(Query q, Evaluator evaluator, Memory memory, Producer p) throws SparqlException {
        if (size() == 0) {
            if (q.isAggregate()) {
                // SPARQL semantics requires that aggregate empty result set return one empty result
                // and count() return 0
                add(Mapping.fake(q));
                setFake(true);
            } else {
                return;
            }
        }

        // select (count(?n) as ?count)
        aggregateExpList(q, evaluator, memory, p, q.getSelectFun(), true);

        // order by with aggregate count(?n)
        aggregateExpList(q, evaluator, memory, p, q.getOrderBy(), false);

        if (q.getHaving() != null) {
            if (hasEvent) {
                manager.send(EventImpl.create(Event.AGG, q.getHaving()));
            }
            aggregateSwitch(q, evaluator, q.getHaving(), memory, p, HAVING);
        }

        finish(q);
    }
    
    /**
     * list = list of select | order by 
     * select (aggregate() as ?c) 
     * order by aggregate()
     */
    void aggregateExpList(Query q, Evaluator evaluator, Memory memory, Producer p, List<Exp> list, boolean isSelect) throws SparqlException {
        int n = 0;
        for (Exp exp : list) {
            if (exp.isAggregate()) {
                if (hasEvent) {
                    manager.send(EventImpl.create(Event.AGG, exp));
                }
                // perform group by and then aggregate
                aggregateSwitch(q, evaluator, exp, memory, p, (isSelect) ? SELECT : n);
            }
            if (!isSelect) {
                n++;
            }
        }
    }

    /**
     * select count(?doc) as ?count group by ?person ?date order by ?count
     */
    private void aggregateSwitch(Query q, Evaluator eval, Exp exp, Memory mem, Producer p, int n) throws SparqlException {
        if (exp.isExpGroupBy()) {
            // min(?l, groupBy(?x, ?y)) as ?min
            evalGroupByExp(q, eval, exp, mem, p, n);
        } else if (q.hasGroupBy()) {
            // perform group by and then aggregate
            aggregateGroupMembers(q, getCreateGroup(), eval, exp, mem, p, n);
        } else {
            aggregate(q, eval, exp, mem, p, n);
        }
    }
    

    /**
     * Compute select aggregate, order by aggregate and having on one group or on
     * whole result (in both case: this Mappings) 
     */
    private boolean aggregate(Query q, Evaluator eval, Exp exp, Memory memory, Producer p, int n) throws SparqlException {
        int iselect = SELECT;
        // get first Mapping in current group
        Mapping firstMap = get(0);
        // bind the Mapping in memory to retrieve group by variables
        memory.aggregate(firstMap);
        boolean res = true;
        Eval ev = memory.getEval();
        
        if (n == HAVING) {
            //res = eval.test(exp.getFilter(), memory, p);
            res = exp.getFilter().getExp().test(eval, memory.getBind(), memory, p);
            if (ev != null) {
                ev.getVisitor().having(ev, exp.getFilter().getExp(), res);
            }
            if (hasEvent) {
                manager.send(EventImpl.create(Event.FILTER, exp, res));
            }
            setValid(res);
        } else {
            Node aggregateValue;
            if (exp.getFilter() == null) {
                // use case: order by var
                aggregateValue = memory.getNode(exp.getNode());
            } else {
                // exp = aggregate(term)
                // call fr.inria.corese.sparql.triple.function.aggregate.${AggregateFunction}
                aggregateValue = eval(exp.getFilter(), eval, memory, p);
                if (ev != null) {
                    ev.getVisitor().aggregate(ev, exp.getFilter().getExp(),
                        (aggregateValue == null) ? null : aggregateValue.getDatatypeValue());
                }
            }
            
            if (hasEvent) {
                manager.send(EventImpl.create(Event.FILTER, exp, aggregateValue));
            }

            for (Mapping map : this) {

                if (n == iselect) {
                    // select (count(?x) as ?c)
                    map.setNode(exp.getNode(), aggregateValue);
                } else {
                    // order by count(?x)
                    map.setOrderBy(n, aggregateValue);
                }
            }
        }

        memory.pop(firstMap);
        return res;
    }
    
    Node eval(Filter f, Evaluator eval, Environment env, Producer p) throws SparqlException {
        //return eval.eval(f, env, p);
        return f.getExp().evalWE(eval, env.getBind(), env, p);
    }

    /**
     * Process aggregate for each group select, order by, having
     */
    private void aggregateGroupMembers(Query q, Group group, Evaluator eval, Exp exp, Memory mem, Producer p, int n) throws SparqlException {
        int count = 0;
        for (Mappings map : group.getValues()) {            
            if (hasEvent) {
                map.setEventManager(manager);
            }
            map.setCount(count++);
            mem.setGroup(map);
            map.aggregate(q, eval, exp, mem, p, n);
            mem.setGroup(null);
        }
    }


    void finish(Query qq) {
        setNbsolutions(size());
        if (qq.getAST().hasMetadata(AGGREGATE_LOCAL)) {
            // keep results as is
        } else if (qq.hasGroupBy() && !qq.isConstruct()) {
            // after group by (and aggregate), leave one Mapping for each group
            // with result of the group
            groupBy();
        } else if (qq.getHaving() != null) {
            // clause 'having' with no group by
            // select (max(?x) as ?max) where {}
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
    
    public void dispose() {
        for (Mapping m : this) {
            m.dispose();
        }
        if (getGroup()!=null) {
            getGroup().dispose();
        }
        if (getDistinct() !=null) {
            getDistinct().dispose();
        }
    }
    
    public void prepareAggregate(Mapping map, Query q, Map<String, IDatatype> bn, int n) {
        setCount(n);
        // in case there is a nested aggregate, map will be an Environment
        // it must implement aggregate() and hence must know current Mappings group
        map.setMappings(this);
        map.setQuery(q);
        // share same bnode table in all Mapping of current group solution
        map.setMap(bn);
    }
    
            // min(?l, groupBy(?x, ?y)) as ?min
    void evalGroupByExp(Query q, Evaluator eval, Exp exp, Memory mem, Producer p, int n) throws SparqlException {
        Group g = createGroup(exp);
        aggregateGroupMembers(q, g, eval, exp, mem, p, n);
        if (exp.isHaving()) {
            // min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min
            having(eval, exp, mem, p, g);
            // remove global group if any 
            // may be recomputed with new Mapping list
            setGroup(null);
        }
    }
    
    /**
     * exp : min(?l, groupBy(?x, ?y), (?l = ?min)) as ?min) test the filter,
     * remove Mappping that fail
     */
    void having(Evaluator eval, Exp exp, Memory mem, Producer p, Group g) throws SparqlException {
        Filter f = exp.getHavingFilter();
        clear();
        for (Mappings lm : g.getValues()) {
            for (Mapping map : lm) {
                mem.push(map, -1);
                //if (eval.test(f, mem, p)) {
                if (f.getExp().test(eval, mem.getBind(), mem, p)) {
                    add(map);
                }
                mem.pop(map);
            }
        }
    }
    
    
    /**
     * Eliminate all Mapping that do not match filter
     */
//    void filter(Evaluator eval, Filter f, Memory mem) throws SparqlException {
//        ArrayList<Mapping> l = new ArrayList<>();
//        for (Mapping map : this) {
//            mem.push(map, -1);
//            if (eval.test(f, mem)) {
//                l.add(map);
//            }
//            mem.pop(map);
//        }
//        setList(l);
//    }

    /**
     * Template perform additionnal group_concat(?out)
     */
    void template(Evaluator eval, Memory mem, Producer p) throws SparqlException {
        template(eval, getQuery(), mem, p);
    }

    void template(Evaluator eval, Query q, Memory mem, Producer p) throws SparqlException {
        if (q.isTemplate() && size() > 0 && !(isFake() && q.isTransformationTemplate())) {
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
            Node node = eval(exp.getFilter().getExp().getExp(0).getFilter(), eval, memory, p);
            if (node != null && !node.isFuture()) {
                // if (node == null) go to aggregate below because we want it to be uniform
                // whether there is one or several results
                return node;
            }
        }

        Node node = eval(exp.getFilter(), eval, memory, p);
        memory.pop(firstMap);
        return node;
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
            if (lMap.isValid()) {
                // clause 'having' may have tagged first mapping as not valid
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
            map.project(getQuery());
        }
        return this;
    }

    public Mappings project(Node q) {
        Mappings map = create(getQuery());
        for (Mapping m : this) {
            Mapping res = m.project(q);
            if (res != null) {
                map.add(res);
            }
        }
        return map;
    }

    /**
     * for group by ?o1 .. ?on
     */
    private Group createGroup() {
        if (getQuery().isConnect()) {
            // group by any
            Merge group = new Merge(this);
            group.merge();
            return group;
        } else {
            Group group = createGroup(getQuery().getGroupBy());
            return group;
        }
    }

    private Group getCreateGroup() {
        if (getGroup() == null) {
            setGroup(createGroup());
        }
        return getGroup();
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group g) {
        group = g;
    }

    /**
     * Generate a group by list of variables
     */
    public Group defineGroup(List<String> list) {
        ArrayList<Exp> el = new ArrayList<Exp>();
        for (String name : list) {
            el.add(getQuery().getSelectExp(name));
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
        Group gp = Group.createFromExp(list);
        gp.setDuplicate(getQuery().isDistribute());
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
        Group group = Group.createFromExp(list);
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
        Mappings res = Mappings.create(getQuery());
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
        Mappings res = Mappings.create(getQuery());
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
        Mappings res = Mappings.create(map1.getQuery());

        for (Mapping m1 : map1) {
            Node val = m1.getNodeValue(cmn);
            if (val == null) {
                // common unbound in m1
                for (Mapping m2 : map2) {
                    Mapping m = m1.merge(m2);
                    if (m != null) {
                        res.add(m);
                    }
                }
            } else {
                for (Mapping m2 : map2) {
                    Node val2 = m2.getNodeValue(cmn);
                    if (val2 == null) {
                        // common unbound in m2
                        Mapping m = m1.merge(m2);
                        if (m != null) {
                            res.add(m);
                        }
                    } else {
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
                        if (m != null) {
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
            Mapping m = getMappingList().get(i);
            Node node = m.getNodeValue(var);
            if (node == null) {
                m.addNode(var, val);
                i++;
            } else if (node.equals(val)) {
                i++;
            } else {
                getMappingList().remove(m);
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

    /**
     * Analyse results of source selection query For each boolean variable,
     * count number of true
     */
    public HashMap<String, Integer> countBooleanValue() {
        HashMap<String, Integer> cmap = new HashMap<>();

        for (Node node : getSelect()) {
            int count = 0;
            boolean bool = true;

            for (Mapping m : this) {
                Node val = m.getNode(node);

                if (val != null) {
                    IDatatype value = val.getDatatypeValue();

                    if (value.isBoolean() || value.isNumber()) {
                        if (value.booleanValue()) {
                            count++;
                        }
                    } else {
                        bool = false;
                        break;
                    }
                }
            }

            if (bool) {
                cmap.put(node.getLabel(), count);
            }
        }

        return cmap;
    }

    public Mappings limit(int n) {
        while (size() > n) {
            remove(size() - 1);
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
                    IDatatype value = m.getValue(var);
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
                IDatatype value = m.getValue(var);
                if (!(value != null && value.isURI() && value.stringValue().startsWith(ns))) {
                    return false;
                }
            } else {
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

    public TripleStore getGraph() {
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

    
    public Eval getEval() {
        return eval;
    }

    
    public void setEval(Eval eval) {
        this.eval = eval;
    }

    
    public boolean isFake() {
        return isFake;
    }

    
    public Mappings setFake(boolean isFake) {
        this.isFake = isFake;
        return this;
    }

    boolean isNodeList() {
        return size() != 0 && getNodeList() != null && !getNodeList().isEmpty();
    }

   
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

//    public Node getResult() {
//        return result;
//    }
//
//   
//    public void setResult(Node result) {
//        this.result = result;
//    }
    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Object getProvenance() {
        return provenance;
    }

    public void setProvenance(Object provenance) {
        this.provenance = provenance;
    }

    void setNamedGraph(Node node) {
        for (Mapping m : this) {
            m.setNamedGraph(node);
        }
    }

    public String getLink(String name) {
        for (var url : getLinkList()) {
            if (url.contains(name)) {
                return url;
            }
        }
        return null;
    }

    public String getLastLink(String name) {
        for (int i = getLinkList().size() - 1; i >= 0; i--) {
            var url = getLinkList().get(i);
            if (url.contains(name)) {
                return url;
            }
        }
        return null;
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

    public Mappings getJoinMappings() {
        return joinMappings;
    }

    public void setJoinMappings(Mappings joinMappings) {
        this.joinMappings = joinMappings;
    }

    public Group getDistinct() {
        return distinct;
    }

    public void setDistinct(Group distinct) {
        this.distinct = distinct;
    }

    public List<Mapping> getMappingList() {
        return list;
    }

    public void setMappingList(List<Mapping> list) {
        this.list = list;
    }

    public IDatatype getReport() {
        return detail;
    }

    public void setReport(IDatatype detail) {
        this.detail = detail;
    }

    /**
     * Detail about service execution as a dt:json/dt:map object
     * Recorded as system generated variable ?_service_detail
     */
    public Mappings recordReport(Node node, IDatatype report) {
        setReport(report);
        if (isEmpty()) {
            // when detail is required, we generate a fake result 
            // to record var=detail
            add(Mapping.create());
        }
        for (Mapping m : this) {
            // augment each result with var=detail
            m.addNode(node, report);            
            m.setReport(report);
        }
        return this;
    }
    
    public Mappings completeReport(String key, IDatatype value) {
        if (getReport() != null) {
            getReport().complete(key, value);
        } else {
            basicCompleteReport(key, value);
        }
        return this;
    }
    
    public Mappings basicCompleteReport(String key, IDatatype value) {
        for (Mapping m : this) {
            if (m.getReport() == null) {
                return this;
            } else {
                m.getReport().set(key, value);
            }
        }
        return this;
    }
    
    public Mappings completeReport(String key, String value) {
        return completeReport(key, DatatypeMap.newInstance(value));
    }
    
    public Mappings completeReport(String key, int value) {
        return completeReport(key, DatatypeMap.newInstance(value));
    }
    
    public Mappings completeReport(String key, double value) {
        return completeReport(key, DatatypeMap.newInstance(value));
    }
    
    public boolean contains(IDatatype value) {
        for (Mapping m : this) {
            for (Node node : m.getNodes()) {
                if (value.equals(node.getDatatypeValue())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Mappings getResult() {
        Query q = getQuery();
        ASTQuery ast = getAST();
        if (ast.hasMetadata(Metadata.SELECTION) && q.getSelection()!=null) {
            return q.getSelection();
        }
        if (ast.hasMetadata(Metadata.DISCOVERY) && q.getDiscorevy()!=null) {
            return q.getDiscorevy();
        }
        return this;
    }
       
}
