package fr.inria.corese.server.webservice;

import fr.inria.corese.core.load.QueryLoad;
import java.net.UnknownHostException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate server file name and URL
 * Save result in file
 */
public class LinkedResult {
    private static Logger logger = LoggerFactory.getLogger(LinkedResult.class);
    
    private static final String LOG_DIR = "/log/";
    
    private String url;
    private String file;
    private String seed;
    
    
    LinkedResult(String name) {
        init(name, "", getCreateSeed());
    }
    
    LinkedResult(String name, String ext) {
        init(name, ext, getCreateSeed());
    }
    
    LinkedResult(String name, String ext, String seed) {
        init(name, ext, seed);
    }
    
    void init(String name, String ext, String seed) {
        String home = EmbeddedJettyServer.resourceURI.getPath() + LOG_DIR;
        String id = seed.concat(ext);
        if (name != null && !name.isEmpty()){
            id = name.concat("-").concat(id);
        }
        setFile(home + id);
        String uri;
        try {
            uri = Profile.getLocalhost();
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage());
            uri = Profile.stdLocalhost();
        }
        uri += LOG_DIR + id;
        setURL(uri);
    }
    
    void write(String str) {
        QueryLoad ql = QueryLoad.create();
        ql.write(getFile(), str);
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSeed() {
        return seed;
    }

    public LinkedResult setSeed(String seed) {
        this.seed = seed;
        return this;
    }
    
    String getCreateSeed() {
        if (getSeed() == null) {
            return UUID.randomUUID().toString();
        }
        return getSeed();
    }
    

}
