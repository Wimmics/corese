package fr.inria.corese.utils.settings;

import com.typesafe.config.Config;

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

    

    public Settings(Config config) {

        this.BLANK_NODE = config.getString("BLANK_NODE");

        this.TRACE_MEMORY = config.getBoolean("TRACE_MEMORY");
        this.TRACE_GENERIC = config.getBoolean("TRACE_GENERIC");

    }

}
