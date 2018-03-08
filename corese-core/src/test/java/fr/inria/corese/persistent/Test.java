package fr.inria.corese.persistent;

import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.fs.StringManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Test.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015
 */
public class Test {

    private IStorage manager;
    private Map<Integer, String> literalsAll = new HashMap<Integer, String>();
    private int min, max;
    private int index = 1;

    public Test(int min, int max) {
        this.min = min;
        this.max = max;
        manager = StringManager.create();
    }

    public void close() {
        manager.clean();
    }

    public double[] testSpeed(int num) throws IOException {
        double[] r = new double[7];
        int c = 0;
        r[c++] = this.min;
        r[c++] = this.max;
        //Constants.MAX_LIT_LEN = min;

        double[] writeNIO = this.writeNIO(num, true);
        r[c++] = writeNIO[0];
        r[c++] = writeNIO[1];
        r[c++] = writeNIO[2];

        double[] read = this.read(false, 0, true);
        r[c++] = read[0];
        r[c++] = read[1];
        System.out.println(manager.toString());
        return r;
    }

    public void testCorrectness(int feed, int repeat, boolean random) {
        int counter = repeat;

        while (counter-- > 0) {
            writeNIO(10 * feed, random);
            read(true, 2 * feed, true);
            delete(3 * feed);
            read(true, 1 * feed, true);
            delete(2 * feed);
            read(true, 1 * feed, true);
            System.out.println(manager);
        }
    }

    private long generate(Map<Integer, String> literalStrings, int count) {
        //Generate ramdom strings
        Random r = new Random();
        int c = count;
        long length = 0;

        String s;

        while (c-- > 0) {
            s = RandomStringUtils.random(r.nextInt(max - min) + min, true, true);
            length += s.length();
            literalStrings.put(index++, s);
        }

        this.literalsAll.putAll(literalStrings);
        System.out.println("[Generate]: " + count + " records generated.");
        return length / count;
    }
        
    public double[] writeNIO(int nRecords, boolean generated) {
        // === 1 generate strings ===
        Map<Integer, String> literalsTmp = new HashMap<Integer, String>();

        long avg = generated ? this.generate(literalsTmp, nRecords) : this.generate(literalsTmp, TestSuite.texts);

        // === 2 write to files ===
        long start = System.currentTimeMillis();
        int counter = 0;

        for (Map.Entry<Integer, String> entrySet : literalsTmp.entrySet()) {
            Integer key = entrySet.getKey();
            String value = entrySet.getValue();
            //if (manager.check(value)) {
                manager.write(key, value);
                counter++;
            //}
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("[Write]: " + (generated ? nRecords : TestSuite.texts.length) + " records wrote to file.");
        return new double[]{(double) avg, time * 1.0 / 1000, time * 1.0 / counter};
    }

    private long generate(Map<Integer, String> literalStrings, String[] strings) {
        long lengths = 0;
        for (String s : strings) {
            literalStrings.put(index++, s);
            lengths += s.length();
        }

        literalsAll.putAll(literalStrings);
        return lengths / strings.length;
    }

    public double[] testWriteIO() throws IOException {
        long start = System.currentTimeMillis();
        int counter = 0;
        File file = new File("/Users/fsong/NetBeansProjects/", "CoreseLit" + System.currentTimeMillis() + ".txt");
        // creates the file
        file.createNewFile();
        // creates a FileWriter Object
        FileWriter writer = new FileWriter(file);
        // Writes the content to the file

        for (Map.Entry<Integer, String> entrySet : literalsAll.entrySet()) {
            //Integer key = entrySet.getKey();
            String value = entrySet.getValue();
            if (manager.check(value)) {
                writer.write(value);
                counter++;
            }
        }
        writer.flush();
        writer.close();
        long time = System.currentTimeMillis() - start;

        return new double[]{time * 1.0 / 1000, time * 1.0 / counter};
    }

    public double[] read(boolean random, int nRecords, boolean check) {
        Map<Integer, String> literalsRead = new HashMap<Integer, String>();
        long start = System.currentTimeMillis();
        int correct = 0;

        if (random) {
            int size = literalsAll.size();
            Random r = new Random();
            int counter = nRecords;

            Integer[] keys = literalsAll.keySet().toArray(new Integer[size]);
            while (counter > 0) {
                int key = keys[r.nextInt(size)];
                //if(literalsRead.containsKey(ind)){
                literalsRead.put(key, manager.read(key));
                counter--;
                //}
            }
        } else {
            for (Map.Entry<Integer, String> entrySet : literalsAll.entrySet()) {
                Integer key = entrySet.getKey();
                literalsRead.put(key, manager.read(key));
            }
        }

        long time = System.currentTimeMillis() - start;
        if (check) {
            //check if read correctly
            for (Map.Entry<Integer, String> entrySet : literalsRead.entrySet()) {
                Integer key = entrySet.getKey();

                if (literalsAll.get(key).equals(literalsRead.get(key))) {
                    correct++;
                } else {
                   // System.out.println("[Read Error]: "+((StringManager)manager).getLiteralsOnDiskMeta(key)+" ====");
                    //System.out.println("=== orginal literal ===\n" + literalsAll.get(key));
                    //System.out.println("\n=== read literal ===\n" + literalsRead.get(key));
                }
            }

            System.out.println("[Read]: " + literalsRead.size() + " / " + correct + ", "
                    + (literalsRead.size() == correct ? "!! OK !!" : "** FAI L**"));
        }
        return new double[]{time * 1.0 / 1000, time * 1.0 / literalsRead.size()};
    }

    public void delete(int nRecords) {
        //generate randomly the literals to delete
        int size = literalsAll.size();
        Random r = new Random();
        int counter = nRecords;
        int nDeleted = 0;
        Integer[] ids = literalsAll.keySet().toArray(new Integer[literalsAll.size()]);
        while (counter-- > 0) {
            int id = ids[r.nextInt(size)];
            if (literalsAll.containsKey(id)) {//maybe has been deleted
                manager.delete(id);
                literalsAll.remove(id);
                nDeleted++;
            }
        }

        System.out.println("[Delete from RAM]: " + nDeleted + " records deleted.");
        if (literalsAll.isEmpty()) {
            System.exit(0);
        }
    }

}
