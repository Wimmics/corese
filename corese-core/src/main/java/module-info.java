module fr.inria.corese.corese_core {
	exports fr.inria.corese.core.load;
	exports fr.inria.corese.core;
	exports fr.inria.corese.core.query;
	exports fr.inria.corese.core.rule;
	exports fr.inria.corese.core.workflow;
	exports fr.inria.corese.core.transform;
	exports fr.inria.corese.core.util;
	exports fr.inria.corese.core.print;
	requires transitive fr.inria.corese.compiler;
	requires org.slf4j;
	requires java.logging;
	requires java.xml;
	requires jsonld.java;
	requires arp;
	requires java.ws.rs;
	requires java.sql;
	requires sesame.rio.api;
	requires sesame.model;
	requires commons.lang;
	requires semargl.core;
	requires semargl.rdfa;
	requires java.management;
	requires jdk.management;
}
