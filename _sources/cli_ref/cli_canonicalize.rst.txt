.. _corese-command-canonicalize:

canonicalize 
============


The ``canonicalize`` command allows applying a specific canonicalization algorithm to RDF files.

**Usage:**

.. code-block:: bash

    corese-command canonicalize [-hRvw] [-i=<inputs>...] [-f=<inputFormat>] 
                          [-r=<canonicalAlgo>] [-o=<output>] 
                          [-c=<configFilePath>]


**Options and arguments:**

- `-i`, `\-\-input-data` `<input file|dir|pattern|URL>` : Optional path to a file, directory, filename pattern, or URL containing the RDF data. Default: standard input.
- `-f`, `-if`, `\-\-input-format` `<format>` : Input format is automatically detected for files. Use this option with the standard input or if you want to force the input file format. Supported input formats are listed :ref:`below <corese-command-canonicalize-input-formats>`.
- `-R`, `\-\-recursive`: Recursively input all the files in the input directory and sub-directories.

- `-r`, `-a`, `ca`, `-of`, `\-\-canonical-algo` `<algo>` : Canonicalization algorithm to apply. Supported algorithms/formats are listed :ref:`below <corese-command-canonicalize-output-formats>`. Default: `markdown` table.
- `-o`, `\-\-output-data` `<output_file>` : Optional path to save the query results. Default: standard output.

- `-c`,  `\-\-config`, `\-\-init` `<path>` : Optional path to the configuration file. Default: `config.properties` file in the current directory. Is this true??
- `-w`, `\-\-no-owl-import` : Disables the automatic import of referenced ontologies specified in 'owl:imports' statements in the `profile.ttl` file. Default: enabled.

- `-v`, `\-\-verbose` : Display verbose output.
- `-h`, `\-\-help`: Display  `sparql` command options. 



**Example:**

To run this example you can download the sample data file :download:`beatles.rdf <../_static/data/beatles.rdf>`.

.. code-block:: bash

    corese-command canonicalize -i beatles.rdf -r rdfc-1.0-sha256 


.. code-block:: turtle

    TBD

    ...

For more examples, see the `Getting Started Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html#7-canonicalize-command>`_.

.. _corese-command-canonicalize-input-formats:
Input formats
^^^^^^^^^^^^^^^^

- RDF/XML: `rdfxml`, `rdf` or `application/rdf+xml`
- Turtle: `turtle`, `ttl` or `text/turtle`
- TriG: `trig` or `application/trig`
- JSON-LD: `jsonld` or `application/ld+json`
- NTRIPLES: `ntriples`, `nt` or `application/n-triples`
- NQUADS: `nquads`, `nq`, or `application/n-quads`
- RDFa/HTML: `rdfa`, `html`, `application/xhtml+xml`

.. _corese-command-canonicalize-output-formats:
Output (Canonicalization Algorithms) formats
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The following canonicalization algorithms are available under the `RDFC 1.0 <https://www.w3.org/TR/rdf-canon/>`_ specification:

- RDFC 1.0 with SHA-256: `rdfc-1.0`, `rdfc-1.0-sha256`
- RDFC 1.0 with SHA-384: `rdfc-1.0-sha384`


