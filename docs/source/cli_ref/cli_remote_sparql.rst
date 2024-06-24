.. _corese-command-remote-sparql:

remote-sparql
=============


The ``remote-sparql`` command allows executing SPARQL queries on a remote server endpoint.

**Usage:**

.. code-block:: bash

    corese-command remote-sparql [-hivw] [-q=<queryUrlOrFile>] 
                                 [-d=<default_graph>...] [-n=<named_graph>...]
                                 [-m=<requestMethod>]  [-a=<accept>] [-H=<headers>...]
                                 [-r=<maxRedirection>] [-o=<output>] 
                                 [-c=<configFilePath>] 
                                 -e=<endpoint_url>
                                    
                                      
**Options and arguments:**

- `-e`, `\-\-endpoint` `<string|path|URL>` : Required URL of a SPARQL endpoint. 
- `-q`, `\-\-query` `<string|path|URL>` : Required SPARQL query string or path/URL to a .rq file
- `-d`, `\-\-default-graph` `<string>` : Optional default graph URI. Can be specified multiple times. The default graph URIs form the `FROM` clause. 
- `-n`, `\-\-named-graph` `<string>` : Optional named graph URI. Can be specified multiple times. The named graph URIs form the `FROM NAMED` clause.
- `-i`, `--ignore-query-validation`: Optional flag to forgo query syntax validation before it is sent to a remote server. Default: false.

- `-m`, `\-\-request-method` `<GET|POST-Encoded|POST-Direct>` : HTTP request method, `POST-Encoded` is equivalent to ``POST`` request with ``Content-Type:application/x-www-form-urlencoded`` header. `POST-Direct` is equivalent to ``POST`` request with ``Content-Type:application/sparql-query`` header.  Default method: GET.
- `-a`, `-of`, `\-\-accept` `<string>` : `Accept` header value which is also an output format. Supported values are listed :ref:`below <corese-command-remote-sparql-output-formats>` Default: `text/csv`.
- `-H`, `\-\-header` `<string>` : Any additional HTTP header to add to the request. Default: none.

- `-r`, `\-\-max-redirection` `<int>` : Maximum number of response redirection. Default: 5.

- `-o`, `\-\-output-data` `<output_file>` : Optional path to save the query results. Default: standard output.

- `-c`,  `\-\-config`, `\-\-init` `<path>` : Optional path to the configuration file. Default: `config.properties` file in the current directory. Is this true??
- `-w`, `\-\-no-owl-import` : Disables the automatic import of referenced ontologies specified in 'owl:imports' statements in the `profile.ttl` file. Default: enabled.

- `-v`, `\-\-verbose` : Display verbose output.
- `-h`, `\-\-help`: Display  `sparql` command options. 


**Example:**

In this example we access the demo endpoint of the Corese server and execute a simple SPARQL query to retrieve all the children and their mothers in the dataset.

.. code-block:: bash

    QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
           SELECT * WHERE { ?child humans:hasMother ?mother. }'

    corese-command remote-sparql -e http://corese.inria.fr/sparql \
                                 -q "$QUERY" 

.. code-block:: 

    child,mother
    http://www.inria.fr/2015/humans-instances#Lucas,http://www.inria.fr/2015/humans-instances#Catherine
    http://www.inria.fr/2015/humans-instances#Catherine,http://www.inria.fr/2015/humans-instances#Laura

For more examples, see the `Getting Started Guide <../getting%20started/Getting%20Started%20With%20Corese-command.html#remote-sparql-command>`_.


.. _corese-command-remote-sparql-output-formats:
Output (Accept) formats
^^^^^^^^^^^^^^^^^^^^^^^^

Output formats depend on the endpoint implementation and the query form. If the remote endpoint is also powered by Corese then the following formats are supported:

SELECT, ASK:

- XML: `application/sparql-results+xml`
- Json: `application/sparql-results+json`
- CSV: `text/csv`
- TSV: `text/tab-separated-values`


CONSTRUCT, DESCRIBE:

- RDF/XML: `rdfxm`application/rdf+xml`
- Turtle: `text/turtle`
- TriG: `application/trig`
- JSON-LD: `application/ld+json`
- NTRIPLES: `application/n-triples`
- NQUADS: `application/n-quads`
