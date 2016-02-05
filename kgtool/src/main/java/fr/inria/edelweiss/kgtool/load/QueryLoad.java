package fr.inria.edelweiss.kgtool.load;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import java.io.InputStream;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public class QueryLoad {

    private static Logger logger = Logger.getLogger(QueryLoad.class);
    static final String HTTP = "http://";
    static final String FILE = "file://";
    static final String FTP = "ftp://";
    static final String[] PROTOCOLS = {HTTP, FILE, FTP};
    static final String NL = System.getProperty("line.separator");
    QueryEngine engine;

    QueryLoad() {
    }

    QueryLoad(QueryEngine e) {
        engine = e;
    }

    public static QueryLoad create() {
        return new QueryLoad();
    }

    public static QueryLoad create(QueryEngine e) {
        return new QueryLoad(e);
    }
        
    public void loadWE(String name) throws LoadException {
        String q = readWE(name);
        if (q != null) {
            Query qq;
            try {
                qq = engine.defQuery(q);
            } catch (EngineException ex) {
                throw LoadException.create(ex);
            }
            if (qq != null) {
                qq.setPragma(Pragma.FILE, name);
            }
        }
    }
     
    @Deprecated
    public void load(String name) {
        String q = read(name);
        if (q != null) {
            try {
                Query qq = engine.defQuery(q);
                if (qq != null) {
                    qq.setPragma(Pragma.FILE, name);
                }
            } catch (EngineException e) {
                logger.error("Loading: " + name);
                e.printStackTrace();
            }
        }
    }

    public void load(Reader read) throws LoadException {
        try {
            String q = read(read);
            if (q != null) {
                engine.defQuery(q);
            }
        } catch (IOException ex) {
            throw new LoadException(ex);
        } catch (EngineException ex) {
            throw new LoadException(ex);        
        }
    }

    boolean isURL(String name) {
        for (String s : PROTOCOLS) {
            if (name.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
    
    @Deprecated
    public String read(InputStream stream) throws IOException {
        return read(new InputStreamReader(stream));
    }
    
     public String readWE(InputStream stream) throws LoadException {
        try {
            return read(new InputStreamReader(stream));
        } catch (IOException ex) {
            throw new LoadException(ex);
        }
    }

     @Deprecated
    public String read(String name) {
        String query = "";
        try {
            query = readWE(name);
        } catch (LoadException ex) {
            java.util.logging.Logger.getLogger(QueryLoad.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (query == "") {
            return null;
        }
        return query;
    }

    public String readWE(String name) throws LoadException {
        String query = "", str = "";
        Reader fr;
        try {
            if (isURL(name)) {
                URL url = new URL(name);
                fr = new InputStreamReader(url.openStream());
            } else {
                fr = new FileReader(name);
            }

            query = read(fr);
        } catch (IOException ex) {
            throw LoadException.create(ex);
        }
        if (query == "") {
            return null;
        }
        return query;
    }

    public String getResource(String name) throws IOException {
        InputStream stream = QueryLoad.class.getResourceAsStream(name);
        if (stream == null) {
            throw new IOException(name);
        }
        Reader fr = new InputStreamReader(stream);
        String str = read(fr);
        return str;
    }

    String read(Reader fr) throws IOException {
        BufferedReader fq = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String str;
        while (true) {
            str = fq.readLine();
            if (str == null) {
                fq.close();
                break;
            }
            sb.append(str);
            sb.append(NL);
        }
        return sb.toString();
    }

    public void write(String name, String str) {
        String query = "";
        try {
            Writer fr = new FileWriter(name);
            BufferedWriter fq = new BufferedWriter(fr);
            fq.write(str);
            fq.flush();
            fr.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

    }
}
