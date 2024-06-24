.. _endpoint-sparql-d3:

/sparql/d3
------------

This endpoint retrieves the JSON representation of triples for D3 graph visualization of a SPARQL query results.
    

**URL:** `{base URL}/sparql/d3`

**Method:** GET

**Headers:** 

- `Accept:` 
    - `application/sparql-results+json`   

**Parameters:**

- `query`: Required query string.
- `default-graph-uri`: Optional list of default graph URIs. Default: none.
- `named-graph-uri`: Optional list of named graph URIs. Default: none.
- `access`: Optional access key that may give access to the protected features on the remote servers. Default: none. 


**Returns:**

- Response status code 200 if the query operation is successful.

- Response status code 500 and the response body containing an error message if the query contains an error.
    

**Request Example:**

.. tab-set::

    .. tab-item:: HTTP 

        .. code-block:: 

            POST /sparql/d3?query=PREFIX%20%20humans%3A%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans%23%3E%20%20%20%20%20%20%20SELECT%20%2A%20WHERE%20%7B%20%3Fchild%20humans%3AhasMother%20%3Fmother.%20%7D'  HTTP/1.1
            Host: https://corese.inria.fr
            Accept: application/sparql-results+json

    .. tab-item:: curl 

        .. code-block:: bash

            # QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#> 
            #        SELECT * WHERE { ?child humans:hasMother ?mother. }'

            curl -X GET \
            --url 'https://corese.inria.fr/sparql/d3?query=PREFIX%20%20humans%3A%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans%23%3E%20%20%20%20%20%20%20SELECT%20%2A%20WHERE%20%7B%20%3Fchild%20humans%3AhasMother%20%3Fmother.%20%7D' \
            --header 'Accept: application/sparql-results+json' 


**Response Example:**


.. code-block:: json

    { "mappings" :
        { "head": {
            "vars": ["child", "mother"]
                },
            "results": { "bindings": [
                    {
                    "child": { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Lucas"},
                    "mother": { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Catherine"}
                    }
                    ,
                    {
                    "child": { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Catherine"},
                    "mother": { "type": "uri", "value": "http://www.inria.fr/2015/humans-instances#Laura"}
                    }
                    ] }
        },
    "d3" :  }
