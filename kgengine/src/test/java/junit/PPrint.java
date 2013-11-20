package junit;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.PPrinter;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TemplateFormat;
import fr.inria.edelweiss.kgtool.print.TemplatePrinter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PPrint {
	
//	static String root  = "/home/corby/workspace/kgengine/src/test/resources/data/";
//	static String data  = "/home/corby/workspace/coreseV2/src/test/resources/data/";
//	static String cos   = "/home/corby/workspace/corese/data/";
        
    
    //relative path for data
     //static String root  = PPrint.class.getClassLoader().getResource("data").getPath()+"/";
//	static String data  = "/home/corby/workspace/coreseV2/src/test/resources/data/";
       // static String data  = PPrint.class.getClassLoader().getResource("data").getPath()+"/";
    
        static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";
        static String root  = data;
        static String srclib = "/user/corby/home/NetBeansProjects/kgram/trunk/kgtool/src/main/resources/template/";
        static String tgtlib = "/user/corby/home/NetBeansProjects/kgram/trunk/kgtool/target/classes/fr/inria/edelweiss/resource/template/";

	static Graph graph;
		

	public void translate(){
		TemplatePrinter p =  
		TemplatePrinter.create(root + "spin/template", root + "pprint/lib/spin.rul");
		//TemplatePrinter.create(root + "spin/template", lib + "spin.rul");
		try {
			p.process();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LoadException e) {
			e.printStackTrace();
		}
	}
        
	
		@Test

        public void pplib(){
                String lib = srclib;
                
                translate(root + "template/spin/template",  lib + "spin.rul");
		translate(root + "template/sql/template",   lib + "sql.rul");
		translate(root + "template/owl/template",   lib + "owl.rul");
		translate(root + "template/turtle/template",lib + "turtle.rul");

                translate(root + "typecheck/template",     lib + "typecheck.rul");       
	}
       
        
        void translate(String src, String tgt){
            	TemplatePrinter p = TemplatePrinter.create(src,  tgt);
                try {
			p.process();                       
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LoadException e) {
			e.printStackTrace();
		}
        }
        
        
        
        
        public void testSPIN() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "spin/data");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }

       
        QueryProcess exec = QueryProcess.create(g);

            PPrinter tf = PPrinter.create(g);
//          tf.setCheck(true);
//          tf.setDetail(true);
            tf.setTemplates(root + "spin/spin.rul");
            tf.setTemplates("ftp://ftp-sop.inria.fr/wimmics/soft/pprint/spin/spin.rul");

            NSManager nsm = NSManager.create();
            nsm.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
            tf.setNSM(nsm);

            //tf.setDebug(true);
            System.out.println("res: ");
            //String str = tf.toString();

            IDatatype dd = tf.pprint(ExpType.KGRAM + "start");
            System.out.println(dd.getLabel());
           
//            try {
//                str = nsm.toString() + "\n" + str;
//                Query qq = exec.compile(str);
//                System.out.println(qq.getAST());
//            } catch (EngineException ex) {
//                Logger.getLogger(PPrint.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
            
            if (! true) {
                String q =
                        "prefix sp: <http://spinrdf.org/sp#> "
                        + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                        + "select ?t (kg:templateWith(<" + root + "spin/template>" + ", kg:start, ?q) as ?pp) "
                        + "where { ?q a ?t "
                        + "filter(?t in (sp:Select, sp:Construct, sp:Ask, sp:Describe)) "
                        + "filter(not exists {?a ?p ?q})}";
                try {
                    Mappings map = exec.query(q);
                    System.out.println(map);
                    exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

                    for (Mapping m : map) {
                        IDatatype dt = (IDatatype) m.getValue("?pp");
                        Query qq = exec.compile(dt.getLabel());
                        System.out.println(qq.getAST());
                    }
                } catch (EngineException ex) {
                    Logger.getLogger(PPrint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

    }
    
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
	
	public void testTemp(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		ld.load("/home/corby/Cours/2013/Corese/");
		
		PPrinter p = PPrinter.create(g, root + "pprint/test2");
		NSManager nsm = NSManager.create();
		nsm.definePrefix("h", "http://www.inria.fr/2007/09/11/humans.rdfs#");
		nsm.definePrefix("i", "http://www.inria.fr/2007/09/11/humans.rdfs-instances#");
		p.setNSM(nsm);
		p.setTurtle(true);
		//p.setDebug(true);
				
		Node n = g.getResource(nsm.toNamespace("h:Man"));
		IDatatype dt = (IDatatype) n.getValue();
		
		
		//System.out.println(p.pprint().getLabel());
		
		
		String temp = 
				"prefix h: <http://www.inria.fr/2007/09/11/humans.rdfs#>"+
				"prefix i: <http://www.inria.fr/2007/09/11/humans.rdfs-instances#>"+
				"template { kg:pprint(?x, </home/corby/AData/pprint/asttemplate>) " +
				"; separator = '\\n\\n'" +
				"}" +
				"where {?x a h:Person}" ;

		QueryProcess exec = QueryProcess.create(g);
		try {
			Mappings map = exec.query(temp);
			//System.out.println(exec.getTemplateResult(map));
			System.out.println(map.getTemplateResult().getLabel());
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public void testPPOWL2(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		//ld.setLimit(1000);
		//ld.load(root + "pprint/owldata/galen.owl");

		try {
			ld.loadWE(root + "pprint/owldata/primer.owl");
		} catch (LoadException e) {
			e.printStackTrace();
		}
		
		QueryProcess exec = QueryProcess.create(g);
		
		String temp = 
				"template {kg:pprint(?x, </home/corby/AData/pprint/owltemplate>)" +
				"; separator = '\\n\\n'" +
				"}" +
				"where {?x a owl:Class" +
				"" +
				"}";
		
		try {
			Mappings map = exec.query(temp);
			System.out.println(map.getTemplateResult().getLabel());
		} catch (EngineException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void testPPOWL3(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		//ld.setLimit(1000);
		//ld.load(root + "pprint/owldata/galen.owl");

		try {
			ld.loadWE(root + "pprint/owldata/primer.owl");
		} catch (LoadException e) {
			e.printStackTrace();
		}
		
		String q = 
				"prefix f: <http://example.com/owl/families/>" +
				"template {" +
				"kg:pprintWith('/home/corby/AData/pprint/owltemplate')" +
				"}" +
				"where {}";
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(q);
			System.out.println(map);
			System.out.println(map.getTemplateResult().getLabel());
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
	}
	

	public void testPPOWL(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		//ld.setLimit(1000);
		//ld.load(root + "pprint/owldata/galen.owl");

		try {
			ld.loadWE(root + "pprint/owldata/primer.owl");
		} catch (LoadException e) {
			e.printStackTrace();
		}


//		TripleFormat f = TripleFormat.create(g);
//		try {
//			f.write(root + "pprint/owldata/galen.ttl");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		

		System.out.println("graph: " + g.size());

		NSManager nsm = NSManager.create();
		
		nsm.definePrefix("fam", "http://example.com/owl/families/");
		nsm.definePrefix("of", "http://example.org/otherOntologies/families/");
//		
		nsm.definePrefix("g", "http://example.org/factkb#");
		
//		nsm.definePrefix("s", "http://www.cs.manchester.ac.uk/substance.owl#");
//		nsm.definePrefix("h", "http://www.geneontology.org/formats/oboInOwl#");
//		nsm.definePrefix("o", "http://purl.obolibrary.org/obo/"); 
		
		String str = "";
		StringBuilder sb = null;
		TemplateFormat tf = null;		
		Date d1 = new Date();
		int max = 1;
		for (int i=0; i<max; i++){
			Date dd1 = new Date();

			tf = TemplateFormat.create(g, root + "pprint/owltemplate");
//			//tf = TemplateFormat.create(g, "ftp://ftp-sop.inria.fr/wimmics/soft/pprint/owl.rul");
//			
//			
			tf.setNSM(nsm);
			tf.setTurtle(true);
			tf.setCheck(true);
//			//tf.setStart(Exp.KGRAM + "test");
//			//tf.setDebug(true);
			//sb = tf.toStringBuilder();
			str = tf.toString();
			
			Date dd2 = new Date();
			System.out.println(i + " : " + (dd2.getTime() - dd1.getTime()) / 1000.0);
			
//			try {
//				tf.write(root + "pprint/galen.fowl");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}

		Date d2 = new Date();
		
		int length = str.length();
		
		//str = nsm.toString() + "\n" + str;
		
		if (max == 1) {
			System.out.println(str);
		}
		double time = ((d2.getTime() - d1.getTime()) / 1000.0) / max;
		System.out.println("Time : " + time);
		System.out.println("Length: " + length);
		System.out.println("graph: " + g.size());

		System.out.println("nb call templates: " + tf.getPPrinter().nbTemplates());
		System.out.println("query per sec: "     + (int) (tf.getPPrinter().nbTemplates() / time));

		System.out.println("max level: "  + tf.getPPrinter().maxLevel());
		System.out.println("stack size: " + tf.getPPrinter().level());
		
		if (tf.getPPrinter().stat){
			tf.getPPrinter().nbcall();
		}

		
//		String q = 
//				"prefix fp: </home/corby/AData/pprint/> " +
//				"prefix f:  <http://example.com/owl/families/>" +
//				"select (kg:pprint(?x, fp:owltemplate) as ?pp) " +
//				"where {" +
//				"?x a ?t " +
//				//"filter not exists { ?y ?p ?x} " +
//				"filter(?t ~ 'Property') " +
//				"filter(isURI(?x))" +
//				"}";
//		
//		QueryProcess exec = QueryProcess.create(g);
//		
//		try {
//			Mappings map = exec.query(q);
//			System.out.println(map);
//		} catch (EngineException e) {
//			e.printStackTrace();
//		}
//		
//		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	public void testRec(){
		Graph g = Graph.create();
//		Load ld = Load.create(g);
//		ld.load(root + "math/eval.ttl");
		
		PPrinter pp = PPrinter.create(g, root + "pprint/test");
		
		
		IDatatype dt = pp.template(ExpType.KGRAM + "rec", DatatypeMap.newInstance(12));
		System.out.println(dt.getLabel());
		
	}
	
	
	

	public void testEval(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		ld.load(root + "math/eval.ttl");
		
		NSManager nsm = NSManager.create();https://www.google.fr/
		nsm.definePrefix("m", "http://ns.inria.fr/2013/math#");
		
		PPrinter pp = PPrinter.create(g, root + "math/eval");
		pp.setNSM(nsm);
		
		System.out.println(pp);

		
	}
	
	
	public void testFormat(){
		String f = "ceci est %1$s test %2$s.";
		String str = String.format(f, "un", 12.5);
		System.out.println(str);
	}



	public void testMath(){
		String latex = "/home/corby/AData/math/latex/";
		Graph g = Graph.create();
		Load ld = Load.create(g);
				
		try {
			ld.loadWE(root + "math/data");
		} catch (LoadException e1) {
			e1.printStackTrace();
		}
		
		QueryLoad ql = QueryLoad.create();
		String qq = ql.read(root + "math/query/query-display.txt");
		String  q = ql.read(root + "math/query/math.txt");
		String test =
				"select * where {" +
				"?x ?p ?y " +
				"filter(regex(?x, '^_:b1$') )" +
				"filter(exists { ?x <p> <a> })" +
				"}";

		TemplateFormat tf = TemplateFormat.create(g, root + "math/latextemplate");
		NSManager nsm = NSManager.create();https://www.google.fr/
		nsm.definePrefix("m", "http://ns.inria.fr/2013/math#");
		tf.setNSM(nsm);

		//System.out.println(tf);
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(test);
			ResultFormat rf = ResultFormat.create(map);
			//rf.write(latex + "tmp.tex");
			System.out.println(rf);
//			Process p = Runtime.getRuntime().exec("/usr/bin/pdftex " + latex  + "tmp.tex");
//			p.waitFor();
//			Runtime.getRuntime().exec("acroread " + latex  + "tmp.pdf");

		} catch (EngineException e) {
			e.printStackTrace();
		} 
		
		
		
	}
	
	
	

	public void testTemplate(){
		
		Graph g = Graph.create(true);
		Load ld = Load.create(g);
		
		ld.load(data + "comma/comma.rdfs");
		ld.load(data + "comma/model.rdf");
		ld.load(data + "comma/data");

		
		String q = 
				"prefix c: <http://www.inria.fr/acacia/comma#> " +
				"select * " +
				"(kg:pprint(?x) as ?out) " +
				"where {?x a c:OrganizationalEntity}";
		
		
		String qq = 
				
				"prefix c: <http://www.inria.fr/acacia/comma#> " +
				"select  (group_concat(distinct xsd:string(?t)) as ?tt) ?in{" +
				"?in a c:Person " +
				"?doc c:CreatedBy ?in " +
				"?doc c:Title ?t" +
				"}" +
				"group by ?in ";

		
		QueryProcess exec = QueryProcess.create(g, true);
		//exec.setPPrinter(root + "pprint/test");
		
		try {
			
			Date d1 = new Date();

			Mappings map  = exec .query(qq);
			
			Date d2 = new Date();

			System.out.println(map);
			
			System.out.println(map.size());
			
			System.out.println((d2.getTime() - d1.getTime()) / 1000.0);

			
		} catch (EngineException e) {
			e.printStackTrace();
		}		
	}
	

	public void testPPrint(){
		Graph g = Graph.create(true);
		Load ld = Load.create(g);
		QueryProcess.definePrefix("ex", "http://www.example.org/");
		
		for (int i = 0; i<1; i++){
			ld.load(root + "pprint/data");
		}

		String q = 
				"prefix ast: <http://www.inria.fr/2012/ast#> " +
				"select  " +
				//"(kg:pprint(?q) as ?out) " +
				"?q " +
				"where {?q a ast:Query}";
		
		
		QueryProcess exec = QueryProcess.create(g);


		
		System.out.println("** start");
		
		//exec.setPPrinter(root + "pprint/template");
		//exec.setPPrinter(root + "pprint/query");
		
		//exec.setPPrinter(root + "pprint/asttemplate");
		
		NSManager nsm = NSManager.create();
		nsm.definePrefix("ex", "http://www.example.org/");
		nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");
		
		PPrinter pp = PPrinter.create(g, root + "pprint/template");
		pp.setNSM(nsm);
		
		String cons = 
				"prefix ast: <http://www.inria.fr/2012/ast#> " +
				"construct {?x ?p ?y} where {?x ?p ?y}" +
				"pragma {kg:display kg:template <" + root + "pprint/html>" + "}";
		
		try {
			Mappings map = exec.query(cons);
						
			Date d1 = new Date();
			
			String str = null;
			for (int i = 0; i<1; i++){
				TemplateFormat tf = TemplateFormat.create(g);
				tf.setPPrinter(root + "pprint/template");
				tf.setNSM(nsm);

				str = tf.toString();
			}
			
			Date d2 = new Date();

			
			System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
			
			str = nsm.toString() + "\n" + str;
			System.out.println(str);
//			map = exec.query(str);
//			
//			System.out.println(map);
			
//			try {
//				tf.write(root + "pprint/tmp.html");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			} 
		catch (EngineException e) {
			e.printStackTrace();
		}
		
		
		
		
				
	}
	
	
	
	
	
	
	
	
	public void testAST(){
		Graph g = Graph.create();
		
		
		Load ld = Load.create(g);
		
		ld.load(root + "pprint/pprint.ttl");
		
		QueryLoad ql = QueryLoad.create();
		String q = ql.read(root  + "pprint/query/pprint.rq");

		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			
			Mappings map1 = exec.query(q + "values ?pp {ast:construct}");
			Mappings map2 = exec.query(q + "values ?pp {ast:where}");
			
			IDatatype cst = (IDatatype) map1.getValue("?res");
			IDatatype whr = (IDatatype) map2.getValue("?res");
						
			System.out.println("construct {" + cst.getLabel() + "}");			
			System.out.println("where {"     + whr.getLabel() + "}");
			
			
			

			
			
		} catch (EngineException e) {
			e.printStackTrace();
		}

		
	}
	
	
	

}
