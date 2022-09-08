package fr.inria.corese.core.producer;

import fr.inria.corese.core.logic.Distance;

/**
 * Corese object associated to DataManager
 * enables corese core to manage additional data such as Distance
 */
public class MetadataManager {

    private Distance distance;
    
    
    public MetadataManager() {
    
    }
    
    public void startWriteTransaction() {
    }
    
    public void endWriteTransaction() {
        setDistance(null);
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }
    
    
}
