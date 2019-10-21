SHACL Interpreter written in SPARQL Template & SPARQL Function

sttl/datashape     for template
function/datashape for function

The entry point is template sttl/datashape/main/start.rq
start  calls sh:target()   defined in function/target.rq
target calls sh:hasShape() defined in function/main.rq

Usage:

function xt:shapeGraph() defined in function/system/shape.rq 
It runs the interpreter on current graph wich contains rdf and shacl
It returns the validation report graph

For Java API, see  ShapeWorkflow in corese-core workflow package :
Graph report = new ShapeWorkflow().process(graph);

templates result pprint the RDF graph and the validation report graph in Turtle HTML for corese server shape service
It displays nodes with errors with specific look (in red)
