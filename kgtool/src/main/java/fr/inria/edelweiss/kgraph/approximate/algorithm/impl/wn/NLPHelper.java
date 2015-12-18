package fr.inria.edelweiss.kgraph.approximate.algorithm.impl.wn;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.sussex.nlp.jws.JWS;
import static fr.inria.edelweiss.kgraph.approximate.algorithm.Parameters.POS_TAGGER;
import static fr.inria.edelweiss.kgraph.approximate.algorithm.Parameters.WN_PATH;
import static fr.inria.edelweiss.kgraph.approximate.algorithm.Parameters.WN_VER;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NLP helper, load wordnet, pos tagger
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 22 oct. 2015
 */
public class NLPHelper {
    private static final NLPHelper helper = null;
    private static JWS jws = null;
    private static MaxentTagger tagger = null;
    
    public static final String NOUN = "n", VERB = "v", OTHER = "T";
    public static final String[] skipped_words = {"be"};

    private NLPHelper() {
    }

    /**
     * Create an instance of NLP helper
     * @return
     * @throws Exception If WordNet and POS tagger are not setup
     */
    public static NLPHelper createInstance() throws Exception {
        return createInstance(WN_PATH, WN_VER, POS_TAGGER);
    }

    public static NLPHelper createInstance(String WNPath, String WNVersion, String taggerFile) throws Exception {
        if (helper != null) {
            return helper;
        }

        //WordNet
        if (WNPath == null || WNVersion == null) {
            throw (new Exception("Error: WordNet dict path and/or version are not specified!"));
        }

        try {
            if (jws == null) {
                jws = new JWS(WNPath, WNVersion);
            }
        } catch (Exception ex) {
            throw (new Exception("Cannot initialize JWS at '" + WNPath + "', version:" + WNVersion, ex));
        }

        //POS tagger
        if (taggerFile == null) {
            throw (new Exception("Error: POS tagger model file is not specified!"));
        }

        try {
            if (tagger == null) {
                tagger = new MaxentTagger(taggerFile);
            }
        } catch (Exception ex) {
            throw (new Exception("Cannot initialize POS tagger: " + taggerFile, ex));
        }

        return new NLPHelper();
    }

    /**
     * Return instance of JWS (string metrics library)
     * @return 
     */
    public JWS getJws() {
        return jws;
    }

    /**
     * Tag words
     * @param text
     * @return 
     */
    public Map<String, Segement> tag(String text) {
        //String[]words = split(text);
        PTBTokenizer<Word> t = PTBTokenizer.newPTBTokenizer(new BufferedReader(new StringReader(text)));
        Segement segVerb = new Segement(VERB);
        Segement segNoun = new Segement(NOUN);
        Segement segOther = new Segement(OTHER);
        List<TaggedWord> taggedWords = tagger.tagSentence(t.tokenize());
        Morphology m = new Morphology();// get base form of words
        for (TaggedWord tw : taggedWords) {
            String pos = tw.tag();
            String word = tw.value();
            String baseForm = m.lemma(word, pos);

            if (pos.startsWith(VERB.toUpperCase())) {
                segVerb.addWord(baseForm);
            }

            if (pos.startsWith(NOUN.toUpperCase())) {
                segNoun.addWord(baseForm);
            }

            if (pos.startsWith("J") || pos.startsWith("RB") || pos.startsWith("W")) {
                segOther.addWord(baseForm);
            }
        }

        Map<String, Segement> map = new HashMap<String, Segement>();
        map.put(segVerb.getPos(), segVerb);
        map.put(segNoun.getPos(), segNoun);
        map.put(segOther.getPos(), segOther);
        return map;
    }

}
