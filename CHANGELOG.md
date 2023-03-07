# Corese Changelog

## 4.4.0 – 2021/02/24 – Storage update

### Added

- Storage Systems in Corese:
  - Integration of Jena TDB1.
  - Integration of Corese Graph.
  - Integration of RDF4J Model.
  - [More information can be found here](https://github.com/Wimmics/corese/blob/master/docs/storage/Configuring%20and%20Connecting%20to%20Different%20Storage%20Systems%20in%20Corese.md).
- Beta support for RDF* and SPARQL* ([Community Group Report 17 December 2021](https://w3c.github.io/rdf-star/cg-spec/2021-12-17.html)).
- Corese Command-Line Interface (Beta):
  - `convert`: Convert RDF files between different serialization formats.
  - `sparql`: Execute SPARQL queries on files.
  - `owlProfile`: Check OWL profiles on files.
  - `ldscript`: Run LDSCRIPT files.
- Corese-Python interface (Beta):
  - [Use the Corese-library with Python](https://github.com/Wimmics/corese/blob/master/docs/corese-python/Corese-library%20with%20Python.md).
  - [Use the Corese-server with Python](https://github.com/Wimmics/corese/blob/master/docs/corese-python/Corese-server%20with%20Python.md).
- Undo/Redo support added to Corese GUI ([Pull Request #97](https://github.com/Wimmics/corese/pull/97) thanks to [@alaabenfatma](https://github.com/alaabenfatma)).

### Changes

- Updated Jetty server library to version 11.0.8.
- Code clean-up, correction, and commenting for improved readability and maintenance.

### Fixed

- Fixed an encoding error when loading a file whose path contains a space in Corese-GUI
- Fixed encoding error with Windows when exporting graphs from Corese-GUI.
- Fixed SPARQL engine bug where it was impossible to load a named graph that contains a non-empty RDF list.
- Fixed issue with "rdf:" not found when sending a federated query to Fuseki. See [issue #114](https://github.com/Wimmics/corese/issues/114).
- Fixed non-standard JSON format on query timeout. See [issue #113](https://github.com/Wimmics/corese/issues/113).
- Fixed inconsistent status of the OWL and Rules checkboxes in Corese-GUI that was not updated during reload. See [issue #110](https://github.com/Wimmics/corese/issues/110).
- Fixed the rule engine that was implementing optimizations incompatible with the `owl:propertyChainAxiom` rule. See [issue #110](https://github.com/Wimmics/corese/issues/110).

### Security

- Bumped testng from 7.3.0 to 7.7.1. See [pull request #118](https://github.com/Wimmics/corese/pull/118).
- Bumped jsoup from 1.14.2 to 1.15.3 in /corese-server. See [pull request #101](https://github.com/Wimmics/corese/pull/101).
- Bumped junit from 4.11 to 4.13.1 in /corese-storage. See [pull request #98](https://github.com/Wimmics/corese/pull/98).
- Bumped xercesImpl from 2.12.0 to 2.12.2. See [pull request #92](https://github.com/Wimmics/corese/pull/92).
- Bumped gremlin-core from 3.2.3 to 3.6.2.

## 4.3.0 – 2021/01/18 – RDF4J Update

### Added

- Graphical editor for SHACL file in Corese-GUI.
- Graphical editor for TURTLE file in Corese-GUI.
- Save graph option un Corese-GUI.
- New type of graph [´CoreseModel´](https://github.com/Wimmics/corese/blob/master/docs/rdf4j/RDF4J%20API%20in%20Corese.md) implementing the [RDF4J](https://rdf4j.org/) [model API](https://rdf4j.org/javadoc/latest/).
- `DataManager` API to allows to connect the Corese SPARQL engine with other triple storage stucture.
- Implement a `DataManager` for RDF4J.

### Security

- Fix Log4j security vulnerabilities.