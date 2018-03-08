package fr.inria.corese.sparql;

import fr.inria.corese.sparql.exceptions.QueryLexicalException;
import fr.inria.corese.sparql.exceptions.QuerySyntaxException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.ParserSparql1;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Triple;

public class Test {

	public static void main(String[] args){
		new Test().process();
	}


	void process(){
		String query;


		query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"prefix data: <file:/home/corby/workspace/corese/data/comma/> " +
			"prefix data2: <file:/home/corby/workspace/corese/data/comma/data2/> " +
			"select   distinct ?graph ?y " +
			"from  data:model.rdf " +
			"from named data2:f1.rdf " +
			"from named data2:f2.rdf " +
			"where {" +
			"graph ?graph {" +
			"?x rdfs:seeAlso ?src filter(?x != 12)" +
			"optional{?x c:FirstName ?nn1} " +
			"optional {graph ?src2 {?x c:FirstName ?nn2}} " +
			"graph ?src { optional { ?y c:FamilyName ?name ?y c:FirstName ?fn}}" +
			"}" +
			"[a e:Term ; e:term @(?x ?y); e:term ()] "
                        + "bind(sql('select from where') as (?x, ?y))" +
			"} " +
			"limit 15" ;

		try {
			ASTQuery ast = ASTQuery.create(query);
			ParserSparql1.create(ast).parse();

			Exp exp = ast.getBody();

			for (Exp ee : exp.getBody()){
				if (ee.isBGP()){
					// Basic Graph Pattern 
					ee.getBody();
				}
				else if (ee.isGraph()){
					// Graph Pattern
					Source s = (Source) ee;
					// graph name
					s.getSource();
					s.getBody();
				}
				else if (ee.isQuery()){
					// subquery
					ASTQuery q = ee.getQuery();
				}
				else if (ee.isOption()){
					// optional is unary (in Corese)
				}
				else if (ee.isMinus()){
					// minus is a binary exp
				}
				else if (ee.isUnion()){
					// union is a binary exp

				}
				else if (ee.isTriple()){
					Triple t = ee.getTriple();
					// subject
					t.getArg(0);
					// object
					t.getArg(1);
					// property name
					t.getPredicate();
					// property variable
					t.getVariable();
				}
			}

			System.out.println(ast);

			//System.out.println(Parser.create().ncompile(ast));
		} catch (QueryLexicalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QuerySyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
