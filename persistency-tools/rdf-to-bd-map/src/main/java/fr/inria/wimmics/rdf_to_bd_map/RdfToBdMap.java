	/*
 * Copyright Inria 2016. 
 */
package fr.inria.wimmics.rdf_to_bd_map;

/**
 * This application: (i) read a RDF file using sesame library; (ii) write the
 * content into a neo4j DB using the neo4j library.
 *
 * @author edemairy
 */
public class RdfToBdMap {
	public static final String LARGE_LITERAL = "large_litteral";
	public static final String LITERAL = "literal";
	public static final String IRI = "IRI";
	public static final String BNODE = "bnode";
	public static final String KIND = "kind";
	public static final String LANG = "lang";
	public static final String TYPE = "type";
	public static final String EDGE_G = "g_value";
	public static final String EDGE_P = "p_value";
	public static final String EDGE_S = "s_value";
	public static final String EDGE_O = "o_value";
	public static final String SUBJECT_EDGE = "subject";
	public static final String OBJECT_EDGE = "object";
	public static final String VERTEX_VALUE = "v_value";
	public static final String VERTEX_LARGE_VALUE = "v_large_value"; // not indexed
	public static final String RDF_EDGE_LABEL = "rdf_edge";
	public static final String RDF_VERTEX_LABEL = "rdf_vertex";
	public static final int MAX_INDEXABLE_LENGTH = 32766; 
}
