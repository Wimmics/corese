package fr.inria.corese.core.shacl;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.extension.SHACL;
import fr.inria.corese.core.query.ProducerImpl;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;

/**
 * Java SHACL Interpreter
 * Result of compiling LDScript SHACL Interpreter
 * 
 * @author Olivier Corby, Wimmics, INRIA, 2019
 */
public class ShaclJava {
    Graph g;
    
    public ShaclJava(Graph g) {
        this.g = g;
    }
    
    public Graph eval() {
        SHACL ext = extension(g);
        IDatatype dt = ext.sh_shacljava();
        Graph res = (Graph) dt.getPointerObject();
        return res;
    }
          
    SHACL extension(Graph g) {
        Memory mem = new Memory();
        mem.init(new Query());
        mem.setBind(Binding.create());
        
        SHACL ext = new SHACL();
        ext.setEnvironment(mem);
        ext.setProducer(new ProducerImpl(g));
        return ext;
    }
    
    
}
