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
cond:       condNot? ID ( condTime | condThrottleOp )? ;
condNot:    '!' ;
condTime:   '+' NUM ;
condThrottleOp: KW_FORWARD | KW_REVERSE | KW_STOPPED | KW_SOUND | KW_LIGHT ;

actionList: action ( ';' action )* ';'? ;
action:     ID ( throttleOp | turnoutOp | timerOp )? funcValue? ;

throttleOp: KW_FORWARD | KW_REVERSE | KW_STOP | KW_SOUND | KW_LIGHT | KW_HORN | KW_FN;
turnoutOp:  KW_NORMAL;  // also KW_REVERSE as in throttleOp.
timerOp:    KW_START;

funcValue:  '=' ( NUM | ID ) ;


WS:  [ \t\u000C]+ -> skip ;   // from https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
EOL: [\r\n]+ ;

SB_COMMENT: '#' ~[\r\n]*;

KW_ARROW:   '->';
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

ID:       IdCharStart IdCharFull* ;
NUM:      IdNum+ ;      // An int literal

fragment IdCharStart: IdUnreserved | IdLetter ;
fragment IdCharFull:  IdUnreserved | IdLetter | IdNum | IdDash ;

// Unreserved characters can be used to start an ID or even be the whole 1-char ID
fragment IdUnreserved: [$?_:*/,.%^()\[\]{}\"\'`~] ;
fragment IdNum:        [0-9] ;
// Letter covers all unicode characters above 0xFF which are not a surrogate
// (syntax doesn't seem right so for now just accepting the tradional A-Z)
fragment IdLetter:     [a-zA-Z] ; // ~[\u0000-\u00FF\uD800-\uDBFF] ;
// Semantic Predicate in ANTLR4. '-' is OK in an ID if not followed by a chevron.
// fragment IdDash: {_input.LA(1) != '-' }? '-' ;
// But that doesn't seem to work so for now -> needs a space before to resolve ambiguity.
fragment IdDash: '-' ;
