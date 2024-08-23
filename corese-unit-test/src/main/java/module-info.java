module fr.inria.corese.corese_test {
    requires fr.inria.corese.sparql;
    requires fr.inria.corese.corese_core;
    requires fr.inria.corese.corese_storage;
    requires fr.inria.corese.compiler;
    requires java.logging;
    requires java.xml;
    requires transitive org.apache.jena.core;
    requires transitive org.apache.jena.tdb;
    requires transitive org.apache.jena.arq;
    requires transitive org.apache.jena.base;
    requires transitive org.apache.jena.ext.com.google;
    requires transitive org.apache.jena.iri;
    requires com.google.common;
    requires org.slf4j;
    requires org.apache.logging.log4j;
}