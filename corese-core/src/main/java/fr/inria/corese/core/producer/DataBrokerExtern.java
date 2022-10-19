package fr.inria.corese.core.producer;

import fr.inria.corese.core.api.DataBroker;
import fr.inria.corese.core.storage.api.dataManager.DataManager;

/**
 * Broker between ProducerImpl and external DataManager
 */

public class DataBrokerExtern implements DataBroker {

    private DataManager dataManager;

    public DataBrokerExtern(DataManager dataManager) {
        setDataManager(dataManager);
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
}
