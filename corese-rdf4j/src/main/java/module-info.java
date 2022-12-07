module fr.inria.corese.corese_rdf4j {
    requires transitive fr.inria.corese.corese_core;
    requires fr.inria.corese.sparql;
    requires rdf4j.model;
    requires rdf4j.model.vocabulary;
    requires rdf4j.model.api;

    exports fr.inria.corese.rdf4j;
}