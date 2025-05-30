grammar BasicArithmetic;

compilationUnit
    : packageDeclaration? (importDeclaration)* (typeDeclaration)* EOF
    ;

packageDeclaration
    : 'package' qualifiedName ';'
    ;

importDeclaration
    : 'import' qualifiedName ('.' '*')? ';'
    ;

typeDeclaration
    : classOrMethodModifiers* (
        classDeclaration
    )
    ;

classOrMethodModifiers
    : 'public'
    | 'protected'
    | 'private'
    | 'static'
    | 'abstract'
    | 'final'
    ;

classDeclaration
    : 'class' IDENTIFIER
    classBody
    ;

classBody
    : '{' classBodyDeclaration* '}'
    ;

classBodyDeclaration
    : methodDeclaration
    | fieldDeclaration
    ;

methodDeclaration
    : methodModifiers* (primitiveType | 'void') IDENTIFIER formalParameters methodBody
    ;

methodModifiers
    : 'public'
    | 'private'
    | 'protected'
    | 'static'
    | 'abstract'
    | 'final'
    | 'default'
    ;

fieldDeclaration
    : primitiveType variableDeclarators ';'
    ;

variableDeclarators
    : IDENTIFIER (('=' expression) | (',' IDENTIFIER)*)
    ;

variableInitialization
    : NUMBERS
    | LETTERS
    | BOOLEAN
    | NUMBERS ('+' | '-' | '*' | '/') NUMBERS
    ;

methodBody
    : '{' (fieldDeclaration | ifStatement | expressionStatement)* '}'
    ;

expressionStatement
    : expression ';'
    ;

ifStatement
    : 'if' '(' ifExpression ')' methodBody ('else' methodBody)? # IfElse
    ;

ifExpression
    : methodCall
    | BOOLEAN
    | expression ('==' | '!=' | '<' | '<=' | '>' | '>=') expression
    ;

expression
    : IDENTIFIER '=' expression # AssignmentExpression
    | methodCall    # MethodCallExpression
    | IDENTIFIER   # Variable
    | NUMBERS      # IntegerLiteral
    | BOOLEAN      # BooleanLiteral
    | STRING       # StringLiteral
    | expression ('+' | '-' | '*' | '/') expression # BinaryExpr
    | '(' expression ')' # ParenthesizedExpression
    ;

argumentList
    : expression (',' expression)*
    ;

methodCall
    : qualifiedName '(' argumentList? ')'
    ;

primitiveType
    : 'boolean'
    | 'char'
    | 'byte'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    | 'String'
    ;

qualifiedName
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

formalParameters
    : '(' (formalParameter (',' formalParameter)*)? ')' # Parameters
    ;

formalParameter
    : primitiveType ('[' ']')* IDENTIFIER # Parameter
    ;

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*; // Matches variable names and identifiers
LETTERS: [a-zA-Z];
NUMBERS: [0-9]+('.'[0-9]+)?;
BOOLEAN: 'true' | 'false';
STRING: '"' (~["\r\n])* '"';
WS     : [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;