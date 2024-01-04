<!-- markdownlint-configure-file { "MD004": { "style": "consistent" } } -->

<!-- markdownlint-disable MD033 -->
<!-- markdownlint-disable MD012 -->
<!-- markdownlint-disable MD041 -->

[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.10376241.svg)](https://doi.org/10.5281/zenodo.10376241) [![SWH](https://archive.softwareheritage.org/badge/swh:1:dir:7ec9a0bcccba1254457985de26d524d0d5c4a9c5/)](https://archive.softwareheritage.org/swh:1:dir:7ec9a0bcccba1254457985de26d524d0d5c4a9c5;origin=https://github.com/Wimmics/corese;visit=swh:1:snp:191fffb23bb08ffc9fd187019982de9d5786861e;anchor=swh:1:rev:f0c09b74b609c395f084e7d069d4b54c575ee20d) [![License: CECILL-C](https://img.shields.io/badge/License-CECILL--C-blue.svg)](https://cecill.info/licences/Licence_CeCILL-C_V1-en.html) [![Discussions](https://img.shields.io/badge/Discussions-GitHub-blue)](https://github.com/Wimmics/corese/discussions)




<p align="center">
    <a href="https://project.inria.fr/corese/">
        <img src="https://user-images.githubusercontent.com/5692787/151987397-316a61f0-8098-4d37-a4e8-69180e33261a.svg" width="300" height="149" alt="Corese-logo">
    </a>
    <br>
    <strong>Software platform for the Semantic Web of Linked Data</strong>
</p>
<!-- markdownlint-enable MD033 -->

Corese is a software platform implementing and extending the standards of the Semantic Web. It allows to create, manipulate, parse, serialize, query, reason and validate RDF data.

Corese implement W3C standards [RDF](https://www.w3.org/RDF/), [RDFS](https://www.w3.org/2001/sw/wiki/RDFS), [SPARQL1.1 Query & Update](https://www.w3.org/2001/sw/wiki/SPARQL), [OWL RL](https://www.w3.org/2005/rules/wiki/OWLRL), [SHACL](https://www.w3.org/TR/shacl/) …
It also implements extensions like [STTL SPARQL](https://files.inria.fr/corese/doc/sttl.html), [SPARQL Rule](https://files.inria.fr/corese/doc/rule.html) and [LDScript](https://files.inria.fr/corese/doc/ldscript.html).

There are several interfaces for Corese:

- **Corese-library:** Java library to process RDF data and use Corese features via an API.
- **Corese-server:** Tool to easily create, configure and manage SPARQL endpoints.
- **Corese-gui:** Graphical interface that allows an easy and visual use of Corese features.
- **Corese-Command:** Command Line Interface for Corese that allows users to interact with Corese features from the terminal.
- **Corese-Python (beta):** Python wrapper for accessing and manipulating RDF data with Corese features using py4j.

## Download and install

### Corese-library

- Download from [maven-central](https://central.sonatype.com/namespace/fr.inria.corese)

```xml
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-core</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- jena storage -->
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-jena</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- rdf4j storage -->
<dependency>
    <groupId>fr.inria.corese</groupId>
    <artifactId>corese-rdf4j</artifactId>
    <version>4.5.0</version>
</dependency>
```

- Documentation: [Getting Started With Corese-library](/docs/getting%20started/Getting%20Started%20With%20Corese-library.md)

### Corese-server

- Download from [Docker-hub](https://hub.docker.com/r/wimmics/corese)

```sh
docker run --name my-corese \
    -p 8080:8080 \
    -d wimmics/corese
```

- Alternatively, download [Corese-server jar file](https://project.inria.fr/corese/jar/).

```sh
wget "files.inria.fr/corese/distrib/corese-server-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-server-4.5.0.jar"
```

- Documentation:
  - [Getting Started With Corese-server](/docs/getting%20started/Getting%20Started%20With%20Corese-server.md)
  - [Use Corese-server with Python](/docs/corese-python/Corese-server%20with%20Python.md)

### Corese-GUI

- Download on Flathub
<!-- markdownlint-disable MD033 -->
<a href='https://flathub.org/apps/fr.inria.corese.CoreseGui'>
   <img src='https://dl.flathub.org/assets/badges/flathub-badge-en.png' alt='Download on Flathub' width='175'/>
</a>
<!-- markdownlint-enable MD033 -->

- Or download [Corese-gui jar file](https://project.inria.fr/corese/jar/).

```sh
wget "files.inria.fr/corese/distrib/corese-gui-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-gui-4.5.0.jar"
```

### Corese-Command

- Download on Flathub
<!-- markdownlint-disable MD033 -->
<a href='https://flathub.org/apps/fr.inria.corese.CoreseCommand'>
   <img src='https://dl.flathub.org/assets/badges/flathub-badge-en.png' alt='Download on Flathub' width='175'/>
</a>
<!-- markdownlint-enable MD033 -->

- Or download [Corese-command jar file](https://project.inria.fr/corese/jar/).

```sh
wget "files.inria.fr/corese/distrib/corese-command-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-command-4.5.0.jar"
```

- Alternatively, use the installation script for Linux and MacOS systems.

```sh
curl -sSL https://files.inria.fr/corese/distrib/script/install-corese-command.sh | bash
```

To uninstall:

```sh
curl -sSL https://files.inria.fr/corese/distrib/script/uninstall-corese-command.sh | bash
```

> If you're using zsh, replace `bash` with `zsh`.

- Documentation: [Getting Started With Corese-command](/docs/getting%20started/Getting%20Started%20With%20Corese-command.md)

### Corese-Python (beta)

- Download [Corese-python jar file](https://project.inria.fr/corese/jar/).

```sh
wget "files.inria.fr/corese/distrib/corese-library-python-4.5.0.jar"
java -jar "-Dfile.encoding=UTF8" "corese-library-python-4.5.0.jar"
```

- Documentation: [Getting Started With Corese-python](/docs/corese-python/Corese-library%20with%20Python.md)

## Compilation from source

Download source code and compile.

```shell
git clone "https://github.com/Wimmics/corese.git"
cd corese
mvn clean install -DskipTests
```

## How to cite Corese

Use the "Cite this repository" option on the right side of this page.

## Contributions and discussions

For support questions, comments, and any ideas for improvements you'd like to discuss, please use our [discussion forum](https://github.com/Wimmics/corese/discussions/).
We welcome everyone to contribute to [issue reports](https://github.com/Wimmics/corese/issues), suggest new features, and create [pull requests](https://github.com/Wimmics/corese/pulls).

## General informations

- [Corese website](https://project.inria.fr/corese)
- [Documentation](https://project.inria.fr/corese/documentation/)
- [Source code](https://github.com/Wimmics/corese)
- [Corese server demo](http://corese.inria.fr/)
- [Changelog](https://github.com/Wimmics/corese/blob/master/CHANGELOG.md)
- **Mailing list:** corese-users at inria.fr
- **Subscribe to the mailing list:** corese-users-request at inria.fr **subject:** subscribe
