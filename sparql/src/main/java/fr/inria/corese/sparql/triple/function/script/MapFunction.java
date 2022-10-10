package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.Expr;
import static fr.inria.corese.kgram.api.core.ExprType.MAPAPPEND;
import static fr.inria.corese.kgram.api.core.ExprType.MAPFIND;
import static fr.inria.corese.kgram.api.core.ExprType.MAPFINDLIST;
import static fr.inria.corese.kgram.api.core.ExprType.MAPLIST;
import static fr.inria.corese.kgram.api.core.ExprType.MAPMERGE;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class MapFunction extends Funcall {  
    
    public MapFunction(){}
    
    public MapFunction(String name){
        super(name);
        setArity(2);
    }
    
//    @Override
//    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
//        return evalnew(eval, b, env, p);
//    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name    = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null) {
            return null;
        }
        Function function = getFunction(eval, b, env, p, name, param.length);
        if (function == null) {
            return null;
        }
                
        Expr exp = this;
        boolean maplist     = exp.oper() == MAPLIST; 
        boolean mapmerge    = exp.oper() == MAPMERGE; 
        boolean mapappend   = exp.oper() == MAPAPPEND; 
        boolean mapfindelem = exp.oper() == MAPFIND;
        boolean mapfindlist = exp.oper() == MAPFINDLIST;
        boolean mapfind     = mapfindelem || mapfindlist;
        boolean hasList     = maplist || mapmerge || mapappend;

        Iterable<IDatatype> iter = null;        
        int k = 0;
        
        for (IDatatype dt : param){  
            if (dt.isList() || dt.isLoop()) {
                iter = dt;
                break;
            }
            else {
                k++;
            }
        }               
        if (iter == null){
            return null;
        }
        IDatatype[] value = param; //new IDatatype[param.length];
        ArrayList<IDatatype> res = (hasList)     ? new ArrayList<>() : null;
        ArrayList<IDatatype> sub = (mapfindlist) ? new ArrayList<>() : null;
        
        for (IDatatype elem : iter){ 
            value[k] = elem;
            // call function on value parameter list
            IDatatype val = call(eval, b, env, p, function, value);  
            if (val == null){
                return null;
            }           
                       
            if (hasList) {
               res.add(val);
            }
            else if (mapfindelem && val.booleanValue()) {
                return elem;
            } else if (mapfindlist && val.booleanValue()) {
                // select elem whose predicate is true
                // mapselect (xt:prime, xt:iota(1, 100))
                sub.add(elem);
            }         
        }
        
        if (mapmerge || mapappend){
            ArrayList<IDatatype> mlist = new ArrayList<>();
            for (IDatatype dt : res){
                if (dt.isList()){
                    for (IDatatype v : dt.getValues()){
                        add(mlist, v, mapmerge);
                    }
                }
                else {
                    add(mlist, dt, mapmerge);
                }
            }
            return DatatypeMap.createList(mlist);
        }
        else if (maplist){
            return DatatypeMap.createList(res); 
        }
        else if (mapfindlist){
            return DatatypeMap.createList(sub);
        }
        else if (mapfindelem){
            return null;
        }
        return TRUE;
    }
    
    //@Override
//    public IDatatype evalold(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
//        IDatatype name    = getBasicArg(0).eval(eval, b, env, p);
//        IDatatype[] param = evalArguments(eval, b, env, p, 1);
//        if (name == null || param == null) {
//            return null;
//        }
//        Function function = getFunction(eval, b, env, p, name, param.length);
//        if (function == null) {
//            return null;
//        }
//                
//        Expr exp = this;
//        boolean maplist     = exp.oper() == MAPLIST; 
//        boolean mapmerge    = exp.oper() == MAPMERGE; 
//        boolean mapappend   = exp.oper() == MAPAPPEND; 
//        boolean mapfindelem = exp.oper() == MAPFIND;
//        boolean mapfindlist = exp.oper() == MAPFINDLIST;
//        boolean mapfind     = mapfindelem || mapfindlist;
//        boolean hasList     = maplist || mapmerge || mapappend;
//
//        Iterable<IDatatype> iter = null;
//        boolean isList = false, isLoop = false;
//        
//        int k = 0;
//        for (IDatatype dt : param){  
//            if (dt.isList() && ! isList && ! isLoop){
//                isList = true;
//                iter = dt;
//            }
//            else if (dt.isLoop()) {
//                if (! isList && ! isLoop) {
//                    isLoop = true;
//                    iter = dt;
//                }
//                else {
//                    // list + loop || loop + loop
//                    // additional Loop -> toList()
//                    param[k] = dt.toList();
//                }
//            }
//            
//            k++;
//        }               
//        if (iter == null){
//            return null;
//        }
//        IDatatype[] value = new IDatatype[param.length];
//        ArrayList<IDatatype> res = (hasList)     ? new ArrayList<>() : null;
//        ArrayList<IDatatype> sub = (mapfindlist) ? new ArrayList<>() : null;
//        int i = 0;
//        
//        // there may be several List, but at most one Loop
//        for (IDatatype dtValue : iter){ 
//            IDatatype elem = null;
//            
//            for (int j = 0; j<value.length; j++){
//                IDatatype dt = param[j];
//                if (dt.isList()){                                        
//                    value[j] = getValue(dt, i);
//                    if (mapfind && elem == null){
//                        elem = value[j];
//                    }
//                }
//                else if (dt.isLoop()) {
//                    value[j] = dtValue;
//                    if (mapfind && elem == null) {
//                        elem = value[j];
//                    }
//                }
//                else {
//                    value[j] = dt;
//                }
//            }
//            
//            i++;
//           
//            // call function on value parameter list
//            IDatatype val = call(eval, b, env, p, function, value);                      
//            if (val == null){
//                return null;
//            }           
//                       
//            if (hasList) {
//               res.add(val);
//            }
//            else if (mapfindelem && val.booleanValue()){
//                return elem;
//            }
//            else if (mapfindlist && val.booleanValue()){
//                    // select elem whose predicate is true
//                    // mapselect (xt:prime, xt:iota(1, 100))
//                    sub.add(elem);
//            }            
//        }
//        
//        if (mapmerge || mapappend){
//            ArrayList<IDatatype> mlist = new ArrayList<>();
//            for (IDatatype dt : res){
//                if (dt.isList()){
//                    for (IDatatype v : dt.getValues()){
//                        add(mlist, v, mapmerge);
//                    }
//                }
//                else {
//                    add(mlist, dt, mapmerge);
//                }
//            }
//            return DatatypeMap.createList(mlist);
//        }
//        else if (maplist){
//            return DatatypeMap.createList(res); 
//        }
//        else if (mapfindlist){
//            return DatatypeMap.createList(sub);
//        }
//        else if (mapfindelem){
//            return null;
//        }
//        return TRUE;
//    }
    
    // if list size is <= i,  focus on last element of the list
    // use case: maplist(?fun, ?list, xt:list(?lst))
    // The second ?lst argument is itself a list and we do not want to iterate this one
    IDatatype getValue(IDatatype dt, int i) {
        return (i < dt.size()) ? dt.get(i) : dt.get(dt.size() - 1);
    }
    
    void add(List<IDatatype> list, IDatatype dt, boolean merge){
        if (merge){
            if (! list.contains(dt)){
                list.add(dt);
            }
        }
        else {
            list.add(dt);
        }
    }
 
   
}
