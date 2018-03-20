package fr.inria.corese.sparql.benchmark;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;

/**
 * TestSorter.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 12 mai 2014
 */
public class TestSorter {

    public final static String G22 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3\n"
            + " ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4 \n"
            + "WHERE {\n"
            + "    kg:productXYZ rdfs:label ?label .\n"
            + "    kg:productXYZ rdfs:comment ?comment .\n"
            + "    kg:productXYZ bsbm:producer ?p .\n"
            + "    ?p rdfs:label ?producer .\n"
            + "    kg:productXYZ dc:publisher ?p . \n"
            + "    kg:productXYZ bsbm:productFeature ?f .\n"
            + "    ?f rdfs:label ?productFeature .\n"
            + "    kg:productXYZ bsbm:productPropertyTextual1 ?propertyTextual1 .\n"
            + "    kg:productXYZ bsbm:productPropertyTextual2 ?propertyTextual2 .\n"
            + "    kg:productXYZ bsbm:productPropertyTextual3 ?propertyTextual3 .\n"
            + "    kg:productXYZ bsbm:productPropertyNumeric1 ?propertyNumeric1 .\n"
            + "    kg:productXYZ bsbm:productPropertyNumeric2 ?propertyNumeric2 .\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyTextual4 ?propertyTextual4 }\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyNumeric4 ?propertyNumeric4 }\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyTextual5 ?propertyTextual5 }\n"
            + "}";

    public final static String G222 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3\n"
            + " ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4 \n"
            + "WHERE {\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyTextual4 ?propertyTextual4 }\n"
            + "    kg:productXYZ rdfs:label ?label .\n"
            + "    kg:productXYZ rdfs:comment ?comment .\n"
            + "    kg:productXYZ bsbm:producer ?p .\n"
            + "    ?p rdfs:label ?producer .\n"
            + "    kg:productXYZ dc:publisher ?p . \n"
            + "    kg:productXYZ bsbm:productFeature ?f .\n"
            + "    ?f rdfs:label ?productFeature .\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyTextual4 ?propertyTextual4 }\n"
            + "    kg:productXYZ bsbm:productPropertyTextual1 ?propertyTextual1 .\n"
            + "    kg:productXYZ bsbm:productPropertyTextual2 ?propertyTextual2 .\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyTextual5 ?propertyTextual5 }\n"
            + "    kg:productXYZ bsbm:productPropertyTextual3 ?propertyTextual3 .\n"
            + "    kg:productXYZ bsbm:productPropertyNumeric1 ?propertyNumeric1 .\n"
            + "    kg:productXYZ bsbm:productPropertyNumeric2 ?propertyNumeric2 .\n"
            + "    OPTIONAL { kg:productXYZ bsbm:productPropertyNumeric4 ?propertyNumeric4 }\n"
            + "}";

    public final static String Q4 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "\n"
            + "SELECT DISTINCT ?product ?label ?propertyTextual\n"
            + "WHERE {\n"
            + "    { \n"
            + "       ?product rdfs:label ?label .\n"
            + "       ?product rdf:type kg:typeXYZ .\n"
            + "       ?product bsbm:productFeature kg:featureXYZ .\n"
            + "	   ?product bsbm:productFeature kg:featureXYZ2 .\n"
            + "       ?product bsbm:productPropertyTextual1 ?propertyTextual .\n"
            + "	   ?product bsbm:productPropertyNumeric1 ?p1 .\n"
            + "	   FILTER ( ?p1 > 1 )\n"
            + "    } UNION {\n"
            + "       ?product rdfs:label ?label .\n"
            + "       ?product rdf:type kg:typeXYZ .\n"
            + "       ?product bsbm:productFeature kg:featureXYZ .\n"
            + "	   ?product bsbm:productFeature kg:featureXYZ3 .\n"
            + "       ?product bsbm:productPropertyTextual1 ?propertyTextual .\n"
            + "	   ?product bsbm:productPropertyNumeric2 ?p2 .\n"
            + "	   FILTER ( ?p2> 2 ) \n"
            + "    } \n"
            + "}\n"
            + "ORDER BY ?label\n"
            + "OFFSET 5\n"
            + "LIMIT 10";
    public final static String Q5 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "\n"
            + "SELECT DISTINCT ?product ?productLabel\n"
            + "WHERE { \n"
            + "	?product rdfs:label ?productLabel .\n"
            + " FILTER (<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product54> != ?product)\n"
            + "	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product54> bsbm:productFeature ?prodFeature .\n"
            + "	?product bsbm:productFeature ?prodFeature .\n"
            + "	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product54> bsbm:productPropertyNumeric1 ?origProperty1 .\n"
            + "	?product bsbm:productPropertyNumeric1 ?simProperty1 .\n"
            + "	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 = (?origProperty1 - 120))\n"
            + "	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product54> bsbm:productPropertyNumeric2 ?origProperty2 .\n"
            + "	?product bsbm:productPropertyNumeric2 ?simProperty2 .\n"
            + "	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170))\n"
            + "}\n"
            + "ORDER BY ?productLabel\n"
            + "LIMIT 5";

    public static final String Q77 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rev: <http://purl.org/stuff/rev#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle \n"
            + "       ?reviewer ?revName ?rating1 ?rating2\n"
            + "WHERE { \n"
            + "	?ProductXYZ rdfs:label ?productLabel .\n"
            + "    OPTIONAL {\n"
            + "        ?offer bsbm:product ?ProductXYZ .\n"
            + "		?offer bsbm:price ?price .\n"
            + "		?offer bsbm:vendor ?vendor .\n"
            + "		?vendor rdfs:label ?vendorTitle .\n"
            + "        ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .\n"
            + "        ?offer dc:publisher ?vendor . \n"
            + "        ?offer bsbm:validTo ?date .\n"
            + "        FILTER (?date > 12 )\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "	?review bsbm:reviewFor ?ProductXYZ .\n"
            + "	?review rev:reviewer ?reviewer .\n"
            + "	?reviewer foaf:name ?revName .\n"
            + "	?review dc:title ?revTitle .\n"
            + "    OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n"
            + "    OPTIONAL { ?review bsbm:rating2 ?rating2 . } \n"
            + "    }\n"
            + "}";

    public static final String Q155 = "PREFIX e: <http://learningsparql.com/ns/expenses#> \n"
            + "\n"
            + "SELECT ?description ?date ?maxAmount \n"
            + "WHERE\n"
            + "{\n"
            + "  {\n"
            + "    SELECT (MAX(?amount) as ?maxAmount) \n"
            + "    WHERE { ?meal e:amount ?amount . }\n"
            + "  }\n"
            + "  {\n"
            + "    ?meal e:description ?description ;\n"
            + "          e:date ?date ;\n"
            + "          e:amount ?maxAmount .\n"
            + "  }\n"
            + "}";
    public static String Q22 = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "\n"
            + "SELECT DISTINCT ?product ?label\n"
            + "WHERE { \n"
            + "    ?product rdfs:label ?label .\n"
            + "    ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType66> .\n"
            + "    ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature3> . \n"
            + "    ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1967> . \n"
            + "    ?product bsbm:productPropertyNumeric1 ?value1 . \n"
            + "	FILTER (?value1 > 136) \n"
            + "	}\n"
            + "ORDER BY ?label\n"
            + "LIMIT 10";

    public static String Q33 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rev: <http://purl.org/stuff/rev#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "CONSTRUCT {  <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:product ?productURI .\n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:productlabel ?productlabel .\n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:vendor ?vendorname .\n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:vendorhomepage ?vendorhomepage . \n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:offerURL ?offerURL .\n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:price ?price .\n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:deliveryDays ?deliveryDays .\n"
            + "             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm-export:validuntil ?validTo } \n"
            + "WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm:product ?productURI .\n"
            + "        ?productURI rdfs:label ?productlabel .\n"
            + "        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm:vendor ?vendorURI .\n"
            + "        ?vendorURI rdfs:label ?vendorname .\n"
            + "        ?vendorURI foaf:homepage ?vendorhomepage .\n"
            + "        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm:offerWebpage ?offerURL .\n"
            + "        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm:price ?price .\n"
            + "        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm:deliveryDays ?deliveryDays .\n"
            + "        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor12/Offer21272> bsbm:validTo ?validTo }";

    public static String Q10 = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT DISTINCT ?offer ?price\n"
            + "WHERE {\n"
            + "	?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer6/Product214> .\n"
            + "	?offer bsbm:vendor ?vendor .\n"
            + "    ?offer dc:publisher ?vendor .\n"
            + "	?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .\n"
            + "	?offer bsbm:deliveryDays ?deliveryDays .\n"
            + "	FILTER (?deliveryDays = 3)\n"
            + "	?offer bsbm:price ?price .\n"
            + "    ?offer bsbm:validTo ?date .\n"
            + "    FILTER (?date > \"2008-06-20T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )\n"
            + "}\n"
            + "ORDER BY xsd:double(str(?price))\n"
            + "LIMIT 10";
    private final static String Q7 = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
            + " PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "PREFIX rev: <http://purl.org/stuff/rev#>\n"
            + "SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle \n"
            + "       ?reviewer ?revName ?rating1 ?rating2\n"
            + "WHERE { \n"
            + "	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product556> rdfs:label ?productLabel .\n"
            + "    OPTIONAL {\n"
            + "        ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product54> .\n"
            + "		?offer bsbm:price ?price .\n"
            + "		?offer bsbm:vendor ?vendor .\n"
            + "		?vendor rdfs:label ?vendorTitle .\n"
            + "        ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .\n"
            + "        ?offer dc:publisher ?vendor . \n"
            + "        ?offer bsbm:validTo ?date .\n"
            + "        FILTER (?date > \"2008-06-20T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer1/Product54> .\n"
            + "	?review rev:reviewer ?reviewer .\n"
            + "	?reviewer foaf:name ?revName .\n"
            + "	?review dc:title ?revTitle .\n"
            + "    OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n"
            + "    OPTIONAL { ?review bsbm:rating2 ?rating2 . } \n"
            + "    }\n"
            + "}";

    final static String QG = "PREFIX  data:  <http://example.org/foaf/>\n"
            + "PREFIX  foaf:  <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX  rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "\n"
            + "SELECT ?mbox ?nick ?ppd\n"
            + "FROM NAMED <http://example.org/foaf/aliceFoaf>\n"
            + "FROM NAMED <http://example.org/foaf/bobFoaf>\n"
            + "WHERE\n"
            + "{\n"
            + "  ?alice ?hates ?nick.\n "
            + "  GRAPH ?ppd\n"
            + "  {\n"
            + "      ?w foaf:mbox ?mbox ;\n"
            + "         foaf:nick ?nick\n"
            + "  }.\n"
            + "  GRAPH data:aliceFoaf\n"
            + "  {\n"
            + "    ?alice foaf:mbox <mailto:alice@work.example> ;\n"
            + "           foaf:knows ?whom .\n"
            + "    ?whom  foaf:mbox ?mbox ;\n"
            + "           rdfs:seeAlso kg:ppd .\n"
            + "    ?ppd  a foaf:PersonalProfileDocument .\n"
            + "  } .\n"
            + "}";

    final static String QG2 = "PREFIX  data:  <http://example.org/foaf/>\n"
            + "PREFIX  foaf:  <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX  rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "\n"
            + "SELECT ?mbox ?nick ?ppd\n"
            + "FROM NAMED <http://example.org/foaf/aliceFoaf>\n"
            + "FROM NAMED <http://example.org/foaf/bobFoaf>\n"
            + "WHERE\n"
            + "{\n"
            + "  GRAPH data:aliceFoaf\n"
            + "  {\n"
            + "    ?alice foaf:mbox <mailto:alice@work.example> ;\n"
            + "           foaf:knows ?whom .\n"
            + "    ?whom  foaf:mbox ?mbox ;\n"
            + "           rdfs:seeAlso ?ppd .\n"
            + "    ?ppd  a foaf:PersonalProfileDocument .\n"
            + "  } .\n"
            + "      ?w foaf:mbox ?mbox ;\n"
            + "         foaf:nick ?nick\n"
            + "}";
    final static String Q13 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rev: <http://purl.org/stuff/rev#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT ?product ?producer ?comment ?page\n"
            + "WHERE {  \n"
            + "  ?product rdfs:comment ?comment.\n"
            + "  ?product rdf:type bsbm:Product.\n"
            + "  ?product bsbm:producer ?producer.\n"
            + "  ?producer foaf:homepage ?page.\n"
            + "}";
    final static String Q14 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rev: <http://purl.org/stuff/rev#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT ?x ?y\n"
            + "WHERE {\n"
            + "  ?x rdf:type ?y\n"
            + "}";
    final static String Q15 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rev: <http://purl.org/stuff/rev#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>\n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
            + "\n"
            + "SELECT ?vendor ?product ?offer ?from\n"
            + "WHERE {\n"
            + "\n"
            + "  ?offer bsbm:validFrom ?from.\n"
            + "  ?offer bsbm:product ?product.\n"
            + "  ?offer bsbm:vendor ?vendor.\n"
            + "  ?offer rdf:type bsbm:Offer.\n"
            + "  \n"
            + "}";

    final static String UNION = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX vcard:   <http://www.w3.org/2001/vcard-rdf/3.0#>\n"
            + "\n"
            + "CONSTRUCT { ?x  vcard:N _:v .\n"
            + "            _:v vcard:givenName ?gname .\n"
            + "            _:v vcard:familyName ?fname }\n"
            + "WHERE\n"
            + " {\n"
            + "    { ?x foaf:firstname ?gname } UNION  { ?x foaf:givenname   ?gname } .\n"
            + "    { ?x foaf:surname   ?fname } UNION  { ?x foaf:family_name ?fname } .\n"
            + " }";

    final static String QG77 = "select * where {graph ?g {?p ?p ?x  {select * where {?q ?q ?y}}     }}";
    final static String QG78 = "select * where {graph ?g {?p ?p ?x minus {?q ?q ?y}}}";
    final static String Q32 = "select where {?x ?p ?y optional{?y rdf:type ?class} filter (! bound(?class) && ! isLiteral(?y))}";
    final static String Q34 = "select  distinct ?src     where { graph ?src { ?x c:FirstName 'Olivier'   } graph ?src2 { ?y c:FirstName 'Olivier'   }  filter (?src = ?src2) } order by ?src";

    final static String QC1 = "select * where {\n"
            + "  ?x ?p ?y \n"
            + "  ?x rdfs:label ?n \n"
            //+"   kg:ss ?some ?oo \n"
            + "}";

    final static String QC2 = "select * where {\n"
            + "  ?x ?p ?y \n"
            + "  filter(?n = 12)\n"
            + "  bind(?y as ?n) \n"
            + "}";

    final static String Q_EXAMPLE = "select ?email ?s ?t ?g\n"
            + "where {\n"
            + "?email kg:dd kg:inria\n"
            + "GRAPH ?g{\n"
            + "?s kg:tt ?t.\n"
            + "?t ?p kg:name.\n"
            + "kg:john kg:has ?s.\n"
            + "}\n"
            + "GRAPH kg:g2{\n"
            + "?s kg:has ?age\n"
            + "?age kg:ss ?year\n"
            + "filter(?age > 22 && ?year = \"2004\")\n"
            + "}\n"
            + "?prof ?x ?email\n"
            + "filter(regex(?email, \"inria.fr\",\"i\"))\n"
            + "filter(?email IN (\"xx\", \"yy\"))"
            + "}";
    final static String Q_EXAMPLE_NO_GRAPH = "select ?email ?s ?t ?g\n"
            + "where {\n"
            + "?email kg:dd kg:inria\n"
            + "?s kg:tt ?t.\n"
            + "?t ?p kg:name.\n"
            + "kg:john kg:has ?s.\n"
            + "?s kg:has ?age\n"
            + "?age kg:ss ?year\n"
            + "filter(?age > 22 && ?year = \"2004\")\n"
            + "?prof ?x ?email\n"
            + "filter(regex(?email, \"inria.fr\",\"i\"))\n"
            + "filter(?email IN (\"xx\", \"yy\"))"
            + "}";

    static String TestQuery9 = "select  *  where {"
            + "?x rdf:type ?t; c:name ?n"
            + "} ";

    static String Qx100 = "PREFIX c: <http://www.inria.fr/acacia/comma#>\n"
            + "select  distinct ?src     where { "
            + "graph ?src { ?x c:FirstName 'Olivier'   } "
            + "graph ?src2 { ?y c:FirstName 'Olivier'   }  filter (?src = ?src2) } order by ?src";

    static String Qx54 = "select * where {"
            + "graph ?g {?s <name> ?o} "
            + "?s <age> ?a"
            + "}";

    static String Qx3 = " select where {?x ?p ?y optional{?y rdf:type ?class} filter (! bound(?class) && ! isLiteral(?y))}";

    //Query 4: Feature with the highest ratio between price with that feature and price without that feature.
    //Use Case Motivation: A customer wants to inform herself which features have the most impact on the product 
    //price to get hints on how to restrict the following product search.
    static String Q_BI_Q4 = "  prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n"
            + "  prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n"
            + "  prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "\n"
            + "  Select ?feature (?withFeaturePrice/?withoutFeaturePrice As ?priceRatio)\n"
            + "  {\n"
            + "    { Select ?feature (avg(xsd:float(xsd:string(?price))) As ?withFeaturePrice)\n"
            + "      {\n"
            + "        ?product a bsbm-inst:ProductType3;\n"
            + "                 bsbm:productFeature ?feature .\n"
            + "        ?offer bsbm:product ?product ;\n"
            + "               bsbm:price ?price .\n"
            + "      }\n"
            + "      Group By ?feature\n"
            + "    }\n"
            + "    { Select ?feature (avg(xsd:float(xsd:string(?price))) As ?withoutFeaturePrice)\n"
            + "      {\n"
            + "        { Select distinct ?feature { \n"
            + "          ?p a bsbm-inst:ProductType3 ;\n"
            + "             bsbm:productFeature ?feature .\n"
            + "        } }\n"
            + "        ?product a bsbm-inst:ProductType3 .\n"
            + "        ?offer bsbm:product ?product ;\n"
            + "               bsbm:price ?price .\n"
            + "        FILTER NOT EXISTS { ?product bsbm:productFeature ?feature }\n"
            + "      }\n"
            + "      Group By ?feature\n"
            + "    }\n"
            + "  }\n"
            + "  Order By desc(?withFeaturePrice/?withoutFeaturePrice) ?feature\n"
            + "  Limit 10";

    static String ssss = "select ?email ?s ?t ?g\n"
            + "where {\n"
            + "?email kg:dd kg:inria.\n"
            + "?s kg:tt ?t.\n"
            + "\n"
            + "GRAPH ?g{\n"
            + "?t ?p kg:name.\n"
            + "kg:john kg:has ?s.\n"
            + "?s kg:has ?age.\n"
            + "}\n"
            + "GRAPH kg:g2{\n"
            + "?age kg:ss ?year.\n"
            + "?prof ?x ?email.\n"
            + "}\n"
            + "}";

    static String BSBM_BI_Q4 = ""
            + "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   "
            + "prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   "
            + "prefix xsd: <http://www.w3.org/2001/XMLSchema#>    "
            + "Select ?feature (?withFeaturePrice/?withoutFeaturePrice As ?priceRatio)   "
            + "{     "
            + "{ "
            + "Select ?feature (avg(xsd:float(xsd:string(?price))) As ?withFeaturePrice)       "
            + "{         ?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2> ; "
            + "                 bsbm:productFeature ?feature .         "
            + "?offer bsbm:product ?product ;               "
            + " bsbm:price ?price .       }       "
            + "Group By ?feature     }    "
            + " { Select ?feature (avg(xsd:float(xsd:string(?price))) As ?withoutFeaturePrice)       "
            + "{         "
            + "{ Select distinct ?feature {           "
            + " ?p a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2> ;              "
            + "bsbm:productFeature ?feature .         } }         "
            + "?product a <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2> .         "
            + "?offer bsbm:product ?product ;               "
            + " bsbm:price ?price .         "
            + "FILTER NOT EXISTS { ?product bsbm:productFeature ?feature }       }       "
            + "Group By ?feature     }   }   "
            + "Order By desc(?withFeaturePrice/?withoutFeaturePrice) ?feature   "
            + "Limit 10";

    private static final String Bi_Q02 = ""
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
    private static final String Bi_Q03 = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   prefix bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>   prefix rev: <http://purl.org/stuff/rev#>   prefix dc: <http://purl.org/dc/elements/1.1/>   prefix xsd: <http://www.w3.org/2001/XMLSchema#>    Select ?product (xsd:float(?monthCount)/?monthBeforeCount As ?ratio)   {     { Select ?product (count(?review) As ?monthCount)       {         ?review bsbm:reviewFor ?product .         ?review dc:date ?date .         Filter(?date >= \"2007-07-23\"^^<http://www.w3.org/2001/XMLSchema#date> && ?date < \"2007-08-20\"^^<http://www.w3.org/2001/XMLSchema#date>)        }       Group By ?product     }  {       Select ?product (count(?review) As ?monthBeforeCount)       {         ?review bsbm:reviewFor ?product .         ?review dc:date ?date .         Filter(?date >= \"2007-06-25\"^^<http://www.w3.org/2001/XMLSchema#date> && ?date < \"2007-07-23\"^^<http://www.w3.org/2001/XMLSchema#date>) #       }       Group By ?product       Having (count(?review)>0)     }   }   Order By desc(xsd:float(?monthCount) / ?monthBeforeCount) ?product ";

    private static final String EXISTS_02 = ""
            + "prefix ex: <http://www.example.org/>\n"
            + "\n"
            + "select * where {\n"
            + "?s ?p ex:o2\n"
            + "filter exists {ex:s ex:p ex:o}\n"
            + "}";

    private static final String Qb6 = "prefix bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>   \n"
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

    public static final String Q_bind08 = "PREFIX : <http://example.org/> \n"
            + "\n"
            + "SELECT ?s ?p ?o ?z\n"
            + "{\n"
            + "  ?s ?p ?o .\n"
            + "  BIND(?z+10 AS ?z10)\n"
            + "  FILTER(?z = 3 )\n"
            + "  BIND(?o+1 AS ?z)\n"
            + "  FILTER(?s = 33 )\n"
            + "  VALUES ?z10 {10 20}\n"
            + "}";

    public static final String Q_slow = "select * where {?x ?p ?y optional {?a ?a ?a} ?z ?q ?t filter(?x = ?y && ! (?y = ?x))  }";

    public static final String QQ33 = "PREFIX : <http://example.org/> \n"
            + "\n"
            + "SELECT ?s ?p ?o ?z\n"
            + "{\n"
            + "  ?s ?p ?o .\n"
            + "  BIND(?o+1 AS ?z)\n"
            + "  FILTER(?z = 3 )\n"
            + "}";

    public static final String  query_werid = 
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
      + "bind ((kg:sparql('"
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "construct  where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
      + "as ?g)"
                + "graph ?g { ?a ?p ?b }"
        + "} ";
    public static void main(String[] args) throws EngineException {

//        test96(Qx100, Query.QP_DEFAULT);
//        test96(Qx100, Query.QP_HEURISTICS_BASED);
//        test96(Qx54, Query.QP_HEURISTICS_BASED);
//        test96(Qx3, Query.QP_HEURISTICS_BASED);
//        test96(Qx3, Query.QP_DEFAULT);
//        test(Q10, "/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale1000.ttl", Query.QP_HEURISTICS_BASED, "Q 10");
//        test(Q7, "/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale1000.ttl", Query.QP_DEFAULT, "Q 7 QP_DEFAULT");
//        test(Q7, "/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale1000.ttl", Query.QP_HEURISTICS_BASED, "Q 7 QP_HEURISTICS_BASED");
//        test(ssss, "/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale100.ttl", Query.QP_DEFAULT, "Q_BI_Q4");
//        test(BSBMQueries.Bi_Q02, "/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale1000.ttl", Query.QP_DEFAULT, "Bi_Q02");
//        test(BSBMQueries.Bi_Q02_SUB1, "/Users/fsong/NetBeansProjects/bsbmtools-0.2/scale1000.ttl", Query.QP_DEFAULT, "Bi_Q02 sub query");
//        for (Object[] query : BSBMQueries.BI_USE_CASE) {
//            System.out.println("=== " + query[0].toString() + " ===");
//            test(query[1].toString(), "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl", Query.QP_DEFAULT, query[0].toString());
//            test(query[1].toString(), "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl", Query.QP_HEURISTICS_BASED, query[0].toString());
//        }
        // test(G22, "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl", Query.QP_HEURISTICS_BASED, "Qb6");
        //test(Q_bind08, "/Users/fsong/NetBeansProjects/kgram/kgtool/src/test/resources/data/w3c-sparql11/sparql11-test-suite/bind/data.ttl", Query.QP_HEURISTICS_BASED, "Qb6");
        //test(Q_bind08, "/Users/fsong/NetBeansProjects/kgram/kgtool/src/test/resources/data/w3c-sparql11/sparql11-test-suite/bind/data.ttl", Query.QP_DEFAULT, "Qb6");
        // test(QC2, "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl", Query.QP_HEURISTICS_BASED, "Qb6");
        //t//est(BSBM_BI_Q4, "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl", Query.QP_HEURISTICS_BASED, "Qb6");
        //test(Q_slow, "/Users/fsong/NetBeansProjects/kgram/kgengine/src/test/resources/data/comma/query.rdf", Query.QP_DEFAULT, "Q_slow");
        //test(Q_slow, "/Users/fsong/NetBeansProjects/kgram/kgengine/src/test/resources/data/comma/query.rdf", Query.QP_HEURISTICS_BASED, "Q_slow 2");

        //test(Q_slow, "/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl", Query.QP_DEFAULT, "Q_slow");
        //test(Q_slow, "/Users/fsong/NetBeansProjects/bsbm/data/scale1000.ttl", Query.QP_HEURISTICS_BASED, "Q_slow 2");
        
        //testQuery1(query_werid, Query.QP_DEFAULT);
        //testQuery1(query_werid, Query.QP_HEURISTICS_BASED);
        test("select * where {?s ?p ?o}", "/Users/fsong/Documents/nquands.nq",Query.QP_DEFAULT, "");
    }

    static void test96(String query, int qp) throws EngineException {
        Graph gg = Graph.create();
        Load load = Load.create(gg);

        String data = "/Users/fsong/NetBeansProjects/kgram/kgengine/src/test/resources/data/";
        load.load(data + "kgraph/rdf.rdf", RDF.RDF);
        load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
        load.load(data + "comma/comma.rdfs");
        load.load(data + "comma/commatest.rdfs");
        load.load(data + "comma/model.rdf");
        load.load(data + "comma/testrdf.rdf");
        load.load(data + "comma/data");
        load.load(data + "comma/data2");
        QueryProcess exec = QueryProcess.create(gg);
        exec.setPlanProfile(qp);

        long start = System.currentTimeMillis();
        Mappings m = exec.query(query);

        System.out.println("\n" + m.getQuery() + "  size:" + m.size());
        System.out.println("== Querying time:" + (System.currentTimeMillis() - start) + "ms ==");

    }
    
    static void testQuery1(String query, int qp) throws EngineException {
        Graph gg = Graph.create();
        Load load = Load.create(gg);

        String data = "/Users/fsong/NetBeansProjects/kgram/kgengine/src/test/resources/data/";
        load.load(data + "comma/comma.rdfs");
        load.load(data + "comma/model.rdf");
        load.load(data + "comma/data");
        QueryProcess exec = QueryProcess.create(gg);
        exec.setPlanProfile(qp);

        long start = System.currentTimeMillis();
        Mappings m = exec.query(query);

        System.out.println("\n" + m.getQuery() + "  size:" + m.size());
        System.out.println("== Querying time:" + (System.currentTimeMillis() - start) + "ms ==");

    }

    static void test(String query, String data, int qp, String name) throws EngineException {
        Graph gg = Graph.create();
        Load load = Load.create(gg);
        load.load(data);
        QueryProcess exec = QueryProcess.create(gg);
        exec.setPlanProfile(qp);

        long start = System.currentTimeMillis();
        Mappings m = exec.query(query);

        System.out.println("============" + name + "=============\n" + m.getQuery() + "  \n size:" + m.size());
        System.out.println("== Querying time:" + (System.currentTimeMillis() - start) + "ms ==\n\n");
    }
}
