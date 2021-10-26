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
}
