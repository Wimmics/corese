package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Metadata implements Iterable<String> {
    
    ArrayList<String> list;
    
    public Metadata(){
        list = new ArrayList<String>();
    }
    
    public void add(String str){
        list.add(str);
    }
    
    public ArrayList<String> getList(){
        return list;
    }
    
    public Iterator<String> iterator(){
            return list.iterator();
    }

}
