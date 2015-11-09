package fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.JiangAndConrath;
import edu.sussex.nlp.jws.LeacockAndChodorow;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Resnik;
import edu.sussex.nlp.jws.WuAndPalmer;
import static fr.inria.edelweiss.kgraph.approximate.similarity.ISimAlgorithm.MIN;

/**
 * String metrics using WordNet
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 22 oct. 2015
 */
public class StringMetrics {

    private Type type;

    public enum Type {
        LeacockAndChodorow,
        WuAndPalmer,
        Resnik,
        Lin,
        JiangAndConrath
    };
    
    private Lin lin;
    private WuAndPalmer wp;
    private JiangAndConrath jc;
    private LeacockAndChodorow lc;
    private Resnik res;

    public StringMetrics(Type sm, JWS jws) {
        this.type = sm;

        switch (sm) {
            case Lin:
                this.lin = jws.getLin();
                break;
            case JiangAndConrath:
                jc = jws.getJiangAndConrath();
                break;
            case LeacockAndChodorow:
                lc = jws.getLeacockAndChodorow();
                break;
            case Resnik:
                res = jws.getResnik();
                break;
            case WuAndPalmer:
                this.wp = jws.getWuAndPalmer();
                break;
        }
    }

    public double calculate(String w1, String w2, String pos) {
        switch (type) {
            case Lin:
                return this.lin.max(w1, w2, pos);
            case JiangAndConrath:
                return this.jc.max(w1, w2, pos);
            case LeacockAndChodorow:
                return this.lc.max(w1, w2, pos);
            case Resnik:
                return this.res.max(w1, w2, pos);
            case WuAndPalmer:
                return this.wp.max(w1, w2, pos);
        }
        return MIN;
    }
}
