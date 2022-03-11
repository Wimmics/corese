package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseUndefLiteral;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.triple.parser.Expression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Extension IDatatype that contains LDScript Pointerable object
 * These objects implement an API that enables them to be processed by LDScript
 * statements such as for (?e in ?g), xt:gget(?e, "?x", 0) mainly speaking they are iterable
 * Pointerable objects have specific extension datatypes such as dt:graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class CoresePointer extends CoreseUndefLiteral {
       
    private static HashMap<PointerType, IDatatype> map;

    Pointerable pobject;
    
    static {
        init();
    }
    
    static void init() {
        map = new HashMap<>();
        for (PointerType type : PointerType.values()) {
            map.put(type, getGenericDatatype(type.getName()));
        }
    }
    
    
    public CoresePointer (Pointerable obj){
        this(obj.getDatatypeLabel(), obj);
    }
        
    public CoresePointer (String name, Pointerable obj){
        super(name);
        pobject = obj;
    } 
    
    @Override
    public IDatatype getDatatype() {
        return getDatatype(pointerType());
    }
    
    public static IDatatype getDatatype(PointerType t) {
        return map.get(t);
    }
    
    @Override
    public Pointerable getPointerObject(){
        return pobject;
    }
    
    @Override
    public PointerType pointerType(){
        if (pobject == null) {
            return PointerType.UNDEF;
        }
        return pobject.pointerType();
    }
    
    @Override
    public boolean isPointer(){
        return true;
    }
    
     @Override
    public boolean isExtension() {
        return pointerType() != PointerType.UNDEF;
    }
    
     @Override
    public boolean isUndefined() {
        return ! isExtension();
    }
    
    @Override
    public Object getNodeObject(){
        // use case: pobject = PointerObject(object)
        return pobject.getPointerObject();
    }
    
    @Override
    public Path getPath() {
        if (pointerType() != PointerType.PATH || getPointerObject() == null) {
            return null;
        }
        return getPointerObject().getPathObject();
    }
    
    @Override
    public void setObject(Object obj) {
        if (obj instanceof Pointerable) {
            pobject = (Pointerable) obj;
        }
    }
    
    @Override
    public boolean isLoop(){
        if (pobject == null){
            return false; 
        }
        switch (pobject.pointerType()){
            // expression must not be loopable in map(fun, exp)
            case EXPRESSION: return false;
            default: return true;
        }
    }
    
    @Override
    public int size() {
        return getPointerObject().size();
    }
    
    @Override
    public List<IDatatype> getValueList() {   
         if (pobject == null){
            return new ArrayList<>(); 
        }
        switch (pobject.pointerType()){
            case EXPRESSION: 
               return ((Expression) pobject).getValueList();
               
            case STATEMENT:
                return getValueList(pobject.getStatement());
               
            default: return super.getValueList();
        }
    }
    
    @Override
    public IDatatype getValue(String var, int ind) {
        return DatatypeMap.getValue(pobject.getValue(var, ind));
    }
    
    List<IDatatype> getValueList(Exp exp) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Exp e : exp) {
            list.add(DatatypeMap.createObject(e));
        }
        return list;
    }
           
    @Override
    public Iterable getLoop(){
        switch (pobject.pointerType()) {
            case STATEMENT:
                return getValueList();
        }
        return pobject.getLoop();
    }
    
    @Override
    public IDatatype display(){
       return DatatypeMap.createUndef(getContent(), getDatatypeURI());
    }
 
    public String display2(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(getContent()).append("\"");
        sb.append("^^").append(nsm().toPrefix(getDatatypeURI()));
        return sb.toString();
    }
    
     @Override
    public boolean equalsWE(IDatatype dt) throws CoreseDatatypeException {
        if (dt.isPointer()) {
            if (getPointerObject() == null || dt.getPointerObject() == null) {
                return getPointerObject() == dt.getPointerObject();
            }
            return getPointerObject().equals(dt.getPointerObject());
        }
        if (dt.isExtension()) {
            return false;
        }
        return super.equalsWE(dt);
    }
    
    public boolean equalsWE2(IDatatype dt) throws CoreseDatatypeException {
        if (dt.getCode() != UNDEF || getDatatype()!= dt.getDatatype()) {
            return super.equalsWE(dt);
        }
        if (getPointerObject() == null || dt.getPointerObject() == null) {
            return getPointerObject() == dt.getPointerObject();
        }        
        return getPointerObject().equals(dt.getPointerObject());
    }
    
    /**
     * Pragma: they have same pointer type
     */
    @Override
    public int defaultCompare(IDatatype d2) {
        return getPointerObject().compare(d2.getPointerObject());
    }
    
    @Override
    public IDatatype set(IDatatype key, IDatatype value) {
        switch (getDatatypeURI()) {
            case GRAPH_DATATYPE:
                getPointerObject().getTripleStore()
                        .set(key, value);
                break;
        }
        return value;
    }
        

}
