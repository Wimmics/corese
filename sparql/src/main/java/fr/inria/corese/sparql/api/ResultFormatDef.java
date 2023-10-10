package fr.inria.corese.sparql.api;

/**
 *
 * @author corby
 */
public interface ResultFormatDef {
    public static final int UNDEF_FORMAT = -1;

    public static final int TEXT_FORMAT = 0;

    public static final int RDF_XML_FORMAT = 1;
    public static final int TURTLE_FORMAT = 2;
    public static final int TRIG_FORMAT = 3;
    public static final int JSONLD_FORMAT = 4;
    public static final int NTRIPLES_FORMAT = 6;
    public static final int NQUADS_FORMAT = 7;

    public static final int XML_FORMAT = 11;
    public static final int RDF_FORMAT = 12;
    public static final int JSON_FORMAT = 13;
    public static final int CSV_FORMAT = 14;
    public static final int TSV_FORMAT = 15;
    public static final int MARKDOWN_FORMAT = 16;

    public static final int HTML_FORMAT = 20;

}
