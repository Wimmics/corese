package fr.inria.corese.kgram.api.core;

public interface ExprType {
    
	public static int UNDEF   = -1;
        public static int UNBOUND = -2;
        // LDScript variable
        public static int LOCAL   = -3;	
        // SPARQL BGP variable
        public static int GLOBAL  = -4;	

	// abstract type
	public static int JOKER = 1;
	public static int ALTER	= 2;
	
	// type
	public static int CONSTANT 	= 6;
	public static int VARIABLE 	= 7;
	public static int BOOLEAN 	= 8;
	public static int TERM 		= 9;
	public static int FUNCTION 	= 10;
	

	
	// boolean
	public static int AND = 11;
	public static int OR = 12;
	public static int NOT = 13;
	
	// function
	public static int BOUND = 14;
	public static int COUNT = 15;
	// ?x in (1, 2)
	public static int INLIST = 16;

	public static int SAMETERM = 17;
	public static int CUSTOM = 18;
	public static int ISNUMERIC = 19;
	public static int MIN = 20;
	public static int MAX = 21;
	public static int AVG = 22;
	public static int SUM = 23;
	public static int ISURI = 24;
	public static int ISBLANK = 25;
	public static int ISLITERAL = 26;
	public static int LANG = 27;
	public static int LANGMATCH = 28;
	public static int REGEX = 29;

	public static int DATATYPE = 30;
	public static int CAST = 31;
	public static int SELF = 32;
	public static int DEBUG = 33;
	public static int EXTERNAL = 34;
	public static int EXTERN = 35;
	public static int KGRAM = 36;
	public static int SQL = 37;
	public static int XPATH = 38;
	public static int SKIP = 39;
	
	public static int LENGTH = 40;
	public static int UNNEST = 41;
	public static int EXIST = 42;
	public static int STRDT = 43;
	public static int STRLANG = 44;
	public static int BNODE = 45;
	public static int COALESCE = 46;
	public static int IF = 47;
	public static int SYSTEM = 48;
	public static int GROUPCONCAT = 49;
	public static int SAMPLE = 50;
	
	public static int STRLEN 	= 51;
	public static int SUBSTR 	= 52;
	public static int UCASE 	= 53;
	public static int LCASE 	= 54;
	public static int ENDS 		= 55;
	public static int STARTS 	= 56;
	public static int CONTAINS 	= 57;
	public static int ENCODE 	= 58;
	public static int CONCAT 	= 59; 
	
	public static int YEAR 		= 60; 
	public static int MONTH 	= 61; 
	public static int DAY 		= 62; 
	public static int HOURS 	= 63;
	public static int MINUTES 	= 64;
	public static int SECONDS 	= 65;
	public static int TIMEZONE 	= 66;
	public static int NOW 		= 67;
	
	public static int ABS 		= 68;
	public static int FLOOR 	= 69;
	public static int ROUND 	= 70;
	public static int CEILING 	= 71;
	public static int RANDOM 	= 72;

	public static int HASH 		= 73;
	public static int URI 		= 74;
	public static int TZ 		= 75;
	public static int STR 		= 76;

	public static int STRBEFORE      = 77;
	public static int STRAFTER 	 = 78;
	public static int STRREPLACE     = 79;
	public static int FUUID 	 = 80;
	public static int STRUUID 	 = 81;
	public static int XSDSTRING 	 = 82;
        public static int APPROXIMATE 	 = 83;
        public static int APP_SIM 	 = 84;
        public static int ISLIST 	 = 85;
        public static int ISUNDEFINED 	 = 86;
        public static int ISWELLFORMED 	 = 87;
        
        public static int TRIPLE 	 = 88;
        public static int SUBJECT 	 = 89;
        public static int PREDICATE 	 = 90;
        public static int OBJECT 	 = 91;
        public static int IS_TRIPLE 	 = 92;
        public static int SPARQL_COMPARE = 93;


	
	// term
	public static int TEQ 	= 101;
	public static int TNEQ 	= 102;
	public static int TLE 	= 103;
	public static int TGE 	= 104;
	public static int TLT 	= 105;
	public static int TGT 	= 106;
        // ==  where type error return false
	public static int EQUAL    = 107;
        // !== where type error return true
	public static int NOT_EQUAL= 108;

	public static int EQNE 	= 109;
	
	public static int EQ 	= 110;
	public static int NE 	= 111;
	public static int NEQ 	= 111;
	
	public static int GL    = 112;
	public static int LE 	= 113;
	public static int GE 	= 114;
	public static int LT 	= 115;
	public static int GT 	= 116;
	
	public static int PLUS 	= 117;
	public static int MINUS = 118;
	public static int MULT 	= 119;
	
	public static int DIV 	= 120;
	
	public static int CONT 	= 121; // ~
	public static int START = 122; // ^
	public static int IN 	= 123; 
	public static int POWER = 124; 
        public static int STAR  = 125;         
        
        
	
        // fake for query
	public static int TINKERPOP = 150; 
	public static int TINKERPOP_RESTRICT = 151; 
	public static int BETWEEN   = 152; 
	public static int MORE      = 153; 
	public static int LESS      = 154; 
	public static int KIND      = 155; 
	public static int BIPREDICATE = 156; 
	public static int EQ_SAME   = 157; 
	
	// extension
	
	public static int DISPLAY 	= 200;
	public static int NUMBER  	= 201;
	public static int SIM 	  	= 202;
	public static int EXTEQUAL	= 203;
	public static int EXTCONT       = 204;
	public static int PROCESS       = 205;
	public static int ENV   	= 206;
	public static int DEPTH 	= 207;
	public static int KG_GRAPH 	= 208;
	public static int NODE 		= 209;
	public static int GET_OBJECT 	= 210;
	public static int SET_OBJECT 	= 211;
	public static int LOAD 		= 212;
	public static int PATHNODE 	= 213;
	public static int GROUPBY 	= 214;
	public static int PSIM 		= 215;
	public static int GETP		= 216;
	public static int SETP 		= 217;
	public static int PWEIGHT 	= 218;
	public static int ANCESTOR 	= 219;
	public static int PROVENANCE 	= 220;
	public static int INDEX 	= 221;
	public static int TIMESTAMP 	= 222;
	public static int ID            = 223;
	public static int TEST          = 224;
	public static int DESCRIBE      = 225;
	public static int STORE         = 226;
		
	

	public static int TURTLE 	= 227;
	public static int LEVEL         = 228;
	public static int INDENT 	= 229;
	public static int PPURI 	= 230;
	public static int URILITERAL 	= 231;
	public static int VISITED 	= 232;
	public static int AGGAND 	= 233;
	public static int PROLOG 	= 234;
	public static int WRITE 	= 235;
	public static int FOCUS_NODE 	= 236;
	public static int XSDLITERAL 	= 237;
	public static int QNAME 	= 238;
        
	public static int STL_DEFAULT 	= 239;
	public static int STL_DEFINE 	= 240;
	public static int STL_NL 	= 241;
	public static int STL_PREFIX 	= 242;
	public static int STL_AGGREGATE = 243;
	public static int STL_CONCAT    = 244;
	public static int STL_GROUPCONCAT=245;
	public static int STL_AND       = 246;
	public static int STL_NUMBER    = 247;
	public static int STL_LOAD      = 248;
	public static int STL_IMPORT    = 249;
	public static int STL_PROCESS 	= 250;
       
        
        public static int APPLY_TEMPLATES           = 251;
	public static int APPLY_TEMPLATES_WITH      = 252;
	public static int APPLY_TEMPLATES_ALL       = 253;
	public static int APPLY_TEMPLATES_WITH_ALL  = 254;
	public static int APPLY_TEMPLATES_GRAPH     = 255;
	public static int APPLY_TEMPLATES_WITH_GRAPH= 256;
        public static int APPLY_TEMPLATES_NOGRAPH   = 257;
	public static int APPLY_TEMPLATES_WITH_NOGRAPH= 258;        
	public static int CALL_TEMPLATE             = 259;
	public static int CALL_TEMPLATE_WITH        = 260;
	public static int STL_TEMPLATE              = 261;
        
 	public static int STL_SET                   = 262;
 	public static int STL_GET                   = 263;
 	public static int STL_BOOLEAN               = 264;
 	public static int STL_VISIT                 = 265;
 	public static int STL_VISITED               = 266;
 	public static int STL_FUTURE                = 267;
 	public static int STL_INDEX                 = 268;
 	public static int STL_VSET                  = 269;
 	public static int STL_VGET                  = 270;
 	public static int STL_PROCESS_URI           = 271;
 	public static int STL_EXPORT                = 272;
 	public static int STL_ERRORS                = 273;
 	public static int STL_ISSTART               = 274;
 	public static int AGGLIST                   = 275;
 	public static int AGGREGATE                 = 276;
 	public static int STL_FORMAT                = 277;
 	public static int STL_VISITED_GRAPH         = 278;
 	public static int STL_CGET                  = 279;
 	public static int STL_CSET                  = 280;
 	public static int STL_HASGET                = 281;
 	public static int STL_STRIP                 = 282;
        public static int FORMAT                    = 283;
        public static int STL_ERROR_MAP             = 284;
        public static int STL_DEFINED               = 285;

              
	public static int ISSKOLEM 	= 300;
	public static int SKOLEM 	= 301;

	public static int QUERY 	= 302;
	public static int EXTENSION 	= 303;
	public static int EVEN          = 304;
	public static int ODD           = 305;
	public static int READ          = 306;
        public static int PACKAGE       = 307;
        
        public static int IOTA          = 308;
        public static int LIST          = 309;
        public static int MAP           = 310;
        public static int MAPLIST       = 311;
        public static int APPLY         = 312;
	public static int LET           = 313;
	public static int LAMBDA        = 314;
	public static int ERROR         = 315;
	public static int MAPEVERY      = 316;
	public static int MAPANY        = 317;
 	public static int MAPFIND       = 318;
	public static int MAPFINDLIST   = 319;
	public static int MAPMERGE      = 320;
	public static int MAPFUN        = 321;
	public static int SET           = 322;
	public static int SEQUENCE      = 323;
	public static int RETURN        = 324;
	public static int EVAL          = 325;
	public static int FUNCALL       = 326;
	public static int FOR           = 327;
	public static int MAPAPPEND     = 328;
	public static int REDUCE        = 329;
        
	public static int XT_SORT       = 330;
	public static int JAVACALL      = 331;
	public static int DSCALL        = 332;
	public static int JAVACAST      = 333;
        public static int ISEXTENSION   = 334;
        public static int SAFE          = 335;
        public static int STATIC        = 336;
        public static int TRY_CATCH     = 337;
        public static int THROW         = 338;
        public static int RESUME        = 339;
        public static int UNSET         = 340;
        public static int STATIC_UNSET  = 341;
        
        public static int XT_MAPPING     = 400;
        public static int XT_ADD         = 401;
        public static int XT_CONCAT      = 402;
        public static int XT_COUNT       = 403;
        public static int XT_CONS        = 404;
        public static int XT_FIRST       = 405;
        public static int XT_REST        = 406;
        public static int XT_GET         = 407;        
        public static int XT_SET         = 408;
	public static int XT_REVERSE     = 409;       
	public static int XT_APPEND      = 410;       
        
        public static int XT_SUBJECT     = 411;
        public static int XT_OBJECT      = 412;
        public static int XT_PROPERTY    = 413;
        public static int XT_VALUE       = 414;
        public static int XT_INDEX       = 415;
        public static int XT_GRAPH       = 416;
        public static int XT_REJECT      = 417;
        public static int XT_VARIABLES   = 418;
        public static int XT_VALUES      = 419;
        public static int XT_EDGES       = 420;
        public static int XT_TRIPLE      = 421;
        public static int XT_GEN_GET     = 422;
        public static int XT_DISPLAY     = 423;
        public static int XT_PRINT       = 424;
        
        public static int XT_UNION       = 425;
        public static int XT_MINUS       = 426;
        public static int XT_OPTIONAL    = 427;
        public static int XT_JOIN        = 428;
        public static int XT_QUERY       = 429;
        public static int XT_AST         = 430;
        public static int XT_CONTEXT     = 431;
        public static int XT_METADATA    = 432;
        public static int XT_FROM        = 433;
        public static int XT_NAMED       = 434;
        public static int XT_MEMBER      = 435;
        public static int XT_MERGE       = 436;
        public static int XT_TOLIST      = 437;
        public static int XT_TUNE        = 438;
        public static int XT_FOCUS       = 439;
        public static int XT_CONTENT     = 440;
        public static int XT_ENTAILMENT  = 441;
        public static int XT_DATATYPE    = 442;
        public static int XT_KIND        = 443;
        public static int XT_METHOD      = 444;
        public static int XT_METHOD_TYPE = 445;
        public static int XT_ITERATE     = 446;
        public static int XT_SWAP        = 447;
        public static int XT_TRACE       = 448;
        public static int XT_PRETTY      = 449;
        public static int XT_EXISTS      = 450;
        public static int XT_REMOVE_INDEX= 451;
        public static int XT_REMOVE      = 452;        
        public static int XT_NAME        = 453;
        public static int XT_GEN_REST    = 454;
        public static int XT_LAST        = 455;
        public static int XT_MAP         = 456;
        public static int XT_RESULT      = 457;
        public static int XT_COMPARE     = 458;
        public static int XT_VISITOR     = 459;
        public static int XT_REPLACE     = 460;
        public static int XT_LOWERCASE   = 461;
        public static int XT_UPPERCASE   = 462;
        public static int XT_XML         = 463;
        public static int XT_JSON        = 464;
        public static int XT_SPIN        = 465;
        public static int XT_RDF         = 466;
        public static int XT_SHAPE_GRAPH = 467;
        public static int XT_SHAPE_NODE  = 468;
        public static int XT_TOGRAPH     = 469;
        public static int XT_EXPAND      = 470;
        public static int XT_NODE        = 471;
        public static int XT_VERTEX      = 472;
        public static int XT_JSON_OBJECT = 473;
        public static int XT_HAS         = 474;
        public static int XT_DEFINE      = 475;
        public static int XT_DEGREE      = 476;
        public static int XT_MINDEGREE   = 477;
        public static int XT_INSERT      = 478;
        public static int XT_DELETE      = 479;
        public static int XT_VALID_URI   = 480;
        public static int XT_STACK       = 481;
        public static int XT_DATATYPE_VALUE= 482;
        public static int XT_CAST        = 483;
        public static int XT_ISFUNCTION  = 484;
        public static int XT_EVENT       = 485;
        public static int XT_SUBJECTS    = 486;
        public static int XT_OBJECTS     = 487;
        public static int XT_SYNTAX      = 488;
        public static int XT_HTTP_GET    = 489;
        public static int XT_GET_DATATYPE_VALUE    = 490;
        public static int XT_CREATE      = 491;
        public static int XT_DOMAIN      = 492;
        public static int XT_SPLIT       = 493;
        public static int XT_PATH        = 494;
        public static int XT_MAPPINGS    = 495;
        public static int XT_PARSE_MAPPINGS= 496;
        public static int XT_LOAD_MAPPINGS= 497;
        public static int XT_DISTANCE    = 498;
        
        
             
        public static int SLICE       = 500;
        public static int EDGE_LEVEL  = 501;
        public static int DB          = 502;
        public static int EDGE_ACCESS = 503;
        public static int EDGE_NESTED = 504;
        public static int XT_ASSERTED = 505;
        public static int XT_EDGE     = 506;
        public static int XT_REFERENCE= 507;
        public static int XT_LABEL    = 508;
        
        
        // DOM XML
        public static int XT_NODE_PROPERTY   = 600;
        public static int XT_NODE_TYPE  = 601;
        public static int XT_ATTRIBUTES = 602;
        public static int XT_ELEMENTS   = 603;
        public static int XT_CHILDREN   = 604;
        public static int XT_NODE_NAME   = 605;
        public static int XT_NODE_VALUE  = 606;
        public static int XT_TEXT_CONTENT= 607;
        public static int XT_NODE_PARENT = 608;
        public static int XT_NODE_DOCUMENT= 609;
        public static int XT_NODE_ELEMENT= 610;
        public static int XT_NAMESPACE   = 611;
        public static int XT_BASE        = 612;
        public static int XT_ATTRIBUTE   = 613;
        public static int XT_HAS_ATTRIBUTE=614;
        public static int XT_NODE_LOCAL_NAME=615;
        public static int XT_NODE_FIRST_CHILD=616;
        
        public static int XT_XSLT        = 650;
        

}
