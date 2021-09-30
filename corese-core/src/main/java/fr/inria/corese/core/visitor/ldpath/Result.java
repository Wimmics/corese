package fr.inria.corese.core.visitor.ldpath;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.kgram.core.Mapping;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Results managed in a table ASTQuery -> Mappings
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class Result {
    static final String NL = System.getProperty("line.separator");
    static final String LDP = "http://ns.inria.fr/ldpath/" ;
    static final String TTL = ".ttl";
    static final String JSON = ".json"; 
            
    HashMap <String, String> map;
    List<String> varList;
    
    HashMap<ASTQuery, Mappings> table;
    ArrayList<ASTQuery> alist;
    ASTQuery ast;
    LinkedDataPath ldp;
    int nbTriple;
    List<Constant> empty;
    String file;
    FileWriter fw;

    Result(ASTQuery ast) {
        this.ast = ast;
        table = new HashMap<>();
        alist = new ArrayList<>();
        nbTriple = nbTriple(ast);
        empty = new ArrayList<>();
        map = new HashMap<>();
        varList = new ArrayList<>();
        init();
    }
    
    void init() {        
        define(AST.COUNT_SOURCE_VAR, "rs:subject");
        define(AST.DATATYPE_VAR,    "rs:datatype");
        define(AST.AVG_VAR,         "rs:avg");
        define(AST.MIN_VAR,         "rs:min");
        define(AST.MAX_VAR,         "rs:max");
        define(AST.LEN_VAR,         "rs:length");
        define(AST.COUNT_VAR,       "rs:count");
        define(AST.DISTINCT_VAR,    "rs:distinct");
        define(AST.GRAPH1_VAR,      "rs:graph1");
        define(AST.GRAPH2_VAR,      "rs:graph2");
    }
    
    void define(String var, String slot) {
        map.put(var, slot);
        varList.add(var);
    }
    
    public HashMap<ASTQuery, Mappings> getResult() {
        return table;
    }
    
    public void setOutputFile(String path) {
        this.file = path;
    }
    
    void setLinkedDataPath(LinkedDataPath ldp) {
        this.ldp = ldp;
    }

    synchronized void record(ASTQuery ast, Mappings map) {
        if (map != null && map.size() > 0) {
            DatatypeValue dt  = map.getValue(AST.COUNT_VAR);
            DatatypeValue dt2 = map.getValue(AST.DISTINCT_VAR);
            if ((dt != null && dt.intValue() > 0) || dt2 != null && dt2.intValue() > 0) {
                alist.add(ast);
                table.put(ast, map);
                try {
                    process((ASTQuery)map.getAST(), map, alist.size());
                } catch (IOException ex) {
                    Logger.getLogger(Result.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    
   public void process() throws IOException, LoadException {
       turtle();
       if (file != null) {
           json();
       }
   }
   
   public void turtle() throws IOException {
        if (file != null) {
            open(turtle(file));
        }
        int log = (int) Math.log10(alist.size());
        //    %[argument_index$][flags][width][.precision]conversion
        String format = "%1$0" + (log + 2) + "d: %2$s %3$s %4$s";
        int i = 1;
        for (ASTQuery ast : alist) {
            Mappings map = table.get(ast);
            i = process(ast, map, i);
        }
        close(fw);
    }
   
   void json() throws LoadException, IOException {
       Graph g = Graph.create();
       Load ld = Load.create(g);
       ld.parse(turtle(file));
       Transformer t = Transformer.create(g, Transformer.JSON);
       t.write(json(file));
   }
   
   String json(String file) {
       if (file.endsWith(JSON)) {
           return file;
       }
       return root(file).concat(JSON);
   }
   
   String turtle(String file) {
      if (file.endsWith(TTL)) {
          return file;
      }
      return root(file).concat(TTL);
   }
   
   String root(String file) {
       if (file.endsWith(TTL)) {
           return file.substring(0, file.indexOf(TTL));
       }
       if (file.endsWith(JSON)) {
           return file.substring(0, file.indexOf(JSON));
       }
       return file;
   }
    
    
    int process(ASTQuery ast, Mappings map, int i) throws IOException {
        for (Mapping m : map) {
            result(i++, ast, m);
        }
        return i;
    }
    
    void result(int i, ASTQuery ast, Mapping map) throws IOException {
        List<Constant> path1 = path(ast);
        List<Constant> path2 = empty;
        IDatatype dtp =  map.getValue(AST.PROPERTY_VAR);
        IDatatype dtg =  map.getValue(AST.GRAPH2_VAR);
        IDatatype dtu =  map.getValue(AST.SERVICE_VAR);
        Constant type = type(ast);
        Constant uri  = (dtu==null)?null:Constant.create(dtu);
        
        if (dtp == null) {
            write(rdf(i, type, path1, path2, uri, map));
        }
        else if (dtu == null && dtg == null) {
            path1.add(Constant.create(dtp));
            write(rdf(i, type, path1, path2, uri, map));
            path1.remove(path1.size() - 1);
        }
        else {
            path2 = new ArrayList<>();
            path2.add(Constant.create(dtp));
            write(rdf(i, type, path1, path2, uri, map));
        }
    }
    
//    int process2(ASTQuery ast, Mappings map, int i) throws IOException {
//        DatatypeValue dt1 = map.getValue(AST.COUNT_VAR);
//        DatatypeValue dt2 = map.getValue(AST.DISTINCT_VAR);
//        if (dt1 != null || dt2 != null){ 
//            List<Constant> path = path(ast);
//            DatatypeValue dtp = map.getValue(AST.PROPERTY_VAR);
//            DatatypeValue dtg = map.getValue(AST.GRAPH2_VAR);
//            Constant uri2 = getEndpoint(map);
//            Constant type = type(ast);
//            if (dtp == null) {
//                // // path with constant p as predicate
//                for (Mapping m : map) {
//                    result(i++, type, path, empty, uri2, m);
//                }
//            } else if (uri2 == null && dtg == null) {
//                // local path with variable ?p as predicate
//                for (Mapping m : map) {
//                    // each Mapping contains ?p = predicate ; ?count = n
//                    IDatatype dtpred = (IDatatype) m.getValue(AST.PROPERTY_VAR);
//                    if (dtpred != null) {
//                        path.add(Constant.create(dtpred));
//                        result(i++, type, path, empty, uri2, m);
//                        path.remove(path.size() -1);
//                    }
//                }
//            } else {
//                // remote endpoint with variable ?p as predicate
//                for (Mapping m : map) {
//                    // each Mapping contains ?p = predicate ; ?count = n
//                    IDatatype dtpred = (IDatatype) m.getValue(AST.PROPERTY_VAR);
//                    if (dtpred != null) {
//                        List<Constant> list = new ArrayList<>();
//                        list.add(Constant.create(dtpred));
//                        result(i++, type, path, list, uri2, m);
//                    }
//                }
//            }
//        }
//        return i;
//    }
    
//    void result(int i, Constant type, List<Constant> path, List<Constant> list, Constant uri, Mapping m) throws IOException {
//        write(rdf(i, type, path, list, uri, m));
//    }
    
    void write(String str) throws IOException {
        if (file != null) {
            save(str);
            save(NL);
        }
        System.out.println(str);
    }
    
    void setFile(String name) {
        file = name;
    }
    
    void open(String file) throws IOException {
        if (file != null) {
         fw = new FileWriter(file);
        }
    }
    
    void save(String str) throws IOException {
        fw.write(str);
    }
    
    void close(FileWriter fw) throws IOException {
        if (file != null) {
            fw.flush();
            fw.close();
        }
    }
    
    String rdf(int i, Constant type, List<Constant> path1, List<Constant> path2, Constant uri, Mapping m) {
        
        StringBuilder sb = new StringBuilder();
        
        if (i == 1) {
           prolog(sb);
        }

        sb.append ("_:b").append(i).append(" ");
        
        if (type != null) {
            sb.append("rs:type").append(" <").append(type.getLongName()).append(">; ");
        }
        
        sb.append("rs:path").append(" ");
        path(path1, sb, nbTriple);
        
        if (!path2.isEmpty()) {
            sb.append("; ").append("rs:path2").append(" ");
            path(path2, sb, 0);
        }
        
        if (uri != null && path2.isEmpty()) {
           slot(sb, "rs:endpoint", uri.toString());
        }
        
        slot(sb, m);

        sb.append(" .");
        return sb.toString();
    }
    
    
    void slot(StringBuilder sb, Mapping m) {
        IDatatype ddt =  m.getValue(AST.DATATYPE_VAR);

        for (String name : varList) {
            IDatatype dd =  m.getValue(name);
            if (dd != null) {
                String slot = map.get(name);
                if (ddt == null && (slot.contains("min") || slot.contains("max") || slot.contains("avg"))) {
                    // skip
                } else if (dd.getCode() == IDatatype.DECIMAL) {
                    slot(sb, slot, String.format("%.3f", dd.doubleValue()));
                }
                else {
                    slot(sb, slot, dd.toString());
                }
            }
        }
    }
    
    
    void slot(StringBuilder sb, String name, String value) {
        sb.append("; ").append(name).append(" ").append(value);
    }
    
    
    void prolog(StringBuilder sb) {
        sb.append(String.format("# Linked Data Path Finder %s \n", new Date()));
        sb.append(String.format("@prefix rs: <%s> \n", LDP));
        if (!ldp.getLocalList().isEmpty()) {
            sb.append(String.format("[] rs:first <%s> ; rs:length %s .\n", ldp.getLocalList().get(0), ldp.getPathLength()));
        }
        if (!ldp.getEndpointList().isEmpty()) {
            sb.append(String.format("[] rs:rest  <%s> ; rs:length %s .\n", ldp.getEndpointList().get(0), ldp.getEndpointPathLength()));
        }
    }
        
    void path(List<Constant> path, StringBuilder sb, int skip) {
        sb.append("(");
        int i = 1;
        for (Constant name : path) {
            if (i <= skip) {
                i++;
            }
            else {
                sb.append(name).append(" ");
            }
        }
        sb.append(")");
    }
    
    // remote endpoint uri
    Constant getEndpoint(Mappings map) {
        if (map.size() > 0) {
            return getVariable(map.get(0), AST.SERVICE_VAR);
        }
        return null;
    }

    Constant getVariable(Mapping map, String name) {
        IDatatype dt =  map.getValue(name);
        if (dt != null) {
            return Constant.create(dt);
        }
        return null;
    }
    
    
    

    ArrayList<Constant> path(ASTQuery ast) {
        return path(ast.getBody());
    }
    
    ArrayList<Constant> path(Exp body) {
        ArrayList<Constant> list = new ArrayList<>();
        for (Exp exp : body) {
            if (exp.isFilter()) {
            } else if (exp.isTriple() && exp.getTriple().predicate().isConstant()) {
                list.add(exp.getTriple().getProperty());

            } else if (exp.isService() || exp.isNamedGraph()) {
                ArrayList<Constant> l = path(exp.getBodyExp());
                return l;
            }
        }
        return list;
    }
    
    Constant type(ASTQuery ast) {
        return type(ast.where());
    }
    
    Constant type(Exp body) {
        for (Exp exp : body) {
            if (exp.isFilter()) {
            } else if (exp.isTriple() && exp.getTriple().isType() && exp.getTriple().getObject().isConstant()) {
                return exp.getTriple().getObject().getConstant();
            }
            else if (exp.isService() || exp.isNamedGraph()) {
                Constant t = type(exp.getBodyExp());
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    
     int nbTriple(ASTQuery ast) {
        int i = 0;
        for (Exp exp : ast.getBody()) {
            if (exp.isFilter()) {}
            else if (exp.isTriple()) {
                i++;
            }
        }
        return i;
    }
    

}
