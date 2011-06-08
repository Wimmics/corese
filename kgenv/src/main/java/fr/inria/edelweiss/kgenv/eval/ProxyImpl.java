package fr.inria.edelweiss.kgenv.eval;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.filter.Proxy;
import fr.inria.lang.StringHelper;

/**
 * Implements evaluator of operators & functions of filter language
 * with IDatatype values
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class ProxyImpl implements Proxy, ExprType {
	static IDatatype TRUE = DatatypeMap.TRUE;
	static IDatatype FALSE = DatatypeMap.FALSE;
	static final String UTF8 = "UTF-8";
	public static final String RDFNS   =  "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFTYPE   = RDFNS + "type";
		
	Proxy plugin;
	Evaluator eval;
	EvalListener el;
	DatatypeMap dm;
	int number = 0;
	
	// KGRAM is relax wrt to string vs literal vs uri input arg of functions
	// eg regex() concat() strdt()
	// setMode(SPARQL_MODE) 
	boolean SPARQLCompliant = false;
	
	
	public ProxyImpl(){
		dm = DatatypeMap.create();
	}
	
	public void setEvaluator(Evaluator ev){
		eval = ev;
	}
	
	public Evaluator getEvaluator(){
		return eval;
	}
	
	public void setPlugin(Proxy p){
		plugin = p;
	}
	
	public Proxy getPlugin(){
		return plugin;
	}
	
	public void setMode(int mode) {
		switch (mode){
		
		case Evaluator.SPARQL_MODE: 
			SPARQLCompliant = true; break;
			
		case Evaluator.KGRAM_MODE: 
			SPARQLCompliant = false; break;	
		}
	}
	
	public void start(){
		number = 0;
	}
	
	@Override
	public Object eval(Expr exp, Environment env, Object o1, Object o2) {
		IDatatype dt1 = (IDatatype) o1;
		IDatatype dt2 = (IDatatype) o2;
		return eval(exp, env, dt1, dt2);
	}
		
	IDatatype eval(Expr exp, Environment env, IDatatype dt1, IDatatype dt2) {
		if (dt2.isArray()){
			// use case: ?x = xpath(?doc, exp)
			// there are several values coming from xpath()
			// test if any value match operator (like in xpath expression itself)
			// catch errors. but if all are errors throw error (like boolean OR)
			for (IDatatype dt : dt2.getValues()){
				IDatatype obj = eval(exp, env, dt1, dt);
				if (obj != null && isTrue(obj)){
					return TRUE;
				}
			}
			return FALSE;
		}
		else if (dt1.isArray()){
			for (IDatatype dt : dt1.getValues()){
				IDatatype obj = eval(exp, env, dt, dt2);
				if (obj != null && isTrue(obj)){
					return TRUE;
				}
			}
			return FALSE;
		}
		
		boolean b = true;
		try {
			switch(exp.oper()){
			case NEQ: 	b = ! dt1.equals(dt2); break;
			case IN:
			case EQ: 	b = dt1.equals(dt2); break;
			case LT: 	b = dt1.less(dt2); break;
			case LE: 	b = dt1.lessOrEqual(dt2); break;
			case GE: 	b = dt1.greaterOrEqual(dt2); break;
			case GT: 	b = dt1.greater(dt2); break;
			case CONT: 	b = dt1.contains(dt2); break;
			case START: b = dt1.startsWith(dt2); break;
			
			case PLUS: 
				if (SPARQLCompliant){
					if (!(dt1.isNumber() && dt2.isNumber())){
						return null;
					}
				}
				return dt1.plus(dt2);
			case MINUS: return dt1.minus(dt2);
			case MULT: 	return dt1.mult(dt2);
			case DIV: 
				try {
					return dt1.div(dt2);
				}
				catch (java.lang.ArithmeticException e){
					return null;
				}
			
			default: 
				if (plugin!=null) 
					return (IDatatype) plugin.eval(exp, env, dt1, dt2);
				return null;
			
			}
		}
		catch (CoreseDatatypeException e){
			//System.out.println(e.getMessage());
			return null;
		}
		return getValue(b);
	}
	



	@Override
	public Object eval(Expr exp, Environment env, Object[] args) {
		switch (exp.oper()){
		
		case NUMBER:
			// number of result
			return getValue(env.count());
			
		case SIM: return similarity(env);
		
		case EXTERNAL:
			// user defined function with prefix/namespace
			// function://package.className
			Processor proc = getProcessor(exp);
			return proc.eval(args);
			
		case KGRAM: 
		case EXTERN:
			return plugin.eval(exp, env, args);
			
		case DEBUG:
			if (el == null){
				el = EvalListener.create();
				env.getEventManager().addEventListener(el);
			}
			int i = 0;
			for (Object arg : args){
				Event e = EventImpl.create(Event.FILTER, exp.getExp(i++), arg);
				env.getEventManager().send(e);
			}
			return TRUE;
		}
		
		
		
		boolean b = true;
		IDatatype dt = null, dt1 = null;
		if (args.length>0){
			dt = (IDatatype) args[0];
		}
		if (args.length>1){
			dt1 = (IDatatype) args[1];
		}
		
		switch (exp.oper()){
						
			case ISURI: 	b = dt.isURI(); 	return getValue(b);
			
			case ISLITERAL: b = dt.isLiteral(); return getValue(b);
			
			case ISBLANK: 	b = dt.isBlank(); 	return getValue(b);
			
			case ISNUMERIC: b = dt.isNumber();	return getValue(b);
			
			case URI:  return uri(exp, dt);
			
			case STR: return str(exp, dt);
				
			case CONTAINS: 
				// SPARQL 1.1 distinguishes cases  
				b = dt.getLabel().contains(dt1.getLabel());
				return getValue(b);
			
			case STARTS: 
				b = dt.startsWith(dt1); 
				return getValue(b);
			
			case ENDS:
				b = dt.getLabel().endsWith(dt1.getLabel());
				return getValue(b);
				
			case CONCAT: 
				return concat(args);
				
			case STRLEN: 
				int l = dt.getLabel().length();
				return getValue(l);
				
			case SUBSTR: 
				IDatatype dt2 = null;
				if (args.length>2){
					dt2 = datatype(args[2]);
				}
				return substr(dt, dt1, dt2);
				
			case UCASE: return ucase(dt);
			
			case LCASE: return lcase(dt);
						
			case ENCODE:  
				return encode(dt);
				
			case RANDOM:
				return getValue(Math.random());
				
			case ABS:
				return abs(dt);
				
			case FLOOR:
				return getValue(Math.floor(dt.getDoubleValue()), dt.getDatatypeURI());
				
			case ROUND:
				return getValue(Math.round(dt.getDoubleValue()), dt.getDatatypeURI());

			case CEILING:
				return getValue(Math.ceil(dt.getDoubleValue()), dt.getDatatypeURI());
			
			case NOW:	return dm.newDate();
			
			case TIMEZONE: return dm.getTimezone(dt);
				
			case TZ: return dm.getTZ(dt);
			
			case YEAR:
			case MONTH:
			case DAY:
			case HOURS:
			case MINUTES:
			case SECONDS: return time(exp, dt);
			
			case HASH:
				return hash(exp, dt);
	
			case LANG: 		return dt.getDataLang(); 
			
			case LANGMATCH: 
				if (args.length!=2) return null;
				return langMatches(dt, dt1); 
				
			case STRDT:
				if (SPARQLCompliant && ! DatatypeMap.isSimpleLiteral(dt)){
					return null;
				}
				return DatatypeMap.createLiteral(dt.getLabel(), dt1.getLabel());

			case STRLANG:
				if (SPARQLCompliant && ! DatatypeMap.isSimpleLiteral(dt)){
					return null;
				}
				return DatatypeMap.createLiteral(dt.getLabel(), null, dt1.getLabel());
				
			case BNODE:
				if (dt!=null)
					return DatatypeMap.createBlank(dt.getLabel());
				else return DatatypeMap.createBlank();

			case CAST: // cast(?x, xsd:string, CoreseString)
				return dt.cast(dt1, datatype(args[2]));
			
			case DATATYPE: return dt.getDatatype();
			
			case REGEX: {
				if (SPARQLCompliant && ! DatatypeMap.isStringLiteral(dt)){
					return null;
				}
				Processor proc = getProcessor(exp);
				b = proc.regex(dt.getLabel(), dt1.getLabel());
				return getValue(b);
			}
			
			
			/***********************************************
			 * Extension Functions
			 */

				
			case XPATH: {
				// xpath(?g, '/book/title')
				Processor proc = getProcessor(exp);
				proc.setResolver(new VariableResolverImpl(env));
				IDatatype res = proc.xpath(dt, dt1);				
				return res;
			}
			
			case SQL: {
				Processor proc = getProcessor(exp);
				// return ResultSet
				return proc.sql(dt, dt1, datatype(args[2]), datatype(args[3]));
			}	
						
			case DISPLAY:
				System.out.println(exp + " = " + dt);
				return TRUE;
				
			case EXTEQUAL: {
				boolean bb = StringHelper.equalsIgnoreCaseAndAccent(dt.getLabel(), dt1.getLabel());
				return getValue(bb);
			}
				
			case EXTCONT: {
				boolean bb = StringHelper.containsWordIgnoreCaseAndAccent(dt.getLabel(), dt1.getLabel());
				return getValue(bb);
			}
			
		}
			
		return null;
	}
	
	IDatatype encode(IDatatype dt){
		try {
			String str = URLEncoder.encode(dt.getLabel(), UTF8);
			return dm.createLiteral(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// first index is 1
	IDatatype substr(IDatatype dt, IDatatype ind, IDatatype len){
		String str = dt.getLabel();
		int start = ind.getIntegerValue();
		start = Math.max(start-1, 0);
		int end = str.length();
		if (len!=null){
			end = len.getIntegerValue();
		}
		end = start + end;
		if (end > str.length()){
			end = str.length();
		}
		str = str.substring(start, end);
		return getValue(dt, str);
	}
	
	// return a Literal (not a xsd:string)
	IDatatype str(Expr exp, IDatatype dt){
//		if (SPARQLCompliant){
//			if (! (dt.isURI() || DatatypeMap.isLiteral(dt))){
//				return null;
//			}
//		}
		return DatatypeMap.createLiteral(dt.getLabel());
	}
	
	IDatatype ucase(IDatatype dt){
		String str = dt.getLabel().toUpperCase();
		return getValue(dt, str);
	}
	
	IDatatype lcase(IDatatype dt){
		String str = dt.getLabel().toLowerCase();
		return getValue(dt, str);
	}		
	
	IDatatype uri(Expr exp, IDatatype dt){
		if (exp.getModality()!=null){
			// with base
			return DatatypeMap.newResource(exp.getModality() + dt.getLabel());
		}
		else if (dt.isURI()) return dt;
		else return DatatypeMap.newResource(dt.getLabel());
	}

	
	/**
	 * literals with same lang return literal@lang
	 * all strings return string
	 * else return literal
	 * error if not literal or string
	 */
	IDatatype concat(Object[] args){
		String str = "",  lang = null;
		if (args.length==0) return getValue(str);
		
		IDatatype dt = datatype(args[0]);
		boolean ok = true, hasLang = false, isString = true;
		if (dt.hasLang()){
			hasLang = true;
			lang = dt.getLang();
		}
		
		for (Object obj : args){
			
			dt = datatype(obj);
			
			if (SPARQLCompliant && ! DatatypeMap.isStringLiteral(dt)){
				return null;
			}
			
			str += dt.getLabel();
			
			if (ok){
				if (hasLang){
					if (! (dt.hasLang() && dt.getLang().equals(lang))){
						ok = false;
					}
				}
				else if (dt.hasLang()){
					ok = false;
				}
				
				if (! DatatypeMap.isString(dt)){
					isString = false;
				}
			}
		}	
		
		if (ok && lang != null){
			return DatatypeMap.createLiteral(str, null, lang);
		}
		else if (isString){
			return getValue(str);
		}
		else {
			return DatatypeMap.createLiteral(str);
		}
	}
	
	
	IDatatype time(Expr exp, IDatatype dt){
		if (dt.getDatatypeURI().equals(RDF.xsddate) ||
			dt.getDatatypeURI().equals(RDF.xsddateTime)){
			
			switch(exp.oper()){
			
			case YEAR: 	return DatatypeMap.getYear(dt);
			case MONTH:	return DatatypeMap.getMonth(dt);
			case DAY:	return DatatypeMap.getDay(dt);
			
			case HOURS: 	return DatatypeMap.getHour(dt);
			case MINUTES:	return DatatypeMap.getMinute(dt);
			case SECONDS: 	return DatatypeMap.getSecond(dt);
			}
		}
		
		return null;
	}
	
	
	IDatatype hash(Expr exp, IDatatype dt){
		String name = exp.getModality();
		String str = dt.getLabel();
		String res = new Hash(name).hash(str);
		if (res == null) return null;
		return dm.createLiteral(res);
	}
	
	
	IDatatype abs(IDatatype dt){
		if (DatatypeMap.isInteger(dt)){
			return getValue(Math.abs(dt.getIntegerValue()));
		}
		else if (DatatypeMap.isLong(dt)){
			return getValue(Math.abs(dt.getlValue()));
		}
		else {
			return getValue(Math.abs(dt.getDoubleValue()));
		}
	}

	/**
	 * sum(?x)
	 */
	public Object aggregate(Expr exp, Environment env, Node qNode){
		Walker walk = new Walker(exp, qNode, this, env);

		// apply the aggregate on current group Mapping, 
		env.aggregate(walk, exp.getFilter());

		return walk.getResult();
	}
	
	
	
	Processor getProcessor(Expr exp){
		return ((Term)exp).getProcessor();
	}


	// IDatatype KGRAM value to target proxy value 
	public Object getConstantValue(Object value) {
		return value;
	}


	@Override
	public boolean isTrue(Object value) {
		IDatatype dt = (IDatatype) value;
		if (! dt.isTrueAble()) return false;
		try {
			return dt.isTrue();
		} catch (CoreseDatatypeException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
	
	public boolean isTrueAble(Object value) {
		IDatatype dt = (IDatatype) value;
		return dt.isTrueAble();
	}

	
	IDatatype datatype(Object o){
		return (IDatatype) o;
	}

	@Override
	public IDatatype getValue(boolean b) {
		// TODO Auto-generated method stub
		if (b) return TRUE;
		else return FALSE;
	}
	
	@Override
	public IDatatype getValue(int value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(long value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(float value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(double value) {
		return dm.newInstance(value);
	}
	
	public IDatatype getValue(double value, String datatype) {
		return dm.newInstance(value, datatype);
	}
	
	// return xsd:string
	public IDatatype getValue(String value) {
		return dm.newInstance(value);
	}
	
	// return rdfs:Literal or xsd:string wrt dt
	public IDatatype getValue(IDatatype dt, String value) {
		if (dt.hasLang()){
			return DatatypeMap.createLiteral(value, null, dt.getLang());
		}
		else if (dt.isLiteral() && dt.getDatatype() == null){
			return dm.createLiteral(value);
		}
		return dm.newInstance(value);
	}
	
	Object langMatches(IDatatype ln1, IDatatype ln2) {
		if (ln2.getLabel().equals("*")) {
			return getValue(ln1.getLabel().length() > 0);
		}
		if (ln2.getLabel().indexOf("-")!=-1){
			// en-us need exact match
			return getValue(ln1.getLowerCaseLabel().equals(ln2.getLowerCaseLabel()));
		}
		return getValue(ln1.getLabel().regionMatches(true, 0, ln2.getLabel(), 0, 2)); 
	}
	
	public Object self(Object obj){
		return obj;
	}

	Object similarity(Environment env){
		if (! (env instanceof Memory)) return getValue(0);
		Memory memory = (Memory) env;
		int count = 0, total = 0;
		
		for (Edge qEdge : memory.getQueryEdges()){
			
			if (qEdge != null && qEdge.getLabel().equals(RDFTYPE)){
				Edge edge = memory.getEdge(qEdge);
				if (edge != null){
					Node type = qEdge.getNode(1);
					if (type.isConstant()){ 
						total += 1;
						if (type.same(edge.getNode(1))){
							count += 1;
						}
					}
				}
			}
		}
		
		if (total == 0) return getValue(1);
		else return getValue(count/total);
		
	}

}
