module fr.inria.corese.sparql {
	//requires fr.inria.corese.kgram;
	requires java.desktop;
	requires org.slf4j;
	requires java.sql;
	requires org.json;
	requires org.apache.commons.text;
        
	exports fr.inria.corese.kgram.core;
	exports fr.inria.corese.kgram.api.core;
	exports fr.inria.corese.kgram.api.query;
	exports fr.inria.corese.kgram.filter;
	exports fr.inria.corese.kgram.event;
	exports fr.inria.corese.kgram.tool;
	exports fr.inria.corese.kgram.sorter.core;
	exports fr.inria.corese.kgram.path;
	exports fr.inria.corese.kgram.sorter.impl.qpv1;

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
}
