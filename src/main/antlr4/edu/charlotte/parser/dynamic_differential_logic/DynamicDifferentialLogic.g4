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
    | '!'formula
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
    | '(' term ')' # ParenthesesTerm
    ;

IDENTIFIER_PRIME    : [a-zA-Z][a-zA-Z0-9]* '\'';
IDENTIFIER  : [a-zA-Z][a-zA-Z0-9]*; // Matches assignment names and identifiers
NUMBER     : [0-9]+'.'[0-9]+;
NON_DET     : '**';
BOOLEANS    : 'true' | 'false';
COMPARISON_OPERATORS : '==' | '!=' | '<=' | '>=' | '<' | '>';
BINARY_EXPRESSION_OPERATORS : '+' | '-' | '*' | '/';
WS          : [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;