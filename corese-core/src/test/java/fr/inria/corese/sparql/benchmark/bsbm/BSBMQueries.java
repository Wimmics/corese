package fr.inria.corese.sparql.benchmark.bsbm;

/**
 * Concrete BSBM queries (replace the variables to constants)
 * 
 * BSBMQueries.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 28 nov. 2014
 */
public class BSBMQueries {

    public static final String DF_Q01 = "select * where {?s ?p ?o}";
    public static final Object[][] DEFAULT_TEST = new Object[][]{{"DF_Q01", DF_Q01}};

    public static final String Ex_Q01 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  SELECT DISTINCT ?product ?label WHERE {      ?product rdfs:label ?label .     ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType66> .     ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature3> .      ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1967> .      ?product bsbm:productPropertyNumeric1 ?value1 .  	FILTER (?value1 > 136)  	} ORDER BY ?label";
    public static final String Ex_Q02 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dc: <http://purl.org/dc/elements/1.1/>  SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3  ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4  WHERE {     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> rdfs:label ?label .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> rdfs:comment ?comment .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:producer ?p .     ?p rdfs:label ?producer .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> dc:publisher ?p .      <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productFeature ?f .     ?f rdfs:label ?productFeature .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyTextual1 ?propertyTextual1 .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyTextual2 ?propertyTextual2 .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyTextual3 ?propertyTextual3 .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyNumeric1 ?propertyNumeric1 .     <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyNumeric2 ?propertyNumeric2 .     OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyTextual4 ?propertyTextual4 }     OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyTextual5 ?propertyTextual5 }     OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product272> bsbm:productPropertyNumeric4 ?propertyNumeric4 } }";
    public static final String Ex_Q03 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  SELECT ?product ?label WHERE {     ?product rdfs:label ?label .     ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType111> . 	?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature903> . 	?product bsbm:productPropertyNumeric1 ?p1 . 	FILTER ( ?p1 > 228 )  	?product bsbm:productPropertyNumeric3 ?p3 . 	FILTER (?p3 < 156 )     OPTIONAL {          ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature904> .         ?product rdfs:label ?testVar }     FILTER (!bound(?testVar))  } ORDER BY ?label ";
    public static final String Ex_Q04 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  SELECT DISTINCT ?product ?label ?propertyTextual WHERE {     {         ?product rdfs:label ?label .        ?product rdf:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType123> .        ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature3841> . 	   ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1132> .        ?product bsbm:productPropertyTextual1 ?propertyTextual . 	   ?product bsbm:productPropertyNumeric1 ?p1 . 	   FILTER ( ?p1 > 137 )     } UNION {        ?product rdfs:label ?label .        ?product rdf:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType123> .        ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature3841> . 	   ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature90> .        ?product bsbm:productPropertyTextual1 ?propertyTextual . 	   ?product bsbm:productPropertyNumeric2 ?p2 . 	   FILTER ( ?p2> 93 )      }  } ORDER BY ?label";
    public static final String Ex_Q05 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>  SELECT DISTINCT ?product ?productLabel WHERE {  	 	?product rdfs:label ?productLabel .     FILTER (<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product534> != ?product) 	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product534> bsbm:productFeature ?prodFeature . 	?product bsbm:productFeature ?prodFeature . 	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product534> bsbm:productPropertyNumeric1 ?origProperty1 . 	?product bsbm:productPropertyNumeric1 ?simProperty1 . 	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 > (?origProperty1 - 120)) 	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product534> bsbm:productPropertyNumeric2 ?origProperty2 . 	?product bsbm:productPropertyNumeric2 ?simProperty2 . 	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170)) } ORDER BY ?productLabel";
    public static final String Ex_Q06 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>  SELECT ?product ?label WHERE { 	?product rdfs:label ?label .     ?product rdf:type bsbm:Product . 	FILTER regex(?label, \"hangfire\") } ";
    public static final String Ex_Q07 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rev: <http://purl.org/stuff/rev#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX dc: <http://purl.org/dc/elements/1.1/>  SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle         ?reviewer ?revName ?rating1 ?rating2 WHERE {  	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product245> rdfs:label ?productLabel .     OPTIONAL {         ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product245> . 		?offer bsbm:price ?price . 		?offer bsbm:vendor ?vendor . 		?vendor rdfs:label ?vendorTitle .         ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .         ?offer dc:publisher ?vendor .          ?offer bsbm:validTo ?date .         FILTER (?date > \"2008-06-20T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )     }     OPTIONAL { 	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product245> . 	?review rev:reviewer ?reviewer . 	?reviewer foaf:name ?revName . 	?review dc:title ?revTitle .     OPTIONAL { ?review bsbm:rating1 ?rating1 . }     OPTIONAL { ?review bsbm:rating2 ?rating2 . }      } }";
    public static final String Ex_Q08 = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX rev: <http://purl.org/stuff/rev#> PREFIX foaf: <http://xmlns.com/foaf/0.1/>  SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4  WHERE {  	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer16/Product755> . 	?review dc:title ?title . 	?review rev:text ?text . 	FILTER langMatches( lang(?text), \"EN\" )  	?review bsbm:reviewDate ?reviewDate . 	?review rev:reviewer ?reviewer . 	?reviewer foaf:name ?reviewerName . 	OPTIONAL { ?review bsbm:rating1 ?rating1 . } 	OPTIONAL { ?review bsbm:rating2 ?rating2 . } 	OPTIONAL { ?review bsbm:rating3 ?rating3 . } 	OPTIONAL { ?review bsbm:rating4 ?rating4 . } } ORDER BY DESC(?reviewDate) ";
    //public static final String Ex_Q09 = "PREFIX rev: <http://purl.org/stuff/rev#>  DESCRIBE ?x WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite1/Review7211> rev:reviewer ?x }";
    public static final String Ex_Q10 = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX dc: <http://purl.org/dc/elements/1.1/>  SELECT DISTINCT ?offer ?price WHERE { 	?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer19/Product890> . 	?offer bsbm:vendor ?vendor .     ?offer dc:publisher ?vendor . 	?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> . 	?offer bsbm:deliveryDays ?deliveryDays . 	FILTER (?deliveryDays <= 3) 	?offer bsbm:price ?price .     ?offer bsbm:validTo ?date .     FILTER (?date > \"2008-06-20T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) } ORDER BY xsd:double(str(?price))";
    public static final String Ex_Q11 = "SELECT ?property ?hasValue ?isValueOf WHERE {   { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor5/Offer9801> ?property ?hasValue }   UNION   { ?isValueOf ?property <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor5/Offer9801> } }";
    //public static final String Ex_Q12 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rev: <http://purl.org/stuff/rev#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/> PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/> PREFIX dc: <http://purl.org/dc/elements/1.1/>  CONSTRUCT {  <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:product ?productURI .              <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:productlabel ?productlabel .              <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:vendor ?vendorname .              <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:vendorhomepage ?vendorhomepage .               <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:offerURL ?offerURL .              <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:price ?price .              <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:deliveryDays ?deliveryDays .              <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm-export:validuntil ?validTo }  WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm:product ?productURI .         ?productURI rdfs:label ?productlabel .         <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm:vendor ?vendorURI .         ?vendorURI rdfs:label ?vendorname .         ?vendorURI foaf:homepage ?vendorhomepage .         <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm:offerWebpage ?offerURL .         <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm:price ?price .         <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm:deliveryDays ?deliveryDays .         <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor7/Offer12837> bsbm:validTo ?validTo } ";

    public static final Object[][] EXPLORE_USE_CASE = new Object[][]{
        {"Ex_Q01", Ex_Q01},
        {"Ex_Q02", Ex_Q02},
        {"Ex_Q03", Ex_Q03},
        {"Ex_Q04", Ex_Q04},
        {"Ex_Q05", Ex_Q05},
        {"Ex_Q06", Ex_Q06},
        {"Ex_Q07", Ex_Q07},
        {"Ex_Q08", Ex_Q08},
        //{"Ex_Q09", Ex_Q09},
        {"Ex_Q10", Ex_Q10},
        {"Ex_Q11", Ex_Q11}, //{"Ex_Q12", Ex_Q12}
    };

    public static final String Bi_Q01 = ""
            + "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>     \n"
            + "prefix rev: <http://purl.org/stuff/rev#>   \n"
            + "Select ?productType ?reviewCount \n"
            + "{  \n"
            + "	{ Select ?productType (count(?review) As ?reviewCount)   \n"
            + "	{    \n"
            + "	?productType a bsbm:ProductType .    \n"
            + "	?product a ?productType .    \n"
            + "	?product bsbm:producer ?producer .    \n"
            + "	?producer bsbm:country <http://downlode.org/rdf/iso-3166/countries#AT> .    \n"
            + "	?review bsbm:reviewFor ?product .    \n"
            + "	?review rev:reviewer ?reviewer .    \n"
            + "	?reviewer bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .   \n"
            + "	}   \n"
            + "	Group By ?productType  \n"
            + "	} \n"
            + "} \n"
            + "Order By desc(?reviewCount) ?productType";
    public static final String Bi_Q02 = ""
            + "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>     \n"
            + "\n"
            + "SELECT ?otherProduct ?sameFeatures   \n"
            + "{     \n"
            + "	?otherProduct a bsbm:Product .     \n"
            + "	FILTER(?otherProduct != <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer9/Product435>)     \n"
            + "	{       \n"
            + "	SELECT ?otherProduct (count(?otherFeature) As ?sameFeatures)       \n"
            + "	{         \n"
            + "		<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer9/Product435> bsbm:productFeature ?feature .\n"
            + "		?otherProduct bsbm:productFeature ?otherFeature .         \n"
            + "		FILTER(?feature=?otherFeature)       \n"
            + "	}       \n"
            + "	Group By ?otherProduct     \n"
            + "	}   \n"
            + "}   \n"
            + "\n"
            + "Order By desc(?sameFeatures) ?otherProduct";
    public static final String Bi_Q02_SUB1 = ""
            + "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>     \n"
            + "SELECT ?otherProduct (count(?otherFeature) As ?sameFeatures)       \n"
            + "	{         \n"
            + "		<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer9/Product435> bsbm:productFeature ?feature .\n"
            + "		?otherProduct bsbm:productFeature ?otherFeature .         \n"
            + "		FILTER(?feature=?otherFeature)       \n"
            + "	}       \n"
            + "	Group By ?otherProduct ";
    public static final String Bi_Q03
            = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   \n"
            + "prefix rev: <http://purl.org/stuff/rev#>   \n"
            + "prefix dc: <http://purl.org/dc/elements/1.1/>   \n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>    \n"
            + "Select ?product (xsd:float(?monthCount)/?monthBeforeCount As ?ratio)   \n"
            + "{     \n"
            + "{ Select ?product (count(?review) As ?monthCount)       \n"
            + "	{         \n"
            + "	?review bsbm:reviewFor ?product .         \n"
            + "	?review dc:date ?date .\n"
            + "        Filter(?date >= \"2007-07-23\"^^<http://www.w3.org/2001/XMLSchema#date> && ?date < \"2007-08-20\"^^<http://www.w3.org/2001/XMLSchema#date>)       \n"
            + "	}       \n"
            + "	Group By ?product     \n"
            + "}  \n"
            + "\n"
            + "{       \n"
            + "Select ?product (count(?review) As ?monthBeforeCount)       \n"
            + "	{\n"
            + "         ?review bsbm:reviewFor ?product .\n"
            + "         ?review dc:date ?date .\n"
            + "         Filter(?date >= \"2007-06-25\"^^<http://www.w3.org/2001/XMLSchema#date> && ?date < \"2007-07-23\"^^<http://www.w3.org/2001/XMLSchema#date>) #       \n"
            + "	}\n"
            + "        Group By ?product       \n"
            + "   	Having (count(?review)>0)     \n"
            + "}   \n"
            + "\n"
            + "}   \n"
            + "Order By desc(xsd:float(?monthCount) / ?monthBeforeCount) ?product";
    public static final String Bi_Q03_Sub1
            = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   \n"
            + "prefix rev: <http://purl.org/stuff/rev#>   \n"
            + "prefix dc: <http://purl.org/dc/elements/1.1/>   \n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>    \n"
            + "Select ?product (count(?review) As ?monthCount)       \n"
            + "	{         \n"
            + "	?review bsbm:reviewFor ?product .         \n"
            + "	?review dc:date ?date .\n"
            + "        Filter(?date >= \"2007-07-23\"^^<http://www.w3.org/2001/XMLSchema#date> && ?date < \"2007-08-20\"^^<http://www.w3.org/2001/XMLSchema#date>)       \n"
            + "	}       \n"
            + "	Group By ?product  ";
    public static final String Bi_Q03_Sub2
            = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   \n"
            + "prefix rev: <http://purl.org/stuff/rev#>   \n"
            + "prefix dc: <http://purl.org/dc/elements/1.1/>   \n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>    \n"
            + "Select ?product (count(?review) As ?monthBeforeCount)       \n"
            + "	{\n"
            + "         ?review bsbm:reviewFor ?product .\n"
            + "         ?review dc:date ?date .\n"
            + "         Filter(?date >= \"2007-06-25\"^^<http://www.w3.org/2001/XMLSchema#date> && ?date < \"2007-07-23\"^^<http://www.w3.org/2001/XMLSchema#date>) #       \n"
            + "	}\n"
            + "        Group By ?product       \n"
            + "   	Having (count(?review)>0)  ";

    public static final String Bi_Q04 = ""
            + "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   \n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>    \n"
            + "Select ?feature (?withFeaturePrice/?withoutFeaturePrice As ?priceRatio)   \n"
            + "{  \n"
            + "   \n"
            + " { \n"
            + "  Select ?feature (avg(xsd:float(xsd:string(?price))) As ?withFeaturePrice)       \n"
            + "	{\n"
            + "         ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType28> ;\n"
            + "         bsbm:productFeature ?feature .\n"
            + "         ?offer bsbm:product ?product ;       \n"
            + "         bsbm:price ?price .       \n"
            + "	}\n"
            + "       Group By ?feature     \n"
            + "  }     \n"
            + "\n"
            + "  { \n"
            + "  Select ?feature (avg(xsd:float(xsd:string(?price))) As ?withoutFeaturePrice)       \n"
            + "   {         \n"
            + "	{ \n"
            + "		Select distinct ?feature \n"
            + "		{            \n"
            + "		?p a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType28> ;      \n"
            + "        	bsbm:productFeature ?feature .         \n"
            + "		} \n"
            + "	}         \n"
            + "	 ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType28> .\n"
            + "         ?offer bsbm:product ?product ;       \n"
            + "         bsbm:price ?price .\n"
            + "         FILTER NOT EXISTS { ?product bsbm:productFeature ?feature }\n"
            + "    }       \n"
            + "	Group By ?feature      \n"
            + "  } \n"
            + "}   \n"
            + "Order By desc(?withFeaturePrice/?withoutFeaturePrice) ?feature";
    public static final String Bi_Q05 = ""
            + "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "  prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "  prefix rev: <http://purl.org/stuff/rev#>\n"
            + "  prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "\n"
            + "  Select ?country ?product ?nrOfReviews ?avgPrice\n"
            + "  {\n"
            + "    { Select ?country (max(?nrOfReviews) As ?maxReviews)\n"
            + "      {\n"
            + "        { Select ?country ?product (count(?review) As ?nrOfReviews)\n"
            + "          {\n"
            + "            ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType144> .\n"
            + "            ?review bsbm:reviewFor ?product ;\n"
            + "                    rev:reviewer ?reviewer .\n"
            + "            ?reviewer bsbm:country ?country .\n"
            + "          }\n"
            + "          Group By ?country ?product\n"
            + "        }\n"
            + "      }\n"
            + "      Group By ?country\n"
            + "    }\n"
            + "    { Select ?country ?product (avg(xsd:float(xsd:string(?price))) As ?avgPrice)\n"
            + "      {\n"
            + "        ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType144> .\n"
            + "        ?offer bsbm:product ?product .\n"
            + "        ?offer bsbm:price ?price .\n"
            + "      }\n"
            + "      Group By ?country ?product\n"
            + "    }\n"
            + "    { Select ?country ?product (count(?review) As ?nrOfReviews)\n"
            + "      {\n"
            + "        ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType144> .\n"
            + "        ?review bsbm:reviewFor ?product .\n"
            + "        ?review rev:reviewer ?reviewer .\n"
            + "        ?reviewer bsbm:country ?country .\n"
            + "      }\n"
            + "      Group By ?country ?product\n"
            + "    }\n"
            + "    FILTER(?nrOfReviews=?maxReviews)\n"
            + "  }\n"
            + "  Order By desc(?nrOfReviews) ?country ?product";
    public static final String Bi_Q06 = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   \n"
            + "prefix rev: <http://purl.org/stuff/rev#>   \n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n"
            + "   \n"
            + "Select ?reviewer (avg(xsd:float(?score)) As ?reviewerAvgScore)   \n"
            + "{     \n"
            + "{ Select (avg(xsd:float(?score)) As ?avgScore)\n"
            + "       {         \n"
            + "	?product bsbm:producer <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer12/Producer12> .         \n"
            + "	?review bsbm:reviewFor ?product .         \n"
            + "	{ ?review bsbm:rating1 ?score . } UNION         \n"
            + "	{ ?review bsbm:rating2 ?score . } UNION         \n"
            + "	{ ?review bsbm:rating3 ?score . } UNION         \n"
            + "	{ ?review bsbm:rating4 ?score . }       \n"
            + "	}     \n"
            + "       }\n"
            + "     \n"
            + "?product bsbm:producer <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer12/Producer12> .     \n"
            + "?review bsbm:reviewFor ?product .     \n"
            + "?review rev:reviewer ?reviewer .     \n"
            + "{ ?review bsbm:rating1 ?score . } UNION     \n"
            + "{ ?review bsbm:rating2 ?score . } UNION     \n"
            + "{ ?review bsbm:rating3 ?score . } UNION     \n"
            + "{ ?review bsbm:rating4 ?score . }   \n"
            + "}   \n"
            + "\n"
            + "Group By ?reviewer   Having (avg(xsd:float(?score)) > min(?avgScore) * 1.5)\n"
            + "  ";
    public static final String Bi_Q07 = ""
            + "  prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "  prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "  prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "\n"
            + "  Select ?product\n"
            + "  {\n"
            + "    { Select ?product\n"
            + "      { \n"
            + "        { Select ?product (count(?offer) As ?offerCount)\n"
            + "          { \n"
            + "            ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType1> .\n"
            + "            ?offer bsbm:product ?product .\n"
            + "          }\n"
            + "          Group By ?product\n"
            + "        }\n"
            + "      }\n"
            + "      Order By desc(?offerCount)\n"
            + "      Limit 1000\n"
            + "    }\n"
            + "    FILTER NOT EXISTS\n"
            + "    {\n"
            + "      ?offer bsbm:product ?product .\n"
            + "      ?offer bsbm:vendor ?vendor .\n"
            + "      ?vendor bsbm:country ?country .\n"
            + "      FILTER(?country=<http://downlode.org/rdf/iso-3166/countries#US>)\n"
            + "    }\n"
            + "  }";
    public static final String Bi_Q08 = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   \n"
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>    \n"
            + "Select ?vendor (xsd:float(?belowAvg)/?offerCount As ?cheapExpensiveRatio)   \n"
            + "{ \n"
            + "    \n"
            + "{ \n"
            + "Select ?vendor (count(?offer) As ?belowAvg)       \n"
            + "{         \n"
            + "	{ \n"
            + "	?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType87> .\n"
            + "	?offer bsbm:product ?product .           \n"
            + "	?offer bsbm:vendor ?vendor .           \n"
            + "	?offer bsbm:price ?price .  \n"
            + "         \n"
            + "	{ \n"
            + "	Select ?product (avg(xsd:float(xsd:string(?price))) As ?avgPrice)             \n"
            + "	{               \n"
            + "	  ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType87> .               \n"
            + "	  ?offer bsbm:product ?product .               \n"
            + "	  ?offer bsbm:vendor ?vendor .               \n"
            + "	  ?offer bsbm:price ?price .  \n"
            + "	}             \n"
            + "	Group By ?product           \n"
            + "	}         \n"
            + "} .         \n"
            + "FILTER (xsd:float(xsd:string(?price)) < ?avgPrice)       \n"
            + "}\n"
            + "Group By ?vendor     \n"
            + "}   \n"
            + "\n"
            + " {\n"
            + " Select ?vendor (count(?offer) As ?offerCount)       \n"
            + "  {         \n"
            + "	?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType87> .         \n"
            + "	?offer bsbm:product ?product .         \n"
            + "	?offer bsbm:vendor ?vendor .       \n"
            + "  }       \n"
            + "  Group By ?vendor     \n"
            + " } \n"
            + "  \n"
            + "}   \n"
            + "\n"
            + "Order by desc(xsd:float(?belowAvg)/?offerCount) ?vendor ";

    public static final Object[][] BI_USE_CASE = new Object[][]{
        {"Bi_Q01", Bi_Q01},
        {"Bi_Q02", Bi_Q02},
        {"Bi_Q03", Bi_Q03},
        {"Bi_Q04", Bi_Q04},
        {"Bi_Q05", Bi_Q05},
        {"Bi_Q06", Bi_Q06},
        {"Bi_Q07", Bi_Q07},
        {"Bi_Q08", Bi_Q08}
    };
    public static final Object[][] BI_USE_CASE2 = new Object[][]{
        {"Bi_Q02", Bi_Q02},
        {"Bi_Q02 SUB query", Bi_Q02_SUB1},
        {"Bi_Q03", Bi_Q03},
        {"Bi_Q03 Sub 1", Bi_Q03_Sub1},
        {"Bi_Q03 Sub 2", Bi_Q03_Sub2}};
}
