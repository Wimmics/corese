package fr.inria.corese.sparql.api;

/**
 *
 * @author corby
 */
public interface IDatatypeList extends IDatatype {
        
    IDatatype first();
    
    IDatatype rest();
    IDatatype rest(IDatatype ind);
    IDatatype rest(IDatatype ind, IDatatype last);
    
    IDatatype get(IDatatype ind);
    IDatatype last(IDatatype ind);

    IDatatype set(IDatatype ind, IDatatype val);

    IDatatype remove(IDatatype elem);
    IDatatype remove(int ind);

    IDatatype cons(IDatatype elem);
    
    IDatatype add(IDatatype elem);
    IDatatype addAll(IDatatype list);
    
    IDatatype add(IDatatype ind, IDatatype val);
    
    IDatatype swap(IDatatype i1, IDatatype i2);

    IDatatype reverse();

    IDatatypeList append(IDatatype list);
    
    IDatatype merge();

    IDatatype merge(IDatatype list);
    
    IDatatype sort();
}
