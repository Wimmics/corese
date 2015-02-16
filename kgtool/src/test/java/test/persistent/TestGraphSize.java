package test.persistent;

import com.javamex.classmexer.MemoryUtil;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.acacia.corese.persistent.api.IOperation;
import fr.inria.acacia.corese.persistent.api.PersistentManager;
import fr.inria.acacia.corese.persistent.ondisk.StringOnDiskManager;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.NodeImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the size of object in memory in bytes TestGraphSize.java
 *
 * Run with
 * javaagent:-javaagent:/Users/fsong/NetBeansProjects/kgram/kgtool/classmexer.jar
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 25 nov. 2014
 */
public class TestGraphSize {

    private static String HOME_DATA = "/Users/fsong/NetBeansProjects/bsbm/data/scale";
    private static String[] scale = new String[]{"100", "300", "500", "1000", "2000", "3000", "5000", "7000", "10000", "15000", "20000", "25000", "30000", "35000"};
    private final static String[] scale0 = new String[]{"100", "300", "500"};
    private static String[] scale2 = new String[]{"25000", "30000", "35000"};
    private static String[] scale3 = new String[]{"40000", "50000", "60000"};

    public static List LITERALS_ALL = new ArrayList();
    private final static int ALL = 0, BN = 1, LITERAL = 2, URI = 3;

    public static void main(String[] args) throws InterruptedException {
        //scale = scale3;
        //System.out.println(args[0]);

//        Parameters.PERSISTENT = true;
//        Parameters.MAX_LIT_LEN = 100;
//        compute("/Users/fsong/NetBeansProjects/bsbm/data/scale100.ttl");
//        System.out.println(PersistentManager.getManager(IOperation.STORAGE_FILE).toString());
//        compute("/Users/fsong/NetBeansProjects/bsbm/data/scale1000.ttl");
//        compute("/Users/fsong/NetBeansProjects/bsbm/data/scale2000.ttl");
        //System.out.println(("This is a test!".getBytes().length));
//        System.gc();
//        Runtime runtime = Runtime.getRuntime();
//        System.out.println("Used Memory:" + convert(runtime.totalMemory() - runtime.freeMemory()));
        loadingTime20k();
    }

    public static void compare() throws InterruptedException {
        compute("/Users/fsong/NetBeansProjects/bsbm/data/scale1000.ttl");
        System.out.println(PersistentManager.getManager(IOperation.STORAGE_FILE));

        compute("/Users/fsong/NetBeansProjects/bsbm/data/scale1000.ttl");
        System.out.println(PersistentManager.getManager(IOperation.STORAGE_FILE));
    }

    public static void loadingTime20k() {
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMaximumFractionDigits(1);

        //double[][] r1 = loadingTime(false);
        double[][] r2 = loadingTime(true);
        //print
        String scales = "Scale:   \t", tripleNb = "Triple nb.:\t",
                ldTime = "LD time:\t", ldTime2 = "LD time IF:\t",
                btGraph = "Size graph:\t", btLit = "Size lit.:\t",
                btGraph2 = "Size graph IF:\t", btLit2 = "Size lit. IF:\t",
                btLitRatio = "Com. r. lit:\t", btGraphRatio = "Com. r. graph:\t",
                nBOndisk = "lit in file:\t";

        for (int i = 0; i < scale.length; i++) {
            scales += scale[i] + "\t";
            tripleNb += (r2[0][i] + "\t");
            //ldTime += (r1[1][i] + "\t");
            ldTime2 += (r2[1][i] + "\t");

            //btGraph += (convert((long) r1[2][i], false) + "\t");
            //btLit += (convert((long) r1[3][i], false) + "\t");
            //btGraph2 += (convert((long) r2[2][i], false) + "\t");
            //btLit2 += (convert((long) r2[3][i], false) + "\t");
            //btGraphRatio += pf.format((r1[2][i] - r2[2][i]) / r1[2][i]) + "\t";
            //btLitRatio += pf.format((r1[3][i] - r2[3][i]) / r1[3][i]) + "\t";
            nBOndisk += pf.format(r2[4][i]) + "\t";
        }

        System.out.println(" =========== results Len = 100 ===========");
        System.out.print(scales + "\n" + tripleNb + "\n" + nBOndisk + "\n");
        System.out.print(ldTime + "\n" + ldTime2 + "\n\n");

        //System.out.print(btGraph + "\n" + btGraph2 + "\n");
        //System.out.print(btGraphRatio + "\n\n");
        //System.out.print(btLit + "\n" + btLit2 + "\n");
        //System.out.print(btLitRatio + "\n\n");
    }

    public static double[][] loadingTime(boolean onfile) {
        double[][] r = new double[5][scale.length];
        for (int i = 0; i < scale.length; i++) {
            IOperation mrg = PersistentManager.create(IOperation.STORAGE_FILE);
            mrg.enabled(onfile);
            String file = HOME_DATA + scale[i] + ".ttl";
            Graph g = Graph.create();
            Load ld = Load.create(g);
            System.out.println("Loading " + file + " ...");
            long begin = System.currentTimeMillis();

            ld.load(file);
            r[1][i] = System.currentTimeMillis() - begin;//loading time
            r[0][i] = g.size();//number of triples in graph

            //r[2][i] = MemoryUtil.deepMemoryUsageOf(g.getIndex());//bytes of graph
            //List allLiterals = getNodes(g, LITERAL);
            //r[3][i] = MemoryUtil.deepMemoryUsageOfAll(allLiterals);//bytes of all literals
            System.out.println("Loaded " + r[0][i] + ", " + scale[i] + ", " + r[1][i] + ", " + convert((long) r[2][i]));
            g.clean();

            System.out.println(mrg.toString());
            if (onfile) {
                r[4][i] = ((StringOnDiskManager) mrg).getLiteralsOnDiskSize() * 1.0d / r[0][i];
                mrg.clean();
            }
        }

        return r;
    }

    public static void compute(String url) throws InterruptedException {
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMaximumFractionDigits(1);

        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(url);

        StringBuffer sb = new StringBuffer("\n#### Graph info.. #####\n");
        long bIndex = MemoryUtil.deepMemoryUsageOf(g.getIndex());

        //NODE
        long bBlank = MemoryUtil.deepMemoryUsageOfAll(getNodes(g, BN));
        long bIndividual = MemoryUtil.deepMemoryUsageOfAll(getNodes(g, URI));
        List lLiteral = getNodes(g, LITERAL);
        long bLiteral = MemoryUtil.deepMemoryUsageOfAll(lLiteral);
        //long bMapLieteral = MemoryUtil.deepMemoryUsageOfAll(g.literal.entrySet());
        //long bMapLieteral =  0 ;
        long bAllNode = bBlank + bIndividual + bLiteral;

        sb.append("G all nodes (" + g.nbNodes() + "): \t").append(convert(bAllNode)).append(" [" + pf.format(bAllNode * 1.0 / bIndex)).append("] , ");
        sb.append("\t each:\t").append(convert(bAllNode / g.nbNodes())).append(", ");
        sb.append("[ref:  " + convert(MemoryUtil.memoryUsageOf(get1Node(g)))).append("]\n");

        sb.append(" - blank (" + g.nbBlanks() + "): \t" + convert(bBlank)).append("\n");

        sb.append(" - uri  (" + g.nbIndividuals() + "): \t").append(convert(bIndividual)).append(" [" + pf.format(bIndividual * 1.0 / bIndex)).append("] , ");
        sb.append(" \t each :\t").append(convert(bIndividual / g.nbIndividuals())).append("\n");

        sb.append(" - lit. (" + g.nbLiterals() + "): \t").append(convert(bLiteral)).append(" [" + pf.format(bLiteral * 1.0 / bIndex)).append("] , ");
        sb.append(" \t each :\t").append(convert(bLiteral / g.nbLiterals())).append("\n");

        //EDGE
        long bAllEdges = MemoryUtil.deepMemoryUsageOfAll(getEdges(g));
        sb.append("G all edges (include nodes) (" + g.size() + "): \t" + convert(bAllEdges)).append(" [" + pf.format((bAllEdges * 1.0 / bIndex)) + "]\n");
        sb.append("            (exclude nodes) (" + g.size() + "): \t" + convert(bAllEdges - bAllNode)).append(" [" + pf.format((bAllEdges - bAllNode) * 1.0 / bIndex)).append("]\n");

        sb.append("G index (include node & edge) (" + g.getIndex().size() + "): \t" + convert(bIndex)).append("\n");
        sb.append("        (exclude node & edge) (" + g.getIndex().size() + "): \t" + convert(bIndex - bAllEdges)).append(", [" + pf.format((bIndex - bAllEdges) * 1.0 / bIndex * 1.0) + "]\n");

        System.out.print(sb);

        //
    }

    public static void compute2(String url) throws InterruptedException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(url);

        //Getting the runtime reference from system
        //Thread.sleep(1 * 1000);
        //System.gc();
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics #####");
        System.out.println("Used Memory:" + convert(runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("Free Memory:" + convert(runtime.freeMemory()));
        System.out.println("Total Memory:" + convert(runtime.totalMemory()));
        System.out.println("Max Memory:" + convert(runtime.maxMemory()));

        StringBuffer sb = new StringBuffer("\n#### Graph info.. #####\n");
        //sb.append("Graph size: " + convert(MemoryUtil.deepMemoryUsageOf(g, ALL))).append("\n");
        //sb.append("Graph size: " + convert(MemoryUtil.memoryUsageOf(g))).append("\n");

        //NODE
        long bBlank = MemoryUtil.deepMemoryUsageOfAll(getNodes(g, BN));
        long bIndividual = MemoryUtil.deepMemoryUsageOfAll(getNodes(g, URI));
        List lLiteral = getNodes(g, LITERAL);
        long bLiteral = MemoryUtil.deepMemoryUsageOfAll(lLiteral);
        //long bMapLieteral = MemoryUtil.deepMemoryUsageOfAll(g.literal.entrySet());
        long bMapLieteral = 0;
        long bAllNode = bBlank + bIndividual + bLiteral;

        sb.append("G all nodes (" + g.nbNodes() + "): \t").append(convert(bAllNode)).append(", ");
        sb.append("\t each:\t").append(convert(bAllNode / g.nbNodes())).append(", ");
        sb.append("[ref:  " + convert(MemoryUtil.memoryUsageOf(get1Node(g)))).append("]\n");

        sb.append(" - blank (" + g.nbBlanks() + "): \t" + convert(bBlank)).append("\n");

        sb.append(" - uri  (" + g.nbIndividuals() + "): \t").append(convert(bIndividual)).append(", ");
        sb.append(" \t each :\t").append(convert(bIndividual / g.nbIndividuals())).append("\n");

        sb.append(" - lit. (" + g.nbLiterals() + "): \t").append(convert(bLiteral)).append(", ");
        sb.append(" \t each :\t").append(convert(bLiteral / g.nbLiterals())).append("\n");
        sb.append(statsLiteral(lLiteral));

        sb.append("     IDataType: \t").append(convert(bMapLieteral)).append(", ");
        sb.append(" \t each :\t").append(convert(bMapLieteral / g.nbIndividuals())).append("\n");

        //EDGE
        long bAllEdges = MemoryUtil.deepMemoryUsageOfAll(getEdges(g));
        sb.append("G all edges (include nodes) (" + g.size() + "): \t" + convert(bAllEdges)).append(",");
        sb.append("\t each: " + convert(bAllEdges / g.size())).append("\n");

        sb.append("            (exclude nodes) (" + g.size() + "): \t" + convert(bAllEdges - bAllNode)).append(",");
        sb.append("\t each: " + convert((bAllEdges - bAllNode) / g.size())).append(", ");
        sb.append("[ref:  " + convert(MemoryUtil.memoryUsageOf(get1Edge(g)))).append("]\n");

        long bIndex = MemoryUtil.deepMemoryUsageOf(g.getIndex());
        sb.append("G index (include node & edge) (" + g.getIndex().size() + "): \t" + convert(bIndex)).append("\n");
        sb.append("        (exclude node & edge) (" + g.getIndex().size() + "): \t" + convert(bIndex - bAllEdges)).append("\n");

        System.out.print(sb);

        //
    }

    static List getEdges(Graph g) {
        List edges = new ArrayList();
        for (Entity next : g.getEdges()) {
            edges.add(next);
        }

        return edges;
    }

    static Object get1Edge(Graph g) {
        return g.getEdges().iterator().hasNext() ? g.getEdges().iterator().next() : null;
    }

    static List getNodes(Graph g, int type) {
        List nodes = new ArrayList();
        Iterable<Entity> it = null;
        switch (type) {
            case ALL://All
                it = g.getAllNodes();
                break;
            case LITERAL://LIteral
                it = g.getLiteralNodes();
                break;
            case BN://BN
                it = g.getBlankNodes();
                break;
            case URI://URI
                it = g.getNodes();
        }
        for (Entity next : it) {
            nodes.add(next);
        }

        return nodes;
    }

    static Object get1Node(Graph g) {
        return g.getAllNodes().iterator().hasNext() ? g.getAllNodes().iterator().next() : null;
    }
    private static final int KB = 1024;
    private static final int MB = KB * KB;
    private static final int GB = MB * KB;

    public static String convert(long bytes) {
        String s;
        if (bytes < 1024) {
            s = bytes + "B";
        } else if (bytes < (MB)) {
            s = bytes / KB + "K, " + convert(bytes % KB);
        } else if (bytes < (GB)) {
            s = bytes / MB + "M, " + convert(bytes % MB);
        } else {
            s = bytes / GB + "G, " + convert(bytes % GB);
        }
        return s;
    }

    public static String convert(long bytes, boolean precise) {
        String s;
        if (bytes < KB) {
            s = bytes + "B";
        } else if (bytes < (MB)) {
            s = bytes / KB + "K ";
            s += precise ? convert(bytes % KB, true) : "~";
        } else if (bytes < GB) {
            s = bytes / MB + "M ";
            s += precise ? convert(bytes % MB, true) : "~";
        } else {
            s = bytes / GB + "G ";
            s += true ? convert(bytes % GB, true) : "~";
        }
        return s;
    }

    static String statsLiteral(List<Entity> list) {
        List<String> ls = new ArrayList<String>();
        int counter = 0;
        long size = 0;
        for (Entity e : list) {
            String s = ((NodeImpl) e).getValue().getLabel();
            long b = MemoryUtil.deepMemoryUsageOf(s);
            if (b >= KB) {
                counter++;
                size += b;
            }
            ls.add(s);
        }

        return ("     (pure strings):\t" + convert(MemoryUtil.deepMemoryUsageOfAll(ls)) + ",\t" + counter + " >1K, " + convert(size) + " \n");
    }
}
