package fr.inria.corese.core.load.jsonld;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.utils.JsonUtils;


/**
 * Load JSON-LD
 *
 * @author Fuqi Song, wimmics inria i3s
 * @date 12 Feb 2014 new
 */
public class JsonldLoader {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(JsonldLoader.class);

    private Reader reader;
    private InputStream is;
    private String base;

    JsonldLoader(InputStream r, String base) {
        this.is = r;
        this.base = base;
    }

    JsonldLoader(Reader r, String base) {
        this.reader = r;
        this.base = base;
    }

    public static JsonldLoader create(InputStream read, String base) {
        JsonldLoader p = new JsonldLoader(read, base);
        return p;
    }

    public static JsonldLoader create(Reader read, String base) {
        JsonldLoader p = new JsonldLoader(read, base);
        return p;
    }

    public static JsonldLoader create(String file) {
        FileReader read;
        try {
            read = new FileReader(file);
            JsonldLoader p = new JsonldLoader(read, file);
            return p;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load triples from JSON-LD to graph
     *
     * @param callback
     * @throws java.io.IOException
     * @throws com.github.jsonldjava.core.JsonLdError
     */
    public void load(JsonLdTripleCallback callback) throws IOException, JsonLdError {
        // resolve the "reader" to JSON objects using parser
        Object jsonObject = JsonUtils.fromReader(this.reader);
        
        JsonLdProcessor.toRDF(jsonObject, callback);
    }
}
