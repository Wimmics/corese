package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseMap;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server Event Manager
 * Record in a map of maps, information about server service call 
 * This map is accessible via a SPARQL Query with LDScript function ds:getPublicDatatypeValue(true)
 * 
 * bind (ds:getPublicDatatypeValue(true) as ?map)
 * bind (xt:get(?map, st:count) as ?count)
 * values (?key ?val) { unnest(?count)  }
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 */
public class EventManager {

    private static Logger logger = LogManager.getLogger(EventManager.class);
    static final String count = NSManager.STL+"count";
    static final String date  = NSManager.STL+"date";
    static final String host  = NSManager.STL+"host";
    static final String template = "/template";
    // service|profile -> count
    private CoreseMap countMap, dateMap, hostMap;
    
    EventManager() {
        init();
    }
    
    void init() {
        setCountMap(DatatypeMap.map());
        setDateMap(DatatypeMap.map());
        setHostMap(DatatypeMap.map());
        
        // can be retrieved in LDScript by ds:getPublicDatatypeValue(true)
        CoreseMap globalMap = DatatypeMap.map();
        DatatypeMap.setPublicDatatypeValue(globalMap);
        
        globalMap.set(count, getCountMap());
        globalMap.set(date,  getDateMap());
        globalMap.set(host,  getHostMap());
    }
    
    /**
     * One server call
     */
    synchronized void call(Context context) {
        record(context);
        log(context);
    }
    
    void log(Context context) {
        logger.info("Workflow Context:\n" + context);
        logger.info(getCountMap());
    }
    
    /**
     * 
     */
    void record(Context c) {
        IDatatype dtserv = getService(c);
        if (dtserv != null) {
           getCountMap().incr(dtserv);
           getDateMap().set(dtserv, DatatypeMap.newDate());
        }

        IDatatype dthost = c.get(Context.STL_REMOTE_HOST);
        if (dthost != null) {
            getHostMap().incr(dthost);           
        }
    }
      
    IDatatype getService(Context c) {
        IDatatype dt = c.get(Context.STL_SERVICE);
        if (dt == null) {
            return null;
        }
        else if (dt.getLabel().equals(template)){
            IDatatype pr = c.get(Context.STL_PROFILE);
            return (pr == null) ? dt : pr;
        }
        return dt;
    }

    /**
     * @return the dateMap
     */
    public CoreseMap getDateMap() {
        return dateMap;
    }

    /**
     * @param dateMap the dateMap to set
     */
    public void setDateMap(CoreseMap dateMap) {
        this.dateMap = dateMap;
    }

    /**
     * @return the hostMap
     */
    public CoreseMap getHostMap() {
        return hostMap;
    }

    /**
     * @param hostMap the hostMap to set
     */
    public void setHostMap(CoreseMap hostMap) {
        this.hostMap = hostMap;
    }
    
    /**
     * @return the countMap
     */
    CoreseMap getCountMap() {
        return countMap;
    }

    /**
     * @param countMap the countMap to set
     */
    void setCountMap(CoreseMap countMap) {
        this.countMap = countMap;
    }
    
}
