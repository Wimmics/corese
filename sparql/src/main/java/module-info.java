module fr.inria.corese.sparql {
	requires fr.inria.corese.kgram;
	requires org.slf4j;
	requires java.sql;
    requires org.json;

	exports fr.inria.corese.sparql.triple.parser;
	exports fr.inria.corese.sparql.exceptions;
	exports fr.inria.corese.sparql.datatype;
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
}
