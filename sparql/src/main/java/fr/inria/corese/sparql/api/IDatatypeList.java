/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.api;

/**
 *
 * @author corby
 */
public interface IDatatypeList {
    
    IDatatype length();
    
    IDatatype first();
    
    IDatatype rest();
    
    IDatatype get(IDatatype ind);

    IDatatype set(IDatatype ind, IDatatype val);

    IDatatype remove(IDatatype ind);

    IDatatype member(IDatatype elem);

    IDatatype cons(IDatatype elem);
    
    IDatatype add(IDatatype elem);
    
    IDatatype add(IDatatype ind, IDatatype val);
    
    IDatatype swap(IDatatype i1, IDatatype i2);

    IDatatype reverse();

    IDatatype append(IDatatype list);
    
    IDatatype merge();

    IDatatype merge(IDatatype list);
    
    IDatatype sort();
}
