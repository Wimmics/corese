package fr.inria.edelweiss.kgraph.approximate.algorithm.impl.wn;

import java.util.ArrayList;
import java.util.List;

/**
 * Segement for storing the intermediate results of tagged words
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 26 oct. 2015
 */
public class Segement {

    private String pos;
    private List<String> words;
    private List<Double> idfs;

    public Segement(String pos, List<String> words, List<Double> idf) {
        this.pos = pos;
        this.words = words;
        this.idfs = idf;

        if (idf == null) {
            this.idfs = new ArrayList<Double>();
            for (String word : this.words) {
                this.idfs.add(1.0d);
            }
        }
    }

    public Segement(String pos, List<String> words) {
        this(pos, words, null);
    }

    public Segement(String pos) {
        this(pos, new ArrayList<String>(), null);
    }

    public void addWord(String word, double idf) {
        this.words.add(word);
        this.idfs.add(idf);
    }

    public void addWord(String word) {
        this.addWord(word, 1.0d);
    }

    public String getPos() {
        return pos;
    }

    public List<String> getWords() {
        return words;
    }

    public List<Double> getIdf() {
        return idfs;
    }

}
