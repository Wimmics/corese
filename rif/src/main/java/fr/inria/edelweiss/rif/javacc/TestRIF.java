package fr.inria.edelweiss.rif.javacc;

import java.io.File;
import java.io.StringReader;

import fr.inria.edelweiss.rif.ast.RIFDocument;
import fr.inria.edelweiss.rif.ast.RIFPSDocument;
import fr.inria.edelweiss.rif.ast.RIFXMLDocument;

public class TestRIF {
	
	private String zou ;
	
	public String getZou() {
		return this.zou == null ? this.zou = "kikoolo" : this.zou ;
	}
	
	public void setZou(String s) {
		this.zou = s ;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String rif = "Document(\n"+
 "Prefix(xs <http://www.w3.org/2001/XMLSchema#>)\n"+
 "Prefix(rdf <http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\n"+ 
 "Prefix(ex <http://example.com/example#>) \n"+
 "Prefix(func <http://www.w3.org/2007/rif-builtin-function#>)\n"+ 
 "Prefix(pred <http://www.w3.org/2007/rif-builtin-predicate#>)\n"+ 

 "Group (\n"+ 
  "Forall ?x ?y ( ex:ok() :-\n"+ 
   "And (\n"+
    "External( pred:literal-not-identical( \"1\"^^xs:integer \"1\"^^xs:string ) )\n"+ 
    "External( pred:literal-not-identical( \"1\"^^xs:integer \"2\"^^xs:integer ) )\n"+
    "External( pred:literal-not-identical( \"Hello world@\"^^rdf:PlainLiteral \"Hello world@\"^^xs:string ) )\n"+
    "External( pred:is-literal-anyURI( \"http://www.example.org\"^^xs:anyURI ) )\n"+
    "External( pred:is-literal-base64Binary( \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz/+\"^^xs:base64Binary ) )\n"+
    "External( pred:is-literal-boolean( \"1\"^^xs:boolean ) )\n"+
    "External( pred:is-literal-date ( \"2000-12-13-11:00\"^^xs:date ) )\n"+
    "External( pred:is-literal-dateTime ( \"2000-12-13T00:11:11.3\"^^xs:dateTime ) )\n"+
    "External( pred:is-literal-dateTimeStamp ( \"2000-12-13T00:11:11.3Z\"^^xs:dateTimeStamp ) )\n"+
    "External( pred:is-listeral-double( \"1.2E34\"^^xs:double ) )\n"+
    "External( pred:is-literal-float( \"-INF\"^^xs:float ) )\n"+
    "External( pred:is-literal-hexBinary( \"aabb\"^^xs:hexBinary ) )\n"+
    "External( pred:is-literal-decimal( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-integer( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-long( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-int( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-short( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-byte( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-nonNegativeInteger( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-positiveInteger( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-unsignedLong( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-unsignedInt( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-unsignedShort( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-unsignedByte( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-nonPositiveInteger( \"-1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-negativeInteger( \"-1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-PlainLiteral( \"Hello world@en\"^^rdf:PlainLiteral ) )\n"+
    "External( pred:is-literal-PlainLiteral( \"Hello world@\"^^rdf:PlainLiteral ) )\n"+
    "External( pred:is-literal-string( \"Hello world@\"^^rdf:PlainLiteral ) )\n"+
    "External( pred:is-literal-string( \"Hello world\"^^xs:string ) )\n"+
    "External( pred:is-literal-normalizedString( \"Hello world\"^^xs:string ) )\n"+
    "External( pred:is-literal-token( \"Hello world\"^^xs:string ) )\n"+
    "External( pred:is-literal-language( \"en\"^^xs:language ) )\n"+
    "External( pred:is-literal-Name( \"Hello\"^^xs:Name ) )\n"+
    "External( pred:is-literal-NCName( \"Hello\"^^xs:NCName ) )\n"+
    "External( pred:is-literal-NMTOKEN( \"Hello\"^^xs:NMTOKEN ) )\n"+
    "External( pred:is-literal-time ( \"00:11:11.3Z\"^^xs:time ) )\n"+
    "External( pred:is-literal-dayTimeDuration ( \"P3DT2H\"^^xs:dayTimeDuration ) )\n"+
    "External( pred:is-literal-yearMonthDuration ( \"P1Y2M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:is-literal-XMLLiteral ( \"<br/>\"^^xs:XMLLiteral ) )\n"+
    "External( pred:is-literal-not-anyURI(\"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-base64Binary( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-boolean( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-date ( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-dateTime ( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-dateTimeStamp ( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-double( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-float( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-hexBinary( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-decimal( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-integer( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-long( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-int( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-short( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-byte( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-nonNegativeInteger( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-positiveInteger( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-unsignedLong( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-unsignedInt( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-unsignedShort( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-unsignedByte( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-nonPositiveInteger( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-negativeInteger( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-PlainLiteral( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-string( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-normalizedString( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-token( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-language( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-Name( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-NCName( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-NMTOKEN( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-not-time ( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-dayTimeDuration ( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-yearMonthDuration ( \"foo\"^^xs:string ) )\n"+
    "External( pred:is-literal-not-XMLLiteral ( \"1\"^^xs:integer ) )\n"+
    "External( pred:is-literal-anyURI( External ( xs:anyURI ( \"http://www.example.org\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-base64Binary( External ( xs:base64Binary ( \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz/+\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-boolean( External ( xs:boolean ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-date ( External ( xs:date ( \"2000-12-13-11:00\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-dateTime ( External ( xs:dateTime ( \"2000-12-13T00:11:11.3\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-dateTimeStamp ( External ( xs:dateTimeStamp ( \"2000-12-13T00:11:11.3Z\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-double( External ( xs:double (\"1.2E34\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-float( External ( xs:float ( \"-1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-hexBinary( External ( xs:hexBinary (  \"aabb\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-decimal( External( xs:decimal ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-integer(  External( xs:integer ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-long( External( xs:long ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-int(  External( xs:int ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-short(  External( xs:short ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-byte(  External( xs:byte ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-nonNegativeInteger(  External( xs:nonNegativeInteger ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-positiveInteger(  External( xs:positiveInteger ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-unsignedLong(  External( xs:unsignedLong ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-unsignedInt(  External( xs:unsignedInt ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-unsignedShort(  External( xs:unsignedShort ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-unsignedByte(  External( xs:unsignedByte ( \"1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-nonPositiveInteger( External( xs:nonPositiveInteger (\"-1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-negativeInteger( External( xs:negativeInteger (\"-1\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-PlainLiteral( External( rdf:PlainLiteral (\"1\"^^xs:integer ) ) ) )\n"+
    "External( pred:is-literal-string(  External( rdf:string (\"1\"^^xs:integer ) ) ) )\n"+
    "External( pred:is-literal-normalizedString(  External( rdf:normalizedString (\"1\"^^xs:integer ) ) ) )\n"+
    "External( pred:is-literal-token( External( xs:token(  \"de\"^^xs:hexBinary ) ) ) )\n"+
    "External( pred:is-literal-language( External ( xs:language ( \"de\"^^xs:hexBinary ) ) ) )\n"+
    "External( pred:is-literal-Name( External ( xs:Name ( \"de\"^^xs:hexBinary ) ) ) )\n"+
    "External( pred:is-literal-NCName( External ( xs:NCName ( \"de\"^^xs:hexBinary ) ) ) )\n"+
    "External( pred:is-literal-NMTOKEN( External ( xs:NMTOKEN ( \"de\"^^xs:hexBinary ) ) ) )\n"+
    "External( pred:is-literal-time (  External ( xs:time ( \"00:11:11.3Z\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-dayTimeDuration (  External ( xs:dayTimeDuration ( \"P3DT2H\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-yearMonthDuration (  External ( xs:yearMonthDuration ( \"P1Y2M\"^^xs:string ) ) ) )\n"+
    "External( pred:is-literal-XMLLiteral (  External ( xs:XMLLiteral( \"<br/>\"^^xs:string ) ) ) )\n"+
    "External( pred:iri-string( <http://www.example.org> \"http://www.example.org\"  ) )\n"+
    "2 = External ( func:numeric-add( 1 1 ) )\n"+
    "1 = External ( func:numeric-subtract( 2 1 ) )\n"+
    "6 = External ( func:numeric-multiply( 2 3 ) )\n"+
    "2 = External ( func:numeric-divide( 6 3 ) )\n"+
    "1 = External ( func:numeric-integer-divide( 5 3 ) )\n"+
    "2 = External ( func:numeric-integer-mod( 5 3 ) )\n"+
    "External ( pred:numeric-equal( \"0.0E0\"^^xs:double External ( func:numeric-subtract( 1 1 ) ) ) )\n"+
    "External ( pred:numeric-not-equal( 0 1 ) )\n"+
    "External (  pred:numeric-less-than ( 1 2 ) )\n"+
    "External (  pred:numeric-less-than-or-equal ( 1 2 ) )\n"+
    "External (  pred:numeric-less-than-or-equal ( 1 1 ) )\n"+
    "External (  pred:numeric-greater-than ( 2 -1 ) )\n"+
    "External (  pred:numeric-greater-than-or-equal ( 2 -1 ) )\n"+
    "External (  pred:numeric-greater-than-or-equal ( 2  2 ) )\n"+
    "\"0\"^^xs:boolean = External ( func:not( \"1\"^^xs:boolean) )\n"+
    "External ( pred:numeric-not-equal( 0 1  ) )\n"+
    "External ( pred:boolean-equal( \"0\"^^xs:boolean \"0\"^^xs:boolean ) )\n"+
    "External ( pred:boolean-less-than( \"0\"^^xs:boolean \"1\"^^xs:boolean ) )\n"+
    "External ( pred:boolean-greater-than( \"1\"^^xs:boolean \"0\"^^xs:boolean ) )\n"+
    "-1 = External ( func:compare( \"bar\" \"foo\" ) )\n"+
    "1 = External ( func:compare( \"foo\" \"bar\" ) )\n"+
    "0 = External ( func:compare( \"bar\" \"bar\" ) )\n"+
    "\"foobar\" = External ( func:concat( \"foo\" \"bar\" ) )\n"+
    "\"foo,bar\" = External ( func:string-join( \"foo\" \"bar\" \",\" ) )\n"+
    "\"bar\" = External ( func:substring( \"foobar\" 3 ) )\n"+
    "\"foo\" = External ( func:substring( \"foobar\" 0 3 ) )\n"+
    "3 = External ( func:string-length( \"foo\" ) )\n"+
    "\"FOOBAR\" = External ( func:upper-case( \"FooBar\" ) )\n"+
    "\"foobar\" = External ( func:lower-case( \"FooBar\" ) )\n"+
    "\"RIF%20Basic%20Logic%20Dialect\" = External ( func:encode-for-uri( \"RIF Basic Logic Dialect\" ) )\n"+
    "\"http://www.example.com/~b%C3%A9b%C3%A9\" = External ( func:iri-to-uri ( \"http://www.example.com/~bébé\" ) )\n"+
    "\"javascript:if (navigator.browserLanguage == 'fr') window.open('http://www.example.com/~b%C3%A9b%C3%A9');\" = External ( func:escape-html-uri ( \"javascript:if (navigator.browserLanguage == 'fr') window.open('http://www.example.com/~bébé');\" ) )\n"+ 
    "\"foo\" = External ( func:substring-before( \"foobar\" \"bar\" ) )\n"+
    "\"bar\" = External ( func:substring-after( \"foobar\" \"foo\" ) )\n"+
    "\"[1=ab][2=]cd\" = External ( func:replace( \"abcd\" \"(ab)|(a)\" \"[1=$1][2=$2]\" ) )\n"+
    "External( pred:contains ( \"foobar\" \"foo\" ) )\n"+
    "External( pred:starts-with ( \"foobar\" \"foo\" ) )\n"+
    "External( pred:ends-with ( \"foobar\" \"bar\" ) )\n"+
    "External( pred:matches ( \"abracadabra\" \"^a.*a$\" ) )\n"+
    "External( func:year-from-dateTime( \"1999-12-31T24:00:00\"^^xs:dateTime ) ) = 2000\n"+
    "External( func:month-from-dateTime( \"1999-05-31T13:20:00-05:00\"^^xs:dateTime ) ) = 5\n"+
    "External( func:day-from-dateTime( \"1999-05-31T13:20:00-05:00\"^^xs:dateTime ) ) = 31\n"+
    "External( func:hours-from-dateTime( \"1999-05-31T08:20:00-05:00\"^^xs:dateTime ) ) = 8\n"+
    "External( func:minutes-from-dateTime( \"1999-05-31T13:20:00-05:00\"^^xs:dateTime ) ) = 20\n"+
    "External( func:seconds-from-dateTime( \"1999-05-31T13:20:00-05:00\"^^xs:dateTime ) ) = 0\n"+
    "External( func:year-from-date( \"1999-12-31\"^^xs:date ) ) = 1999\n"+
    "External( func:month-from-date( \"1999-05-31\"^^xs:date ) ) = 5\n"+
    "External( func:day-from-date( \"1999-05-31\"^^xs:date ) ) = 31\n"+
    "External( func:hours-from-time( \"08:20:00-05:00\"^^xs:time ) ) = 8\n"+
    "External( func:minutes-from-time( \"13:20:00-05:00\"^^xs:time ) ) = 20\n"+
    "External( func:seconds-from-time( \"13:20:00-05:00\"^^xs:time ) ) = 0\n"+
    "External( func:timezone-from-dateTime( \"1999-05-31T13:20:00-05:00\"^^xs:dateTime ) ) = \"-PT5H\"^^xs:dayTimeDuration\n"+
    "External( func:timezone-from-date( \"1999-05-31-05:00\"^^xs:date ) ) = \"-PT5H\"^^xs:dayTimeDuration\n"+
    "External( func:timezone-from-time( \"13:20:00-05:00\"^^xs:time) ) = \"-PT5H\"^^xs:dayTimeDuration\n"+
    "External( func:years-from-duration( \"P20Y15M\"^^xs:yearMonthDuration ) ) = 21\n"+
    "External( func:months-from-duration( \"P20Y15M\"^^xs:yearMonthDuration ) ) = 3\n"+
    "External( func:days-from-duration( \"P3DT10H\"^^xs:dayTimeDuration ) ) = 3\n"+
    "External( func:hours-from-duration( \"P3DT10H\"^^xs:dayTimeDuration ) ) = 10\n"+ 
    "External( func:minutes-from-duration( \"-P5DT12H30M\"^^xs:dayTimeDuration ) ) = -30\n"+
    "External( func:seconds-from-duration( \"P3DT10H12.5S\"^^xs:dayTimeDuration ) ) = 12.5\n"+
    "External( func:subtract-dateTimes( \"2000-10-30T06:12:00\"^^xs:dateTime \"1999-11-28T09:00:00Z\"^^xs:dateTime) ) = \"P337DT2H12M\"^^xs:dayTimeDuration\n"+
    "External( func:subtract-dates( \"2000-10-30\"^^xs:date \"1999-11-28\"^^xs:date ) ) = \"P337D\"^^xs:dayTimeDuration\n"+
    "External( func:subtract-times( \"11:12:00Z\"^^xs:time \"04:00:00\"^^xs:time ) ) = \"PT2H12M\"^^xs:dayTimeDuration\n"+
    "External( func:add-yearMonthDurations(\"P2Y11M\"^^xs:yearMonthDuration \"P3Y3M\"^^xs:yearMonthDuration) ) = \"P6Y2M\"^^xs:yearMonthDuration\n"+
    "External( func:subtract-yearMonthDurations(\"P2Y11M\"^^xs:yearMonthDuration \"P3Y3M\"^^xs:yearMonthDuration ) ) = \"-P4M\"^^xs:yearMonthDuration\n"+
    "External( func:multiply-yearMonthDuration(\"P2Y11M\"^^xs:yearMonthDuration 2.3 ) ) = \"P6Y9M\"^^xs:yearMonthDuration\n"+
    "External( func:divide-yearMonthDuration(\"P2Y11M\"^^xs:yearMonthDuration 1.5 ) ) = \"P1Y11M\"^^xs:yearMonthDuration\n"+
    "External( func:divide-yearMonthDuration-by-yearMonthDuration( \"P3Y4M\"^^xs:yearMonthDuration \"-P1Y4M\"^^xs:yearMonthDuration ) ) = -2.5\n"+
    "External( func:add-dayTimeDurations( \"P2DT12H5M\"^^xs:dayTimeDuration \"P5DT12H\"^^xs:dayTimeDuration) ) = \"P8DT5M\"^^xs:dayTimeDuration\n"+
    "External( func:subtract-dayTimeDurations( \"P2DT12H\"^^xs:dayTimeDuration \"P1DT10H30M\"^^xs:dayTimeDuration ) ) = \"P1DT1H30M\"^^xs:dayTimeDuration\n"+
    "External( func:multiply-dayTimeDuration( \"PT2H10M\"^^xs:dayTimeDuration 2.1 ) ) = \"PT4H33M\"^^xs:dayTimeDuration\n"+
    "External( func:divide-dayTimeDuration( \"P4D\"^^xs:yearMonthDuration 2 ) ) = \"P2D\"^^xs:dayTimeDuration\n"+
   "External( func:divide-dayTimeDuration-by-dayTimeDuration( \"P4D\"^^xs:yearMonthDuration \"P2D\"^^xs:dayTimeDuration ) ) = 2\n"+
    "External( func:add-yearMonthDuration-to-dateTime( \"2000-10-30T11:12:00\"^^xs:dateTime \"P1Y2M\"^^xs:yearMonthDuration ) ) = \"2001-12-30T11:12:00\"^^xs:dateTime\n"+
    "External( func:add-yearMonthDuration-to-date( \"2000-10-30\"^^xs:date \"P1Y2M\"^^xs:yearMonthDuration ) ) = \"2001-12-30\"^^xs:date\n"+
    "External( func:add-dayTimeDuration-to-dateTime( \"2000-10-30T11:12:00\"^^xs:dateTime \"P3DT1H15M\"^^xs:dayTimeDuration ) ) = \"2000-11-02T12:27:00\"^^xs:dayTime\n"+
    "External( func:add-dayTimeDuration-to-date(\"2004-10-30Z\"^^xs:date \"P2DT2H30M0S\"^^xs:dayTimeDuration ) ) = \"2004-11-01\"^^xs:date\n"+
    "External( func:add-dayTimeDuration-to-time( \"11:12:00\"^^xs:time \"P3DT1H15M\"^^xs:dayTimeDuration ) ) = \"12:27:00\"^^xs:time\n"+
    "External( func:add-dayTimeDuration-to-time( \"23:12:00+03:00\"^^xs:time \"P1DT3H15M\"^^xs:dayTimeDuration ) ) = \"02:27:00+03:00\"^^xs:time\n"+
    "External( func:subtract-yearMonthDuration-from-dateTime( \"2000-10-30T11:12:00\"^^xs:dateTime \"P1Y2M\"^^xs:yearMonthDuration ) ) = \"1999-08-30T11:12:00\"^^xs:dateTime\n"+
    "External( func:subtract-yearMonthDuration-from-date( \"2000-10-30\"^^xs:date \"P1Y2M\"^^xs:yearMonthDuration ) ) = \"1999-08-30\"^^xs:date\n"+
    "External( func:subtract-dayTimeDuration-from-dateTime( \"2000-10-30T11:12:00\"^^xs:dateTime \"P3DT1H15M\"^^xs:dayTimeDuration ) ) = \"2000-10-27T09:57:00\"^^xs:dateTime\n"+
    "External( func:subtract-dayTimeDuration-from-date( \"2000-10-30\"^^xs:date \"P3DT1H15M\"^^xs:dayTimeDuration ) ) = \"2000-10-26\"^^xs:date\n"+
    "External( func:subtract-dayTimeDuration-from-time( \"11:12:00\"^^xs:time \"P3DT1H15M\"^^xs:dayTimeDuration ) ) = \"09:57:00\"^^xs:time\n"+
    "External( pred:dateTime-equal( \"2002-04-02T12:00:00-01:00\"^^xs:dateTime \"2002-04-02T17:00:00+04:00\"^^xs:dateTime ) )\n"+
    "External( pred:dateTime-less-than( \"2002-04-01T12:00:00-01:00\"^^xs:dateTime \"2002-04-02T17:00:00+04:00\"^^xs:dateTime ) )\n"+
    "External( pred:dateTime-greater-than( \"2002-04-03T12:00:00-01:00\"^^xs:dateTime \"2002-04-02T17:00:00+04:00\"^^xs:dateTime ) )\n"+
    "External( pred:date-equal( \"2004-12-25-12:00\"^^xs:date \"2004-12-26+12:00\"^^xs:date ) )\n"+
    "External( pred:date-less-than( \"2004-12-24\"^^xs:date \"2004-12-26\"^^xs:date ) )\n"+
    "External( pred:date-greater-than( \"2004-12-26\"^^xs:date \"2004-12-25\"^^xs:date ) )\n"+
    "External( pred:time-equal( \"21:30:00+10:30\"^^xs:time \"06:00:00-05:00\"^^xs:time ) )\n"+
    "External( pred:time-less-than( \"20:30:00+10:30\"^^xs:time \"06:00:00-05:00\"^^xs:time ) )\n"+
    "External( pred:time-greater-than( \"22:30:00+10:30\"^^xs:time \"06:00:00-05:00\"^^xs:time ) )\n"+
    "External( pred:duration-equal( \"P1Y\"^^xs:yearMonthDuration \"P12M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:yearMonthDuration-less-than( \"P1Y\"^^xs:yearMonthDuration \"P13M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:yearMonthDuration-greater-than( \"P1Y\"^^xs:yearMonthDuration \"P11M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:dayTimeDuration-less-than( \"P1D\"^^xs:dayTimeDuration \"PT25H\"^^xs:dayTimeDuration ) )\n"+
    "External( pred:dayTimeDuration-greater-than( \"P1D\"^^xs:dayTimeDuration \"PT23H\"^^xs:dayTimeDuration ) )\n"+
    "External( pred:dateTime-not-equal( \"2002-04-01T12:00:00-01:00\"^^xs:dateTime \"2002-04-02T17:00:00+04:00\"^^xs:dateTime ) )\n"+
    "External( pred:dateTime-less-than-or-equal( \"2002-04-01T12:00:00-01:00\"^^xs:dateTime \"2002-04-02T17:00:00+04:00\"^^xs:dateTime ) )\n"+
    "External( pred:dateTime-greater-than-or-equal( \"2002-04-03T12:00:00-01:00\"^^xs:dateTime \"2002-04-02T17:00:00+04:00\"^^xs:dateTime ) )\n"+
    "External( pred:date-not-equal( \"2004-12-24\"^^xs:date \"2004-12-26\"^^xs:date ) )\n"+
    "External( pred:date-less-than-or-equal( \"2004-12-24\"^^xs:date \"2004-12-26\"^^xs:date ) )\n"+
    "External( pred:date-greater-than-or-equal( \"2004-12-26\"^^xs:date \"2004-12-25\"^^xs:date ) )\n"+
    "External( pred:time-not-equal( \"20:30:00+10:30\"^^xs:time \"06:00:00-05:00\"^^xs:time ) )\n"+
    "External( pred:time-less-than-or-equal( \"20:30:00+10:30\"^^xs:time \"06:00:00-05:00\"^^xs:time ) )\n"+
    "External( pred:time-greater-than-or-equal( \"22:30:00+10:30\"^^xs:time \"06:00:00-05:00\"^^xs:time ) )\n"+
    "External( pred:duration-not-equal( \"P1Y\"^^xs:yearMonthDuration \"P1M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:yearMonthDuration-less-than-or-equal( \"P1Y\"^^xs:yearMonthDuration \"P13M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:yearMonthDuration-greater-than-or-equal( \"P1Y\"^^xs:yearMonthDuration \"P11M\"^^xs:yearMonthDuration ) )\n"+
    "External( pred:dayTimeDuration-less-than-or-equal( \"P1D\"^^xs:dayTimeDuration \"PT25H\"^^xs:dayTimeDuration ) )\n"+
    "External( pred:dayTimeDuration-greater-than-or-equal( \"P1D\"^^xs:dayTimeDuration \"PT23H\"^^xs:dayTimeDuration ) )\n"+
    "External( pred:XMLLiteral-equal( \"<br/>\"^^xs:XMLLiteral \"<br/>\"^^xs:XMLLiteral ) )\n"+
    "External( pred:XMLLiteral-not-equal( \"<br/>\"^^xs:XMLLiteral \"<br><br/>\"^^xs:XMLLiteral ) )\n"+
    "External( func:PlainLiteral-from-string-lang( \"Hello World!\" \"en\" ) ) = \"Hello World!\"@en\n"+
    "External( func:string-from-PlainLiteral( \"Hello World!\"@en ) ) = \"Hello World!\"\n"+
    "External( func:string-from-PlainLiteral( \"Hello World!@en\" ) )= \"Hello World!@en\"\n"+
    "External( func:lang-from-PlainLiteral( \"Hello World!@en\"^^rdf:PlainLiteral ) )= \"en\"^^xs:lang\n"+
    "External( func:lang-from-PlainLiteral( \"Hello World!@en\" ) ) = \"\"\n"+
    "-1 = External ( func:PlainLiteral-compare( \"hallo\"@de \"welt\"@de ) )\n"+
    "1 = External ( func:PlainLiteral-compare( \"welt\"@de \"hallo\"@de ) )\n"+
    "0 = External ( func:PlainLiteral-compare( \"hallo\"@de \"hallo\"@de ) )\n"+
    "External ( pred:matches-language-range( \"Schlagobers ist dasselbe wie Schlagsahne.\"@de-at \"de-*\" ) )\n"+
   ")\n"+
  ")\n"+
 ")\n"+
")\n";
		RIFPSDocument doc = RIFPSDocument.create(rif) ;
		RIFPSParser parser = new RIFPSParser(new StringReader(rif));
		parser.setRIFDocument(doc) ;
		//System.out.println("** Test: " + doc);
		try {
			parser.Document() ;
			System.out.println(doc.getDocumentText());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//RIFXMLDocument doc = RIFXMLDocument.create(new File("C:\\example.xml")) ;
		//doc.compile() ;
		
		//System.out.println(Boolean.toString(true || false || false)) ;
		
		return ;
	}
}
