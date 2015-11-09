package fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn;

import fr.inria.edelweiss.kgraph.approximate.aggregation.AlgType;
import fr.inria.edelweiss.kgraph.approximate.similarity.Utils;
import fr.inria.edelweiss.kgraph.approximate.similarity.impl.BaseAlgorithm;
import static fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn.NLPHelper.NOUN;
import static fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn.NLPHelper.OTHER;
import static fr.inria.edelweiss.kgraph.approximate.similarity.impl.wn.NLPHelper.VERB;
import java.util.List;
import java.util.Map;

/**
 * TextSimilarity.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 26 oct. 2015
 */
public class TextSimilarity extends BaseAlgorithm {

    public static void main(String[] args) throws Exception {
        NLPHelper nlp = NLPHelper.createInstance();
        TextSimilarity ts = new TextSimilarity(AlgType.wn, nlp);
        double d = ts.calculate("Dog likes vegetables, but it is an animal", "Humain likes meat, because they are animals");
        System.out.println(d);

        d = ts.calculate("John", "John");
        System.out.println(d);
    }
    private WordSimilarity alg;
    private NLPHelper nlp;

    public enum StringMetrics {

        LeacockAndChodorow,
        WuAndPalmer,
        Resnik,
        Lin,
        JiangAndConrath
    };

    public TextSimilarity(AlgType type, NLPHelper nlp) {
        this(type, nlp, StringMetrics.valueOf(NLPHelper.STRING_METRIC));
    }

    public TextSimilarity(AlgType type, NLPHelper nlp, StringMetrics sm) {
        super(type);
        this.nlp = nlp;
        this.alg = new WordSimilarity(sm, nlp.getJws());
    }

    @Override
    public double calculate(String text1, String text2) {
        //pos -> segement
        Map<String, Segement> segements1 = nlp.tag(text1);//s1
        Map<String, Segement> segements2 = nlp.tag(text2);//s2

        String[] poss = new String[]{NOUN, VERB, OTHER};

        //Utils.show("WN Similarity:", "***", "***", 0);
        double sim = this.calculate(segements1, segements2, poss);
        double simReverse = this.calculate(segements2, segements1, poss);

        sim = (sim + simReverse) / 2.0d;
        Utils.show("WN", text1, text2, sim);
        return sim;
    }

    private double calculate(Map<String, Segement> segements1, Map<String, Segement> segements2, String[] poss) {
        double simSum = 0, idfSum = 0;
        for (String pos : poss) {

            Segement seg1 = segements1.get(pos);
            Segement seg2 = segements2.get(pos);
            if (seg1.getWords().isEmpty() || seg2.getWords().isEmpty()) {
                continue;
            }

            double[] sim = this.calculateMax(seg1.getWords(), seg2.getWords(), pos);
            double[] result = this.aggregate(sim, seg1.getIdf());

            simSum += result[0];
            idfSum += result[1];
        }

        if (idfSum == 0) {
            return 0;
        }
        return simSum / idfSum;
    }

    private double[] calculateMax(List<String> ls1, List<String> ls2, String pos) {
        double[] similarities = new double[ls1.size()];
        for (int i = 0; i < ls1.size(); i++) {
            similarities[i] = this.calculateMax(ls1.get(i), ls2, pos);
        }

        return similarities;
    }

    private double calculateMax(String w1, List<String> ls, String pos) {
        double simMax = SIM_MIN;
        String wMax = "";
        for (String w2 : ls) {
            double sim = pos.equals(OTHER) ? calculateLex(w1, w2) : alg.calculate(w1, w2, pos);
            if (sim > simMax) {
                simMax = sim;
                wMax = w2;
            }
            if (simMax >= SIM_MAX) {
                break;
            }
        }
        //Utils.show("WN:", w1, wMax, simMax);
        return simMax;
    }

    private double[] aggregate(double[] simArray, List<Double> idfArray) {
        double sim = 0, idf = 0;
        for (int i = 0; i < simArray.length; i++) {
            sim += simArray[i] * idfArray.get(i);
            idf += idfArray.get(i);
        }
        if (idf == 0) {
            idf = 1;
        }
        return new double[]{sim, idf};
    }

    private double calculateLex(String s1, String s2) {
        return s1.equalsIgnoreCase(s2) ? SIM_MAX : SIM_MIN;
    }
}
