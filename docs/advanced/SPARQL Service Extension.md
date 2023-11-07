# SPARQL Service Extension

## Abstract

This document presents extensions of SPARQL service implemented in Corese.

## Introduction

In a SERVICE clause, Corese allows for the addition of URL parameters to the service URL. These parameters are used to tune the behaviour of the service. For example, we can specify the format of the query result returned by the service.

For example, the following query will make Corese write a trace of the execution of the query in its logs:
```
http://corese.inria.fr/sparql?mode=debug&query=select * {?s ?p ?o}
```

<!-- Furthermore, SPARQL protocol can be used not only to execute standard SPARQL queries and SPARQL services but also additional kinds of services. For example, a SPARQL endpoint where the query result is processed by a transformation before return. Query Results can be augmented with Linked Results by means of the standard ''link'' tag.

In addition, a server can send a message back to a client by means of a JSON object returned as Linked Result. Hence, we design a communication model between sparql client-server back and forth. User writes SPARQL service or HTTP request with client/server URL parameters. Client parses and processes client parameters and sends HTTP request with server URL parameters to server. Server parses and process server parameters. Server responds to client with SPARQL Query Results augmented with a Linked Result message URL & document. Client reads server message and processes it. -->

#### Prefix used in this document.

```turtle
prefix st:  <http://ns.inria.fr/sparql-template/>
prefix stm: <http://ns.inria.fr/sparql-template/navlab#>
prefix d3:  <http://ns.inria.fr/sparql-template/d3#>
```


## Server URL Parameter

### Standard parameters
In this section, we consider endpoint URL parameters decoded by a SPARQL endpoint receiving an HTTP request.

Standard endpoint URL parameters are available.

```
query
default-graph-uri 
named-graph-uri
```


Currently corese does not implement the Update parameters below. Users can use query parameters listed above instead.

```
update
using-graph-uri 
using-named-graph-uri
```

### Shorthand format parameters
`format` specifies query result format when HTTP Accept header cannot be set. For example, `format=json` is equivalent to: `header "Accept: application/sparql-results+json"`.

For SELECT and ASK queries:
```
format = json   | xml 
```
For SELECT, ASK, DESCRIBE and CONSTRUCT queries:
```
format = jsonld | rdfxml | turtle
```

### Trace parameter
It is possible to specify several kinds of traces of execution.

```
mode = debug | trace 
```

For example, for the query `SELECT * { ?s ?p ?o } LIMIT 1` the normal trace is:
```
[datetime] INFO  webservice.SPARQLRestAPI.getTriplesXMLForGet:311 [] - getTriplesXMLForGet
[datetime] INFO  webservice.SPARQLResult.getResultFormat:90 [] - Endpoint URL: http://localhost:8080/sparql
[datetime] INFO  webservice.SPARQLResult.getResultFormat:96 [] - Query: SELECT * { ?s ?p ?o } LIMIT 1
[datetime] INFO  webservice.EventManager.log:72 [] - Workflow Context:
st:remoteHost : "[0:0:0:0:0:0:0:1]"
st:service : "http://ns.inria.fr/sparql-template/default"
request : "[org.eclipse.jetty.server.Request:Request(GET http://localhost:8080/sparql?query=SELECT%20*%20%7B%20?s%20?p%20?o%20%7D%20LIMIT%201)@429f0053]"^^dt:pointer
url : <http://localhost:8080/sparql>
user query: true
level: PRIVATE

[datetime] INFO  webservice.EventManager.log:73 [] - {st:count="[Map: size=2]"^^dt:map, st:date="[Map: size=2]"^^dt:map, st:host="[Map: size=2]"^^dt:map, st:hostlite="[Map: size=1]"^^dt:map}
[datetime] INFO  webservice.EventManager.log:74 [] - {st:sparql=2, "http://ns.inria.fr/sparql-template/default"=17}
[datetime] INFO  webservice.EventManager.log:76 [] - {"127.0.0.1"=11}
```

The `debug` parameter change the trace in the following:
```
[datetime] INFO  webservice.SPARQLRestAPI.getTriplesXMLForGet:311 [] - getTriplesXMLForGet
[datetime] INFO  webservice.SPARQLResult.getResultFormat:90 [] - Endpoint URL: http://localhost:8080/sparql
[datetime] INFO  webservice.SPARQLResult.getResultFormat:96 [] - Query: SELECT * { ?s ?p ?o } LIMIT 1
[datetime] INFO  webservice.EventManager.log:72 [] - Workflow Context:
debug : true
st:remoteHost : "[0:0:0:0:0:0:0:1]"
st:service : "http://ns.inria.fr/sparql-template/default"
mode : "("debug" )"^^dt:list
request : "[org.eclipse.jetty.server.Request:Request(GET http://localhost:8080/sparql?query=SELECT%20*%20%7B%20?s%20?p%20?o%20%7D%20LIMIT%201&mode=debug)@5c192889]"^^dt:pointer
url : <http://localhost:8080/sparql>
user query: true
level: PRIVATE

[datetime] INFO  webservice.EventManager.log:73 [] - {st:count="[Map: size=2]"^^dt:map, st:date="[Map: size=2]"^^dt:map, st:host="[Map: size=2]"^^dt:map, st:hostlite="[Map: size=1]"^^dt:map}
[datetime] INFO  webservice.EventManager.log:74 [] - {st:sparql=2, "http://ns.inria.fr/sparql-template/default"=18}
[datetime] INFO  webservice.EventManager.log:76 [] - {"127.0.0.1"=11}
select [NODE {?s }, NODE {?p }, NODE {?o }]
QUERY {
AND {
  EDGE {?s ?p ?o} } }
[datetime] WARN  tool.Message.log:64 [] - Eval: 00 AND {
EDGE {?s ?p ?o} }
[datetime] WARN  tool.Message.log:64 [] - Loop: 3 1
[datetime] INFO  webservice.SPARQLResult.getFormat:411 [] - transform: null
```

The `trace` parameter change the trace in the following:
```
[datetime] INFO  webservice.SPARQLRestAPI.getTriplesXMLForGet:311 [] - getTriplesXMLForGet
[datetime] INFO  webservice.SPARQLResult.getResultFormat:90 [] - Endpoint URL: http://localhost:8080/sparql
[datetime] INFO  webservice.SPARQLResult.getResultFormat:96 [] - Query: SELECT * { ?s ?p ?o } LIMIT 1
Endpoint HTTP Request
header: Accept: */*
header: User-Agent: Wget/1.21.3
header: Connection: keep-alive
header: Host: localhost:8080
header: Accept-Encoding: identity
param: query=SELECT * { ?s ?p ?o } LIMIT 1
param: mode=trace
[datetime] INFO  webservice.EventManager.log:72 [] - Workflow Context:
st:remoteHost : "[0:0:0:0:0:0:0:1]"
st:service : "http://ns.inria.fr/sparql-template/default"
mode : "("trace" )"^^dt:list
request : "[org.eclipse.jetty.server.Request:Request(GET http://localhost:8080/sparql?query=SELECT%20*%20%7B%20?s%20?p%20?o%20%7D%20LIMIT%201&mode=trace)@11b799cb]"^^dt:pointer
trace : true
url : <http://localhost:8080/sparql>
user query: true
level: PRIVATE

[datetime] INFO  webservice.EventManager.log:73 [] - {st:count="[Map: size=2]"^^dt:map, st:date="[Map: size=2]"^^dt:map, st:host="[Map: size=2]"^^dt:map, st:hostlite="[Map: size=1]"^^dt:map}
[datetime] INFO  webservice.EventManager.log:74 [] - {st:sparql=2, "http://ns.inria.fr/sparql-template/default"=19}
[datetime] INFO  webservice.EventManager.log:76 [] - {"127.0.0.1"=11}
SPARQL endpoint
select * 
where {
  ?s ?p ?o .
}
limit 1 
01 ?s = <http://linkedgeodata.org/ontology/RailwayConstruction>; ?p = rdf:type; ?o = owl:Class; 

service result: 
<?xml version="1.0" ?>
<sparql xmlns='http://www.w3.org/2005/sparql-results#'>
<head>
<variable name='s'/>
<variable name='p'/>
<variable name='o'/>
</head>
<results>
<result>
<binding name='s'><uri>http://linkedgeodata.org/ontology/RailwayConstruction</uri></binding>
<binding name='p'><uri>http://www.w3.org/1999/02/22-rdf-syntax-ns#type</uri></binding>
<binding name='o'><uri>http://www.w3.org/2002/07/owl#Class</uri></binding>
</result>
</results>
</sparql>
```

## Client URL Parameter

In this section we consider service URL parameters decoded by SPARQL service interpreter when calling a service.

```
service <url?param=value> { BGP }
```
Such parameters are used to modify the way the Corese server will interact with the remote service. For example, we can specify the format of the query result returned by the service.

### Standard parameters
Standard dataset uri parameters are available. Hence we can specify a dataset for a service.

```
default-graph-uri = uri
named-graph-uri   = uri
```

### HTTP method
`method` specifies HTTP GET or POST method for calling the service.
```
method = get | post
```


`header` cna be used to specify any HTTP header parameter.
```
header=name:value
```
For example, to specify the HTTP Accept header:
```sparql
SELECT * { 
    ?s ?p ?o . 
    SERVICE <http://examp.le/sparql?header=Accept:application/json> { 
        ?o a ?c 
    } 
} LIMIT 1
```

This previous query is equivalent to the usage of `format=json`. `format` can be used to specify the HTTP Accept header. 
Specify the format of the service query result returned by the endpoint using content negotiation.
```
format = xml | json
```

### Trace mode
`mode` in client URL parameter is equivalent to `mode` in server URL parameter. It specifies the trace mode of the service. It add to the trace trace intermediate results of service, and shows the string result returned by service.
```
mode = debug | trace
```


The `trap` mode "traps" syntax error when parsing service query results and in case of an error, return a subset of results if possible.
```
mode = trap
```

### Bindings

#### Bindings values transmission
`binding` specifies the syntax used for variable bindings sent with the service. Variable bindings are the results of intermediate statement evaluation that can be passed as argument of the service.

```
binding = filter | values 
```

`binding=filter` generates bindings with the following syntax:
```
filter (?x = x1 && ?y = y1)
```

`binding=values` generates bindings with the following syntax:
```
values (?x ?y) { (x1 y1) }
```

For example, with the following data in the local endpoint:

```turtle
@prefix : <http://example.org/> .

ex:John :name "John" .
```
if the following query is executed:

```sparql
PREFIX : <http://example.org/>
SELECT ?x ?age {
    ?x :name ?name .
    SERVICE <http://example.org/sparql?binding=values> {
        ?x :name ?name ;
            :age ?age .
    }
}
```

then the query sent to the remote endpoint will be:

```sparql
PREFIX : <http://example.org/>
SELECT * {
    VALUES ?name { "John" }
    ?x :name ?name ;
        :age ?age .
}
```

#### Bindings variable selection
`focus` and `skip` specify variables that must or must not be passed as variable bindings to the service.
```
focus=x
skip=y
```

#### Bindings in-scope
In order to have their bindings passed into a service clause, variables must be [in-scope](https://www.w3.org/TR/sparql11-query/#variableScope) in the service statement. When a variable is not in-scope, it is possible to make it in-scope with a values expression in the service statement.
```
values var { undef }
```

#### Binding slicing
Specify the size of the slice of intermediate results variable bindings sent with the service. Default is 20 sets of variable bindings (i.e. from 20 intermediate results).

```
slice = integer
```


Specify a limit for the number of results returned by the service.

```
limit = integer
```


Specify a timeout in millisecond for the service.

```
timeout = 123
```


### Exotic Extension

Any parameter value can be a LDScript global variable. The SPARQL interpreter evaluates the variable at runtime and replaces it by its value.  

```
param={?paramValue}
```


Remote server return an RDF document instead of SPARQL query results. Parse the RDF document, create an RDF graph, evaluate service BGP locally on the RDF graph.

```
mode=construct
```


Remote server return a document (e.g. JSON) instead of SPARQL query results. Parse the document using a LDScript function that returns an RDF graph. Evaluate service BGP locally on the graph returned by the function.

```
wrapper=functionNameURI
```


## Service Log

Obtain additional information about query execution and query results such as explanation, trace, etc.

### Log

Detailed log of federated query execution returned as Linked Result RDF/Turtle document, with source selection, rewritten query, intermediate query results.

```
mode=log
```


Query string returned as Linked Result.

```
mode=logquery
```


### Explain

Explain why federated query fail. Save intermediate query and results as Linked Result documents. Return one JSON object that contains the list of URLs of these Linked Results. This mode is processed by corese GUI.

```
mode=why
```


Show where query fail: display last executed statement.

```
mode=explain
```


### Message

Return a JSON object message as Linked Result. JSON message contain the Context, the date, execution time. It contains also endpoint exceptions and service that fail in case of federated query. Message is displayed by corese GUI. It is possible to obtain a message systematically by specifying the default mode as such (see below).

```
mode=message
```


## Service Extension

### Endpoint URL Default Parameter

Define default parameter values for SPARQL endpoint URL in urlprofile.ttl. Mode *, if any, is applied to every service. Parameter "document" is an URL that is added in Query Results "link" tag.

```
[] st:mode "*" ;
st:param (
("mode" "message") ("document" <http://corese.inria.fr>)
)
.
```


Define parameter values associated to specific mode.

```
[] st:mode "map" ;
st:param (("mode" "link") ("transform" stm:mapper)) 
.
```


Define service URL with predefined parameter values.

```
<http://localhost:8080/halopendata/psparql>
st:param (("mode" "map"))
.
```


### Federated SPARQL endpoint

A federated SPARQL endpoint is an endpoint who dispatches a SPARQL query to several endpoints member of a federation. It processes and returns the union of the query results, processing aggregates, if any, on the union of the results. It is equivalent to a query with a union of service clauses on every endpoint of the federation.  
A federation is an URL associated to a list of SPARQL endpoints. It is defined using a Turtle format configuration file, as shown below.

```
<http://corese.inria.fr/d2kab/sparql> a st:Federation ;
st:definition (
<http://147.100.179.235:8082/blazegraph/namespace/AnaEE_sites/sparql>
<https://ico.iate.inra.fr/fuseki/annotation/query> 
<http://taxref-graphdb.i3s.unice.fr/repositories/geco>
)
```


The idea is that a federation is hidden behind a single SPARQL endpoint URL.  
The provenance parameter returns the URL of the target endpoint for each result.

```
http://corese.inria.fr/d2kab/sparql?mode=provenance&
query=select * where { ?s rdfs:label ?l filter regex(?l, "bio") } limit 10
```


A variant of federated SPARQL endpoint splits and rewrites the SPARQL query with appropriate service clauses. The endpoint URL is defined with /federate instead of /sparql.

```
http://corese.inria.fr/d2kab/federate?query=select where {}
```


### Federated endpoint explain mode

Federated endpoint with mode explain generate Linked Result for source selection query and results, rewritten federated query. Linked Result also for intermediate service call and service results.  
It works for federated engine but also for sparql engine with a standard query with services, sent to corese server with /sparql?mode=why.  
The interpreter logs intermediate services and results and at the end, in case of mode=why, it generates Linked Results.

Corese GUI display Linked Results in several query panels with their results.  
Intermediate service call can be executed again in GUI.  
GUI can save and load query results with Linked Result. Hence we can keep track of federated query results during the lifetime of the endpoint because documents are managed on server side in temporary files.

```
mode=why
```


### Compiler Service

Compile a federated query as 1) select source query, 2) federated query with service clauses. Return result of select query. Generate two link href documents for select and federated query.

```
mode=compile

<link href="http://corese.inria.fr/log/select-f"/>
<link href="http://corese.inria.fr/log/rewrite-f"/>
```


### Evaluation Report Service

A report can be generated for federated query. Report is stored in a document and an URL for this document is stored in the link href tag of the query result.

```
mode=log

<link href="http://corese.inria.fr/log/log-f1.ttl"/>
```


### Transformation Service

SPARQL endpoint where the result of the SPARQL query is transformed using an STTL transformation specified using a transform parameter.  
There may be several transformation parameters.  

Specific transformation URI for sparql query result format.

```
transform=st:xml | st:json | st:rdf | st:all
```


#### Linked Result

The result of a transformation may be stored in a document and an URL for this document is stored in the link href tag of the query result.

```
mode=link

<link href="http://corese.inria.fr/log/xml-f2"/>
```


#### Transformation

The stm:mapper transformation generates a map when query solution contains variables "location", "lat", and "lon".

```
transform=stm:mapper
```


Transformation d3:chart for graphic chart

```
transform=d3:chart
```


Transformation d3:hierarchy for class hierarchy, d3:graphic for graph, d3:all for both.

```
transform=d3:hierarchy
transform=d3:graphic
transform=d3:all
```


### SHACL Service

Execution of SHACL shapes and execution of a SPARQL query on the SHACL validation report graph. Parameter shacl-shape-url is the URL of a SHACL document that contains the shapes to be evaluated.

```
/sparql?
mode=shacl&
uri=shacl-shape-url&
query=select * where { ?report sh:conforms ?b }
```


### Service with Before & After

Exemple of service where queries are executed before and after the main query. Parameters uri are URL of SPARQL query documents.

```
mode=before&uri=url1&mode=after&uri=url2
```
