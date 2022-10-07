package fr.inria.corese.kgram.api.core;

import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import java.util.Collection;
import java.util.List;

/**
 * Expr api refer to sparql.triple.parser.Expression
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Expr {

    Filter getFilter();

    // Exp as Object for modularity
    Object getPattern();

    boolean isSystem();

    boolean isPublic();

    boolean isDynamic();

    void setPublic(boolean b);

    boolean isTrace();

    boolean isDebug();

    boolean isTester();

    String getLabel();

    String getShortName();

    String getModality();

    void setModality(String mod);

    List<Expr> getExpList();

    Expr getExp(int i);

    void setExp(int i, Expr e);

    void addExp(int i, Expr e);

    Expr getArg();

    void setArg(Expr exp);

    IDatatype getValue();

    IDatatype getDatatypeValue();

    int type();

    int subtype();

    void setSubtype(int n);

    int oper();

    boolean match(int oper);

    void setOper(int n);

    boolean isAggregate();

    boolean isRecAggregate();

    boolean isExist();

    boolean isRecExist();

    boolean isVariable();

    boolean isConstant();

    boolean isFuncall();

    boolean isBound();

    boolean isDistinct();

    int arity();

    int getIndex();

    void setIndex(int index);

    Expr getDefine();

    void setDefine(Expr exp);

    Expr getFunction();

    Expr getBody();

    Expr getVariable();

    Expr getDefinition();

    int getNbVariable();

    IDatatype[] getArguments(int n);

    List<String> getMetadataValues(String name);

    Collection<String> getMetadataList();

    boolean hasMetadata(String name);

    IDatatype evalWE(Computer eval, Binding b, Environment env, Producer p) throws EngineException;
    
    default boolean test(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = evalWE(eval, b, env, p);
        if (dt == null) {
            return false;
        }
        return dt.isTrueTest();
    }
   
}
