# Getting Started With Corese-server

This tutorial shows how to use the basic features of the Corese-server framework.

1. [Getting Started With Corese-server](#getting-started-with-corese-server)
   1. [1. Load data](#1-load-data)
      1. [1.1. Command line](#11-command-line)
      2. [1.2. Profile file](#12-profile-file)
   2. [2. Create multiple endpoints](#2-create-multiple-endpoints)
      1. [2.1. Multiple endpoints with different data](#21-multiple-endpoints-with-different-data)
   3. [3. Restrict access to external endpoints](#3-restrict-access-to-external-endpoints)
   4. [4. To go deeper](#4-to-go-deeper)

## 1. Load data

There are two methods to load data into the Corese-server: command line and profile file.
The two examples below show how to load data from a file named "beatles.ttl".

### 1.1. Command line

To load data with command line use the `-l` option.

```shell
java -jar corese-server.jar -l "[…]/beatles.ttl"
```

> It's also possible to load data from several files or URL.
> 
> E.g: `java -jar corese-server.jar -l "./file_1.ttl" -l "file_2.ttl" -l "http://file_3.ttl"`.

### 1.2. Profile file

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

## 2. Create multiple endpoints

### 2.1. Multiple endpoints with different data

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

## 3. Restrict access to external endpoints

It is possible to allow access to external endpoints by defining a list of authorized terminals in the profile.

```turtle
prefix st: <http://ns.inria.fr/sparql-template/> 

# List external endpoints allowed
st:access st:namespace
    <http://fr.dbpedia.org/sparql>,
    <http://dbpedia.org/sparql>,
    <https://query.wikidata.org/sparql>.
```

## 4. To go deeper

- Technical documentation : <https://files.inria.fr/corese/doc/server.html>
