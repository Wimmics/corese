module fr.inria.corese.corese_storage {
    requires transitive fr.inria.corese.corese_core;
    requires org.apache.jena.tdb;
    requires transitive org.apache.jena.arq;
    requires transitive org.apache.jena.core;
    requires transitive org.apache.jena.base;
    requires com.google.common;

    exports fr.inria.corese.storage.jenatdb1.convertDatatype;
    exports fr.inria.corese.storage.jenatdb1;
}
