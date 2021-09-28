package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.core.BgpGenerator;

/**
 *
 * @author corby
 */
public interface DQPFactory {
    
    BgpGenerator instance();
    
}
