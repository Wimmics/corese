package fr.inria.corese.core.visitor.ldpath;

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

    
   public void process() throws IOException {
        open(file);
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
    
    
    int process(ASTQuery ast, Mappings map, int i) throws IOException {
        DatatypeValue dt1 = map.getValue(AST.COUNT_VAR);
        DatatypeValue dt2 = map.getValue(AST.DISTINCT_VAR);
        if (dt1 != null || dt2 != null){ 
            List<Constant> path = path(ast);
            DatatypeValue dtp = map.getValue(AST.PROPERTY_VAR);
            Constant uri2 = getEndpoint(map);
            if (dtp == null) {
                // // path with constant p as predicate
                result(i++, path, empty, uri2, dt1, dt2);
            } else if (uri2 == null) {
                // local path with variable ?p as predicate
                for (Mapping m : map) {
                    // each Mapping contains ?p = predicate ; ?count = n
                    IDatatype dtpred = (IDatatype) m.getValue(AST.PROPERTY_VAR);
                    if (dtpred != null) {
                        path.add(Constant.create(dtpred));
                        dt1 = m.getValue(AST.COUNT_VAR);
                        dt2 = m.getValue(AST.DISTINCT_VAR);
                        result(i++, path, empty, uri2, dt1, dt2);
                        path.remove(path.size() -1);
                    }
                }
            } else {
                // remote endpoint with variable ?p as predicate
                for (Mapping m : map) {
                    // each Mapping contains ?p = predicate ; ?count = n
                    IDatatype dtpred = (IDatatype) m.getValue(AST.PROPERTY_VAR);
                    if (dtpred != null) {
                        List<Constant> list = new ArrayList<>();
                        list.add(Constant.create(dtpred));
                        dt1 = m.getValue(AST.COUNT_VAR);
                        dt2 = m.getValue(AST.DISTINCT_VAR);
                        result(i++, path, list, uri2, dt1, dt2);
                    }
                }
            }
        }
        return i;
    }
    
    

    void print(String format, int i, List<Constant> path, DatatypeValue dt, DatatypeValue dt2) {
        System.out.println(String.format(format, i, path, dt.intValue(), (dt2 == null) ? "" : dt2.intValue()));
    }
    
    void result(int i, List<Constant> path, List<Constant> list, Constant uri, DatatypeValue dt, DatatypeValue dt2) throws IOException {
        write(rdf(i, path, list, uri, dt, dt2));
    }
    
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
    
    String rdf(int i, List<Constant> path, List<Constant> list, Constant uri, DatatypeValue dt, DatatypeValue dt2) {
        StringBuilder sb = new StringBuilder();
        
        if (i == 1) {
           prolog(sb);
        }

        sb.append ("_:b").append(i).append(" ");
        
        sb.append("rs:path").append(" ");
        path(path, sb, nbTriple);
        
        if (!list.isEmpty()) {
            sb.append("; ").append("rs:path2").append(" ");
            path(list, sb, 0);
        }
        
        if (uri != null && list.isEmpty()) {
           sb.append("; ").append("rs:endpoint").append(" ").append(uri); 
        }
        
        if (dt != null) {
            sb.append("; ").append("rs:count").append(" ").append(dt.intValue());
        }
        
        if (dt2 != null) {
            sb.append("; ").append("rs:distinct").append(" ").append(dt2.intValue());
        }
        
        sb.append(" .");
        return sb.toString();
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
    Constant getEndpoint(Mappings map){
        if (map.size() > 0) {
            IDatatype dt = (IDatatype) map.getValue(AST.SERVICE_VAR);
            if (dt != null) {
                return Constant.create(dt);
            }
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

            } else if (exp.isService()) {
                ArrayList<Constant> l = path(exp.getBodyExp());
                return l;
            }
        }
        return list;
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
