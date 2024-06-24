.. _corese-command-shacl:

shacl
=====

The ``shacl`` command allows validation of RDF files against SHACL shapes.

**Usage:**

.. code-block:: bash

    corese-command shacl [-hRvw]
                                [-a=<shapesFormat>]
                                [-i=<rdfData>...] [-f=<inputFormat>]
                                [-o=<output>]  [-r=<outputFormat>] 
                                [-c=<configFilePath>] 
                                -s=<shaclShapes>  [-s=<shaclShapes>...] 
                               

**Options and arguments:**

- `-s`, `\-\-shapes` `<file|URL>` : Required path or URL of the file containing the SHACL shapes.
- `-a`, `-sf`, `\-\-shape-format` `<format>` : Serialization format of the SHACL shapes. Supported shape formats are listed :ref:`below <corese-command-shacl-input-formats>`.

- `-i`, `\-\-input-data` `<file|dir|pattern|URL>` : Optional path to a file, directory, filename pattern, or URL containing the RDF data. Default: standard input.
- `-f`, `-if`, `\-\-input-format` `<format>` : Input format is automatically detected for files. Use this option with the standard input or if you want to force the input file format. Supported input formats are listed :ref:`below <corese-command-convert-input-formats>`.
- `-R`, `\-\-recursive` : Recursively input all the files in the input directory and sub-directories.

- `-o`, `\-\-output-data` `<file>` : Optional file path to save the validation report. Default: standard output.
- `-r`, `-of`, `\-\-result-format` `<format>` : Optional validation report format. Default: turtle.  Supported report formats are listed :ref:`below <corese-command-shacl-output-formats>`. 

- `-c`,  `\-\-config`, `\-\-init` `<path>` : Optional path to the configuration file. Default: `config.properties` file in the current directory. Is this true??
- `-w`, `\-\-no-owl-import` : Disables the automatic import of referenced ontologies specified in 'owl:imports' statements in the `profile.ttl` file. Default: enabled.

- `-v`, `\-\-verbose` : Display verbose output.
- `-h`, `\-\-help`: Display  `sparql` comamnd options. 



**Example:**

To run this example you can download the sample data file :download:`beatles.rdf <../_static/data/beatles.rdf>` and the sample shapes file :download:`album_shapes.ttl <../_static/data/album_shapes.ttl>`.

The shapes file validates that each album has at least one track. The data file contains three albums, one with a track and two without. It also validates that all the songs have an integer length. The data file has one song that has a length.

.. code-block:: bash

    corese-command shacl -i beatles.rdf -s album_shapes.ttl 


.. code-block:: turtle

    @prefix xsh: <http://www.w3.org/ns/shacl#> .
    @prefix ns1: <http://example.com/> .
    @prefix sh: <http://www.w3.org/ns/shacl#> .
    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

    <urn:uuid:09fa0411-395d-4a5e-8013-4b2c2d7c4586> a sh:ValidationResult ;
    sh:focusNode ns1:McCartney ;
    sh:resultMessage "Fail at: [sh:class <http://example.com/Song> ;
                                sh:minCount 1 ;
                                sh:path <http://example.com/track>]" ;
    sh:resultPath ns1:track ;
    sh:resultSeverity sh:Violation ;
    sh:sourceConstraintComponent sh:MinCountConstraintComponent ;
    sh:sourceShape _:b2 ;
    sh:value 0 .

    <urn:uuid:804cd082-d664-45af-8e7c-30562ec3da1c> a sh:ValidationResult ;
    sh:focusNode ns1:Imagine ;
    sh:resultMessage "Fail at: [sh:class <http://example.com/Song> ;
                                sh:minCount 1 ;
                                sh:path <http://example.com/track>]" ;
    sh:resultPath ns1:track ;
    sh:resultSeverity sh:Violation ;
    sh:sourceConstraintComponent sh:MinCountConstraintComponent ;
    sh:sourceShape _:b2 ;
    sh:value 0 .

    _:bb2 a sh:ValidationReport ;
    sh:conforms false ;
    sh:result <urn:uuid:09fa0411-395d-4a5e-8013-4b2c2d7c4586> ;
    sh:result <urn:uuid:804cd082-d664-45af-8e7c-30562ec3da1c> .
   

For more examples, see the `Getting Started Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html#the-shacl-command>`_.

.. _corese-command-shacl-input-formats:
Input formats
^^^^^^^^^^^^^^^^

- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`
- RDFa/HTML: `rdfa`, `html`, `application/xhtml+xml`

.. _corese-command-shacl-output-formats:
Output (report) formats
^^^^^^^^^^^^^^
- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`

