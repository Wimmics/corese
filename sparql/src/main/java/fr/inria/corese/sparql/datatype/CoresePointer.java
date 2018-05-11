package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.CoreseDatatype.getGenericDatatype;
import fr.inria.corese.kgram.api.core.Loopable;
import fr.inria.corese.kgram.api.core.Pointerable;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class CoresePointer extends CoreseUndefLiteral {
    private static final IDatatype dt           = getGenericDatatype(IDatatype.POINTER);
    private static final IDatatype graph_dt     = getGenericDatatype(IDatatype.GRAPH_DATATYPE);
    private static final IDatatype triple_dt    = getGenericDatatype(IDatatype.TRIPLE_DATATYPE);
    private static final IDatatype mappings_dt  = getGenericDatatype(IDatatype.MAPPINGS_DATATYPE);
    private static final IDatatype mapping_dt   = getGenericDatatype(IDatatype.MAPPING_DATATYPE);
    private static final IDatatype context_dt   = getGenericDatatype(IDatatype.CONTEXT_DATATYPE);
    private static final IDatatype nsmanager_dt = getGenericDatatype(IDatatype.NSM_DATATYPE);
    private static final IDatatype annotation_dt= getGenericDatatype(IDatatype.METADATA_DATATYPE);
    private static final IDatatype expression_dt= getGenericDatatype(IDatatype.EXPRESSION_DATATYPE);

    Pointerable pobject;
    
    CoresePointer (Pointerable obj){
        this("pointer", obj);
    }
        
    CoresePointer (String name, Pointerable obj){
        super(name);
        pobject = obj;
    } 
    
    @Override
    public IDatatype getDatatype() {
        switch (pointerType()){
            case Pointerable.EDGE_POINTER:    return triple_dt;
            case Pointerable.GRAPH_POINTER:     return graph_dt;
            case Pointerable.MAPPINGS_POINTER:  return mappings_dt;
            case Pointerable.MAPPING_POINTER:   return mapping_dt;
            case Pointerable.CONTEXT_POINTER:   return context_dt;
            case Pointerable.NSMANAGER_POINTER: return nsmanager_dt;
            case Pointerable.METADATA_POINTER:  return annotation_dt;
            case Pointerable.EXPRESSION_POINTER:return expression_dt;
            default: return dt;
        }
    }
    
    @Override
    public Pointerable getPointerObject(){
        return pobject;
    }
    
    @Override
    public int pointerType(){
        return pobject.pointerType();
    }
    
    @Override
    public boolean isPointer(){
        return true;
    }
    
    @Override
    public Object getObject(){
        return pobject;
    }
    
    @Override
    public boolean isLoop(){
        if (pobject == null){
            return false; 
        }
        switch (pobject.pointerType()){
            case EXPRESSION_POINTER: return false;
            default: return true;
        }
    }
           
    @Override
    public Iterable getLoop(){
        return ((Loopable) pobject).getLoop();
    }
    
    @Override
    public IDatatype display(){
       return DatatypeMap.createUndef(getContent(), getDatatypeURI());
    }
 
    public String display2(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(getContent()).append("\"");
        sb.append("^^").append(nsm.toPrefix(getDatatypeURI()));
        return sb.toString();
    }

}
