/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * @author Andrien Basse
 * @author Alban Gaignard
 */
package fr.inria.edelweiss.kgdqp.index;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.time.StopWatch;

@Deprecated
public class IndexPropriete {

    private List<String> propExistencyIndex;  
    private HashMap<String,Long> propCardIndex;  
    
    protected EngineFactory ef = new EngineFactory();
    protected IEngine myCorese;
    protected String repertoire;

    public IndexPropriete(String rep) {
        ef = new EngineFactory();
        repertoire = rep;
        ef.setProperty(EngineFactory.ENGINE_RULE_RUN, "true");
        myCorese = ef.newInstance();
        propExistencyIndex = new ArrayList<String>();
        propCardIndex = new HashMap<String, Long>();
        try {
            System.out.println("Loading file from path :" + repertoire + "\n");
            myCorese.load(repertoire);
            System.out.println(((GraphEngine) myCorese).getGraph().size());
            System.out.println(" fichier charge.\n\n");
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public IResults chargerQuery(String req) {
        try {
            IResults res = myCorese.SPARQLQuery(req);
            return res;
        } catch (EngineException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getSourceI() {
        return propExistencyIndex;
    }

    public void setSourceI(ArrayList<String> sourceI) {
        this.propExistencyIndex = sourceI;
    }

    /*
     * Remplissage de l'index des proprietes
     */
    public void hashPropriete() {
        String requeteCardinality = "select distinct * projection 2000000 "
                + "where"
                + "{"
                + "?sujet ?propriete ?objet"
                + "}"
                + "order by ?propriete"
                + " limit 1000000";
        
        String requeteExistency = "select distinct ?propriete projection 2000000 "
                + "where"
                + "{"
                + "?sujet ?propriete ?objet"
                + "}"
                + "order by ?propriete"
                + " limit 1000000";
        
        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = this.chargerQuery(requeteCardinality);
        for (IResult r : res) {
            String propriete = r.getStringValue("?propriete");
            if(propCardIndex.containsKey(propriete)) {
                long card = propCardIndex.get(propriete);
                card = card + 1;
                propCardIndex.put(propriete, card);
            } else {
                propCardIndex.put(propriete, new Long(1));
            }
        }
        System.out.println("Cardinality index calculated in "+sw.getTime()+" ms");
        sw.reset();
        sw.start();
        
        res = this.chargerQuery(requeteExistency);
        for (IResult r : res) {
            String propriete = r.getStringValue("?propriete");
            propExistencyIndex.add(propriete);
        }
        System.out.println("Existency index calculated in "+sw.getTime()+" ms");
    }
    /*
     * Verifie si  une propriete est instanciee dans la source ou pas.
     * return un boolean
     */

    public boolean verifierProp(String propriete) {
        return propExistencyIndex.contains(propriete);
    }

    public static void main(String arg[]) {
//        IndexPropriete source1 = new IndexPropriete("/Users/gaignard/Desktop/Expe-index-kgram/albanProp/src/base/");
//        source1.hashPropriete();
//        System.out.println("http://xmlns.com/foaf/0.1/givenname dans source 1? " + source1.verifierProp("http://xmlns.com/foaf/0.1/givenname"));
//        System.out.println("http://xmlns.com/foaf/0.1/friend dans source 1? " + source1.verifierProp("http://xmlns.com/foaf/0.1/friend"));
//        IndexPropriete source2 = new IndexPropriete("/Users/gaignard/Desktop/Expe-index-kgram/albanProp/src/base1/");
//        source2.hashPropriete();
//        System.out.println("http://xmlns.com/foaf/0.1/givenname dans source 2? " + source2.verifierProp("http://xmlns.com/foaf/0.1/givenname"));
//        System.out.println("http://xmlns.com/foaf/0.1/friend dans source 2? " + source2.verifierProp("http://xmlns.com/foaf/0.1/friend"));
        
//        IndexPropriete source3 = new IndexPropriete("/Users/gaignard/Desktop/LUBM-1-100K");
        IndexPropriete source3 = new IndexPropriete("/Users/gaignard/Desktop/LUBM-10-1.3M");
        source3.hashPropriete();
        System.out.println("");

    }
}
