package test;

import fr.inria.acacia.corese.exceptions.QueryLexicalException;
import fr.inria.acacia.corese.exceptions.QuerySyntaxException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;

public class Test {
	
	public static void main(String[] args){
		new Test().process();
	}
	
	
	void process(){
		String query;
		query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
		"select * " +
		"(xsd:integer(?val) as ?int) " +
		"where {" +
		"{select (xpath('file', '/book') as ?book) where {}}" +
		"{select (xpath(?book, 'num[@rdf:datatype]/text()') as ?val) where {}}" +
		"?x ?p ?v " +
		"}";
		
		query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"prefix data: <file:/home/corby/workspace/corese/data/comma/> " +
			"prefix data2: <file:/home/corby/workspace/corese/data/comma/data2/> " +
		"select   distinct ?graph ?y " +
		"from named data:model.rdf " +
		"from named data2:f1.rdf " +
		"from named data2:f2.rdf " +
		"where {" +
		"graph ?graph {" +
			"?x rdfs:seeAlso ?src " +
			"optional{?x c:FirstName ?nn1} " +
			"optional {graph ?src2 {?x c:FirstName ?nn2}} " +
			"graph ?src { optional { ?y c:FamilyName ?name ?y c:FirstName ?fn}}" +
			"}" +
		"} " +
		"limit 15" ;
		
		
		try {
			ASTQuery ast = ASTQuery.create(query);
			ast.setKgram(true);
			ParserSparql1.create(ast).parse();
			System.out.println(ast);
		} catch (QueryLexicalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QuerySyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
