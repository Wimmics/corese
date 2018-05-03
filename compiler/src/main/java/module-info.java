module fr.inria.corese.compiler {
	requires transitive fr.inria.corese.sparql;
	requires org.slf4j;
	requires java.xml;
	requires java.sql;
	exports fr.inria.corese.compiler.parser;
	exports fr.inria.corese.compiler.eval;
	exports fr.inria.corese.compiler.api;
    exports fr.inria.corese.compiler.result;
}
