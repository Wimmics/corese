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

public class QueryLoad {

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

    public void load(String name) {
        String q = read(name);
        if (q != null) {
            try {
                Query qq = engine.defQuery(q);
                if (qq != null) {
                    qq.setPragma(Pragma.FILE, name);
                }
            } catch (EngineException e) {
                e.printStackTrace();
            }
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

    public String read(InputStream stream) throws IOException {
        return load(new InputStreamReader(stream));
    }

    public String read(String name) {
        String query = "", str = "";
        try {
            Reader fr;
            if (isURL(name)) {
                URL url = new URL(name);
                fr = new InputStreamReader(url.openStream());
            } else {
                fr = new FileReader(name);
            }
            
            query = load(fr);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        if (query == "") {
            return null;
        }
        return query;
    }

    String load(Reader fr) throws IOException {
        BufferedReader fq = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
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
