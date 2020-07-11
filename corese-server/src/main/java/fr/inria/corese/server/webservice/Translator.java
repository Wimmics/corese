package fr.inria.corese.server.webservice;

import fr.inria.corese.core.workflow.PreProcessor;
import fr.inria.corese.shex.shacl.Shex;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author corby
 */
public class Translator implements PreProcessor {
    
    @Override
    public String translate(String str) {
        try {
            return process(str);
        } catch (IOException ex) {
            Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
            return str;
        } catch (Exception ex) {
            Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
            return str;
        }
    }
    
    String process(String str) throws Exception {
        Shex shex = new Shex().setExtendShacl(true);
        StringBuilder sb = shex.parseString(str);
        return sb.toString();
    }
    
    
}
