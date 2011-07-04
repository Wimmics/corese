package fr.inria.acacia.corese.triple.parser;

import java.util.StringTokenizer;

import fr.inria.acacia.corese.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This is the Lexer of the triple parser, it delivers tokens of the 
 * triple query language; it is used by the 1st parser.
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public class Lexer extends StringTokenizer {
  static String SDT=KeywordPP.SDT;
  static String LANG=KeywordPP.LANG;
  static String end1="\")";
  static String end2="')";
  String token=null;
  boolean isOpenParen=false;
  boolean isCloseParen=false;

  Lexer(String str){
    super(str);
  }

  public String next(){
     String str = nextToken(false);
     return str;
   }

  public String lookAhead(){
    String str = nextToken(true);
     return str;
  }

  public boolean hasMore(){
    return token != null || hasMoreElements() ;
  }

  /**
   *
   * @param lookahead = true : lookahead one token
   * store the token and return it at nextToken()
   * @return
   */
  public String nextToken(boolean lookahead){
    String str, save=null, result;
    if (token==null){
      //token=super.nextToken();
      token=parseToken();
    }
    if (lookahead)
      save=token;
    String open=KeywordPP.OPEN_PARENTHESIS, close = KeywordPP.CLOSE_PARENTHESIS;
    int index=token.indexOf(open);
    if (index == -1){
      index = token.indexOf(KeywordPP.OPEN_BRACKET); // fake sparql
      if (index > 0) index = -1;
    }
    if (index == 0){
      if (token.length()>1)
        token=token.substring(1);
      else token=null;
      result= open;
    }
    else if (index > 0){
      result= token.substring(0, index);
      token=token.substring(index);
    }
    else {
      index=token.indexOf(close);
      if (index == -1){
        index=token.indexOf(KeywordPP.CLOSE_BRACKET); // sparql
        if (index > 0) index = -1;
      }
      if ( index > 0){
        // ends with )
        str=token.substring(0, index);
        token=token.substring(index);
        result= str;
      }
      else if (index==0){
        if (token.length()>1)
          token=token.substring(1);
        else token=null;
        result= close;
      }
      else {
        result= token;
        token=null;
      }
    }
    if (lookahead)
      token=save;
    return result;
  }

  /**
   *  nextToken that parses string with space inside such as :
   * " aa bb " and "xxx jjj"^^xsd:string
   */
  String parseToken() {
    String v = super.nextToken();
    String str = "";
    String end = null, endbis=null;
    if (v.startsWith("\"")) {
      end = "\"";
      endbis=end1; // case ")
    }
    else if (v.startsWith("'")) {
      end = "'";
      endbis=end2; // case ')
    }
    if (end != null) {
      // tricky : case " "  ; first token is " ; tricky : "  \"   "
      while (hasMoreElements() &&
             (v.equals(end)      || v.endsWith("\\\"")  ||
              ! (v.endsWith(end) || v.indexOf(SDT) >= 0 || v.indexOf(LANG) >= 0 ||
                 v.endsWith(endbis)))) {
        // value might be "aaa bbb"^^xsd:string
        str = super.nextToken();
        v += " " + str;
      }
    }
    return v;
  }


  boolean isOpenParen(){
    return isOpenParen;
  }

  boolean isCloseParen(){
    return isCloseParen;
  }

}