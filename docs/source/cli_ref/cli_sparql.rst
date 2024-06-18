.. _corese-command-sparql:

sparql
======


The ``sparql`` command allows executing SPARQL queries specifying various options for input and output formats.

**Usage:**

.. code-block:: bash

    corese-command sparql [-hRvw] [-i=<inputs>...] [-f=<inputFormat>] 
                          [-r=<resultFormat>] [-o=<output>] 
                          [-c=<configFilePath>]
                          -q=<queryUrlOrFile>


**Options and arguments:**

- `-q`, `\-\-query` `<string|path|URL>` : Required SPARQL query string or path/URL to a .rq file

- `-i`, `\-\-input-data` `<input file|dir|pattern|URL>` : Optional path to a file, directory, filename pattern, or URL containing the RDF data. Default: standard input.
- `-f`, `-if`, `\-\-input-format` `<format>` : Input format is automatically detected for files. Use this option with the standard input or if you want to force the input file format. Supported input formats are listed :ref:`below <corese-command-sparql-input-formats>`.
- `-R`, `\-\-recursive`: Recursively input all the files in the input directory and sub-directories.

- `-r`, `-of`, `\-\-result-format` `<format>` : Output format of the query results. Supported output formats are listed :ref:`below <corese-command-sparql-output-formats>`. Default: `markdown` table.
- `-o`, `\-\-output-data` `<output_file>` : Optional path to save the query results. Default: standard output.

- `-c`,  `\-\-config`, `\-\-init` `<path>` : Optional path to the configuration file. Default: `config.properties` file in the current directory. Is this true??
- `-w`, `\-\-no-owl-import` : Disables the automatic import of referenced ontologies specified in 'owl:imports' statements in the `profile.ttl` file. Default: enabled.

- `-v`, `\-\-verbose` : Display verbose output.
- `-h`, `\-\-help`: Display  `sparql` command options. 

.. note::
    Multiple files and mixture of file sources can be specified as input data. Each source has to be preceded by the `-i` option.  


**Example:**

To run this example you can download the sample data file :download:`beatles.rdf <../_static/data/beatles.rdf>`.

.. code-block:: bash

    corese-command sparql -q "select * where {?s ?p ?o} limit 2" \
                          -i beatles.rdf \
                          -r tsv 


.. code-block:: 

    ?s      ?p      ?o
    <http://example.com/Please_Please_Me>   <http://example.com/artist>     <http://example.com/The_Beatles>
    <http://example.com/McCartney>  <http://example.com/artist>     <http://example.com/Paul_McCartney>

For more examples, see the `Getting Started Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html#the-sparql-command>`_.

.. _corese-command-sparql-input-formats:
Input formats
^^^^^^^^^^^^^^^^

- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`
- RDFa/HTML: `rdfa`, `html`, `application/xhtml+xml`

.. _corese-command-sparql-output-formats:
Output (results) formats
^^^^^^^^^^^^^^^^^^^^^^^^

Output formats depend on the query form. The following formats are supported:

SELECT, ASK:

- XML: `xml`, `srx` or `application/sparql-results+xml`
- Json: `json`, `srj` or `application/sparql-results+json`
- CSV: `csv` or `text/csv`
- TSV: `tsv` or `text/tab-separated-values`
- Markdown: `markdown`, `md` or `text/markdown

CONSTRUCT, DESCRIBE, INSERT, INSERT-WHERE, DELETE, DELETE-WHERE:

- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`
