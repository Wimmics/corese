package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Expression;
import static fr.inria.corese.sparql.triple.function.aggregate.Aggregate.NL;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateGroupConcat extends Aggregate {

    IDatatype dtres;
    int count = 0;
    boolean ok = true, hasLang = false, isString = true;
    StringBuilder sb;
    String lang, sep = "";

    public AggregateGroupConcat(){}
    
    public AggregateGroupConcat(String name) {
        super(name);
        start();
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        init(eval, b, env, p);
        return super.eval(eval, b, env, p);
    }

    @Override
    public void aggregate(IDatatype dt) {
        if (accept(dt)) {
            if (dt != null) {

                if (count == 0 && dt.hasLang()) { // 0 vs 1
                    hasLang = true;
                    lang = dt.getLang();
                }

                if (ok) {
                    if (hasLang) {
                        if (!(dt.hasLang() && dt.getLang().equals(lang))) {
                            ok = false;
                        }
                    } else if (dt.hasLang()) {
                        ok = false;
                    }

                    if (!DatatypeMap.isString(dt)) {
                        isString = false;
                    }
                }

                if (count++ > 0) {
                    sb.append(sep);
                }

                StringBuilder s = dt.getStringBuilder();
                if (s != null) {
                    sb.append(s);
                } else {
                    sb.append(dt.getLabel());
                }
            }
        }
    }
    
    void init(Computer eval, Binding b, Environment env, Producer prod) throws EngineException {
        sep = format(eval, b, env, prod);
    }

    String format(Computer eval, Binding b, Environment env, Producer prod) throws EngineException {
        String sep = " ";
        if (getArg() != null) {
            // separator as an evaluable expression: st:nl()
            IDatatype dt = getArg().eval(eval, b, env, prod);
            sep = dt.getLabel();
        } else {
            if (getModality() != null) {
                sep = getModality();
            }

            if (env.getQuery().isTemplate()) {
                if (sep.equals("\n") || sep.equals("\n\n")) {
                    // get the indentation by evaluating a predefined st:nl()
                    // computed by PluginImpl/Transformer
                    // same as: separator = 'st:nl()'
                    Expression nl = (Expression) env.getQuery().getTemplateNL().getFilter().getExp();
                    IDatatype dt = nl.eval(eval, b, env, prod);
                    String str = dt.getLabel();
                    if (sep.equals("\n\n")) {
                        str = NL + str;
                    }
                    sep = str;
                }
            }
        }
        return sep;
    }

    // result(sb, isString, (ok && lang != null)?lang:null);
    @Override
    public IDatatype result() {
        lang = (ok && lang != null) ? lang : null;
        if (lang != null) {
            return DatatypeMap.createLiteral(sb.toString(), null, lang);
        } else if (isString) {
            return DatatypeMap.newStringBuilder(sb);
        } else {
            return DatatypeMap.newLiteral(sb.toString());
        }
    }

    @Override
    public void start() {
        sb = new StringBuilder();
        count = 0;
        ok = true;
        hasLang = false;
        isString = true;
    }
}
