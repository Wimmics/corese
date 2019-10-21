package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author corby
 */
public class ExpHandler {
    
    private List<Node> nodeList = new ArrayList<Node>();
    private List<Node> selectNodeList = new ArrayList<Node>();
    private List<Node> existNodeList = new ArrayList<Node>();
    
    private boolean inSubScope=false;
    private boolean bind=false;
    private boolean blank=false;
    private boolean optional=false;
    private boolean exist=false;
    private boolean all = false;

    public ExpHandler() {
        nodeList = new ArrayList<Node>();
        selectNodeList = new ArrayList<Node>();
        existNodeList = new ArrayList<Node>();
    }
    
    public ExpHandler(boolean exist, boolean inSubScope, boolean bind, boolean blank) {
        setExist(exist).setInSubScope(inSubScope).setBind(bind).setBlank(blank);
    }
      
    public ExpHandler copy() {
        ExpHandler h = new ExpHandler();
        h.setAll(isAll()).setBind(isBind()).setBlank(isBlank()).setExist(isExist()).setInSubScope(isInSubScope());
        return h;     
    }
    
    void add(Node node) {
        if (node != null
                && (isBlank() || (node.isVariable() && !node.isBlank()))
                && !getNodeList().contains(node)) {
            getNodeList().add(node);
        }
    }

    /**
     * @return the nodeList
     */
    public List<Node> getNodeList() {
        return nodeList;
    }

    /**
     * @param nodeList the nodeList to set
     */
    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * @return the selectNodeList
     */
    public List<Node> getSelectNodeList() {
        return selectNodeList;
    }

    /**
     * @param selectNodeList the selectNodeList to set
     */
    public void setSelectNodeList(List<Node> selectNodeList) {
        this.selectNodeList = selectNodeList;
    }

    /**
     * @return the existNodeList
     */
    public List<Node> getExistNodeList() {
        return existNodeList;
    }

    /**
     * @param existNodeList the existNodeList to set
     */
    public void setExistNodeList(List<Node> existNodeList) {
        this.existNodeList = existNodeList;
    }

    /**
     * @return the inSubScope
     */
    public boolean isInSubScope() {
        return inSubScope;
    }
    
    boolean isInSubScopeLimited() {
        return isInSubScope() && ! isAll();
    }

    /**
     * @param inSubScope the inSubScope to set
     */
    public ExpHandler setInSubScope(boolean inSubScope) {
        this.inSubScope = inSubScope;
        return this;
    }

    /**
     * @return the bind
     */
    public boolean isBind() {
        return bind;
    }

    /**
     * @param bind the bind to set
     */
    public ExpHandler setBind(boolean bind) {
        this.bind = bind;
        return this;
    }

    /**
     * @return the blank
     */
    public boolean isBlank() {
        return blank;
    }

    /**
     * @param blank the blank to set
     */
    public ExpHandler setBlank(boolean blank) {
        this.blank = blank;
        return this;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param optional the optional to set
     */
    public ExpHandler setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    /**
     * @return the exist
     */
    public boolean isExist() {
        return exist;
    }

    /**
     * @param exist the exist to set
     */
    public ExpHandler setExist(boolean exist) {
        this.exist = exist;
        return this;
    }

    /**
     * @return the all
     */
    public boolean isAll() {
        return all;
    }

    /**
     * @param all the all to set
     */
    public ExpHandler setAll(boolean all) {
        this.all = all;
        return this;
    }
    
}
