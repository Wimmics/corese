<!-- markdownlint-disable MD024 -->
# Corese Changelog

## Unreleased

### Added

- Support for [RDF*](https://w3c.github.io/rdf-star/cg-spec/2021-07-01.html) (beta)
- Support for [SPARQL*](https://w3c.github.io/rdf-star/cg-spec/2021-07-01.html) (beta)

### Fixed

- Encoding error with Windows when exporting graphs from Corese-Gui.

## 4.3.0 – 2021/01/18 – RDF4J Update

### Added

- Graphical editor for SHACL file in Corese-GUI.
- Graphical editor for TURTLE file in Corese-GUI.
- Save graph option un Corese-GUI.
- New type of graph [´CoreseModel´](https://notes.inria.fr/s/OB038LBLV#) implementing the [RDF4J](https://rdf4j.org/) [model API](https://rdf4j.org/javadoc/latest/).
- `DataManager` API to allows to connect the Corese SPARQL engine with other triple storage stucture.
- Implement a `DataManager` for RDF4J.

### Security

- Fix Log4j security vulnerabilities.
