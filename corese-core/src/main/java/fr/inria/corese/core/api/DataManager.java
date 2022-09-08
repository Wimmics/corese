package fr.inria.corese.core.api;

import fr.inria.corese.core.producer.MetadataManager;

/**
 * Interface to adapt an external graph implementation to Corese.
 * 
 * {@code DataManagerRead} for {@code select where} SPARQL queries.
 * {@code DataManagerUpdate} for {@code update} and {@code construct} queries.
 * 
 * @author Olivier Corby
 * @author Rémi ceres
 */
public interface DataManager extends DataManagerRead, DataManagerUpdate {

    default boolean hasMetadataManager() {
        return getMetadataManager()!=null;
    }
    
    default MetadataManager getMetadataManager() {
        return null;
    }
    
    default MetadataManager getCreateMetadataManager() {
        if (!hasMetadataManager()) {
            setMetadataManager(new MetadataManager(this));
        }
        return getMetadataManager();
    }
    
    default void setMetadataManager(MetadataManager mgr) {};

    default void startWriteTransaction() {
    };

    default void endWriteTransaction() {
    };

    default void startReadTransaction() {
    };

    default void endReadTransaction() {
    };

    default void abortTransaction() {
    };

    default boolean isInTransaction() {
        return false;
    };

    default boolean isInReadTransaction() {
        return false;
    };

    default boolean isInWriteTransaction() {
        return false;
    };

    default String getStoragePath() {
        return null;
    };

}
