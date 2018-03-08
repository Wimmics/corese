package test.kgraph;

import fr.inria.corese.kgengine.api.IModel;
import fr.inria.acacia.corese.triple.parser.*;
import java.util.*;
/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2002</p>
 * <p>Company: </p>
 * @author Olivier Corby & Olivier Savoie
 * @version 1.0
 */

class Param extends Hashtable implements IModel {
	
     public String getParameter(String var){
       Object res = get(var);
       if (res instanceof String[]){
         String[] tab = (String[]) res;
         if (tab.length > 0) return tab[0];
         else return null;
       }
       else return (String) res;
     }

     public String[] getParameterValues(String var) {
       Object res = get(var);
       if (res == null)
         return null;
       else if (res instanceof String[]) {
         return (String[]) res;
       }
       else {
         String tab[] = new String[1];
         tab[0] = (String) res;
         return tab;
       }


     }
     
    
   }
