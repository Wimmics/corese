package fr.inria.corese.test.dev.shex;

import fr.inria.corese.shex.Shex;


/**
 *
 * @author corby
 */
public class TestShex {

    static String data = Shex.class.getClassLoader().getResource("data/").getPath();
    static String shex = Shex.class.getClassLoader().getResource("data/shex/").getPath();
    static String demo = "/user/corby/home/AADemo/ashex/";

    public static void main(String[] args) throws Exception {
        //StringBuilder sb = new Shex().parse(shex + "test1.shex");
        StringBuilder sb = new Shex().parse(demo + "test1.shex");
        System.out.println(sb);
    }

}
