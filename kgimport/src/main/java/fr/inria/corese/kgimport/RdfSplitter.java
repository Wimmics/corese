/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgimport;

import com.google.common.io.Files;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A simple tool to fragment RDF data into homogeneous segments (same size),
 * inhomogeneous (segments described by percentages of the input dataset),
 * vertical fragments (described by a string used as predicate filters).
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class RdfSplitter {

    private static Logger logger = LogManager.getLogger(RdfSplitter.class);

    /**
     * The input directory describing the RDF dataset to be fragmented.
     */
    private String inputDirPath = null;

    /**
     * The output directory containg the RDF fragments.
     */
    private String outputDirPath = null;

    /**
     * The list of fragment percentages. The sum of percentages should be less
     * or equals than 100.
     */
    private ArrayList<Integer> fragList = new ArrayList<Integer>();

    /**
     * The list of predicate filters. If a filter is contained in the URI of an
     * RDF predicate, it is then matched and and included into the corresponding
     * RDF fragment.
     */
    private ArrayList<String> inputPredicates = new ArrayList<String>();

    /**
     * The number of fragments used in homogeneous fragmentation (fragmets with
     * the same size).
     */
    private int fragNb = 1;

    /**
     *
     * @return
     */
    public String getInputDirPath() {
        return inputDirPath;
    }

    /**
     *
     * @param inputDirPath
     */
    public void setInputDirPath(String inputDirPath) {
        this.inputDirPath = inputDirPath;
    }

    /**
     *
     * @return
     */
    public String getOutputDirPath() {
        return outputDirPath;
    }

    /**
     *
     * @param outputDirPath
     */
    public void setOutputDirPath(String outputDirPath) {
        this.outputDirPath = outputDirPath;
    }

    /**
     *
     * @return
     */
    public ArrayList<Integer> getFragList() {
        return fragList;
    }

    /**
     *
     * @param fragList
     */
    public void setFragList(ArrayList<Integer> fragList) {
        this.fragList = fragList;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getInputPredicates() {
        return inputPredicates;
    }

    /**
     *
     * @param inputPredicates
     */
    public void setInputPredicates(ArrayList<String> inputPredicates) {
        this.inputPredicates = inputPredicates;
    }

    /**
     *
     * @return
     */
    public int getFragNb() {
        return fragNb;
    }

    /**
     *
     * @param fragNb
     */
    public void setFragNb(int fragNb) {
        this.fragNb = fragNb;
    }

    /**
     * Logs the size of a set of JENA models.
     *
     * @param models input JENA models.
     */
    public void dumpFragSize(Collection<Model> models) {
        int i = 1;
        for (Model m : models) {
            logger.info("Jena Model#" + i + " size: " + m.size());
            i++;
        }
    }

    /**
     * Homogenously & horizontally fragments an input dataset given a number of
     * fragments. The resulting fragments have the same size.
     *
     * @param model the intial RDF dataset to be homogeneously horizontally
     * fragmented.
     * @param nbFragments the number of produced fragments.
     * @return a collection of fragments. Joined together they correspond
     * exactly to the input dataset.
     */
    public Collection<Model> getFragHoriz(Model model, int nbFragments) {
        logger.info("Starting homogeneous horizontal fragmentation");
        ArrayList<Model> resFrags = new ArrayList<Model>();
        long fragSize = Math.round(model.size() / nbFragments) + 1;

        Model fragment = ModelFactory.createDefaultModel();
        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            Statement st = it.nextStatement();
            fragment.add(st);
            if (fragment.size() == fragSize) {
                resFrags.add(fragment);
                fragment = ModelFactory.createDefaultModel();
            }
        }
        resFrags.add(fragment);
//        dumpFragSize(resFrags);
        return resFrags;
    }

    /**
     * Inhomogenously & horizontally fragments an input dataset given a list of
     * percentages. The resulting fragments have sizes corresponding to the
     * input list of percentages. If the sum of percentages is superior to 100,
     * only the first fragments are considered until the whole input dataset is
     * processed.
     *
     * @param model the intial RDF dataset to be inhomogeneously horizontally
     * fragmented.
     * @param fragList the list of size percentages.
     * @return a collection of fragments. Joined together they correspond
     * exactly to the input dataset.
     */
    public Collection<Model> getFragHoriz(Model model, ArrayList<Integer> fragList) {
        logger.info("Starting inhomogeneous horizontal fragmentation");
        ArrayList<Model> resFrags = new ArrayList<Model>();

        int i = 1;
        boolean done = false;
        int frag = fragList.get(i - 1);
        long fragSize = Math.round(model.size() * frag / 100) + 1;

        Model fragment = ModelFactory.createDefaultModel();
        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            Statement st = it.nextStatement();
            fragment.add(st);
            if ((!done) && (fragment.size() == fragSize)) {
                resFrags.add(fragment);

                fragment = ModelFactory.createDefaultModel();
                i++;
                if (i > fragList.size()) {
                    done = true;
                } else {
                    frag = fragList.get(i - 1);
                    fragSize = Math.round(model.size() * frag / 100) + 1;
                }
            }
        }
        resFrags.add(fragment);

//        dumpFragSize(resFrags);
        return resFrags;
    }

    /**
     * Vertically fragments the input RDF dataset based on a list of predicate
     * filters. All triples matching a predicate filter are stored in a fragment
     * corresponding to the predicate filter.
     * <p>
     * As an example, we could fragment a DBpedia dataset based on the following
     * list pf filters ("foaf", "dbpedia"). The fragmentation would lead to two
     * fragments. The first one will store all triples whose predicate URI
     * contains "foaf", while the second one will store all triples whose
     * predicate URI contains "dbpedia".
     * </p>
     *
     * @param model the intial RDF dataset to be vertically partitionned.
     * @param inputPredicates the list of predicate filters.
     * @return a map which associate each fragment to its filter.
     */
    public HashMap<String, Model> getFragVert(Model model, ArrayList<String> inputPredicates) {
        logger.info("Starting vertical fragmentation");

        // Pred->Model map initialization
        HashMap<String, Model> fragments = new HashMap<String, Model>();
        for (String key : inputPredicates) {
            fragments.put(key, ModelFactory.createDefaultModel());
        }
        fragments.put("Other", ModelFactory.createDefaultModel());

        // Vertical fragmentation
        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            Statement st = it.nextStatement();
            Property pred = st.getPredicate();
//            String ns = pred.getNameSpace();
            boolean added = false;
            for (String k : fragments.keySet()) {
                if (pred.toString().contains(k)) {
                    fragments.get(k).add(st);
                    added = true;
                    break;
                }
            }
            if (!added) {
                fragments.get("Other").add(st);
            }
        }

        return fragments;
    }

    /**
     * Saves a set of RDF fragments to RDF files.
     *
     * @param fragments
     * @param namePrefix
     */
    public void saveFragmentsRDF(Collection<Model> fragments, String namePrefix) {
        int i = 1;
        for (Model frag : fragments) {
            File oF = new File(this.getOutputDirPath() + "/" + namePrefix + "-frag-" + i + ".rdf");
            OutputStream oS;
            try {
                oS = new FileOutputStream(oF);
                frag.write(oS, "RDF/XML");
                logger.info("Written " + oF.getAbsolutePath() + " - size = " + frag.size() + " triples");
                i++;
            } catch (FileNotFoundException ex) {
                logger.error("File " + oF.getAbsolutePath() + " not found !");
            }

        }
    }

    /**
     * Saves a set of RDF fragments to RDF files.
     *
     * @param fragments the input fragments to be persisted.
     */
    public void saveFragmentsRDF(HashMap<String, Model> fragments) {
        int i = 1;
        for (String k : fragments.keySet()) {
            Model frag = fragments.get(k);
            File oF = new File(this.getOutputDirPath() + "/" + k.replace("/", "_").replace(":", "_") + "-frag-" + i + ".rdf");
            OutputStream oS;
            try {
                oS = new FileOutputStream(oF);
                frag.write(oS, "RDF/XML");
                logger.info("Written " + oF.getAbsolutePath() + " - size = " + frag.size() + " triples");
                i++;
            } catch (FileNotFoundException ex) {
                logger.error("File " + oF.getAbsolutePath() + " not found !");
            }
        }
    }

    /**
     * Saves a set of RDF fragments to JENA TDB backends.
     *
     * @param fragments the input fragments to be persisted.
     * @param namePrefix the prefix used to name fragments.
     */
    public void saveFragmentsTDB(Collection<Model> fragments, String namePrefix) {
        int i = 1;
        for (Model frag : fragments) {

            String directory = this.getOutputDirPath() + "/" + namePrefix + "-TDB#" + i;
            Dataset dataset = TDBFactory.createDataset(directory);
            dataset.begin(ReadWrite.WRITE);
            Model tdbModel = dataset.getDefaultModel();
            tdbModel.add(frag);
            dataset.commit();
            dataset.end();

            logger.info("Written " + directory + " - size = " + tdbModel.size() + " triples");
            // memory optimization to be done : 
            // tdbModel.removeAll();
            // frag.removeAll();
            i++;
        }
    }

    /**
     * Saves a set of RDF fragments to a JENA TDB backend.
     *
     * @param fragments the input fragments to be persisted.
     */
    public void saveFragmentsTDB(HashMap<String, Model> fragments) {
        int i = 1;
        for (String k : fragments.keySet()) {
            Model frag = fragments.get(k);

            String directory = this.getOutputDirPath() + "/" + k.replace("/", "_").replace(":", "_") + "-TDB#" + i;
            Dataset dataset = TDBFactory.createDataset(directory);
            dataset.begin(ReadWrite.WRITE);
            Model tdbModel = dataset.getDefaultModel();
            tdbModel.add(frag);
            dataset.commit();
            dataset.end();

            logger.info("Written " + directory + " - size = " + tdbModel.size() + " triples");
            i++;
        }
    }

    /**
     * The application entrypoint, configured through the command line input
     * arguments.
     *
     * @param args the input command line arguments.
     */
    public static void main(String args[]) {

        RdfSplitter rdfSplitter = new RdfSplitter();

        Options options = new Options();
        Option helpOpt = new Option("h", "help", false, "Print usage information.");
        Option inDirOpt = new Option("i", "input-dir", true, "The directory containing RDF files to be loaded.");
        Option outDirOpt = new Option("o", "output-dir", true, "The directory containing the generated RDF fragments");
        Option predFiltOpt = new Option("p", "predicate-filter", true, "Predicate filter used to segment the dataset. "
                + "You can use multiple filters, typically one per fragment.");
        Option fragNbOpt = new Option("n", "number-of-fragments", true, "Number of fragments generated for the whole input dataset.");
        Option fragRepOpt = new Option("f", "fractionning-percentage", true, "Percentage of the whole input dataset for this fragment.");
        Option tdbOpt = new Option("tdb", "tdb-storage", false, "RDF fragments are persisted into a Jena TDB backend.");
        Option versionOpt = new Option("v", "version", false, "Print the version information and exit.");
        options.addOption(inDirOpt);
        options.addOption(outDirOpt);
        options.addOption(predFiltOpt);
        options.addOption(helpOpt);
        options.addOption(versionOpt);
        options.addOption(fragNbOpt);
        options.addOption(fragRepOpt);
        options.addOption(tdbOpt);

        String header = "RDF data fragmentation tool command line interface";
        String footer = "\nPlease report any issue to alban.gaignard@cnrs.fr";

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar [].jar", header, options, footer, true);
                System.exit(0);
            }

            if (!cmd.hasOption("i")) {
                logger.warn("You must specify a valid input directory !");
                System.exit(-1);
            } else {
                rdfSplitter.setInputDirPath(cmd.getOptionValue("i"));
            }
            if (!cmd.hasOption("o")) {
                logger.warn("You must specify a valid output directory !");
                System.exit(-1);
            } else {
                rdfSplitter.setOutputDirPath(cmd.getOptionValue("o"));
            }
            if (cmd.hasOption("p")) {
                rdfSplitter.setInputPredicates(new ArrayList<String>(Arrays.asList(cmd.getOptionValues("p"))));
            }
            if (cmd.hasOption("f")) {
                ArrayList<String> opts = new ArrayList<String>(Arrays.asList(cmd.getOptionValues("f")));
                for (String opt : opts) {
                    try {
                        rdfSplitter.getFragList().add(Integer.parseInt(opt));
                    } catch (NumberFormatException e) {
                        logger.error(opt + " cannot be pased as an percentage value.");
                        System.exit(-1);
                    }
                }
            }
            if (cmd.hasOption("n")) {
                try {
                    rdfSplitter.setFragNb(Integer.parseInt(cmd.getOptionValue("n")));
                } catch (NumberFormatException e) {
                    logger.error(cmd.getOptionValue("n") + " cannot be pased as an integer value.");
                    System.exit(-1);
                }
            }

            File oDir = new File(rdfSplitter.getOutputDirPath());
            if (oDir.exists()) {
                logger.warn(rdfSplitter.getOutputDirPath() + " already exists !");
                oDir = Files.createTempDir();
                logger.warn(oDir.getAbsolutePath() + " created.");
                rdfSplitter.setOutputDirPath(oDir.getAbsolutePath());
            } else {
                if (oDir.mkdir()) {
                    logger.info(rdfSplitter.getOutputDirPath() + " created.");
                }
            }

            if (!cmd.hasOption("n") && !cmd.hasOption("f") && !cmd.hasOption("p")) {
                logger.error("You must specify just one fragmentation type through '-n', '-f', or 'p' options");
                for (String arg : args) {
                    logger.trace(arg);
                }
                System.exit(-1);
            }

            String fragName = rdfSplitter.getInputDirPath().substring(rdfSplitter.getInputDirPath().lastIndexOf("/") + 1);

            //Input data loading
            Model model = ModelFactory.createDefaultModel();
            File inputDir = new File(rdfSplitter.getInputDirPath());
            if (inputDir.isDirectory()) {
                for (File f : inputDir.listFiles()) {
                    logger.info("Loading " + f.getAbsolutePath());
                    if (f.isDirectory()) {
                        String directory = f.getAbsolutePath();
                        Dataset dataset = TDBFactory.createDataset(directory);
                        dataset.begin(ReadWrite.READ);
                        // Get model inside the transaction
                        model.add(dataset.getDefaultModel());
                        dataset.end();
                    } else {
                        InputStream iS;
                        try {
                            iS = new FileInputStream(f);
                            if (f.getAbsolutePath().endsWith(".n3")) {
                                model.read(iS, null, "N3");
                            } else if (f.getAbsolutePath().endsWith(".nt")) {
                                model.read(iS, null, "N-TRIPLES");
                            } else if (f.getAbsolutePath().endsWith(".rdf")) {
                                model.read(iS, null);
                            }
                        } catch (FileNotFoundException ex) {
                            LogManager.getLogger(RdfSplitter.class.getName()).log(Level.ERROR, "", ex);
                        }
                    }
                }
                logger.info("Loaded " + model.size() + " triples");
            } else {
                System.exit(0);
            }

            StopWatch sw = new StopWatch();
            if (cmd.hasOption("n")) {
                sw.start();
                if (cmd.hasOption("tdb")) {
                    rdfSplitter.saveFragmentsTDB(rdfSplitter.getFragHoriz(model, rdfSplitter.getFragNb()), "Homog-" + fragName);
                } else {
                    rdfSplitter.saveFragmentsRDF(rdfSplitter.getFragHoriz(model, rdfSplitter.getFragNb()), "Homog-" + fragName);
                }
                logger.info("Homog horiz frag in " + sw.getTime() + "ms");
                sw.reset();
            } else if (cmd.hasOption("f")) {
                sw.start();
                if (cmd.hasOption("tdb")) {
                    rdfSplitter.saveFragmentsTDB(rdfSplitter.getFragHoriz(model, rdfSplitter.getFragList()), "Inhomog-" + fragName);
                } else {
                    rdfSplitter.saveFragmentsRDF(rdfSplitter.getFragHoriz(model, rdfSplitter.getFragList()), "Inhomog-" + fragName);
                }
                logger.info("Inhomog horiz frag in " + sw.getTime() + "ms");
                sw.reset();
            } else if (cmd.hasOption("p")) {
                sw.start();
                if (cmd.hasOption("tdb")) {
                    rdfSplitter.saveFragmentsTDB(rdfSplitter.getFragVert(model, rdfSplitter.getInputPredicates()));
                } else {
                    rdfSplitter.saveFragmentsRDF(rdfSplitter.getFragVert(model, rdfSplitter.getInputPredicates()));
                }
                logger.info("Vert frag in " + sw.getTime() + "ms");
                sw.reset();
            }

        } catch (ParseException ex) {
            logger.error("Impossible to parse the input command line " + cmd.toString());
        }
    }
}
