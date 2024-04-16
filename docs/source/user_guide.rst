User Guide
=================

Introduction
------------

CORESE is a Semantic Web Factory that implements W3C RDF, RDFS and SPARQL recommendations. It provides a rule-based inference engine and a query engine for SPARQL.

Getting Started
---------------

To start CORESE, navigate to the directory containing `corese.jar` and run the following command:

.. code-block:: bash

   java -jar corese.jar

This will start the CORESE engine.

Loading Data
------------

To load RDF data into CORESE, use the following command:

.. code-block:: bash

   java -jar corese.jar -load <path_to_your_rdf_file>

Querying Data
-------------

To query the loaded data, you can use the SPARQL query language. Here is an example of a simple SPARQL query:

.. code-block:: sparql

   SELECT ?s ?p ?o WHERE {
      ?s ?p ?o .
   }

This query will return all triples in the loaded data.

For more complex queries, please refer to the SPARQL specification.

Conclusion
----------

This guide provides a basic introduction to using CORESE. For more detailed information, please refer to the official CORESE documentation.