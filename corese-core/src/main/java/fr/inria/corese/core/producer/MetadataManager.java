package fr.inria.corese.core.producer;

import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.logic.Distance;

/**
 * Corese object associated to DataManager
 * enables corese core to manage additional data such as Distance
 */
public class MetadataManager {

    private DataManager dataManager;
    private Distance distance;
    private boolean debug = false;
    
    
    public MetadataManager() {    
    }
    
    public MetadataManager(DataManager man) {
        setDataManager(man);
    }
    
    public void startReadTransaction() {
        traceStart("read");
    }
    
    public void endReadTransaction() {
        traceEnd("read");
    }
    
    public void startWriteTransaction() {
        traceStart("write");
    }
    
    public void endWriteTransaction() {
        clean();
        traceEnd("write");
    }
    
    void clean() {
        setDistance(null);    
    }
    
    void traceStart(String name) {
        if (isDebug()) {
            System.out.println(
                String.format("start %s %s", name,
                        getDataManager().getStoragePath()));
        }
    }

    void traceEnd(String name) {
        if (isDebug()) {
            System.out.println(
                String.format("end %s %s", name,
                        getDataManager().getStoragePath()));
        }
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
    
    
}
