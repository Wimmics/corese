package fr.inria.corese.test.shex;

import fr.inria.corese.shex.shacl.Shex;


/**
 *
 * @author corby
 */
public class TestShex {

//    static String data = Shex.class.getClassLoader().getResource("data/").getPath();
    static String demo = "/user/corby/home/AADemo/ashex/";

    public static void main(String[] args) throws Exception {
        //StringBuilder sb = new Shex().parse(shex + "test1.shex");
        //StringBuilder sb = new Shex().setExtendShacl(true).parse(demo + "test2.shex", demo+"shaclres.ttl");
        StringBuilder sb = new Shex()
                .setExtendShacl(true)
                //.setCardinality(false)
                //.setClosed(false)
                .parse(demo + "human4.shex", demo+"human4shex.ttl");
                //.parse(demo + "test3.shex", demo+"test3shex.ttl");
                //.parse(demo + "shex.shex", demo+"shexshex.ttl");
        System.out.println(sb);
    }

}
