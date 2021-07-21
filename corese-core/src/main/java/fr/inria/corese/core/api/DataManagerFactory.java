package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import java.util.List;

public interface DataManagerFactory {
    
    /**
     * 
     * @param queryGraphNode  query named graph URI or variable or null
     * @param targetGraphNode target graph name if any
     * @param dataset: from or from named or null
     * @param skipMetadataNode default is false
     * @param access access right manager
     * @return 
     */
    DataManager newInstance(Node queryGraphNode, Node targetGraphNode, List<Node> dataset, boolean skipMetadataNode, AccessRight access);
    
}
