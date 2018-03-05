var sparql11update_all_tests=[
  { name:"data-sparql11/add/add-01.ru",
    comment:"Add the default graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nADD DEFAULT TO :g1",
    expected:true
  },
  { name:"data-sparql11/add/add-01.ru",
    comment:"Add the default graph to a non-existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nADD DEFAULT TO :g1",
    expected:true
  },
  { name:"data-sparql11/add/add-03.ru",
    comment:"Add a named graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nADD :g1 TO :g2",
    expected:true
  },
  { name:"data-sparql11/add/add-03.ru",
    comment:"Add a named graph to a non-existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nADD :g1 TO :g2",
    expected:true
  },
  { name:"data-sparql11/add/add-05.ru",
    comment:"Add a named graph to an existing graph with overlapping data",
    query:"PREFIX : <http:\/\/example.org\/>\nADD :g1 TO :g3",
    expected:true
  },
  { name:"data-sparql11/add/add-06.ru",
    comment:"Add a non-existing graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nADD SILENT :g4 TO :g1",
    expected:true
  },
  { name:"data-sparql11/add/add-07.ru",
    comment:"Add an existing graph to the default graph",
    query:"PREFIX : <http:\/\/example.org\/>\nADD :g1 TO DEFAULT",
    expected:true
  },
  { name:"data-sparql11/add/add-08.ru",
    comment:"Add a graph to itself",
    query:"PREFIX : <http:\/\/example.org\/>\nADD :g1 TO :g1",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-data-spo1.ru",
    comment:"This is a simple insert of a single triple to the unnamed graph of an empty graph store",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nINSERT DATA { :s :p :o }",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-data-named1.ru",
    comment:"This is a simple insert of a single triple into the named graph <http:\/\/example.org\/g1> of an empty graph store",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nINSERT DATA { GRAPH <http:\/\/example.org\/g1> { :s :p :o } }",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-data-named2.ru",
    comment:"This is a simple insert of a single triple into the named graph <http:\/\/example.org\/g1> of a graph store consisting of an empty unnamed graph and the named graph holds one (different) triple already",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nINSERT DATA { GRAPH <http:\/\/example.org\/g1> { :s :p :o2 } }",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-data-named1.ru",
    comment:"This is a simple insert of a single triple into the named graph <http:\/\/example.org\/g1> of a graph store consisting of an empty unnamed graph and the named holds the inserted triple already (using the same query as insert-data-named1)",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nINSERT DATA { GRAPH <http:\/\/example.org\/g1> { :s :p :o } }",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-01.ru",
    comment:"This is a INSERT over a dataset with a single triple in the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nINSERT {\n\t?s ?p \"q\"\n} WHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-02.ru",
    comment:"This is a INSERT over a dataset with a single triple in the default graph, inserting into a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nINSERT {\n\tGRAPH :g1 {\n\t\t?s ?p \"q\"\n\t}\n} WHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-03.ru",
    comment:"This is a INSERT over a dataset with a single triple in a named graph, inserting into the named graph using the WITH keyword",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nWITH :g1\nINSERT {\n\t?s ?p \"z\"\n} WHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-04.ru",
    comment:"This is a INSERT of a triple over a dataset with data in named graphs, inserting into the default graph using the USING keyword",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nINSERT {\n\t?s ?p \"q\"\n}\nUSING :g1\nWHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-using-01.ru",
    comment:"This is an INSERT into the default graph of two triples constructed from the data in two named graphs that are treated as the default graph during matching with the USING keyword.",
    query:"PREFIX     : <http:\/\/example.org\/> \n\nINSERT {\n\t?s ?p \"q\"\n}\nUSING :g1\nUSING :g2\nWHERE {\n\t?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/basic-update/insert-05a.ru",
    comment:"As per http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2012AprJun\/0165.html",
    query:"PREFIX : <http:\/\/example.org\/>\n\nINSERT { GRAPH :g2  { ?S ?P ?O } }\nWHERE { GRAPH :g1  { ?S ?P ?O } } ;\n\nINSERT { GRAPH :g2  { ?S ?P ?O } }\nWHERE { GRAPH :g1  { ?S ?P ?O } } ;\n\nINSERT { GRAPH :g3 { :s :p ?count } }\nWHERE {\n\tSELECT (COUNT(*) AS ?count) WHERE {\n\t\tGRAPH :g2 { ?s ?p ?o }\n\t}\n} ;\nDROP GRAPH :g1 ;\nDROP GRAPH :g2",
    expected:true
  },
  { name:"data-sparql11/copy/copy-01.ru",
    comment:"Copy the default graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nCOPY DEFAULT TO :g1",
    expected:true
  },
  { name:"data-sparql11/copy/copy-01.ru",
    comment:"Copy the default graph to a non-existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nCOPY DEFAULT TO :g1",
    expected:true
  },
  { name:"data-sparql11/copy/copy-03.ru",
    comment:"Copy a named graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nCOPY :g1 TO :g2",
    expected:true
  },
  { name:"data-sparql11/copy/copy-03.ru",
    comment:"Copy a named graph to a non-existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nCOPY :g1 TO :g2",
    expected:true
  },
  { name:"data-sparql11/copy/copy-06.ru",
    comment:"Copy an existing graph to the default graph",
    query:"PREFIX : <http:\/\/example.org\/>\nCOPY :g1 TO DEFAULT",
    expected:true
  },
  { name:"data-sparql11/copy/copy-07.ru",
    comment:"Copy a graph to itself",
    query:"PREFIX : <http:\/\/example.org\/>\nCOPY :g1 TO :g1",
    expected:true
  },
  { name:"data-sparql11/delete-data/delete-data-01.ru",
    comment:"This is a simple delete of an existing triple from the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE DATA \n{\n  :a foaf:knows :b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-data/delete-data-02.ru",
    comment:"This is a simple delete of an existing triple from a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE DATA \n{\n  GRAPH <http:\/\/example.org\/g1> { :a foaf:knows :b }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-data/delete-data-03.ru",
    comment:"This is a simple delete of a non-existing triple from the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE DATA \n{\n  :a foaf:knows :c .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-data/delete-data-04.ru",
    comment:"This is a simple delete of a non-existing triple from a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE DATA \n{\n  GRAPH <http:\/\/example.org\/g1> { :a foaf:knows :c }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-data/delete-data-05.ru",
    comment:"Test 1 for DELETE DATA only modifying the desired graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE DATA \n{ \n  :a foaf:knows :b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-data/delete-data-06.ru",
    comment:"Test 2 for DELETE DATA only modifying the desired graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE DATA \n{\n  GRAPH <http:\/\/example.org\/g2> { :c foaf:name \"Chris\" }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-01.ru",
    comment:"This update request reverts all foaf:knows relations",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?b .\n}\nINSERT\n{\n  ?b foaf:knows ?a .\n}\nWHERE\n{\n  ?a foaf:knows ?b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-01b.ru",
    comment:"This test case, as a variant of dawg-delete-insert-01, shoes that DELETE followed by INSERT is different from DELETE INSERT in a single operation",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?b .\n}\nWHERE\n{\n  ?a foaf:knows ?b .\n}\n;\nINSERT\n{\n  ?b foaf:knows ?a .\n}\nWHERE\n{\n  ?a foaf:knows ?b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-01c.ru",
    comment:"This test case, as a variant of dawg-delete-insert-01, shoes that INSERT followed by DELETE is different from DELETE INSERT in a single operation.",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nINSERT\n{\n  ?b foaf:knows ?a .\n}\nWHERE\n{\n  ?a foaf:knows ?b .\n}\n;\nDELETE \n{\n  ?a foaf:knows ?b .\n}\nWHERE\n{\n  ?a foaf:knows ?b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-02.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\'.",
    query:"PREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?b .\n}\nWHERE\n{\n  ?a foaf:name \"Alan\" . ?a foaf:knows ?b \n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-04.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' using a naive rewriting, as suggested in http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2011JanMar\/0305.html",
    query:"PREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?Var_B .\n}\nWHERE\n{\n  { ?a foaf:name \"Alan\" }\n  { SELECT DISTINCT ?Var_B \n            {  { ?Var_B ?Var_B1 ?Var_B2 } UNION \n               { ?Var_B1 ?Var_B ?Var_B2 } UNION \n               { ?Var_B1 ?Var_B2 ?Var_B } UNION \n               { GRAPH ?Var_Bg {?Var_B ?Var_B1 ?Var_B2 } } UNION\n               { GRAPH ?Var_Bg {?Var_B1 ?Var_B ?Var_B2 } } UNION\n               { GRAPH ?Var_Bg {?Var_B1 ?Var_B2 ?Var_B } } } }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-04b.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' using a simpler rewriting than dawg-delete-insert-04",
    query:"PREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?Var_B .\n}\nWHERE\n{\n  { ?a foaf:name \"Alan\" }\n  { ?a foaf:knows ?Var_B . }\n  \n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-05b.ru",
    comment:"This deletes all foaf:knows relations from anyone named \'Alan\' and inserts that all \'Alans\' know themselves only, using a rewriting analogous to :dawg-delete-insert-04b",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?Var_B .\n}\nINSERT\n{\n  ?a foaf:knows ?a .\n}\nWHERE\n{\n  { ?a foaf:name \"Alan\" . }\n  { ?a foaf:knows ?Var_B . }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-insert/delete-insert-05b.ru",
    comment:"dawg-delete-insert-06 and dawg-delete-insert-06b show that the rewriting in dawg-delete-insert-05b.ru isn\'t equivalent to dawg-delete-insert-05.ru in case Alan doesn\'t know anybody.",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?a foaf:knows ?Var_B .\n}\nINSERT\n{\n  ?a foaf:knows ?a .\n}\nWHERE\n{\n  { ?a foaf:name \"Alan\" . }\n  { ?a foaf:knows ?Var_B . }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-where/delete-where-01.ru",
    comment:"This is a simple delete of an existing triple from the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE WHERE\n{\n  :a foaf:knows ?b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-where/delete-where-02.ru",
    comment:"This is a simple delete of an existing triple from a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE WHERE \n{\n  GRAPH <http:\/\/example.org\/g1> { :a foaf:knows ?b }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-where/delete-where-03.ru",
    comment:"This is a simple delete of a non-existing triple from the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE WHERE \n{\n  ?a foaf:knows :c .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-where/delete-where-04.ru",
    comment:"This is a simple delete of a non-existing triple from a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE WHERE\n{\n  GRAPH <http:\/\/example.org\/g1> { ?a foaf:knows :c }\n}",
    expected:true
  },
  { name:"data-sparql11/delete-where/delete-where-05.ru",
    comment:"Test 1 for DELETE WHERE only modifying the desired graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE WHERE \n{ \n  ?a foaf:knows :b .\n}",
    expected:true
  },
  { name:"data-sparql11/delete-where/delete-where-06.ru",
    comment:"Test 2 for DELETE WHERE only modifying the desired graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE WHERE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?c foaf:name \"Chris\" }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-01.ru",
    comment:"This is a simple delete of an existing triple from the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE\n{\n  :a foaf:knows ?s .\n  ?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-02.ru",
    comment:"This is a simple delete of an existing triple from a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g1> { ?s ?p ?o }\n}\nWHERE \n{\n  GRAPH <http:\/\/example.org\/g1> { :a foaf:knows ?s .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-03.ru",
    comment:"This is a simple delete of a non-existing triple from the default graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE \n{\n  ?s foaf:knows :c .\n  ?s ?p ?o \n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-04.ru",
    comment:"This is a simple delete of a non-existing triple from a named graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g1> { ?s ?p ?o }\n}\nWHERE\n{\n  GRAPH <http:\/\/example.org\/g1> { ?s foaf:knows :c .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-05.ru",
    comment:"Test 1 for DELETE only modifying the desired graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE \n{ \n  :a foaf:knows ?s .\n  ?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-06.ru",
    comment:"Test 2 for DELETE only modifying the desired graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?s ?p ?o }\n}\nWHERE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?s foaf:name \"Chris\" .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-07.ru",
    comment:"This is a simple delete to test that unbound variables in the DELETE clause do not act as wildcards",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE\n{\n  :a foaf:knows ?s .\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-with-01.ru",
    comment:"This is a simple delete using a WITH clause to identify the active graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nWITH <http:\/\/example.org\/g1>\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE\n{\n  :a foaf:knows ?s .\n  ?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-with-02.ru",
    comment:"This is a simple test to make sure the GRAPH clause overrides the WITH clause",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nWITH <http:\/\/example.org\/g2>\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g1> { ?s ?p ?o }\n}\nWHERE \n{\n  GRAPH <http:\/\/example.org\/g1> { :a foaf:knows ?s .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-with-03.ru",
    comment:"This is a simple delete of a non-existing triple using a WITH clause to identify the active graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nWITH <http:\/\/example.org\/g1>\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE \n{\n  ?s foaf:knows :c .\n  ?s ?p ?o\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-with-04.ru",
    comment:"This is a simple delete of a non-existing triple making sure that the GRAPH clause overrides the WITH clause",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nWITH <http:\/\/example.org\/g2>\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g1> { ?s ?p ?o }\n}\nWHERE\n{\n  GRAPH <http:\/\/example.org\/g1> { ?s foaf:knows :c .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-with-05.ru",
    comment:"Test 1 for DELETE only modifying the desired graph using a WITH clause to specify the active graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nWITH <http:\/\/example.org\/g1>\nDELETE \n{\n  ?s ?p ?o .\n}\nWHERE \n{ \n  ?s foaf:knows :b .\n  ?s ?p ?o \n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-with-06.ru",
    comment:"Test 2 for DELETE only modifying the desired graph making sure the GRAPH clause overrides the WITH clause",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nWITH <http:\/\/example.org\/g3>\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?s ?p ?o }\n}\nWHERE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?s foaf:name \"Chris\" .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-using-01.ru",
    comment:"This is a simple delete using a USING clause to identify the active graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nUSING <http:\/\/example.org\/g2>\nWHERE\n{\n  :a foaf:knows ?s .\n  ?s ?p ?o \n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-using-02.ru",
    comment:"This is a simple test to make sure the GRAPH clause overrides the USING clause",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nUSING <http:\/\/example.org\/g3>\nWHERE \n{\n  GRAPH <http:\/\/example.org\/g2> { :a foaf:knows ?s .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-using-03.ru",
    comment:"This is a simple delete of a non-existing triple using a USING clause to identify the active graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nUSING <http:\/\/example.org\/g2>\nWHERE \n{\n  ?s foaf:knows :d .\n  ?s ?p ?o \n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-using-04.ru",
    comment:"This is a simple delete of a non-existing triple making sure that the GRAPH clause overrides the USING clause",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  ?s ?p ?o .\n}\nUSING <http:\/\/example.org\/g3>\nWHERE\n{\n  GRAPH <http:\/\/example.org\/g2> { ?s foaf:knows :d .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-using-05.ru",
    comment:"Test 1 for DELETE only modifying the desired graph using a USING clause to specify the active graph",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g1> { ?s ?p ?o }\n}\nUSING <http:\/\/example.org\/g1>\nWHERE \n{ \n  ?s foaf:knows :b .\n  ?s ?p ?o \n}",
    expected:true
  },
  { name:"data-sparql11/delete/delete-using-06.ru",
    comment:"Test 2 for DELETE only modifying the desired graph making sure the GRAPH clause overrides the USING clause",
    query:"PREFIX     : <http:\/\/example.org\/> \nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/> \n\nDELETE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?s ?p ?o }\n}\nUSING <http:\/\/example.org\/g3>\nWHERE \n{\n  GRAPH <http:\/\/example.org\/g2> { ?s foaf:name \"Chris\" .\n                                  ?s ?p ?o }\n}",
    expected:true
  },
  { name:"data-sparql11/move/move-01.ru",
    comment:"Move the default graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nMOVE DEFAULT TO :g1",
    expected:true
  },
  { name:"data-sparql11/move/move-01.ru",
    comment:"Move the default graph to a non-existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nMOVE DEFAULT TO :g1",
    expected:true
  },
  { name:"data-sparql11/move/move-03.ru",
    comment:"Move a named graph to an existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nMOVE :g1 TO :g2",
    expected:true
  },
  { name:"data-sparql11/move/move-03.ru",
    comment:"Move a named graph to a non-existing graph",
    query:"PREFIX : <http:\/\/example.org\/>\nMOVE :g1 TO :g2",
    expected:true
  },
  { name:"data-sparql11/move/move-06.ru",
    comment:"Move an existing graph to the default graph",
    query:"PREFIX : <http:\/\/example.org\/>\nMOVE :g1 TO DEFAULT",
    expected:true
  },
  { name:"data-sparql11/move/move-07.ru",
    comment:"Move a graph to itself",
    query:"PREFIX : <http:\/\/example.org\/>\nMOVE :g1 TO :g1",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-01.ru",
    comment:"",
    query:"BASE <http:\/\/example\/base#>\nPREFIX : <http:\/\/example\/>\nLOAD <http:\/\/example.org\/faraway>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-02.ru",
    comment:"",
    query:"# Comment\nBASE <http:\/\/example\/base#>\n# Comment\nPREFIX : <http:\/\/example\/>\n# Comment\nLOAD <http:\/\/example.org\/faraway>\n# Comment",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-03.ru",
    comment:"",
    query:"LOAD <http:\/\/example.org\/faraway> ;",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-04.ru",
    comment:"",
    query:"LOAD <http:\/\/example.org\/faraway> INTO GRAPH <localCopy>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-05.ru",
    comment:"",
    query:"DROP NAMED",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-06.ru",
    comment:"",
    query:"DROP DEFAULT",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-07.ru",
    comment:"",
    query:"DROP ALL",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-08.ru",
    comment:"",
    query:"DROP GRAPH <graph>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-09.ru",
    comment:"",
    query:"DROP SILENT NAMED",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-10.ru",
    comment:"",
    query:"DROP SILENT DEFAULT",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-11.ru",
    comment:"",
    query:"DROP SILENT ALL",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-12.ru",
    comment:"",
    query:"DROP SILENT GRAPH <graph>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-13.ru",
    comment:"",
    query:"CREATE GRAPH <graph>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-14.ru",
    comment:"",
    query:"CREATE SILENT GRAPH <graph>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-15.ru",
    comment:"",
    query:"CLEAR NAMED",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-16.ru",
    comment:"",
    query:"CLEAR DEFAULT",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-17.ru",
    comment:"",
    query:"CLEAR ALL",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-18.ru",
    comment:"",
    query:"CLEAR GRAPH <graph>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-19.ru",
    comment:"",
    query:"CLEAR SILENT NAMED",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-20.ru",
    comment:"",
    query:"CLEAR SILENT DEFAULT",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-21.ru",
    comment:"",
    query:"CLEAR SILENT ALL",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-22.ru",
    comment:"",
    query:"CLEAR SILENT GRAPH <graph>",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-23.ru",
    comment:"",
    query:"INSERT DATA { <s> <p> \'o1\', \'o2\', \'o3\' }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-24.ru",
    comment:"",
    query:"INSERT DATA { GRAPH <G> { <s> <p> \'o1\', \'o2\', \'o3\' } }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-25.ru",
    comment:"",
    query:"INSERT DATA { \n  <s1> <p1> <o1>\n  GRAPH <G> { <s> <p1> \'o1\'; <p2> <o2> } \n  GRAPH <G1> { <s> <p1> \'o1\'; <p2> <o2> } \n  <s1> <p1> <o1>\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-26.ru",
    comment:"",
    query:"INSERT \n# Comment\nDATA { GRAPH <G> { <s> <p> \'o1\', \'o2\', \'o3\' } }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-27.ru",
    comment:"",
    query:"INSERT \nDATA { }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-28.ru",
    comment:"",
    query:"INSERT \nDATA {  GRAPH <G>{} }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-29.ru",
    comment:"",
    query:"DELETE DATA { <s> <p> \'o1\', \'o2\', \'o3\' }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-30.ru",
    comment:"",
    query:"DELETE DATA { GRAPH <G> { <s> <p> \'o1\', \'o2\', \'o3\' } }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-31.ru",
    comment:"",
    query:"DELETE DATA { \n  <s1> <p1> <o1>\n  GRAPH <G> { <s> <p1> \'o1\'; <p2> <o2> } \n  GRAPH <G1> { <s> <p1> \'o1\'; <p2> <o2> } \n  <s1> <p1> <o1>\n}",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-32.ru",
    comment:"",
    query:"BASE    <base:>\nPREFIX  :     <http:\/\/example\/>\n\nWITH :g\nDELETE {\n  <s> ?p ?o .\n}\nINSERT {\n  ?s ?p <#o> .\n}\nUSING <base:g1>\nUSING <base:g2>\nUSING NAMED :gn1\nUSING NAMED :gn2\nWHERE\n  { ?s ?p ?o }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-33.ru",
    comment:"",
    query:"PREFIX  :     <http:\/\/example\/>\nWITH :g\nDELETE {\n  <base:s> ?p ?o .\n}\nWHERE\n  { ?s ?p ?o }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-34.ru",
    comment:"",
    query:"PREFIX  :     <http:\/\/example\/>\nWITH :g\nINSERT {\n  <base:s> ?p ?o .\n}\nWHERE\n  { ?s ?p ?o }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-35.ru",
    comment:"",
    query:"DELETE WHERE { ?s ?p ?o }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-36.ru",
    comment:"",
    query:"# Comment\nDELETE \n# Comment\nWHERE \n# Comment\n{ GRAPH <G> { <s> <p> 123 ; <q> 4567.0 . } }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-37.ru",
    comment:"",
    query:"CREATE GRAPH <g> ;\nLOAD <remote> INTO GRAPH <g> ;",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-38.ru",
    comment:"",
    query:"# Empty",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-39.ru",
    comment:"",
    query:"BASE <http:\/\/example\/>\n# Otherwise empty",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-40.ru",
    comment:"",
    query:"PREFIX : <http:\/\/example\/>\n# Otherwise empty",
    expected:true
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-01.ru",
    comment:"",
    query:"# No URL\nLOAD ;",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-02.ru",
    comment:"",
    query:"# Typo in keyword.\nCREATE DEAFULT",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-03.ru",
    comment:"",
    query:"# Variable in data.\nDELETE DATA { ?s <p> <o> }",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-04.ru",
    comment:"",
    query:"# Variable in data.\nINSERT DATA { GRAPH ?g {<s> <p> <o> } }",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-05.ru",
    comment:"",
    query:"# Nested GRAPH\nDELETE DATA { \n  GRAPH <G> { \n    <s> <p> <o> .\n    GRAPH <G1> { <s> <p1> \'o1\' }\n  }\n}",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-06.ru",
    comment:"",
    query:"# Missing template\nINSERT WHERE { ?s ?p ?o }",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-07.ru",
    comment:"",
    query:"# No separator\nCREATE GRAPH <g>\nLOAD <remote> INTO GRAPH <g>",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-08.ru",
    comment:"",
    query:"# Too many separators\nCREATE GRAPH <g>\n;;\nLOAD <remote> INTO GRAPH <g>",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-09.ru",
    comment:"",
    query:"CREATE GRAPH <g>\n;\nLOAD <remote> INTO GRAPH <g>\n;;",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-10.ru",
    comment:"",
    query:"# BNode in DELETE WHERE\nDELETE WHERE { _:a <p> <o> }",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-11.ru",
    comment:"",
    query:"# BNode in DELETE template\nDELETE { <s> <p> [] } WHERE { ?x <p> <o> }",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-bad-12.ru",
    comment:"",
    query:"# BNode in DELETE DATA\nDELETE DATA { _:a <p> <o> }",
    expected:false
  },
  { name:"data-sparql11/syntax-update-1/syntax-update-53.ru",
    comment:"",
    query:"PREFIX : <http:\/\/www.example.org\/>\n\nINSERT DATA { \n              GRAPH<g1> { _:b1 :p :o } \n              GRAPH<g2> { _:b1 :p :o } \n            }",
    expected:true
  },
  { name:"data-sparql11/syntax-update-2/large-request-01.ru",
    comment:"",
    query:"BASE <http:\/\/foo.com\/>\nINSERT DATA {\n  GRAPH <http:\/\/example.com\/data> {\n    <a> <b> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,\n            10, 11, 12, 13, 14, 15, 16, 17, 18, 19,\n            20, 21, 22, 23, 24, 25, 26, 27, 28, 29 .\n    <c> <d> \"000\"; <d> \"001\"; <d> \"002\"; <d> \"003\"; <d> \"004\"; <d> \"005\"; <d> \"006\"; <d> \"007\"; <d> \"008\"; <d> \"009\";\n        <d> \"010\"; <d> \"011\"; <d> \"012\"; <d> \"013\"; <d> \"014\"; <d> \"015\"; <d> \"016\"; <d> \"017\"; <d> \"018\"; <d> \"019\";\n        <d> \"020\"; <d> \"021\"; <d> \"022\"; <d> \"023\"; <d> \"024\"; <d> \"025\"; <d> \"026\"; <d> \"027\"; <d> \"028\"; <d> \"029\";\n        <d> \"030\"; <d> \"031\"; <d> \"032\"; <d> \"033\"; <d> \"034\"; <d> \"035\"; <d> \"036\"; <d> \"037\"; <d> \"038\"; <d> \"039\";\n        <d> \"040\"; <d> \"041\"; <d> \"042\"; <d> \"043\"; <d> \"044\"; <d> \"045\"; <d> \"046\"; <d> \"047\"; <d> \"048\"; <d> \"049\";\n        <d> \"050\"; <d> \"051\"; <d> \"052\"; <d> \"053\"; <d> \"054\"; <d> \"055\"; <d> \"056\"; <d> \"057\"; <d> \"058\"; <d> \"059\";\n        <d> \"060\"; <d> \"061\"; <d> \"062\"; <d> \"063\"; <d> \"064\"; <d> \"065\"; <d> \"066\"; <d> \"067\"; <d> \"068\"; <d> \"069\";\n        <d> \"070\"; <d> \"071\"; <d> \"072\"; <d> \"073\"; <d> \"074\"; <d> \"075\"; <d> \"076\"; <d> \"077\"; <d> \"078\"; <d> \"079\";\n        <d> \"080\"; <d> \"081\"; <d> \"082\"; <d> \"083\"; <d> \"084\"; <d> \"085\"; <d> \"086\"; <d> \"087\"; <d> \"088\"; <d> \"089\";\n        <d> \"090\"; <d> \"091\"; <d> \"092\"; <d> \"093\"; <d> \"094\"; <d> \"095\"; <d> \"096\"; <d> \"097\"; <d> \"098\"; <d> \"099\";\n        <d> \"100\"; <d> \"101\"; <d> \"102\"; <d> \"103\"; <d> \"104\"; <d> \"105\"; <d> \"106\"; <d> \"107\"; <d> \"108\"; <d> \"109\";\n        <d> \"110\"; <d> \"111\"; <d> \"112\"; <d> \"113\"; <d> \"114\"; <d> \"115\"; <d> \"116\"; <d> \"117\"; <d> \"118\"; <d> \"119\";\n        <d> \"120\"; <d> \"121\"; <d> \"122\"; <d> \"123\"; <d> \"124\"; <d> \"125\"; <d> \"126\"; <d> \"127\"; <d> \"128\"; <d> \"129\";\n        <d> \"130\"; <d> \"131\"; <d> \"132\"; <d> \"133\"; <d> \"134\"; <d> \"135\"; <d> \"136\"; <d> \"137\"; <d> \"138\"; <d> \"139\";\n        <d> \"140\"; <d> \"141\"; <d> \"142\"; <d> \"143\"; <d> \"144\"; <d> \"145\"; <d> \"146\"; <d> \"147\"; <d> \"148\"; <d> \"149\";\n        <d> \"150\"; <d> \"151\"; <d> \"152\"; <d> \"153\"; <d> \"154\"; <d> \"155\"; <d> \"156\"; <d> \"157\"; <d> \"158\"; <d> \"159\";\n        <d> \"160\"; <d> \"161\"; <d> \"162\"; <d> \"163\"; <d> \"164\"; <d> \"165\"; <d> \"166\"; <d> \"167\"; <d> \"168\"; <d> \"169\";\n        <d> \"170\"; <d> \"171\"; <d> \"172\"; <d> \"173\"; <d> \"174\"; <d> \"175\"; <d> \"176\"; <d> \"177\"; <d> \"178\"; <d> \"179\";\n        <d> \"180\"; <d> \"181\"; <d> \"182\"; <d> \"183\"; <d> \"184\"; <d> \"185\"; <d> \"186\"; <d> \"187\"; <d> \"188\"; <d> \"189\";\n        <d> \"190\"; <d> \"191\"; <d> \"192\"; <d> \"193\"; <d> \"194\"; <d> \"195\"; <d> \"196\"; <d> \"197\"; <d> \"198\"; <d> \"199\";\n        <d> \"200\"; <d> \"201\"; <d> \"202\"; <d> \"203\"; <d> \"204\"; <d> \"205\"; <d> \"206\"; <d> \"207\"; <d> \"208\"; <d> \"209\";\n        <d> \"210\"; <d> \"211\"; <d> \"212\"; <d> \"213\"; <d> \"214\"; <d> \"215\"; <d> \"216\"; <d> \"217\"; <d> \"218\"; <d> \"219\";\n        <d> \"220\"; <d> \"221\"; <d> \"222\"; <d> \"223\"; <d> \"224\"; <d> \"225\"; <d> \"226\"; <d> \"227\"; <d> \"228\"; <d> \"229\";\n        <d> \"230\"; <d> \"231\"; <d> \"232\"; <d> \"233\"; <d> \"234\"; <d> \"235\"; <d> \"236\"; <d> \"237\"; <d> \"238\"; <d> \"239\";\n        <d> \"240\"; <d> \"241\"; <d> \"242\"; <d> \"243\"; <d> \"244\"; <d> \"245\"; <d> \"246\"; <d> \"247\"; <d> \"248\"; <d> \"249\";\n        <d> \"250\"; <d> \"251\"; <d> \"252\"; <d> \"253\"; <d> \"254\"; <d> \"255\"; <d> \"256\"; <d> \"257\"; <d> \"258\"; <d> \"259\";\n        <d> \"260\"; <d> \"261\"; <d> \"262\"; <d> \"263\"; <d> \"264\"; <d> \"265\"; <d> \"266\"; <d> \"267\"; <d> \"268\"; <d> \"269\";\n        <d> \"270\"; <d> \"271\"; <d> \"272\"; <d> \"273\"; <d> \"274\"; <d> \"275\"; <d> \"276\"; <d> \"277\"; <d> \"278\"; <d> \"279\";\n        <d> \"280\"; <d> \"281\"; <d> \"282\"; <d> \"283\"; <d> \"284\"; <d> \"285\"; <d> \"286\"; <d> \"287\"; <d> \"288\"; <d> \"289\";\n        <d> \"290\"; <d> \"291\"; <d> \"292\"; <d> \"293\"; <d> \"294\"; <d> \"295\"; <d> \"296\"; <d> \"297\"; <d> \"298\"; <d> \"299\".\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> . <e> <f> <g> .\n    <e> <f> <g000> . <e> <f> <g001> . <e> <f> <g002> . <e> <f> <g003> . <e> <f> <g004> .\n    <e> <f> <g005> . <e> <f> <g006> . <e> <f> <g007> . <e> <f> <g008> . <e> <f> <g009> .\n    <e> <f> <g010> . <e> <f> <g011> . <e> <f> <g012> . <e> <f> <g013> . <e> <f> <g014> .\n    <e> <f> <g015> . <e> <f> <g016> . <e> <f> <g017> . <e> <f> <g018> . <e> <f> <g019> .\n    <e> <f> <g020> . <e> <f> <g021> . <e> <f> <g022> . <e> <f> <g023> . <e> <f> <g024> .\n    <e> <f> <g025> . <e> <f> <g026> . <e> <f> <g027> . <e> <f> <g028> . <e> <f> <g029> .\n    <e> <f> <g030> . <e> <f> <g031> . <e> <f> <g032> . <e> <f> <g033> . <e> <f> <g034> .\n    <e> <f> <g035> . <e> <f> <g036> . <e> <f> <g037> . <e> <f> <g038> . <e> <f> <g039> .\n    <e> <f> <g040> . <e> <f> <g041> . <e> <f> <g042> . <e> <f> <g043> . <e> <f> <g044> .\n    <e> <f> <g045> . <e> <f> <g046> . <e> <f> <g047> . <e> <f> <g048> . <e> <f> <g049> .\n    <e> <f> <g050> . <e> <f> <g051> . <e> <f> <g052> . <e> <f> <g053> . <e> <f> <g054> .\n    <e> <f> <g055> . <e> <f> <g056> . <e> <f> <g057> . <e> <f> <g058> . <e> <f> <g059> .\n    <e> <f> <g060> . <e> <f> <g061> . <e> <f> <g062> . <e> <f> <g063> . <e> <f> <g064> .\n    <e> <f> <g065> . <e> <f> <g066> . <e> <f> <g067> . <e> <f> <g068> . <e> <f> <g069> .\n    <e> <f> <g070> . <e> <f> <g071> . <e> <f> <g072> . <e> <f> <g073> . <e> <f> <g074> .\n    <e> <f> <g075> . <e> <f> <g076> . <e> <f> <g077> . <e> <f> <g078> . <e> <f> <g079> .\n    <e> <f> <g080> . <e> <f> <g081> . <e> <f> <g082> . <e> <f> <g083> . <e> <f> <g084> .\n    <e> <f> <g085> . <e> <f> <g086> . <e> <f> <g087> . <e> <f> <g088> . <e> <f> <g089> .\n    <e> <f> <g090> . <e> <f> <g091> . <e> <f> <g092> . <e> <f> <g093> . <e> <f> <g094> .\n    <e> <f> <g095> . <e> <f> <g096> . <e> <f> <g097> . <e> <f> <g098> . <e> <f> <g099> .\n    <e> <f> <g100> . <e> <f> <g101> . <e> <f> <g102> . <e> <f> <g103> . <e> <f> <g104> .\n    <e> <f> <g105> . <e> <f> <g106> . <e> <f> <g107> . <e> <f> <g108> . <e> <f> <g109> .\n    <e> <f> <g110> . <e> <f> <g111> . <e> <f> <g112> . <e> <f> <g113> . <e> <f> <g114> .\n    <e> <f> <g115> . <e> <f> <g116> . <e> <f> <g117> . <e> <f> <g118> . <e> <f> <g119> .\n    <e> <f> <g120> . <e> <f> <g121> . <e> <f> <g122> . <e> <f> <g123> . <e> <f> <g124> .\n    <e> <f> <g125> . <e> <f> <g126> . <e> <f> <g127> . <e> <f> <g128> . <e> <f> <g129> .\n    <e> <f> <g130> . <e> <f> <g131> . <e> <f> <g132> . <e> <f> <g133> . <e> <f> <g134> .\n    <e> <f> <g135> . <e> <f> <g136> . <e> <f> <g137> . <e> <f> <g138> . <e> <f> <g139> .\n    <e> <f> <g140> . <e> <f> <g141> . <e> <f> <g142> . <e> <f> <g143> . <e> <f> <g144> .\n    <e> <f> <g145> . <e> <f> <g146> . <e> <f> <g147> . <e> <f> <g148> . <e> <f> <g149> .\n    <e> <f> <g150> . <e> <f> <g151> . <e> <f> <g152> . <e> <f> <g153> . <e> <f> <g154> .\n    <e> <f> <g155> . <e> <f> <g156> . <e> <f> <g157> . <e> <f> <g158> . <e> <f> <g159> .\n    <e> <f> <g160> . <e> <f> <g161> . <e> <f> <g162> . <e> <f> <g163> . <e> <f> <g164> .\n    <e> <f> <g165> . <e> <f> <g166> . <e> <f> <g167> . <e> <f> <g168> . <e> <f> <g169> .\n    <e> <f> <g170> . <e> <f> <g171> . <e> <f> <g172> . <e> <f> <g173> . <e> <f> <g174> .\n    <e> <f> <g175> . <e> <f> <g176> . <e> <f> <g177> . <e> <f> <g178> . <e> <f> <g179> .\n    <e> <f> <g180> . <e> <f> <g181> . <e> <f> <g182> . <e> <f> <g183> . <e> <f> <g184> .\n    <e> <f> <g185> . <e> <f> <g186> . <e> <f> <g187> . <e> <f> <g188> . <e> <f> <g189> .\n    <e> <f> <g190> . <e> <f> <g191> . <e> <f> <g192> . <e> <f> <g193> . <e> <f> <g194> .\n    <e> <f> <g195> . <e> <f> <g196> . <e> <f> <g197> . <e> <f> <g198> . <e> <f> <g199> .\n    <e> <f> <g200> . <e> <f> <g201> . <e> <f> <g202> . <e> <f> <g203> . <e> <f> <g204> .\n    <e> <f> <g205> . <e> <f> <g206> . <e> <f> <g207> . <e> <f> <g208> . <e> <f> <g209> .\n    <e> <f> <g210> . <e> <f> <g211> . <e> <f> <g212> . <e> <f> <g213> . <e> <f> <g214> .\n    <e> <f> <g215> . <e> <f> <g216> . <e> <f> <g217> . <e> <f> <g218> . <e> <f> <g219> .\n    <e> <f> <g220> . <e> <f> <g221> . <e> <f> <g222> . <e> <f> <g223> . <e> <f> <g224> .\n    <e> <f> <g225> . <e> <f> <g226> . <e> <f> <g227> . <e> <f> <g228> . <e> <f> <g229> .\n    <e> <f> <g230> . <e> <f> <g231> . <e> <f> <g232> . <e> <f> <g233> . <e> <f> <g234> .\n    <e> <f> <g235> . <e> <f> <g236> . <e> <f> <g237> . <e> <f> <g238> . <e> <f> <g239> .\n    <e> <f> <g240> . <e> <f> <g241> . <e> <f> <g242> . <e> <f> <g243> . <e> <f> <g244> .\n    <e> <f> <g245> . <e> <f> <g246> . <e> <f> <g247> . <e> <f> <g248> . <e> <f> <g249> .\n    <e> <f> <g250> . <e> <f> <g251> . <e> <f> <g252> . <e> <f> <g253> . <e> <f> <g254> .\n    <e> <f> <g255> . <e> <f> <g256> . <e> <f> <g257> . <e> <f> <g258> . <e> <f> <g259> .\n    <e> <f> <g260> . <e> <f> <g261> . <e> <f> <g262> . <e> <f> <g263> . <e> <f> <g264> .\n    <e> <f> <g265> . <e> <f> <g266> . <e> <f> <g267> . <e> <f> <g268> . <e> <f> <g269> .\n    <e> <f> <g270> . <e> <f> <g271> . <e> <f> <g272> . <e> <f> <g273> . <e> <f> <g274> .\n    <e> <f> <g275> . <e> <f> <g276> . <e> <f> <g277> . <e> <f> <g278> . <e> <f> <g279> .\n    <e> <f> <g280> . <e> <f> <g281> . <e> <f> <g282> . <e> <f> <g283> . <e> <f> <g284> .\n    <e> <f> <g285> . <e> <f> <g286> . <e> <f> <g287> . <e> <f> <g288> . <e> <f> <g289> .\n    <e> <f> <g290> . <e> <f> <g291> . <e> <f> <g292> . <e> <f> <g293> . <e> <f> <g294> .\n    <e> <f> <g295> . <e> <f> <g296> . <e> <f> <g297> . <e> <f> <g298> . <e> <f> <g299> .\n    <e> <f> <g300> . <e> <f> <g301> . <e> <f> <g302> . <e> <f> <g303> . <e> <f> <g304> .\n    <e> <f> <g305> . <e> <f> <g306> . <e> <f> <g307> . <e> <f> <g308> . <e> <f> <g309> .\n    <e> <f> <g310> . <e> <f> <g311> . <e> <f> <g312> . <e> <f> <g313> . <e> <f> <g314> .\n    <e> <f> <g315> . <e> <f> <g316> . <e> <f> <g317> . <e> <f> <g318> . <e> <f> <g319> .\n    <e> <f> <g320> . <e> <f> <g321> . <e> <f> <g322> . <e> <f> <g323> . <e> <f> <g324> .\n    <e> <f> <g325> . <e> <f> <g326> . <e> <f> <g327> . <e> <f> <g328> . <e> <f> <g329> .\n    <e> <f> <g330> . <e> <f> <g331> . <e> <f> <g332> . <e> <f> <g333> . <e> <f> <g334> .\n    <e> <f> <g335> . <e> <f> <g336> . <e> <f> <g337> . <e> <f> <g338> . <e> <f> <g339> .\n    <e> <f> <g340> . <e> <f> <g341> . <e> <f> <g342> . <e> <f> <g343> . <e> <f> <g344> .\n    <e> <f> <g345> . <e> <f> <g346> . <e> <f> <g347> . <e> <f> <g348> . <e> <f> <g349> .\n    <e> <f> <g350> . <e> <f> <g351> . <e> <f> <g352> . <e> <f> <g353> . <e> <f> <g354> .\n    <e> <f> <g355> . <e> <f> <g356> . <e> <f> <g357> . <e> <f> <g358> . <e> <f> <g359> .\n    <e> <f> <g360> . <e> <f> <g361> . <e> <f> <g362> . <e> <f> <g363> . <e> <f> <g364> .\n    <e> <f> <g365> . <e> <f> <g366> . <e> <f> <g367> . <e> <f> <g368> . <e> <f> <g369> .\n    <e> <f> <g370> . <e> <f> <g371> . <e> <f> <g372> . <e> <f> <g373> . <e> <f> <g374> .\n    <e> <f> <g375> . <e> <f> <g376> . <e> <f> <g377> . <e> <f> <g378> . <e> <f> <g379> .\n    <e> <f> <g380> . <e> <f> <g381> . <e> <f> <g382> . <e> <f> <g383> . <e> <f> <g384> .\n    <e> <f> <g385> . <e> <f> <g386> . <e> <f> <g387> . <e> <f> <g388> . <e> <f> <g389> .\n    <e> <f> <g390> . <e> <f> <g391> . <e> <f> <g392> . <e> <f> <g393> . <e> <f> <g394> .\n    <e> <f> <g395> . <e> <f> <g396> . <e> <f> <g397> . <e> <f> <g398> . <e> <f> <g399> .\n  }\n}",
    expected:true
  },
  { name:"data-sparql11/update-silent/load-silent.ru",
    comment:"Loading a non-existent graph",
    query:"LOAD SILENT <somescheme:\/\/www.example.com\/THIS-GRAPH-DOES-NOT-EXIST\/>",
    expected:true
  },
  { name:"data-sparql11/update-silent/load-silent-into.ru",
    comment:"Loading a non-existent named graph",
    query:"LOAD SILENT <somescheme:\/\/www.example.com\/THIS-GRAPH-DOES-NOT-EXIST\/> INTO GRAPH <http:\/\/www.example.org\/>",
    expected:true
  },
  { name:"data-sparql11/update-silent/clear-silent.ru",
    comment:"Clearing a non-existent named graph",
    query:"CLEAR SILENT GRAPH <http:\/\/www.example.org>",
    expected:true
  },
  { name:"data-sparql11/update-silent/clear-default-silent.ru",
    comment:"Clearing the already empty default graph. (This operation would also succeed without SILENT)",
    query:"CLEAR DEFAULT",
    expected:true
  },
  { name:"data-sparql11/update-silent/create-silent.ru",
    comment:"Creation of an already existent named graph",
    query:"CREATE SILENT GRAPH <http:\/\/example.org\/g1>",
    expected:true
  },
  { name:"data-sparql11/update-silent/drop-silent.ru",
    comment:"Clearing a non-existent named graph",
    query:"DROP SILENT GRAPH <http:\/\/www.example.org>",
    expected:true
  },
  { name:"data-sparql11/update-silent/drop-default-silent.ru",
    comment:"Clearing the already empty default graph. (This operation would also succeed withou SILENT)",
    query:"DROP DEFAULT",
    expected:true
  },
  { name:"data-sparql11/update-silent/copy-silent.ru",
    comment:"copy a non-existent graph",
    query:"COPY SILENT GRAPH <http:\/\/www.example.com\/g1> TO GRAPH <http:\/\/www.example.com\/g2>",
    expected:true
  },
  { name:"data-sparql11/update-silent/copy-to-default-silent.ru",
    comment:"copy a non-existent graph to default graph",
    query:"COPY SILENT GRAPH <http:\/\/www.example.com\/g1> TO DEFAULT",
    expected:true
  },
  { name:"data-sparql11/update-silent/move-silent.ru",
    comment:"move a non-existent graph",
    query:"MOVE SILENT GRAPH <http:\/\/www.example.com\/g1> TO GRAPH <http:\/\/www.example.com\/g2>",
    expected:true
  },
  { name:"data-sparql11/update-silent/move-to-default-silent.ru",
    comment:"move a non-existent graph to default graph",
    query:"MOVE SILENT GRAPH <http:\/\/www.example.com\/g1> TO DEFAULT",
    expected:true
  },
  { name:"data-sparql11/update-silent/add-silent.ru",
    comment:"add a non-existent graph",
    query:"ADD SILENT GRAPH <http:\/\/www.example.com\/g1> TO GRAPH <http:\/\/www.example.com\/g2>",
    expected:true
  },
  { name:"data-sparql11/update-silent/add-to-default-silent.ru",
    comment:"add a non-existent graph to default graph",
    query:"ADD SILENT GRAPH <http:\/\/www.example.com\/g1> TO DEFAULT",
    expected:true
  }
]
