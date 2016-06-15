package fr.inria.edelweiss.kgram.api.query;

import fr.inria.edelweiss.kgram.core.BgpGenerator;

/**
 *
 * @author corby
 */
public interface DQPFactory {
    
    BgpGenerator instance();
    
}
