.. _endpoint-sparql-load:

/sparql/load
------------

This endpoint allows you to load RDF data to the Corese server.

**URL:** `{base URL}/sparql/load`

**Method:** POST

**Headers:** 

- `Content-Type:` 
    - `application/x-www-form-urlencoded`

**Parameters:**

- `remote_path`: Required URL of the server used to store the data, all of the rdf files in the path will be loaded
- `source`: Optional graph name for the uploaded data.

.. note::
    SPARQL Update operations may not be authorized by a remote server.

**Returns:**

Response status code 200 and the response body as "Successfully loaded <name of the data path>" if the load operation is successful.

Response status code 500 and the response body containing an error message if the load operation fails.
    

**Request Example:**

To execute this example we recommend launching the `Corese Docker <../docker/README.html>`_ container. 

This example will load test dataset stored in the http://ns.inria.fr/humans/data remote directory into a graph `<http://ns.inria.fr/humans>`.

.. tab-set::

    .. tab-item:: HTTP 

        .. code-block:: 

            POST /sparql/load HTTP/1.1
            Host: https://localhost:8080
            Content-Type: x-www-form-urlencoded
            remote_path="http://ns.inria.fr/humans/data"
            source="http://ns.inria.fr/humans"

    .. tab-item:: curl 

        .. code-block:: bash

            curl -X POST \
                --url http://localhost:8080/sparql/load \
                --header 'Content-Type: application/x-www-form-urlencoded' \
                --data "remote_path=http://ns.inria.fr/humans/data" \
                --data "source=http://ns.inria.fr/humans"

**Response Example:**


.. code-block:: text

    Successfully loaded http://ns.inria.fr/humans/data