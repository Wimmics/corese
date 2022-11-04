package fr.inria.corese.core.query;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.storage.CoreseGraphDataManagerBuilder;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.utils.settings.SettingsManager;
import fr.inria.corese.utils.settings.storage.StorageConfig;
import fr.inria.corese.utils.settings.storage.StorageModeEnum;
import fr.inria.corese.utils.settings.storage.StorageTypeEnum;

/**
 * Manage dataset and/or db storage according to Property
 * Create one DataManager in StorageFactory for each db path
 * STORAGE_PATH = type1,id1,[param1];type2,id2,[param2]
 * STORAGE = dataset|db|db_all
 */
public class DatasetManager {
    private static Logger logger = LoggerFactory.getLogger(DatasetManager.class);

    private DataManager dataManager;
    private String id;

    public DatasetManager() {
    }

    public DatasetManager init() {

        // Storage list
        List<StorageConfig> storageConfigList = SettingsManager.getSettings().STORAGE_LIST;

        if (storageConfigList != null && storageConfigList.size() > 0) {
            for (StorageConfig storageConfig : storageConfigList) {
                defineDataManager(storageConfig);
                if (getId() == null) {
                    setId(storageConfig.getId());
                }
            }

            // Storage mode
            if (isStorage()) {
                // default mode is db storage
                setDataManager(StorageFactory.getDataManager(getId()));
                logger.info("Storage: " + getId());
            }
        }

        return this;
    }

    // define db data manager, whatever mode is
    // overloaded in corese gui and server
    public void defineDataManager(StorageConfig storageConfig) {
        String id = storageConfig.getId();
        StorageTypeEnum type = storageConfig.getType();
        HashMap<String, String> parameters = storageConfig.getParameters();

        logger.info("Create data manager " + type + " with id " + id + " with config " + parameters);
        if (type == StorageTypeEnum.JAVA) {
            // TODO: terminer la configuration pour la javaDaManager
            // StorageFactory.defineDataManager(param, new DataManagerJava(param));
        } else if (type == StorageTypeEnum.CORESE_GRAPH) {
            StorageFactory.defineDataManager(id, new CoreseGraphDataManagerBuilder().build());
        }
    }

    public QueryProcess createQueryProcess(Graph g) {
        if (isDataset()) {
            return QueryProcess.create(g);
        }
        return createStorageQueryProcess(g);
    }

    public QueryProcess createStorageQueryProcess(Graph g) {
        if (isStorageAll()) {
            return createStorageQueryProcessList(g);
        }
        return createStorageQueryProcessBasic(g);
    }

    public QueryProcess createStorageQueryProcessBasic(Graph g) {
        return QueryProcess.create(g, getDataManager());
    }

    public QueryProcess createStorageQueryProcessList(Graph g) {
        DataManager[] dmList = StorageFactory.getDataManagerList()
                .toArray(new DataManager[StorageFactory.getDataManagerList().size()]);
        return QueryProcess.create(g, dmList);
    }

    public QueryProcess createQueryProcess() {
        return createQueryProcess(Graph.create());
    }

    public Load createLoad(Graph g) {
        Load load = Load.create(g);
        if (isStorage()) {
            load.setDataManager(getDataManager());
        }
        return load;
    }

    public RuleEngine createRuleEngine(Graph g) {
        if (isDataset() || getDataManager() == null) {
            return RuleEngine.create(g);
        } else {
            return RuleEngine.create(g, getDataManager());
        }
    }

    public boolean isDataset() {
        return SettingsManager.getSettings().STORAGE_MODE == StorageModeEnum.DATASET;
    }

    public boolean isStorage() {
        return SettingsManager.getSettings().STORAGE_MODE == StorageModeEnum.DB;
    }

    public boolean isStorageAll() {
        return SettingsManager.getSettings().STORAGE_MODE == StorageModeEnum.DB_ALL;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public DataManager getDataManager(String path) {
        return StorageFactory.getDataManager(path);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
