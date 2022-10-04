package fr.inria.corese.server.webservice;

import fr.boreal.model.kb.api.FactBase;
import fr.boreal.storage.builder.StorageBuilder;
import fr.inria.corese.core.query.StorageFactory;
import fr.inria.corese.rdf4j.Rdf4jDataManager;
import fr.inria.corese.storage.inteGraal.InteGraalDataManager;
import fr.inria.corese.storage.jenatdb1.JenaDataManager;

/**
 *
 */
public class DatasetManager
        extends fr.inria.corese.core.query.DatasetManager {

    public DatasetManager() {
    }

    @Override
    public DatasetManager init() {
        super.init();
        return this;
    }

    // define db data manager, whatever mode is
    @Override
    public void defineDataManager(TypeDataBase typeDB, String path) {
        super.defineDataManager(typeDB, path);
        if (typeDB == TypeDataBase.JENATDB1) {
            StorageFactory.defineDataManager(path, new JenaDataManager(path));
        } else if (typeDB == TypeDataBase.RDF4J) {
            StorageFactory.defineDataManager(path, new Rdf4jDataManager());
        } else if (typeDB == TypeDataBase.INTEGRAAL_MEMORY) {
            StorageFactory.defineDataManager(path, new InteGraalDataManager());
        } else if (typeDB == TypeDataBase.INTEGRAAL_SQL) {
            FactBase factBase = StorageBuilder.defaultBuilder().useSQLiteDB(path).build().get();
            StorageFactory.defineDataManager(path, new InteGraalDataManager(factBase));
        }
    }

}
