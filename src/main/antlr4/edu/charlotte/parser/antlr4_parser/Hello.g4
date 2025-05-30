grammar Hello;

start: greeting EOF;
greeting: 'Hello' ID;
ID: [a-zA-Z]+;
WS: [ \t\r\n]+ -> skip;