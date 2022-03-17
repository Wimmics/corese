package fr.inria.corese.core.producer;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import java.util.ArrayList;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 * Simple filter applied to object or subject of current edge
 * during iteration 
 * iterate().filter(">=", 10)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataFilter implements ExprType {
    public static final int SUBJECT = 0;
    public static final int OBJECT = 1;
    public static final int PROPERTY = -2;
    public static final int GRAPH = Graph.IGRAPH;
    // when true:  return nested and asserted edge
    // when false: return asserted target edge for asserted query edge
    // and every edge for nested query edge
    public static boolean RDF_STAR_SELECT = false;
    
    private int test;
    int index, other = 1;
    byte level = AccessRight.DEFAULT;
    private AccessRight accessRight;
    private boolean nested = false;
    
    private IDatatype value;
    
    DataFilter(){}
    
    public DataFilter(int test){
        this(test, null, 1);
        complete();
    }
    
    public DataFilter(int test, int i1, int i2){
        this(test, null, i1);
        other = i2;
    }
    
    public DataFilter(int test, int index){
       this(test, null, index);
    }
    
    public DataFilter(int test, byte level){
       this.test = test;
       this.level = level;
    }
     
    public DataFilter(int test, AccessRight ac){
       this.test = test;
       setAccessRight(ac);
    } 
    
    public DataFilter(int test, boolean nested){
       this.test = test;
       setNested(nested);
    } 
    
    public DataFilter(int test, IDatatype dt){
        this(test, dt, 1);
    }
    
    public DataFilter(int test, IDatatype dt, int index){
        this.test = test;
        this.value = dt;
        this.index = index;
    }
    
    DataFilter add(DataFilter f){
        return this;
    }
    
    boolean setFilter(DataFilter f){
        return true;
    }
    
    ArrayList<DataFilter> getList(){
        return null;
    }
    
    boolean isBoolean(){
        return false;
    }
    
    void complete(){
        switch (test){
            case EQ:
            case NEQ:
                if (value == null){
                    index = 0;
                    other = 1;
                }
        }
    }
    
    boolean result(boolean b){
        return b;
    }
    
    /**
     * true means if test fail, stop iteration
     * 
     */
    boolean fail(){
        switch (test){
            case EDGE_LEVEL: return true;
            default: return false;
        }
    }
    
    Node getNode(Edge ent, int n){
        switch (n){
            case PROPERTY: return ent.getEdgeNode();
            default: return ent.getNode(n);
        }
    }
    
    IDatatype getValue(Edge ent, int n){
        return  getNode(ent, n).getValue();
    }
    
    boolean eval(Edge edge) {
        switch (getOper()) {

            // Rule Engine optimization require edge with index >= index
            case EDGE_LEVEL:
                return result(edge.getEdgeIndex() >= index);
                
            case EDGE_ACCESS:
                return getAccessRight().acceptWhere(edge.getLevel());  
                
            case EDGE_NESTED:
                // if query edge is
                // nested:   asserted edge ok, nested edge ok
                // asserted: asserted edge ok, nested edge not ok
                return (isNested() || RDF_STAR_SELECT) ? true : edge.isAsserted();
                
            default:
                IDatatype dt =  getValue(edge, index);
                IDatatype dt2 = getValue();
                if (dt2 == null){
                    dt2 =  getValue(edge, other);
                }
                try {
                    switch (getOper()) {
                      
                        case GT:
                            return result(dt.greater(dt2));
                        case GE:
                            return result(dt.greaterOrEqual(dt2));
                        case LE:
                            return result(dt.lessOrEqual(dt2));
                        case LT:
                            return result(dt.less(dt2));
                        case EQ:
                            return result(dt.equals(dt2));
                        case NEQ:
                            return result(!dt.equals(dt2));
                        case CONTAINS:
                            return result(dt.contains(dt2));
                        case STARTS:
                            return result(dt.startsWith(dt2));
                        case IN:
                            return result(in(dt, dt2));

                        case ISURI:
                            return result(dt.isURI());

                        case ISLITERAL:
                            return result(dt.isLiteral());

                        case ISBLANK:
                            return result(dt.isBlank());

                        case ISSKOLEM:
                            return result(dt.isSkolem());

                        case ISNUMERIC:
                            return result(dt.isNumber());

                        default:
                            return true;
                    }
                } catch (CoreseDatatypeException e) {
                    return false;
                }
        }
    }
    
    boolean in(IDatatype dt, IDatatype dl){
        if (dl.isList()){
            for (IDatatype val : dl.getValues()){
                if (val.equals(val)){
                    return true;
                }
            }
            return false;
        }
        return dt.equals(dl);
    }

    /**
     * @return the test
     */
    public int getOper() {
        return test;
    }

    /**
     * @return the value
     */
    public IDatatype getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(IDatatype value) {
        this.value = value;
    }

    /**
     * @return the accessRight
     */
    public AccessRight getAccessRight() {
        return accessRight;
    }

    /**
     * @param accessRight the accessRight to set
     */
    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }

    public boolean isNested() {
        return nested;
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }
    

}
