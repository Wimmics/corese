package fr.inria.corese.utils.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import fr.inria.corese.utils.settings.storage.StorageConfig;
import fr.inria.corese.utils.settings.storage.StorageModeEnum;
import fr.inria.corese.utils.settings.storage.StorageTypeEnum;

public class Settings {

    /**
     * @path: BLANK_NODE
     * @name: BLANK_NODE
     * @description: Prefix of blank nodes
     * @type: String
     * @default : "_:b"
     */
    public final String BLANK_NODE;

    /**
     * @path: TRACE_MEMORY
     * @name: TRACE_MEMORY
     * @description: Print logs of memory usage in the rules engine
     * @type: Boolean
     * @default : false
     */
    public final Boolean TRACE_MEMORY;

    /**
     * @path: TRACE_GENERIC
     * @name: TRACE_GENERIC
     * @description: Print logs on the federated query engine
     * @type: Boolean
     * @default : false
     */
    public final Boolean TRACE_GENERIC;

    public final StorageModeEnum STORAGE_MODE;
    public final ArrayList<StorageConfig> STORAGE_LIST;

    public Settings(Config config) {

        this.BLANK_NODE = config.getString("BLANK_NODE");

        this.TRACE_MEMORY = config.getBoolean("TRACE_MEMORY");
        this.TRACE_GENERIC = config.getBoolean("TRACE_GENERIC");

        if (config.hasPath("storage.mode")) {
            this.STORAGE_MODE = this.initStorageMode(config.getString("storage.mode"));
        } else {
            this.STORAGE_MODE = null;
        }

        if (config.hasPath("storage.list")) {
            this.STORAGE_LIST = this.initStorageList(config.getObjectList("storage.list"));
        } else {
            this.STORAGE_LIST = null;
        }

    }

    private StorageModeEnum initStorageMode(String storageModeString) {

        switch (storageModeString) {
            case "dataset":
                return StorageModeEnum.DATASET;

            case "db":
                return StorageModeEnum.DB;

            case "db all":
                return StorageModeEnum.DB_ALL;

            default:
                throw new IllegalArgumentException(
                        "Unknown storage mode : " + storageModeString + "\nAccepted values : (dataset|db|bd all)");
        }

    }

    private ArrayList<StorageConfig> initStorageList(List<? extends ConfigObject> ConfigList) {

        ArrayList<StorageConfig> result = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        for (ConfigObject configObject : ConfigList) {
            Config config = configObject.toConfig();

            // Get ID
            String id;
            if (config.hasPath("id")) {
                id = config.getString("id");
            } else {
                throw new IllegalArgumentException(
                        "The 'id' field is missing in the storage configuration : " + config);
            }
            // Check the uniqueness of the ID
            if (ids.contains(id)) {
                throw new IllegalArgumentException("Two storages cannot have the same id :" + id);
            } else {
                ids.add(id);
            }

            // Get Type
            StorageTypeEnum type;
            if (config.hasPath("type")) {
                String typeString = config.getString("type");
                switch (typeString) {
                    case "jenatdb1":
                        type = StorageTypeEnum.JENA_TDB1;
                        break;
                    case "rdf4jmodel":
                        type = StorageTypeEnum.RDF4J;
                        break;
                    case "coreseGraph":
                        type = StorageTypeEnum.CORESE_GRAPH;
                        break;
                    case "java":
                        type = StorageTypeEnum.JAVA;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown storage type : " + typeString
                                + "\nAccepted values : (jenatdb1, rdf4jmodel, coreseGraph, java)");
                }
            } else {
                throw new IllegalArgumentException(
                        "The 'type' field is missing in the storage configuration : " + config);
            }

            // Get parameters
            HashMap<String, String> params = new HashMap<>();
            for (Entry<String, ConfigValue> param : config.entrySet()) {
                if (!param.getKey().equals("id") && !param.getKey().equals("type")) {
                    String key = param.getKey();
                    params.put(key, config.getString(key));
                }
            }

            result.add(new StorageConfig(id, type, params));
        }

        return result;

    }

}
