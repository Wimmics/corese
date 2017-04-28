package fr.inria.corese.compiler.java;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Header {
    static final String NL = System.getProperty("line.separator");
    
 static final String importList = 
              "import fr.inria.acacia.corese.api.IDatatype;\n"
            + "import fr.inria.edelweiss.kgraph.query.PluginImpl;\n"
            + "import fr.inria.acacia.corese.cg.datatype.DatatypeMap;\n";  
  
 
    JavaCompiler jc;
    StringBuilder sb ;
    
    Header(JavaCompiler jc){
        this.jc = jc;
        sb = new StringBuilder();
    }
    
    void process(String name) {
        sb.append("package fr.inria.corese.extension;");
        nl();
        nl();
        sb.append(importList);      
        nl();
        nl();
        sb.append(String.format("public class %s extends PluginImpl { ", name));
        nl();
        nl();          
    }
    
    void nl(){
        sb.append(NL);
    }
    
    StringBuilder getStringBuilder(){
        return sb;
    }

}
