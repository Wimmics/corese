CORESE APIs
###########

.. toctree::
   :hidden:

   CLI <cli_ref/cli_root>
   REST API <rest_api/api_root>
   Java API <java_api/library_root>
   Python API <python_api/library_root>
   

.. grid:: 2

    .. grid-item-card::  
      :shadow: sm
      :class-card: sd-rounded-3
      :link: cli_ref/cli_root.html

      Corese command-line interface 
      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      Corese-Command is a command-line interface (CLI) for the Corese Semantic Web Factory. It facilitates:

      * running SPARQL queries on the loaded RDF datasets and remote SPARQL endpoints
      * converting RDF files between different serialization formats
      * validating RDF data against SHACL shapes

      and more...


    .. grid-item-card::  
      :shadow: sm
      :class-card: sd-rounded-3
      :link: rest_api/api_root.html

      Corese REST API
      ^^^^^^^^^^^^^^^^^^^^^^^
      The Corese server provides several functionalities available through a REST API:

      * SPARQL endpoint
      * SPARQL to SPIN conversion endpoint
      * Graph Store HTTP Protocol endpoint

      and more...


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
