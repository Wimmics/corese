.. _rest-api-reference:
.. _user-guide: /docs/build/html/getting%20started/Getting%20Started%20With%20Corese-server.html 


REST API Reference
==================

This document provides a reference guide for the REST API of CORESE server. It outlines the available endpoints, their request and response formats, and any additional details that may be relevant.

For user guide on how to setup a CORESE server, please refer to the `user-guide`_.

Base URL
--------

The base URL for all API endpoints could be: 

* localhost: `http://localhost:8080`
* docker: `http://localhost:8080`
* external server: for example `https://corese.inria.fr`


Endpoints
---------

.. list-table::
   :header-rows: 1

   * - Endpoint
     - Summary
   * - :ref:`/sparql <endpoint-sparql>`
     - Allows to query the CORESE server using SPARQL queries.
   * - :ref:`/sparql/load <endpoint-sparql-load>`
     - Allows to load RDF data to the CORESE server.    
   * - :ref:`/sparql/reset <endpoint-sparql-reset>`
     - Allows to define and reset the endpoint.
   * - :ref:`/sparql/d3 <endpoint-sparql-d3>`
     - Allows to define and reset the endpoint.

.. note::
    
    There are also other endpoints available for the CORESE server.  Are they used? (ask Remi)
        - /sparql/d3 (output for visualization) - document
        - /sparql/draw - the same as /sparql/d3 but with only *query* parameter
        - /sparql/debug - I think it just a server test for
        - /spin/tosparql - document
        - /spin/tospin - document
        - /ldp (Linked Data Platform) (ask Fabien)
        - ldp/upload (ask Fabien)
    

    [Source 1](https://github.com/Wimmics/corese/wiki/CORESE-server#what-is-the-corese-server)
    [Source 2](https://files.inria.fr/corese/doc/server.html)

    There are also other endpoints for the CORESE server in the code. Looks like they have limited usability.

        - /rdf-graph-store - document because it is in the standard
        - /agent
        - /sdk (misnomer, it is actually a sudoku game. could be fun to include)
        - /compute/{name}
        - /service/{serv}
        - /template
        - /tutorial/{serv}


.. toctree::
   :maxdepth: 3
   :hidden:


   sparql
   sparql_load
   sparql_reset