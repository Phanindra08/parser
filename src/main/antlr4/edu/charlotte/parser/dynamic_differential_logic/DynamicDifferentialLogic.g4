grammar DynamicDifferentialLogic;

dlProgram
    : formula EOF
    ;

program
    : IDENTIFIER DL_ASSIGNMENT_OPERATOR term DL_SEMICOLON
    | IDENTIFIER DL_ASSIGNMENT_OPERATOR NON_DET DL_SEMICOLON
    | program DL_SEMICOLON program
    | program DL_UNION_OPERATOR program
    | DL_OPEN_CURLY_BRACKETS program DL_CLOSE_CURLY_BRACKETS NON_DET
    | DL_TERNARY_OPERATOR formula DL_SEMICOLON
    | DL_OPEN_CURLY_BRACKETS differentialEquation (DL_COMMA differentialEquation)* DL_AND_OPERATOR formula DL_CLOSE_CURLY_BRACKETS
    ;

differentialEquation
    : IDENTIFIER_PRIME DL_DIFFERENTIAL_EQUAL_OPERATOR term
    ;

formula
    : term COMPARISON_OPERATORS term
    | BOOLEANS
    | DL_NOT_OPERATOR DL_OPEN_BRACKETS formula DL_CLOSE_BRACKETS
    | formula DL_AND_OPERATOR formula
    | formula DL_OR_OPERATOR formula
    | formula DL_IMPLICATION_OPERATOR formula
    | formula DL_BI_IMPLICATION_OPERATOR formula
    | DL_OPEN_MODALITY_OPERATOR program DL_CLOSE_MODALITY_OPERATOR formula
    | DL_OPEN_DIAMOND_MODALITY_OPERATOR program DL_CLOSE_DIAMOND_MODALITY_OPERATOR formula
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
NUMBER     : '-'? [0-9]+'.'[0-9]+;
NON_DET     : '**';
COMPARISON_OPERATORS : '==' | '!=' | '<=' | '>=' | '<' | '>';
BINARY_EXPRESSION_OPERATORS : '+' | '-' | '*' | '/';

DL_OPEN_BRACKETS : '(';
DL_CLOSE_BRACKETS : ')';
DL_ASSIGNMENT_OPERATOR : ':=';
DL_SEMICOLON : ';';
DL_UNION_OPERATOR : '++';
DL_OPEN_CURLY_BRACKETS : '{';
DL_CLOSE_CURLY_BRACKETS : '}';
DL_TERNARY_OPERATOR : '?';
DL_DIFFERENTIAL_EQUAL_OPERATOR : '=';
DL_AND_OPERATOR : '&&';
DL_OR_OPERATOR : '||';
DL_IMPLICATION_OPERATOR : '->';
DL_BI_IMPLICATION_OPERATOR : '<->';
DL_OPEN_MODALITY_OPERATOR : '[';
DL_CLOSE_MODALITY_OPERATOR : ']';
DL_OPEN_DIAMOND_MODALITY_OPERATOR : '<<';
DL_CLOSE_DIAMOND_MODALITY_OPERATOR : '>>';
DL_NOT_OPERATOR : '!';
DL_COMMA : ',';

WS          : [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;