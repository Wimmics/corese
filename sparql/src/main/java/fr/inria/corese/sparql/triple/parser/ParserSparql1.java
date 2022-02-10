package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.exceptions.EngineException;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.sparql.exceptions.QueryLexicalException;
import fr.inria.corese.sparql.exceptions.QuerySyntaxException;
import fr.inria.corese.sparql.triple.javacc1.JavaccParseException;
import fr.inria.corese.sparql.triple.javacc1.SparqlCorese;
import fr.inria.corese.sparql.triple.javacc1.TokenMgrError;

/**
 * <p>
 * Copyright: Copyright INRIA (c) 2011</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Edelweiss</p>
 *
 * @author Olivier Corby
 */
public class ParserSparql1 {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(ParserSparql1.class);

    SparqlCorese parser;
    ASTQuery ast;
    // true: load turtle file as a sparql where graph pattern
    private boolean load = false;

    public static ParserSparql1 create(ASTQuery aq) {
        return new ParserSparql1(aq);
    }

    public ASTQuery getAST() {
        return ast;
    }

    ParserSparql1(ASTQuery aq) {
        if (aq.getText() != null) {
            createParserJavaCC1(aq.getText(), aq);
            ast = aq;
        }
    }

    private void createParserJavaCC1(String query, ASTQuery aq) {
        // Create an instance of SparqlCorese to read and parse the query
        StringReader queryReader = new StringReader(query);
        parser = new SparqlCorese(queryReader);
        parser.setHandler(new ParserHandler());
        // set the parser and the astquery
        parser.setASTQuery(aq);
    }

    public ASTQuery parse() throws QueryLexicalException, QuerySyntaxException {
        try {
            ASTQuery ast;
            
            if (isLoad()) {
                // parse turtle as sparql where graph pattern
                parser.getHandler().enterWhere();
                ast = parser.load();
                ast.setSelectAll(true);
            }
            else {
                ast = parser.parse();
            }
            
            for (EngineException e : parser.getHandler().getErrorList()) {
                throw new QuerySyntaxException(e.getMessage());
            }
            return ast;
        } catch (JavaccParseException e) {
            logger.debug(ast.getText());
            throw new QuerySyntaxException(e);
        } catch (TokenMgrError e) {
            logger.debug(ast.getText());
            throw new QueryLexicalException(e.getMessage());
        }
    }

    public boolean isLoad() {
        return load;
    }

    public ParserSparql1 setLoad(boolean load) {
        this.load = load;
        return this;
    }
    
    
}
