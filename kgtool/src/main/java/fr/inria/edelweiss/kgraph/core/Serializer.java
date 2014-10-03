package fr.inria.edelweiss.kgraph.core;

/**
 * Serialize a Java Object in RDF Turtle
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Serializer {

    static final String NL = System.getProperty("line.separator");
    StringBuilder sb;

    Serializer() {
        sb = new StringBuilder();
    }

    void append(Object s1) {
        sb.append(s1);
    }

    void append(Object s1, Object s2) {
        sb.append(s1);
        sb.append(s2);
    }

    void appendNL(Object s1, Object s2) {
        append(s1, s2);
        append(NL);
    }
    
    void nl(){
        sb.append(NL);
    }
 
    void appendNL(Object s1) {
        sb.append(s1);
        sb.append(NL);
    }

    void appendPNL(Object s1, Object s2) {
        append(s1, s2);
        append(";", NL);
    }
    
     void appendP(Object s1, Object s2) {
        append(s1, s2);
        append("; ");
    }
    
    void open(String type){
        appendPNL("[] a ", type);
    }
    
    void close(){
        appendNL(".");
    }

    public String toString() {
        return sb.toString();
    }
}
