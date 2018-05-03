package fr.inria.corese.core.load.rdfa;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.semarglproject.rdf.core.ParseException;
import org.semarglproject.rdf.rdfa.RdfaParser;
import org.semarglproject.source.StreamProcessor;

/**
 * Load RDFa
 *
 * @author Fuqi Song, wimmics inria i3s
 * @date 27 Jan 2014 new
 */
public class RDFaLoader {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(RDFaLoader.class);

    private Reader reader;
    private InputStream is;
    private String base;

    RDFaLoader(InputStream r, String base) {
        this.is = r;
        this.base = base;
    }

    RDFaLoader(Reader r, String base) {
        this.reader = r;
        this.base = base;
    }

    public static RDFaLoader create(InputStream read, String base) {
        RDFaLoader p = new RDFaLoader(read, base);
        return p;
    }

    public static RDFaLoader create(Reader read, String base) {
        RDFaLoader p = new RDFaLoader(read, base);
        return p;
    }

    public static RDFaLoader create(String file) {
        FileReader read;
        try {
            read = new FileReader(file);
            RDFaLoader p = new RDFaLoader(read, file);
            return p;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Load triples from RDFa file to graph
     *
     * @param sink
     * @throws ParseException
     */
    public void load(CoreseRDFaTripleSink sink) throws ParseException {

        StreamProcessor processor = new StreamProcessor(RdfaParser.connect(sink));
        processor.process(this.reader, this.base);
    }

}
