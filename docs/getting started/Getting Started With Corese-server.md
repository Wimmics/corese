# Getting Started With Corese-server

This tutorial shows how to use the basic features of the Corese-server framework.

1. [Getting Started With Corese-server](#getting-started-with-corese-server)
   1. [1. Installation](#1-installation)
   2. [2. Load data](#2-load-data)
      1. [2.1. Command line](#21-command-line)
      2. [2.2. Profile file](#22-profile-file)
   3. [3. Create multiple endpoints](#3-create-multiple-endpoints)
      1. [3.1. Multiple endpoints with different data](#31-multiple-endpoints-with-different-data)
   4. [4. Restrict access to external endpoints](#4-restrict-access-to-external-endpoints)
   5. [5. To go deeper](#5-to-go-deeper)

## 1. Installation

Installations instructions are available on the [Corese-Command GitHub repository](https://github.com/Wimmics/corese).

## 2. Load data

There are two methods to load data into the Corese-server: command line and profile file.
The two examples below show how to load data from a file named "beatles.ttl".

### 2.1. Command line

To load data with command line use the `-l` option.

```shell
java -jar corese-server.jar -l "[…]/beatles.ttl"
```

> It's also possible to load data from several files or URL.
>
> E.g: `java -jar corese-server.jar -l "./file_1.ttl" -l "file_2.ttl" -l "http://file_3.ttl"`.

## 3. Profile file

A profile is a Turtle file that allows users to configure the Corese-server.

```turtle
prefix st: <http://ns.inria.fr/sparql-template/>
prefix sw: <http://ns.inria.fr/sparql-workflow/>

#############
# EndPoints #
#############

# Default EndPoints, available at http://localhost:8080/sparql
st:user a st:Server;
    st:content <#loadBeatles>.

############
# Workflow #
############

<#loadBeatles> a st:Workflow;
    sw:body (
        [
            a sw:Load;
            sw:path <[…]/beatles.ttl>;
        ]
    ).
```

The keyword `st:user` designates the default endpoint available in <http://localhost:8080/sparql>.
In this example, we add on the default endpoint the workflow named `<#loadBeatles>` which loads the file "beatles.tll".
There can be several load in a workflow body.

To load Corese-server with a profile, use the options `-lp -pp "profileFile"`.

```shell
java -jar corese-server.jar -lp -pp "myprofile.ttl"
```

### 3.1 Create multiple endpoints

#### 3.1.1 Multiple endpoints with different data

It is possible to create multiple endpoints with a single Corese-server instance.
The profile file below shows how to create three endpoints and load data into each.

```turtle
prefix st: <http://ns.inria.fr/sparql-template/> 
prefix sw: <http://ns.inria.fr/sparql-workflow/> 

#############
# EndPoints #
#############

# Default endpoint, available in http://localhost:8080/sparql
st:user a st:Server;
    st:content <#loadBeatles>.

# Beatles endpoint, available in http://localhost:8080/person/sparql
<#person> a st:Server;
    st:service "person";
    st:content <#loadPerson>.

# Music endpoint, available in http://localhost:8080/music/sparql
<#music> a st:Server;
    st:service "music";
    st:content <#loadMusic>.

############
# Workflow #
############

<#loadBeatles> a sw:Workflow;
    sw:body (
        [
            a sw:Load;
            sw:path <[…]/beatles.ttl>
        ]
    ).

<#loadPerson> a st:Workflow;
    sw:body (
        [
            a sw:Load;
            sw:path <[…]/person.ttl>
        ]
    ).

<#loadMusic> a sw:Workflow;
    sw:body (
        [
            a sw:Load;
            sw:path <[…]/music.ttl>
        ]
    ).
```

This profile defines three endpoints: the default endpoint (`st:user`), the person (`<#person>`) endpoint and the music endpoint (`<#music>`). Each endpoint is associated with a workflow to load data via the `st:content` property.

The default endpoint (`st:user`) is accessible with the url <http://localhost:8080/sparql>.
The other endpoints (`<#person>` and `<#music>`) are accessible through the URL <http://localhost:8080/${SERVER_NAME}/sparql> where `${SERVER_NAME}` is the value of the `st:service` property (E.g: <http://localhost:8080/music/sparql>).

### 3.2 Restrict access to external endpoints

It is possible to allow access to external endpoints by defining a list of authorized terminals in the profile.

```turtle
prefix st: <http://ns.inria.fr/sparql-template/> 

# List external endpoints allowed
st:access st:namespace
    <http://fr.dbpedia.org/sparql>,
    <http://dbpedia.org/sparql>,
    <https://query.wikidata.org/sparql>.
```

## 4. Property configuration file

The behavior of the Corese-server can be modified by adding options in a properties file. The default properties file is named `corese.properties` and is located in the same directory as the Corese-server jar file. It is possible to specify another properties file with the `-init` option.
An example of properties file is available on the [Corese-Command GitHub repository](https://raw.githubusercontent.com/Wimmics/corese/master/corese-server/build-docker/corese/corese-default-properties.properties).

Here we list only some of the most commonly used properties.

### 4.1. Blank node format
```properties
BLANK_NODE              = _:b
```
`BLANK_NODE` specifies the format of blank nodes. The default value is `_:b`.

### 4.2. Loading in the default graph
```properties
LOAD_IN_DEFAULT_GRAPH   = true
```
By default, the data is loaded into the default graph. If `LOAD_IN_DEFAULT_GRAPH` is set to `false`, the data is loaded into a named graph whose name is the path of the file.
Note that internally, the default graph of the Corese server is named `http://ns.inria.fr/corese/kgram/default`, or `kg:default`.

#### 4.3. RDF* (RDF Star)
```properties
RDF_STAR                = false
```
Corese implements a prototype extension for the RDF* specification. `RDF_STAR` enables this extension.

### 4.4. OWL utilities

```properties
DISABLE_OWL_AUTO_IMPORT = true
```
By default, when a triple with the predicate `owl:imports` is loaded, the Corese-server automatically loads the ontology specified in the object of the triple. If `DISABLE_OWL_AUTO_IMPORT` is set to `true`, the Corese-server does not load the ontology specified in the object of the triple.

### 4.5. SPARQL engine behavior
```properties
SPARQL_COMPLIANT        = false
```
`SPARQL_COMPLIANT` specifies the behavior of the SPARQL engine. If `SPARQL_COMPLIANT` is set to `true`, the SPARQL engine is compliant with the W3C test cases. In practice, this means that the SPARQL engine will consider that two literals are different if they have the same value but different types (E.g: `1` and `"1"^^xsd:integer`).

```properties
REENTRANT_QUERY         = false
```
`REENRANT_QUERY` enables the update during a query. This option was implemented in cooperation with the [SPARQL micro-service project](https://github.com/frmichel/sparql-micro-service).

### 4.6. SPARQL federation behavior
```properties
SERVICE_BINDING     = values 
```
When binding values between clauses from different endpoints, the Corese-server uses the `SERVICE_BINDING` property to specify the method to use. The default value is `values`. The other possible value is `filter`.

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
    SERVICE <http://example.org/sparql> {
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

```properties
SERVICE_SLICE       = 20
```
`SERVICE_SLICE` specifies the number of bindings to send to a remote endpoint. The default value is `20`.

```properties
SERVICE_LIMIT       = 1000
```

`SERVICE_LIMIT` specifies the maximum number of results to return from a remote endpoint. The default value is `1000`. In the previous example, the query sent to the remote endpoint should actually be:

```sparql
PREFIX : <http://example.org/>
SELECT * {
    VALUES ?name { "John" }
    ?x :name ?name ;
        :age ?age .
    LIMIT 1000
}
```
Corese will try to obtain the next 1000 results by sending the same query with the `OFFSET` clause.

```properties
SERVICE_TIMEOUT     = 2000
```
`SERVICE_TIMEOUT` specifies the timeout in milliseconds for a remote endpoint. The default value is `10000`.

### 4.7. SPARQL LOAD parameters
```properties
LOAD_LIMIT   = 10
```
`LOAD_LIMIT` specifies the maximum number of triples to load from a file. This feature is not enabled by default.

```properties
LOAD_WITH_PARAMETER = true
```
`LOAD_WITH_PARAMETER` enables the use of the `LOAD` clause with a parameter. This feature is not enabled by default.

```properties
LOAD_FORMAT   = text/turtle;q=1.0, application/rdf+xml;q=0.9, application/ld+json;q=0.7; application/json;q=0.6
```
```properties
LOAD_FORMAT   = application/rdf+xml
```
If `LOAD_WITH_PARAMETER` is enabled, `LOAD_FORMAT` can be used to specify which mime type should be resquest as format for the loaded data.

## 6. To go deeper

- [Technical documentation](https://files.inria.fr/corese/doc/server.html)
- [Storage](https://github.com/Wimmics/corese/blob/master/docs/storage/Configuring%20and%20Connecting%20to%20Different%20Storage%20Systems%20in%20Corese.md#configuring-and-connecting-to-different-storage-systems-in-corese)


