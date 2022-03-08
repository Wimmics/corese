package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.exceptions.EngineException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.exceptions.QueryLexicalException;
import fr.inria.corese.sparql.exceptions.QuerySyntaxException;
import fr.inria.corese.sparql.triple.api.Creator;
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
public class LoadTurtle {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(LoadTurtle.class);

    SparqlCorese parser;
    Creator create;
    ASTQuery ast;
    private boolean nquad = false;

    LoadTurtle(InputStream r, Creator c, String base) {
        setLoader(r, c, base);
    }

    LoadTurtle(Reader r, Creator c, String base) {
        setLoader(r, c, base);
    }

    public static LoadTurtle create(InputStream read, Creator cr, String base) {
        LoadTurtle p = new LoadTurtle(read, cr, base);
        return p;
    }

    public static LoadTurtle create(Reader read, Creator cr, String base) {
        LoadTurtle p = new LoadTurtle(read, cr, base);
        return p;
    }

    public static LoadTurtle create(String file, Creator cr) {
        FileReader read;
        try {
            read = new FileReader(file);
            LoadTurtle p = new LoadTurtle(read, cr, file);
            return p;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    void setCreator(Creator c) {
        create = c;
    }

    public Creator getCreator() {
        return create;
    }

    private void setLoader(InputStream stream, Creator c, String base) {
        parser = new SparqlCorese(stream);
        parser.setHandler(new ParserHandler());        
        setLoader(parser, c, base);
    }

    private void setLoader(Reader stream, Creator c, String base) {
        parser = new SparqlCorese(stream);
        parser.setHandler(new ParserHandler());
        setLoader(parser, c, base);
    }
    
    private void setLoader(SparqlCorese parser, Creator c, String base) {
        try {
            ASTQuery ast = ASTQuery.create();
            ast.getNSM().setBase(base);
            ast.setRenameBlankNode(c.isRenameBlankNode());
            parser.setASTQuery(ast);
            parser.set(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() throws QueryLexicalException, QuerySyntaxException {
        try {
            //logger.info("start parser");
            if (isNquad()) {
                parser.nquad();
            }
            else {
                parser.load();
            }
            //logger.info("finish parser");
            for (EngineException e : parser.getHandler().getErrorList()) {
                throw new QuerySyntaxException(e.getMessage());
            }
        } catch (JavaccParseException e) {
            if (e.isStop()) {
                // parser stop after limit
                logger.info("finish parser after limit");
            }
            else {
                throw new QuerySyntaxException(e.getMessage());
            }
        } catch (TokenMgrError e) {
            throw new QueryLexicalException(e.getMessage());
        }
    }

    public boolean isNquad() {
        return nquad;
    }

    public LoadTurtle setNquad(boolean nquad) {
        this.nquad = nquad;
        return this;
    }

}
