package fr.inria.corese.core.api;

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

    default void startReadTransaction() {
    };

    default void startWriteTransaction() {
    };

    default void endTransaction() {
    };

    default void abortTransaction() {
    };

    default void commitTransaction() {
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
