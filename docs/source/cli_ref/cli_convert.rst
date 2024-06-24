.. _corese-command-convert:

convert
=======


The ``convert`` command allows converting RDF files from one format to another.

**Usage:**

.. code-block:: bash

    corese-command convert [-hvw] [-i=<input>] [-f=<inputFormat>]
                            [-c=<configFilePath>]  [-o[=<output>]]
                             -r=<outputFormat>

**Options and arguments:**

- `-r`, `-of`, `\-\-result-format` `<format>` : Required output format. Supported output formats are listed :ref:`below <corese-command-convert-output-formats>`. 

- `-i`, `\-\-input-data` `<input file|URL>` : Optional path to a file or URL containing the RDF data. Default: standard input.
- `-f`, `-if`, `\-\-input-format` `<format>` : Input format is automatically detected for files. Use this option with the standard input or if you want to force the input file format. Supported input formats are listed :ref:`below <corese-command-convert-input-formats>`.

- `-o`, `\-\-output-data` `<output_file>` : Optional path to save the converted results. Default: standard output.

- `-c`,  `\-\-config`, `\-\-init` `<path>` : Optional path to the configuration file. Default: `config.properties` file in the current directory. Is this true??
- `-w`, `\-\-no-owl-import` : Disables the automatic import of referenced ontologies specified in 'owl:imports' statements in the `profile.ttl` file. Default: enabled.

- `-v`, `\-\-verbose` : Display verbose output.
- `-h`, `\-\-help`: Display  `sparql` comamnd options. 



**Example:**

To run this example you can download the sample data file :download:`beatles.rdf <../_static/data/beatles.rdf>`.

.. code-block:: bash

    corese-command convert -i beatles.rdf -r tutle 


.. code-block:: turtle

    prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
    @prefix ns1: <http://example.com/> .
    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

    ns1:Love_Me_Do ns1:length 125 ;
    ns1:name "Love Me Do" ;
    ns1:writer ns1:John_Lennon ;
    ns1:writer ns1:Paul_McCartney ;
    a ns1:Song .

    ns1:Please_Please_Me ns1:artist ns1:The_Beatles ;
    ns1:date "1963-03-22"^^xsd:date ;
    ns1:name "Please Please Me" ;
    ns1:track ns1:Love_Me_Do ;
    a ns1:Album .

    ...

For more examples, see the `Getting Started Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html#the-convert-command>`_.

.. _corese-command-convert-input-formats:
Input formats
^^^^^^^^^^^^^^^^

- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`
- RDFa/HTML: `rdfa`, `html`, `application/xhtml+xml`

.. _corese-command-convert-output-formats:
Output formats
^^^^^^^^^^^^^^
- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`

