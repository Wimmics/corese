package fr.inria.corese.core.load.result;

import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.load.LoadFormat;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 */
public class SPARQLResultParser {
    static final String JSON = NSManager.EXT+"json";
    static final String XML  = NSManager.EXT+"xml";
    
    public SPARQLResultParser() {}
    
    public Mappings parse(String path, String... format)
            throws ParserConfigurationException, SAXException, IOException {
        int ft = LoadFormat.getFormat(path);
        
        if (format.length > 0) {
            switch (format[0]) {
                case JSON: ft = Loader.JSON_FORMAT;
                break;
                case XML: ft = Loader.XML_FORMAT;
                break; 
            }
        }
        switch (ft) {
            case Loader.JSON_FORMAT:
                return SPARQLJSONResult.create().parse(path);
            default:
                return SPARQLResult.create().parse(path);
        }
    }
    
    // format  xt:json | xt:xml
    public Mappings parseString(String str, String... format)
            throws ParserConfigurationException, SAXException, IOException {
        if (format.length == 0) {
            return SPARQLResult.create().parseString(str);
        }
        switch (format[0]) {
            case JSON: return SPARQLJSONResult.create().parseString(str);
            default:   return SPARQLResult.create().parseString(str);
        }

    }
    
}
