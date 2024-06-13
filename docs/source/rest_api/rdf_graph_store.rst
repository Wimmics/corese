.. _endpoint-rdf-graph-store:

/rdf-graph-store
----------------

.. _SPARQL 1.1 Graph Store HTTP Protocol: https://www.w3.org/TR/sparql11-http-rdf-update
.. _SPARQL Update: sparql-update

Corese implements `SPARQL 1.1 Graph Store HTTP Protocol`_ to manage a collection of RDF graphs. 

This endpoint offers an alternative to the :ref:`SPARQL update <sparql-update>`. Most of the operations defined here can be performed using SPARQL update, but for some clients, this endpoint may be easier to work with.

The difference is that the client needs to provide only the RDF triples to be added to the graph, while the SPARQL update requires the client to provide the whole SPARQL query.


**URL:** `{base URL}/rdf-graph-store`

**Method:** POST, PUT, GET

.. note::
    The implementation of the POST and PUT methods is the same. 

    The GET method is used to retrieve the content of a graph. The graph is identified by the `graph` parameter. If the `graph` parameter is not provided, the default graph is assumed.

    Looks like the GET method does not work as intended.  

    The DELETE method is not implemented

**Headers:** 

- `Content-Type:`
    - `application/x-www-form-urlencoded`
    - `multipart/form-data`

- `Accept:` 
    - `application/rdf+xml`
    - `text/plain` 
    - `text/html`

.. note:: 
    The `Accept` header is optional. If not provided, the server will return the response with no data as `text/html` for POST and PUT and JSON-LD for GET requests.


**Parameters:**

- `query`: Required list of RDF triples to be added to the graph. The triples that you list must include URIs, literal, values, or blank nodes. 
- `graph`: The URI of a graph to be updated. Default: default graph.
- `access`: Optional access key that may give access to the protected features on the remote servers. Default: none. 

**Returns:**

- Response status code 200 if the operation is successful.

- Response status code 500 and the response body containing an error message if a query contains an error.


**Request Example:**

To execute this example we recommend launching the `Corese Docker <../docker/README.html>`_ container. 

.. tab-set::

    .. tab-item:: HTTP POST

        .. code-block:: 

            POST rdf-graph-store HTTP/1.1
            Host: localhost:8080
            Content-Type: application/x-www-form-urlencoded
            graph=http://ns.inria.fr/books
            RDF=<http://example/book1> <http://purl.org/dc/elements/1.1/title> "A new book" .

    .. tab-item:: curl 

        .. code-block:: bash

            RDF='<http://example/book1> <http://purl.org/dc/elements/1.1/title> "A new book" .'
            graph='http://ns.inria.fr/books'

            curl -X POST \
            --url 'http://localhost:8080/rdf-graph-store' \
            --header 'Content-Type: application/x-www-form-urlencoded' \
            --data "query=$RDF" \
            --data "graph=$graph"

  
**Response Example:**


.. code-block:: xml

    <?xml version="1.0" ?>
    <sparql xmlns='http://www.w3.org/2005/sparql-results#'>
    <head>
    </head>
    <results>
    <result>
    </result>
    </results>
    </sparql>
