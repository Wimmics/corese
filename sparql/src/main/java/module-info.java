module fr.inria.corese.sparql {
	requires transitive fr.inria.corese.kgram;
	requires org.slf4j;
	requires java.sql;
	exports fr.inria.corese.sparql.triple.parser;
	exports fr.inria.corese.sparql.exceptions;
	exports fr.inria.corese.sparql.datatype;
	exports fr.inria.corese.sparql.api;
	exports fr.inria.corese.sparql.triple.cst;
	exports fr.inria.corese.sparql.triple.update;
	exports fr.inria.corese.sparql.triple.function.script;
	exports fr.inria.corese.sparql.triple.function.term;
	exports fr.inria.corese.sparql.compiler.java;
	exports fr.inria.corese.sparql.datatype.function;
}
