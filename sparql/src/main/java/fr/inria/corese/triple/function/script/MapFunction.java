package fr.inria.corese.triple.function.script;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.Expr;
import static fr.inria.edelweiss.kgram.api.core.ExprType.MAPAPPEND;
import static fr.inria.edelweiss.kgram.api.core.ExprType.MAPFIND;
import static fr.inria.edelweiss.kgram.api.core.ExprType.MAPFINDLIST;
import static fr.inria.edelweiss.kgram.api.core.ExprType.MAPLIST;
import static fr.inria.edelweiss.kgram.api.core.ExprType.MAPMERGE;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class MapFunction extends Funcall {  
    
    public MapFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype name    = getArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null) {
            return null;
        }
        Function function = (Function) eval.getDefineGenerate(this, env, name.stringValue(), param.length);
        if (function == null) {
            return null;
        }
        
       //return eval.map(name, args, this, env, p);
        
        Expr exp = this;
        boolean maplist     = exp.oper() == MAPLIST; 
        boolean mapmerge    = exp.oper() == MAPMERGE; 
        boolean mapappend   = exp.oper() == MAPAPPEND; 
        boolean mapfindelem = exp.oper() == MAPFIND;
        boolean mapfindlist = exp.oper() == MAPFINDLIST;
        boolean mapfind     = mapfindelem || mapfindlist;
        boolean hasList     = maplist || mapmerge || mapappend;

        IDatatype list = null;
        IDatatype ldt = null;
        Iterator<IDatatype> loop = null ;
        boolean isList = false, isLoop = false;
        
        int k = 0;
        for (IDatatype dt : param){  
            if (dt.isList() && ! isList && ! isLoop){
                isList = true;
                list = dt;
            }
            else if (dt.isLoop()) {
                if (! isList && ! isLoop) {
                    isLoop = true;
                    ldt = dt;
                    loop = ldt.iterator();
                }
                else {
                    // list + loop || loop + loop
                    // loop.toList()
                    param[k] = dt.toList();
                }
            }
            
            k++;
        }               
        if (list == null && ldt == null){
            return null;
        }
        IDatatype[] value = new IDatatype[param.length];
        ArrayList<IDatatype> res = (hasList)     ? new ArrayList<IDatatype>() : null;
        ArrayList<IDatatype> sub = (mapfindlist) ? new ArrayList<IDatatype>() : null;
        int size = 0; 
        
        for (int i = 0;  (isList) ? i< list.size() : loop.hasNext(); i++){ 
            IDatatype elem = null;
            
            for (int j = 0; j<value.length; j++){
                IDatatype dt = param[j];
                if (dt.isList()){                   
                    /**
                     * if list size is <= i,  focus on last element of the list
                     * use case: maplist(?fun, ?list, xt:list(?lst))
                     * The second ?lst argument is itself a list and we do not want to iterate this one
                     */  
                    value[j] = (i < dt.size()) ? dt.get(i) : dt.get(dt.size()-1);
                    if (mapfind && elem == null){
                        elem = value[j];
                    }
                }
                else if (isLoop && dt.isLoop()){
                    // TODO: track several dt Loop
                    if (loop.hasNext()){
                       value[j] = loop.next(); 
                       if (mapfind && elem == null){
                         elem = value[j];
                       }
                    }
                    else {
                        return null;
                    }
                }
                else {
                    value[j] = dt;
                }
            }
                        
            // call function on value parameter list
            IDatatype val = call(eval, b, env, p, function, value);                      
            if (val == null){
                return null;
            }           
                       
            if (hasList) {
                if (val.isList()){
                    size += val.size();
                }
                else {
                    size += 1;
                }
               res.add(val);
            }
            else if (mapfindelem && val.booleanValue()){
                return elem;
            }
            else if (mapfindlist && val.booleanValue()){
                    // select elem whose predicate is true
                    // mapselect (xt:prime, xt:iota(1, 100))
                    sub.add(elem);
            }
            
        }
        
        if (mapmerge || mapappend){
            int i = 0;
            ArrayList<IDatatype> mlist = new ArrayList<IDatatype>();
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
