
var sparql11query_all_tests=[
  { name:"data-sparql11/aggregates/agg01.rq",
    comment:"Simple count",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT (COUNT(?O) AS ?C)\nWHERE { ?S ?P ?O }",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg02.rq",
    comment:"Count with grouping",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT ?P (COUNT(?O) AS ?C)\nWHERE { ?S ?P ?O }\nGROUP BY ?P",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg03.rq",
    comment:"Count with grouping and HAVING clause",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT ?P (COUNT(?O) AS ?C)\nWHERE { ?S ?P ?O }\nGROUP BY ?P\nHAVING (COUNT(?O) > 2 )",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg04.rq",
    comment:"Count(*)",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT (COUNT(*) AS ?C)\nWHERE { ?S ?P ?O }",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg05.rq",
    comment:"Count(*) with grouping",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT ?P (COUNT(*) AS ?C)\nWHERE { ?S ?P ?O }\nGROUP BY ?P",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg06.rq",
    comment:"Count(*) with HAVING Count(*)",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT (COUNT(*) AS ?C)\nWHERE { ?S ?P ?O }\nHAVING (COUNT(*) > 0 )",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg07.rq",
    comment:"Count(*) with grouping and HAVING Count(*)",
    query:"PREFIX : <http:\/\/www.example.org>\n\nSELECT ?P (COUNT(*) AS ?C)\nWHERE { ?S ?P ?O }\nGROUP BY ?P\nHAVING ( COUNT(*) > 2 )",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg08.rq",
    comment:"grouping by expression, done wrong",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nSELECT ((?O1 + ?O2) AS ?O12) (COUNT(?O1) AS ?C)\nWHERE { ?S :p ?O1; :q ?O2 } GROUP BY (?O1 + ?O2)\nORDER BY ?O12",
    expected:false
  },
  { name:"data-sparql11/aggregates/agg08b.rq",
    comment:"grouping by expression, done correctly",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\n   SELECT ?O12 (COUNT(?O1) AS ?C)\n   WHERE { ?S :p ?O1; :q ?O2 } GROUP BY ((?O1 + ?O2) AS ?O12)\n   ORDER BY ?O12",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg09.rq",
    comment:"Projection of an ungrouped variable (not appearing in the GROUP BY expression)",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nSELECT ?P (COUNT(?O) AS ?C)\nWHERE { ?S ?P ?O } GROUP BY ?S",
    expected:false
  },
  { name:"data-sparql11/aggregates/agg10.rq",
    comment:"Projection of an ungrouped variable (no GROUP BY expression at all)",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nSELECT ?P (COUNT(?O) AS ?C)\nWHERE { ?S ?P ?O }",
    expected:false
  },
  { name:"data-sparql11/aggregates/agg11.rq",
    comment:"Use of an ungrouped variable in a project expression",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nSELECT ((?O1 + ?O2) AS ?O12) (COUNT(?O1) AS ?C)\nWHERE { ?S :p ?O1; :q ?O2 } GROUP BY (?S)",
    expected:false
  },
  { name:"data-sparql11/aggregates/agg12.rq",
    comment:"Use of an ungrouped variable in a project expression, where the variable appears in a GROUP BY expression",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nSELECT ?O1 (COUNT(?O2) AS ?C)\nWHERE { ?S :p ?O1; :q ?O2 } GROUP BY (?O1 + ?O2)",
    expected:false
  },
  { name:"data-sparql11/aggregates/agg-groupconcat-1.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nASK {\n\t{SELECT (GROUP_CONCAT(?o) AS ?g) WHERE {\n\t\t[] :p1 ?o\n\t}}\n\tFILTER(?g = \"1 22\" || ?g = \"22 1\")\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-groupconcat-2.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT (COUNT(*) AS ?c) {\n\t{SELECT ?p (GROUP_CONCAT(?o) AS ?g) WHERE {\n\t\t[] ?p ?o\n\t} GROUP BY ?p}\n\tFILTER(\n\t\t(?p = :p1 && (?g = \"1 22\" || ?g = \"22 1\"))\n\t\t|| (?p = :p2 && (?g = \"aaa bb c\" || ?g = \"aaa c bb\" || ?g = \"bb aaa c\" || ?g = \"bb c aaa\" || ?g = \"c aaa bb\" || ?g = \"c bb aaa\"))\n\t)\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-groupconcat-3.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nASK {\n\t{SELECT (GROUP_CONCAT(?o;SEPARATOR=\":\") AS ?g) WHERE {\n\t\t[] :p1 ?o\n\t}}\n\tFILTER(?g = \"1:22\" || ?g = \"22:1\")\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-sum-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT (SUM(?o) AS ?sum)\nWHERE {\n\t?s :dec ?o\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-sum-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT ?s (SUM(?o) AS ?sum)\nWHERE {\n\t?s ?p ?o\n}\nGROUP BY ?s",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-avg-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT (AVG(?o) AS ?avg)\nWHERE {\n\t?s :dec ?o\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-avg-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT ?s (AVG(?o) AS ?avg)\nWHERE {\n\t?s ?p ?o\n}\nGROUP BY ?s\nHAVING (AVG(?o) <= 2.0)",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-min-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT (MIN(?o) AS ?min)\nWHERE {\n\t?s :dec ?o\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-min-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT ?s (MIN(?o) AS ?min)\nWHERE {\n\t?s ?p ?o\n}\nGROUP BY ?s",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-max-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT (MAX(?o) AS ?max)\nWHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-max-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nSELECT ?s (MAX(?o) AS ?max)\nWHERE {\n\t?s ?p ?o\n}\nGROUP BY ?s",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-sample-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\nASK {\n\t{\n\t\tSELECT (SAMPLE(?o) AS ?sample)\n\t\tWHERE {\n\t\t\t?s :dec ?o\n\t\t}\n\t}\n\tFILTER(?sample = 1.0 || ?sample = 2.2 || ?sample = 3.5)\n}",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-err-01.rq",
    comment:"Error in AVG return no binding",
    query:"PREFIX : <http:\/\/example.com\/data\/#>\nSELECT ?g (AVG(?p) AS ?avg) ((MIN(?p) + MAX(?p)) \/ 2 AS ?c)\nWHERE {\n  ?g :p ?p .\n}\nGROUP BY ?g",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-err-02.rq",
    comment:"Protect from error in AVG using IF and COALESCE",
    query:"PREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX : <http:\/\/example.com\/data\/#>\nSELECT ?g \n(AVG(IF(isNumeric(?p), ?p, COALESCE(xsd:double(?p),0))) AS ?avg) \nWHERE {\n  ?g :p ?p .\n}\nGROUP BY ?g",
    expected:true
  },
  { name:"data-sparql11/aggregates/agg-empty-group.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.com\/>\nSELECT ?x (MAX(?value) AS ?max)\nWHERE {\n\t?x ex:p ?value\n} GROUP BY ?x",
    expected:true
  },
  { name:"data-sparql11/bind/bind01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?z\n{\n  ?s ?p ?o .\n  BIND(?o+10 AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?o ?z ?z2\n{\n  ?s ?p ?o .\n  BIND(?o+10 AS ?z)\n  BIND(?o+100 AS ?z2)\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?z ?s1\n{\n  ?s ?p ?o .\n  BIND(?o+1 AS ?z)\n  ?s1 ?p1 ?z\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT *\n{\n  ?s ?p ?o .\n  BIND(?nova AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?p ?o ?z\n{\n  ?s ?p ?o .\n  BIND(?o+1 AS ?z)\n  FILTER(?z = 3 )\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind06.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT *\n{\n  ?s ?p ?o .\n  BIND(?o+10 AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind07.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?p ?o ?z\n{\n  ?s ?p ?o .\n  { BIND(?o+1 AS ?z) } UNION { BIND(?o+2 AS ?z) }\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind08.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?p ?o ?z\n{\n  ?s ?p ?o .\n  FILTER(?z = 3 )\n  BIND(?o+1 AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind10.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?v ?z\n{\n  # See also bind11.rq\n  BIND(4 AS ?z)\n  {\n    # ?z is not in-scope at the time of filter execution.\n    ?s :p ?v . FILTER(?v = ?z)\n  }\n}",
    expected:true
  },
  { name:"data-sparql11/bind/bind11.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?v ?z\n{\n  # See also bind10.rq\n  BIND(4 AS ?z)\n  # ?z is in scope at the time of filter execution.\n  ?s :p ?v . \n  FILTER(?v = ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values01.rq",
    comment:"",
    query:"PREFIX dc:   <http:\/\/purl.org\/dc\/elements\/1.1\/> \nPREFIX :     <http:\/\/example.org\/book\/> \nPREFIX ns:   <http:\/\/example.org\/ns#> \n\nSELECT ?book ?title ?price\n{\n   ?book dc:title ?title ;\n         ns:price ?price .\n}\nVALUES ?book {\n :book1\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values02.rq",
    comment:"",
    query:"# bindings with one element and one value in the object variable\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o\n{\n  ?s ?p ?o .\n} VALUES ?o {\n :b\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values03.rq",
    comment:"",
    query:"# bindings with two variables and one set of values\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  ?s ?p1 ?o1 .\n  ?s ?p2 ?o2 .\n} VALUES (?o1 ?o2) {\n (\"Alan\" \"alan@example.org\")\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values04.rq",
    comment:"",
    query:"# bindings with one element UNDEF\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  ?s ?p1 ?o1 .\n  ?s ?p2 ?o2 .\n} VALUES (?o1 ?o2) {\n (\"Alan\" UNDEF)\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  ?s ?p1 ?o1 .\n  ?s ?p2 ?o2 .\n} VALUES (?o1 ?o2) {\n (UNDEF \"Alan\")\n (:b UNDEF)\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values06.rq",
    comment:"",
    query:"# bindings with two variables and two sets of values\n\nPREFIX : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \nSELECT ?s ?p1 ?o1\n{\n  ?s ?p1 ?o1 .\n} VALUES ?p1 {\n foaf:knows\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values07.rq",
    comment:"",
    query:"# bindings with two variables and two sets of values\n\nPREFIX : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \nSELECT ?s ?o1 ?o2\n{\n  ?s ?p1 ?o1 \n  OPTIONAL { ?s foaf:knows ?o2 }\n} VALUES (?o2) {\n (:b)\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/values08.rq",
    comment:"",
    query:"PREFIX dc:   <http:\/\/purl.org\/dc\/elements\/1.1\/> \nPREFIX :     <http:\/\/example.org\/book\/> \nPREFIX ns:   <http:\/\/example.org\/ns#> \n\nSELECT ?book ?title ?price\n{\n   ?book dc:title ?title ;\n         ns:price ?price .\n}\nVALUES (?book ?title) {\n (UNDEF \"SPARQL Tutorial\")\n (:book2 UNDEF)\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/inline01.rq",
    comment:"",
    query:"PREFIX dc:   <http:\/\/purl.org\/dc\/elements\/1.1\/> \nPREFIX :     <http:\/\/example.org\/book\/> \nPREFIX ns:   <http:\/\/example.org\/ns#> \n\nSELECT ?book ?title ?price\n{\n   VALUES ?book { :book1 }\n   ?book dc:title ?title ;\n         ns:price ?price .\n}",
    expected:true
  },
  { name:"data-sparql11/bindings/inline02.rq",
    comment:"",
    query:"# bindings with one element and one value in the object variable\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o {\n\t{\n\t\tSELECT * WHERE {\n\t\t\t?s ?p ?o .\n\t\t}\n\t\tVALUES (?o) { (:b) }\n\t}\n}",
    expected:true
  },
  { name:"data-sparql11/construct/constructwhere01.rq",
    comment:"CONSTRUCT WHERE { ?S ?P ?O }",
    query:"PREFIX : <http:\/\/example.org\/>\n\nCONSTRUCT WHERE { ?s ?p ?o}",
    expected:true
  },
  { name:"data-sparql11/construct/constructwhere02.rq",
    comment:"CONSTRUCT WHERE  with join",
    query:"PREFIX : <http:\/\/example.org\/>\n\nCONSTRUCT WHERE { :s1 :p ?o . ?s2 :p ?o }",
    expected:true
  },
  { name:"data-sparql11/construct/constructwhere03.rq",
    comment:"CONSTRUCT WHERE  with join, using shortcut notation",
    query:"PREFIX : <http:\/\/example.org\/>\n\nCONSTRUCT WHERE { :s2 :p ?o1, ?o2 }",
    expected:true
  },
  { name:"data-sparql11/construct/constructwhere04.rq",
    comment:"CONSTRUCT WHERE  with DatasetClause",
    query:"PREFIX : <http:\/\/example.org\/>\n\nCONSTRUCT \nFROM <data.ttl>\nWHERE { ?s ?p ?o }",
    expected:true
  },
  { name:"data-sparql11/construct/constructwhere05.rq",
    comment:"CONSTRUCT WHERE  with FILTER",
    query:"PREFIX : <http:\/\/example.org\/>\n\nCONSTRUCT \nWHERE { ?s ?p ?o FILTER ( ?o = :o1) }",
    expected:false
  },
  { name:"data-sparql11/construct/constructwhere06.rq",
    comment:"",
    query:"CONSTRUCT \nWHERE { GRAPH <data.ttl> { ?s ?p ?o } }",
    expected:false
  },
  { name:"data-sparql11/csv-tsv-res/csvtsv01.rq",
    comment:"SELECT * WHERE { ?S ?P ?O }",
    query:"PREFIX : <http:\/\/example.org\/>\n\nSELECT * WHERE { ?s ?p ?o} ORDER BY ?s ?p ?o",
    expected:true
  },
  { name:"data-sparql11/csv-tsv-res/csvtsv02.rq",
    comment:"SELECT with OPTIONAL (i.e. not all vars bound in all results)",
    query:"PREFIX : <http:\/\/example.org\/>\n\nSELECT * WHERE { ?s ?p ?o OPTIONAL {?o ?p2 ?o2 } } ORDER BY ?s ?p ?o ?p2 ?o2",
    expected:true
  },
  { name:"data-sparql11/csv-tsv-res/csvtsv01.rq",
    comment:"SELECT * WHERE { ?S ?P ?O } with some corner cases of typed literals",
    query:"PREFIX : <http:\/\/example.org\/>\n\nSELECT * WHERE { ?s ?p ?o} ORDER BY ?s ?p ?o",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-03.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' using an unnamed bnode as wildcard",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows [] .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" .\n}",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-03b.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' using a named bnode as wildcard",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows _:b .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" .\n}",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-05.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' and inserts that all \'Alans\' know themselves only.",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows [] .\n}\nINSERT\n{\n  ?a foaf:knows ?a .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" .\n}",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-05.ru",
    comment:"dawg-delete-insert-06 and dawg-delete-insert-06b show that the rewriting in dawg-delete-insert-05b.ru isn\'t equivalent to dawg-delete-insert-05.ru in case Alan doesn\'t know anybody.",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows [] .\n}\nINSERT\n{\n  ?a foaf:knows ?a .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" .\n}",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-07.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' and inserts a single foaf:knows triple with a blank node as object for \'Alan\'. This shows the different behavior of bnodes in INSERT (similar to CONSTRUCT) and DELETE (bnodes act as wildcards) templates.",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows [] .\n}\nINSERT\n{\n  ?a foaf:knows [] .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" . \n}",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-07b.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' and replaces them by bnodes. This shows the different behavior of bnodes in INSERT (similar to CONSTRUCT) and DELETE (bnodes act as wildcards) templates. As opposed to test case dawg-delete-insert-7, note that the result graph in this example is non-lean.",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows [] .\n}\nINSERT\n{\n  ?a foaf:knows [] .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" . ?a foaf:knows [] .\n}",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-08.ru",
    comment:"This DELETE test was first brought up in http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2011JanMar\/0290.html. It demonstrates how unbound variables (from an OPTIONAL) are handled in DELETE templates",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nDELETE\n { _:a :p 12 .\n   _:a :q ?o .\n }\nWHERE {?s :r ?q OPTIONAL { ?q :s ?o } }",
    expected:false
  },
  { name:"data-sparql11/delete-insert/delete-insert-09.ru",
    comment:"This DELETE test was first brought up in http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2011JanMar\/0317.html. It demonstrates the behavior of shared bnodes in a DELETE template.",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nDELETE\n { _:a :p 12 .\n   _:a :q _:b .\n }\nWHERE {}",
    expected:false
  },
  { name:"data-sparql11/entailment/rdf01.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\nSELECT ?x\nWHERE {\n  ex:b ?x rdf:Property .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdf02.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\nSELECT ?x\nWHERE {\n  ?x rdf:type rdf:Property .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdf03.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nSELECT ?x\nWHERE {\n  ?x ex:b1 _:c .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdf04.rq",
    comment:"",
    query:"PREFIX    :  <http:\/\/example.org\/x\/>\nPREFIX rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\nSELECT ?x\nWHERE { ?x rdf:type :c . }",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs01.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nSELECT ?x\nWHERE {\n  ex:a ?x ex:c .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs02.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nSELECT ?x\nWHERE {\n  ?x ex:b2 ex:c .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs03.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nSELECT ?x\nWHERE {\n  ?x rdf:type ex:c2 .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs04.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nSELECT ?x\nWHERE {\n  ?x rdf:type ex:c2 .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs05.rq",
    comment:"",
    query:"PREFIX     :  <http:\/\/example.org\/x\/>\nPREFIX  rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX rdfs:  <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\n\nSELECT ?x ?c\nWHERE { ?x rdf:type ?c . ?c rdfs:subClassOf :d }",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs06.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nSELECT ?x\nWHERE {\n  ?x rdf:type ex:aType .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs07.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nSELECT ?x\nWHERE {\n  ?x rdf:type ex:cType .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs08.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nSELECT ?x\nWHERE {\n  ex:d rdfs:range ?x .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs09.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nSELECT ?x\nWHERE {\n  ?x rdf:type ex:f .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs10.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nSELECT ?x\nWHERE {\n  ?x ex:f ?y .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs11.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nSELECT ?x\nWHERE {\n  ?x rdfs:subClassOf rdfs:Container .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs12.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example.org\/ns#>\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nSELECT ?x\nWHERE {\n  ?x rdf:type rdfs:ContainerMembershipProperty .\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/rdfs13.rq",
    comment:"",
    query:"PREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nSELECT ?L\nWHERE {\n  ?L a rdfs:Literal\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/d-ent-01.rq",
    comment:"",
    query:"PREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?L\nWHERE {\n  ?L a xsd:integer\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/owlds01.rq",
    comment:"",
    query:"PREFIX   ex:  <http:\/\/example.org\/x\/>\nPREFIX  rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX rdfs:  <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?x ?c\nWHERE { \n?x rdf:type ?c . \n?c rdfs:subClassOf ex:c . \n?x ex:p _:y . \n\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/owlds02.rq",
    comment:"",
    query:"PREFIX     :  <http:\/\/example.org\/x\/>\nPREFIX  rdf:  <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX rdfs:  <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\n\nSELECT ?x ?y\nWHERE { \n?x :p ?y . \n?y rdf:type :c . \n}",
    expected:true
  },
  { name:"data-sparql11/entailment/lang.rq",
    comment:"",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX foaf:  <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT ?x\nWHERE { ?x foaf:name \"name\"@en .\n      }",
    expected:true
  },
  { name:"data-sparql11/entailment/bind01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?z\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  BIND(?o+10 AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?o ?z ?z2\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  BIND(?o+10 AS ?z)\n  BIND(?o+100 AS ?z2)\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?z ?s1\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  BIND(?o+1 AS ?z)\n  ?s1 ?p1 ?z .\n  ?p1 a owl:DatatypeProperty . \n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT *\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  BIND(?nova AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?s ?p ?o ?z\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  BIND(?o+1 AS ?z)\n  FILTER(?z = 3 )\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind06.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT *\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  BIND(?o+10 AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind07.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?s ?p ?o ?z\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  { BIND(?o+1 AS ?z) } UNION { BIND(?o+2 AS ?z) }\n}",
    expected:true
  },
  { name:"data-sparql11/entailment/bind08.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/> \nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX owl: <http:\/\/www.w3.org\/2002\/07\/owl#>\n\nSELECT ?s ?p ?o ?z\n{\n  ?s ?p ?o .\n  ?p a owl:DatatypeProperty . \n  FILTER(?z = 3 )\n  BIND(?o+1 AS ?z)\n}",
    expected:true
  },
  { name:"data-sparql11/exists/exists01.rq",
    comment:"",
    query:"prefix ex: <http:\/\/www.example.org\/>\n\nselect * where {\n?s ?p ?o\nfilter exists {?s ?p ex:o}\n}",
    expected:true
  },
  { name:"data-sparql11/exists/exists02.rq",
    comment:"",
    query:"prefix ex: <http:\/\/www.example.org\/>\n\nselect * where {\n?s ?p ex:o2\nfilter exists {ex:s ex:p ex:o}\n}",
    expected:true
  },
  { name:"data-sparql11/exists/exists03.rq",
    comment:"Checks that exists is interpreted within named graph",
    query:"prefix ex: <http:\/\/www.example.org\/>\n\nselect * where {\ngraph <exists02.ttl> { \n  ?s ?p ex:o1\n  filter exists { ?s ?p ex:o2 } \n}\n\n}",
    expected:true
  },
  { name:"data-sparql11/exists/exists04.rq",
    comment:"",
    query:"prefix ex: <http:\/\/www.example.org\/>\n\nselect * where {\n  ?s ?p ex:o\n  filter exists { ?s ?p ex:o1  filter exists { ?s ?p ex:o2 } } \n}",
    expected:true
  },
  { name:"data-sparql11/exists/exists05.rq",
    comment:"",
    query:"prefix ex: <http:\/\/www.example.org\/>\n\nselect * where {\n  ?s ?p ex:o\n  filter exists { ?s ?p ex:o1  filter not exists { ?s ?p ex:o2 } } \n}",
    expected:true
  },
  { name:"data-sparql11/functions/strdt01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s (STRDT(?str,xsd:string) AS ?str1) WHERE {\n\t?s :str ?str\n\tFILTER(LANGMATCHES(LANG(?str), \"en\"))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strdt02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s (STRDT(STR(?str),xsd:string) AS ?str1) WHERE {\n\t?s :str ?str\n\tFILTER(LANGMATCHES(LANG(?str), \"en\"))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strdt03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s (STRDT(?o,xsd:string) AS ?str1) WHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strlang01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (STRLANG(?str,\"en-US\") AS ?s2) WHERE {\n\t?s :str ?str\n\tFILTER(LANGMATCHES(LANG(?str), \"en\"))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strlang02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (STRLANG(STR(?str),\"en-US\") AS ?s2) WHERE {\n\t?s :str ?str\n\tFILTER(LANGMATCHES(LANG(?str), \"en\"))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strlang03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (STRLANG(?o,\"en-US\") AS ?str1) WHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/functions/isnumeric01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s ?num WHERE {\n\t?s ?p ?num\n\tFILTER isNumeric(?num)\n}",
    expected:true
  },
  { name:"data-sparql11/functions/abs01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE {\n\t?s :num ?num\n\tFILTER(ABS(?num) >= 2)\n}",
    expected:true
  },
  { name:"data-sparql11/functions/ceil01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?num (CEIL(?num) AS ?ceil) WHERE {\n\t?s :num ?num\n}",
    expected:true
  },
  { name:"data-sparql11/functions/floor01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?num (FLOOR(?num) AS ?floor) WHERE {\n\t?s :num ?num\n}",
    expected:true
  },
  { name:"data-sparql11/functions/round01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?num (ROUND(?num) AS ?round) WHERE {\n\t?s :num ?num\n}",
    expected:true
  },
  { name:"data-sparql11/functions/concat01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (CONCAT(?str1,?str2) AS ?str) WHERE {\n\t:s6 :str ?str1 .\n\t:s7 :str ?str2 .\n}",
    expected:true
  },
  { name:"data-sparql11/functions/concat02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (CONCAT(?str1,?str2) AS ?str) WHERE {\n\t?s1 :str ?str1 .\n\t?s2 :str ?str2 .\n}",
    expected:true
  },
  { name:"data-sparql11/functions/substring01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s ?str (SUBSTR(?str,1,1) AS ?substr) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/substring02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s ?str (SUBSTR(?str,2) AS ?substr) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/length01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?str (STRLEN(?str) AS ?len) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/ucase01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (UCASE(?str) AS ?ustr) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/lcase01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (LCASE(?str) AS ?lstr) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/encode01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?str (ENCODE_FOR_URI(?str) AS ?encoded) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/contains01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?str WHERE {\n\t?s :str ?str\n\tFILTER CONTAINS(?str, \"a\")\n}",
    expected:true
  },
  { name:"data-sparql11/functions/starts01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?str WHERE {\n\t?s ?p ?str\n\tFILTER STRSTARTS(STR(?str), \"1\")\n}",
    expected:true
  },
  { name:"data-sparql11/functions/ends01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s ?str WHERE {\n\t?s ?p ?str\n\tFILTER STRENDS(?str, \"bc\")\n}",
    expected:true
  },
  { name:"data-sparql11/functions/plus-1.rq",
    comment:"plus operator on ?x + ?y on string and numeric values",
    query:"PREFIX  : <http:\/\/example\/>\nSELECT  ?x ?y ( ?x + ?y AS ?sum)\nWHERE\n    { ?s :p ?x ; :q ?y . \n    }\nORDER BY ?x ?y ?sum",
    expected:true
  },
  { name:"data-sparql11/functions/plus-2.rq",
    comment:"plus operator in combination with str(), i.e.  str(?x) + str(?y), on string and numeric values",
    query:"PREFIX  : <http:\/\/example\/>\n\nSELECT  ?x ?y ( str(?x) + str(?y) AS ?sum)\nWHERE\n    { ?s :p ?x ; :q ?y . \n    }\nORDER BY ?x ?y ?sum",
    expected:true
  },
  { name:"data-sparql11/functions/md5-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (MD5(?l) AS ?hash) WHERE {\n\t:s1 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/md5-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (MD5(?l) AS ?hash) WHERE {\n\t:s4 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/sha1-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (SHA1(?l) AS ?hash) WHERE {\n\t:s1 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/sha1-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (SHA1(?l) AS ?hash) WHERE {\n\t:s8 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/sha256-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (SHA256(?l) AS ?hash) WHERE {\n\t:s1 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/sha256-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (SHA256(?l) AS ?hash) WHERE {\n\t:s8 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/sha512-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (SHA512(?l) AS ?hash) WHERE {\n\t:s1 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/sha512-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT (SHA512(?l) AS ?hash) WHERE {\n\t:s8 :str ?l\n}",
    expected:true
  },
  { name:"data-sparql11/functions/minutes-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (MINUTES(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/seconds-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (SECONDS(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/hours-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (HOURS(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/month-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (MONTH(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/year-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (YEAR(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/day-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (DAY(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/timezone-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (TIMEZONE(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/tz-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s (TZ(?date) AS ?x) WHERE {\n\t?s :date ?date\n}",
    expected:true
  },
  { name:"data-sparql11/functions/bnode01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s1 ?s2\n(BNODE(?s1) AS ?b1) (BNODE(?s2) AS ?b2)\nWHERE {\n\t?a :str ?s1 .\n\t?b :str ?s2 .\n\tFILTER (?a = :s1 || ?a = :s3)\n\tFILTER (?b = :s1 || ?b = :s3)\n}",
    expected:true
  },
  { name:"data-sparql11/functions/bnode02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT (BNODE() AS ?b1) (BNODE() AS ?b2)\nWHERE {}",
    expected:true
  },
  { name:"data-sparql11/functions/in01.rq",
    comment:"",
    query:"ASK {\n\tFILTER(2 IN (1, 2, 3))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/in02.rq",
    comment:"",
    query:"ASK {\n\tFILTER(2 IN (1, 3))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/notin01.rq",
    comment:"",
    query:"ASK {\n\tFILTER(2 NOT IN ())\n}",
    expected:true
  },
  { name:"data-sparql11/functions/notin02.rq",
    comment:"",
    query:"ASK {\n\tFILTER(2 NOT IN (1\/0, 2))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/now01.rq",
    comment:"",
    query:"PREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK {\n\tBIND(NOW() AS ?n)\n\tFILTER(DATATYPE(?n) = xsd:dateTime)\n}",
    expected:true
  },
  { name:"data-sparql11/functions/rand01.rq",
    comment:"",
    query:"PREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK {\n\tBIND(RAND() AS ?r)\n\tFILTER(DATATYPE(?r) = xsd:double && ?r >= 0.0 && ?r < 1.0)\n}",
    expected:true
  },
  { name:"data-sparql11/functions/iri01.rq",
    comment:"",
    query:"BASE <http:\/\/example.org\/>\nSELECT (URI(\"uri\") AS ?uri) (IRI(\"iri\") AS ?iri)\nWHERE {}",
    expected:true
  },
  { name:"data-sparql11/functions/if01.rq",
    comment:"",
    query:"BASE <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?o (IF(lang(?o) = \"ja\", true, false) AS ?integer)\nWHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/functions/if02.rq",
    comment:"",
    query:"SELECT (IF(1\/0, false, true) AS ?error) WHERE {}",
    expected:true
  },
  { name:"data-sparql11/functions/coalesce01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT\n\t(COALESCE(?x, -1) AS ?cx)     # error when ?x is unbound -> -1\n\t(COALESCE(?o\/?x, -2) AS ?div) # error when ?x is unbound or zero -> -2\n\t(COALESCE(?z, -3) AS ?def)    # always unbound -> -3\n\t(COALESCE(?z) AS ?err)        # always an error -> unbound\nWHERE {\n\t?s :p ?o .\n\tOPTIONAL {\n\t\t?s :q ?x\n\t}\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strbefore01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s (STRBEFORE(?str,\"s\") AS ?prefix) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strbefore02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT\n\t?s\n\t?str\n\t(STRBEFORE(?str,\"b\") AS ?bb)\n\t(STRBEFORE(?str,\"bc\") AS ?bbc)\n\t(STRBEFORE(?str,\"b\"@cy) AS ?bbcy)\n\t(STRBEFORE(?str,\"\") AS ?b)\n\t(STRBEFORE(?str,\"\"@en) AS ?ben)\n\t(STRBEFORE(?str,\"b\"^^xsd:string) AS ?bbx)\n\t(STRBEFORE(?str,\"xyz\"^^xsd:string) AS ?bxyzx)\nWHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strafter01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s (STRAFTER(?str,\"e\") AS ?suffix) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/strafter02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT\n\t?s\n\t?str\n\t(STRAFTER(?str,\"b\") AS ?ab)\n\t(STRAFTER(?str,\"ab\") AS ?aab)\n\t(STRAFTER(?str,\"b\"@cy) AS ?abcy)\n\t(STRAFTER(?str,\"\") AS ?a)\n\t(STRAFTER(?str,\"\"@en) AS ?aen)\n\t(STRAFTER(?str,\"b\"^^xsd:string) AS ?abx)\n\t(STRAFTER(?str,\"xyz\"^^xsd:string) AS ?axyzx)\nWHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/replace01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s (REPLACE(?str,\"[^a-z0-9]\", \"-\") AS ?new) WHERE {\n\t?s :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/replace02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT (REPLACE(?str,\"ana\", \"*\") AS ?new) WHERE {\n\t:s8 :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/replace03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT (REPLACE(?str,\"(ab)|(a)\", \"[1=$1][2=$2]\") AS ?new) WHERE {\n\t:s9 :str ?str\n}",
    expected:true
  },
  { name:"data-sparql11/functions/uuid01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT (STRLEN(STR(?uuid)) AS ?length)\nWHERE {\n\tBIND(UUID() AS ?uuid)\n\tFILTER(ISIRI(?uuid) && REGEX(STR(?uuid), \"^urn:uuid:[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$\", \"i\"))\n}",
    expected:true
  },
  { name:"data-sparql11/functions/struuid01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT (STRLEN(?uuid) AS ?length)\nWHERE {\n\tBIND(STRUUID() AS ?uuid)\n\tFILTER(ISLITERAL(?uuid) && REGEX(?uuid, \"^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$\", \"i\"))\n}",
    expected:true
  },
  { name:"data-sparql11/grouping/group01.rq",
    comment:"Simple grouping",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?s\n{\n  ?s :p ?v .\n}\nGROUP BY ?s",
    expected:true
  },
  { name:"data-sparql11/grouping/group03.rq",
    comment:"Grouping with an unbound",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?w (SAMPLE(?v) AS ?S)\n{\n  ?s :p ?v .\n  OPTIONAL { ?s :q ?w }\n}\nGROUP BY ?w",
    expected:true
  },
  { name:"data-sparql11/grouping/group04.rq",
    comment:"Grouping with expression",
    query:"PREFIX :        <http:\/\/example\/>\nPREFIX xsd:     <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?X (SAMPLE(?v) AS ?S)\n{\n  ?s :p ?v .\n  OPTIONAL { ?s :q ?w }\n}\nGROUP BY (COALESCE(?w, \"1605-11-05\"^^xsd:date) AS ?X)",
    expected:true
  },
  { name:"data-sparql11/grouping/group05.rq",
    comment:"Grouping with unbound ",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?s ?w\n{\n  ?s :p ?v .\n  OPTIONAL { ?s :q ?w }\n}\nGROUP BY ?s ?w",
    expected:true
  },
  { name:"data-sparql11/grouping/group06.rq",
    comment:"projection of ungrouped variable",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?s ?v\n{\n  ?s :p ?v .\n}\nGROUP BY ?s",
    expected:false
  },
  { name:"data-sparql11/grouping/group07.rq",
    comment:"projection of ungrouped variable, more complex example than Group-6",
    query:"prefix lode: <http:\/\/linkedevents.org\/ontology\/>\nprefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nprefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\n\nselect ?event ?eventName ?venue ?photo\nwhere {\n   ?photo lode:illustrate ?event .\n   {\n   select ?event ?eventName ?venue\n   where {\n         ?event dc:title ?eventName .\n         ?event lode:atPlace ?venue .\n         ?venue rdfs:label \"Live Music Hall\" .\n         }\n   }\n}\nGROUP BY ?event",
    expected:false
  },
  { name:"data-sparql11/json-res/jsonres01.rq",
    comment:"SELECT * WHERE { ?S ?P ?O }",
    query:"PREFIX : <http:\/\/example.org\/>\n\nSELECT * WHERE { ?s ?p ?o} ORDER BY ?s ?p ?o",
    expected:true
  },
  { name:"data-sparql11/json-res/jsonres02.rq",
    comment:"SELECT with OPTIONAL (i.e. not all vars bound in all results)",
    query:"PREFIX : <http:\/\/example.org\/>\n\nSELECT * WHERE { ?s ?p ?o OPTIONAL {?o ?p2 ?o2 } } ORDER BY ?s ?p ?o ?p2 ?o2",
    expected:true
  },
  { name:"data-sparql11/json-res/jsonres03.rq",
    comment:"ASK - answer: true",
    query:"PREFIX : <http:\/\/example.org\/>\n\nASK WHERE { :s1 :p1 :s2 }",
    expected:true
  },
  { name:"data-sparql11/json-res/jsonres04.rq",
    comment:"ASK - answer: false",
    query:"PREFIX : <http:\/\/example.org\/>\n\nASK WHERE { :s1 :p1 :o1 }",
    expected:true
  },
  { name:"data-sparql11/negation/subsetByExcl01.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/www.w3.org\/2009\/sparql\/docs\/tests\/data-sparql11\/negation#>\nSELECT ?animal { \n  ?animal a ex:Animal \n  FILTER NOT EXISTS { ?animal a ex:Insect } \n}",
    expected:true
  },
  { name:"data-sparql11/negation/subsetByExcl02.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/www.w3.org\/2009\/sparql\/docs\/tests\/data-sparql11\/negation#>\nSELECT ?animal { \n  ?animal a ex:Animal MINUS { \n    ?animal a ?type \n    FILTER(?type = ex:Reptile || ?type = ex:Insect) \n  } \n}",
    expected:true
  },
  { name:"data-sparql11/negation/temporalProximity01.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/www.w3.org\/2009\/sparql\/docs\/tests\/data-sparql11\/negation#>\nPREFIX dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\n\n# The closest pre-operative physical examination\nSELECT ?exam ?date { \n  ?exam a ex:PhysicalExamination; \n        dc:date ?date;\n        ex:precedes ex:operation1 .\n  ?op   a ex:SurgicalProcedure; dc:date ?opDT .\n  FILTER NOT EXISTS {\n    ?otherExam a ex:PhysicalExamination; \n               ex:follows ?exam;\n               ex:precedes ex:operation1\n  } \n}",
    expected:true
  },
  { name:"data-sparql11/negation/subset-01.rq",
    comment:"",
    query:"PREFIX :    <http:\/\/example\/>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n# SPARQL 1.1\nSELECT (?s1 AS ?subset) (?s2 AS ?superset)\nWHERE\n{\n    # All pairs of sets except (S,S)\n    ?s2 rdf:type :Set .\n    ?s1 rdf:type :Set .\n    FILTER(?s1 != ?s2)\n    MINUS \n    {\n    \t# The MINUS RHS is (?s1, ?s2) where \n        # ?s1 has a member not in ?s2\n        ?s1 rdf:type :Set .\n        ?s2 rdf:type :Set .\n        FILTER(?s1 != ?s2)\n\n        ?s1 :member ?x .\n        FILTER NOT EXISTS { ?s2 :member ?x . }\n    }\n}",
    expected:true
  },
  { name:"data-sparql11/negation/subset-02.rq",
    comment:"",
    query:"PREFIX :    <http:\/\/example\/>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\nSELECT (?s1 AS ?subset) (?s2 AS ?superset)\nWHERE\n{\n    # All pairs of sets\n    ?s2 rdf:type :Set .\n    ?s1 rdf:type :Set .\n\n    MINUS {\n        ?s1 rdf:type :Set .\n        ?s2 rdf:type :Set .\n        # Assumes ?s1 has at least one member \n        ?s1 :member ?x .\n        # If we want to exclude A as a subset of A.\n        # This is not perfect as \"?s1 = ?s2\" is not a\n        # contents based comparison.\n        FILTER ( ?s1 = ?s2 || NOT EXISTS { ?s2 :member ?x . } )\n    }\n    MINUS {\n        # If we don\'t want the empty set being a subset of itself.\n        ?s1 rdf:type :Set .\n        ?s2 rdf:type :Set .\n        # Choose the pair (empty set, empty set)\n        FILTER ( NOT EXISTS { ?s1 :member ?y . } )\n        FILTER ( NOT EXISTS { ?s2 :member ?y . } )\n    }\n}",
    expected:true
  },
  { name:"data-sparql11/negation/set-equals-1.rq",
    comment:"",
    query:"PREFIX :    <http:\/\/example\/>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\n# Find sets that have exactly the same members.\n# Find all (s1,s2) such that (s1 subset of s) and (s2 subset of s1).\n\nSELECT DISTINCT ?s1 ?s2\nWHERE\n{\n    # All pairs of sets (no duplicates, not reveres pairs)\n    ?s2 rdf:type :Set .\n    ?s1 rdf:type :Set .\n    FILTER(str(?s1) < str(?s2))\n    MINUS \n    {\n        ?s1 rdf:type :Set .\n        ?s2 rdf:type :Set .\n        ?s1 :member ?x .\n        FILTER NOT EXISTS { ?s2 :member ?x . }\n    }\n    MINUS \n    {\n        ?s1 rdf:type :Set .\n        ?s2 rdf:type :Set .\n        ?s2 :member ?x .\n        FILTER NOT EXISTS { ?s1 :member ?x . }\n    }\n}",
    expected:true
  },
  { name:"data-sparql11/negation/subset-03.rq",
    comment:"",
    query:"PREFIX :    <http:\/\/example\/>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\nSELECT (?s1 AS ?subset) (?s2 AS ?superset)\nWHERE\n{\n    # All pairs of sets except (S,S)\n    ?s2 rdf:type :Set .\n    ?s1 rdf:type :Set .\n    MINUS {\n        # See subset-01 ...\n        ?s1 rdf:type :Set .\n        ?s2 rdf:type :Set .\n        ?s1 :member ?x .\n        FILTER ( NOT EXISTS { ?s2 :member ?x . } )\n    }\n    # Remove those that are the pairs with the same elements.\n    # See set-equals-1\n    MINUS {\n        ?s2 rdf:type :Set .\n        ?s1 rdf:type :Set .\n        MINUS \n        {\n            ?s1 rdf:type :Set .\n            ?s2 rdf:type :Set .\n            ?s1 :member ?x .\n            FILTER NOT EXISTS { ?s2 :member ?x . }\n        }\n        MINUS \n        {\n            ?s1 rdf:type :Set .\n            ?s2 rdf:type :Set .\n            ?s2 :member ?x .\n            FILTER NOT EXISTS { ?s1 :member ?x . }\n        }\n    }\n}",
    expected:true
  },
  { name:"data-sparql11/negation/exists-01.rq",
    comment:"",
    query:"PREFIX :    <http:\/\/example\/>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n# SPARQL 1.1\nSELECT *\nWHERE\n{\n\t?set a :Set .\n\tFILTER EXISTS {\n\t\t?set :member 9\n\t}\n}",
    expected:true
  },
  { name:"data-sparql11/negation/exists-02.rq",
    comment:"",
    query:"PREFIX :    <http:\/\/example\/>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n# SPARQL 1.1\nSELECT *\nWHERE\n{\n\t?set a :Set .\n\tFILTER EXISTS {\n\t\t?set :member 7\n\t}\n}",
    expected:true
  },
  { name:"data-sparql11/negation/full-minuend.rq",
    comment:"",
    query:"prefix : <http:\/\/example\/>\n\nselect ?a ?b ?c {\n  ?a :p1 ?b; :p2 ?c\n  MINUS {\n    ?d a :Sub\n    OPTIONAL { ?d :q1 ?b }\n    OPTIONAL { ?d :q2 ?c }\n  }\n}\norder by ?a",
    expected:true
  },
  { name:"data-sparql11/negation/part-minuend.rq",
    comment:"",
    query:"prefix : <http:\/\/example\/>\n\nselect ?a ?b ?c {\n  ?a a :Min\n  OPTIONAL { ?a :p1 ?b }\n  OPTIONAL { ?a :p2 ?c }\n  MINUS {\n    ?d a :Sub\n    OPTIONAL { ?d :q1 ?b }\n    OPTIONAL { ?d :q2 ?c }\n  }\n}\norder by ?a",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp01.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x ?y ?z ((?y = ?z) as ?eq) where {\n  ?x ex:p ?y .\n  ?x ex:q ?z\n}",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp02.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x ?y ?z ((?y + ?z) as ?sum) where {\n  ?x ex:p ?y .\n  ?x ex:q ?z\n}",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp03.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x ?y ?z\n  ((?y + ?z) as ?sum) \n  ((2 * ?sum) as ?twice)\nwhere {\n  ?x ex:p ?y .\n  ?x ex:q ?z\n}",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp04.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x ?y \n  ((?y + ?y) as ?sum) \nwhere {\n  ?x ex:p ?y\n}\norder by ?sum",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp05.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x ?l (datatype(?l) as ?dt) where {\n  ?x ex:p ?l\n}",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp06.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect  ?x ?l (datatype(?m) as ?dt) where {\n  ?x ex:p ?l\n}",
    expected:true
  },
  { name:"data-sparql11/project-expression/projexp07.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x (datatype(?l) as ?dt) where {\n  ?x ex:p ?y .\n  optional {?x ex:q ?l}\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp01.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\nin:a ex:p1\/ex:p2\/ex:p3 ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp02.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\nin:a (ex:p1\/ex:p2\/ex:p3)* ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp03.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\nin:a ex:p1\/ex:p2\/ex:p3\/ex:p4 ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp06.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x where {\ngraph ?g {in:a ex:p1\/ex:p2 ?x}\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp06.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x where {\ngraph ?g {in:a ex:p1\/ex:p2 ?x}\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp08.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nask {\nin:b ^ex:p in:a\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp09.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect  * where {\nin:c ^(ex:p1\/ex:p2) ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp10.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\nin:a !(ex:p1|ex:p2) ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp11.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\nin:a ex:p1\/ex:p2 ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp12.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\nin:a (ex:p1\/ex:p2)+ ?x\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp14.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT *\nWHERE { ?X foaf:knows* ?Y } \nORDER BY ?X ?Y",
    expected:true
  },
  { name:"data-sparql11/property-path/pp14.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT *\nWHERE { ?X foaf:knows* ?Y } \nORDER BY ?X ?Y",
    expected:true
  },
  { name:"data-sparql11/property-path/path-2-2.rq",
    comment:"",
    query:"prefix : <http:\/\/example\/> \n\nselect * where {\n    :a :p+ ?z\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-2-2.rq",
    comment:"",
    query:"prefix : <http:\/\/example\/> \n\nselect * where {\n    :a :p+ ?z\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-2-2.rq",
    comment:"",
    query:"prefix : <http:\/\/example\/> \n\nselect * where {\n    :a :p+ ?z\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-3-3.rq",
    comment:"",
    query:"prefix : <http:\/\/example\/> \n\nselect * where {\n    :a (:p\/:p)? ?t\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-p1.rq",
    comment:"",
    query:"prefix :  <http:\/\/www.example.org\/>\nselect ?t\nwhere {\n  :a :p1|:p2\/:p3|:p4 ?t\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-p2.rq",
    comment:"",
    query:"prefix :  <http:\/\/www.example.org\/>\nselect ?t\nwhere {\n  :a (:p1|:p2)\/(:p3|:p4) ?t\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-p3.rq",
    comment:"",
    query:"prefix :  <http:\/\/www.example.org\/>\nselect ?t\nwhere {\n  :a :p0|^:p1\/:p2|:p3 ?t\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-p4.rq",
    comment:"",
    query:"prefix :  <http:\/\/www.example.org\/>\nselect ?t\nwhere {\n  :a (:p0|^:p1)\/:p2|:p3 ?t\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-ng-01.rq",
    comment:"",
    query:"prefix :  <http:\/\/www.example.org\/>\nselect ?t\nwhere {\n  GRAPH <ng-01.ttl> {\n    ?s :p1* ?t }\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/path-ng-02.rq",
    comment:"",
    query:"prefix :  <http:\/\/www.example.org\/>\nselect ?t\nwhere {\n  GRAPH ?g {\n    ?s :p1* ?t }\n  FILTER (?g = <ng-01.ttl>)\n}",
    expected:true
  },
  { name:"data-sparql11/property-path/pp36.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { :a0 (:p)* :a1 }",
    expected:true
  },
  { name:"data-sparql11/property-path/pp37.rq",
    comment:"Test case as per http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg-comments\/2012Feb\/0006.html",
    query:"prefix : <http:\/\/example.org\/>\nselect ?X where { :A0 ((:P)*)* ?X }\norder by ?X",
    expected:true
  },
  { name:"data-sparql11/service/service01.rq",
    comment:"",
    query:"# SERVICE join with pattern in the default graph\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2 \n{\n  ?s ?p1 ?o1 .\n  SERVICE <http:\/\/example.org\/sparql> {\n    ?s ?p2 ?o2\n  }\n}",
    expected:true
  },
  { name:"data-sparql11/service/service02.rq",
    comment:"",
    query:"# SERVICE and OPTIONAL\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  SERVICE <http:\/\/example1.org\/sparql> {\n  ?s ?p ?o1 . }\n  OPTIONAL {\n\tSERVICE <http:\/\/example2.org\/sparql> {\n    ?s ?p2 ?o2 }\n  }\n}",
    expected:true
  },
  { name:"data-sparql11/service/service03.rq",
    comment:"",
    query:"# SERVICE with one optional and a nested SERVICE. This query depends in the capabilities of the example1.org endpoint\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  SERVICE <http:\/\/example1.org\/sparql> {\n  ?s ?p ?o1 .\n  OPTIONAL {\n\tSERVICE <http:\/\/example2.org\/sparql> {\n    ?s ?p2 ?o2 }\n  }\n}\n}",
    expected:true
  },
  { name:"data-sparql11/service/service04a.rq",
    comment:"",
    query:"# bindings with two variables and two sets of values\n\nPREFIX : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \nSELECT ?s ?o1 ?o2\n{\n  ?s ?p1 ?o1 \n  OPTIONAL { SERVICE <http:\/\/example.org\/sparql> {?s foaf:knows ?o2 }}\n} VALUES (?o2) {\n (:b)\n}",
    expected:true
  },
  { name:"data-sparql11/service/service05.rq",
    comment:"",
    query:"PREFIX  void: <http:\/\/rdfs.org\/ns\/void#>\nPREFIX  dc:   <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  doap: <http:\/\/usefulinc.com\/ns\/doap#> \n\nSELECT ?service ?title\nWHERE {\n  {\n    # Find the service with subject \"remote\".\n    # Inner block to fix the FILTER not be over\n    # just this local pattern.\n    ?p dc:subject ?projectSubject ;\n       void:sparqlEndpoint ?service  \n       FILTER regex(?projectSubject, \"remote\")\n  }\n\n  # Query that service projects.\n \n  SERVICE ?service {\n     ?project  doap:name ?title . } \n}",
    expected:true
  },
  { name:"data-sparql11/service/service06.rq",
    comment:"",
    query:"# SERVICE with one optional and a nested SERVICE. This query depends in the capabilities of the example1.org endpoint\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  SERVICE <http:\/\/example1.org\/sparql> {\n  ?s ?p ?o1 .\n  OPTIONAL {\n\tSERVICE SILENT <http:\/\/invalid.endpoint.org\/sparql> {\n    ?s ?p2 ?o2 }\n  }\n}\n}",
    expected:true
  },
  { name:"data-sparql11/service/service07.rq",
    comment:"",
    query:"# invalid URI for a SERVICE with SILENT\n\nPREFIX : <http:\/\/example.org\/> \n\nSELECT ?s ?o1 ?o2\n{\n  ?s ?p ?o1 .\n  SERVICE SILENT <http:\/\/invalid.endpoint.org\/sparql> {\n    ?s ?p2 ?o2 }\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq01.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect  ?x ?p where {\ngraph ?g {\n{select * where {?x ?p ?y}}\n}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq02.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect  ?x ?p where {\ngraph ?g {\n{select * where {?x ?p ?g}}\n}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq03.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x where {\ngraph ?g {\n  {select ?x where {?x ?p ?g}}\n}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq04.rq",
    comment:"",
    query:"select ?x \nwhere {\ngraph ?g {\n{select * where {?x ?p ?y}}\n}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq05.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x \nwhere {\ngraph ?g {\n{select * where {?x ?p ?y}}\n}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq06.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x\nwhere {\n{select * where {?x ?p ?y}}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq07.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x\nwhere {\n{select * where {graph ?g {?x ?p ?y}}}\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq08.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x ?max where {\n{select (max(?y) as ?max) where {?x ex:p ?y} } \n?x ex:p ?max\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq09.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect * where {\n\n{select * where { \n  {select ?x where {?x ex:q ?t}}\n}}\n\n?x ex:p ?y \n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq10.rq",
    comment:"",
    query:"prefix ex:\t<http:\/\/www.example.org\/schema#>\nprefix in:\t<http:\/\/www.example.org\/instance#>\n\nselect ?x  where {\n{select * where {?x ex:p ?y}} \nfilter(exists {?x ex:q ?y}) \n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq11.rq",
    comment:"This query limits results per number of orders, rather than by number of rows",
    query:"# return labels of items for the first 2 orders\n\nPREFIX : <http:\/\/www.example.org>\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\n\nSELECT ?L\nWHERE {\n ?O :hasItem [ rdfs:label ?L ] .\n {\n SELECT DISTINCT ?O  \n WHERE { ?O a :Order }\n ORDER BY ?O\n LIMIT 2\n }\n} ORDER BY ?L",
    expected:true
  },
  { name:"data-sparql11/subquery/sq12.rq",
    comment:"This query constructs full names from first and last names",
    query:"PREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nCONSTRUCT{ ?P foaf:name ?FullName }\nWHERE {\n SELECT ?P ( CONCAT(?F, \" \", ?L) AS ?FullName ) \n WHERE { ?P foaf:firstName ?F ; foaf:lastName ?L. }\n}",
    expected:true
  },
  { name:"data-sparql11/subquery/sq11.rq",
    comment:"The result of this subquery is a Kartesian product of all orders, rather than paris of orders sharing products, since subqueries are evaluated independent from bindings from outside the subquery",
    query:"# return labels of items for the first 2 orders\n\nPREFIX : <http:\/\/www.example.org>\nPREFIX rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\n\nSELECT ?L\nWHERE {\n ?O :hasItem [ rdfs:label ?L ] .\n {\n SELECT DISTINCT ?O  \n WHERE { ?O a :Order }\n ORDER BY ?O\n LIMIT 2\n }\n} ORDER BY ?L",
    expected:true
  },
  { name:"data-sparql11/subquery/sq14.rq",
    comment:"",
    query:"PREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\nCONSTRUCT {\n   ?person a foaf:Person ;\n           foaf:name ?name ;\n           foaf:homepage ?homepage ;\n           foaf:mbox ?mbox .\n} WHERE {\n  {\n    SELECT ?person ?name WHERE {\n       ?person a foaf:Person ;\n               foaf:name ?name .\n      } ORDER BY ?name LIMIT 3\n  }\n  ?person foaf:homepage ?homepage .\n  OPTIONAL { ?person foaf:mbox ?mbox . }        \n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-select-expr-01.rq",
    comment:"",
    query:"SELECT (?x +?y AS ?z) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-select-expr-02.rq",
    comment:"",
    query:"SELECT ?x ?y (?x +?y AS ?z) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-select-expr-03.rq",
    comment:"",
    query:"SELECT (datatype(?x +?y) AS ?z) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-select-expr-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT (:function(?x +?y) AS ?F) ?z {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-select-expr-05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT (COUNT(*) AS ?count) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-01.rq",
    comment:"",
    query:"SELECT (COUNT(*) AS ?count) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-02.rq",
    comment:"",
    query:"SELECT (COUNT(DISTINCT *) AS ?count) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-03.rq",
    comment:"",
    query:"SELECT (COUNT(?x) AS ?count) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-04.rq",
    comment:"",
    query:"SELECT (COUNT(DISTINCT ?x) AS ?count) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-05.rq",
    comment:"",
    query:"SELECT (SUM(?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-06.rq",
    comment:"",
    query:"SELECT (SUM(DISTINCT ?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-07.rq",
    comment:"",
    query:"SELECT (MIN(?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-08.rq",
    comment:"",
    query:"SELECT (MIN(DISTINCT ?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-09.rq",
    comment:"",
    query:"SELECT (MAX(?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-10.rq",
    comment:"",
    query:"SELECT (MAX(DISTINCT ?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-11.rq",
    comment:"",
    query:"SELECT (AVG(?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-12.rq",
    comment:"",
    query:"SELECT (AVG(DISTINCT ?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-13.rq",
    comment:"",
    query:"SELECT (GROUP_CONCAT(?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-14.rq",
    comment:"",
    query:"SELECT (GROUP_CONCAT(DISTINCT ?x) AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-aggregate-15.rq",
    comment:"",
    query:"SELECT (GROUP_CONCAT(?x; SEPARATOR=\';\') AS ?y) {}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-subquery-01.rq",
    comment:"",
    query:"SELECT * { SELECT * { ?s ?p ?o } }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-subquery-02.rq",
    comment:"",
    query:"SELECT * { \n   {} \n   {SELECT * { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-subquery-03.rq",
    comment:"",
    query:"SELECT * { {} OPTIONAL {SELECT * { ?s ?p ?o }} }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-not-exists-01.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(NOT EXISTS{?s ?p ?o}) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-not-exists-02.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER NOT EXISTS{?s ?p ?o} }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-not-exists-03.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(true && NOT EXISTS{?s ?p ?o}) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-exists-01.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(EXISTS{?s ?p ?o}) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-exists-02.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER EXISTS{?s ?p ?o} }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-exists-03.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(! EXISTS{?s ?p ?o}) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-minus-01.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o MINUS { ?s ?q ?v } }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-oneof-01.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(?o NOT IN(1,2,?s+57)) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-oneof-02.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(?o NOT IN()) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-oneof-03.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o FILTER(?o IN(1,<x>)) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-bindings-01.rq",
    comment:"",
    query:"SELECT ?Z { ?s ?p ?o . BIND(?o+1 AS ?Z) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-bindings-02a.rq",
    comment:"",
    query:"SELECT * { } VALUES () { }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-bindings-03a.rq",
    comment:"",
    query:"SELECT * { } VALUES () { () }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-bindings-05a.rq",
    comment:"",
    query:"SELECT * { } VALUES (?x ?y) { (1 2) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-bind-02.rq",
    comment:"",
    query:"SELECT ?Z { ?s ?p ?o . BIND(?o+1 AS ?Z) BIND(?Z\/2 AS ?Zby2) }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-construct-where-01.rq",
    comment:"",
    query:"CONSTRUCT WHERE { ?s ?p 1816 }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-construct-where-02.rq",
    comment:"",
    query:"CONSTRUCT \nFROM <file>\nWHERE { ?s ?p 1816 }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-bad-01.rq",
    comment:"",
    query:"# Not allowed with GROUP BY\nSELECT * { ?s ?p ?o } GROUP BY ?s",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-02.rq",
    comment:"",
    query:"# required syntax error : out of scope variable in SELECT from group\nSELECT ?o { ?s ?p ?o } GROUP BY ?s",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-03.rq",
    comment:"",
    query:"SELECT (1 AS ?X) (1 AS ?X) {}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-04.rq",
    comment:"",
    query:"SELECT (?x +?y) {}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-05.rq",
    comment:"",
    query:"SELECT COUNT(*) {}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-06.rq",
    comment:"",
    query:"SELECT (SUM(?x,?y) AS ?S) {}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-07.rq",
    comment:"",
    query:"SELECT * { {} SELECT * { ?s ?p ?o } }",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-08.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o UNION ?s ?p ?o  }",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syntax-bindings-09.rq",
    comment:"",
    query:"SELECT * { } BINDINGS ?x ?y { (1 2) (3) }",
    expected:false
  },
  { name:"data-sparql11/syntax-query/qname-escape-02.rq",
    comment:"",
    query:"PREFIX og: <http:\/\/ogp.me\/ns#>\nSELECT * WHERE {\n\t?page og:audio%3Atitle ?title\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/qname-escape-03.rq",
    comment:"",
    query:"PREFIX og: <http:\/\/ogp.me\/ns#>\nSELECT * WHERE {\n\t?page og:audio:title ?title\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope1.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\n SELECT *\n WHERE {\n    :s :p ?o .\n    BIND((1+?o) AS ?o1)\n    :s :q ?o1\n }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope2.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\n SELECT *\n WHERE {\n    :s :p ?o .\n    :s :q ?o1\n    { BIND((1+?o) AS ?o1) }\n }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope3.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\n SELECT *\n WHERE {\n    { \n    :s :p ?o .\n    :s :q ?o1\n    }\n    { BIND((1+?o) AS ?o1) }\n }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope4.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\nSELECT *\n   {\n    { BIND (1 AS ?Y) }\n     UNION\n    { :s :p ?Y }\n  }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope5.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\nSELECT *\n   {\n    { :s :p ?Y }\n     UNION\n    { BIND (1 AS ?Y) } \n   }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope6.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\n SELECT *\n WHERE {\n    :s :p ?o .\n    :s :q ?o1 .\n    BIND((1+?o) AS ?o1)\n }",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope7.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\n SELECT *\n WHERE {\n    {\n    :s :p ?o .\n    :s :q ?o1 .\n    }\n    BIND((1+?o) AS ?o1)\n }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-BINDscope8.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org>\nSELECT *\n   {\n    {\n    { :s :p ?Y }\n     UNION\n    { :s :p ?Z }\n    }\n    BIND (1 AS ?Y) \n   }",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-propertyPaths-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nSELECT ?X WHERE\n{ \n  [ :p|:q|:r ?X ]\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-SELECTscope1.rq",
    comment:"",
    query:"SELECT *\nWHERE {\n  {SELECT (1 AS ?X ) {}\n  }\n  UNION\n  {SELECT (2 AS ?X ) {}\n  }\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syntax-SELECTscope2.rq",
    comment:"",
    query:"SELECT (1 AS ?X ) \n  {\n    SELECT (2 AS ?X ) {} \n  }",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syntax-SELECTscope3.rq",
    comment:"",
    query:"SELECT *\nWHERE {\n  {SELECT (1 AS ?X ) {}\n  }\n  {SELECT (1 AS ?X ) {}\n  }\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nASK{}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-02.rq",
    comment:"",
    query:"PREFIX ex: <http:\/\/example\/>\nASK{}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a :b :c .\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a :b :c:d .\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a :b :c:d\\? .\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-06.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a :b :c\\~z\\. .\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-07.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a :b%3D :c\\~z\\. .\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-08.rq",
    comment:"",
    query:"PREFIX z: <http:\/\/example\/>\nSELECT *\n{\n  _:a z:b _:1_2_3_\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-pname-09.rq",
    comment:"",
    query:"PREFIX z: <http:\/\/example\/z#>\nPREFIX : <http:\/\/example\/>\n\nSELECT *\n{\n  :a :123 :12.3 .\n  :: z:: :_12.3_ .\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-01.rq",
    comment:"",
    query:"# Bad declaration.\nPREFIX ex:ex: <http:\/\/example\/>\n\nASK{}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-02.rq",
    comment:"",
    query:"# Bad declaration.\nPREFIX ex:ex:ex <http:\/\/example\/>\n\nASK{}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-03.rq",
    comment:"",
    query:"# Bad declaration\nPREFIX :: <http:\/\/example\/>\n\nASK{}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-04.rq",
    comment:"",
    query:"# Bad declaration\nPREFIX :a: <http:\/\/example\/>\n\nASK{}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-05.rq",
    comment:"",
    query:"PREFIX 1: <http:\/\/example\/>\n\nASK{}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-06.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a :b :c\\:z .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-07.rq",
    comment:"",
    query:"PREFIX 1: <http:\/\/example\/>\n\nASK{}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-08.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a:b:c .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-09.rq",
    comment:"",
    query:"PREFIX x: <http:\/\/example\/>\nSELECT *\n{\n  x:a:b:c .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-10.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  ?x:a :b :c .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-11.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\nSELECT *\n{\n  :a ?x:b :c .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-12.rq",
    comment:"",
    query:"PREFIX z: <http:\/\/example\/>\nSELECT *\n{\n  z:a z:b ?x:c .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-bad-pname-13.rq",
    comment:"",
    query:"PREFIX z: <http:\/\/example\/>\nSELECT *\n{\n  _:az:b <p> <q> .\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-query/syn-pp-in-collection.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE {\n\t?s ?p ( [:p*\/:q 123 ] [ ^:r \"hello\"] )\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-fed/syntax-service-01.rq",
    comment:"",
    query:"SELECT * { SERVICE <g> { ?s ?p ?o } }",
    expected:true
  },
  { name:"data-sparql11/syntax-fed/syntax-service-02.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o SERVICE <g> { ?s ?p ?o } ?s ?p ?o }",
    expected:true
  },
  { name:"data-sparql11/syntax-fed/syntax-service-03.rq",
    comment:"",
    query:"SELECT * { ?s ?p ?o SERVICE SILENT <g> { ?s ?p ?o } ?s ?p ?o }",
    expected:true
  }
]
