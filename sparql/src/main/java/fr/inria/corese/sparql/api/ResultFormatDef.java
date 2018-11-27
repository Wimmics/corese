package fr.inria.corese.sparql.api;

/**
 *
 * @author corby
 */
public interface ResultFormatDef {    
    public static final int UNDEF_FORMAT   = -1;    
    public static final int RDF_XML_FORMAT = 1;
    public static final int TURTLE_FORMAT  = 2;
    public static final int JSON_LD_FORMAT = 3;    
    public static final int XML_FORMAT     = 11;
    public static final int RDF_FORMAT     = 12;
    public static final int JSON_FORMAT    = 13;     
}
