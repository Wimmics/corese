package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.extension.CoreseMap;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server Event Manager
 * Record in a map of maps, information about server service call 
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 */
public class EventManager {

    private static Logger logger = LogManager.getLogger(EventManager.class);
    static final String count = NSManager.STL+"count";
    static final String date  = NSManager.STL+"date";
    static final String host  = NSManager.STL+"host";
    static final String template = "/template";
    private static EventManager singleton;
    
    // service|profile -> count
    private CoreseMap globalMap, countMap, dateMap, hostMap;
    
    static {
        setSingleton(new EventManager());
    }
    
    EventManager() {
        init();
    }
    
    void init() {
        setCountMap(map());
        setDateMap(map());
        setHostMap(map());
        
        globalMap = map();
        DatatypeMap.setPublicDatatypeValue(globalMap);
        Binding.setStaticVariable("?staticEventManagerMap", globalMap);
        
        globalMap.set(count, getCountMap());
        globalMap.set(date,  getDateMap());
        globalMap.set(host,  getHostMap());
    }
    
    CoreseMap map () {
        return DatatypeMap.map();
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
        logger.info(globalMap.getMap());
        logger.info(getCountMap().getMap());
        logger.info(getDateMap().getMap());
        logger.info(getHostMap().getMap());
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

    public static EventManager getSingleton() {
        return singleton;
    }

    public static void setSingleton(EventManager aSingleton) {
        singleton = aSingleton;
    }
    
}
