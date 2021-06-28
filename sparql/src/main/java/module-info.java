module fr.inria.corese.sparql {
	requires fr.inria.corese.kgram;
	requires org.slf4j;
	requires java.sql;
	requires org.json;
	requires org.apache.commons.text;
	requires transitive rdf4j.model.api;
	requires transitive rdf4j.model;
	requires transitive rdf4j.model.vocabulary;

	exports fr.inria.corese.sparql.triple.parser;
	exports fr.inria.corese.sparql.triple.parser.visitor;
	exports fr.inria.corese.sparql.triple.parser.context;
	exports fr.inria.corese.sparql.exceptions;
	exports fr.inria.corese.sparql.datatype;
	exports fr.inria.corese.sparql.datatype.extension;
	exports fr.inria.corese.sparql.api;
	exports fr.inria.corese.sparql.triple.cst;
	exports fr.inria.corese.sparql.triple.update;
	exports fr.inria.corese.sparql.triple.function.script;
	exports fr.inria.corese.sparql.triple.function.extension;
	exports fr.inria.corese.sparql.triple.function.term;
	exports fr.inria.corese.sparql.triple.function.proxy;
	exports fr.inria.corese.sparql.compiler.java;
	exports fr.inria.corese.sparql.datatype.function;
	exports fr.inria.corese.sparql.storage.api;
	exports fr.inria.corese.sparql.storage.util;
	exports fr.inria.corese.sparql.triple.printer;
	exports fr.inria.corese.sparql.triple.api;
	exports fr.inria.corese.sparql.triple.function.core;
	exports fr.inria.corese.sparql.storage.fs;
	exports fr.inria.corese.sparql.rdf4j;
}
