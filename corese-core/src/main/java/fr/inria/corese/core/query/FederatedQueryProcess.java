package fr.inria.corese.core.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.util.Arrays;

/**
 * Specific class for Federated Query Processing
 * Define a federation
 * Execute query on federation
 */
public class FederatedQueryProcess  {
    
    private QueryProcess queryProcess; 
    private Metadata metadata;
    
    
    public FederatedQueryProcess() {
        setQueryProcess(QueryProcess.create());
        setMetadata(new Metadata());
    }
    
    public FederatedQueryProcess(Graph g) {
        setQueryProcess(QueryProcess.create(g));
    }
    
    public FederatedQueryProcess defineFederation(String name, String... url) {
        getQueryProcess().defineFederation(name, Arrays.asList(url));
        getMetadata().add(Metadata.FEDERATION, name);
        return this;
    }
    
    public Mappings query(String query) throws EngineException {
        getQueryProcess().setMetadata(getMetadata());
        return getQueryProcess().query(query);
    }

    public Query compile(String query) throws EngineException {
        getQueryProcess().setMetadata(getMetadata());
        return getQueryProcess().compile(query);
    }
    
    
    
    
    
    
    public FederatedQueryProcess setLimit(int n) {
        getMetadata().add(Metadata.LIMIT, DatatypeMap.newInstance(n));
        return this;
    }
    
    public FederatedQueryProcess setTimeout(int n) {
        getMetadata().add(Metadata.TIMEOUT, DatatypeMap.newInstance(n));
        return this;
    }    
    
    public FederatedQueryProcess setTrace(boolean b) {
        if (b) {
            getMetadata().add(Metadata.TRACE);
        }
        else {
            getMetadata().remove(Metadata.TRACE);
        }
        return this;
    } 
    
    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
