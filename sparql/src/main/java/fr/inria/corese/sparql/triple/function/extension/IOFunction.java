package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 */
public class IOFunction extends TermEval {
    
    
    public IOFunction(String name) {
        super(name);
    }
    
     @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }
        
        switch (oper()) {
            case ExprType.XT_VALID_URI:
                return validURI(dt);
        }
        
        return TRUE;
        
    }
    
    public IDatatype validURI(IDatatype dt) {
        HttpURLConnection connection = null;
        try {
            URL myurl = new URL(dt.getLabel());
            connection = (HttpURLConnection) myurl.openConnection();
            connection.setRequestMethod("HEAD");
            int code = connection.getResponseCode();
            connection.disconnect();
            return TRUE;
        } catch (MalformedURLException ex) {
            return FALSE;
        } catch (IOException ex) {
            connection.disconnect();
            return FALSE;
        }
    }
    
}
