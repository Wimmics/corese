
package fr.inria.corese.kgram.api.core;

/**
 *
 * Interface for (var in exp) {}
 * @author Olivier Corby 2015
 */
public interface Loopable<T> {
    
    Iterable<T> getLoop();
    
}
