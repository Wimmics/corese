.. _endpoint-spin-tospin:

/spin/tospin
------------

.. _SPARQL Inferencing Notation: https://www.w3.org/submissions/2011/SUBM-spin-overview-20110222/

SPIN (`SPARQL Inferencing Notation`_) is the de-facto industry standard to represent SPARQL rules and constraints on Semantic Web models. 
SPIN provides a vocabulary to represent SPARQL queries as RDF triples. 

This endpoint allows you to convert SPARQL queries to SPIN RDF triples.

**URL:** `{base URL}/spin/tospin`

**Method:** GET, POST

**Headers:** 

- `Content-Type:`
    - `application/x-www-form-urlencoded`

- `Accept:` 
    - `text/html`


**Parameters:**

- `query`: The SPARQL query to be converted to SPIN RDF triples. Default `SELECT * WHERE { ?x ?p ?y }`.

**Returns:**

Response status code 200 if the operation is successful.

Response status code 500 and the response body containing an error message if a query contains an error.
    
.. note:: 
    This endpoint always returns HTML with the actual SPIN in the body. It's not exactly a RESTful API, but it can be a useful tool anyway.

**Request Example:**

.. tab-set::

    .. tab-item:: GET

        .. code-block:: bash

            # GET /spin/tospin?query=PREFIX%20%20humans%3A%20%3Chttp%3A%2F%2Fwww.inria.fr%2F2015%2Fhumans%23%3E%20%20SELECT%20%2A%20WHERE%20%7B%20%3Fchild%20humans%3AhasMother%20%3Fmother.%20%7D HTTP/1.1
            # Host: https://corese.inria.fr
            # Accept: text/html

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
                    SELECT * WHERE { ?child humans:hasMother ?mother. }'

            curl -G \
            --url 'https://corese.inria.fr/spin/tospin' \
            --header 'Accept: text/html' \
            --data-urlencode "query=$QUERY"

    .. tab-item:: POST url-encoded

        .. code-block:: bash

            # POST /spin/tospin HTTP/1.1
            # Host: https://corese.inria.fr
            # Accept: text/html
            # Content-Type: application/x-www-form-urlencoded

            QUERY='PREFIX  humans: <http://www.inria.fr/2015/humans#>
                    SELECT * WHERE { ?child humans:hasMother ?mother. }'

            curl -X POST \
            --url 'https://corese.inria.fr/spin/tospin' \
            --header 'Content-Type: application/x-www-form-urlencoded' \
            --header 'Accept: text/html' \
            --data "query=$QUERY"


**Response Example:**

The example below shows the part of the returned HTML with the resulting SPIN RDF.

.. code-block:: turtle

    @prefix sp: <http://spinrdf.org/sp#> .

    [a sp:Select ;
    sp:star true ;
    sp:where ([sp:object [sp:varName "mother"] ;
        sp:predicate <http://www.inria.fr/2015/humans#hasMother> ;
        sp:subject [sp:varName "child"]])] .
