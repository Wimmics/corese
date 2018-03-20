package fr.inria.corese.core.logic;

public interface OWL {
	
	public static final String OWL   =  "http://www.w3.org/2002/07/owl#";

	public static final String CLASS            = OWL + "Class";
	public static final String THING            = OWL + "Thing";
	
	public static final String INTERSECTIONOF   = OWL + "intersectionOf";
	public static final String UNIONOF          = OWL + "unionOf";
	public static final String EQUIVALENTCLASS  = OWL + "equivalentClass";
	public static final String COMPLEMENTOF     = OWL + "complementOf";
 	public static final String DISJOINTWITH     = OWL + "disjointWith";
       
	public static final String ALLVALUESFROM    = OWL + "allValuesFrom";
	public static final String SOMEVALUESFROM   = OWL + "someValuesFrom";
	public static final String ONCLASS          = OWL + "onClass";
        
	
	public static final String INVERSEOF        = OWL + "inverseOf";
	public static final String EQUIVALENTPROPERTY= OWL + "equivalentProperty";
	public static final String SYMMETRIC        = OWL + "SymmetricProperty";
	public static final String TRANSITIVE       = OWL + "TransitiveProperty";
	public static final String REFLEXIVE        = OWL + "ReflexiveProperty";
	
	public static final String TOPOBJECTPROPERTY= OWL + "topObjectProperty";
	public static final String TOPDATAPROPERTY  = OWL + "topDataProperty";
	public static final String SAMEAS           = OWL + "sameAs";
	
}
