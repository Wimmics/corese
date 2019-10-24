/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.core.extension;

import fr.inria.corese.core.query.PluginImpl;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author corby
 */
public class Core extends PluginImpl {
    
    void xt_print(IDatatype... dt) {
    }
    
    IDatatype xt_load(IDatatype... dt) {
        return null;
    }
    
    IDatatype xt_graph() {
        return null;
    }
    
    IDatatype xt_value(IDatatype g, IDatatype s, IDatatype p) {
        return null;
    }
    
    IDatatype xt_value(IDatatype s, IDatatype p) {
        return null;
    }
    
    IDatatype xt_set(IDatatype... a) {
        return null;
    }
    
    IDatatype xt_objects(IDatatype... a) {
        return null;
    }
    
    IDatatype xt_subjects(IDatatype... a) {
        return null;
    }
    
    IDatatype xt_grest(IDatatype... a) {
        return null;
    }
    
    IDatatype xt_insert(IDatatype... a) {
        return null;
    }
    
    IDatatype xt_focus(IDatatype g, IDatatype e) {
        return null;
    }
    
    IDatatype xt_turtle(IDatatype... dt) {
        return null;
    }
    
    IDatatype mapany(IDatatype... dt) {
        return TRUE;
    }
    
    IDatatype mapevery(IDatatype... dt) {
        return TRUE;
    }
    
    IDatatype mapfindlist(IDatatype... dt) {
        return TRUE;
    }
    
    IDatatype dt_list(IDatatype dt) {
        return null;
    }
    
    IDatatype xt_strip(IDatatype dt) {
        return dt;
    }
    
    IDatatype xt_map() {
        return DatatypeMap.map();
    }
    
    IDatatype xt_validURI(IDatatype dt) {
        return dt;
    }
    
    IDatatype xt_sparql(IDatatype... dt)  {
        return null;
    } 
    
    IDatatype xt_replace(IDatatype... dt)  {
        return null;
    } 
    
    IDatatype st_get(IDatatype... dt)  {
        return null;
    }  
    
    IDatatype st_visit(IDatatype... dt)  {
        return null;
    } 
     
    IDatatype st_apply_templates_with_graph(IDatatype... dt)  {
        return null;
    } 
    
    IDatatype st_turtle(IDatatype... dt)  {
        return null;
    } 
    
    IDatatype rq_strstarts(IDatatype... dt)  {
        return null;
    } 
    
    
}
