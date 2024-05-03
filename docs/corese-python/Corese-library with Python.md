# Getting Started With Corese-python (beta)

## 1. Setup and run

1. Install java and python
2. Install python dependencies `pip install --user py4j`
3. Download [corese-library-python-4.5.0.jar](https://github.com/Wimmics/corese/releases/download/release-4.5.0/corese-library-python-4.5.0.jar)
4. Place in the same directory `corese-library-python-4.5.0.jar` and your code `myCode.py`
5. Run with `python myCode.py`

## 2. Template

Python script template:

```python
import atexit
import subprocess
from time import sleep

from py4j.java_gateway import JavaGateway


# Start java gateway
java_process = subprocess.Popen(
    ['java', '-jar', '-Dfile.encoding=UTF-8', 'corese-library-python-4.5.0.jar'])
sleep(1)
gateway = JavaGateway()


# Stop java gateway at the enf od script
def exit_handler():
    gateway.shutdown()
    print('\n' * 2)
    print('Gateway Server Stop!')

atexit.register(exit_handler)


#######################
# Type your code here #
#######################
```

## 3. A functional example

Here is an example of a python script that shows how to :

- Build a graph by program (Corese API);
- Execute a SPARQL query;
- Load a graph from a file;
- Export a graph to a file.

```python
import atexit
import subprocess
from time import sleep

from py4j.java_gateway import JavaGateway

# Start java gateway
java_process = subprocess.Popen(
    ['java', '-jar', '-Dfile.encoding=UTF-8', 'corese-library-python-4.5.0.jar'])
sleep(1)
gateway = JavaGateway()


def exit_handler():
    # Stop java gateway at the enf od script
    gateway.shutdown()
    print('\n' * 2)
    print('Gateway Server Stop!')


atexit.register(exit_handler)

# Import of class
Graph = gateway.jvm.fr.inria.corese.core.Graph
Load = gateway.jvm.fr.inria.corese.core.load.Load
QueryProcess = gateway.jvm.fr.inria.corese.core.query.QueryProcess
RDF = gateway.jvm.fr.inria.corese.core.logic.RDF
TripleFormat = gateway.jvm.fr.inria.corese.core.print.TripleFormat
RDFFormat = gateway.jvm.fr.inria.corese.core.print.RDFFormat
JSONLDFormat = gateway.jvm.fr.inria.corese.core.print.JSONLDFormat
NTripleFormat = gateway.jvm.fr.inria.corese.core.print.NTripleFormat
NQuadsFormat = gateway.jvm.fr.inria.corese.core.print.NQuadsFormat
Shacl = gateway.jvm.fr.inria.corese.core.shacl.Shacl

###############
# Build Graph #
###############


def BuildGraphCoreseApi():
    """Build a graph with a single statement (Edith Piaf is a singer) with the Corese API
    :returns: graph with a single statement (Edith Piaf is a singer)
    """
    corese_graph = Graph()

    # NameSpace
    ex = "http://example.org/"

    # Create and add statement: Edith Piaf is an Singer
    edith_Piaf_IRI = corese_graph.addResource(ex + "EdithPiaf")
    rdf_Type_Property = corese_graph.addProperty(RDF.TYPE)
    singer_IRI = corese_graph.addResource(ex + "Singer")

    corese_graph.addEdge(edith_Piaf_IRI, rdf_Type_Property, singer_IRI)

    return corese_graph

##########
# Sparql #
##########


def sparqlQuery(graph, query):
    """Run a query on a graph

    :param graph: the graph on which the query is executed
    :param query: query to run
    :returns: query result
    """
    exec = QueryProcess.create(graph)
    return exec.query(query)

#########
# SHACL #
#########

def shaclValidation(graph, shacl):
    """Run a SHACL validation on a graph

    :param graph: the graph on which the SHACL validation is executed
    :param shacl: the SHACL graph
    :returns: SHACL validation report
    """
    shacl = Shacl(graph, shacl)
    result = shacl.eval()
    return result

#################
# Load / Export #
#################


def serialize(graph, format):
    """Export a graph to a file

    :param graph: graph to export
    :param format: format of export
    :returns: the graph export
    """
    if format == 'turtle':
        content = TripleFormat.create(graph).toString()
    elif format == 'rdfxml':
        content = RDFFormat.create(graph).toString()
    elif format == 'jsonld':
        content = JSONLDFormat.create(graph).toString()
    elif format == 'n3':
        content = NTripleFormat.create(graph).toString()
    elif format == 'n4':
        content = NQuadsFormat.create(graph).toString()
    else:
        raise Exception('Format not supported : ' + format)
    
    return content

def writeToFile(content, path):
    """Write content to a file

    :param content: content to write
    :param path: path of the file
    :returns: the file write
    """
    with open(path, "w") as file:
        file.write(content)
    return file

def load(path):
    """Load a graph from a local file or a URL

    :param path: local path or a URL
    :returns: the graph load
    """
    graph = Graph()

    ld = Load.create(graph)
    ld.parse(path)

    return graph

########
# Main #
########


def printTitle(title):
    title = "== " + title + " =="
    border = "=" * len(title)
    print("\n" * 2)
    print(border)
    print(title)
    print(border)


###
# Build a graph with the Corese API
###
printTitle("Build a graph with the Corese API")

graph = BuildGraphCoreseApi()
print("Graph build ! (" + str(graph.size()) + " triplets)")

print("\nPrint Graph:")
print(serialize(graph, 'n4'))


###
# SPARQL Query
###
printTitle("SPARQL Query")

graph = load(
    "https://raw.githubusercontent.com/stardog-union/stardog-tutorials/master/music/beatles.ttl") # Uri or path to the graph
print("Graph load ! (" + str(graph.size()) + " triplets)")

# List of U2 albums
query = """
        prefix : <http://stardog.com/tutorial/>

        SELECT ?member 
        WHERE {
            ?The_Beatles :member ?member
        }
        """

map = sparqlQuery(graph, query)
print("\nQuery result ! (List of members of bands \"The Beatles\"): ")
print(map)

###
# SHACL Validation
###
printTitle("SHACL Validation")

graph = load(
    "https://files.inria.fr/corese/data/unit-test/beatles.ttl")
print("Graph load ! (" + str(graph.size()) + " triplets)")

shacl = load(
    "https://files.inria.fr/corese/data/unit-test/beatles-validator.ttl")
print("SHACL load ! (" + str(shacl.size()) + " triplets)")

result = shaclValidation(graph, shacl)
print("SHACL validation report: ")
print(serialize(result, 'turtle'))


###
# Load / Export
###
printTitle("Load / Export")

graph = load(
    "https://raw.githubusercontent.com/stardog-union/stardog-tutorials/master/music/beatles.ttl")
print("Graph load ! (" + str(graph.size()) + " triplets)")

path_export_file = "export.rdf"
writeToFile(serialize(graph, 'turtle'), path_export_file)
print("Graph Export in file (" + path_export_file + ")")

```

Results :

```plaintext
Loaded default config
CoresePy4j gateway server started on port 25333



=======================================
== Build a graph with the Corese API ==
=======================================
Graph build ! (1 triplets)

Print Graph:
<http://example.org/EdithPiaf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/Singer>  .




==================
== SPARQL Query ==
==================
Graph load ! (28 triplets)

Query result ! (List of members of bands "The Beatles"): 
01 ?member = <http://stardog.com/tutorial/John_Lennon>; 
02 ?member = <http://stardog.com/tutorial/Paul_McCartney>; 
03 ?member = <http://stardog.com/tutorial/Ringo_Starr>; 
04 ?member = <http://stardog.com/tutorial/George_Harrison>; 




======================
== SHACL Validation ==
======================
Graph load ! (28 triplets)
SHACL load ! (46 triplets)
SHACL validation report: 
@prefix xsh: <http://www.w3.org/ns/shacl#> .
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

_:b8 a sh:ValidationReport ;
  sh:conforms true .





===================
== Load / Export ==
===================
Graph load ! (28 triplets)
Graph Export in file (export.rdf)



Gateway Server Stop!
```

## 4. Options

- `-p`, `--port` : port of the java gateway server (default: 25333).
- `-c`, `--config`, `--init` : path of the Corese config file, (See a example on [GitHub](https://github.com/Wimmics/corese/blob/master/corese-core/src/main/resources/data/corese/property.properties)).
