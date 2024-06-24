# Federated Queries and Federation

Corese facilitates Federated Queries, enabling users to execute SPARQL queries seamlessly across multiple data sources or endpoints. This document guides you through utilizing Federated Queries and Federation in Corese and details the configuration necessary.

1. [Federated Queries and Federation](#federated-queries-and-federation)
   1. [1. Federated Queries](#1-federated-queries)
   2. [2. Federations](#2-federations)

## 1. Federated Queries

**Basic syntax:**

Use the `@federate` directive to specify different endpoints. Below is the basic syntax:

```sparql
@federate <uri1endpoint1> <uri2endpoint2> …
select * where {?x ?p ?y}
```

**Retrieving Provenance Information:**

To obtain additional details on the origin of the data, include the `@provenance` keyword:

```sparql
@federate <uri1endpoint1> <uri2endpoint2> …
@provenance
select * where {?x ?p ?y}
```

**Configuration for Corese-Server:**

In Corese-Server, it's necessary to explicitly specify the list of allowed endpoints. For more details, refer to [Restrict Access to External Endpoints](https://github.com/Wimmics/corese/blob/master/docs/getting%20started/Getting%20Started%20With%20Corese-server.md#4-restrict-access-to-external-endpoints).

For non-public servers, the `-su` option can be used to allow all endpoints:

```shell
java -jar corese-server.jar -su
```

This option executes the server in superuser mode, allowing connections to all endpoints. This setting is not recommended for public servers due to security concerns.

## 2. Federations

A Federation in Corese is a named set of endpoints, defined in a file to avoid the repetition of listing endpoints in each query.

**Defining a Federation:**

1. Create a `federation.ttl` file with the following content:

```turtle
# Define a federation
<http://myserver.fr/myname/federate> a st:Federation ;
    st:definition (
        <endpoint1> 
        <endpoint2>
    ).
```

2. Next, create a `config.properties` file with the following line

```properties
FEDERATION = /path/to/federation.ttl
```

3. Launch Corese using the `config.properties` file:

```shell
java -jar corese-server.jar -init config.properties
```

```shell
java -jar corese-gui.jar -init config.properties
```

```shell
echo "" | java -jar corese-command.jar sparql -if turtle -q ./query.rq --init config.properties
```

> Note: `echo ""` and `-if` turtle are workaround methods as this command is not designed to function without input.

4. Finally, execute a federated query using the federation:

```sparql
@federation <http://myserver.fr/myname/federate>
select * where {?x ?p ?y}
```

<!-- **Execute a query without `@federation`:**

In Corese-Server, it's possible to execute a federated query without the `@federation` directive by defining the federation in the config file as shown above.

Then, a SPARQL query sent to the Corese SPARQL endpoint URL, <http://myserver.fr/myname/federate>, will be processed as a federated query. -->
