.. _endpoint-sparql:

/sparql
---------------

.. _SPARQL 1.1 Protocol: https://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#protocol

Corese implements `SPARQL 1.1 Protocol`_. This endpoint allows you to query and update an RDF Dataset using SPARQL. 
The supported query forms are SELECT, ASK, CONSTRUCT, DESCRIBE or update (DELETE/INSERT). 

The return data format can be specified in the `Accept` HTTP header and depends on the query form. The default return format is XML. 


**URL:** `{base URL}/sparql`

SELECT or ASK 
^^^^^^^^^^^^^^^^

The most often used SPARQL query form is SELECT for selecting or finding data. It returns the results in a tabular form that can be represented in many different formats.

ASK query form is used to determine whether a particular triple pattern exists in the specified data set. ASK returns true or false, depending on whether the solution or match exists. The result format can be modified depending on the client application.


**Method:** GET or POST

**Headers:** 

- `Content-Type:`
    - `application/x-www-form-urlencoded` 
    - `application/sparql-query` 

- `Accept:` 
    - `application/sparql-results+json`
    - `application/sparql-results+xml`
    - `text/csv`
    - `text/tab-separated-values`
    - `text/plain` 
    - `text/html`

**Parameters:**

- `query`: Required query string.
- `default-graph-uri`: Optional list of default graph URIs. Default: none.
- `named-graph-uri`: Optional list of named graph URIs. Default: none.
 
.. note::
    The RDF Dataset for a query can be passed in the HTTP request as `default-graph-uri` and `named-graph-uri` parameters or in the SPARQL query string using the FROM and FROM NAMED keywords. If they differ the request parameters take precedence.

    The `default-graph-uri` and `named-graph-uri` Have to be passed as query string parameters if using `Content-Type: application/sparql-query` header.

.. .. note::

..     The parameters below are optional and non-standard. They are not part of the SPARQL 1.1 Protocol specification.    

..     - `format`: Optional output format json|xml|text|csv|tsv|html|turtle|nt (case sensitive) to specify return format. Default: xml. Alternatively, the output format can  be specified in the  `Accept` HTTP header. 
..     - `transform`: Optional list of result transformations such as *st:map*. Default: none.
..     - `mode`: Optional list like mode=debug;link;log. Default: none. (Perhaps only used in development. Does not seem like working)
..     - `uri`: Optional list of URIs. Default: none. Use case: URL of federated query. 
..     - `access`: Optional access key that may give access to the protected features on the remote servers. Default: none. 
..     - `param`: Optional parameter in format: param=key~val.

.. _endpoint-sparql-select:
There are three ways to send a SPARQL query to a Corese endpoint:

- Send a query as a URL-encoded query string in the `query` parameter
    The query string must be passed in the URL as `query=...`.
- Send a query as a URL-encoded POST request. 
    The `Content-Type` header must be set to `application/x-www-form-urlencoded`.
    The query string must be passed in the POST request body as `query=...`
- Send a query directly in the POST request body.
    The `Content-Type` header must be set to `application/sparql-query`.
    The query string must be passed in the POST request body.    

All other parameters can be passed in the URL as query string parameters.   
 
.. .. note::
..     The direct POST query only works with `Accept:application/sparql-results+xml` and  `Accept:application/sparql-results+json` headers .

**SELECT Request Example:**

This example demonstrates three way to run the same query on the remote SPARQL endpoint  `<https://corese.inria.fr/sparql>`_.

The query is to find all the children who have a mother in the `Humans` dataset . 

.. tab-set::

    .. tab-item:: GET

        .. code-block:: bash
            
            # GET /sparql?query=PREFIX%20%20humans%3A%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans%23%3E%20%20SELECT%20%2A%20WHERE%20%7B%20%3Fchild%20humans%3AhasMother%20%3Fmother.%20%7D HTTP/1.1
            # Host: https://corese.inria.fr
            # Accept: application/sparql-results+json

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
                   SELECT * WHERE { ?child humans:hasMother ?mother. }'

            curl -G \
                --url https://corese.inria.fr/sparql \
                --header "Accept: application/sparql-results+json" \
                --data-urlencode "query=$QUERY"

    .. tab-item:: POST url-encoded

        .. code-block:: bash 
            # POST /sparql HTTP/1.1
            # Host: https://corese.inria.fr
            # Content-Type: application/x-www-form-urlencoded
            # Accept: application/sparql-results+json
            # query= "PREFIX  humans: <http://www.inria.fr/2015/humans#>  SELECT * WHERE { ?child humans:hasMother ?mother. }"
    
            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
            SELECT * WHERE { ?child humans:hasMother ?mother. }'

            curl -X POST \
            --url https://corese.inria.fr/sparql \
            --header "Content-Type: application/x-www-form-urlencoded" \
            --header "Accept: application/sparql-results+json" \
            --data "query=$QUERY"

    .. tab-item:: POST direct

        .. code-block:: bash

            # POST /sparql HTTP/1.1
            # Host: https://corese.inria.fr
            # Content-Type: application/sparql-query
            # Accept: application/sparql-results+xml
            # PREFIX  humans: <http://www.inria.fr/2015/humans#>
            # SELECT * WHERE { ?child humans:hasMother ?mother. }

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
                   SELECT * WHERE { ?child humans:hasMother ?mother. }'

            curl -X POST \
                --url https://corese.inria.fr/sparql \
                --header "Content-Type: application/sparql-query" \
                --header "Accept: application/sparql-results+json" \
                --data "$QUERY" 

.. code-block:: json

    {
    "head": {
            "vars": [ "child", "mother"]
            },
    "results": { 
            "bindings": [
                    {
                    "child":  { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Lucas"},
                    "mother": { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Catherine"}
                    },
                    {
                    "child":  { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Catherine"},
                    "mother": { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Laura"}
                    } 
                        ]
                } 
    }

**ASK Request Example:**

.. tab-set::

    .. tab-item:: GET

        .. code-block:: bash 

            # GET /sparql?query=PREFIX%20%20humans%3A%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans%23%3E%20%20ASK%20%7B%20%3Fchild%20humans%3AhasMother%20%3Fmother.%20%7D' HTTP/1.1
            # Host: https://corese.inria.fr
            # Accept: application/sparql-results+json

            ASK='PREFIX  humans: <http://www.inria.fr/2015/humans#>  
                 ASK { ?child humans:hasMother ?mother. }'

            curl -G \
                 --url https://corese.inria.fr/sparql \
                 --header "Accept: application/sparql-results+json" \
                 --data-urlencode "query=$ASK"


.. code-block:: json

    {
    "head": { } ,

    "boolean" : true
    }   

CONSTRUCT or DESCRIBE 
^^^^^^^^^^^^^^^^^^^^^^

CONSTRUCT query form is used to create new data from your existing data. DESCRIBE query form is used to retrieve all the triples associated with a resource. Both return results in RDF format.

**Method:** GET or POST

**Headers:** 

- `Content-Type:`
    - `application/x-www-form-urlencoded` 
    - `application/sparql-query`

- `Accept:` 
    - `application/ld+json`
    - `application/rdf+xml`
    - `application/turtle`
    - `application/trig`
    - `application/n-triples`
    - `application/n-quads`
    - `text/nt`

There are also three ways to send these types of queries as described in the :ref:`SELECT or ASK<endpoint-sparql-select>` section.

**CONSTRUCT Request Example:**

.. tab-set::

    .. tab-item:: POST url-encoded

        .. code-block:: bash 
            
            # POST /sparql HTTP/1.1
            # Content-Type: application/x-www-form-urlencoded
            # Accept: application/turtle
            # Host: https://corese.inria.fr
            # query="PREFIX  humans: <http://www.inria.fr/2015/humans#>  CONSTRUCT { ?mother humans:hasChild ?child. } WHERE { ?child humans:hasMother ?mother. }"

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>  
                   CONSTRUCT { ?mother humans:hasChild ?child. } 
                   WHERE { ?child humans:hasMother ?mother. }'

            curl -X POST \
            --url https://corese.inria.fr/sparql \
            --header "Accept: application/turtle" \
            --header "Content-Type: application/x-www-form-urlencoded" \
            --data "query=$QUERY"


    .. tab-item:: POST direct 

        .. code-block:: bash

            #POST /sparql HTTP/1.1
            #Content-Type: application/sparql-query
            #Accept: application/turtle
            #Host: https://corese.inria.fr

            #PREFIX  humans: <http://www.inria.fr/2015/humans#>
            #CONSTRUCT { ?mother humans:hasChild ?child. } 
            #WHERE { ?child humans:hasMother ?mother. }

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>  
                   CONSTRUCT { ?mother humans:hasChild ?child. } 
                   WHERE { ?child humans:hasMother ?mother. }'

            curl -X POST \
            --url https://corese.inria.fr/sparql \
            --header "Accept: application/turtle" \
            --header "Content-Type: application/sparql-query" \
            --data "$QUERY"


.. code-block:: turtle

    @prefix ns1: <http://www.inria.fr/2015/humans-instances#> .
    @prefix humans: <http://www.inria.fr/2015/humans#> .

    ns1:Catherine humans:hasChild ns1:Lucas .

    ns1:Laura humans:hasChild ns1:Catherine .

**DESCRIBE Request Example:**

.. tab-set::

    .. tab-item:: GET

        .. code-block:: bash

            # GET /sparql?query="PREFIX%20%20humans%3A%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans%23%3E%20%20DESCRIBE%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans-instances%23Catherine%3E" HTTP/1.1
            # Accept: text/nt
            # Host: https://corese.inria.fr

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
                   DESCRIBE <http://www.inria.fr/2015/humans-instances#Catherine>'

            curl -G \
            --url https://corese.inria.fr/sparql \
            --header "Accept: application/turtle" \
            --data-urlencode "query=$QUERY" 

.. code-block:: turtle

    @prefix ns1: <http://www.inria.fr/2015/humans-instances#> .
    @prefix humans: <http://www.inria.fr/2015/humans#> .

    ns1:Lucas humans:hasMother ns1:Catherine .

    ns1:Karl humans:hasSpouse ns1:Catherine .

    ns1:Catherine humans:hasMother ns1:Laura ;
                  humans:name "Catherine"@fr ;
                  a humans:Woman .

.. _sparql-update:

Update (DELETE/INSERT)
^^^^^^^^^^^^^^^^^^^^^^

This operation allows to update the RDF dataset. The supported update operations are INSERT DATA, DELETE DATA, DELETE WHERE, INSERT WHERE.

**Method:** POST

**Headers:** 

- `Content-Type:` 
    - `application/x-www-form-urlencoded`
    - `application/sparql-update`
- `Accept:` 
    - `application/sparql-results+json`
    - `application/sparql-results+xml`
    - `application/turtle`
    - `text/plain` 

**Parameters:**

- `update`: Required parameter for the update operation.
- `using-graph-uri`: Optional list of graph URIs for the update operation. Default: none.
- `using-named-graph-uri`: Optional list of named graph URIs for the update operation. Default: none.
- `access`: Optional access key that may give access to the protected features on the remote servers. Default: none.  

.. note::
    Use of the `using-graph-uri` and `using-named-graph-uri` parameters together with the USING, USING NAMED, or WITH clauses in the query is not permitted.
    

.. note::
    SPARQL Update operations may not be authorized by a remote server. To execute an update operation on a remote server, the `access` parameter must be set to the access key that gives access to the protected features on the remote server.  


There are two ways to send the update query to a Corese endpoint:

- Send an update query as a URL-encoded POST request. 
    The `Content-Type` header must be set to `application/x-www-form-urlencoded`.
    The update string must be passed in the POST request body as `update=...`.  as well as and the `*-graph-uri` parameters.

- Send an update query directly in the POST request body.
    The `Content-Type` header must be set to `application/sparql-query`.
    The update string must be passed in the POST request body.

.. note::
    The 'using-graph-uri' and 'using-named-graph-uri' parameters have to be passed as query string parameters in the POST direct.


**INSERT Request Example:**

To execute this example we recommend launching the `Corese Docker <../docker/README.html>`_ container.


.. tab-set::

    .. tab-item:: POST url-encoded

        .. code-block:: bash
            
            # POST /sparql
            # Host: https://localhost:8080
            # Content-Type: `application/x-www-form-urlencoded`
            # update='PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT DATA { <http://example/book1> dc:title "A new book" .}'

            QUERY='PREFIX dc: <http://purl.org/dc/elements/1.1/> 
                   INSERT DATA { <http://example/book1> dc:title "A newer book" . }'
            GRAPH='http://example.org/graph'  

            curl -X POST \
            --url http://localhost:8080/sparql \
            --header "Content-Type: application/x-www-form-urlencoded" \
            --data "update=$QUERY" \
            --data "using-graph-uri=$GRAPH"

    .. tab-item:: POST direct 

        .. code-block:: bash

            # POST /sparql
            # Host: https://localhost:8080
            # Content-Type: application/sparql-update
            #
            # PREFIX dc: <http://purl.org/dc/elements/1.1/> 
            # INSERT DATA { <http://example/book1> dc:title "A new book" .}

            QUERY='PREFIX dc: <http://purl.org/dc/elements/1.1/> 
                   INSERT DATA { <http://example/book1> dc:title "A newer book" . }'
            GRAPH='http://example.org/graph'  

            curl -X POST \
            --url http://localhost:8080/sparql?using-graph-uri="$GRAPH" \
            --header "Content-Type: application/sparql-update" \
            --data "$QUERY" -v

.. tab-set::

    .. tab-item:: POST url-encoded

        .. code-block:: xml

            <?xml version="1.0" ?>
            <sparql
                xmlns='http://www.w3.org/2005/sparql-results#'>
                <head></head>
                <results>
                    <result></result>
                </results>
            </sparql>

    .. tab-item:: POST direct

        .. code-block:: html

            HTTP/1.1 200 OK
            Date: Tue, 25 Jun 2024 14:54:47 GMT
            Access-Control-Allow-Origin: *
            Content-Type: text/plain
            Content-Length: 0
            Server: Jetty(11.0.14)

.. note:: 
    The update query returns an empty result set formatted according to the `Accept` header if the `Content-Type:application/x-www-form-urlencoded` and if the update operation is successful.

    The update query returns no body and `Length=0` if the `Content-Type:application/sparql-update` and if the update operation is successful.

    If the update operation fails, the response status code is 500 and the response body contains an error message.