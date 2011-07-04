package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class parses objects of type Exp (mainly triples).<br>
 * With classes TermParser.java and Parser.java, they are the most important classes for the 1st parser.
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public class ExpParser {

  public static final String CONNEX="connex";
  public static final String SORT="sort";
  public static final String REVERSE="reverse";
  public static final String SYSVAR = "?_cos_";
  public static final String KGRAMVAR = "?_kgram_";
  public static final String BNVAR = "?_bn_";
  public static final String VSORT="order";
  public static final String[] ASORT={SORT,  VSORT};
  public static final String[] ABY={ "by"};
  public static final String[] AREVERSE={REVERSE,  "desc"};
  public static final String DISPLAYBNID="bnid"; // display blank node id
  public static final String[] ALIMIT={KeywordPP.PROJECTION,  KeywordPP.LIMIT};  // max number of projection
  public static final String FAKE="fake"; // do not produce results, just count proj
  public static final String NONE="none";
  public static final String BIND="bind"; // generate variable binding in Result
  public static final String SOURCE = "source";
  public static final String STATE = KeywordPP.STATE;
  public static final String LEAF = KeywordPP.LEAF;
  
  public static final String[] ASOURCE ={SOURCE, STATE, KeywordPP.GRAPH};
  public static final String ACCESS  ="access";
  public static final String OPTION  ="option";
  public static final String ASQUERY = "asquery";
  public static final String[] AOPTION  ={OPTION,  KeywordPP.OPTIONAL} ;
  public static final String NOT  = "not";
  public static final String OR  ="or";
  public static final String[] AUNION  ={OR, KeywordPP.UNION, Keyword.SEOR};
  public static final String OPAREN  = KeywordPP.OPEN_PARENTHESIS;
  public static final String CPAREN  = KeywordPP.CLOSE_PARENTHESIS;
  public static final String [] AFROM  = {KeywordPP.FROM};
  public static final String [] ANAMED  = {KeywordPP.NAMED};
  public static final String [] AAND  = {"and", Keyword.SEAND};
  public static final String [] ANOT  = {NOT, Keyword.SENOT};
  public static final String [] AFILTER  = {KeywordPP.FILTER};
  public static final String DELETE  = KeywordPP.DELETE;

  ExpParser(){
  }

  /**
   * Parse a triple query
   * May start with : select [more|list|merge] where
   * @deprecated
   */
     Exp parse(Parser parser, String squery){
       Lexer lex=new Lexer(squery);
       Exp exp=null, res;
       String fst=null;
       if (lex.hasMoreElements()){
         fst=lex.lookAhead();
         if (fst.equalsIgnoreCase(ACCESS)){
           fst=lex.next(); // eat access
           fst=next(parser, lex);// eat access value
//           if (! fst.equalsIgnoreCase(NONE))
//             parser.setAccess(fst);
           if (lex.hasMoreElements())
             fst=lex.lookAhead();
           else return new And();
         }
         while (isKW(fst, KeywordPP.PREFIX)) {
           //e = new Triple(parser.getTripleId()).parse(parser, lex);
           Triple triple=new Triple(); //parser.getTripleId());
           parse(parser, lex, triple);
           //if (exp == null) exp = new And();
           //exp.add(e);
           if (lex.hasMoreElements())
             fst=lex.lookAhead();
         }

         if (isKW(fst, KeywordPP.DELETE)){
           fst = lex.next(); // delete
           fst = lex.next(); // *
           fst = lex.lookAhead();
           parser.setDelete(true);
         }

          if (isKW(fst, KeywordPP.SELECT)){
           fst=lex.next();
           boolean go=true;
           while (go && lex.hasMoreElements()){
             fst=next(parser, lex);
             if (isMore(fst)){
              parser.setMore(true);
             }
             else if (fst.equalsIgnoreCase(KeywordPP.ONE)){
               parser.setOne(true);
             }
             else if (fst.equalsIgnoreCase(KeywordPP.DEBUG)){
              parser.setDebug(true);
            }
            else if (fst.equalsIgnoreCase(KeywordPP.NOSORT)){
              parser.setSorted(false);
            }
             else if (isKW(fst, KeywordPP.DISTINCT)){
               String next=lex.lookAhead();
               if (next.equalsIgnoreCase(KeywordPP.SORTED)){
                 // Corese distinct sorted
                 lex.next();
                 parser.setStrictDistinct(false);
               }
               else {
                 parser.setStrictDistinct(true);
               }
               parser.setDistinct(true);
             }
             else if (isMerge(fst)){
               parser.setMerge(true);
             }
             else if (isList(fst)){
               parser.setList(true);
             }
             else if (fst.equalsIgnoreCase(BIND)){
              parser.setBinding(true);
            }
             else if (fst.equalsIgnoreCase(CONNEX)){
               parser.setConnex(true);
             }
             else if (fst.equalsIgnoreCase(KeywordPP.TABLE)){
              parser.setTable(true);
            }
            else if (fst.equalsIgnoreCase(FAKE)){
              parser.setFake(true);
            }
            else if (fst.equalsIgnoreCase(KeywordPP.THRESHOLD)){
              if (lex.hasMoreElements()) {
                fst = next(parser, lex);
                parser.setThreshold(Float.parseFloat(fst.trim()));
              }
            }
            else if (fst.equalsIgnoreCase(KeywordPP.GROUP)){
              if (lex.hasMoreElements()) {
                fst = next(parser, lex);
                parser.setGroup(fst);
              }
            }
            else if (isKW(fst, ASORT)) {
              boolean reverse = false;
              if (lex.hasMoreElements()) {
                fst = lex.lookAhead();
                if (isKW(fst, AREVERSE)) {
                  fst = next(parser, lex);
                  reverse = true;
                }
                Expression sort = new TermParser(parser).bool(lex);
                parser.setSort(sort, reverse);
              }
            }

           else if (fst.equalsIgnoreCase(KeywordPP.COUNT)){
            if (lex.hasMoreElements()) {
              fst = next(parser, lex);
              parser.setCount(fst);
            }
          }
            else if (fst.equalsIgnoreCase(KeywordPP.DISPLAY)) {
              if (lex.hasMoreElements()) {
                fst = next(parser, lex);
                if (fst.equalsIgnoreCase(NONE)) {}
                else {
                  if (fst.equalsIgnoreCase(KeywordPP.TABLE)) {
                    parser.setTable(true);
                  }
                  else
                  if (fst.equalsIgnoreCase(KeywordPP.XML)) {
                    parser.setXMLBind(true);
                  }
                  else if (fst.equalsIgnoreCase(DISPLAYBNID) || fst.equalsIgnoreCase(KeywordPP.BLANK)) {
                    parser.setDisplayBNID(true);
                  }
                  else if (fst.equalsIgnoreCase(KeywordPP.FLAT)) {
                    parser.setFlat(true);
                  }
                  else if (fst.equalsIgnoreCase(ASQUERY)) {
                    parser.setPQuery(true);
                  }
                  else {
                    parser.setMaxDisplay(Integer.parseInt(fst.trim()));
                  }
                }
              }
            }
            else if (fst.equalsIgnoreCase(KeywordPP.RESULT)){
              if (lex.hasMoreElements()){
                fst=next(parser, lex);
                parser.setMaxResult(Integer.parseInt(fst.trim()));
              }
            }
            else if (isKW(fst, ALIMIT)){
             if (lex.hasMoreElements()){
               fst=next(parser, lex);
               parser.setMaxProjection(Integer.parseInt(fst.trim()));
             }
           }
           else if (fst.equals(KeywordPP.STAR)) {
             parser.setSelectAll(true);
         }
         else if (isKW(fst, AFROM)) {
           fst = lex.next();
           if (isKW(fst, ANAMED)) {
             fst=lex.next();
             parser.setNamed(fst);
           }
           else {
             parser.setFrom(fst);
           }
         }

         else if (!isKW(fst, KeywordPP.WHERE) && !fst.equalsIgnoreCase(NONE)) {
           parser.setSelect(fst);
         }
            if (isKW(fst, KeywordPP.WHERE)){
              go=false;
            }
           }
           fst=null;
         }
       }
       res= parse(parser, lex);
       if (exp != null){ // prefix
         exp.add(res);
         res  = exp;
       }
       return res;
     }

     String next(Parser parser, Lexer lex) {
       String fst = lex.next();
       if (parser.isPGet(fst)) {
         fst = parser.getPValue(fst); //parser.getValue(parser.get(fst));
       }
       if (fst==null || fst.equals(""))
         fst=NONE;
       return fst;
     }


     /**
      * Parse the body of the query, after the select where
      *
      */
     Exp parse(Parser parser, Lexer st){
       String fst, src=null,  score=null;
       Atom asrc=null;
       boolean state = false, leaf = false;
       boolean isFilter = false;
       Exp exp=null, e=null;
       And stack=new And();
       Triple triple;
       boolean isOption=false, isNot=false;
       if (! st.hasMoreElements())
         return stack;
       fst=st.lookAhead();
       while (st.hasMoreElements() && ! isCloseParen(fst)){
         isOption=false;
         isNot=false;
         isFilter = false;
         if (isKW(fst, AOPTION)){
           st.next();
           fst=st.lookAhead();
           isOption=true;
         }
         else if (isKW(fst, ANOT)){
           st.next();
           fst=st.lookAhead();
           isNot=true;
         }
         if (isKW(fst, ASOURCE)) {
           if (isKW(fst, STATE)) state = true;
           st.next();
           fst = st.lookAhead();
           if (isKW(fst, LEAF)){
        	   leaf = true;
        	   st.next();
           }
           src = st.next();
    	   if (Triple.isVariable(src)){
    		   asrc = new Variable(src);
    		   parser.setSource(src);
    	   }
    	   else asrc = new Constant(src);  
    	   fst = st.lookAhead();
         }
         if (isKW(fst, KeywordPP.SCORE)) {
           st.next();
           score = st.next();
           fst = st.lookAhead();
         }
         if (isKW(fst, AFILTER)) {
           isFilter = true;
           st.next();
           fst=st.lookAhead();
         }

         if (isOpenParen(fst) && ! isFilter){
           // parse an ( )
           st.next(); // eat "("
           e=new ExpParser().parse(parser, st);
           if (asrc != null){
        	   Source s =  Source.create(asrc, e);
             e = s;
             if (state) s.setState(true);
             if (leaf){ s.setLeaf(true); }
             //e.finalize(parser);
             //e.setSource(src);
           }
           if (score != null){
             parser.setScore(true);
             e = new Score(score, e);
           }
           if (isOption) {
             //e.setOption(true);
             e= Option.create(e);
           }
          /* if (isNot){
             Term term=e.toTerm();
             parser.trace("** ExpParser : " + e) ;
             e =  Triple.create(new Term(CompMarkerParser.STNOT, term, term));
             //parser.trace("** ExpParser : " + e);
           }*/
           src=null;
           asrc = null;
           state = false;
           leaf = false;
           st.next(); // eat ")"
           stack.add(e);
         }

         else if (isFilter){
           // parse a filter
           Expression filter = new TermParser(parser).bool(st);
           // compute get:gui if any :
           filter = filter.parseGet(parser);
           if (filter != null){
             triple = Triple.create(filter);
             //triple.setID(parser.getTripleId());
             stack.add(triple);
             parser.addFilter(filter);
           }
         }

         else {
           // parse a triple
           triple=new Triple();//parser.getTripleId());
           e = parse(parser, st, triple);
           if (e != null){
             // if triple is get:gui and there is no value, it may be null
             if (asrc != null){
               e =  Source.create(asrc, e);
               //e.finalize(parser);
               //e.setSource(src);
             }
             if (isOption) e =  Option.create(new And(e)); //e.setOption(true);
             else if (isNot) e.setNegation(true);
             src = null;
             stack.add(e);
           }
         }

         if (st.hasMoreElements())
           fst=st.lookAhead();
         if (fst.equals(".")){
           st.next();
           if (st.hasMoreElements())
             fst=st.lookAhead();
         }
         if (isAnd(fst)){
           st.next();
           if (st.hasMoreElements())
             fst=st.lookAhead();
         }
         else if (isKW(fst, AUNION)){
           st.next();
           parser.setAll(true);
           if (exp==null){
             exp=new Or();
           }
           exp.add(stack);
           stack=new And();
           if (st.hasMoreElements())
             fst=st.lookAhead();
         }
       }
       if (exp==null)
         exp=stack;
       else exp.add(stack);
       return exp;
     }

     Exp parse(Parser parser, Lexer st, Triple triple) {
       TermParser tp = new TermParser(parser);
       String str = st.lookAhead();
       if (isKW(str, "tuple")){
    	   return parseNary(parser, st, triple);
       }
       Expression exp1 = tp.funatom(st);
       String prop = st.next();
       Expression exp2 = tp.funatom(st);
       return triple; //triple.parse(parser, exp1, prop, exp2);
     }
     
     
     Exp parseNary(Parser parser, Lexer st, Triple triple) {
         TermParser tp = new TermParser(parser);
         String str = st.next(); // eat token triple
         st.next(); // eat token "("
         
         Expression exp1 = tp.funatom(st);
         String prop = st.next();
         Expression exp2 = tp.funatom(st);
         
    	 Vector<Expression> vexp = new Vector<Expression>();
         while (st.hasMore()){
      	   str = st.lookAhead();
      	   if (isKW(str, ")")){
      		   st.next(); // eat ")"
      		   break;
      	   }
      	   else {
      		   Expression e3 = tp.funatom(st);
      		   vexp.add(e3);  
      	   } 
         }
		 return triple; //triple.parse(parser, exp1, prop, exp2, vexp);    
     }
     
     

//     Exp parse2(Parser parser, Lexer st, Triple triple) {
//       TermParser tp = new TermParser(parser);
//       Expression exp1 = tp.exp(st);
//       String prop = st.next();
//       Expression exp2 = tp.exp(st);
//       return triple.parse(parser, exp1, prop, exp2);
//     }



     boolean isNot(String fst){
      return isKW(fst, ANOT);
    }

     boolean isMore(String fst){
       return fst.equalsIgnoreCase(KeywordPP.MORE);
     }

     boolean isMerge(String fst){
       return fst.equalsIgnoreCase(KeywordPP.MERGE);
     }

     boolean isList(String fst){
       return fst.equalsIgnoreCase(KeywordPP.LIST);
     }

    static boolean isKW(String fst, String[] kw){
       for (int i=0; i<kw.length; i++){
         if (fst.equalsIgnoreCase(kw[i]))
           return true;
       }
       return false;
    }

    static boolean isKW(String fst, String kw) {
      return fst.equalsIgnoreCase(kw);
    }

     boolean isAnd(String fst){
       return isKW(fst, AAND);
     }

     boolean isCloseParen(String fst){
       return fst.equals(CPAREN);
     }

     boolean isOpenParen(String fst){
       return fst.equals(OPAREN);
     }

   }

