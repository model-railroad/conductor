grammar Conductor;

script     : scriptLine ( EOL scriptLine )* EOL? EOF ;
scriptLine : ( defLine | eventLine )? SB_COMMENT? ;

defLine: defStrLine | defIntLine ;

defStrLine: defStrType ID '=' ID ;
defStrType: KW_SENSOR | KW_TURNOUT ;

defIntLine: defIntType ID '=' NUM;
defIntType: KW_THROTTLE | KW_VAR  | KW_TIMER ;


eventLine: condList '->' actionList;

condList:   cond ( '&' cond )* ;
cond:       condNot? ID ( condTime | condOp )? ;
condNot:    '!' ;
condTime:   '+' NUM ;
condOp:     KW_FORWARD | KW_REVERSE | KW_STOPPED ;

actionList: action ( ';' action )* ';'? ;
action:     ID ( throttleOp | turnoutOp | timerOp )? funcValue? ;

throttleOp: KW_FORWARD | KW_REVERSE | KW_SOUND | KW_HORN | KW_STOP | KW_FN;
turnoutOp:  KW_NORMAL;  // also KW_REVERSE as in throttleOp.
timerOp:    KW_START;

funcValue:  '=' ( NUM | ID ) ;

WS: [ \t\u000C]+ -> skip ;   // from https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
EOL:    [\r\n]+ ;

SB_COMMENT: '#' ~[\r\n]*;

// Keywords... defining them here mean they are not part of "ID" anymore.
// (ANTLR uses the grammar order... ID matches everything not defined here.)
// Note: we use a case-insensitive input stream which works by converting the input to
// lowercase so all the keywords here need to be lower case. However when the visitor
// uses ctx.getText(), it will get the original case of the source file.
KW_VAR:     'var';
KW_THROTTLE:'throttle';
KW_SENSOR:  'sensor';
KW_TURNOUT: 'turnout';
KW_TIMER:   'timer';
KW_FORWARD: 'forward';
KW_REVERSE: 'reverse';
KW_NORMAL:  'normal';
KW_SOUND:   'sound';
KW_LIGHT:   'light';
KW_HORN:    'horn';
KW_STOP:    'stop';
KW_STOPPED: 'stopped';
KW_START:   'start';
KW_FN:      'f' [0-9] [0-9]? ;

KW_ARROW:   '->';
KW_EQUAL:   '=';
KW_AND:     '&';
KW_NOT:     '!';
KW_PLUS:    '+';
KW_SEMI:    ';';

ID:       IdCharStart IdCharFull* | IdNum IdCharFull* IdCharStart IdCharFull* ;
NUM:      IdNum+ ;      // An int literal
RESERVED: [=&!+;# ] ;   // Reserve these so that they don't match below

// Unreserved characters can be used to start an ID or even be the whole 1-char ID
fragment IdUnreserved: [$?_:*/,.%^()\[\]{}\"\'`~] ;
fragment IdNum:        [0-9] ;
// Letter covers all unicode characters above 0xFF which are not a surrogate
fragment IdLetter:     [a-zA-Z] ~[\u0000-\u00FF\uD800-\uDBFF] ;

fragment IdCharStart: IdUnreserved | [a-zA-Z_] ;
fragment IdCharFull:  IdUnreserved | [a-zA-Z_] | IdNum | [-] ;

