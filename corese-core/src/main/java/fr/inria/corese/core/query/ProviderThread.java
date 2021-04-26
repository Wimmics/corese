package fr.inria.corese.core.query;

import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class ProviderThread extends Thread {

    private ProviderService provider;
    Query q;
    private URLServer service;
    Mappings map, sol;
    boolean slice;
    int length;
    int timeout;
    
    ProviderThread(ProviderService p, URLServer service, Mappings map, Mappings sol, boolean slice, int length, int timeout){
        provider = p;
        this.service = service;
        this.map = map;
        this.sol = sol;
        this.slice = slice;
        this.length = length;
        this.timeout = timeout;
    }
    
    @Override
    public void run() {
        process();
    }
    
    void process() {
        try {
            getProvider().process(service, map, sol, slice, length, timeout);
        } catch (EngineException ex) {           
            ProviderImpl.logger.error(ex.getMessage());
        }
    }
       
    public URLServer getService() {
        return service;
    }
    
    public void setService(URLServer service) {
        this.service = service;
    }

    /**
     * @return the provider
     */
    public ProviderService getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(ProviderService provider) {
        this.provider = provider;
    }
    

}
