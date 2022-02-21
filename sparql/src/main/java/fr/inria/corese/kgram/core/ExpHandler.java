package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExpHandler {

    private List<Node> nodeList;
    private List<Node> selectNodeList;
    private List<Node> existNodeList;

    private boolean inSubScope = false;
    private boolean bind = false;
    private boolean blank = false;
    private boolean optional = false;
    private boolean exist = false;
    // when false, return nodes from first exp
    private boolean all = true;

    public ExpHandler() {
        nodeList = new ArrayList<>();
        selectNodeList = new ArrayList<>();
        existNodeList = new ArrayList<>();
    }

    public ExpHandler(boolean exist, boolean inSubScope, boolean bind, boolean blank) {
        this();
        setExist(exist).setInSubScope(inSubScope).setBind(bind).setBlank(blank);
    }
    
    public ExpHandler(boolean exist, boolean inSubScope, boolean bind) {
        this();
        setExist(exist).setInSubScope(inSubScope).setBind(bind);
    }
    
    public ExpHandler(boolean inSubScope, boolean bind) {
        this();
        setInSubScope(inSubScope).setBind(bind);
    }

    public ExpHandler copy() {
        ExpHandler h = new ExpHandler();
        h.setAll(isAll()).setBind(isBind())
                .setBlank(isBlank()).setExist(isExist())
                .setInSubScope(isInSubScope());
        return h;
    }

    void add(Node node) {
        if (node != null && (isBlank() || (node.isVariable() && !node.isBlank()))) {
            addDistinct(node);
        }
    }
    
    void addDistinct(Node node) {
        if (! getNodeList().contains(node)) {
            getNodeList().add(node);
        }
    }
    
    // add select nodes that are not already in node list
    public List<Node> getNodes(){
        for (Node selectNode : getSelectNodeList()) {
            if (!getNodeList().contains(selectNode)) {                                                 
                getNodeList().add(overloadSelectNodeByExistNode(selectNode));
            }
        }

        if (isExist()) {
            // collect exists { } nodes
            for (Node existNode : getExistNodeList()) {
                addDistinct(existNode);
            }
        }
        
        return getNodeList();
    }
    
   /**
    * use case: 
    * select * where { 
    * {select * where {?x foaf:knows ?y}} 
    * filter exists {?x foaf:knows ?y} } 
    * 
    * lNode = {} lSelNode = {?x, ?y} lExistNode = {?x, ?y} 
    * overload select nodes of subquery by exists nodes
    * @hint: this code would be useful if nodes ?y and ?y were different 
    * currently they are the same, hence it is useless
    */    
    Node overloadSelectNodeByExistNode(Node node) {
        if (getExistNodeList().contains(node)) {            
            return get(getExistNodeList(), node);
        } else {
            return node;
        }
    }
    
    Node get(List<Node> lNode, Node node) {
        for (Node qNode : lNode) {
            if (qNode.equals(node)) {
                return qNode;
            }
        }
        return null;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public List<Node> getSelectNodeList() {
        return selectNodeList;
    }

    public void setSelectNodeList(List<Node> selectNodeList) {
        this.selectNodeList = selectNodeList;
    }

    public List<Node> getExistNodeList() {
        return existNodeList;
    }

    public void setExistNodeList(List<Node> existNodeList) {
        this.existNodeList = existNodeList;
    }

    public boolean isInSubScope() {
        return inSubScope;
    }

    boolean isInSubScopeSample() {
        return isInSubScope() && !isAll();
    }

    public ExpHandler setInSubScope(boolean inSubScope) {
        this.inSubScope = inSubScope;
        return this;
    }

    public boolean isBind() {
        return bind;
    }

    public ExpHandler setBind(boolean bind) {
        this.bind = bind;
        return this;
    }

    public boolean isBlank() {
        return blank;
    }

    public ExpHandler setBlank(boolean blank) {
        this.blank = blank;
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    public ExpHandler setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    public boolean isExist() {
        return exist;
    }

    public ExpHandler setExist(boolean exist) {
        this.exist = exist;
        return this;
    }

    public boolean isAll() {
        return all;
    }

    public ExpHandler setAll(boolean all) {
        this.all = all;
        return this;
    }

    // return nodes from first exp of BGP
    // use case: compute relevant variable bindings
    public ExpHandler sample() {
        setAll(false);
        return this;
    }

    // return all nodes
    public ExpHandler all() {
        setAll(true);
        return this;
    }

}
