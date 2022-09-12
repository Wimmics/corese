package fr.inria.corese.core.producer;

import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.logic.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Corese object associated to DataManager
 * enables corese core to manage additional data such as Distance
 */
public class MetadataManager {
    private static Logger logger = LoggerFactory.getLogger(MetadataManager.class);

    private DataManager dataManager;
    private Distance distance;
    private boolean debug = true;
    
    
    public MetadataManager() {    
    }
    
    public MetadataManager(DataManager man) {
        setDataManager(man);
        startDataManager();
    }
    
    void startDataManager() {
        trace("create data manager");
    }
    
    public void endDataManager() {
        trace("end data manager");
    }
    
    public void startReadTransaction() {
        trace("start read");
    }
    
    public void endReadTransaction() {
        trace("end read");
    }
    
    public void startWriteTransaction() {
        trace("start write");
    }
    
    public void endWriteTransaction() {
        clean();
        trace("end write");
    }
    
    void clean() {
        setDistance(null);    
    }
    

    public Distance getCreateDistance() {
        if (getDistance() == null) {
            setDistance(new Distance(getDataManager()));
            getDistance().start();
        }
        return getDistance();
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    void trace(String mes) {
        if (isDebug()) {
            logger.info(
                String.format("%s %s", mes,
                        getDataManager().getStoragePath()));
        }
    }

   
    
}
