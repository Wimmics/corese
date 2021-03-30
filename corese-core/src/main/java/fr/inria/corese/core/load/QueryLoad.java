package fr.inria.corese.core.load;

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

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryLoad extends Load {

    private static Logger logger = LoggerFactory.getLogger(QueryLoad.class);
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
    
    @Deprecated
    public void loadWE(String name) throws LoadException {
        parse(name);
    }
        
    public void parse(String name) throws LoadException {
        String q = readWE(name);
        if (q != null) {
            Query qq;
            try {
                qq = engine.defQuery(q);
            } catch (EngineException ex) {
                throw LoadException.create(ex).setPath(name);
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
    
    
    @Deprecated
    public void load(Reader read) throws LoadException {
        parse(read);
    }

    public void parse(Reader read) throws LoadException {
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

     boolean isURL(String path) {
        try {
            new URL(path);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
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
            LoggerFactory.getLogger(QueryLoad.class.getName()).error( "", ex);
        }
        if (query == "") {
            return null;
        }
        return query;
    }
       
    public String readURL(String name) throws LoadException {
        return readWE(name);
    }
    
    public String readProtect(String name) throws LoadException {
        return readWE(name);
    }
    
    public String readWE(String name, boolean protect) throws LoadException {
        return readWE(name);
    }


    public String readWithAccess(String name) throws LoadException {
        check(Access.Feature.READ_WRITE, name, TermEval.READ_MESS);
        return readWE(name);
    }
    
    public String readWE(String name) throws LoadException {
        String query = "", str = "";
        Reader fr;
        try {
            if (NSManager.isResource(name)) {
                fr = new InputStreamReader(getClass().getResourceAsStream(NSManager.stripResource(name)));
            }
            else if (isURL(name)) {
                URL url = new URL(name);
                fr = new InputStreamReader(url.openStream());
            } else {
                fr = new FileReader(name);
            }

            if (fr == null) {
                throw LoadException.create(new IOException(name)).setPath(name);
            }
            query = read(fr);
        } catch (IOException ex) {
            throw LoadException.create(ex).setPath(name);
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
    
    // TODO: clean 
    public String basicParse(String path) throws EngineException {
        String pp = (path.endsWith("/")) ? path.substring(0, path.length() - 1) : path;
        String str = null;
        try {
            if (NSManager.isResource(pp)) { 
                // @import <function/test.rq> within transformation such as st:turtle
                // the import uri is st:function/test.rq
                // consider it as a resource
                String name = NSManager.stripResource(pp); 
                str = getResource(name);
            } else {
                str = readWE(pp);
            }
            return str;
        } catch (LoadException | IOException ex) {
            logger.error(ex.getMessage());
            throw new EngineException(ex);
        }
    }


    String read(Reader fr) throws IOException {
        BufferedReader fq = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String str;
        boolean isnl = false;
        while (true) {
            str = fq.readLine();
            if (str == null) {
                fq.close();
                break;
            }
            if (isnl){
                sb.append(NL);
            }
            else {
                isnl = true;
            }
            sb.append(str);
            //sb.append(NL);
        }
        return sb.toString();
    }
    
    public String writeTemp(String name, String str) {
        String query = "";
        try {
            File file = File.createTempFile(getName(name), getSuffix(name));
            Writer fr = new FileWriter(file);
            BufferedWriter fq = new BufferedWriter(fr);
            fq.write(str);
            fq.flush();
            fr.close();
            return file.toString();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
    
    public String writeTemp(String name, IDatatype dt) {
        String query = "";
        try {
            File file = File.createTempFile(getName(name), getSuffix(name));
            Writer fr = new FileWriter(file);
            BufferedWriter fq = new BufferedWriter(fr);
            if (dt.isList()) {
                for (IDatatype elem : dt) {
                    fq.write(elem.stringValue());
                    fq.write(NL);
                }
            }
            else {
                fq.write(dt.stringValue());
            }
            fq.flush();
            fr.close();
            return file.toString();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
     
    String getName(String name) {
         int index = name.indexOf(".");
         if (index == -1) {
             return name;
         }
         return name.substring(0, index);
    }
     
    String getSuffix(String name) {
         int index = name.indexOf(".");
         if (index == -1) {
             return ".txt";
         }
         return name.substring(index);
     }
      
    public void write(String name, IDatatype dt) {
        write(name, dt.stringValue());
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
            e.printStackTrace();
        } catch (IOException e) {
        }
        
    }

}
