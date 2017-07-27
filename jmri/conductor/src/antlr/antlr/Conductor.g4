grammar Conductor;

script     : scriptLine ( EOL scriptLine )* EOL? EOF;
scriptLine : ( defLine | eventLine )? SB_COMMENT?;

defLine: defStrLine | defIntLine | defThrottleLine | defEnumLine | defMapLine | defRouteLine;

defStrLine: defStrType ID '=' ID;
defStrType: KW_SENSOR | KW_TURNOUT;

defIntLine: defIntType ID '=' NUM;
defIntType: KW_VAR  | KW_TIMER;

defThrottleLine: KW_THROTTLE ID '=' NUM+;

defEnumLine: KW_ENUM ID '=' defEnumValues;
defEnumValues: ( ID )+;

defMapLine: KW_MAP ID '=' STR;

defRouteLine:  KW_ROUTE ID '=' routeInfoList;
routeInfoList: routeInfo ( ',' routeInfo )* ','? ;
routeInfo:     routeInfoOpId ':' ID | routeInfoOpNum ':' NUM;
routeInfoOpId: KW_TOGGLE | KW_STATUS;
routeInfoOpNum:KW_THROTTLE;

eventLine: condList '->' actionList;

condList:   cond ( '&' cond )* ;
cond:       condNot? ID condEnum? condThrottleOp? condTime? ;
condNot:    '!';
condTime:   '+' NUM;
condThrottleOp: KW_FORWARD | KW_REVERSE | KW_STOPPED | KW_SOUND | KW_LIGHT;
condEnum:   condEnumOp ID;
condEnumOp: KW_IS_EQ | KW_IS_NEQ;

actionList: action ( ';' action )* ';'? ;
action:     EOL? ( idAction | fnAction ) ;
idAction:   ID ( throttleOp | turnoutOp | timerOp )? funcValue? ;
fnAction:   KW_RESET KW_TIMERS;

throttleOp: KW_FORWARD | KW_REVERSE | KW_STOP | KW_SOUND | KW_LIGHT | KW_HORN | KW_FN;
turnoutOp:  KW_NORMAL ;  // KW_REVERSE is captured by throttleOp.
timerOp:    KW_START | KW_END;

funcValue:  '=' ( NUM | ID ) ;


WS:  [ \t\u000C]+ -> skip ;   // from https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
EOL: [\r\n]+ ;

SB_COMMENT: '#' ~[\r\n]*;

KW_ARROW:   '->';
KW_IS_EQ:   '==';
KW_IS_NEQ:  '!=';
KW_EQUAL:   '=';
KW_AND:     '&';
KW_NOT:     '!';
KW_PLUS:    '+';
KW_SEMI:    ';';

// Keywords... defining them here mean they are not part of "ID" anymore.
// (ANTLR uses the grammar order... ID matches everything not defined here.)
// Note: we use a case-insensitive input stream which works by converting the input to
// lowercase so all the keywords here need to be lower case. However when the visitor
// uses ctx.getText(), it will get the original case of the source file.
KW_END:     'end';
KW_ENUM:    'enum';
KW_FORWARD: 'forward';
KW_HORN:    'horn';
KW_LIGHT:   'light';
KW_MAP:     'map';
KW_NORMAL:  'normal';
KW_RESET:   'reset';
KW_REVERSE: 'reverse';
KW_ROUTE:   'route';
KW_SENSOR:  'sensor';
KW_SOUND:   'sound';
KW_START:   'start';
KW_STATUS:  'status';
KW_STOP:    'stop';
KW_STOPPED: 'stopped';
KW_THROTTLE:'throttle';
KW_TIMER:   'timer';
KW_TIMERS:  'timers';
KW_TOGGLE:  'toggle';
KW_TURNOUT: 'turnout';
KW_VAR:     'var';
KW_FN:      KW_F0 | KW_F10 | KW_F20 ;
fragment KW_F0 :      'f'  [0-9] ;
fragment KW_F10:      'f1' [0-9] ;
fragment KW_F20:      'f2' [0-8] ;

// An ID. Can be 1 or more characters. Some specific non-alpha chars are accepted.
// An ID can contain a dash in the middle but not start or end with one (to avoid conflict with ->).
ID:      IdCharStart ( IdCharFull* IdCharLast )? ;
NUM:     IdNum+ ;      // An int literal
STR:     '"' ~["\r\n]* '"';

fragment IdCharStart: IdUnreserved | IdLetter ;
fragment IdCharFull:  IdUnreserved | IdLetter | IdNum | IdDash ;
fragment IdCharLast:  IdUnreserved | IdLetter | IdNum ;

// Unreserved characters can be used to start an ID or even be the whole 1-char ID
// ;:, are not allowed since they are separators and " is treated separately due to STR vs ID.
fragment IdUnreserved: [$?_*/.%^()\[\]{}\'`~] ;
fragment IdNum:        [0-9] ;
// Letter covers all unicode characters above 0xFF which are not a surrogate
// (syntax doesn't seem right so for now just accepting the tradional A-Z)
fragment IdLetter:     [a-zA-Z] ; // ~[\u0000-\u00FF\uD800-\uDBFF] ;
fragment IdDash: '-' ;
fragment IdDoubleQuote: '"';
