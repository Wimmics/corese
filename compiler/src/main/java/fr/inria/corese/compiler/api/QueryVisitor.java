package fr.inria.corese.compiler.api;

import fr.inria.corese.kgram.core.Mappings;

public interface QueryVisitor extends fr.inria.corese.sparql.api.QueryVisitor {

    default Mappings getMappings() {
        return null;
    }

}
