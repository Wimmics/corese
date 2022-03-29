<!-- markdownlint-configure-file { "MD004": { "style": "consistent" } } -->
<!-- markdownlint-disable MD033 -->

#

<p align="center">
    <a href="https://project.inria.fr/corese/">
        <img src="https://user-images.githubusercontent.com/5692787/151987397-316a61f0-8098-4d37-a4e8-69180e33261a.svg" width="300" height="149" alt="Corese-logo">
    </a>
    <br>
    <strong>Software platform for the Semantic Web of Linked Data</strong>
</p>
<!-- markdownlint-enable MD033 -->

Corese is a software platform implementing and extending the standards of the Semantic Web. It allows to create, manipulate, parse, serialize, query, reason and validate RDF data.

Corese implement W3C standarts [RDF](https://www.w3.org/RDF/), [RDFS](https://www.w3.org/2001/sw/wiki/RDFS), [SPARQL1.1 Query & Update](https://www.w3.org/2001/sw/wiki/SPARQL), [OWL RL](https://www.w3.org/2005/rules/wiki/OWLRL), [SHACL](https://www.w3.org/TR/shacl/) …
It also implements extensions like [STTL SPARQL](https://files.inria.fr/corese/doc/sttl.html), [SPARQL Rule](https://files.inria.fr/corese/doc/rule.html) and [LDScript](https://files.inria.fr/corese/doc/ldscript.html).

There are three versions of Corese:

- **Corese-library:** Java library to process RDF data and use Corese features via an API.
- **Corese-server:** Tool to easily create, configure and manage SPARQL endpoints.
- **Corese-gui:** Graphical interface that allows an easy and visual use of Corese features.

## Download and install

### Corese-library

- Download from [maven-central](https://search.maven.org/search?q=g:fr.inria.corese)

```xml
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-core</artifactId>
    <version>4.3.0</version>
</dependency>

<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-rdf4j</artifactId>
    <version>4.3.0</version>
</dependency>
```

Documentation: [Getting Started With Corese-library](https://notes.inria.fr/s/hiiedLfVe#)

### Corese-server

- Download from [Docker-hub](https://hub.docker.com/r/wimmics/corese)

```sh
docker run --name my-corese \
    -p 8080:8080 \
    -d wimmics/corese
```

- Download [Corese-server jar file](https://project.inria.fr/corese/download/).

```sh
# Replace ${VERSION} with the desired version number (e.g: 4.3.0)
wget "files.inria.fr/corese/distrib/corese-server-${VERSION}.jar"
java -jar "corese-server-${VERSION}.jar"
```

Documentation: [Getting Started With Corese-server](https://notes.inria.fr/s/SoyFglO_1#)

### Corese-GUI

- Download [Corese-gui jar file](https://project.inria.fr/corese/download/).

```sh
# Replace ${VERSION} with the desired version number (e.g: 4.3.0)
wget "files.inria.fr/corese/distrib/corese-gui-${VERSION}.jar"
java -jar "corese-gui-${VERSION}.jar"
```

## Compilation from source

Download source code and compile.

```shell
git clone "https://github.com/Wimmics/corese.git"
cd corese
mvn -Dmaven.test.skip=true package
```

## Contributions and discussions

For support questions, comments, and any ideas for improvements you'd like to discuss, please use our [discussion forum](https://github.com/Wimmics/corese/discussions/).
We welcome everyone to contribute to [issue reports](https://github.com/Wimmics/corese/issues), suggest new features, and create [pull requests](https://github.com/Wimmics/corese/pulls).

## General informations

- [Corese website](https://project.inria.fr/corese)
- [Source code](https://github.com/Wimmics/corese)
- [Corese server demo](http://corese.inria.fr/)
- [Changelog](https://notes.inria.fr/s/TjriAbX14#)
- **Mailing list:** corese-users at inria.fr
- **Subscribe to the mailing list:** corese-users-request at inria.fr **subject:** subscribe
