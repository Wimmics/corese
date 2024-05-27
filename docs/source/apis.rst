CORESE APIs
###########

.. toctree::
   :hidden:

   CLI <cli/reference>
   REST API <rest_api/reference>
   Java API <java_api/library_root>
   Python API <python_api/library_root>

.. grid:: 2

    .. grid-item-card::  
      :shadow: sm
      :class-card: sd-rounded-3

      Corese command line interface 
      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      Corese-Command is a command-line interface (CLI) for the Corese Semantic Web Factory. It facilitates:

      * running SPARQL queries on the loaded RDF datasets and remote SPARQL endpoints
      * converting RDF files between different serialization formats
      * validating RDF data against SHACL shapes

      and more...

    .. grid-item-card::  
      :shadow: sm
      :class-card: sd-rounded-3
      :link: rest_api/reference.html

      Corese REST API
      ^^^^^^^^^^^^^^^^^^^^^^^
      The Corese server provides several functionalities available through a REST API:

      * SPARQL endpoint
      * STTL endpoint (can't find it)
      * SPIN endpoint
      * DQP (distributed query processor) endpoint
      * LDP (Linked Data Platform) endpoint

      and more...

      [Source](https://github.com/Wimmics/corese/wiki/CORESE-server#what-is-the-corese-server)
      [Doc](https://files.inria.fr/corese/doc/server.html)

    .. grid-item-card::  
      :shadow: sm
      :class-card: sd-rounded-3
      :link: java_api/library_root.html

      Corese Java API
      ^^^^^^^^^^^^^^^^^^^^^^^
      Developers can use the Corese Java API to:

      * load RDF data from files 
      * export RDF data to files
      * build graphs programmatically
      * run SPARQL queries (SELECT, CONSTRUCT, ASK, UPDATE)
      * validate RDF data against SHACL shapes (SHACL)
      * transform RDF data using STTL (SPARQL Template Transformation Language)
      * apply SPARQL rules 
      * execute LDScript functionalities

      and more...

      [Source](https://github.com/Wimmics/corese/blob/master/docs/getting%20started/Getting%20Started%20With%20Corese-library.md#22-load-graph-from-file)

    .. grid-item-card::  
      :shadow: sm
      :class-card: sd-rounded-3

      Corese Python API
      ^^^^^^^^^^^^^^^^^^^^^^^
      The Corese Python API allows developers to:

      * load RDF data from files
      * export RDF data to files
      * run SPARQL queries (SELECT, CONSTRUCT, ASK, UPDATE)
      * validate RDF data against SHACL shapes

            in development...
