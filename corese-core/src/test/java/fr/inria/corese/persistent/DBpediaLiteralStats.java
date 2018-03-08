package fr.inria.corese.persistent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * DBpediaLiteralStats.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 2 mars 2015
 */
public class DBpediaLiteralStats {

    public static void main(String[] args) throws IOException {
        long nb = 0, space = 0;
        int[][] data = load();

        for (int[] d : data) {
            nb += d[1];
            space += d[0] * d[1];
        }

        System.out.println("nb: " + nb + ", space: " + space + ", avg:" + (space / nb));

        for (int rl : range_len) {
            long nb_tmep = 0, space_temp = 0;
            for (int[] d : data) {
                if (d[0] <= rl) {
                    nb_tmep += d[1];
                    space_temp += d[0] * d[1];
                }
            }

            System.out.println(rl + ": \t" +nb_tmep+"\t"+ (nb_tmep * 1.0 / nb) + "\t" +space_temp+"\t"+ (space_temp * 1.0 / space));
        }
    }

    public static int[][] load() throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(new File("/Users/fsong/Google Drive/mac osx/5. Persistent/dbpedia_literal.txt"));

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        int[][] data = new int[9338][2];
        String line = null;
        int i = 0;

        while ((line = br.readLine()) != null) {
            //System.out.println(line);
            String[] s = line.split(",");
            data[i][0] = Integer.valueOf(s[0]);
            data[i][1] = Integer.valueOf(s[1]);
            i++;
        }

        br.close();
        return data;
    }
    public static int[] range_len = {1, 2, 3, 4, 5, 6, 7, 8, 9, 19, 29, 39, 49, 59, 69, 79, 89, 99, 199, 299, 399, 499, 599, 699, 799, 899, 999, Integer.MAX_VALUE};
}
