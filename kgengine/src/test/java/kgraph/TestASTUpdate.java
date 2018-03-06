package kgraph;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.acacia.corese.triple.update.ASTUpdate;
import fr.inria.acacia.corese.triple.update.Composite;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.print.RDFFormat;


public class TestASTUpdate {

	public static void main(String[] args){
		new TestASTUpdate().process();
	}

	void process(){

		ASTQuery ast = ASTQuery.create();
		ast.setBody(BasicGraphPattern.create());
  		ast.setResultForm(ASTQuery.QT_UPDATE);

		NSManager nsm = NSManager.create();
		nsm.definePrefix("ns", "http://ns.inria.fr/schema/");
		ast.setNSM(nsm);
		
		ASTUpdate astu = ASTUpdate.create();
		ast.set(astu);
		
		
		
		Triple t1 = Triple.create( ast.createConstant("John"), ast.createConstant("ns:name"), ast.createConstant("John", "xsd:string"));
		BasicGraphPattern bgp = BasicGraphPattern.create();
		bgp.add(t1);
		
		
		Composite ope;
		
		ope = Composite.create(Update.INSERT, bgp);
		astu.add(ope);
		
		
		
		Triple t2 = Triple.create( ast.createConstant("John"), ast.createConstant("ns:name"), Variable.create("?n"));
		bgp = BasicGraphPattern.create();
		bgp.add(t2);
		
		Composite upd = Composite.create(Update.COMPOSITE); 
		upd.setBody(bgp); 

		ope = Composite.create(Update.DELETE);    
		ope.setPattern(bgp); 

		upd.add(ope);
		astu.add(upd);


		
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		exec.setDebug(true);
		ast.setDebug(true);
		exec.query(ast);
		
		RDFFormat f = RDFFormat.create(g, ast.getNSM());
		System.out.println(f);
		System.out.println(g);

	}

}