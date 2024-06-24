.. _rest-api-reference:

REST API Reference
==================

This document provides a reference guide for the REST API of the Corese server. It outlines the available endpoints, their request and response formats, and any additional details that may be relevant.

For the User Guide on how to setup a Corese server, please refer to the `User Guide <../getting%20started/Getting%20Started%20With%20Corese-server.html>`_.

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
     - Allows quering the CORESE server using SPARQL queries.
   * - :ref:`/sparql/load <endpoint-sparql-load>`
     - Allows loading RDF data to the CORESE server.    
   * - :ref:`/sparql/reset <endpoint-sparql-reset>`
     - Allows to define and reset the endpoint.
   * - :ref:`/sparql/d3 <endpoint-sparql-d3>`
     - Allows returning JSON query results to visualize with d3.
   * - :ref:`/spin/tospin <endpoint-spin-tospin>`
     - Allows to convert SPARQL query to SPIN RDF.
   * - :ref:`/rdf-graph-store <endpoint-rdf-graph-store>`
     - Allows updating the RDF graph store.       


.. note::
    
    There are also other endpoints available for the CORESE server.  Are they used? (ask Remi)
        - /sparql/d3 (output for visualization) - document
        - /sparql/draw - the same as /sparql/d3 but with only *query* parameter
        - /sparql/debug - I think it just a server test for
        - /spin/tosparql - document (not working properly, ask Fabien if we need it)
        - /spin/tospin - document
        - /ldp (Linked Data Platform) (ask Fabien)
        - ldp/upload (ask Fabien)
    

    [Source 1](https://github.com/Wimmics/corese/wiki/CORESE-server#what-is-the-corese-server)
    [Source 2](https://files.inria.fr/corese/doc/server.html)

    There are also other endpoints for the CORESE server in the code. Looks like they have limited usability.

        - /rdf-graph-store - document because it is in the standard
        - /agent
        - /sdk (misnomer, it is actually a sudoku game)
        - /compute/{name}
        - /service/{serv}
        - /template
        - /tutorial/{serv}


.. toctree::
  :hidden:

  sparql
  sparql_load
  sparql_reset
  sparql_d3
  spin_tospin
  rdf_graph_store
