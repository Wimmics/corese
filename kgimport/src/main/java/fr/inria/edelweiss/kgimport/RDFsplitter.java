/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgimport;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class RDFsplitter {

//    static String inputDirPath = "/Users/gaignard/Documents/Experiences/jows-2013/KEGG-2010-11";
    static String inputDirPath = "/Users/gaignard/Documents/Experiences/ExpeFedBench-2013/FedBench-DS-2013/kegg/KEGG-2010-11";
    static String outputDirPath = "/tmp/frag/";
    static int fragNb = 4;

    public static void main(String args[]) throws FileNotFoundException {

        File oDir = new File(outputDirPath);
        if (oDir.exists()) {
            System.out.println(outputDirPath + " already exists !");
            System.exit(1);
        } else {
            if (oDir.mkdir()) {
                System.out.println(outputDirPath + " created !");
            }
        }

        Model model = ModelFactory.createDefaultModel();
        File inputDir = new File(inputDirPath);
        if (inputDir.isDirectory()) {
            for (File f : inputDir.listFiles()) {
                System.out.println("Loading " + f.getAbsolutePath());
                InputStream iS = new FileInputStream(f);
                if (f.getAbsolutePath().endsWith(".n3")) {
                    model.read(iS, null, "N3");
                } else if (f.getAbsolutePath().endsWith(".nt")) {
                    model.read(iS, null, "N-TRIPLES");
                } else if (f.getAbsolutePath().endsWith(".rdf")) {
                    model.read(iS, null);
                }
                
                System.out.println("Loaded " + model.size() + " triples");
            }
        }

        long fragSize = Math.round(model.size() / fragNb) + 1;

        int i = 1;
        Model fragment = ModelFactory.createDefaultModel();
        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            fragment.add(it.nextStatement());
            if (fragment.size() == fragSize) {
                File oF = new File(outputDirPath + "/fragment-" + i + ".rdf");
                OutputStream oS = new FileOutputStream(oF);
                fragment.write(oS, "RDF/XML");
                System.out.println("Writen " + oF.getAbsolutePath() + " - size = " + fragment.size() + " triples");

                i++;
                fragment = ModelFactory.createDefaultModel();
            }
        }

        File oF = new File(outputDirPath + "/fragment-" + i + ".rdf");
        OutputStream oS = new FileOutputStream(oF);
        fragment.write(oS, "RDF/XML");
        i++;
        System.out.println("Writen " + oF.getAbsolutePath() + " - size = " + fragment.size() + " triples.");
    }
}
