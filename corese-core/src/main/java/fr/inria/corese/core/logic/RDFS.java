package fr.inria.corese.core.logic;

public interface RDFS {
	
	public static final String RDFS  =  "http://www.w3.org/2000/01/rdf-schema#";
        
	public static final String SUBPROPERTYOF = RDFS + "subPropertyOf";
	public static final String SUBCLASSOF 	 = RDFS + "subClassOf";
	public static final String DOMAIN 		 = RDFS + "domain";
	public static final String RANGE 		 = RDFS + "range";
	public static final String MEMBER 		 = RDFS + "member";
	public static final String MEMBERSHIP 		 = RDFS + "ContainerMembershipProperty";       
	public static final String CLASS 	 	 = RDFS + "Class";
	public static final String RESOURCE		 = RDFS + "Resource";
	public static final String LABEL 	 	 = RDFS + "label";
	public static final String COMMENT 	 	 = RDFS + "comment";
        	
}
