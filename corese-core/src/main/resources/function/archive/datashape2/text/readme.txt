#
# SHACL Interpreter 
#
# Olivier Corby - Wimmics Inria I3S - 2016-2019
#

main : the entry point API
core : dispatch shape processing to property and node shape
path : property shape
constraint :  node shape

operator :  function definitions for constraint evaluation

ppath : property path interpreter
ppathext : pp extension with filter and service

focus, target : compute target nodes for shapes
sparql : SHACL SPARQL


Shapes are analysed  by SPARQL query which generate list of (shape constraint)
The lists are recorded into specific maps thanks to sh:getShapeConstraint(name, graph, shape)
where name is the name of a family of similar shape constraints
The first call to getConstraint execute funcall(name) which returns a list of (shape constraint) which is stored into a map associated to name
Further call to getConstraint read  the list in the map.

For example :

sh:path1 =
sh:minLength sh:maxLength sh:datatype sh:minInclusive sh:minExclusive sh:maxInclusive sh:maxExclusive 
sh:nodeKind sh:class sh:in sh:languageIn sh:node sh:property

sh:path2 = sh:qualifiedValueShape

sh:path3 =
sh:equals sh:disjoint sh:and sh:or sh:xone sh:not sh:maxCount sh:minCount
sh:hasValue sh:uniqueLang sh:lessThan sh:lessThanOrEquals

sh:pathpattern = sh:pattern

sh:cstgeneric1 =
sh:hasValue, sh:datatype, sh:minInclusive, sh:minExclusive, sh:maxInclusive, sh:maxExclusive, sh:minLength, sh:maxLength, sh:nodeKind, sh:node, sh:in, sh:languageIn

sh:cstgeneric2 =
sh:class, sh:disjoint, sh:equals

sh:booleancore =
sh:and sh:or sh:xone sh:not


Shape constraints are evaluated by means of a function call where the function name is the name of the constraint, e.g. funcall(sh:minCount, node, value)
