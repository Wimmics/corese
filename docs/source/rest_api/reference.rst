.. _rest-api-reference:
.. _user-guide: /docs/build/html/getting%20started/Getting%20Started%20With%20Corese-server.html 

REST API Reference
==================


Introduction
------------

This document provides a reference guide for the REST API of CORESE server. It outlines the available endpoints, their request and response formats, and any additional details that may be relevant.

For user guide on how to setup a CORESE server, please refer to the `user-guide`_.

Base URL
--------

The base URL for all API endpoints could be: 

* localhost: `http://localhost:8080`
* docker: `http://localhost:8080`
* external server: for example `https://corese.inria.fr`

Authentication
--------------

No authentication is required to access the API (??) . 

Endpoints
---------

/sparql
^^^^^^^

This endpoint allows you to query the CORESE server using SPARQL queries. The supported query types are SELECT, CONSTRUCT, ASK, or update (INSERT/DELETE).

**URL:** `{base URL}/sparql`

**Method:** GET

**Parameters:**

- `query`: Required SELECT or ASK query string.
- `access`: Optional key that may give access to the protected features. (in use??)
- `default-graph-uri`: Optional default graph URI.
- `named-graph-uri`: Optional named graph URI.
- `format`: Optional output format json|xml (case sensitive) to specify return format when there is no http header content. Default: xml
- `transform`: Optional list of transformation such as *st:map*.
- `param`: Optional parameter in format: param=key~val;val.
- `mode`: Optional list like mode=debug;link;log.
- `uri`: Otional list of URIs. Use case: URL of shacl shape.

    TODO: Add defaults to the optional parameters.

**Request Example:**

.. tab-set::

    .. tab-item:: HTTP

        .. code-block:: http

            GET /sparql?query="SELECT * WHERE {?s ?p ?o} LIMIT 5"&format=JSON
            Host: https://corese.inria.fr
            Headers: Content-Type: application/json
   


    .. tab-item:: curl

        .. code-block:: bash

            curl https://corese.inria.fr/sparql --data-urlencode "query=SELECT * WHERE {?s ?p ?o} LIMIT 2" \
                                                --data-urlencode "format=json"

**Response Example:**

.. code-block:: json

    {
    "head": {
            "vars": [ "s", "p", "o"]
            },
    "results": { 
            "bindings": [
                    {
                    "s": { "type": "uri", "value": "http://www.inria.fr/2015/humans#Man"},
                    "p": { "type": "uri", "value": "http://ns.inria.fr/sparql-template/icon"},
                    "o": { "type": "uri", "value": "http://corese.inria.fr:34660/img/male.png"}
                    },
                    {
                    "s": { "type": "uri", "value": "http://www.inria.fr/2015/humans#Person"},
                    "p": { "type": "uri", "value": "http://ns.inria.fr/sparql-template/icon"},
                    "o": { "type": "uri", "value": "http://corese.inria.fr:34660/img/person.png"}
                    } 
                        ]
                } 
    }

2. Endpoint 2
    ------------

    Description of endpoint 2.

    **URL:** `/api/endpoint2`

    **Method:** POST

    **Parameters:**

    - `param1`: Description of param1.
    - `param2`: Description of param2.

    **Request Example:**

    .. code-block:: http

        POST /api/endpoint2
        Host: api.example.com
        Authorization: Bearer YOUR_API_KEY
        Content-Type: application/json

        {
             "param1": "value1",
             "param2": "value2"
        }

    **Response Example:**

    .. code-block:: json

        {
             "key1": "value1",
             "key2": "value2"
        }

Additional Information
----------------------

Include any additional information or guidelines for using the API here.


