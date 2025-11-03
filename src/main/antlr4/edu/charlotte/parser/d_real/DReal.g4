grammar DReal;

// Defining Parser Rules
dRealProgram
    : script EOF
    ;

response
    : general_response EOF
    ;

generalReservedWord
    : EXCLAMATION
    | UNDERSCORE
    | AS
    | BINARY_WORD
    | DECIMAL_WORD
    | EXISTS
    | HEXA_DECIMAL_WORD
    | FOR_ALL
    | LET
    | MATCH_WORD
    | NUMERAL_WORD
    | PAR
    | STRING_WORD
    ;

simpleSymbol
    : predefSymbol
    | UNDEFINED_SYMBOL
    ;

quotedSymbol
    : QUOTED_SYMBOL
    ;

predefSymbol
    : NOT
    | BOOL
    | CONTINUED_EXECUTION
    | ERROR
    | FALSE
    | IMMEDIATE_EXIT
    | INCOMPLETE
    | LOGIC
    | MEMOUT
    | SAT
    | SUCCESS
    | THEORY
    | TRUE
    | UNKNOWN
    | UNSUPPORTED
    | UNSAT
    ;

predefKeyword
    : ALL_STATISTICS
    | ASSERTION_STACK_LEVELS
    | AUTHORS
    | CATEGORY
    | CHAINABLE
    | DEFINITION
    | DIAGNOSTIC_OUTPUT_CHANNEL
    | ERROR_BEHAVIOUR
    | EXTENSION
    | FUNCTIONS
    | FUNCTIONS_DESCRIPTION
    | GLOBAL_DECLARATIONS
    | INTERACTIVE_MODE
    | LANGUAGE
    | LEFT_ASSOC
    | LICENSE
    | NAMED
    | NAME
    | NOTES
    | PATTERN
    | PRINT_SUCCESS
    | PRODUCE_ASSERTIONS
    | PRODUCE_ASSIGNMENTS
    | PRODUCE_MODELS
    | PRODUCE_PROOFS
    | PRODUCE_UNSAT_ASSUMPTIONS
    | PRODUCE_UNSAT_CORES
    | RANDOM_SEED
    | REASON_UNKNOWN
    | REGULAR_OUTPUT_CHANNEL
    | REPRODUCIBLE_RESOURCE_LIMIT
    | RIGHT_ASSOC
    | SMT_LIB_VERSION
    | SORTS
    | SORTS_DESCRIPTION
    | SOURCE
    | STATUS
    | THEORIES
    | VALUES
    | VERBOSITY
    | VERSION
    ;

symbol
    : simpleSymbol
    | quotedSymbol
    ;

keyword
    : predefKeyword
    | COLON simpleSymbol
    ;

// S-expression
spec_constant
    : NUMERALS
    | DECIMAL
    | HEXA_DECIMAL
    | BINARY
    | STRING
    ;

s_expr
    : spec_constant
    | symbol
    | keyword
    | OPEN_BRACKETS s_expr* CLOSE_BRACKETS
    ;

// Identifiers
index
    : NUMERALS
    | symbol
    ;

identifier
    : symbol
    | OPEN_BRACKETS UNDERSCORE symbol index+ CLOSE_BRACKETS
    ;

// Attributes
attribute_value
    : spec_constant
    | symbol
    | OPEN_BRACKETS s_expr* CLOSE_BRACKETS
    ;

attribute
    : keyword
    | keyword attribute_value
    ;

// Sorts
sort
    : identifier
    | OPEN_BRACKETS identifier sort+ CLOSE_BRACKETS
    ;

// Terms and Formulas
qual_identifier
    : identifier
    | OPEN_BRACKETS AS identifier sort CLOSE_BRACKETS
    ;

var_binding
    : OPEN_BRACKETS symbol term CLOSE_BRACKETS
    ;

sorted_var
    : OPEN_BRACKETS symbol sort CLOSE_BRACKETS
    ;

pattern
    : symbol
    | OPEN_BRACKETS symbol symbol+ CLOSE_BRACKETS
    ;

match_case
    : OPEN_BRACKETS pattern term CLOSE_BRACKETS
    ;

term
    : spec_constant
    | qual_identifier
    | OPEN_BRACKETS qual_identifier term+ CLOSE_BRACKETS
    | OPEN_BRACKETS LET OPEN_BRACKETS var_binding+ CLOSE_BRACKETS term CLOSE_BRACKETS
    | OPEN_BRACKETS FOR_ALL OPEN_BRACKETS sorted_var+ CLOSE_BRACKETS term CLOSE_BRACKETS
    | OPEN_BRACKETS EXISTS OPEN_BRACKETS sorted_var+ CLOSE_BRACKETS term CLOSE_BRACKETS
    | OPEN_BRACKETS MATCH_WORD term OPEN_BRACKETS match_case+ CLOSE_BRACKETS CLOSE_BRACKETS
    | OPEN_BRACKETS EXCLAMATION term attribute+ CLOSE_BRACKETS
    ;

// Theory Declarations
sort_symbol_decl
    : OPEN_BRACKETS identifier NUMERALS attribute* CLOSE_BRACKETS;

meta_spec_constant
    : NUMERAL_WORD
    | DECIMAL_WORD
    | STRING_WORD
    ;

fun_symbol_decl
    : OPEN_BRACKETS spec_constant sort attribute* CLOSE_BRACKETS
    | OPEN_BRACKETS meta_spec_constant sort attribute* CLOSE_BRACKETS
    | OPEN_BRACKETS identifier sort+ attribute* CLOSE_BRACKETS
    ;

par_fun_symbol_decl
    : fun_symbol_decl
    | OPEN_BRACKETS PAR OPEN_BRACKETS symbol+ CLOSE_BRACKETS OPEN_BRACKETS identifier sort+
    attribute* CLOSE_BRACKETS CLOSE_BRACKETS
    ;

theory_attribute
    : SORTS OPEN_BRACKETS sort_symbol_decl+ CLOSE_BRACKETS
    | FUNCTIONS OPEN_BRACKETS par_fun_symbol_decl+ CLOSE_BRACKETS
    | SORTS_DESCRIPTION STRING
    | FUNCTIONS_DESCRIPTION STRING
    | DEFINITION STRING
    | VALUES STRING
    | NOTES STRING
    | attribute
    ;

theory_decl
    : OPEN_BRACKETS THEORY symbol theory_attribute+ CLOSE_BRACKETS
    ;

// Logic Declarations
logic_attribue
    : THEORIES OPEN_BRACKETS symbol+ CLOSE_BRACKETS
    | LANGUAGE STRING
    | EXTENSION STRING
    | VALUES STRING
    | NOTES STRING
    | attribute
    ;

logic
    : OPEN_BRACKETS LOGIC symbol logic_attribue+ CLOSE_BRACKETS
    ;

// Scripts
sort_dec
    : OPEN_BRACKETS symbol NUMERALS CLOSE_BRACKETS
    ;

selector_dec
    : OPEN_BRACKETS symbol sort CLOSE_BRACKETS
    ;

constructor_dec
    : OPEN_BRACKETS symbol selector_dec* CLOSE_BRACKETS
    ;

datatype_dec
    : OPEN_BRACKETS constructor_dec+ CLOSE_BRACKETS
    | OPEN_BRACKETS PAR OPEN_BRACKETS symbol+ CLOSE_BRACKETS OPEN_BRACKETS constructor_dec+
    CLOSE_BRACKETS CLOSE_BRACKETS
    ;

function_dec
    : OPEN_BRACKETS symbol OPEN_BRACKETS sorted_var* CLOSE_BRACKETS sort CLOSE_BRACKETS
    ;

function_def
    : symbol OPEN_BRACKETS sorted_var* CLOSE_BRACKETS sort term
    ;

prop_literal
    : symbol
    | OPEN_BRACKETS NOT symbol CLOSE_BRACKETS
    ;

script
    : command*
    ;

command
    : OPEN_BRACKETS ASSERT term CLOSE_BRACKETS
    | OPEN_BRACKETS CHECK_SAT CLOSE_BRACKETS
    | OPEN_BRACKETS CHECK_SAT_ASSUMING CLOSE_BRACKETS
    | OPEN_BRACKETS DECLARE_CONST symbol sort CLOSE_BRACKETS
    | OPEN_BRACKETS DECLARE_DATATYPE symbol datatype_dec CLOSE_BRACKETS
    | OPEN_BRACKETS DECLARE_DATATYPES OPEN_BRACKETS sort_dec+ CLOSE_BRACKETS OPEN_BRACKETS
    datatype_dec+ CLOSE_BRACKETS CLOSE_BRACKETS
    | OPEN_BRACKETS DECLARE_FUNCTION symbol OPEN_BRACKETS sort* CLOSE_BRACKETS sort CLOSE_BRACKETS
    | OPEN_BRACKETS DECLARE_SORT symbol NUMERALS CLOSE_BRACKETS
    | OPEN_BRACKETS DEFINE_FUNCTION function_def CLOSE_BRACKETS
    | OPEN_BRACKETS DEFINE_FUNCTION_REC function_def CLOSE_BRACKETS
    | OPEN_BRACKETS DEFINE_FUNTIONS_REC OPEN_BRACKETS function_dec+ CLOSE_BRACKETS
    OPEN_BRACKETS term+ CLOSE_BRACKETS CLOSE_BRACKETS
    | OPEN_BRACKETS DEFINE_SORT symbol OPEN_BRACKETS symbol* CLOSE_BRACKETS sort CLOSE_BRACKETS
    | OPEN_BRACKETS ECHO STRING CLOSE_BRACKETS
    | OPEN_BRACKETS EXIT CLOSE_BRACKETS
    | OPEN_BRACKETS GET_ASSERTIONS CLOSE_BRACKETS
    | OPEN_BRACKETS GET_ASSIGNMENT CLOSE_BRACKETS
    | OPEN_BRACKETS GET_INFO info_flag CLOSE_BRACKETS
    | OPEN_BRACKETS GET_MODEL CLOSE_BRACKETS
    | OPEN_BRACKETS GET_OPTION keyword CLOSE_BRACKETS
    | OPEN_BRACKETS GET_PROOF CLOSE_BRACKETS
    | OPEN_BRACKETS GET_UNSAT_ASSUMPTIONS CLOSE_BRACKETS
    | OPEN_BRACKETS GET_UNSAT_CORE CLOSE_BRACKETS
    | OPEN_BRACKETS GET_VALUE OPEN_BRACKETS term+ CLOSE_BRACKETS CLOSE_BRACKETS
    | OPEN_BRACKETS POP NUMERALS CLOSE_BRACKETS
    | OPEN_BRACKETS PUSH NUMERALS CLOSE_BRACKETS
    | OPEN_BRACKETS RESET CLOSE_BRACKETS
    | OPEN_BRACKETS RESET_ASSERTIONS CLOSE_BRACKETS
    | OPEN_BRACKETS SET_INFO attribute CLOSE_BRACKETS
    | OPEN_BRACKETS SET_LOGIC symbol CLOSE_BRACKETS
    | OPEN_BRACKETS SET_OPTION option CLOSE_BRACKETS
    ;

b_value
    : TRUE
    | FALSE
    ;

option
    : DIAGNOSTIC_OUTPUT_CHANNEL STRING
    | GLOBAL_DECLARATIONS b_value
    | INTERACTIVE_MODE b_value
    | PRINT_SUCCESS b_value
    | PRODUCE_ASSERTIONS b_value
    | PRODUCE_ASSIGNMENTS b_value
    | PRODUCE_MODELS b_value
    | PRODUCE_PROOFS b_value
    | PRODUCE_UNSAT_ASSUMPTIONS b_value
    | PRODUCE_UNSAT_CORES b_value
    | RANDOM_SEED NUMERALS
    | REGULAR_OUTPUT_CHANNEL STRING
    | REPRODUCIBLE_RESOURCE_LIMIT NUMERALS
    | VERBOSITY NUMERALS
    | attribute
    ;

info_flag
    : ALL_STATISTICS
    | ASSERTION_STACK_LEVELS
    | AUTHORS
    | ERROR_BEHAVIOUR
    | NAME
    | REASON_UNKNOWN
    | VERSION
    | keyword
    ;

// responses
error_behaviour
    : IMMEDIATE_EXIT
    | CONTINUED_EXECUTION
    ;

reason_unknown
    : MEMOUT
    | INCOMPLETE
    | s_expr
    ;

model_response
    : OPEN_BRACKETS DEFINE_FUNCTION function_def CLOSE_BRACKETS
    | OPEN_BRACKETS DEFINE_FUNCTION_REC function_def CLOSE_BRACKETS
    // cardinalitiees for function_dec and term have to be n+1
    | OPEN_BRACKETS DEFINE_FUNTIONS_REC OPEN_BRACKETS function_dec+ CLOSE_BRACKETS OPEN_BRACKETS term+
    CLOSE_BRACKETS CLOSE_BRACKETS
    ;

info_response
    : ASSERTION_STACK_LEVELS NUMERALS
    | AUTHORS STRING
    | ERROR_BEHAVIOUR error_behaviour
    | NAME STRING
    | REASON_UNKNOWN reason_unknown
    | VERSION STRING
    | attribute
    ;

valuation_pair
    : OPEN_BRACKETS term term CLOSE_BRACKETS
    ;

t_valuation_pair
    : OPEN_BRACKETS symbol b_value CLOSE_BRACKETS
    ;

check_sat_response
    : SAT
    | UNSAT
    | UNKNOWN
    ;

echo_response
    : STRING
    ;

get_assertions_response
    : OPEN_BRACKETS term* CLOSE_BRACKETS
    ;

get_assignment_response
    : OPEN_BRACKETS t_valuation_pair* CLOSE_BRACKETS
    ;

get_info_response
    : OPEN_BRACKETS info_response+ CLOSE_BRACKETS
    ;

get_model_response
    : OPEN_BRACKETS model_response* CLOSE_BRACKETS
    ;

get_option_response
    : attribute_value
    ;

get_proof_response
    : s_expr
    ;

get_unsat_assump_response
    : OPEN_BRACKETS symbol* CLOSE_BRACKETS
    ;

get_unsat_core_response
    : OPEN_BRACKETS symbol* CLOSE_BRACKETS
    ;

get_value_response
    : OPEN_BRACKETS valuation_pair+ CLOSE_BRACKETS
    ;

specific_success_response
    : check_sat_response
    | echo_response
    | get_assertions_response
    | get_assignment_response
    | get_info_response
    | get_model_response
    | get_option_response
    | get_proof_response
    | get_unsat_assump_response
    | get_unsat_core_response
    | get_value_response
    ;

general_response
    : SUCCESS
    | specific_success_response
    | UNSUPPORTED
    | OPEN_BRACKETS ERROR STRING CLOSE_BRACKETS
    ;

// Defining Lexer Rules
// Fragments
fragment HEX_DIGITS   : '0' .. '9' | 'a' .. 'f' | 'A' .. 'F';
fragment DIGITS  : [0-9];
fragment SYMBOLS
    : 'a'..'z'
    | 'A' .. 'Z'
    | '+'
    | '='
    | '/'
    | '*'
    | '%'
    | '?'
    | '!'
    | '$'
    | '-'
    | '_'
    | '~'
    | '&'
    | '^'
    | '<'
    | '>'
    | '@'
    | '.'
    ;
fragment BINARY_DIGITS  : [01];
fragment PRINTABLE_CHAR
    : '\u0020' .. '\u007E'
    | '\u0080' .. '\uffff'
    | ESCAPED_SPACE
    ;
fragment PRINTABLE_CHAR_NO_DQUOTE
    : '\u0020' .. '\u0021'
    | '\u0023' .. '\u007E'
    | '\u0080' .. '\uffff'
    | ESCAPED_SPACE
    ;
fragment PRINTABLE_CHAR_NO_BACKSLASH
    : '\u0041' .. '\u005A'
    | '\u0061' .. '\u007A'
    | '\u005F'
    ;
fragment ESCAPED_SPACE   : '""';
fragment WHITE_SPACE_CHAR
    : '\u0009'
    | '\u000A'
    | '\u000D'
    | '\u0020'
    ;

// Predefined Symbols
NOT  : 'not';
BOOL : 'Bool';
CONTINUED_EXECUTION  : 'continued-execution';
ERROR    : 'error';
FALSE    : 'false';
IMMEDIATE_EXIT   : 'immediate-exit';
INCOMPLETE   : 'incomplete';
LOGIC    : 'logic';
MEMOUT   : 'memout';
SAT  : 'sat';
SUCCESS  : 'success';
THEORY   : 'theory';
TRUE    : 'true';
UNKNOWN : 'unknown';
UNSUPPORTED : 'unsupported';
UNSAT   : 'unsat';
OPEN_BRACKETS    : '(';
CLOSE_BRACKETS   : ')';
SEMICOLON   : ';';
COLON   : ':';
EXCLAMATION : '!';
UNDERSCORE  : '_';
AS  : 'as';
FOR_ALL : 'forall';
LET : 'let';
EXISTS  : 'exists';
PAR : 'par';

// Keywords
BINARY_WORD  : 'BINARY';
DECIMAL_WORD : 'DECIMAL';
HEXA_DECIMAL_WORD    : 'HEXADECIMAL';
NUMERAL_WORD : 'NUMERAL';
MATCH_WORD   : 'match';
STRING_WORD : 'string';

STRING  : '"' (PRINTABLE_CHAR_NO_DQUOTE | WHITE_SPACE_CHAR) + '"';
QUOTED_SYMBOL    : PRINTABLE_CHAR_NO_BACKSLASH | WHITE_SPACE_CHAR;

// Predefined Keywords
ALL_STATISTICS    : ':all-statistics';
ASSERTION_STACK_LEVELS  : ':assertion-stack-levels';
AUTHORS : ':authors';
CATEGORY    : ':category';
CHAINABLE   : ':chainable';
DEFINITION  : ':definition';
DIAGNOSTIC_OUTPUT_CHANNEL   : ':diagnostic-output-channel';
ERROR_BEHAVIOUR : ':error-behavior';
EXTENSION   : ':extensions';
FUNCTIONS   : ':funs';
FUNCTIONS_DESCRIPTION   : ':funs-description';
GLOBAL_DECLARATIONS   : ':global-declarations';
INTERACTIVE_MODE  : ':interactive-mode';
LANGUAGE : ':language';
LEFT_ASSOC    : ':left-assoc';
LICENSE  : ':license';
NAMED    : ':named';
NAME    : ':name';
NOTES    : ':notes';
PATTERN : ':pattern';
PRINT_SUCCESS : ':print-success';
PRODUCE_ASSERTIONS    : ':produce-assertions';
PRODUCE_ASSIGNMENTS   : ':produce-assignments';
PRODUCE_MODELS    : ':produce-models';
PRODUCE_PROOFS    : ':produce-proofs';
PRODUCE_UNSAT_ASSUMPTIONS  : ':produce-unsat-assumptions';
PRODUCE_UNSAT_CORES    : ':produce-unsat-cores';
RANDOM_SEED   : ':random-seed';
REASON_UNKNOWN    : ':reason-unknown';
REGULAR_OUTPUT_CHANNEL : ':regular-output-channel';
REPRODUCIBLE_RESOURCE_LIMIT : ':reproducible-resource-limit';
RIGHT_ASSOC : ':right-assoc';
SMT_LIB_VERSION    : ':smt-lib-version';
SORTS    : ':sorts';
SORTS_DESCRIPTION : ':sorts-description';
SOURCE   : ':source';
STATUS   : ':status';
THEORIES : ':theories';
VALUES   : ':values';
VERBOSITY    : ':verbosity';
VERSION  : ':version';
UNDEFINED_SYMBOL :   SYMBOLS (DIGITS | SYMBOLS)*;

// Commands
ASSERT  : 'assert';
CHECK_SAT   : 'check-sat';
CHECK_SAT_ASSUMING    : 'check-sat-assuming';
DECLARE_CONST    : 'declare-const';
DECLARE_DATATYPE    : 'declare-datatype';
DECLARE_DATATYPES   : 'declare-datatypes';
DECLARE_FUNCTION    : 'declare-fun';
DECLARE_SORT    : 'declare-sort';
DEFINE_FUNCTION : 'define-fun';
DEFINE_FUNCTION_REC : 'define-fun-rec';
DEFINE_FUNTIONS_REC : 'define-funs-rec';
DEFINE_SORT : 'define-sort';
ECHO    : 'echo';
EXIT    : 'exit';
GET_ASSERTIONS  : 'get-assertions';
GET_ASSIGNMENT  : 'get-assignment';
GET_INFO    : 'get-info';
GET_MODEL   : 'get-model';
GET_OPTION  : 'get-option';
GET_PROOF   : 'get-proof';
GET_UNSAT_ASSUMPTIONS   : 'get-unsat-assumptions';
GET_UNSAT_CORE  : 'get-unsat-core';
GET_VALUE   : 'get-value';
POP : 'pop';
PUSH    : 'push';
RESET   : 'reset';
RESET_ASSERTIONS    : 'reset-assertions';
SET_INFO    : 'set-info';
SET_LOGIC   : 'set-logic';
SET_OPTION  : 'set-option';

NUMERALS
    : '0'
    | [1-9] DIGITS*
    ;
BINARY  : '#b' BINARY_DIGITS+;
HEXA_DECIMAL  : '#x' HEX_DIGITS+;
DECIMAL : NUMERALS '.' '0'* NUMERALS;

WS  : [ \t\r\n]+ -> skip;
COMMENT : SEMICOLON ~[\r\n]* -> skip;