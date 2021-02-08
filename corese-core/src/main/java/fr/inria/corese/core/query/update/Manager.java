package fr.inria.corese.core.query.update;


import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;


/**
 * SPARQL 1.1 Update
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public interface Manager {

        // basic operations: load, copy, etc.
	boolean process(Query q, Basic b, Dataset ds) throws EngineException ;

        /**
         * For each Mapping
         *   instantiate delete/insert template and delete/insert it 
         * For insert with blank, each mapping generates a new blank 
         * template may contain a graph pattern
         * if there is a dataset with from and no graph pattern, delete edges in dataset from
         * (sparql compliance)
         * 
         */
        void delete(Query q, Mappings map, Dataset ds);

        void insert(Query q, Mappings map, Dataset ds);
        
        void setLevel(Level l);
        
        void setAccessRight(AccessRight access);
        	

}
