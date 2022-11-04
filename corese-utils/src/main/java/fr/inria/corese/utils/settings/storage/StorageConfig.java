package fr.inria.corese.utils.settings.storage;

import java.util.HashMap;

public class StorageConfig {

    private String id;
    private StorageTypeEnum type;
    private HashMap<String, String> parameters;

    public StorageConfig(String id, StorageTypeEnum type, HashMap<String, String> parameters) {
        this.id = id;
        this.type = type;
        this.parameters = parameters;
    }

    public String getId() {
        return this.id;
    }

    public StorageTypeEnum getType() {
        return this.type;
    }

    public HashMap<String, String> getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", type='" + getType() + "'" +
                ", parameters='" + getParameters() + "'" +
                "}";
    }

}
