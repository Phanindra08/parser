grammar DynamicDifferentialLogic;

dlProgram
    : formula EOF
    ;

program
    : IDENTIFIER':='term';'
    | IDENTIFIER':='NON_DET';'
    | program';' program
    | program '++' program
    | '{'program'}'NON_DET
    | '?'formula';'
    | '{'IDENTIFIER_PRIME '='term '&&' formula'}'
    ;

formula
    : term COMPARISON_OPERATORS term
    | BOOLEANS
    | '!' DL_OPEN_BRACKETS formula DL_CLOSE_BRACKETS
    | formula '&&' formula
    | formula '||' formula
    | formula '->' formula
    | formula '<->' formula
    | '['program']'formula
    | '<<'program'>>'formula
    ;

term
    : IDENTIFIER   # AssignmentIdentifier
    | NUMBER      # IntegerLiteral
    | term BINARY_EXPRESSION_OPERATORS term # BinaryExpr
    | DL_OPEN_BRACKETS term DL_CLOSE_BRACKETS # ParenthesesTerm
    ;

BOOLEANS    : 'true' | 'false';
IDENTIFIER_PRIME    : [a-zA-Z][a-zA-Z0-9]* '\'';
IDENTIFIER  : [a-zA-Z][a-zA-Z0-9]*; // Matches assignment names and identifiers
NUMBER     : [0-9]+'.'[0-9]+;
NON_DET     : '**';
COMPARISON_OPERATORS : '==' | '!=' | '<=' | '>=' | '<' | '>';
BINARY_EXPRESSION_OPERATORS : '+' | '-' | '*' | '/';
DL_OPEN_BRACKETS : '(';
DL_CLOSE_BRACKETS : ')';
WS          : [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;