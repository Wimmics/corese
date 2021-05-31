package fr.inria.corese.core;

/**
 * Serialize a Java Object in RDF Turtle
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Serializer {

    static final String NL = System.getProperty("line.separator");
    StringBuilder sb;

    public Serializer() {
        sb = new StringBuilder();
    }

    public Serializer append(Object s1) {
        sb.append(s1);
        return this;
    }

    public void append(Object s1, Object s2) {
        sb.append(s1);
        sb.append(s2);
    }

    public void appendNL(Object s1, Object s2) {
        append(s1, s2);
        append(NL);
    }
    
    public void nl(){
        sb.append(NL);
    }
 
    public void appendNL(Object s1) {
        sb.append(s1);
        sb.append(NL);
    }
    
    public void appendPNL(Object s1) {
        sb.append(s1);
        append(" ; ");
        sb.append(NL);
    }

    public void appendPNL(Object s1, Object s2) {
        append(s1, s2);
        append(" ;", NL);
    }
    
     public void appendP(Object s1, Object s2) {
        append(s1, s2);
        append(" ; ");
    }
    
    public void open(String type){
        appendPNL("[] a ", type);
    }
    
    public void close(){
        appendNL(".");
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
