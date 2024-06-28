.. _corese-command-canonicalize:

canonicalize 
============


The ``canonicalize`` command allows applying a specific canonicalization algorithm to RDF files. At times, it becomes necessary to compare the differences between sets of graphs, digitally sign them, or generate short identifiers for graphs via hashing algorithms. The canonicalization process ensures that the same RDF graph is represented in the same way, regardless of the order of triples or the use of blank nodes. The canonicalization process is based on the `RDFC 1.0 <https://www.w3.org/TR/rdf-canon/>`_ specification.

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

To run this example you can download the sample data files :download:`beatles_blank.ttl <../_static/data/beatles_blank.ttl>` or :download:`figure3.ttl <../_static/data/figure3.ttl>`.

.. code-block:: bash

    corese-command canonicalize -i beatles_blank.rdf -r rdfc-1.0 


.. code-block:: xml

    <http://example.com/George_Harrison> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/SoloArtist> .
    <http://example.com/Imagine> <http://example.com/artist> <http://example.com/John_Lennon> .
    <http://example.com/Imagine> <http://example.com/date> "1971-10-11"^^<http://www.w3.org/2001/XMLSchema#date> .
    <http://example.com/Imagine> <http://example.com/name> "Imagine" .
    <http://example.com/Imagine> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/Album> .
    <http://example.com/John_Lennon> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/SoloArtist> .
    <http://example.com/McCartney> <http://example.com/artist> <http://example.com/Paul_McCartney> .
    <http://example.com/McCartney> <http://example.com/date> "1970-04-17"^^<http://www.w3.org/2001/XMLSchema#date> .
    <http://example.com/McCartney> <http://example.com/name> "McCartney" .
    <http://example.com/McCartney> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/Album> .
    <http://example.com/Paul_McCartney> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/SoloArtist> .
    <http://example.com/Please_Please_Me> <http://example.com/artist> <http://example.com/The_Beatles> .
    <http://example.com/Please_Please_Me> <http://example.com/date> "1963-03-22"^^<http://www.w3.org/2001/XMLSchema#date> .
    <http://example.com/Please_Please_Me> <http://example.com/name> "Please Please Me" .
    <http://example.com/Please_Please_Me> <http://example.com/track> _:c14n0 .
    <http://example.com/Please_Please_Me> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/Album> .
    <http://example.com/Ringo_Starr> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/SoloArtist> .
    <http://example.com/The_Beatles> <http://example.com/member> <http://example.com/George_Harrison> .
    <http://example.com/The_Beatles> <http://example.com/member> <http://example.com/John_Lennon> .
    <http://example.com/The_Beatles> <http://example.com/member> <http://example.com/Paul_McCartney> .
    <http://example.com/The_Beatles> <http://example.com/member> <http://example.com/Ringo_Starr> .
    <http://example.com/The_Beatles> <http://example.com/name> "The Beatles" .
    <http://example.com/The_Beatles> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/Band> .
    _:c14n0 <http://example.com/length> "125"^^<http://www.w3.org/2001/XMLSchema#integer> .
    _:c14n0 <http://example.com/name> "Love Me Do" .
    _:c14n0 <http://example.com/writer> <http://example.com/John_Lennon> .
    _:c14n0 <http://example.com/writer> <http://example.com/Paul_McCartney> .
    _:c14n0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.com/Song> .

    

For more examples, see the `Getting Started Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html#the-canonicalize-command>`_.

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

- RDFC: `rdfc-1.0`, `rdfc-1.0-sha256`, `rdfc-1.0-sha384`


