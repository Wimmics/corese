# General informations
Corese is a Semantic Web Factory (triple store & SPARQL endpoint) implementing RDF, RDFS, SPARQL 1.1 Query & Update.

* Contact: olivier.corby at inria.fr
* Mailing list: corese-users at inria.fr
* Subscribe to mailing list: corese-users-request at inria.fr   subject: subscribe
* Licence: Open source software with Licence CeCILL-C (aka LGPL).
* Main forge: https://github.com/Wimmics/corese
* Old forge (deprecated): https://gforge.inria.fr/projects/kgram/

# Compilation from source
To download the source code:

    git clone https://github.com/Wimmics/corese.git

or

    git clone git@github.com:Wimmics/corese.git


It should create a corese directory.

    cd corese
    mvn -Dmaven.test.skip=true package

# Features:
* Distributed Query Processing
* Corese HTTP server
* SPARQL Inference Rules
* SPARQL Template Transformation Language
* SPARQL approximate search
* SPARQL Property Path extensions
* SPIN Syntax
* RDF Graph as Query Graph Pattern
* SQL extension function
