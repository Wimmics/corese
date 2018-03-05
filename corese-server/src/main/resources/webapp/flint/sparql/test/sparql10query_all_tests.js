var sparql10query_all_tests=[
  { name:"syntax-sparql1/syntax-basic-01.rq",
    comment:"",
    query:"SELECT *\nWHERE { }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-basic-02.rq",
    comment:"",
    query:"SELECT * {}",
    expected:true
  },
  { name:"syntax-sparql1/syntax-basic-03.rq",
    comment:"",
    query:"# No trailing dot\nPREFIX : <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { ?x ?y ?z }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-basic-04.rq",
    comment:"",
    query:"# With trailing dot\nSELECT *\nWHERE { ?x ?y ?z . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-basic-05.rq",
    comment:"",
    query:"# Two triples : no trailing dot\nSELECT *\nWHERE { ?x ?y ?z . ?a ?b ?c }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-basic-06.rq",
    comment:"",
    query:"# Two triples : with trailing dot\nSELECT *\nWHERE { ?x ?y ?z . ?a ?b ?c . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT *\n{ ?x :p ?z }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { :x :p :z . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { :_1 :p.rdf :z.z . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-04.rq",
    comment:"",
    query:"PREFIX :  <http:\/\/example.org\/ns#> \nPREFIX a: <http:\/\/example.org\/ns2#> \nSELECT *\nWHERE { : a: :a . : : : . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-05.rq",
    comment:"",
    query:"PREFIX :  <> \nSELECT *\nWHERE { : : : . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-06.rq",
    comment:"",
    query:"PREFIX :  <#> \nSELECT *\nWHERE { : : : . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-07.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT *\nWHERE { : : : . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-qname-08.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#>\nPREFIX x.y:  <x#>\nSELECT *\nWHERE { :a.b  x.y:  : . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-01.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"x\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-02.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'x\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-03.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"x\\\"y\'z\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-04.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'x\"y\\\'z\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-05.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"x\\\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-06.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'x\\\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-07.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p 123 }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-08.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p 123. . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-09.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"\"\"Long\n\"\"\nLiteral\n\"\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-10.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'\'\'Long\n\'\' \"\"\"\nLiteral\'\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-11.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"\"\"Long\"\"\\\"Literal\"\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-12.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'\'\'Long\'\'\\\'Literal\'\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-13.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"\"\"Long\\\"\"\"Literal\"\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-14.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'\'\'Long\\\'\'\'Literal\'\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-15.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'\'\'Long \'\' Literal\'\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-16.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'\'\'Long \' Literal\'\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-17.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \'\'\'Long\'\'\\\\Literal with \'\\\\ single quotes \'\'\' }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-18.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"\"\"Long \"\" Literal\"\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-19.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"\"\"Long \" Literal\"\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lit-20.rq",
    comment:"",
    query:"BASE   <http:\/\/example.org\/>\nPREFIX :  <#> \nSELECT * WHERE { :x :p \"\"\"Long\"\"\\\\Literal with \"\\\\ single quotes\"\"\" }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-01.rq",
    comment:"",
    query:"# Operator\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ OPTIONAL { } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-02.rq",
    comment:"",
    query:"# Operator\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ OPTIONAL { :a :b :c } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-03.rq",
    comment:"",
    query:"# Triple, no DOT, operator\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ :p :q :r OPTIONAL { :a :b :c } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-05.rq",
    comment:"",
    query:"# Triple, DOT, operator\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ :p :q :r . OPTIONAL { :a :b :c } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-06.rq",
    comment:"",
    query:"# Triple, DOT, operator, DOT\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ :p :q :r . OPTIONAL { :a :b :c } . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-07.rq",
    comment:"",
    query:"# Operator, no DOT\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ OPTIONAL { :a :b :c } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-08.rq",
    comment:"",
    query:"# Operator, DOT\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ OPTIONAL { :a :b :c } . }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-09.rq",
    comment:"",
    query:"# Operator, triple\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ OPTIONAL { :a :b :c } ?x ?y ?z }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-10.rq",
    comment:"",
    query:"# Operator, DOT triple\nPREFIX :  <http:\/\/example.org\/ns#> \nSELECT *\n{ OPTIONAL { :a :b :c } . ?x ?y ?z }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-11.rq",
    comment:"",
    query:"# Triple, semi, operator\nPREFIX :  <http:\/\/example.org\/ns#>\nSELECT *\n{ :p :q :r ; OPTIONAL { :a :b :c } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-12.rq",
    comment:"",
    query:"# Triple, semi, DOT, operator\nPREFIX :  <http:\/\/example.org\/ns#>\nSELECT *\n{ :p :q :r ; . OPTIONAL { :a :b :c } }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-13.rq",
    comment:"",
    query:"# Two elements in the group\nPREFIX :  <http:\/\/example.org\/ns#>\nSELECT *\n{ :p :q :r . OPTIONAL { :a :b :c } \n  :p :q :r . OPTIONAL { :a :b :c } \n}",
    expected:true
  },
  { name:"syntax-sparql1/syntax-struct-14.rq",
    comment:"",
    query:"# Two elements in the group\nPREFIX :  <http:\/\/example.org\/ns#>\nSELECT *\n{ :p :q :r  OPTIONAL { :a :b :c } \n  :p :q :r  OPTIONAL { :a :b :c } \n}",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lists-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT * WHERE { ( ?x ) :p ?z  }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lists-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT * WHERE { ?x :p ( ?z ) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lists-03.rq",
    comment:"",
    query:"SELECT * WHERE { ( ?z ) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lists-04.rq",
    comment:"",
    query:"SELECT * WHERE { ( ( ?z ) ) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-lists-05.rq",
    comment:"",
    query:"SELECT * WHERE { ( ( ) ) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-bnodes-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { [:p :q ] }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-bnodes-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { [] :p :q }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-bnodes-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { [ ?x ?y ] :p [ ?pa ?b ] }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-bnodes-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { [ :p :q ; ] }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-bnodes-05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { _:a :p1 :q1 .\n        _:a :p2 :q2 .\n      }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-forms-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { ( [ ?x ?y ] ) :p ( [ ?pa ?b ] 57 ) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-forms-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { ( [] [] ) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-union-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT *\n{\n  { ?s ?p ?o } UNION { ?a ?b ?c } \n}",
    expected:true
  },
  { name:"syntax-sparql1/syntax-union-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT *\n{\n  { ?s ?p ?o } UNION { ?a ?b ?c } UNION { ?r ?s ?t }\n}",
    expected:true
  },
  { name:"syntax-sparql1/syntax-expr-01.rq",
    comment:"",
    query:"SELECT *\nWHERE { ?s ?p ?o . FILTER (?o) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-expr-02.rq",
    comment:"",
    query:"SELECT *\nWHERE { ?s ?p ?o . FILTER REGEX(?o, \"foo\") }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-expr-03.rq",
    comment:"",
    query:"SELECT *\nWHERE { ?s ?p ?o . FILTER REGEX(?o, \"foo\", \"i\") }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-expr-04.rq",
    comment:"",
    query:"PREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT *\nWHERE { ?s ?p ?o . FILTER xsd:integer(?o) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-expr-05.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT *\nWHERE { ?s ?p ?o . FILTER :myFunc(?s,?o) }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-01.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY ?o",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-02.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY (?o+5)",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-03.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY ASC(?o)",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-04.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY DESC(?o)",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-05.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY DESC(:func(?s, ?o))",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-06.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY \n  DESC(?o+57) :func2(?o) ASC(?s)",
    expected:true
  },
  { name:"syntax-sparql1/syntax-order-07.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY str(?o)",
    expected:true
  },
  { name:"syntax-sparql1/syntax-limit-offset-01.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY ?o\nLIMIT 5",
    expected:true
  },
  { name:"syntax-sparql1/syntax-limit-offset-02.rq",
    comment:"",
    query:"# LIMIT and OFFSET can be in either order\nPREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY ?o\nLIMIT 5\nOFFSET 3",
    expected:true
  },
  { name:"syntax-sparql1/syntax-limit-offset-03.rq",
    comment:"",
    query:"# LIMIT and OFFSET can be in either order\nPREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY ?o\nOFFSET 3\nLIMIT 5",
    expected:true
  },
  { name:"syntax-sparql1/syntax-limit-offset-04.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example.org\/ns#> \nSELECT *\n{ ?s ?p ?o }\nORDER BY ?o\nOFFSET 3",
    expected:true
  },
  { name:"syntax-sparql1/syntax-pat-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nSELECT *\n{ }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-pat-02.rq",
    comment:"",
    query:"# No DOT after optional\nPREFIX : <http:\/\/example.org\/ns#> \nSELECT *\n{ ?a :b :c OPTIONAL{:x :y :z} :x ?y ?z }",
    expected:true
  },
  { name:"syntax-sparql1/syntax-pat-03.rq",
    comment:"",
    query:"# No DOT between non-triples patterns\nPREFIX : <http:\/\/example.org\/ns#> \nSELECT *\n{ ?a :b :c \n  OPTIONAL{:x :y :z} \n  { :x1 :y1 :z1 } UNION { :x2 :y2 :z2 }\n}",
    expected:true
  },
  { name:"syntax-sparql1/syntax-pat-04.rq",
    comment:"",
    query:"# No DOT between non-triples patterns\nPREFIX : <http:\/\/example.org\/ns#> \nSELECT *\n{\n  OPTIONAL{:x :y :z} \n  ?a :b :c \n  { :x1 :y1 :z1 } UNION { :x2 :y2 :z2 }\n}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-01.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b><c> }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-02.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>_:x }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-03.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-04.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>+11 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-05.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>-1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-06.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>1.0 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-07.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>+1.0 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-08.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>-1.0 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-09.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>1.0e0 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-10.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>+1.0e+1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-11.rq",
    comment:"",
    query:"SELECT * WHERE { <a><b>-1.0e-1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-12.rq",
    comment:"",
    query:"# Legal, if unusual, IRIs\nSELECT * WHERE { <a> <b> <?z> }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-13.rq",
    comment:"",
    query:"# Legal, if unusual, IRIs\nBASE <http:\/\/example\/page.html>\nSELECT * WHERE { <a> <b> <#x> }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-general-14.rq",
    comment:"",
    query:"# Legal, if unusual, IRIs\nBASE <http:\/\/example\/page.html?query>\nSELECT * WHERE { <a> <b> <&param=value> }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-keywords-01.rq",
    comment:"",
    query:"# use keyword FILTER as a namespace prefix\nPREFIX FILTER: <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { ?x FILTER:foo ?z FILTER (?z) }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-keywords-02.rq",
    comment:"",
    query:"# use keyword FILTER as a local name\nPREFIX : <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { ?x :FILTER ?z FILTER (?z) }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-keywords-03.rq",
    comment:"",
    query:"# use keyword UNION as a namespace prefix\nPREFIX UNION: <http:\/\/example.org\/ns#> \nSELECT *\nWHERE { ?x UNION:foo ?z }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-lists-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { () :p 1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-lists-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { ( ) :p 1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-lists-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { ( \n) :p 1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-lists-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { ( 1 2\n) :p 1 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-lists-05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { ( 1 2\n) }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-bnode-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { [] :p [] }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-bnode-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\n# Tab\nSELECT * WHERE { [ ] :p [\n\t] }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-bnode-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT * WHERE { [ :p :q \n ] }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-function-01.rq",
    comment:"",
    query:"PREFIX q: <http:\/\/example.org\/>\nSELECT * WHERE { FILTER (q:name()) }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-function-02.rq",
    comment:"",
    query:"PREFIX q: <http:\/\/example.org\/>\nSELECT * WHERE { FILTER (q:name( )) }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-function-03.rq",
    comment:"",
    query:"PREFIX q: <http:\/\/example.org\/>\nSELECT * WHERE { FILTER (q:name(\n)) }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-function-04.rq",
    comment:"",
    query:"PREFIX q: <http:\/\/example.org\/>\nSELECT * WHERE { FILTER (q:name(1\n)) . FILTER (q:name(1,2)) . FILTER (q:name(1\n,2))}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-select-01.rq",
    comment:"",
    query:"SELECT * WHERE { }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-select-02.rq",
    comment:"",
    query:"SELECT * { }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-ask-02.rq",
    comment:"",
    query:"ASK {}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-construct01.rq",
    comment:"",
    query:"CONSTRUCT { ?s <p1> <o> . ?s <p2> ?o } WHERE {?s ?p ?o}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-construct02.rq",
    comment:"",
    query:"CONSTRUCT { ?s <p1> <o> . ?s <p2> ?o .} WHERE {?s ?p ?o}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-construct03.rq",
    comment:"",
    query:"PREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nCONSTRUCT { [] rdf:subject ?s ;\n               rdf:predicate ?p ;\n               rdf:object ?o }\nWHERE {?s ?p ?o}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-construct04.rq",
    comment:"",
    query:"PREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nCONSTRUCT { [] rdf:subject ?s ;\n               rdf:predicate ?p ;\n               rdf:object ?o . }\nWHERE {?s ?p ?o}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-construct06.rq",
    comment:"",
    query:"CONSTRUCT {} WHERE {}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-describe01.rq",
    comment:"",
    query:"DESCRIBE <u>",
    expected:true
  },
  { name:"syntax-sparql2/syntax-form-describe02.rq",
    comment:"",
    query:"DESCRIBE <u> ?u WHERE { <x> <q> ?u . }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-dataset-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?x\nFROM <http:\/\/example.org\/graph>\nWHERE {}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-dataset-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?x\nFROM NAMED <http:\/\/example.org\/graph1>\nWHERE {}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-dataset-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?x\nFROM NAMED :graph1\nFROM NAMED :graph2\nWHERE {}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-dataset-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?x\nFROM :g1\nFROM :g2\nFROM NAMED :graph1\nFROM NAMED :graph2\nWHERE {}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-graph-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  GRAPH ?g { } \n}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-graph-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  GRAPH :a { } \n}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-graph-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  GRAPH ?g { :x :b ?a } \n}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-graph-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  :x :p :z\n  GRAPH ?g { :x :b ?a } \n}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-graph-05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  :x :p :z\n  GRAPH ?g { :x :b ?a . GRAPH ?g2 { :x :p ?x } }\n}",
    expected:true
  },
  { name:"syntax-sparql2/syntax-esc-01.rq",
    comment:"",
    query:"SELECT *\nWHERE { <x> <p> \"\\t\" }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-esc-02.rq",
    comment:"",
    query:"SELECT *\nWHERE { <x> <p> \"x\\t\" }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-esc-03.rq",
    comment:"",
    query:"SELECT *\nWHERE { <x> <p> \"\\tx\" }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-esc-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/> \nSELECT *\nWHERE { <\\u0078> :\\u0070 ?xx\\u0078 }",
    expected:true
  },
  { name:"syntax-sparql2/syntax-esc-05.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example\/> \nSELECT *\n# Comments can contain \\ u\n# <\\u0078> :\\u0070 ?xx\\u0078\nWHERE { <\\u0078> :\\u0070 ?xx\\u0078 }",
    expected:true
  },
  { name:"syntax-sparql3/syn-01.rq",
    comment:"",
    query:"# Dot after triple\nSELECT * WHERE\n{ ?s ?p ?o . }",
    expected:true
  },
  { name:"syntax-sparql3/syn-02.rq",
    comment:"",
    query:"# No dot after triple\nSELECT * WHERE\n{ ?s ?p ?o }",
    expected:true
  },
  { name:"syntax-sparql3/syn-03.rq",
    comment:"",
    query:"SELECT * WHERE\n{ ?s ?p ?o . ?s ?p ?o . }",
    expected:true
  },
  { name:"syntax-sparql3/syn-04.rq",
    comment:"",
    query:"# No dot\nSELECT * WHERE\n{ ?s ?p ?o . ?s ?p ?o }",
    expected:true
  },
  { name:"syntax-sparql3/syn-05.rq",
    comment:"",
    query:"# DOT after non-triples\nSELECT * WHERE\n{ FILTER (?o>5) . }",
    expected:true
  },
  { name:"syntax-sparql3/syn-06.rq",
    comment:"",
    query:"# DOT after non-triples\nSELECT * WHERE\n{ FILTER (?o>5) . ?s ?p ?o }",
    expected:true
  },
  { name:"syntax-sparql3/syn-07.rq",
    comment:"",
    query:"# Trailing ;\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s :p :o ; FILTER(?x) }",
    expected:true
  },
  { name:"syntax-sparql3/syn-08.rq",
    comment:"",
    query:"# Broken ;\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s :p :o ; . }",
    expected:true
  },
  { name:"syntax-sparql3/syn-bad-01.rq",
    comment:"",
    query:"# More a test that bad syntax tests work!\nPREFIX ex:   <http:\/\/example\/ns#>\nSELECT *",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-02.rq",
    comment:"",
    query:"# Missing DOT, 2 triples\nPREFIX :   <http:\/\/example\/ns#>\nSELECT *\n{ :s1 :p1 :o1 :s2 :p2 :o2 . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-03.rq",
    comment:"",
    query:"# Missing DOT between triples\nPREFIX :   <http:\/\/example\/ns#>\nSELECT *\n{ :s1 :p1 :o1 :s2 :p2 :o2 . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-04.rq",
    comment:"",
    query:"# Missing DOT after ; between triples\nPREFIX :   <http:\/\/example\/ns#>\nSELECT *\n{ :s1 :p1 :o1 ; :s2 :p2 :o2 . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-05.rq",
    comment:"",
    query:"# DOT, no triples\nSELECT * WHERE\n{ . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-06.rq",
    comment:"",
    query:"# DOT, no triples\nSELECT * WHERE\n{ . . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-07.rq",
    comment:"",
    query:"# DOT, then triples\nSELECT * WHERE\n{ . ?s ?p ?o }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-08.rq",
    comment:"",
    query:"# Multiple DOTs\nSELECT * WHERE\n{ ?s ?p ?o . . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-09.rq",
    comment:"",
    query:"# Multiple DOTs\nSELECT * WHERE\n{ ?s ?p ?o .. }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-10.rq",
    comment:"",
    query:"# Multiple DOTs\nSELECT * WHERE\n{ ?s ?p ?o . . ?s1 ?p1 ?o1 }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-11.rq",
    comment:"",
    query:"# Multiple DOTs\nSELECT * WHERE\n{ ?s ?p ?o .. ?s1 ?p1 ?o1 }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-12.rq",
    comment:"",
    query:"# Multiple DOTs\nSELECT * WHERE\n{ ?s ?p ?o . . ?s1 ?p1 ?o1 }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-13.rq",
    comment:"",
    query:"# Multiple DOTs\nSELECT * WHERE\n{ ?s ?p ?o . ?s1 ?p1 ?o1 .. }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-14.rq",
    comment:"",
    query:"# DOT, no triples\nSELECT * WHERE\n{ . FILTER(?x) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-15.rq",
    comment:"",
    query:"# Broken ;\nSELECT * WHERE\n{ ; FILTER(?x) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-16.rq",
    comment:"",
    query:"# Broken ;\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s ; :p :o }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-17.rq",
    comment:"",
    query:"# Broken ;\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s :p ; }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-18.rq",
    comment:"",
    query:"# Broken ;\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s :p ; FILTER(?x) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-19.rq",
    comment:"",
    query:"# Broken ;\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s :p :o . ;  }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-20.rq",
    comment:"",
    query:"# Broken ,\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s , :p :o  }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-21.rq",
    comment:"",
    query:"# Broken ,\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s  :p , :o  }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-22.rq",
    comment:"",
    query:"# Broken ,\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s  :p , }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-23.rq",
    comment:"",
    query:"# Broken , can\'t trail\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s  :p :o , }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-24.rq",
    comment:"",
    query:"# Broken , (should be ;)\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ :s :p1 :o1 , :p2 :o2}",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-25.rq",
    comment:"",
    query:"CONSTRUCT",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-26.rq",
    comment:"",
    query:"# Tokenizing matters.\n# \"longest token rule\" means this isn\'t a \"<\" and \"&&\"\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE\n{ FILTER (?x<?a&&?b>?y) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-27.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { :x [] :q }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-28.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\nSELECT * WHERE { :x _:a :q }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-29.rq",
    comment:"",
    query:"# Syntactic blank node in a filter.\nSELECT * WHERE { <a><b>_:x FILTER(_:x) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-30.rq",
    comment:"",
    query:"# Syntactic blank node in a filter.\nSELECT * WHERE { <a><b>_:x FILTER(_:x < 3) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-31.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  GRAPH [] { } \n}",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-bnode-dot.rq",
    comment:"",
    query:"# NegativeSyntax\/bnode-dot.rq\nSELECT * WHERE {[] . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-bnodes-missing-pvalues-01.rq",
    comment:"",
    query:"# NegativeSyntax\/bnodes-missing-pvalues.rq\nPREFIX :   <http:\/\/example\/ns#>\nSELECT * WHERE { [,] :p [;] . }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-bnodes-missing-pvalues-02.rq",
    comment:"",
    query:"# NegativeSyntax\/bnodes-missing-pvalues-02.rq\nSELECT * WHERE {() . [,] . [,;] }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-empty-optional-01.rq",
    comment:"",
    query:"# NegativeSyntax\/empty-optional.rq\nSELECT * { OPTIONAL FILTER (?x) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-empty-optional-02.rq",
    comment:"",
    query:"# NegativeSyntax\/empty-optional-02.rq\nSELECT * { OPTIONAL GRAPH ?v OPTIONAL FILTER (?x) }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-filter-missing-parens.rq",
    comment:"",
    query:"# NegativeSyntax\/filter-missing-parens.rq\nSELECT * { ?s ?p ?o FILTER ?x }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-lone-list.rq",
    comment:"",
    query:"# NegativeSyntax\/lone-list.rq\nSELECT * WHERE { () }",
    expected:false
  },
  { name:"syntax-sparql3/syn-bad-lone-node.rq",
    comment:"",
    query:"# NegativeSyntax\/lone-node.rq\nSELECT * WHERE {<a>}",
    expected:false
  },
  { name:"syntax-sparql3/syn-blabel-cross-filter.rq",
    comment:"",
    query:"# $Id: syn-blabel-cross-filter.rq,v 1.2 2007\/04\/09 21:40:22 eric Exp $\n# BNode label used across a FILTER.\nPREFIX : <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nASK { _:who :homepage ?homepage \n      FILTER REGEX(?homepage, \"^http:\/\/example.org\/\") \n      _:who :schoolHomepage ?schoolPage }",
    expected:true
  },
  { name:"syntax-sparql3/syn-blabel-cross-graph-bad.rq",
    comment:"",
    query:"# $Id: syn-blabel-cross-graph-bad.rq,v 1.2 2007\/04\/18 23:11:57 eric Exp $\n# BNode label used across a GRAPH.\nPREFIX : <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nASK { _:who :homepage ?homepage \n      GRAPH ?g { ?someone :made ?homepage } \n      _:who :schoolHomepage ?schoolPage }",
    expected:false
  },
  { name:"syntax-sparql3/syn-blabel-cross-optional-bad.rq",
    comment:"",
    query:"# $Id: syn-blabel-cross-optional-bad.rq,v 1.5 2007\/09\/04 15:04:22 eric Exp $\n# BNode label used across an OPTIONAL.\n# This isn\'t necessarily a *syntax* test, but references to bnode labels\n# may not span basic graph patterns.\nPREFIX foaf: \t<http:\/\/xmlns.com\/foaf\/0.1\/>\n\nASK { _:who foaf:homepage ?homepage \n      OPTIONAL { ?someone foaf:made ?homepage } \n      _:who foaf:schoolHomepage ?schoolPage }",
    expected:false
  },
  { name:"syntax-sparql3/syn-blabel-cross-union-bad.rq",
    comment:"",
    query:"# $Id: syn-blabel-cross-union-bad.rq,v 1.4 2007\/09\/04 15:04:09 eric Exp $\n# BNode label used across a UNION.\n# This isn\'t necessarily a *syntax* test, but references to bnode labels\n# may not span basic graph patterns.\nPREFIX foaf: \t<http:\/\/xmlns.com\/foaf\/0.1\/>\n\nASK { _:who foaf:homepage ?homepage \n      { ?someone foaf:made ?homepage }\n      UNION\n      { ?homepage foaf:maker ?someone }\n      _:who foaf:schoolHomepage ?schoolPage }",
    expected:false
  },
  { name:"syntax-sparql4/syn-09.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v .  _:a ?q 1 \n}",
    expected:true
  },
  { name:"syntax-sparql4/syn-10.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  { _:a ?p ?v .  _:a ?q _:a } UNION { _:b ?q _:c }\n}",
    expected:true
  },
  { name:"syntax-sparql4/syn-11.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v .  FILTER(true) . [] ?q _:a\n}",
    expected:true
  },
  { name:"syntax-sparql4/syn-bad-34.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v . { _:a ?q 1 }\n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-35.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  { _:a ?p ?v . } _:a ?q 1 \n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-36.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  { _:a ?p ?v . } UNION { _:a ?q 1 } \n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-37.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  { _:a ?p ?v . } _:a ?q 1 \n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-38.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v . OPTIONAL {_:a ?q 1 }\n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-OPT-breaks-BGP.rq",
    comment:"bad: re-used BNode label after OPTIONAL",
    query:"# bad: re-used BNode label after OPTIONAL\n# $Id: syn-bad-OPT-breaks-BGP.rq,v 1.1 2007\/02\/15 15:14:31 eric Exp $\n\nPREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v . OPTIONAL { ?s ?p ?v } _:a ?q 1\n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-UNION-breaks-BGP.rq",
    comment:"bad: re-used BNode label after UNION",
    query:"# bad: re-used BNode label after UNION\n# $Id: syn-bad-UNION-breaks-BGP.rq,v 1.3 2007\/09\/04 15:03:54 eric Exp $\n# This isn\'t necessarily a *syntax* test, but references to bnode labels\n# may not span basic graph patterns.\n\nPREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v1 { ?s <p1> ?o } UNION { ?s <p2> ?o } _:a ?p ?v2\n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-bad-GRAPH-breaks-BGP.rq",
    comment:"bad: re-used BNode label after GRAPH",
    query:"# bad: re-used BNode label after GRAPH\n# $Id: syn-bad-GRAPH-breaks-BGP.rq,v 1.1 2007\/02\/15 15:14:31 eric Exp $\n\nPREFIX : <http:\/\/example.org\/>\nSELECT *\nWHERE\n{\n  _:a ?p ?v . GRAPH ?g { ?s ?p ?v } _:a ?q 1\n}",
    expected:false
  },
  { name:"syntax-sparql4/syn-leading-digits-in-prefixed-names.rq",
    comment:"",
    query:"PREFIX dob: <http:\/\/placetime.com\/interval\/gregorian\/1977-01-18T04:00:00Z\/P> \nPREFIX time: <http:\/\/www.ai.sri.com\/daml\/ontologies\/time\/Time.daml#>\nPREFIX dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nSELECT ?desc\nWHERE  { \n  dob:1D a time:ProperInterval;\n         dc:description ?desc.\n}",
    expected:true
  },
  { name:"syntax-sparql5/syntax-reduced-01.rq",
    comment:"",
    query:"SELECT REDUCED * WHERE { ?x ?y ?z }",
    expected:true
  },
  { name:"syntax-sparql5/syntax-reduced-02.rq",
    comment:"",
    query:"SELECT REDUCED ?x ?y WHERE { ?x ?y ?z }",
    expected:true
  },
  { name:"basic/base-prefix-1.rq",
    comment:"",
    query:"BASE <http:\/\/example.org\/x\/> \nPREFIX : <>\n\nSELECT * WHERE { :x ?p ?v }",
    expected:true
  },
  { name:"basic/base-prefix-2.rq",
    comment:"",
    query:"BASE <http:\/\/example.org\/x\/> \nPREFIX : <#>\n\nSELECT * WHERE { :x ?p ?v }",
    expected:true
  },
  { name:"basic/base-prefix-3.rq",
    comment:"",
    query:"PREFIX ns: <http:\/\/example.org\/ns#>\nPREFIX x:  <http:\/\/example.org\/x\/>\n\nSELECT * WHERE { x:x ns:p ?v }",
    expected:true
  },
  { name:"basic/base-prefix-4.rq",
    comment:"",
    query:"BASE <http:\/\/example.org\/x\/>\n\nSELECT * WHERE { <x> <p> ?v }",
    expected:true
  },
  { name:"basic/base-prefix-5.rq",
    comment:"",
    query:"BASE <http:\/\/example.org\/x\/>\n\nSELECT * WHERE { <#x> <#p> ?v }",
    expected:true
  },
  { name:"basic/list-1.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?p\n{ :x ?p () . }",
    expected:true
  },
  { name:"basic/list-2.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?p\n{ :x ?p (1) . }",
    expected:true
  },
  { name:"basic/list-3.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?p ?v\n{ :x ?p (?v) . }",
    expected:true
  },
  { name:"basic/list-4.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?p ?v ?w\n{ :x ?p (?v ?w) . }",
    expected:true
  },
  { name:"basic/quotes-1.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?x\n{ ?x ?p \'\'\'x\'\'\' }",
    expected:true
  },
  { name:"basic/quotes-2.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?x\n{ ?x ?p \"\"\"x\"\"\" }",
    expected:true
  },
  { name:"basic/quotes-3.rq",
    comment:"",
    query:"# This query uses UNIX line end conventions.\n# It is in CVS in binary.\nPREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?x\n{ ?x ?p \'\'\'x\ny\'\'\'\n}",
    expected:true
  },
  { name:"basic/quotes-4.rq",
    comment:"",
    query:"# This query uses UNIX line end conventions.\n# It is in CVS in binary.\nPREFIX : <http:\/\/example.org\/ns#>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#> \n\nSELECT ?x\n{ ?x ?p \"\"\"x\ny\"\"\"^^:someType\n}",
    expected:true
  },
  { name:"basic/term-1.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x ?p true . }",
    expected:true
  },
  { name:"basic/term-2.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x ?p false }",
    expected:true
  },
  { name:"basic/term-3.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x a ?C . }",
    expected:true
  },
  { name:"basic/term-4.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x ?p 123.0 }",
    expected:true
  },
  { name:"basic/term-5.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x ?p 123.0. }",
    expected:true
  },
  { name:"basic/term-6.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\n# DOT is part of the decimal.\nSELECT * { :x ?p 456. }",
    expected:true
  },
  { name:"basic/term-7.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\n# DOT is part of the decimal.\nSELECT * { :x ?p 456. . }",
    expected:true
  },
  { name:"basic/term-8.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\n# DOT is part of the decimal.\nSELECT * { :x ?p +5 }",
    expected:true
  },
  { name:"basic/term-9.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\n# DOT is part of the decimal.\nSELECT * { :x ?p -18 }",
    expected:true
  },
  { name:"basic/var-1.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x ?p $v }",
    expected:true
  },
  { name:"basic/var-2.rq",
    comment:"",
    query:"PREFIX :     <http:\/\/example.org\/ns#>\nPREFIX xsd:  <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT * { :x ?p $v . :x ?p ?v }",
    expected:true
  },
  { name:"basic/bgp-no-match.rq",
    comment:"Patterns not in data don\'t match",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?x\nWHERE {\n  ?x foaf:name \"John Smith\" ;\n       a foaf:Womble .\n}",
    expected:true
  },
  { name:"basic/spoo-1.rq",
    comment:"Test the :x :y :o1, :o2 construct",
    query:"PREFIX : <http:\/\/example.org\/ns#> \nPREFIX xsd:        <http:\/\/www.w3.org\/2001\/XMLSchema#> \n\nSELECT ?s WHERE {\n ?s :p1 1, 2 .\n}",
    expected:true
  },
  { name:"basic/prefix-name-1.rq",
    comment:"No local name - foo:",
    query:"PREFIX ex: <http:\/\/example.org\/ns#x> \nSELECT ?p {\n  ex: ?p 1 .\n}",
    expected:true
  },
  { name:"triple-match/dawg-tp-01.rq",
    comment:"Simple triple match",
    query:"PREFIX : <http:\/\/example.org\/data\/>\n\nSELECT *\nWHERE { :x ?p ?q . }",
    expected:true
  },
  { name:"triple-match/dawg-tp-02.rq",
    comment:"Simple triple match",
    query:"PREFIX : <http:\/\/example.org\/data\/>\n\nSELECT *\nWHERE { ?x :p ?q . }",
    expected:true
  },
  { name:"triple-match/dawg-tp-03.rq",
    comment:"Simple triple match - repeated variable",
    query:"SELECT *\nWHERE { ?a ?a ?b . }",
    expected:true
  },
  { name:"triple-match/dawg-tp-04.rq",
    comment:"Simple triple match - two triples, common variable",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX  foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT ?name\nWHERE {\n  ?x rdf:type foaf:Person .\n  ?x foaf:name ?name .\n}",
    expected:true
  },
  { name:"open-world/open-eq-01.rq",
    comment:"graph match - no lexical form in data (assumes no value matching)",
    query:"# SPARQL is defined over simple entailment so\n# only syntactic matches show.  \n# (Some systems may match because they do\n# value-based matching in the graph (D-entailment))\n\n# Does not strictly match \"1\"^xsd:integer\n\nPREFIX  :       <http:\/\/example\/ns#>\nPREFIX  t:      <http:\/\/example\/t#>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{ ?x :p \"001\"^^xsd:integer }",
    expected:true
  },
  { name:"open-world/open-eq-02.rq",
    comment:"graph match - unknown type",
    query:"# Test matching in a graph pattern\n# Unknown type\n\nPREFIX  :       <http:\/\/example\/ns#>\nPREFIX  t:      <http:\/\/example\/t#>\n\nSELECT *\n{ ?x :p \"a\"^^t:type1 }",
    expected:true
  },
  { name:"open-world/open-eq-03.rq",
    comment:"Filter(?v=1)",
    query:"# SPARQL FILTER test by value.\n# A processor knows about XSD integer\n# so 1 and 01 pass the filter\n\nPREFIX  :       <http:\/\/example\/ns#>\nPREFIX  t:      <http:\/\/example\/t#>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX  rdfs:   <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{ ?x :p ?v \n  FILTER ( ?v = 1 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-04.rq",
    comment:"Filter(?v!=1)",
    query:"# SPARQL FILTER test by value.\n# A processor knows about XSD integer\n# so 1 and 01 are excluded by the filter\n\nPREFIX  :       <http:\/\/example\/ns#>\nPREFIX  t:      <http:\/\/example\/t#>\nPREFIX  rdf:    <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX  rdfs:   <http:\/\/www.w3.org\/2000\/01\/rdf-schema#>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{ ?x :p ?v \n  FILTER ( ?v != 1 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-05.rq",
    comment:"FILTER(?v = unknown type)",
    query:"# SPARQL FILTER test by value.\n# Only one valus is known to be \"a\"^^t:type1\n# (others maybe but the processor does not positively know this)\n\nPREFIX  :       <http:\/\/example\/ns#>\nPREFIX  t:      <http:\/\/example\/t#>\n\nSELECT *\n{ ?x :p ?v \n  FILTER ( ?v = \"a\"^^t:type1 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-06.rq",
    comment:"FILTER(?v != unknown type)",
    query:"# SPARQL FILTER test by value for known types.\n# Nothing is known to be not the same value as  \"a\"^^t:type1\n#  \"b\"^^t:type1 might be a different lexical form for the same value\n#  \"a\"^^t:type2 might have overlapping value spaces for this lexicial form.\n\nPREFIX  :       <http:\/\/example\/ns#>\nPREFIX  t:      <http:\/\/example\/t#>\n\nSELECT *\n{ ?x :p ?v \n  FILTER ( ?v != \"a\"^^t:type1 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-07.rq",
    comment:"Test of \'=\' ",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER ( ?v1 = ?v2 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-08.rq",
    comment:"Test of \'!=\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER ( ?v1 != ?v2 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-09.rq",
    comment:"Test of \'=\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x :p ?v1 .\n    ?y :q ?v2 .\n    FILTER ( ?v1 = ?v2 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-10.rq",
    comment:"Test of \'!=\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x :p ?v1 .\n    ?y :q ?v2 .\n    FILTER ( ?v1 != ?v2 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-11.rq",
    comment:"test of \'=\' || \'!=\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x :p ?v1 .\n    ?y :q ?v2 .\n    FILTER ( ?v1 != ?v2 || ?v1 = ?v2 )\n}",
    expected:true
  },
  { name:"open-world/open-eq-12.rq",
    comment:"find pairs that don\'t value-compare",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x ?v1 ?y ?v2\n{\n    ?x :p ?v1 .\n    ?y :p ?v2 .\n    OPTIONAL { ?y :p ?v3 . FILTER( ?v1 != ?v3 || ?v1 = ?v3 )}\n    FILTER (!bound(?v3))\n}",
    expected:true
  },
  { name:"open-world/date-1.rq",
    comment:"Added type : xsd:date \'=\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x :r ?v .\n    FILTER ( ?v = \"2006-08-23\"^^xsd:date )\n}",
    expected:true
  },
  { name:"open-world/date-2.rq",
    comment:"Added type : xsd:date \'!=\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x :r ?v .\n    FILTER ( ?v != \"2006-08-23\"^^xsd:date )\n}",
    expected:true
  },
  { name:"open-world/date-3.rq",
    comment:"Added type : xsd:date \'>\'",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x :r ?v .\n    FILTER ( ?v > \"2006-08-22\"^^xsd:date )\n}",
    expected:true
  },
  { name:"open-world/date-4.rq",
    comment:"xsd:date ORDER BY",
    query:"PREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x ?date\n{\n    ?x :s ?date .\n    FILTER ( datatype(?date) = xsd:date )\n}",
    expected:true
  },
  { name:"open-world/open-cmp-01.rq",
    comment:"Find things that compare with < or >",
    query:"PREFIX      :    <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x ?v1 ?v2\n{\n    ?x :p [ :v1 ?v1 ; :v2 ?v2 ] .\n    FILTER ( ?v1 < ?v2 || ?v1 > ?v2 )\n}",
    expected:true
  },
  { name:"open-world/open-cmp-02.rq",
    comment:"Find things that compare with <= and >",
    query:"PREFIX      :    <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x ?v1 ?v2\n{\n    ?x :p [ :v1 ?v1 ; :v2 ?v2 ] .\n    FILTER ( ?v1 < ?v2 || ?v1 = ?v2 || ?v1 > ?v2 )\n}",
    expected:true
  },
  { name:"algebra/two-nested-opt.rq",
    comment:"Nested-optionals with a shared variable that does not appear in the middle pattern (a not well-formed query pattern as per \"Semantics and Complexity\" of SPARQL",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT *\n{ \n    :x1 :p ?v .\n    OPTIONAL\n    {\n      :x3 :q ?w .\n      OPTIONAL { :x2 :p ?v }\n    }\n}",
    expected:true
  },
  { name:"algebra/two-nested-opt-alt.rq",
    comment:"OPTIONALs parse in a left-associative manner",
    query:"PREFIX :    <http:\/\/example\/>\n\n## The nested optional example, rewritten to a form that is the same\n## for the SPARQL algebra and the declarative semantics.\nSELECT *\n{ \n    :x1 :p ?v .\n    OPTIONAL { :x3 :q ?w }\n    OPTIONAL { :x3 :q ?w  . :x2 :p ?v }\n}",
    expected:true
  },
  { name:"algebra/opt-filter-1.rq",
    comment:"A FILTER inside an OPTIONAL can reference a variable bound in the required part of the OPTIONAL",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT *\n{ \n  ?x :p ?v .\n  OPTIONAL\n  { \n    ?y :q ?w .\n    FILTER(?v=2)\n  }\n}",
    expected:true
  },
  { name:"algebra/opt-filter-2.rq",
    comment:"FILTERs inside an OPTIONAL can refer to variables from both the required and optional parts of the construct.",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT *\n{ \n  ?x :p ?v .\n  OPTIONAL\n  { \n    ?y :q ?w .\n    FILTER(?v=2)\n    FILTER(?w=3)\n  }\n}",
    expected:true
  },
  { name:"algebra/opt-filter-3.rq",
    comment:"FILTERs in an OPTIONAL do not extend to variables bound outside of the LeftJoin(...) operation",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT *\n{ \n    :x :p ?v . \n    { :x :q ?w \n      # ?v is not in scope so ?v2 never set\n      OPTIONAL {  :x :p ?v2 FILTER(?v = 1) }\n    }\n}",
    expected:true
  },
  { name:"algebra/filter-placement-1.rq",
    comment:"FILTER placed after the triple pattern that contains the variable tested",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?v \n{ \n    ?s :p ?v . \n    FILTER (?v = 2)\n}",
    expected:true
  },
  { name:"algebra/filter-placement-2.rq",
    comment:"FILTERs are scoped to the nearest enclosing group - placement within that group does not matter",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?v \n{ \n    FILTER (?v = 2)\n    ?s :p ?v . \n}",
    expected:true
  },
  { name:"algebra/filter-placement-3.rq",
    comment:"FILTERs are scoped to the nearest enclosing group - placement within that group does not matter",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT ?v ?w\n{ \n    FILTER (?v = 2)\n    FILTER (?w = 3)\n    ?s :p ?v . \n    ?s :q ?w .\n}",
    expected:true
  },
  { name:"algebra/filter-nested-1.rq",
    comment:"A FILTER is in scope for variables bound at the same level of the query tree",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT ?v\n{ :x :p ?v . FILTER(?v = 1) }",
    expected:true
  },
  { name:"algebra/filter-nested-2.rq",
    comment:"A FILTER in a group { ... } cannot see variables bound outside that group",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT ?v\n{ :x :p ?v . { FILTER(?v = 1) } }",
    expected:true
  },
  { name:"algebra/filter-scope-1.rq",
    comment:"FILTERs in an OPTIONAL do not extend to variables bound outside of the LeftJoin(...) operation",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT *\n{ \n    :x :p ?v . \n    { :x :q ?w \n      OPTIONAL {  :x :p ?v2 FILTER(?v = 1) }\n    }\n}",
    expected:true
  },
  { name:"algebra/var-scope-join-1.rq",
    comment:"Variables have query scope.",
    query:"PREFIX : <http:\/\/example\/>\n\nSELECT *\n{ \n  ?X  :name \"paul\"\n  {?Y :name \"george\" . OPTIONAL { ?X :email ?Z } }\n}",
    expected:true
  },
  { name:"algebra/join-combo-1.rq",
    comment:"Tests nested combination of Join with a BGP \/ OPT and a BGP \/ UNION",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT ?a ?y ?d ?z\n{ \n    ?a :p ?c OPTIONAL { ?a :r ?d }. \n    ?a ?p 1 { ?p a ?y } UNION { ?a ?z ?p } \n}",
    expected:true
  },
  { name:"algebra/join-combo-2.rq",
    comment:"Tests combination of Join operator with Graph on LHS and Union on RHS",
    query:"PREFIX :    <http:\/\/example\/>\n\nSELECT ?x ?y ?z\n{ \n    GRAPH ?g { ?x ?p 1 } { ?x :p ?y } UNION { ?p a ?z }\n}",
    expected:true
  },
  { name:"bnode-coreference/query.rq",
    comment:"Query results must maintain bnode co-references in the dataset",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX  foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT ?x ?y\nWHERE {\n  ?x foaf:knows ?y .\n}",
    expected:true
  },
  { name:"optional/q-opt-1.rq",
    comment:"One optional clause",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT ?mbox ?name\n   {\n     ?x foaf:mbox ?mbox .\n     OPTIONAL { ?x foaf:name  ?name } .\n   }",
    expected:true
  },
  { name:"optional/q-opt-2.rq",
    comment:"One optional clause",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT ?mbox ?name ?nick\n   {\n     ?x foaf:mbox ?mbox .\n     OPTIONAL { ?x foaf:name  ?name } .\n     OPTIONAL { ?x foaf:nick  ?nick } .\n   }",
    expected:true
  },
  { name:"optional/q-opt-3.rq",
    comment:"Union is not optional",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nSELECT ?mbox ?name\n   {\n     { ?x foaf:mbox ?mbox }\n   UNION \n     { ?x foaf:mbox ?mbox . ?x foaf:name  ?name }\n   }",
    expected:true
  },
  { name:"optional/q-opt-complex-1.rq",
    comment:"Complex optional: LeftJoin(LeftJoin(BGP(..),{..}),Join(BGP(..),Union(..,..)))",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?person ?nick ?page ?img ?name ?firstN\n{ \n    ?person foaf:nick ?nick\n    OPTIONAL { ?person foaf:isPrimaryTopicOf ?page } \n    OPTIONAL { \n        ?person foaf:name ?name \n        { ?person foaf:depiction ?img } UNION \n        { ?person foaf:firstName ?firstN } \n    } FILTER ( bound(?page) || bound(?img) || bound(?firstN) ) \n}",
    expected:true
  },
  { name:"optional/q-opt-complex-2.rq",
    comment:"Complex optional: LeftJoin(Join(BGP(..),Graph(var,{..})),Union(..,..))",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX    ex:   <http:\/\/example.org\/things#>\nSELECT ?id ?ssn\nWHERE \n{ \n    ?person \n        a foaf:Person;\n        foaf:name ?name . \n    GRAPH ?x { \n        [] foaf:name ?name;\n           foaf:nick ?nick\n    } \n    OPTIONAL { \n        { ?person ex:empId ?id } UNION { ?person ex:ssn ?ssn } \n    } \n}",
    expected:true
  },
  { name:"optional/q-opt-complex-3.rq",
    comment:"Complex optional: LeftJoin(Join(BGP(..),Graph(var,{..})),LeftJoin(BGP(..),{..}))",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX    ex:   <http:\/\/example.org\/things#>\nSELECT ?name ?nick ?plan ?dept\nWHERE \n{ \n    ?person \n        a foaf:Person;\n        foaf:name ?name . \n    GRAPH ?x { \n        [] foaf:name ?name;\n           foaf:nick ?nick\n    } \n    OPTIONAL { \n        ?person ex:healthplan ?plan \n        OPTIONAL { ?person ex:department ?dept } \n    } \n}",
    expected:true
  },
  { name:"optional/q-opt-complex-4.rq",
    comment:"Complex optional: LeftJoin(Join(BGP(..),Union(..,..)),Join(BGP(..),Graph(varOrIRI,{..})))",
    query:"PREFIX  foaf:   <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX    ex:   <http:\/\/example.org\/things#>\nSELECT ?name ?plan ?dept ?img \nWHERE \n{ \n    ?person foaf:name ?name  \n    { ?person ex:healthplan ?plan } UNION { ?person ex:department ?dept } \n    OPTIONAL { \n        ?person a foaf:Person\n        GRAPH ?g { \n            [] foaf:name ?name;\n               foaf:depiction ?img \n        } \n    } \n}",
    expected:true
  },
  { name:"optional-filter/expr-1.rq",
    comment:"FILTER inside an OPTIONAL does not block an entire solution",
    query:"PREFIX  dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  x: <http:\/\/example.org\/ns#>\nSELECT  ?title ?price\nWHERE\n    { ?book dc:title ?title . \n      OPTIONAL\n        { ?book x:price ?price . \n          FILTER (?price < 15) .\n        } .\n    }",
    expected:true
  },
  { name:"optional-filter/expr-2.rq",
    comment:"FILTER outside an OPTIONAL tests bound and unbound variables",
    query:"PREFIX  dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  x: <http:\/\/example.org\/ns#>\nSELECT  ?title ?price\nWHERE\n    { ?book dc:title ?title . \n      OPTIONAL\n        { ?book x:price ?price } . \n      FILTER (?price < 15)  .\n    }",
    expected:true
  },
  { name:"optional-filter/expr-3.rq",
    comment:"Use !bound to only run outer FILTERs against variables bound in an OPTIONAL",
    query:"PREFIX  dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  x: <http:\/\/example.org\/ns#>\nSELECT  ?title ?price\nWHERE\n    { ?book dc:title ?title . \n      OPTIONAL\n        { ?book x:price ?price } . \n      FILTER ( ( ! bound(?price) ) || ( ?price < 15 ) ) .\n    }",
    expected:true
  },
  { name:"optional-filter/expr-4.rq",
    comment:"FILTER inside an OPTIONAL does not corrupt the entire solution",
    query:"PREFIX  dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  x: <http:\/\/example.org\/ns#>\nSELECT  ?title ?price\nWHERE\n    { ?book dc:title ?title . \n      OPTIONAL\n        { ?book x:price ?price . \n          FILTER (?price < 15 && ?title = \"TITLE 2\") .\n        } .\n    }",
    expected:true
  },
  { name:"optional-filter/expr-5.rq",
    comment:"Double curly braces get simplified to single curly braces early on, before filters are scoped",
    query:"PREFIX  dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  x: <http:\/\/example.org\/ns#>\nSELECT  ?title ?price\nWHERE\n    { ?book dc:title ?title . \n      OPTIONAL\n        {\n          { \n            ?book x:price ?price . \n            FILTER (?title = \"TITLE 2\") .\n          }\n        } .\n    }",
    expected:true
  },
  { name:"optional-filter/expr-5.rq",
    comment:"Double curly braces do NOT get simplified to single curly braces early on, before filters are scoped",
    query:"PREFIX  dc: <http:\/\/purl.org\/dc\/elements\/1.1\/>\nPREFIX  x: <http:\/\/example.org\/ns#>\nSELECT  ?title ?price\nWHERE\n    { ?book dc:title ?title . \n      OPTIONAL\n        {\n          { \n            ?book x:price ?price . \n            FILTER (?title = \"TITLE 2\") .\n          }\n        } .\n    }",
    expected:true
  },
  { name:"graph/graph-01.rq",
    comment:"Data: default graph \/ Query: default graph",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * { ?s ?p ?o }",
    expected:true
  },
  { name:"graph/graph-02.rq",
    comment:"Data: named graph \/ Query: default graph",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * { ?s ?p ?o }",
    expected:true
  },
  { name:"graph/graph-03.rq",
    comment:"Data: named graph \/ Query: named graph graph",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * { \n    GRAPH ?g { ?s ?p ?o }\n}",
    expected:true
  },
  { name:"graph/graph-04.rq",
    comment:"Data: named graph \/ Query: default graph",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * { \n    GRAPH ?g { ?s ?p ?o }\n}",
    expected:true
  },
  { name:"graph/graph-05.rq",
    comment:"Data: default and named \/ Query: default graph",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * { ?s ?p ?o }",
    expected:true
  },
  { name:"graph/graph-06.rq",
    comment:"Data: default and named \/ Query: named graph",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * { \n    GRAPH ?g { ?s ?p ?o }\n}",
    expected:true
  },
  { name:"graph/graph-07.rq",
    comment:"Data: default and named \/ Query: all data by UNION",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \n{ \n   { ?s ?p ?o }\n  UNION\n   { GRAPH ?g { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"graph/graph-08.rq",
    comment:"Data: default and named \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"graph/graph-09.rq",
    comment:"Data: default and named (bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"graph/graph-10.rq",
    comment:"Data: default and named (same data, with bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"graph/graph-10.rq",
    comment:"Data: default and named (same data, with bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"graph/graph-11.rq",
    comment:"Data: default and named (several) \/ Query: get everything",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \n{ \n   { ?s ?p ?o }\n  UNION\n   { GRAPH ?g { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"dataset/dataset-01.rq",
    comment:"Data: default dataset \/ Query: default dataset",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\n{ ?s ?p ?o }",
    expected:true
  },
  { name:"dataset/dataset-02.rq",
    comment:"Data: named dataset \/ Query: default dataset",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT *\nFROM NAMED <data-g1.ttl>\n{ ?s ?p ?o }",
    expected:true
  },
  { name:"dataset/dataset-03.rq",
    comment:"Data: named dataset \/ Query: named dataset dataset",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM NAMED <data-g1.ttl>\n{ \n    GRAPH ?g { ?s ?p ?o }\n}",
    expected:true
  },
  { name:"dataset/dataset-04.rq",
    comment:"Data: named dataset \/ Query: default dataset",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\n{ \n    GRAPH ?g { ?s ?p ?o }\n}",
    expected:true
  },
  { name:"dataset/dataset-05.rq",
    comment:"Data: default and named \/ Query: default dataset",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\n{ ?s ?p ?o }",
    expected:true
  },
  { name:"dataset/dataset-06.rq",
    comment:"Data: default and named \/ Query: named dataset",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\n{ \n    GRAPH ?g { ?s ?p ?o }\n}",
    expected:true
  },
  { name:"dataset/dataset-07.rq",
    comment:"Data: default and named \/ Query: all data by UNION",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\n{ \n   { ?s ?p ?o }\n  UNION\n   { GRAPH ?g { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"dataset/dataset-08.rq",
    comment:"Data: default and named \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"dataset/dataset-09.rq",
    comment:"Data: default and named (bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g3.ttl>\nFROM NAMED <data-g3.ttl>{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"dataset/dataset-10.rq",
    comment:"Data: default and named (same data, with bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g3.ttl>\nFROM NAMED <data-g3.ttl>\n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"dataset/dataset-11.rq",
    comment:"Data: default and named (several) \/ Query: get everything",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\nFROM NAMED <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\nFROM NAMED <data-g3.ttl>\nFROM NAMED <data-g4.ttl>\n{ \n   { ?s ?p ?o }\n  UNION\n   { GRAPH ?g { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"dataset/dataset-12.rq",
    comment:"Data: default (several) and named (several) \/ Query: get everything",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1.ttl>\nFROM <data-g2.ttl>\nFROM <data-g3.ttl>\nFROM <data-g4.ttl>\nFROM NAMED <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\nFROM NAMED <data-g3.ttl>\nFROM NAMED <data-g4.ttl>\n{ \n   { ?s ?p ?o }\n  UNION\n   { GRAPH ?g { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"dataset/dataset-09b.rq",
    comment:"Data: default and named (bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g3-dup.ttl>\nFROM NAMED <data-g3.ttl>{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"dataset/dataset-10b.rq",
    comment:"Data: default and named (same data, with bnodes) \/ Query: common subjects",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g3-dup.ttl>\nFROM NAMED <data-g3.ttl>\n{ \n   ?s ?p ?o\n   GRAPH ?g { ?s ?q ?v }\n}",
    expected:true
  },
  { name:"dataset/dataset-12b.rq",
    comment:"Data: default (several) and named (several) \/ Query: get everything",
    query:"PREFIX : <http:\/\/example\/> \n\nSELECT * \nFROM <data-g1-dup.ttl>\nFROM <data-g2-dup.ttl>\nFROM <data-g3-dup.ttl>\nFROM <data-g4-dup.ttl>\nFROM NAMED <data-g1.ttl>\nFROM NAMED <data-g2.ttl>\nFROM NAMED <data-g3.ttl>\nFROM NAMED <data-g4.ttl>\n{ \n   { ?s ?p ?o }\n  UNION\n   { GRAPH ?g { ?s ?p ?o } }\n}",
    expected:true
  },
  { name:"type-promotion/tP-double-double.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-double-double.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:double1 rdf:value ?l .\n         t:double1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:double ) }",
    expected:true
  },
  { name:"type-promotion/tP-double-float.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-double-float.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:double1 rdf:value ?l .\n         t:float1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:double ) }",
    expected:true
  },
  { name:"type-promotion/tP-double-decimal.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-double-decimal.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:double1 rdf:value ?l .\n         t:decimal1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:double ) }",
    expected:true
  },
  { name:"type-promotion/tP-float-float.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-float-float.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:float1 rdf:value ?l .\n         t:float1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:float ) }",
    expected:true
  },
  { name:"type-promotion/tP-float-decimal.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-float-decimal.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:float1 rdf:value ?l .\n         t:decimal1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:float ) }",
    expected:true
  },
  { name:"type-promotion/tP-decimal-decimal.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-decimal-decimal.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:decimal1 rdf:value ?l .\n         t:decimal1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:decimal ) }",
    expected:true
  },
  { name:"type-promotion/tP-integer-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-integer-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:integer1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-nonPositiveInteger-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-nonPositiveInteger-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:nonPositiveIntegerN1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-negativeInteger-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-negativeInteger-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:negativeIntegerN1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-long-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-long-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:long1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-int-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-int-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:int1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-byte-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-byte-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:byte1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-nonNegativeInteger-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-nonNegativeInteger-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:nonNegativeInteger1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-unsignedLong-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-unsignedLong-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:unsignedLong1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-unsignedInt-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-unsignedInt-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:unsignedInt1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-unsignedShort-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-unsignedShort-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:unsignedShort1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-unsignedByte-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-unsignedByte-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:unsignedByte1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-positiveInteger-short.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-positiveInteger-short.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:positiveInteger1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:integer ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-double.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-double.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:double1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:double ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-float.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-float.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:float1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:float ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-decimal.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-decimal.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:decimal1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:decimal ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-short-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-short-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:short ) }",
    expected:true
  },
  { name:"type-promotion/tP-byte-short-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-byte-short-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:byte1 rdf:value ?l .\n         t:short1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:short ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-long-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-long-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:long1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:decimal ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-int-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-int-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:int1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:float ) }",
    expected:true
  },
  { name:"type-promotion/tP-short-byte-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-short-byte-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:short1 rdf:value ?l .\n         t:byte1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:double ) }",
    expected:true
  },
  { name:"type-promotion/tP-double-float-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-double-float-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:double1 rdf:value ?l .\n         t:float1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:float ) }",
    expected:true
  },
  { name:"type-promotion/tP-double-decimal-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-double-decimal-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:double1 rdf:value ?l .\n         t:decimal1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:decimal ) }",
    expected:true
  },
  { name:"type-promotion/tP-float-decimal-fail.rq",
    comment:"Positive test: product of type promotion within the xsd:decimal type tree.",
    query:"# Positive test: product of type promotion within the xsd:decimal type tree.\n# $Id: tP-float-decimal-fail.rq,v 1.1 2007\/06\/29 14:24:48 aseaborne Exp $\n\nPREFIX t: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/TypePromotion\/tP-0#>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nASK\n WHERE { t:float1 rdf:value ?l .\n         t:decimal1 rdf:value ?r .\n         FILTER ( datatype(?l + ?r) = xsd:decimal ) }",
    expected:true
  },
  { name:"cast/cast-str.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:string(?v)) = xsd:string) .\n}",
    expected:true
  },
  { name:"cast/cast-flt.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:float(?v)) = xsd:float) .\n}",
    expected:true
  },
  { name:"cast/cast-dbl.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:double(?v)) = xsd:double) .\n}",
    expected:true
  },
  { name:"cast/cast-dec.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:decimal(?v)) = xsd:decimal) .\n}",
    expected:true
  },
  { name:"cast/cast-int.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:integer(?v)) = xsd:integer) .\n}",
    expected:true
  },
  { name:"cast/cast-dT.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:dateTime(?v)) = xsd:dateTime) .\n}",
    expected:true
  },
  { name:"cast/cast-bool.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n    ?s :p ?v .\n    FILTER(datatype(xsd:boolean(?v)) = xsd:boolean) .\n}",
    expected:true
  },
  { name:"boolean-effective-value/query-boolean-literal.rq",
    comment:"",
    query:"prefix : <http:\/\/example.org\/ns#>\nselect ?x where {\n    ?x :p \"foo\" .\n    FILTER (true) .\n}",
    expected:true
  },
  { name:"boolean-effective-value/query-bev-1.rq",
    comment:"Non-zero numerics, non-empty strings, and the true boolean have an EBV of true",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a\nWHERE\n    { ?a :p ?v . \n      FILTER (?v) .\n    }",
    expected:true
  },
  { name:"boolean-effective-value/query-bev-2.rq",
    comment:"Zero-valued numerics, the empty string, and the false boolean have an EBV of false",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a\nWHERE\n    { ?a :p ?v . \n      FILTER ( ! ?v ) .\n    }",
    expected:true
  },
  { name:"boolean-effective-value/query-bev-3.rq",
    comment:"The && operator takes the EBV of its operands",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a\nWHERE\n    { ?a :p ?v . \n      FILTER (\"true\"^^xsd:boolean && ?v) .\n    }",
    expected:true
  },
  { name:"boolean-effective-value/query-bev-4.rq",
    comment:"The || operator takes the EBV of its operands",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a\nWHERE\n    { ?a :p ?v . \n      FILTER (\"false\"^^xsd:boolean || ?v) .\n    }",
    expected:true
  },
  { name:"boolean-effective-value/query-bev-5.rq",
    comment:"The EBV of an unbound value  or a literal with an unknown datatype is a type error, which eliminates the solution in question",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a\nWHERE\n    { ?a :p ?v . \n      OPTIONAL\n        { ?a :q ?w } . \n      FILTER (?w) .\n    }",
    expected:true
  },
  { name:"boolean-effective-value/query-bev-6.rq",
    comment:"Negating a type error is still a type error",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a ?w\nWHERE\n    { ?a :p ?v . \n      OPTIONAL\n        { ?a :q ?w } . \n      FILTER ( ! ?w ) .\n    }",
    expected:true
  },
  { name:"bound/bound1.rq",
    comment:"BOUND test case.",
    query:"PREFIX  : <http:\/\/example.org\/ns#>\nSELECT  ?a ?c\nWHERE\n    { ?a :b ?c . \n      OPTIONAL\n        { ?c :d ?e } . \n      FILTER (! bound(?e)) \n    }",
    expected:true
  },
  { name:"expr-builtin/q-str-1.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER ( str(?v) = \"1\" ) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-str-2.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER ( str(?v) = \"01\" ) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-str-3.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER ( str(?v) = \"zzz\" ) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-str-4.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER ( str(?v) = \"\"  ) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-blank-1.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER isBlank(?v) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-isliteral-1.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example\/>\nSELECT  ?x \nWHERE\n    { ?x :p ?v . \n      FILTER isLiteral(?v) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-datatype-1.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER ( datatype(?v) = xsd:double ) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-datatype-2.rq",
    comment:"updated from original test case: eliminated ordering from test",
    query:"# Which literals have a datatype and which are errors.\n\nPREFIX : <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x\n{ ?x :p ?v . \n  FILTER(  datatype(?v) != <http:\/\/example\/NotADataTypeIRI> ) \n}",
    expected:true
  },
  { name:"expr-builtin/q-datatype-3.rq",
    comment:"updated from original test case: eliminated ordering from test",
    query:"# Whichliterals have xsd:string as a datatype\n\nPREFIX : <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x\n{ ?x :p ?v . \n  FILTER( datatype(?v) = xsd:string ) \n}",
    expected:true
  },
  { name:"expr-builtin/q-lang-1.rq",
    comment:"updated from original test case: eliminated ordering from test",
    query:"# Test which things have a lang tag of some form.\n\nPREFIX : <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x\n{ ?x :p ?v . \n  FILTER ( lang(?v) != \'@NotALangTag@\' )\n}",
    expected:true
  },
  { name:"expr-builtin/q-lang-2.rq",
    comment:"updated from original test case: eliminated ordering from test",
    query:"PREFIX : <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x\n{ ?x :p ?v . \n  FILTER ( lang(?v) = \'\' )\n}",
    expected:true
  },
  { name:"expr-builtin/q-lang-3.rq",
    comment:"updated from original test case: eliminated ordering from test",
    query:"PREFIX : <http:\/\/example\/> \nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?x\n{ ?x :p \"string\"@EN\n}",
    expected:true
  },
  { name:"expr-builtin/q-uri-1.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER isURI(?v) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-iri-1.rq",
    comment:"",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x ?v\nWHERE\n    { ?x :p ?v . \n      FILTER isIRI(?v) .\n    }",
    expected:true
  },
  { name:"expr-builtin/q-langMatches-1.rq",
    comment:"langMatches(lang(?v), \'en-GB\') matches \'abc\'@en-gb",
    query:"PREFIX : <http:\/\/example.org\/#>\n\nSELECT *\n{ :x ?p ?v . FILTER langMatches(lang(?v), \"en-GB\") . }",
    expected:true
  },
  { name:"expr-builtin/q-langMatches-2.rq",
    comment:"langMatches(lang(?v), \'en\') matches \'abc\'@en, \'abc\'@en-gb",
    query:"PREFIX : <http:\/\/example.org\/#>\n\nSELECT *\n{ :x ?p ?v . FILTER langMatches(lang(?v), \"en\") . }",
    expected:true
  },
  { name:"expr-builtin/q-langMatches-3.rq",
    comment:"langMatches(lang(?v), \'*\') matches \'abc\'@en, \'abc\'@en-gb, \'abc\'@fr",
    query:"PREFIX : <http:\/\/example.org\/#>\n\nSELECT *\n{ :x ?p ?v . FILTER langMatches(lang(?v), \"*\") . }",
    expected:true
  },
  { name:"expr-builtin/q-langMatches-4.rq",
    comment:"! langMatches(lang(?v), \'*\') matches \'abc\'",
    query:"PREFIX : <http:\/\/example.org\/#>\n\nSELECT *\n{ :x ?p ?v . FILTER (! langMatches(lang(?v), \"*\")) . }",
    expected:true
  },
  { name:"expr-builtin/q-langMatches-de-de.rq",
    comment:"the basic range \'de-de\' does not match \'de-Latn-de\'",
    query:"# q-langMatches-de-de.rq\n# $Id: q-langMatches-de-de.rq,v 1.1 2007\/08\/11 18:32:04 eric Exp $\n\nPREFIX : <http:\/\/example.org\/#>\n\nSELECT *\n{ :x ?p ?v . FILTER langMatches(lang(?v), \"de-de\") . }",
    expected:true
  },
  { name:"expr-builtin/lang-case-sensitivity-eq.rq",
    comment:"\'xyz\'@en = \'xyz\'@EN",
    query:"# Test: \'xyz\'@en = \'xyz\'@EN\n# $Id: lang-case-sensitivity-eq.rq,v 1.1 2007\/06\/24 23:15:38 lfeigenb Exp $\n\nPREFIX     :    <http:\/\/example\/>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER ( ?v1 = ?v2 )\n}",
    expected:true
  },
  { name:"expr-builtin/lang-case-sensitivity-ne.rq",
    comment:"\'xyz\'@en != \'xyz\'@EN",
    query:"# Test: \'xyz\'@en != \'xyz\'@EN\n# $Id: lang-case-sensitivity-ne.rq,v 1.1 2007\/06\/24 23:15:38 lfeigenb Exp $\n\nPREFIX     :    <http:\/\/example\/>\nPREFIX  xsd:    <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER ( ?v1 != ?v2 )\n}",
    expected:true
  },
  { name:"expr-builtin/sameTerm.rq",
    comment:"sameTerm(?v1, ?v2)",
    query:"# Test: sameTerm\n# $Id: sameTerm.rq,v 1.1 2007\/08\/31 14:01:57 eric Exp $\n\nPREFIX     :    <http:\/\/example.org\/things#>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER sameTerm(?v1, ?v2)\n}",
    expected:true
  },
  { name:"expr-builtin/sameTerm-eq.rq",
    comment:"sameTerm(?v1, ?v2) && ?v1 = ?v2",
    query:"# Test: sameTerm and eq\n# $Id: sameTerm-eq.rq,v 1.1 2007\/08\/31 14:01:57 eric Exp $\n\nPREFIX     :    <http:\/\/example.org\/things#>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER ( sameTerm(?v1, ?v2) && ?v1 = ?v2 )\n}",
    expected:true
  },
  { name:"expr-builtin/sameTerm-not-eq.rq",
    comment:"!sameTerm(?v1, ?v2) && ?v1 = ?v2",
    query:"# Test: !sameTerm and eq\n# $Id: sameTerm-not-eq.rq,v 1.1 2007\/08\/31 14:01:57 eric Exp $\n\nPREFIX     :    <http:\/\/example.org\/things#>\n\nSELECT *\n{\n    ?x1 :p ?v1 .\n    ?x2 :p ?v2 .\n    FILTER ( !sameTerm(?v1, ?v2) && ?v1 = ?v2 )\n}",
    expected:true
  },
  { name:"expr-ops/query-ge-1.rq",
    comment:">= in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    FILTER(?o >= 3) .\n}",
    expected:true
  },
  { name:"expr-ops/query-le-1.rq",
    comment:"<= in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    FILTER(?o <= 2) .\n}",
    expected:true
  },
  { name:"expr-ops/query-mul-1.rq",
    comment:"A * B in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    ?s2 :p ?o2 .\n    FILTER(?o * ?o2 = 4) .\n}",
    expected:true
  },
  { name:"expr-ops/query-plus-1.rq",
    comment:"A + B in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    ?s2 :p ?o2 .\n    FILTER(?o + ?o2 = 3) .\n}",
    expected:true
  },
  { name:"expr-ops/query-minus-1.rq",
    comment:"A - B in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    ?s2 :p ?o2 .\n    FILTER(?o - ?o2 = 3) .\n}",
    expected:true
  },
  { name:"expr-ops/query-unplus-1.rq",
    comment:"+A in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    FILTER(?o = +3) .\n}",
    expected:true
  },
  { name:"expr-ops/query-unminus-1.rq",
    comment:"-A in FILTER expressions",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n    ?s :p ?o .\n    FILTER(-?o = -2) .\n}",
    expected:true
  },
  { name:"expr-equals/query-eq-1.rq",
    comment:"= in FILTER expressions is value equality",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p ?v . \n      FILTER ( ?v = 1 ) .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-2.rq",
    comment:"= in FILTER expressions is value equality",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p ?v . \n      FILTER ( ?v = 1.0e0 )  .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-3.rq",
    comment:"Numerics are not value-equivalent to plain literals",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p ?v . \n      FILTER ( ?v = \"1\" ) .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-4.rq",
    comment:"= compares plain literals and unknown types with the same lexical form as false",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p ?v . \n      FILTER ( ?v = \"zzz\" ) .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-5.rq",
    comment:"= on IRI terms",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p ?v . \n      FILTER ( ?v = :z  ) .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq2-1.rq",
    comment:"= in FILTER is value equality",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?v1 ?v2\nWHERE\n    { ?x1 :p ?v1 .\n      ?x2 :p ?v2 . \n      FILTER ( ?v1 = ?v2 ) .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq2-1.rq",
    comment:"!= in FILTER is value inequality",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?v1 ?v2\nWHERE\n    { ?x1 :p ?v1 .\n      ?x2 :p ?v2 . \n      FILTER ( ?v1 = ?v2 ) .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-graph-1.rq",
    comment:"Graph pattern matching matches exact terms, not values",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p 1 . \n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-graph-2.rq",
    comment:"Graph pattern matching matches exact terms, not values",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p 1.0e0 .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-graph-3.rq",
    comment:"Graph pattern matching matches exact terms, not values",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p \"1\"\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-graph-4.rq",
    comment:"Graph pattern matching matches exact terms, not values",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p \"zzz\" .\n    }",
    expected:true
  },
  { name:"expr-equals/query-eq-graph-5.rq",
    comment:"Graph pattern matching matches exact terms, not values",
    query:"PREFIX  xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nPREFIX  : <http:\/\/example.org\/things#>\nSELECT  ?x\nWHERE\n    { ?x :p ?v . \n      FILTER ( ?v = :z  ) .\n    }",
    expected:true
  },
  { name:"regex/regex-query-001.rq",
    comment:"Simple unanchored match test",
    query:"PREFIX  rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX  ex: <http:\/\/example.com\/#>\n\nSELECT ?val\nWHERE {\n\tex:foo rdf:value ?val .\n\tFILTER regex(?val, \"GHI\")\n}",
    expected:true
  },
  { name:"regex/regex-query-002.rq",
    comment:"Case insensitive unanchored match test",
    query:"PREFIX  ex: <http:\/\/example.com\/#>\nPREFIX  rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\n\nSELECT ?val\nWHERE {\n\tex:foo rdf:value ?val .\n\tFILTER regex(?val, \"DeFghI\", \"i\")\n}",
    expected:true
  },
  { name:"regex/regex-query-003.rq",
    comment:"Use\/mention test",
    query:"PREFIX  rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX  ex:  <http:\/\/example.com\/#>\n\nSELECT ?val\nWHERE {\n\tex:foo rdf:value ?val .\n\tFILTER regex(?val, \"example\\\\.com\")\n}",
    expected:true
  },
  { name:"regex/regex-query-004.rq",
    comment:"str()+URI test",
    query:"PREFIX  rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#>\nPREFIX  ex: <http:\/\/example.com\/#>\nSELECT ?val\nWHERE {\n\tex:foo rdf:value ?val .\n\tFILTER regex(str(?val), \"example\\\\.com\")\n}",
    expected:true
  },
  { name:"i18n/kanji-01.rq",
    comment:"",
    query:"# $Id: kanji-01.rq,v 1.3 2005\/11\/06 08:27:50 eric Exp $\n# test kanji QNames\nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX \u98DF: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/i18n\/kanji.ttl#>\nSELECT ?name ?food WHERE {\n  [ foaf:name ?name ;\n    \u98DF:\u98DF\u3079\u308B ?food ] . }",
    expected:true
  },
  { name:"i18n/kanji-02.rq",
    comment:"",
    query:"# $Id: kanji-02.rq,v 1.4 2005\/11\/06 08:27:50 eric Exp $\n# test wide spaces\nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX \u98DF: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/i18n\/kanji.ttl#>\nSELECT ?name WHERE {\n  [ foaf:name ?name ;\n    \u98DF:\u98DF\u3079\u308B \u98DF:\u6D77\u8001 ] . }",
    expected:true
  },
  { name:"i18n/normalization-01.rq",
    comment:"",
    query:"# Figure out what happens with normalization form C.\nPREFIX foaf: <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX HR: <http:\/\/www.w3.org\/2001\/sw\/DataAccess\/tests\/data\/i18n\/normalization.ttl#>\nSELECT ?name\n WHERE { [ foaf:name ?name; \n           HR:resume\u0301 ?resume ] . }",
    expected:true
  },
  { name:"i18n/normalization-02.rq",
    comment:"Example 1 from http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2005JulSep\/0096",
    query:"# Example 1 from\n# http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2005JulSep\/0096\n# $Id: normalization-02.rq,v 1.1 2005\/08\/09 14:35:26 eric Exp $\nPREFIX : <http:\/\/example\/vocab#>\nPREFIX p1: <eXAMPLE:\/\/a\/.\/b\/..\/b\/%63\/%7bfoo%7d#>\n\nSELECT ?S WHERE { ?S :p p1:xyz }",
    expected:true
  },
  { name:"i18n/normalization-03.rq",
    comment:"Example 2 from http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2005JulSep\/0096",
    query:"# Example 2 from\n# http:\/\/lists.w3.org\/Archives\/Public\/public-rdf-dawg\/2005JulSep\/0096\n# $Id: normalization-03.rq,v 1.1 2005\/08\/09 14:35:26 eric Exp $\nPREFIX : <http:\/\/example\/vocab#>\nPREFIX p2: <http:\/\/example.com:80\/#>\n\nSELECT ?S WHERE { ?S :p p2:abc }",
    expected:true
  },
  { name:"construct/query-ident.rq",
    comment:"Graph equivalent result graph",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX  foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nCONSTRUCT { ?s ?p ?o . }\nWHERE {\n  ?s ?p ?o .\n}",
    expected:true
  },
  { name:"construct/query-subgraph.rq",
    comment:"Result subgraph of original graph",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX  foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nCONSTRUCT { ?s foaf:name ?o . }\nWHERE {\n  ?s foaf:name ?o .\n}",
    expected:true
  },
  { name:"construct/query-reif-1.rq",
    comment:"Reification of the default graph",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX  foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nCONSTRUCT { [ rdf:subject ?s ;\n              rdf:predicate ?p ;\n              rdf:object ?o ] . }\nWHERE {\n  ?s ?p ?o .\n}",
    expected:true
  },
  { name:"construct/query-reif-2.rq",
    comment:"Reification of the default graph",
    query:"PREFIX rdf: <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#> \nPREFIX  foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\n\nCONSTRUCT { _:a rdf:subject ?s ;\n                rdf:predicate ?p ;\n                rdf:object ?o  . }\nWHERE {\n  ?s ?p ?o .\n}",
    expected:true
  },
  { name:"construct/query-construct-optional.rq",
    comment:"Reification of the default graph",
    query:"PREFIX : <http:\/\/example\/> \n\nCONSTRUCT { ?x :p2 ?v }\n\nWHERE\n{\n  ?x :p ?o .\n  OPTIONAL {?o :q ?v }\n}",
    expected:true
  },
  { name:"ask/ask-1.rq",
    comment:"",
    query:"PREFIX :   <http:\/\/example\/>\n\nASK { :x :p 1 }",
    expected:true
  },
  { name:"ask/ask-4.rq",
    comment:"",
    query:"PREFIX :   <http:\/\/example\/>\n\nASK { :x :p 99 }",
    expected:true
  },
  { name:"ask/ask-7.rq",
    comment:"",
    query:"PREFIX :   <http:\/\/example\/>\n\nASK { :x :p ?x }",
    expected:true
  },
  { name:"ask/ask-8.rq",
    comment:"",
    query:"PREFIX :   <http:\/\/example\/>\n\nASK { :x :p ?x . FILTER(?x = 99) }",
    expected:true
  },
  { name:"distinct/no-distinct-1.rq",
    comment:"",
    query:"SELECT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/distinct-1.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT DISTINCT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/no-distinct-1.rq",
    comment:"",
    query:"SELECT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/distinct-1.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT DISTINCT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/no-distinct-1.rq",
    comment:"",
    query:"SELECT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/distinct-1.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT DISTINCT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/no-distinct-2.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT ?v\n{\n    :x1 ?p ?o\n    OPTIONAL { ?o :q ?v }\n}",
    expected:true
  },
  { name:"distinct/distinct-2.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT DISTINCT ?v\n{\n    :x1 ?p ?o\n    OPTIONAL { ?o :q ?v }\n}",
    expected:true
  },
  { name:"distinct/no-distinct-1.rq",
    comment:"",
    query:"SELECT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/distinct-1.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT DISTINCT ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  },
  { name:"distinct/distinct-star-1.rq",
    comment:"",
    query:"PREFIX :         <http:\/\/example\/> \nPREFIX xsd:      <http:\/\/www.w3.org\/2001\/XMLSchema#> \nSELECT DISTINCT * \nWHERE { \n  { ?s :p ?o } UNION { ?s :q ?o }\n}",
    expected:true
  },
  { name:"sort/query-sort-1.rq",
    comment:"Alphabetic sort (ascending) on untyped literals",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?name\nWHERE { ?x foaf:name ?name }\nORDER BY ?name",
    expected:true
  },
  { name:"sort/query-sort-2.rq",
    comment:"Alphabetic sort (descending) on untyped literals",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?name\nWHERE { ?x foaf:name ?name }\nORDER BY DESC(?name)",
    expected:true
  },
  { name:"sort/query-sort-3.rq",
    comment:"Sort on (possibly unbound) URIs",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?name ?mbox\nWHERE { ?x foaf:name ?name .\n           OPTIONAL { ?x foaf:mbox ?mbox }\n      }\nORDER BY ASC(?mbox)",
    expected:true
  },
  { name:"sort/query-sort-4.rq",
    comment:"Sort on datatyped (integer) literals",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX ex:        <http:\/\/example.org\/things#> \n\nSELECT ?name ?emp\nWHERE { ?x foaf:name ?name ;\n           ex:empId ?emp\n      }\nORDER BY ASC(?emp)",
    expected:true
  },
  { name:"sort/query-sort-5.rq",
    comment:"Sort first on untyped literals (ascending), then on datatyped (integer) literals (descending",
    query:"PREFIX foaf:    <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX ex:      <http:\/\/example.org\/things#> \n\nSELECT ?name ?emp\nWHERE { ?x foaf:name ?name ; \n           ex:empId ?emp \n      }\nORDER BY ?name DESC(?emp)",
    expected:true
  },
  { name:"sort/query-sort-6.rq",
    comment:"Sort on mixed result of uris and literals.",
    query:"PREFIX ex:      <http:\/\/example.org\/things#> \n\nSELECT ?address\nWHERE { ?x ex:address ?address }\nORDER BY ASC(?address)",
    expected:true
  },
  { name:"sort/query-sort-4.rq",
    comment:"Sort on comparable mixed typed literals (integer and float)",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX ex:        <http:\/\/example.org\/things#> \n\nSELECT ?name ?emp\nWHERE { ?x foaf:name ?name ;\n           ex:empId ?emp\n      }\nORDER BY ASC(?emp)",
    expected:true
  },
  { name:"sort/query-sort-4.rq",
    comment:"Sort on several mixed values (bnode, uri, literal)",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nPREFIX ex:        <http:\/\/example.org\/things#> \n\nSELECT ?name ?emp\nWHERE { ?x foaf:name ?name ;\n           ex:empId ?emp\n      }\nORDER BY ASC(?emp)",
    expected:true
  },
  { name:"sort/query-sort-9.rq",
    comment:"Alphabetic sort (ascending) on datatyped (string) literals",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?name\nWHERE { ?x foaf:name ?name }\nORDER BY ?name",
    expected:true
  },
  { name:"sort/query-sort-10.rq",
    comment:"Alphabetic sort (descending) on datatyped (string) literals",
    query:"PREFIX foaf:       <http:\/\/xmlns.com\/foaf\/0.1\/>\nSELECT ?name\nWHERE { ?x foaf:name ?name }\nORDER BY DESC(?name)",
    expected:true
  },
  { name:"sort/query-sort-numbers.rq",
    comment:"Sort by a bracketted expression",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n  ?s :p ?o1 ; :q ?o2 .\n} ORDER BY (?o1 + ?o2)",
    expected:true
  },
  { name:"sort/query-sort-builtin.rq",
    comment:"Sort by a builtin operator",
    query:"PREFIX : <http:\/\/example.org\/>\nSELECT ?s WHERE {\n  ?s :p ?o .\n} ORDER BY str(?o)",
    expected:true
  },
  { name:"sort/query-sort-function.rq",
    comment:"Sort by function invocation",
    query:"PREFIX : <http:\/\/example.org\/>\nPREFIX xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#>\nSELECT ?s WHERE {\n  ?s :p ?o .\n} ORDER BY xsd:integer(?o)",
    expected:true
  },
  { name:"solution-seq/slice-01.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nLIMIT 1",
    expected:true
  },
  { name:"solution-seq/slice-02.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nLIMIT 100",
    expected:true
  },
  { name:"solution-seq/slice-03.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nLIMIT 0",
    expected:true
  },
  { name:"solution-seq/slice-04.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT DISTINCT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nLIMIT 100",
    expected:true
  },
  { name:"solution-seq/slice-10.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 1",
    expected:true
  },
  { name:"solution-seq/slice-11.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 0",
    expected:true
  },
  { name:"solution-seq/slice-12.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 100",
    expected:true
  },
  { name:"solution-seq/slice-13.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT DISTINCT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 2",
    expected:true
  },
  { name:"solution-seq/slice-20.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nLIMIT 1\nOFFSET 1",
    expected:true
  },
  { name:"solution-seq/slice-21.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 1\nLIMIT 2",
    expected:true
  },
  { name:"solution-seq/slice-22.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT ?v\nWHERE { [] ?p ?v }\nORDER BY ?v\nOFFSET 100\nLIMIT 1",
    expected:true
  },
  { name:"solution-seq/slice-23.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT  ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 2\nLIMIT 5",
    expected:true
  },
  { name:"solution-seq/slice-24.rq",
    comment:"",
    query:"PREFIX : <http:\/\/example.org\/ns#>\n\nSELECT  DISTINCT ?v\nWHERE { [] :num ?v }\nORDER BY ?v\nOFFSET 2\nLIMIT 5",
    expected:true
  },
  { name:"reduced/reduced-1.rq",
    comment:"",
    query:"PREFIX :         <http:\/\/example\/> \nPREFIX xsd:      <http:\/\/www.w3.org\/2001\/XMLSchema#> \nSELECT REDUCED * \nWHERE { \n  { ?s :p ?o } UNION { ?s :q ?o }\n}",
    expected:true
  },
  { name:"reduced/reduced-2.rq",
    comment:"",
    query:"PREFIX :      <http:\/\/example\/> \nPREFIX xsd:   <http:\/\/www.w3.org\/2001\/XMLSchema#>\n\nSELECT REDUCED ?v\n{\n    ?x ?p ?v .\n}",
    expected:true
  }
]
