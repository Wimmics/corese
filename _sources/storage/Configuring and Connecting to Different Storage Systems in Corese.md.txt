# Configuring and Connecting to Different Storage Systems in Corese

Corese offers the possibility to connect to a range of storage systems for storing and managing your RDF data. In this document, you'll find information on how to use these storage systems with Corese, including instructions on configuring and utilizing them in the GUI, Server, and Library.

## 1. Introduction

In Corese versions prior to 4.4, graphs were loaded and manipulated in RAM. With the introduction of Data Manager in version 4.4, the Corese engine and storage systems are now fully decoupled. This offers several advantages such as :

- The ability to easily add new storage systems
- The use of persistent storage systems
- The simultaneous use of multiple storage systems
- The flexibility to choose a storage system that meets specific needs

## 2. What is a Data Manager?

A data manager in Corese is a bridge between the Corese engine and a storage system, enabling the engine to store and manage graph data in a variety of storage systems while abstracting away the underlying details of those systems.

A data manager is implemented as an interface called `Datamanager`, and concrete implementations such as `JenaTdb1DataManager` and `Rdf4jModelDataManager` are provided for specific storage systems. By implementing the `Datamanager` interface, it is possible to use the Corese engine with any storage structure.

Currently, there are three implementations of `Datamanager` available:

- `JenaTdb1DataManager` for Jena TDB1 storage
- `Rdf4jModelDataManager` for RDF4J model
- `CoreseGraphDataManager` for Corese graph

## 3. Available Data Manager Implementations

### 3.1. Jena TDB1

[Jena](https://jena.apache.org/) is an open-source Semantic Web framework written in Java and developed by the [Apache Jena project](https://jena.apache.org/). It provides a set of libraries and tools for building applications that process and manipulate RDF data.

[TDB](https://jena.apache.org/documentation/tdb/) is a native triple store for Jena, designed to efficiently store and query large amounts of RDF data. It supports the full range of RDF languages and standards.

The `JenaTdb1DataManager` allows the Corese engine to connect to a TDB1 database. Corese can connect to an existing Jena TDB1 database or create a new one.

TDB1 is a persistent storage system that supports transactions and native concurrent access. In our larger tests, it has been able to efficiently load and manage graphs with up to 600 million triples. However, it is likely capable of handling even larger graph sizes.

Here is a summary of TDB1's characteristics:

| Characteristic           | Description |
| ------------------------ | ----------- |
| Persistence of data      | Yes         |
| Native concurrent access | Yes         |

> You can use all the native tools and [Apache Jena - TDB Command-line Utilities](https://jena.apache.org/documentation/tdb/commands.html) . For example, you can use `tdbloader`, a software, to efficiently create a TDB1 database from serialized RDF data.

### 3.2. Corese Graph

Corese Graph is the historical API of Corese for storing and manipulating RDF data in memory. It is stable and optimized, and capable of handling large graphs within the limits of available RAM.

However, Corese Graph does not offer persistence of data and supports native concurrent access.

Here is a summary of Corese Graph's characteristics:

| Characteristic           | Description |
| ------------------------ | ----------- |
| Persistence of data      | No          |
| Native concurrent access | Yes         |

### 3.3. RDF4J Model

[RDF4J](https://rdf4j.org/) is an open-source Java library for working with RDF data. It provides a set of APIs for parsing and serializing RDF, querying with SPARQL, and modeling RDF data with RDFS and OWL.

[The RDF Model API](https://rdf4j.org/documentation/programming/model/) is a Java interface for storing and manipulating RDF data in memory (It does not store data on disk). This API provides a high-level, abstract representation of an RDF graph. The `Rdf4JModelDataManager` allows the Corese engine to connect to an existing RDF4J `Model` object or create a new one.

This implementation is not optimized for storing large amounts of data and does not support persistence of data, concurrency, or transactions. It was our first implementation as a proof-of-concept and is not recommended for use in production environments. However, it may still be useful for small-scale testing and development purposes.

Here is a summary of the RDF4J Model's characteristics:

| Characteristic           | Description |
| ------------------------ | ----------- |
| Persistence of data      | No          |
| Native concurrent access | No          |

## 4. Configuring Storage Systems in Corese-GUI and Corese-Server

To configure storage systems in the Corese GUI or Server, it is necessary to create a properties file. This file should include the `STORAGE` configuration property, which specifies the storage systems to use.

To run Corese-GUI or Corese-Server with a property file, the `-init` option must be used. For instance, the following bash command runs Corese-GUI using the `gui.properties` file:

```bash
java -jar corese.jar -init "config.properties"
```

This will load the storage systems specified in the `STORAGE` property in the `config.properties` file.

> If no configuration file is specified, Corese will use the default configuration, which is to use a single Corese graph storage system in memory. This behavior is the same as in versions prior to 4.4.

### 4.1. Configuring One Storage System

To configure a single storage system, you need to specify the type and ID of the system in the `STORAGE` property. You can also include optional parameters for the system.

```properties
STORAGE = TYPE_BD1,ID_DB1,PARAM_BD1
```

The fields are as follows:

`TYPE_BD1`: The type of storage system to use. Possible values are `jena_tdb1`, `rdf4j_model`, and `corese_graph`.

`ID_DB1`: The ID of the storage system. This ID will be used to reference the storage system in SPARQL queries.

`PARAM_BD1`: (Optional) Constructor parameter for the storage system.

| DB type      | Parameter                                                                    |
| ------------ | ---------------------------------------------------------------------------- |
| jena_tdb1    | Empty (use JenaTDB in memory) or path of TDB1 database (use JenaTDB in a DB) |
| rdf4j_model  | Empty                                                                        |
| corese_graph | Empty                                                                        |

For example, to configure a Jena TDB1 storage system with ID `musicDB` and the `/…/music` directory as the storage location, the following `STORAGE` property should be specified:

```properties
STORAGE = jena_tdb1,musicDB,/…/music
```

### 4.2. Configuring Multiple Storage Systems

To configure multiple storage systems in Corese, simply separate the configurations for each storage system with a semicolon (`;`). This is similar to configuring a single storage system, as described in the previous section.

```properties
STORAGE = TYPE_BD1,ID_DB1,PARAM_BD1;TYPE_BD2,ID_DB2,PARAM_BD2;…
```

In the case where multiple storage systems are configured, the first storage system listed is the default storage system. It is accessible directly in SPARQL queries, while the other storage systems must be accessed using the `SERVICE` keyword.

For example, given the following configuration:

```properties
STORAGE = corese_graph,friend;jena_tdb1,mélomane;jena_tdb1,music
```

The following SPARQL query retrieves information about a person's friends and the music they like:

```sparql
PREFIX music: <http://example.com/music/>
PREFIX person: <http://example.com/person/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

SELECT ?friendName ?artistName ?description
WHERE {
    # Select Casandra's friends from the "friend" database (default dataset)
    person:Casandra foaf:knows ?friend .

    # Retrieve the Casandra's friend's name and the artist they like from the "mélomane" database
    SERVICE <store:mélomane> {
        ?friend person:likeMusic ?artist .
        ?friend foaf:firstName ?friendName .
    }

    # Retrieve the artist's name and a description of their music from the "music" database
    SERVICE <store:music> {
        ?artist music:name ?artistName .
        ?artist music:description ?description .
    }
}
```

To execute a query in the GUI, open the Corese GUI and enter the query in the SPARQL Query tab.

To execute in the Server, send a request to the endpoint `http://localhost:8080/sparql` if you are running it locally.

### 4.3. [Optional] Assign storage to a specific SPARQL endpoint with Corese Server

Corese Server allows you to assign storage to a specific SPARQL endpoint by define a properties file (eg: `server.properties`) and a profile file (eg: `profile.ttl`). The properties file defines the storage systems available and their locations, while the profile file assigns a storage system to a specific endpoint.

To create two storage systems, `db1` and `db2`, using the Jena TDB1 storage system and located at `/…/album` and `/…/music`, respectively, you can use the following in the properties file:

```properties
STORAGE = jena_tdb1,db1,/…/album;jena_tdb1,db2,/…/music
```

To assign the `db1` and `db2` storage systems to the `album` (`<http://localhost:8080/album/sparql>`) and `music` (`<http://localhost:8080/music/sparql>`) endpoints, respectively, you can use the following in the profile file:

```turtle
prefix st: <http://ns.inria.fr/sparql-template/>

# Album endpoint, available at http://localhost:8080/album/sparql
<#_1> a st:Server;
    st:service "album"; # Assigns the name "album" to this endpoint
    st:storage "db1". # Assigns the "db1" storage system to this endpoint

# Music endpoint, available at http://localhost:8080/music/sparql
<#_2> a st:Server;
    st:service "music"; # Assigns the name "music" to this endpoint
    st:storage "db2". # Assigns the "db2" storage system to this endpoint
```

With this configuration, the endpoint `<http://localhost:8080/album/sparql>` will use `db1` data, and `<http://localhost:8080/music/sparql>` will use `db2` data.

To start the server with these configurations, run the following command:

```bash
java -jar corese-server.jar -init "server.properties" -pp "profile.ttl"
```

> You can learn more about profile files here: [Getting Started With Corese-server](https://github.com/Wimmics/corese/blob/master/docs/getting%20started/Getting%20Started%20With%20Corese-server.md#4-to-go-deeper)

## 5. Use Storage Systems in Corese-Library

To build a `dataManager` using the Corese-Library, you can use a `dataManager` builder class to configure and create the `dataManager`. There are different types of `dataManager` builders available, depending on the type of `dataManager` you want to create.

For example, the `JenaTdb1DataManagerBuilder` can be used to build a `JenaTdb1DataManager`. To build a `JenaTdb1DataManager` with a specific storage path, you can use the following code:

```java
JenaTdb1DataManagerBuilder builder = new JenaTdb1DataManagerBuilder();
builder.setStoragePath("storage/path");
JenaTdb1DataManager dataManager = builder.build();
```

Similarly, you can use the `CoreseGraphDataManagerBuilder` to build a `CoreseGraphDataManager` or the `Rdf4jModelDataManagerBuilder` to build an `Rdf4jModelDataManager`.

To execute a query on the `dataManager`, you can use the `QueryProcess` class as follows:

```java
// Create a QueryProcess using the dataManager
QueryProcess exec = QueryProcess.create(dataManager);

// Execute a SPARQL query and retrieve the result as a Mappings object
Mappings map = exec.query("select * where { ?s ?p ?o }");

// Print the results of the query
for (Mapping m : map) {
    System.out.println(m);
}
```

This will execute the specified SPARQL query on the `dataManager` and print the results.

> You can learn more about Corese-Library here: [Getting Started With Corese-library]([corese/Getting Started With Corese-library.md at master · Wimmics/corese · GitHub](https://github.com/Wimmics/corese/blob/master/docs/getting%20started/Getting%20Started%20With%20Corese-library.md))
