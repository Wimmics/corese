CodeMirror.defineMode("sparql10", function(config, parserConfig) {

	var indentUnit = config.indentUnit;

	// ll1_table is auto-generated from grammar
	// - do not edit manually
	// %%%table%%%
var ll1_table=
{
  "*[ (,), expression]" : {
     ",": ["[ (,), expression]","*[ (,), expression]"], 
     ")": []}, 
  "*[ (,), object]" : {
     ",": ["[ (,), object]","*[ (,), object]"], 
     ".": [], 
     ";": [], 
     "]": [], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": []}, 
  "*[ (;), ?[verb, objectList]]" : {
     ";": ["[ (;), ?[verb, objectList]]","*[ (;), ?[verb, objectList]]"], 
     ".": [], 
     "]": [], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": []}, 
  "*[&&, valueLogical]" : {
     "&&": ["[&&, valueLogical]","*[&&, valueLogical]"], 
     ")": [], 
     ",": [], 
     "||": []}, 
  "*[UNION, groupGraphPattern]" : {
     "UNION": ["[UNION, groupGraphPattern]","*[UNION, groupGraphPattern]"], 
     "VAR1": [], 
     "VAR2": [], 
     "NIL": [], 
     "(": [], 
     "[": [], 
     "IRI_REF": [], 
     "TRUE": [], 
     "FALSE": [], 
     "BLANK_NODE_LABEL": [], 
     "ANON": [], 
     "PNAME_LN": [], 
     "PNAME_NS": [], 
     "STRING_LITERAL1": [], 
     "STRING_LITERAL2": [], 
     "STRING_LITERAL_LONG1": [], 
     "STRING_LITERAL_LONG2": [], 
     "INTEGER": [], 
     "DECIMAL": [], 
     "DOUBLE": [], 
     "INTEGER_POSITIVE": [], 
     "DECIMAL_POSITIVE": [], 
     "DOUBLE_POSITIVE": [], 
     "INTEGER_NEGATIVE": [], 
     "DECIMAL_NEGATIVE": [], 
     "DOUBLE_NEGATIVE": [], 
     ".": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": [], 
     "}": []}, 
  "*[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]" : {
     "FILTER": ["[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]","*[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]"], 
     "OPTIONAL": ["[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]","*[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]"], 
     "{": ["[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]","*[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]"], 
     "GRAPH": ["[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]","*[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]"], 
     "}": []}, 
  "*[||, conditionalAndExpression]" : {
     "||": ["[||, conditionalAndExpression]","*[||, conditionalAndExpression]"], 
     ")": [], 
     ",": []}, 
  "*datasetClause" : {
     "FROM": ["datasetClause","*datasetClause"], 
     "WHERE": [], 
     "{": []}, 
  "*describeDatasetClause" : {
     "FROM": ["describeDatasetClause","*describeDatasetClause"], 
     "ORDER": [], 
     "LIMIT": [], 
     "OFFSET": [], 
     "WHERE": [], 
     "{": [], 
     "$": []}, 
  "*graphNode" : {
     "(": ["graphNode","*graphNode"], 
     "[": ["graphNode","*graphNode"], 
     "VAR1": ["graphNode","*graphNode"], 
     "VAR2": ["graphNode","*graphNode"], 
     "NIL": ["graphNode","*graphNode"], 
     "IRI_REF": ["graphNode","*graphNode"], 
     "TRUE": ["graphNode","*graphNode"], 
     "FALSE": ["graphNode","*graphNode"], 
     "BLANK_NODE_LABEL": ["graphNode","*graphNode"], 
     "ANON": ["graphNode","*graphNode"], 
     "PNAME_LN": ["graphNode","*graphNode"], 
     "PNAME_NS": ["graphNode","*graphNode"], 
     "STRING_LITERAL1": ["graphNode","*graphNode"], 
     "STRING_LITERAL2": ["graphNode","*graphNode"], 
     "STRING_LITERAL_LONG1": ["graphNode","*graphNode"], 
     "STRING_LITERAL_LONG2": ["graphNode","*graphNode"], 
     "INTEGER": ["graphNode","*graphNode"], 
     "DECIMAL": ["graphNode","*graphNode"], 
     "DOUBLE": ["graphNode","*graphNode"], 
     "INTEGER_POSITIVE": ["graphNode","*graphNode"], 
     "DECIMAL_POSITIVE": ["graphNode","*graphNode"], 
     "DOUBLE_POSITIVE": ["graphNode","*graphNode"], 
     "INTEGER_NEGATIVE": ["graphNode","*graphNode"], 
     "DECIMAL_NEGATIVE": ["graphNode","*graphNode"], 
     "DOUBLE_NEGATIVE": ["graphNode","*graphNode"], 
     ")": []}, 
  "*or([[*, unaryExpression], [/, unaryExpression]])" : {
     "*": ["or([[*, unaryExpression], [/, unaryExpression]])","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "/": ["or([[*, unaryExpression], [/, unaryExpression]])","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     ")": [], 
     ",": [], 
     "||": [], 
     "&&": [], 
     "=": [], 
     "!=": [], 
     "<": [], 
     ">": [], 
     "<=": [], 
     ">=": [], 
     "+": [], 
     "-": [], 
     "INTEGER_POSITIVE": [], 
     "DECIMAL_POSITIVE": [], 
     "DOUBLE_POSITIVE": [], 
     "INTEGER_NEGATIVE": [], 
     "DECIMAL_NEGATIVE": [], 
     "DOUBLE_NEGATIVE": []}, 
  "*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])" : {
     "+": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "-": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "INTEGER_POSITIVE": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DECIMAL_POSITIVE": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DOUBLE_POSITIVE": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "INTEGER_NEGATIVE": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DECIMAL_NEGATIVE": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DOUBLE_NEGATIVE": ["or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     ")": [], 
     ",": [], 
     "||": [], 
     "&&": [], 
     "=": [], 
     "!=": [], 
     "<": [], 
     ">": [], 
     "<=": [], 
     ">=": []}, 
  "*orderCondition" : {
     "ASC": ["orderCondition","*orderCondition"], 
     "DESC": ["orderCondition","*orderCondition"], 
     "VAR1": ["orderCondition","*orderCondition"], 
     "VAR2": ["orderCondition","*orderCondition"], 
     "(": ["orderCondition","*orderCondition"], 
     "STR": ["orderCondition","*orderCondition"], 
     "LANG": ["orderCondition","*orderCondition"], 
     "LANGMATCHES": ["orderCondition","*orderCondition"], 
     "DATATYPE": ["orderCondition","*orderCondition"], 
     "BOUND": ["orderCondition","*orderCondition"], 
     "SAMETERM": ["orderCondition","*orderCondition"], 
     "ISIRI": ["orderCondition","*orderCondition"], 
     "ISURI": ["orderCondition","*orderCondition"], 
     "ISBLANK": ["orderCondition","*orderCondition"], 
     "ISLITERAL": ["orderCondition","*orderCondition"], 
     "REGEX": ["orderCondition","*orderCondition"], 
     "IRI_REF": ["orderCondition","*orderCondition"], 
     "PNAME_LN": ["orderCondition","*orderCondition"], 
     "PNAME_NS": ["orderCondition","*orderCondition"], 
     "LIMIT": [], 
     "OFFSET": [], 
     "$": []}, 
  "*prefixDecl" : {
     "PREFIX": ["prefixDecl","*prefixDecl"], 
     "SELECT": [], 
     "CONSTRUCT": [], 
     "DESCRIBE": [], 
     "ASK": []}, 
  "*var" : {
     "VAR1": ["var","*var"], 
     "VAR2": ["var","*var"], 
     "WHERE": [], 
     "{": [], 
     "FROM": []}, 
  "*varOrIRIref" : {
     "VAR1": ["varOrIRIref","*varOrIRIref"], 
     "VAR2": ["varOrIRIref","*varOrIRIref"], 
     "IRI_REF": ["varOrIRIref","*varOrIRIref"], 
     "PNAME_LN": ["varOrIRIref","*varOrIRIref"], 
     "PNAME_NS": ["varOrIRIref","*varOrIRIref"], 
     "ORDER": [], 
     "LIMIT": [], 
     "OFFSET": [], 
     "WHERE": [], 
     "{": [], 
     "FROM": [], 
     "$": []}, 
  "+graphNode" : {
     "(": ["graphNode","*graphNode"], 
     "[": ["graphNode","*graphNode"], 
     "VAR1": ["graphNode","*graphNode"], 
     "VAR2": ["graphNode","*graphNode"], 
     "NIL": ["graphNode","*graphNode"], 
     "IRI_REF": ["graphNode","*graphNode"], 
     "TRUE": ["graphNode","*graphNode"], 
     "FALSE": ["graphNode","*graphNode"], 
     "BLANK_NODE_LABEL": ["graphNode","*graphNode"], 
     "ANON": ["graphNode","*graphNode"], 
     "PNAME_LN": ["graphNode","*graphNode"], 
     "PNAME_NS": ["graphNode","*graphNode"], 
     "STRING_LITERAL1": ["graphNode","*graphNode"], 
     "STRING_LITERAL2": ["graphNode","*graphNode"], 
     "STRING_LITERAL_LONG1": ["graphNode","*graphNode"], 
     "STRING_LITERAL_LONG2": ["graphNode","*graphNode"], 
     "INTEGER": ["graphNode","*graphNode"], 
     "DECIMAL": ["graphNode","*graphNode"], 
     "DOUBLE": ["graphNode","*graphNode"], 
     "INTEGER_POSITIVE": ["graphNode","*graphNode"], 
     "DECIMAL_POSITIVE": ["graphNode","*graphNode"], 
     "DOUBLE_POSITIVE": ["graphNode","*graphNode"], 
     "INTEGER_NEGATIVE": ["graphNode","*graphNode"], 
     "DECIMAL_NEGATIVE": ["graphNode","*graphNode"], 
     "DOUBLE_NEGATIVE": ["graphNode","*graphNode"]}, 
  "+orderCondition" : {
     "ASC": ["orderCondition","*orderCondition"], 
     "DESC": ["orderCondition","*orderCondition"], 
     "VAR1": ["orderCondition","*orderCondition"], 
     "VAR2": ["orderCondition","*orderCondition"], 
     "(": ["orderCondition","*orderCondition"], 
     "STR": ["orderCondition","*orderCondition"], 
     "LANG": ["orderCondition","*orderCondition"], 
     "LANGMATCHES": ["orderCondition","*orderCondition"], 
     "DATATYPE": ["orderCondition","*orderCondition"], 
     "BOUND": ["orderCondition","*orderCondition"], 
     "SAMETERM": ["orderCondition","*orderCondition"], 
     "ISIRI": ["orderCondition","*orderCondition"], 
     "ISURI": ["orderCondition","*orderCondition"], 
     "ISBLANK": ["orderCondition","*orderCondition"], 
     "ISLITERAL": ["orderCondition","*orderCondition"], 
     "REGEX": ["orderCondition","*orderCondition"], 
     "IRI_REF": ["orderCondition","*orderCondition"], 
     "PNAME_LN": ["orderCondition","*orderCondition"], 
     "PNAME_NS": ["orderCondition","*orderCondition"]}, 
  "+var" : {
     "VAR1": ["var","*var"], 
     "VAR2": ["var","*var"]}, 
  "+varOrIRIref" : {
     "VAR1": ["varOrIRIref","*varOrIRIref"], 
     "VAR2": ["varOrIRIref","*varOrIRIref"], 
     "IRI_REF": ["varOrIRIref","*varOrIRIref"], 
     "PNAME_LN": ["varOrIRIref","*varOrIRIref"], 
     "PNAME_NS": ["varOrIRIref","*varOrIRIref"]}, 
  "?." : {
     ".": ["."], 
     "VAR1": [], 
     "VAR2": [], 
     "NIL": [], 
     "(": [], 
     "[": [], 
     "IRI_REF": [], 
     "TRUE": [], 
     "FALSE": [], 
     "BLANK_NODE_LABEL": [], 
     "ANON": [], 
     "PNAME_LN": [], 
     "PNAME_NS": [], 
     "STRING_LITERAL1": [], 
     "STRING_LITERAL2": [], 
     "STRING_LITERAL_LONG1": [], 
     "STRING_LITERAL_LONG2": [], 
     "INTEGER": [], 
     "DECIMAL": [], 
     "DOUBLE": [], 
     "INTEGER_POSITIVE": [], 
     "DECIMAL_POSITIVE": [], 
     "DOUBLE_POSITIVE": [], 
     "INTEGER_NEGATIVE": [], 
     "DECIMAL_NEGATIVE": [], 
     "DOUBLE_NEGATIVE": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": [], 
     "}": []}, 
  "?WHERE" : {
     "WHERE": ["WHERE"], 
     "{": []}, 
  "?[ (,), expression]" : {
     ",": ["[ (,), expression]"], 
     ")": []}, 
  "?[., ?constructTriples]" : {
     ".": ["[., ?constructTriples]"], 
     "}": []}, 
  "?[., ?triplesBlock]" : {
     ".": ["[., ?triplesBlock]"], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": []}, 
  "?[verb, objectList]" : {
     "a": ["[verb, objectList]"], 
     "VAR1": ["[verb, objectList]"], 
     "VAR2": ["[verb, objectList]"], 
     "IRI_REF": ["[verb, objectList]"], 
     "PNAME_LN": ["[verb, objectList]"], 
     "PNAME_NS": ["[verb, objectList]"], 
     ";": [], 
     ".": [], 
     "]": [], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": []}, 
  "?argList" : {
     "NIL": ["argList"], 
     "(": ["argList"], 
     ")": [], 
     ",": [], 
     "||": [], 
     "&&": [], 
     "=": [], 
     "!=": [], 
     "<": [], 
     ">": [], 
     "<=": [], 
     ">=": [], 
     "+": [], 
     "-": [], 
     "INTEGER_POSITIVE": [], 
     "DECIMAL_POSITIVE": [], 
     "DOUBLE_POSITIVE": [], 
     "INTEGER_NEGATIVE": [], 
     "DECIMAL_NEGATIVE": [], 
     "DOUBLE_NEGATIVE": [], 
     "*": [], 
     "/": []}, 
  "?baseDecl" : {
     "BASE": ["baseDecl"], 
     "SELECT": [], 
     "CONSTRUCT": [], 
     "DESCRIBE": [], 
     "ASK": [], 
     "PREFIX": []}, 
  "?constructTriples" : {
     "VAR1": ["constructTriples"], 
     "VAR2": ["constructTriples"], 
     "NIL": ["constructTriples"], 
     "(": ["constructTriples"], 
     "[": ["constructTriples"], 
     "IRI_REF": ["constructTriples"], 
     "TRUE": ["constructTriples"], 
     "FALSE": ["constructTriples"], 
     "BLANK_NODE_LABEL": ["constructTriples"], 
     "ANON": ["constructTriples"], 
     "PNAME_LN": ["constructTriples"], 
     "PNAME_NS": ["constructTriples"], 
     "STRING_LITERAL1": ["constructTriples"], 
     "STRING_LITERAL2": ["constructTriples"], 
     "STRING_LITERAL_LONG1": ["constructTriples"], 
     "STRING_LITERAL_LONG2": ["constructTriples"], 
     "INTEGER": ["constructTriples"], 
     "DECIMAL": ["constructTriples"], 
     "DOUBLE": ["constructTriples"], 
     "INTEGER_POSITIVE": ["constructTriples"], 
     "DECIMAL_POSITIVE": ["constructTriples"], 
     "DOUBLE_POSITIVE": ["constructTriples"], 
     "INTEGER_NEGATIVE": ["constructTriples"], 
     "DECIMAL_NEGATIVE": ["constructTriples"], 
     "DOUBLE_NEGATIVE": ["constructTriples"], 
     "}": []}, 
  "?limitClause" : {
     "LIMIT": ["limitClause"], 
     "$": []}, 
  "?limitOffsetClauses" : {
     "LIMIT": ["limitOffsetClauses"], 
     "OFFSET": ["limitOffsetClauses"], 
     "$": []}, 
  "?offsetClause" : {
     "OFFSET": ["offsetClause"], 
     "$": []}, 
  "?or([DISTINCT, REDUCED])" : {
     "DISTINCT": ["or([DISTINCT, REDUCED])"], 
     "REDUCED": ["or([DISTINCT, REDUCED])"], 
     "*": [], 
     "VAR1": [], 
     "VAR2": []}, 
  "?or([LANGTAG, [^^, iriRef]])" : {
     "LANGTAG": ["or([LANGTAG, [^^, iriRef]])"], 
     "^^": ["or([LANGTAG, [^^, iriRef]])"], 
     "a": [], 
     "VAR1": [], 
     "VAR2": [], 
     "IRI_REF": [], 
     "PNAME_LN": [], 
     "PNAME_NS": [], 
     ".": [], 
     ";": [], 
     ",": [], 
     "(": [], 
     "[": [], 
     "NIL": [], 
     "TRUE": [], 
     "FALSE": [], 
     "BLANK_NODE_LABEL": [], 
     "ANON": [], 
     "STRING_LITERAL1": [], 
     "STRING_LITERAL2": [], 
     "STRING_LITERAL_LONG1": [], 
     "STRING_LITERAL_LONG2": [], 
     "INTEGER": [], 
     "DECIMAL": [], 
     "DOUBLE": [], 
     "INTEGER_POSITIVE": [], 
     "DECIMAL_POSITIVE": [], 
     "DOUBLE_POSITIVE": [], 
     "INTEGER_NEGATIVE": [], 
     "DECIMAL_NEGATIVE": [], 
     "DOUBLE_NEGATIVE": [], 
     ")": [], 
     "]": [], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": [], 
     "||": [], 
     "&&": [], 
     "=": [], 
     "!=": [], 
     "<": [], 
     ">": [], 
     "<=": [], 
     ">=": [], 
     "+": [], 
     "-": [], 
     "*": [], 
     "/": []}, 
  "?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])" : {
     "=": ["or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "!=": ["or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "<": ["or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     ">": ["or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "<=": ["or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     ">=": ["or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     ")": [], 
     ",": [], 
     "||": [], 
     "&&": []}, 
  "?orderClause" : {
     "ORDER": ["orderClause"], 
     "LIMIT": [], 
     "OFFSET": [], 
     "$": []}, 
  "?propertyListNotEmpty" : {
     "a": ["propertyListNotEmpty"], 
     "VAR1": ["propertyListNotEmpty"], 
     "VAR2": ["propertyListNotEmpty"], 
     "IRI_REF": ["propertyListNotEmpty"], 
     "PNAME_LN": ["propertyListNotEmpty"], 
     "PNAME_NS": ["propertyListNotEmpty"], 
     ".": [], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": []}, 
  "?triplesBlock" : {
     "VAR1": ["triplesBlock"], 
     "VAR2": ["triplesBlock"], 
     "NIL": ["triplesBlock"], 
     "(": ["triplesBlock"], 
     "[": ["triplesBlock"], 
     "IRI_REF": ["triplesBlock"], 
     "TRUE": ["triplesBlock"], 
     "FALSE": ["triplesBlock"], 
     "BLANK_NODE_LABEL": ["triplesBlock"], 
     "ANON": ["triplesBlock"], 
     "PNAME_LN": ["triplesBlock"], 
     "PNAME_NS": ["triplesBlock"], 
     "STRING_LITERAL1": ["triplesBlock"], 
     "STRING_LITERAL2": ["triplesBlock"], 
     "STRING_LITERAL_LONG1": ["triplesBlock"], 
     "STRING_LITERAL_LONG2": ["triplesBlock"], 
     "INTEGER": ["triplesBlock"], 
     "DECIMAL": ["triplesBlock"], 
     "DOUBLE": ["triplesBlock"], 
     "INTEGER_POSITIVE": ["triplesBlock"], 
     "DECIMAL_POSITIVE": ["triplesBlock"], 
     "DOUBLE_POSITIVE": ["triplesBlock"], 
     "INTEGER_NEGATIVE": ["triplesBlock"], 
     "DECIMAL_NEGATIVE": ["triplesBlock"], 
     "DOUBLE_NEGATIVE": ["triplesBlock"], 
     "}": [], 
     "FILTER": [], 
     "OPTIONAL": [], 
     "{": [], 
     "GRAPH": []}, 
  "?whereClause" : {
     "WHERE": ["whereClause"], 
     "{": ["whereClause"], 
     "ORDER": [], 
     "LIMIT": [], 
     "OFFSET": [], 
     "$": []}, 
  "[ (,), expression]" : {
     ",": [",","expression"]}, 
  "[ (,), object]" : {
     ",": [",","object"]}, 
  "[ (;), ?[verb, objectList]]" : {
     ";": [";","?[verb, objectList]"]}, 
  "[!, primaryExpression]" : {
     "!": ["!","primaryExpression"]}, 
  "[!=, numericExpression]" : {
     "!=": ["!=","numericExpression"]}, 
  "[&&, valueLogical]" : {
     "&&": ["&&","valueLogical"]}, 
  "[*, unaryExpression]" : {
     "*": ["*","unaryExpression"]}, 
  "[+, multiplicativeExpression]" : {
     "+": ["+","multiplicativeExpression"]}, 
  "[+, primaryExpression]" : {
     "+": ["+","primaryExpression"]}, 
  "[-, multiplicativeExpression]" : {
     "-": ["-","multiplicativeExpression"]}, 
  "[-, primaryExpression]" : {
     "-": ["-","primaryExpression"]}, 
  "[., ?constructTriples]" : {
     ".": [".","?constructTriples"]}, 
  "[., ?triplesBlock]" : {
     ".": [".","?triplesBlock"]}, 
  "[/, unaryExpression]" : {
     "/": ["/","unaryExpression"]}, 
  "[<, numericExpression]" : {
     "<": ["<","numericExpression"]}, 
  "[<=, numericExpression]" : {
     "<=": ["<=","numericExpression"]}, 
  "[=, numericExpression]" : {
     "=": ["=","numericExpression"]}, 
  "[>, numericExpression]" : {
     ">": [">","numericExpression"]}, 
  "[>=, numericExpression]" : {
     ">=": [">=","numericExpression"]}, 
  "[UNION, groupGraphPattern]" : {
     "UNION": ["UNION","groupGraphPattern"]}, 
  "[^^, iriRef]" : {
     "^^": ["^^","iriRef"]}, 
  "[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]" : {
     "FILTER": ["or([graphPatternNotTriples, filter])","?.","?triplesBlock"], 
     "OPTIONAL": ["or([graphPatternNotTriples, filter])","?.","?triplesBlock"], 
     "{": ["or([graphPatternNotTriples, filter])","?.","?triplesBlock"], 
     "GRAPH": ["or([graphPatternNotTriples, filter])","?.","?triplesBlock"]}, 
  "[verb, objectList]" : {
     "a": ["verb","objectList"], 
     "VAR1": ["verb","objectList"], 
     "VAR2": ["verb","objectList"], 
     "IRI_REF": ["verb","objectList"], 
     "PNAME_LN": ["verb","objectList"], 
     "PNAME_NS": ["verb","objectList"]}, 
  "[||, conditionalAndExpression]" : {
     "||": ["||","conditionalAndExpression"]}, 
  "additiveExpression" : {
     "!": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "+": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "-": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "VAR1": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "VAR2": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "(": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "STR": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "LANG": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "LANGMATCHES": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DATATYPE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "BOUND": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "SAMETERM": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "ISIRI": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "ISURI": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "ISBLANK": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "ISLITERAL": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "TRUE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "FALSE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "REGEX": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "IRI_REF": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "STRING_LITERAL1": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "STRING_LITERAL2": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "STRING_LITERAL_LONG1": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "STRING_LITERAL_LONG2": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "INTEGER": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DECIMAL": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DOUBLE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "INTEGER_POSITIVE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DECIMAL_POSITIVE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DOUBLE_POSITIVE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "INTEGER_NEGATIVE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DECIMAL_NEGATIVE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "DOUBLE_NEGATIVE": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "PNAME_LN": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"], 
     "PNAME_NS": ["multiplicativeExpression","*or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])"]}, 
  "argList" : {
     "NIL": ["NIL"], 
     "(": ["(","expression","*[ (,), expression]",")"]}, 
  "askQuery" : {
     "ASK": ["ASK","*datasetClause","whereClause","solutionModifier"]}, 
  "baseDecl" : {
     "BASE": ["BASE","IRI_REF"]}, 
  "blankNode" : {
     "BLANK_NODE_LABEL": ["BLANK_NODE_LABEL"], 
     "ANON": ["ANON"]}, 
  "blankNodePropertyList" : {
     "[": ["[","propertyListNotEmpty","]"]}, 
  "booleanLiteral" : {
     "TRUE": ["TRUE"], 
     "FALSE": ["FALSE"]}, 
  "brackettedExpression" : {
     "(": ["(","expression",")"]}, 
  "builtInCall" : {
     "STR": ["STR","(","expression",")"], 
     "LANG": ["LANG","(","expression",")"], 
     "LANGMATCHES": ["LANGMATCHES","(","expression",",","expression",")"], 
     "DATATYPE": ["DATATYPE","(","expression",")"], 
     "BOUND": ["BOUND","(","var",")"], 
     "SAMETERM": ["SAMETERM","(","expression",",","expression",")"], 
     "ISIRI": ["ISIRI","(","expression",")"], 
     "ISURI": ["ISURI","(","expression",")"], 
     "ISBLANK": ["ISBLANK","(","expression",")"], 
     "ISLITERAL": ["ISLITERAL","(","expression",")"], 
     "REGEX": ["regexExpression"]}, 
  "collection" : {
     "(": ["(","+graphNode",")"]}, 
  "conditionalAndExpression" : {
     "!": ["valueLogical","*[&&, valueLogical]"], 
     "+": ["valueLogical","*[&&, valueLogical]"], 
     "-": ["valueLogical","*[&&, valueLogical]"], 
     "VAR1": ["valueLogical","*[&&, valueLogical]"], 
     "VAR2": ["valueLogical","*[&&, valueLogical]"], 
     "(": ["valueLogical","*[&&, valueLogical]"], 
     "STR": ["valueLogical","*[&&, valueLogical]"], 
     "LANG": ["valueLogical","*[&&, valueLogical]"], 
     "LANGMATCHES": ["valueLogical","*[&&, valueLogical]"], 
     "DATATYPE": ["valueLogical","*[&&, valueLogical]"], 
     "BOUND": ["valueLogical","*[&&, valueLogical]"], 
     "SAMETERM": ["valueLogical","*[&&, valueLogical]"], 
     "ISIRI": ["valueLogical","*[&&, valueLogical]"], 
     "ISURI": ["valueLogical","*[&&, valueLogical]"], 
     "ISBLANK": ["valueLogical","*[&&, valueLogical]"], 
     "ISLITERAL": ["valueLogical","*[&&, valueLogical]"], 
     "TRUE": ["valueLogical","*[&&, valueLogical]"], 
     "FALSE": ["valueLogical","*[&&, valueLogical]"], 
     "REGEX": ["valueLogical","*[&&, valueLogical]"], 
     "IRI_REF": ["valueLogical","*[&&, valueLogical]"], 
     "STRING_LITERAL1": ["valueLogical","*[&&, valueLogical]"], 
     "STRING_LITERAL2": ["valueLogical","*[&&, valueLogical]"], 
     "STRING_LITERAL_LONG1": ["valueLogical","*[&&, valueLogical]"], 
     "STRING_LITERAL_LONG2": ["valueLogical","*[&&, valueLogical]"], 
     "INTEGER": ["valueLogical","*[&&, valueLogical]"], 
     "DECIMAL": ["valueLogical","*[&&, valueLogical]"], 
     "DOUBLE": ["valueLogical","*[&&, valueLogical]"], 
     "INTEGER_POSITIVE": ["valueLogical","*[&&, valueLogical]"], 
     "DECIMAL_POSITIVE": ["valueLogical","*[&&, valueLogical]"], 
     "DOUBLE_POSITIVE": ["valueLogical","*[&&, valueLogical]"], 
     "INTEGER_NEGATIVE": ["valueLogical","*[&&, valueLogical]"], 
     "DECIMAL_NEGATIVE": ["valueLogical","*[&&, valueLogical]"], 
     "DOUBLE_NEGATIVE": ["valueLogical","*[&&, valueLogical]"], 
     "PNAME_LN": ["valueLogical","*[&&, valueLogical]"], 
     "PNAME_NS": ["valueLogical","*[&&, valueLogical]"]}, 
  "conditionalOrExpression" : {
     "!": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "+": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "-": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "VAR1": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "VAR2": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "(": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "STR": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "LANG": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "LANGMATCHES": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DATATYPE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "BOUND": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "SAMETERM": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "ISIRI": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "ISURI": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "ISBLANK": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "ISLITERAL": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "TRUE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "FALSE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "REGEX": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "IRI_REF": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "STRING_LITERAL1": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "STRING_LITERAL2": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "STRING_LITERAL_LONG1": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "STRING_LITERAL_LONG2": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "INTEGER": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DECIMAL": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DOUBLE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "INTEGER_POSITIVE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DECIMAL_POSITIVE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DOUBLE_POSITIVE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "INTEGER_NEGATIVE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DECIMAL_NEGATIVE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "DOUBLE_NEGATIVE": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "PNAME_LN": ["conditionalAndExpression","*[||, conditionalAndExpression]"], 
     "PNAME_NS": ["conditionalAndExpression","*[||, conditionalAndExpression]"]}, 
  "constraint" : {
     "(": ["brackettedExpression"], 
     "STR": ["builtInCall"], 
     "LANG": ["builtInCall"], 
     "LANGMATCHES": ["builtInCall"], 
     "DATATYPE": ["builtInCall"], 
     "BOUND": ["builtInCall"], 
     "SAMETERM": ["builtInCall"], 
     "ISIRI": ["builtInCall"], 
     "ISURI": ["builtInCall"], 
     "ISBLANK": ["builtInCall"], 
     "ISLITERAL": ["builtInCall"], 
     "REGEX": ["builtInCall"], 
     "IRI_REF": ["functionCall"], 
     "PNAME_LN": ["functionCall"], 
     "PNAME_NS": ["functionCall"]}, 
  "constructQuery" : {
     "CONSTRUCT": ["CONSTRUCT","constructTemplate","*datasetClause","whereClause","solutionModifier"]}, 
  "constructTemplate" : {
     "{": ["{","?constructTriples","}"]}, 
  "constructTriples" : {
     "VAR1": ["triplesSameSubject","?[., ?constructTriples]"], 
     "VAR2": ["triplesSameSubject","?[., ?constructTriples]"], 
     "NIL": ["triplesSameSubject","?[., ?constructTriples]"], 
     "(": ["triplesSameSubject","?[., ?constructTriples]"], 
     "[": ["triplesSameSubject","?[., ?constructTriples]"], 
     "IRI_REF": ["triplesSameSubject","?[., ?constructTriples]"], 
     "TRUE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "FALSE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "BLANK_NODE_LABEL": ["triplesSameSubject","?[., ?constructTriples]"], 
     "ANON": ["triplesSameSubject","?[., ?constructTriples]"], 
     "PNAME_LN": ["triplesSameSubject","?[., ?constructTriples]"], 
     "PNAME_NS": ["triplesSameSubject","?[., ?constructTriples]"], 
     "STRING_LITERAL1": ["triplesSameSubject","?[., ?constructTriples]"], 
     "STRING_LITERAL2": ["triplesSameSubject","?[., ?constructTriples]"], 
     "STRING_LITERAL_LONG1": ["triplesSameSubject","?[., ?constructTriples]"], 
     "STRING_LITERAL_LONG2": ["triplesSameSubject","?[., ?constructTriples]"], 
     "INTEGER": ["triplesSameSubject","?[., ?constructTriples]"], 
     "DECIMAL": ["triplesSameSubject","?[., ?constructTriples]"], 
     "DOUBLE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "INTEGER_POSITIVE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "DECIMAL_POSITIVE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "DOUBLE_POSITIVE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "INTEGER_NEGATIVE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "DECIMAL_NEGATIVE": ["triplesSameSubject","?[., ?constructTriples]"], 
     "DOUBLE_NEGATIVE": ["triplesSameSubject","?[., ?constructTriples]"]}, 
  "datasetClause" : {
     "FROM": ["FROM","or([defaultGraphClause, namedGraphClause])"]}, 
  "defaultGraphClause" : {
     "IRI_REF": ["sourceSelector"], 
     "PNAME_LN": ["sourceSelector"], 
     "PNAME_NS": ["sourceSelector"]}, 
  "describeDatasetClause" : {
     "FROM": ["FROM","or([defaultGraphClause, namedGraphClause])"]}, 
  "describeQuery" : {
     "DESCRIBE": ["DESCRIBE","or([+varOrIRIref, *])","*describeDatasetClause","?whereClause","solutionModifier"]}, 
  "expression" : {
     "!": ["conditionalOrExpression"], 
     "+": ["conditionalOrExpression"], 
     "-": ["conditionalOrExpression"], 
     "VAR1": ["conditionalOrExpression"], 
     "VAR2": ["conditionalOrExpression"], 
     "(": ["conditionalOrExpression"], 
     "STR": ["conditionalOrExpression"], 
     "LANG": ["conditionalOrExpression"], 
     "LANGMATCHES": ["conditionalOrExpression"], 
     "DATATYPE": ["conditionalOrExpression"], 
     "BOUND": ["conditionalOrExpression"], 
     "SAMETERM": ["conditionalOrExpression"], 
     "ISIRI": ["conditionalOrExpression"], 
     "ISURI": ["conditionalOrExpression"], 
     "ISBLANK": ["conditionalOrExpression"], 
     "ISLITERAL": ["conditionalOrExpression"], 
     "TRUE": ["conditionalOrExpression"], 
     "FALSE": ["conditionalOrExpression"], 
     "REGEX": ["conditionalOrExpression"], 
     "IRI_REF": ["conditionalOrExpression"], 
     "STRING_LITERAL1": ["conditionalOrExpression"], 
     "STRING_LITERAL2": ["conditionalOrExpression"], 
     "STRING_LITERAL_LONG1": ["conditionalOrExpression"], 
     "STRING_LITERAL_LONG2": ["conditionalOrExpression"], 
     "INTEGER": ["conditionalOrExpression"], 
     "DECIMAL": ["conditionalOrExpression"], 
     "DOUBLE": ["conditionalOrExpression"], 
     "INTEGER_POSITIVE": ["conditionalOrExpression"], 
     "DECIMAL_POSITIVE": ["conditionalOrExpression"], 
     "DOUBLE_POSITIVE": ["conditionalOrExpression"], 
     "INTEGER_NEGATIVE": ["conditionalOrExpression"], 
     "DECIMAL_NEGATIVE": ["conditionalOrExpression"], 
     "DOUBLE_NEGATIVE": ["conditionalOrExpression"], 
     "PNAME_LN": ["conditionalOrExpression"], 
     "PNAME_NS": ["conditionalOrExpression"]}, 
  "filter" : {
     "FILTER": ["FILTER","constraint"]}, 
  "functionCall" : {
     "IRI_REF": ["iriRef","argList"], 
     "PNAME_LN": ["iriRef","argList"], 
     "PNAME_NS": ["iriRef","argList"]}, 
  "graphGraphPattern" : {
     "GRAPH": ["GRAPH","varOrIRIref","groupGraphPattern"]}, 
  "graphNode" : {
     "VAR1": ["varOrTerm"], 
     "VAR2": ["varOrTerm"], 
     "NIL": ["varOrTerm"], 
     "IRI_REF": ["varOrTerm"], 
     "TRUE": ["varOrTerm"], 
     "FALSE": ["varOrTerm"], 
     "BLANK_NODE_LABEL": ["varOrTerm"], 
     "ANON": ["varOrTerm"], 
     "PNAME_LN": ["varOrTerm"], 
     "PNAME_NS": ["varOrTerm"], 
     "STRING_LITERAL1": ["varOrTerm"], 
     "STRING_LITERAL2": ["varOrTerm"], 
     "STRING_LITERAL_LONG1": ["varOrTerm"], 
     "STRING_LITERAL_LONG2": ["varOrTerm"], 
     "INTEGER": ["varOrTerm"], 
     "DECIMAL": ["varOrTerm"], 
     "DOUBLE": ["varOrTerm"], 
     "INTEGER_POSITIVE": ["varOrTerm"], 
     "DECIMAL_POSITIVE": ["varOrTerm"], 
     "DOUBLE_POSITIVE": ["varOrTerm"], 
     "INTEGER_NEGATIVE": ["varOrTerm"], 
     "DECIMAL_NEGATIVE": ["varOrTerm"], 
     "DOUBLE_NEGATIVE": ["varOrTerm"], 
     "(": ["triplesNode"], 
     "[": ["triplesNode"]}, 
  "graphPatternNotTriples" : {
     "OPTIONAL": ["optionalGraphPattern"], 
     "{": ["groupOrUnionGraphPattern"], 
     "GRAPH": ["graphGraphPattern"]}, 
  "graphTerm" : {
     "IRI_REF": ["iriRef"], 
     "PNAME_LN": ["iriRef"], 
     "PNAME_NS": ["iriRef"], 
     "STRING_LITERAL1": ["rdfLiteral"], 
     "STRING_LITERAL2": ["rdfLiteral"], 
     "STRING_LITERAL_LONG1": ["rdfLiteral"], 
     "STRING_LITERAL_LONG2": ["rdfLiteral"], 
     "INTEGER": ["numericLiteral"], 
     "DECIMAL": ["numericLiteral"], 
     "DOUBLE": ["numericLiteral"], 
     "INTEGER_POSITIVE": ["numericLiteral"], 
     "DECIMAL_POSITIVE": ["numericLiteral"], 
     "DOUBLE_POSITIVE": ["numericLiteral"], 
     "INTEGER_NEGATIVE": ["numericLiteral"], 
     "DECIMAL_NEGATIVE": ["numericLiteral"], 
     "DOUBLE_NEGATIVE": ["numericLiteral"], 
     "TRUE": ["booleanLiteral"], 
     "FALSE": ["booleanLiteral"], 
     "BLANK_NODE_LABEL": ["blankNode"], 
     "ANON": ["blankNode"], 
     "NIL": ["NIL"]}, 
  "groupGraphPattern" : {
     "{": ["{","?triplesBlock","*[or([graphPatternNotTriples, filter]), ?., ?triplesBlock]","}"]}, 
  "groupOrUnionGraphPattern" : {
     "{": ["groupGraphPattern","*[UNION, groupGraphPattern]"]}, 
  "iriRef" : {
     "IRI_REF": ["IRI_REF"], 
     "PNAME_LN": ["prefixedName"], 
     "PNAME_NS": ["prefixedName"]}, 
  "iriRefOrFunction" : {
     "IRI_REF": ["iriRef","?argList"], 
     "PNAME_LN": ["iriRef","?argList"], 
     "PNAME_NS": ["iriRef","?argList"]}, 
  "limitClause" : {
     "LIMIT": ["LIMIT","INTEGER"]}, 
  "limitOffsetClauses" : {
     "LIMIT": ["limitClause","?offsetClause"], 
     "OFFSET": ["offsetClause","?limitClause"]}, 
  "multiplicativeExpression" : {
     "!": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "+": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "-": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "VAR1": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "VAR2": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "(": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "STR": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "LANG": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "LANGMATCHES": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DATATYPE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "BOUND": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "SAMETERM": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "ISIRI": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "ISURI": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "ISBLANK": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "ISLITERAL": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "TRUE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "FALSE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "REGEX": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "IRI_REF": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "STRING_LITERAL1": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "STRING_LITERAL2": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "STRING_LITERAL_LONG1": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "STRING_LITERAL_LONG2": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "INTEGER": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DECIMAL": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DOUBLE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "INTEGER_POSITIVE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DECIMAL_POSITIVE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DOUBLE_POSITIVE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "INTEGER_NEGATIVE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DECIMAL_NEGATIVE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "DOUBLE_NEGATIVE": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "PNAME_LN": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"], 
     "PNAME_NS": ["unaryExpression","*or([[*, unaryExpression], [/, unaryExpression]])"]}, 
  "namedGraphClause" : {
     "NAMED": ["NAMED","sourceSelector"]}, 
  "numericExpression" : {
     "!": ["additiveExpression"], 
     "+": ["additiveExpression"], 
     "-": ["additiveExpression"], 
     "VAR1": ["additiveExpression"], 
     "VAR2": ["additiveExpression"], 
     "(": ["additiveExpression"], 
     "STR": ["additiveExpression"], 
     "LANG": ["additiveExpression"], 
     "LANGMATCHES": ["additiveExpression"], 
     "DATATYPE": ["additiveExpression"], 
     "BOUND": ["additiveExpression"], 
     "SAMETERM": ["additiveExpression"], 
     "ISIRI": ["additiveExpression"], 
     "ISURI": ["additiveExpression"], 
     "ISBLANK": ["additiveExpression"], 
     "ISLITERAL": ["additiveExpression"], 
     "TRUE": ["additiveExpression"], 
     "FALSE": ["additiveExpression"], 
     "REGEX": ["additiveExpression"], 
     "IRI_REF": ["additiveExpression"], 
     "STRING_LITERAL1": ["additiveExpression"], 
     "STRING_LITERAL2": ["additiveExpression"], 
     "STRING_LITERAL_LONG1": ["additiveExpression"], 
     "STRING_LITERAL_LONG2": ["additiveExpression"], 
     "INTEGER": ["additiveExpression"], 
     "DECIMAL": ["additiveExpression"], 
     "DOUBLE": ["additiveExpression"], 
     "INTEGER_POSITIVE": ["additiveExpression"], 
     "DECIMAL_POSITIVE": ["additiveExpression"], 
     "DOUBLE_POSITIVE": ["additiveExpression"], 
     "INTEGER_NEGATIVE": ["additiveExpression"], 
     "DECIMAL_NEGATIVE": ["additiveExpression"], 
     "DOUBLE_NEGATIVE": ["additiveExpression"], 
     "PNAME_LN": ["additiveExpression"], 
     "PNAME_NS": ["additiveExpression"]}, 
  "numericLiteral" : {
     "INTEGER": ["numericLiteralUnsigned"], 
     "DECIMAL": ["numericLiteralUnsigned"], 
     "DOUBLE": ["numericLiteralUnsigned"], 
     "INTEGER_POSITIVE": ["numericLiteralPositive"], 
     "DECIMAL_POSITIVE": ["numericLiteralPositive"], 
     "DOUBLE_POSITIVE": ["numericLiteralPositive"], 
     "INTEGER_NEGATIVE": ["numericLiteralNegative"], 
     "DECIMAL_NEGATIVE": ["numericLiteralNegative"], 
     "DOUBLE_NEGATIVE": ["numericLiteralNegative"]}, 
  "numericLiteralNegative" : {
     "INTEGER_NEGATIVE": ["INTEGER_NEGATIVE"], 
     "DECIMAL_NEGATIVE": ["DECIMAL_NEGATIVE"], 
     "DOUBLE_NEGATIVE": ["DOUBLE_NEGATIVE"]}, 
  "numericLiteralPositive" : {
     "INTEGER_POSITIVE": ["INTEGER_POSITIVE"], 
     "DECIMAL_POSITIVE": ["DECIMAL_POSITIVE"], 
     "DOUBLE_POSITIVE": ["DOUBLE_POSITIVE"]}, 
  "numericLiteralUnsigned" : {
     "INTEGER": ["INTEGER"], 
     "DECIMAL": ["DECIMAL"], 
     "DOUBLE": ["DOUBLE"]}, 
  "object" : {
     "(": ["graphNode"], 
     "[": ["graphNode"], 
     "VAR1": ["graphNode"], 
     "VAR2": ["graphNode"], 
     "NIL": ["graphNode"], 
     "IRI_REF": ["graphNode"], 
     "TRUE": ["graphNode"], 
     "FALSE": ["graphNode"], 
     "BLANK_NODE_LABEL": ["graphNode"], 
     "ANON": ["graphNode"], 
     "PNAME_LN": ["graphNode"], 
     "PNAME_NS": ["graphNode"], 
     "STRING_LITERAL1": ["graphNode"], 
     "STRING_LITERAL2": ["graphNode"], 
     "STRING_LITERAL_LONG1": ["graphNode"], 
     "STRING_LITERAL_LONG2": ["graphNode"], 
     "INTEGER": ["graphNode"], 
     "DECIMAL": ["graphNode"], 
     "DOUBLE": ["graphNode"], 
     "INTEGER_POSITIVE": ["graphNode"], 
     "DECIMAL_POSITIVE": ["graphNode"], 
     "DOUBLE_POSITIVE": ["graphNode"], 
     "INTEGER_NEGATIVE": ["graphNode"], 
     "DECIMAL_NEGATIVE": ["graphNode"], 
     "DOUBLE_NEGATIVE": ["graphNode"]}, 
  "objectList" : {
     "(": ["object","*[ (,), object]"], 
     "[": ["object","*[ (,), object]"], 
     "VAR1": ["object","*[ (,), object]"], 
     "VAR2": ["object","*[ (,), object]"], 
     "NIL": ["object","*[ (,), object]"], 
     "IRI_REF": ["object","*[ (,), object]"], 
     "TRUE": ["object","*[ (,), object]"], 
     "FALSE": ["object","*[ (,), object]"], 
     "BLANK_NODE_LABEL": ["object","*[ (,), object]"], 
     "ANON": ["object","*[ (,), object]"], 
     "PNAME_LN": ["object","*[ (,), object]"], 
     "PNAME_NS": ["object","*[ (,), object]"], 
     "STRING_LITERAL1": ["object","*[ (,), object]"], 
     "STRING_LITERAL2": ["object","*[ (,), object]"], 
     "STRING_LITERAL_LONG1": ["object","*[ (,), object]"], 
     "STRING_LITERAL_LONG2": ["object","*[ (,), object]"], 
     "INTEGER": ["object","*[ (,), object]"], 
     "DECIMAL": ["object","*[ (,), object]"], 
     "DOUBLE": ["object","*[ (,), object]"], 
     "INTEGER_POSITIVE": ["object","*[ (,), object]"], 
     "DECIMAL_POSITIVE": ["object","*[ (,), object]"], 
     "DOUBLE_POSITIVE": ["object","*[ (,), object]"], 
     "INTEGER_NEGATIVE": ["object","*[ (,), object]"], 
     "DECIMAL_NEGATIVE": ["object","*[ (,), object]"], 
     "DOUBLE_NEGATIVE": ["object","*[ (,), object]"]}, 
  "offsetClause" : {
     "OFFSET": ["OFFSET","INTEGER"]}, 
  "optionalGraphPattern" : {
     "OPTIONAL": ["OPTIONAL","groupGraphPattern"]}, 
  "or([+var, *])" : {
     "VAR1": ["+var"], 
     "VAR2": ["+var"], 
     "*": ["*"]}, 
  "or([+varOrIRIref, *])" : {
     "VAR1": ["+varOrIRIref"], 
     "VAR2": ["+varOrIRIref"], 
     "IRI_REF": ["+varOrIRIref"], 
     "PNAME_LN": ["+varOrIRIref"], 
     "PNAME_NS": ["+varOrIRIref"], 
     "*": ["*"]}, 
  "or([ASC, DESC])" : {
     "ASC": ["ASC"], 
     "DESC": ["DESC"]}, 
  "or([DISTINCT, REDUCED])" : {
     "DISTINCT": ["DISTINCT"], 
     "REDUCED": ["REDUCED"]}, 
  "or([LANGTAG, [^^, iriRef]])" : {
     "LANGTAG": ["LANGTAG"], 
     "^^": ["[^^, iriRef]"]}, 
  "or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])" : {
     "!": ["[!, primaryExpression]"], 
     "+": ["[+, primaryExpression]"], 
     "-": ["[-, primaryExpression]"], 
     "VAR1": ["primaryExpression"], 
     "VAR2": ["primaryExpression"], 
     "(": ["primaryExpression"], 
     "STR": ["primaryExpression"], 
     "LANG": ["primaryExpression"], 
     "LANGMATCHES": ["primaryExpression"], 
     "DATATYPE": ["primaryExpression"], 
     "BOUND": ["primaryExpression"], 
     "SAMETERM": ["primaryExpression"], 
     "ISIRI": ["primaryExpression"], 
     "ISURI": ["primaryExpression"], 
     "ISBLANK": ["primaryExpression"], 
     "ISLITERAL": ["primaryExpression"], 
     "TRUE": ["primaryExpression"], 
     "FALSE": ["primaryExpression"], 
     "REGEX": ["primaryExpression"], 
     "IRI_REF": ["primaryExpression"], 
     "STRING_LITERAL1": ["primaryExpression"], 
     "STRING_LITERAL2": ["primaryExpression"], 
     "STRING_LITERAL_LONG1": ["primaryExpression"], 
     "STRING_LITERAL_LONG2": ["primaryExpression"], 
     "INTEGER": ["primaryExpression"], 
     "DECIMAL": ["primaryExpression"], 
     "DOUBLE": ["primaryExpression"], 
     "INTEGER_POSITIVE": ["primaryExpression"], 
     "DECIMAL_POSITIVE": ["primaryExpression"], 
     "DOUBLE_POSITIVE": ["primaryExpression"], 
     "INTEGER_NEGATIVE": ["primaryExpression"], 
     "DECIMAL_NEGATIVE": ["primaryExpression"], 
     "DOUBLE_NEGATIVE": ["primaryExpression"], 
     "PNAME_LN": ["primaryExpression"], 
     "PNAME_NS": ["primaryExpression"]}, 
  "or([[*, unaryExpression], [/, unaryExpression]])" : {
     "*": ["[*, unaryExpression]"], 
     "/": ["[/, unaryExpression]"]}, 
  "or([[+, multiplicativeExpression], [-, multiplicativeExpression], numericLiteralPositive, numericLiteralNegative])" : {
     "+": ["[+, multiplicativeExpression]"], 
     "-": ["[-, multiplicativeExpression]"], 
     "INTEGER_POSITIVE": ["numericLiteralPositive"], 
     "DECIMAL_POSITIVE": ["numericLiteralPositive"], 
     "DOUBLE_POSITIVE": ["numericLiteralPositive"], 
     "INTEGER_NEGATIVE": ["numericLiteralNegative"], 
     "DECIMAL_NEGATIVE": ["numericLiteralNegative"], 
     "DOUBLE_NEGATIVE": ["numericLiteralNegative"]}, 
  "or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])" : {
     "=": ["[=, numericExpression]"], 
     "!=": ["[!=, numericExpression]"], 
     "<": ["[<, numericExpression]"], 
     ">": ["[>, numericExpression]"], 
     "<=": ["[<=, numericExpression]"], 
     ">=": ["[>=, numericExpression]"]}, 
  "or([defaultGraphClause, namedGraphClause])" : {
     "IRI_REF": ["defaultGraphClause"], 
     "PNAME_LN": ["defaultGraphClause"], 
     "PNAME_NS": ["defaultGraphClause"], 
     "NAMED": ["namedGraphClause"]}, 
  "or([graphPatternNotTriples, filter])" : {
     "OPTIONAL": ["graphPatternNotTriples"], 
     "{": ["graphPatternNotTriples"], 
     "GRAPH": ["graphPatternNotTriples"], 
     "FILTER": ["filter"]}, 
  "or([selectQuery, constructQuery, describeQuery, askQuery])" : {
     "SELECT": ["selectQuery"], 
     "CONSTRUCT": ["constructQuery"], 
     "DESCRIBE": ["describeQuery"], 
     "ASK": ["askQuery"]}, 
  "orderClause" : {
     "ORDER": ["ORDER","BY","+orderCondition"]}, 
  "orderCondition" : {
     "ASC": ["or([ASC, DESC])","brackettedExpression"], 
     "DESC": ["or([ASC, DESC])","brackettedExpression"], 
     "(": ["constraint"], 
     "STR": ["constraint"], 
     "LANG": ["constraint"], 
     "LANGMATCHES": ["constraint"], 
     "DATATYPE": ["constraint"], 
     "BOUND": ["constraint"], 
     "SAMETERM": ["constraint"], 
     "ISIRI": ["constraint"], 
     "ISURI": ["constraint"], 
     "ISBLANK": ["constraint"], 
     "ISLITERAL": ["constraint"], 
     "REGEX": ["constraint"], 
     "IRI_REF": ["constraint"], 
     "PNAME_LN": ["constraint"], 
     "PNAME_NS": ["constraint"], 
     "VAR1": ["var"], 
     "VAR2": ["var"]}, 
  "prefixDecl" : {
     "PREFIX": ["PREFIX","PNAME_NS","IRI_REF"]}, 
  "prefixedName" : {
     "PNAME_LN": ["PNAME_LN"], 
     "PNAME_NS": ["PNAME_NS"]}, 
  "primaryExpression" : {
     "(": ["brackettedExpression"], 
     "STR": ["builtInCall"], 
     "LANG": ["builtInCall"], 
     "LANGMATCHES": ["builtInCall"], 
     "DATATYPE": ["builtInCall"], 
     "BOUND": ["builtInCall"], 
     "SAMETERM": ["builtInCall"], 
     "ISIRI": ["builtInCall"], 
     "ISURI": ["builtInCall"], 
     "ISBLANK": ["builtInCall"], 
     "ISLITERAL": ["builtInCall"], 
     "REGEX": ["builtInCall"], 
     "IRI_REF": ["iriRefOrFunction"], 
     "PNAME_LN": ["iriRefOrFunction"], 
     "PNAME_NS": ["iriRefOrFunction"], 
     "STRING_LITERAL1": ["rdfLiteral"], 
     "STRING_LITERAL2": ["rdfLiteral"], 
     "STRING_LITERAL_LONG1": ["rdfLiteral"], 
     "STRING_LITERAL_LONG2": ["rdfLiteral"], 
     "INTEGER": ["numericLiteral"], 
     "DECIMAL": ["numericLiteral"], 
     "DOUBLE": ["numericLiteral"], 
     "INTEGER_POSITIVE": ["numericLiteral"], 
     "DECIMAL_POSITIVE": ["numericLiteral"], 
     "DOUBLE_POSITIVE": ["numericLiteral"], 
     "INTEGER_NEGATIVE": ["numericLiteral"], 
     "DECIMAL_NEGATIVE": ["numericLiteral"], 
     "DOUBLE_NEGATIVE": ["numericLiteral"], 
     "TRUE": ["booleanLiteral"], 
     "FALSE": ["booleanLiteral"], 
     "VAR1": ["var"], 
     "VAR2": ["var"]}, 
  "prologue" : {
     "PREFIX": ["?baseDecl","*prefixDecl"], 
     "BASE": ["?baseDecl","*prefixDecl"], 
     "SELECT": ["?baseDecl","*prefixDecl"], 
     "CONSTRUCT": ["?baseDecl","*prefixDecl"], 
     "DESCRIBE": ["?baseDecl","*prefixDecl"], 
     "ASK": ["?baseDecl","*prefixDecl"]}, 
  "propertyList" : {
     "a": ["?propertyListNotEmpty"], 
     "VAR1": ["?propertyListNotEmpty"], 
     "VAR2": ["?propertyListNotEmpty"], 
     "IRI_REF": ["?propertyListNotEmpty"], 
     "PNAME_LN": ["?propertyListNotEmpty"], 
     "PNAME_NS": ["?propertyListNotEmpty"], 
     ".": ["?propertyListNotEmpty"], 
     "}": ["?propertyListNotEmpty"], 
     "FILTER": ["?propertyListNotEmpty"], 
     "OPTIONAL": ["?propertyListNotEmpty"], 
     "{": ["?propertyListNotEmpty"], 
     "GRAPH": ["?propertyListNotEmpty"]}, 
  "propertyListNotEmpty" : {
     "a": ["verb","objectList","*[ (;), ?[verb, objectList]]"], 
     "VAR1": ["verb","objectList","*[ (;), ?[verb, objectList]]"], 
     "VAR2": ["verb","objectList","*[ (;), ?[verb, objectList]]"], 
     "IRI_REF": ["verb","objectList","*[ (;), ?[verb, objectList]]"], 
     "PNAME_LN": ["verb","objectList","*[ (;), ?[verb, objectList]]"], 
     "PNAME_NS": ["verb","objectList","*[ (;), ?[verb, objectList]]"]}, 
  "query" : {
     "SELECT": ["prologue","or([selectQuery, constructQuery, describeQuery, askQuery])"], 
     "CONSTRUCT": ["prologue","or([selectQuery, constructQuery, describeQuery, askQuery])"], 
     "DESCRIBE": ["prologue","or([selectQuery, constructQuery, describeQuery, askQuery])"], 
     "ASK": ["prologue","or([selectQuery, constructQuery, describeQuery, askQuery])"], 
     "PREFIX": ["prologue","or([selectQuery, constructQuery, describeQuery, askQuery])"], 
     "BASE": ["prologue","or([selectQuery, constructQuery, describeQuery, askQuery])"]}, 
  "rdfLiteral" : {
     "STRING_LITERAL1": ["string","?or([LANGTAG, [^^, iriRef]])"], 
     "STRING_LITERAL2": ["string","?or([LANGTAG, [^^, iriRef]])"], 
     "STRING_LITERAL_LONG1": ["string","?or([LANGTAG, [^^, iriRef]])"], 
     "STRING_LITERAL_LONG2": ["string","?or([LANGTAG, [^^, iriRef]])"]}, 
  "regexExpression" : {
     "REGEX": ["REGEX","(","expression",",","expression","?[ (,), expression]",")"]}, 
  "relationalExpression" : {
     "!": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "+": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "-": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "VAR1": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "VAR2": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "(": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "STR": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "LANG": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "LANGMATCHES": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DATATYPE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "BOUND": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "SAMETERM": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "ISIRI": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "ISURI": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "ISBLANK": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "ISLITERAL": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "TRUE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "FALSE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "REGEX": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "IRI_REF": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "STRING_LITERAL1": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "STRING_LITERAL2": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "STRING_LITERAL_LONG1": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "STRING_LITERAL_LONG2": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "INTEGER": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DECIMAL": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DOUBLE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "INTEGER_POSITIVE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DECIMAL_POSITIVE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DOUBLE_POSITIVE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "INTEGER_NEGATIVE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DECIMAL_NEGATIVE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "DOUBLE_NEGATIVE": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "PNAME_LN": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"], 
     "PNAME_NS": ["numericExpression","?or([[=, numericExpression], [!=, numericExpression], [<, numericExpression], [>, numericExpression], [<=, numericExpression], [>=, numericExpression]])"]}, 
  "s" : {
     "SELECT": ["query","$"], 
     "CONSTRUCT": ["query","$"], 
     "DESCRIBE": ["query","$"], 
     "ASK": ["query","$"], 
     "PREFIX": ["query","$"], 
     "BASE": ["query","$"]}, 
  "selectQuery" : {
     "SELECT": ["SELECT","?or([DISTINCT, REDUCED])","or([+var, *])","*datasetClause","whereClause","solutionModifier"]}, 
  "solutionModifier" : {
     "LIMIT": ["?orderClause","?limitOffsetClauses"], 
     "OFFSET": ["?orderClause","?limitOffsetClauses"], 
     "ORDER": ["?orderClause","?limitOffsetClauses"], 
     "$": ["?orderClause","?limitOffsetClauses"]}, 
  "sourceSelector" : {
     "IRI_REF": ["iriRef"], 
     "PNAME_LN": ["iriRef"], 
     "PNAME_NS": ["iriRef"]}, 
  "storeProperty" : {
     "VAR1": [], 
     "VAR2": [], 
     "IRI_REF": [], 
     "PNAME_LN": [], 
     "PNAME_NS": [], 
     "a": []}, 
  "string" : {
     "STRING_LITERAL1": ["STRING_LITERAL1"], 
     "STRING_LITERAL2": ["STRING_LITERAL2"], 
     "STRING_LITERAL_LONG1": ["STRING_LITERAL_LONG1"], 
     "STRING_LITERAL_LONG2": ["STRING_LITERAL_LONG2"]}, 
  "triplesBlock" : {
     "VAR1": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "VAR2": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "NIL": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "(": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "[": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "IRI_REF": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "TRUE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "FALSE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "BLANK_NODE_LABEL": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "ANON": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "PNAME_LN": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "PNAME_NS": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "STRING_LITERAL1": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "STRING_LITERAL2": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "STRING_LITERAL_LONG1": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "STRING_LITERAL_LONG2": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "INTEGER": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "DECIMAL": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "DOUBLE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "INTEGER_POSITIVE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "DECIMAL_POSITIVE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "DOUBLE_POSITIVE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "INTEGER_NEGATIVE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "DECIMAL_NEGATIVE": ["triplesSameSubject","?[., ?triplesBlock]"], 
     "DOUBLE_NEGATIVE": ["triplesSameSubject","?[., ?triplesBlock]"]}, 
  "triplesNode" : {
     "(": ["collection"], 
     "[": ["blankNodePropertyList"]}, 
  "triplesSameSubject" : {
     "VAR1": ["varOrTerm","propertyListNotEmpty"], 
     "VAR2": ["varOrTerm","propertyListNotEmpty"], 
     "NIL": ["varOrTerm","propertyListNotEmpty"], 
     "IRI_REF": ["varOrTerm","propertyListNotEmpty"], 
     "TRUE": ["varOrTerm","propertyListNotEmpty"], 
     "FALSE": ["varOrTerm","propertyListNotEmpty"], 
     "BLANK_NODE_LABEL": ["varOrTerm","propertyListNotEmpty"], 
     "ANON": ["varOrTerm","propertyListNotEmpty"], 
     "PNAME_LN": ["varOrTerm","propertyListNotEmpty"], 
     "PNAME_NS": ["varOrTerm","propertyListNotEmpty"], 
     "STRING_LITERAL1": ["varOrTerm","propertyListNotEmpty"], 
     "STRING_LITERAL2": ["varOrTerm","propertyListNotEmpty"], 
     "STRING_LITERAL_LONG1": ["varOrTerm","propertyListNotEmpty"], 
     "STRING_LITERAL_LONG2": ["varOrTerm","propertyListNotEmpty"], 
     "INTEGER": ["varOrTerm","propertyListNotEmpty"], 
     "DECIMAL": ["varOrTerm","propertyListNotEmpty"], 
     "DOUBLE": ["varOrTerm","propertyListNotEmpty"], 
     "INTEGER_POSITIVE": ["varOrTerm","propertyListNotEmpty"], 
     "DECIMAL_POSITIVE": ["varOrTerm","propertyListNotEmpty"], 
     "DOUBLE_POSITIVE": ["varOrTerm","propertyListNotEmpty"], 
     "INTEGER_NEGATIVE": ["varOrTerm","propertyListNotEmpty"], 
     "DECIMAL_NEGATIVE": ["varOrTerm","propertyListNotEmpty"], 
     "DOUBLE_NEGATIVE": ["varOrTerm","propertyListNotEmpty"], 
     "(": ["triplesNode","propertyList"], 
     "[": ["triplesNode","propertyList"]}, 
  "unaryExpression" : {
     "!": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "+": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "-": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "VAR1": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "VAR2": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "(": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "STR": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "LANG": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "LANGMATCHES": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DATATYPE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "BOUND": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "SAMETERM": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "ISIRI": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "ISURI": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "ISBLANK": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "ISLITERAL": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "TRUE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "FALSE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "REGEX": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "IRI_REF": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "STRING_LITERAL1": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "STRING_LITERAL2": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "STRING_LITERAL_LONG1": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "STRING_LITERAL_LONG2": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "INTEGER": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DECIMAL": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DOUBLE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "INTEGER_POSITIVE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DECIMAL_POSITIVE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DOUBLE_POSITIVE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "INTEGER_NEGATIVE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DECIMAL_NEGATIVE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "DOUBLE_NEGATIVE": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "PNAME_LN": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"], 
     "PNAME_NS": ["or([[!, primaryExpression], [+, primaryExpression], [-, primaryExpression], primaryExpression])"]}, 
  "valueLogical" : {
     "!": ["relationalExpression"], 
     "+": ["relationalExpression"], 
     "-": ["relationalExpression"], 
     "VAR1": ["relationalExpression"], 
     "VAR2": ["relationalExpression"], 
     "(": ["relationalExpression"], 
     "STR": ["relationalExpression"], 
     "LANG": ["relationalExpression"], 
     "LANGMATCHES": ["relationalExpression"], 
     "DATATYPE": ["relationalExpression"], 
     "BOUND": ["relationalExpression"], 
     "SAMETERM": ["relationalExpression"], 
     "ISIRI": ["relationalExpression"], 
     "ISURI": ["relationalExpression"], 
     "ISBLANK": ["relationalExpression"], 
     "ISLITERAL": ["relationalExpression"], 
     "TRUE": ["relationalExpression"], 
     "FALSE": ["relationalExpression"], 
     "REGEX": ["relationalExpression"], 
     "IRI_REF": ["relationalExpression"], 
     "STRING_LITERAL1": ["relationalExpression"], 
     "STRING_LITERAL2": ["relationalExpression"], 
     "STRING_LITERAL_LONG1": ["relationalExpression"], 
     "STRING_LITERAL_LONG2": ["relationalExpression"], 
     "INTEGER": ["relationalExpression"], 
     "DECIMAL": ["relationalExpression"], 
     "DOUBLE": ["relationalExpression"], 
     "INTEGER_POSITIVE": ["relationalExpression"], 
     "DECIMAL_POSITIVE": ["relationalExpression"], 
     "DOUBLE_POSITIVE": ["relationalExpression"], 
     "INTEGER_NEGATIVE": ["relationalExpression"], 
     "DECIMAL_NEGATIVE": ["relationalExpression"], 
     "DOUBLE_NEGATIVE": ["relationalExpression"], 
     "PNAME_LN": ["relationalExpression"], 
     "PNAME_NS": ["relationalExpression"]}, 
  "var" : {
     "VAR1": ["VAR1"], 
     "VAR2": ["VAR2"]}, 
  "varOrIRIref" : {
     "VAR1": ["var"], 
     "VAR2": ["var"], 
     "IRI_REF": ["iriRef"], 
     "PNAME_LN": ["iriRef"], 
     "PNAME_NS": ["iriRef"]}, 
  "varOrTerm" : {
     "VAR1": ["var"], 
     "VAR2": ["var"], 
     "NIL": ["graphTerm"], 
     "IRI_REF": ["graphTerm"], 
     "TRUE": ["graphTerm"], 
     "FALSE": ["graphTerm"], 
     "BLANK_NODE_LABEL": ["graphTerm"], 
     "ANON": ["graphTerm"], 
     "PNAME_LN": ["graphTerm"], 
     "PNAME_NS": ["graphTerm"], 
     "STRING_LITERAL1": ["graphTerm"], 
     "STRING_LITERAL2": ["graphTerm"], 
     "STRING_LITERAL_LONG1": ["graphTerm"], 
     "STRING_LITERAL_LONG2": ["graphTerm"], 
     "INTEGER": ["graphTerm"], 
     "DECIMAL": ["graphTerm"], 
     "DOUBLE": ["graphTerm"], 
     "INTEGER_POSITIVE": ["graphTerm"], 
     "DECIMAL_POSITIVE": ["graphTerm"], 
     "DOUBLE_POSITIVE": ["graphTerm"], 
     "INTEGER_NEGATIVE": ["graphTerm"], 
     "DECIMAL_NEGATIVE": ["graphTerm"], 
     "DOUBLE_NEGATIVE": ["graphTerm"]}, 
  "verb" : {
     "VAR1": ["storeProperty","varOrIRIref"], 
     "VAR2": ["storeProperty","varOrIRIref"], 
     "IRI_REF": ["storeProperty","varOrIRIref"], 
     "PNAME_LN": ["storeProperty","varOrIRIref"], 
     "PNAME_NS": ["storeProperty","varOrIRIref"], 
     "a": ["storeProperty","a"]}, 
  "whereClause" : {
     "{": ["?WHERE","groupGraphPattern"], 
     "WHERE": ["?WHERE","groupGraphPattern"]}
};

var keywords=/^(BASE|PREFIX|SELECT|CONSTRUCT|DESCRIBE|ASK|FROM|NAMED|ORDER|BY|LIMIT|ASC|DESC|OFFSET|DISTINCT|REDUCED|WHERE|GRAPH|OPTIONAL|UNION|FILTER|STR|LANGMATCHES|LANG|DATATYPE|BOUND|SAMETERM|ISIRI|ISURI|ISBLANK|ISLITERAL|REGEX|TRUE|FALSE)/i ;

var punct=/^(\*|a|\.|\{|\}|,|\(|\)|;|\[|\]|\|\||&&|=|!=|!|<=|>=|<|>|\+|-|\/|\^\^)/ ;

var defaultQueryType=null;
var lexVersion="sparql10";
var startSymbol="query";
var acceptEmpty=false;

	function getTerminals()
	{
		var IRI_REF = '<[^<>\"\'\|\{\}\^\\\x00-\x20]*>';
		/*
		 * PN_CHARS_BASE =
		 * '[A-Z]|[a-z]|[\\u00C0-\\u00D6]|[\\u00D8-\\u00F6]|[\\u00F8-\\u02FF]|[\\u0370-\\u037D]|[\\u037F-\\u1FFF]|[\\u200C-\\u200D]|[\\u2070-\\u218F]|[\\u2C00-\\u2FEF]|[\\u3001-\\uD7FF]|[\\uF900-\\uFDCF]|[\\uFDF0-\\uFFFD]|[\\u10000-\\uEFFFF]';
		 */

		var PN_CHARS_BASE =
			'[A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]';
		var PN_CHARS_U = PN_CHARS_BASE+'|_';

		var PN_CHARS= '('+PN_CHARS_U+'|-|[0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040])';
		var VARNAME = '('+PN_CHARS_U+'|[0-9])'+
			'('+PN_CHARS_U+'|[0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040])*';
		var VAR1 = '\\?'+VARNAME;
		var VAR2 = '\\$'+VARNAME;

		var PN_PREFIX= '('+PN_CHARS_BASE+')((('+PN_CHARS+')|\\.)*('+PN_CHARS+'))?';

		var HEX= '[0-9A-Fa-f]';
		var PERCENT='(%'+HEX+HEX+')';
		var PN_LOCAL_ESC='(\\\\[_~\\.\\-!\\$&\'\\(\\)\\*\\+,;=/\\?#@%])';
		var PLX= '('+PERCENT+'|'+PN_LOCAL_ESC+')';
		var PN_LOCAL;
		var BLANK_NODE_LABEL;
		if (lexVersion=="sparql11") {
			PN_LOCAL= '('+PN_CHARS_U+'|:|[0-9]|'+PLX+')(('+PN_CHARS+'|\\.|:|'+PLX+')*('+PN_CHARS+'|:|'+PLX+'))?';
			BLANK_NODE_LABEL = '_:('+PN_CHARS_U+'|[0-9])(('+PN_CHARS+'|\\.)*'+PN_CHARS+')?';
		} else {
			PN_LOCAL= '('+PN_CHARS_U+'|[0-9])((('+PN_CHARS+')|\\.)*('+PN_CHARS+'))?';
			BLANK_NODE_LABEL = '_:'+PN_LOCAL;
		}
		var PNAME_NS = '('+PN_PREFIX+')?:';
		var PNAME_LN = PNAME_NS+PN_LOCAL;
		var LANGTAG = '@[a-zA-Z]+(-[a-zA-Z0-9]+)*';

		var EXPONENT = '[eE][\\+-]?[0-9]+';
		var INTEGER = '[0-9]+';
		var DECIMAL = '(([0-9]+\\.[0-9]*)|(\\.[0-9]+))';
		var DOUBLE =
			'(([0-9]+\\.[0-9]*'+EXPONENT+')|'+
			'(\\.[0-9]+'+EXPONENT+')|'+
			'([0-9]+'+EXPONENT+'))';

		var INTEGER_POSITIVE = '\\+' + INTEGER;
		var DECIMAL_POSITIVE = '\\+' + DECIMAL;
		var DOUBLE_POSITIVE  = '\\+' + DOUBLE;
		var INTEGER_NEGATIVE = '-' + INTEGER;
		var DECIMAL_NEGATIVE = '-' + DECIMAL;
		var DOUBLE_NEGATIVE  = '-' + DOUBLE;

		// var ECHAR = '\\\\[tbnrf\\"\\\']';
		var ECHAR = '\\\\[tbnrf\\\\"\']';

		var STRING_LITERAL1 = "'(([^\\x27\\x5C\\x0A\\x0D])|"+ECHAR+")*'";
		var STRING_LITERAL2 = '"(([^\\x22\\x5C\\x0A\\x0D])|'+ECHAR+')*"';

		var STRING_LITERAL_LONG1 = "'''(('|'')?([^'\\\\]|"+ECHAR+"))*'''";
		var STRING_LITERAL_LONG2 = '"""(("|"")?([^"\\\\]|'+ECHAR+'))*"""';

		var WS    =        '[\\x20\\x09\\x0D\\x0A]';
		// Careful! Code mirror feeds one line at a time with no \n
		// ... but otherwise comment is terminated by \n
		var COMMENT = '#([^\\n\\r]*[\\n\\r]|[^\\n\\r]*$)';
		var WS_OR_COMMENT_STAR = '('+WS+'|('+COMMENT+'))*';
		var NIL   = '\\('+WS_OR_COMMENT_STAR+'\\)';
		var ANON  = '\\['+WS_OR_COMMENT_STAR+'\\]';

		var terminals=
			{
				terminal: [

					{ name: "WS",
						regex:new RegExp("^"+WS+"+"),
						style:"sp-ws" },

					{ name: "COMMENT",
						regex:new RegExp("^"+COMMENT),
						style:"sp-comment" },

					{ name: "IRI_REF",
						regex:new RegExp("^"+IRI_REF),
						style:"sp-uri" },

					{ name: "VAR1",
						regex:new RegExp("^"+VAR1),
						style:"sp-var"},

					{ name: "VAR2",
						regex:new RegExp("^"+VAR2),
						style:"sp-var"},

					{ name: "LANGTAG",
						regex:new RegExp("^"+LANGTAG),
						style:"sp-punc"},

					{ name: "DOUBLE",
						regex:new RegExp("^"+DOUBLE),
						style:"sp-number" },

					{ name: "DECIMAL",
						regex:new RegExp("^"+DECIMAL),
						style:"sp-number" },

					{ name: "INTEGER",
						regex:new RegExp("^"+INTEGER),
						style:"sp-number" },

					{ name: "DOUBLE_POSITIVE",
						regex:new RegExp("^"+DOUBLE_POSITIVE),
						style:"sp-number" },

					{ name: "DECIMAL_POSITIVE",
						regex:new RegExp("^"+DECIMAL_POSITIVE),
						style:"sp-number" },

					{ name: "INTEGER_POSITIVE",
						regex:new RegExp("^"+INTEGER_POSITIVE),
						style:"sp-number" },

					{ name: "DOUBLE_NEGATIVE",
						regex:new RegExp("^"+DOUBLE_NEGATIVE),
						style:"sp-number" },

					{ name: "DECIMAL_NEGATIVE",
						regex:new RegExp("^"+DECIMAL_NEGATIVE),
						style:"sp-number" },

					{ name: "INTEGER_NEGATIVE",
						regex:new RegExp("^"+INTEGER_NEGATIVE),
						style:"sp-number" },

					{ name: "STRING_LITERAL_LONG1",
						regex:new RegExp("^"+STRING_LITERAL_LONG1),
						style:"sp-literal" },

					{ name: "STRING_LITERAL_LONG2",
						regex:new RegExp("^"+STRING_LITERAL_LONG2),
						style:"sp-literal" },

					{ name: "STRING_LITERAL1",
						regex:new RegExp("^"+STRING_LITERAL1),
						style:"sp-literal" },

					{ name: "STRING_LITERAL2",
						regex:new RegExp("^"+STRING_LITERAL2),
						style:"sp-literal" },

					// Enclosed comments won't be highlighted
					{ name: "NIL",
						regex:new RegExp("^"+NIL),
						style:"sp-punc" },

					// Enclosed comments won't be highlighted
					{ name: "ANON",
						regex:new RegExp("^"+ANON),
						style:"sp-punc" },

					{ name: "PNAME_LN",
						regex:new RegExp("^"+PNAME_LN),
						style:"sp-prefixed" },

					{ name: "PNAME_NS",
						regex:new RegExp("^"+PNAME_NS),
						style:"sp-prefixed" },

					{ name: "BLANK_NODE_LABEL",
						regex:new RegExp("^"+BLANK_NODE_LABEL),
						style:"sp-prefixed" }
				],

			}
		return terminals;
	}

	function getPossibles(symbol)
	{
		var possibles=[], possiblesOb=ll1_table[symbol];
		if (possiblesOb!=undefined)
			for (var property in possiblesOb)
				possibles.push(property.toString());
		else
			possibles.push(symbol);
		return possibles;
	}

	var tms= getTerminals();
	var terminal=tms.terminal;

	function tokenBase(stream, state) {

		function nextToken() {

			var consumed=null;
			// Tokens defined by individual regular expressions
			for (var i=0; i<terminal.length; ++i) {
				consumed= stream.match(terminal[i].regex,true,false);
				if (consumed)
					return { cat: terminal[i].name,
									 style: terminal[i].style,
									 text: consumed[0]
								 };
			}

			// Keywords
			consumed= stream.match(keywords,true,false);
			if (consumed)
				return { cat: stream.current().toUpperCase(),
								 style: "sp-keyword",
								 text: consumed[0]
							 };

			// Punctuation
			consumed= stream.match(punct,true,false);
			if (consumed)
				return { cat: stream.current(),
								 style: "sp-punc",
								 text: consumed[0]
							 };

			// Token is invalid
			// better consume something anyway, or else we're stuck
			consumed= stream.match(/^.[A-Za-z0-9]*/,true,false);
			return { cat:"<invalid_token>",
							 style: "sp-invalid",
							 text: consumed[0]
						 };
		}

		function recordFailurePos() {
			// tokenOb.style= "sp-invalid";
			var col= stream.column();
			state.errorStartPos= col;
			state.errorEndPos= col+tokenOb.text.length;
		};

		function setQueryType(s) {
			if (state.queryType==null) {
				if (s=="SELECT" || s=="CONSTRUCT" || s=="ASK" || s=="DESCRIBE")
					state.queryType=s;
			}
		}

		// Some fake non-terminals are just there to have side-effect on state
		// - i.e. allow or disallow variables and bnodes in certain non-nesting
		// contexts
		function setSideConditions(topSymbol) {
			if (topSymbol=="disallowVars") state.allowVars=false;
			else if (topSymbol=="allowVars") state.allowVars=true;
			else if (topSymbol=="disallowBnodes") state.allowBnodes=false;
			else if (topSymbol=="allowBnodes") state.allowBnodes=true;
			else if (topSymbol=="storeProperty") state.storeProperty=true;
		}

		function checkSideConditions(topSymbol) {
			return(
				(state.allowVars || topSymbol!="var") &&
					(state.allowBnodes ||
					 (topSymbol!="blankNode" &&
						topSymbol!="blankNodePropertyList" &&
						topSymbol!="blankNodePropertyListPath")))
		}

		// CodeMirror works with one line at a time,
		// but newline should behave like whitespace
		// - i.e. a definite break between tokens (for autocompleter)
		if (stream.pos==0)
			state.possibleCurrent= state.possibleNext;

		var tokenOb= nextToken();


		if (tokenOb.cat=="<invalid_token>") {
			// set error state, and
			if (state.OK==true) {
				state.OK=false;
				recordFailurePos();
			}
			state.complete=false;
			// alert("Invalid:"+tokenOb.text);
			return tokenOb.style;
		}

		if (tokenOb.cat == "WS" ||
				tokenOb.cat == "COMMENT") {
			state.possibleCurrent= state.possibleNext;
			return(tokenOb.style)
		}
		// Otherwise, run the parser until the token is digested
		// or failure
		var finished= false;
		var topSymbol;
		var token= tokenOb.cat;

		// Incremental LL1 parse
		while(state.stack.length>0 && token && state.OK && !finished ) {
			topSymbol= state.stack.pop();

			if (!ll1_table[topSymbol]) {
				// Top symbol is a terminal
				if (topSymbol==token) {
					// Matching terminals
					// - consume token from input stream
					finished=true;
					setQueryType(topSymbol);
					// Check whether $ (end of input token) is poss next
					// for everything on stack
					var allNillable=true;
					for(var sp=state.stack.length;sp>0;--sp) {
						var item=ll1_table[state.stack[sp-1]];
						if (!item || !item["$"])
							allNillable=false;
					}
					state.complete= allNillable;
					if (state.storeProperty && token.cat!="sp-punc") {
							state.lastProperty= tokenOb.text;
							state.storeProperty= false;
						}
				} else {
					state.OK=false;
					state.complete=false;
					recordFailurePos();
				}
			} else {
				// topSymbol is nonterminal
				// - see if there is an entry for topSymbol
				// and nextToken in table
				var nextSymbols= ll1_table[topSymbol][token];
				if (nextSymbols!=undefined
						&& checkSideConditions(topSymbol)
					 )
				{
					// Match - copy RHS of rule to stack
					for (var i=nextSymbols.length-1; i>=0; --i)
						state.stack.push(nextSymbols[i]);
					// Peform any non-grammatical side-effects
					setSideConditions(topSymbol);
				} else {
					// No match in table - fail
					state.OK=false;
					state.complete=false;
					recordFailurePos();
					state.stack.push(topSymbol);  // Shove topSymbol back on stack
				}
			}
		}
		if (!finished && state.OK) { 
			state.OK=false; state.complete=false; recordFailurePos(); 
    }

		state.possibleCurrent= state.possibleNext;
		state.possibleNext= getPossibles(state.stack[state.stack.length-1]);

		// alert(token+"="+tokenOb.style+'\n'+state.stack);
		return tokenOb.style;
	}

	var indentTop={
		"*[,, object]": 3,
		"*[ (,), object]": 3,
		"*[ (,), objectPath]": 3,
		"*[/, pathEltOrInverse]": 2,
		"object": 2,
		"objectPath": 2,
		"objectList": 2,
		"objectListPath": 2,
		"storeProperty": 2,
		"pathMod": 2,
		"?pathMod": 2,
		"propertyListNotEmpty": 1,
		"propertyList": 1,
		"propertyListPath": 1,
		"propertyListPathNotEmpty": 1,
		"?[verb, objectList]": 1,
		"?[or([verbPath, verbSimple]), objectList]": 1
	};

	var indentTable={
		"}":1,
		"]":0,
		")":1,
		"{":-1,
		"(":-1
	};

	function indent(state, textAfter) {
		var n = 0; // indent level
		var i=state.stack.length-1;

		if (/^[\}\]\)]/.test(textAfter)) {
			// Skip stack items until after matching bracket
			var closeBracket=textAfter.substr(0,1);
			for( ;i>=0;--i)
			{
				if (state.stack[i]==closeBracket)
				{--i; break};
			}
		} else {
			// Consider nullable non-terminals if at top of stack
			var dn=indentTop[state.stack[i]];
			if (dn) { n+=dn; --i}
		}
		for( ;i>=0;--i)
		{
			var dn=indentTable[state.stack[i]];
			if (dn) n+=dn;
		}
		return n * config.indentUnit;
	};

	return {
		token: tokenBase,
		startState: function(base) {
			return {
				tokenize: tokenBase,
				OK: true,
				complete: acceptEmpty,
				errorStartPos: null,
				errorEndPos: null,
				queryType: defaultQueryType,
				possibleCurrent: getPossibles(startSymbol),
				possibleNext: getPossibles(startSymbol),
				allowVars : true,
				allowBnodes : true,
				storeProperty : false,
				lastProperty : "",
				stack: [startSymbol] }; },
		indent: indent,
		electricChars: "}])"
	};
});

