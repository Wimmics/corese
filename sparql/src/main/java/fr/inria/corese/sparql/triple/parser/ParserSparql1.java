package fr.inria.corese.sparql.triple.parser;


import java.io.StringReader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import fr.inria.corese.sparql.exceptions.QueryLexicalException;
import fr.inria.corese.sparql.exceptions.QuerySyntaxException;
import fr.inria.corese.sparql.triple.javacc1.JavaccParseException;
import fr.inria.corese.sparql.triple.javacc1.SparqlCorese;
import fr.inria.corese.sparql.triple.javacc1.TokenMgrError;

/**
 * <p>Copyright: Copyright INRIA (c) 2011</p>
 * <p>Company: INRIA</p>
 * <p>Project: Edelweiss</p>
 * 
 * @author Olivier Corby
 */

public class ParserSparql1 {

	/** logger from log4j */
	private static Logger logger = LogManager.getLogger(ParserSparql1.class);
	
    SparqlCorese parser;
    
    ASTQuery ast;
 
    
    public static ParserSparql1 create(ASTQuery aq) {   
    	return new ParserSparql1(aq);
    }
    
    
    public ASTQuery getAST(){
    	return ast;
    }

    ParserSparql1(ASTQuery aq) {      
    	if (aq.getText() != null){
    		createParserJavaCC1(aq.getText(), aq);
    		ast = aq;
    	}
    }
    

    private void createParserJavaCC1(String query, ASTQuery aq) {
        // Create an instance of SparqlCorese to read and parse the query
        StringReader queryReader = new StringReader(query);
        parser = new SparqlCorese(queryReader);
        // set the parser and the astquery
        parser.setASTQuery(aq);
    }
    
    public ASTQuery parse() throws QueryLexicalException, QuerySyntaxException {
    	try {
    		ASTQuery ast = parser.parse();
    		return ast;
    	}  
    	 catch (JavaccParseException e) {
    		 logger.debug(ast.getText());
     		throw new QuerySyntaxException(e);
		} catch (TokenMgrError e) {
			logger.debug(ast.getText());
    		throw new QueryLexicalException(e.getMessage());
		}
    }
    

}
