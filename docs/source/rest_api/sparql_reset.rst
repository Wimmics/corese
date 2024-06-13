.. _endpoint-sparql-reset:

/sparql/reset
-------------

This endpoint allows you to define and reset the endpoint.

**URL:** `{base URL}/sparql/reset`

**Method:** POST

**Parameters:**

- `entailments`: Optional boolean flag (case sensitive) to enable standard entailments. Default: false.
- `owlrl`: Optional boolean flag to enable OWL RL reasoning. Default: false.
- `load`: Optional boolean flag to (re)load data. Default: false.
- `profile`: Optional configuration `.ttl` file in addition to the `profile.ttl`. Default: none.
- `localhost`: Optional Boolean flag indicating whether to use `http://localhost:8080` address notation for the server (true) or attempt to retrieve the canonical address of the server (false). Default is false

**Returns:**

- Response status code 200 and the response body as "Endpoint reset" if the reset operation is successful.

- Response status code 500 and the response body containing an error message if the load operation fails.


**Request Example:**

.. tab-set::

    .. tab-item:: HTTP 

        .. code-block:: 

            POST /sparql/reset HTTP/1.1
            Host: https://localhost:8080
            load="true"

    .. tab-item:: curl 

        .. code-block:: bash

            curl -X POST \
                --url http://localhost:8080/sparql/reset \
                --data "load=true" 

**Response Example:**

.. code-block:: text

    Endpoint reset