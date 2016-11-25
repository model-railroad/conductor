grammar Conductor;

script     : scriptLine* EOF ;
scriptLine : ( defLine | eventLine )? eol ;

eol         : comment? EOL ;
comment     : SB_COMMENT ;

defLine: defType ID '=' NUM ;
defType: KW_VAR | KW_THROTTLE | KW_SENSOR | KW_TURNOUT | KW_TIMER ;

eventLine: condList '->' instList;

condList: cond ( '&' cond )* ;
cond: '!'? ID ( '+' NUM | cond_op )? ;
cond_op: KW_FORWARD | KW_REVERSE | KW_STOPPED ;

instList: inst ( ';' inst )* ';'? ;
inst: ID op? ( '=' ( NUM | ID ) )? ;

op: KW_FORWARD | KW_REVERSE | KW_NORMAL | KW_SOUND | KW_HORN | KW_STOP | KW_START | KW_FN;

WS: [ \t\u000C]+ -> skip ;   // from https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
EOL:    [\r\n]+ ;

SB_COMMENT: '#' ~[\r\n]*;

// keywords... defining them here mean they are not part of "ID" anymore.
KW_VAR:     'Var';
KW_THROTTLE:'Throttle';
KW_SENSOR:  'Sensor';
KW_TURNOUT: 'Turnout';
KW_TIMER:   'Timer';
KW_FORWARD: 'Forward';
KW_REVERSE: 'Reverse';
KW_NORMAL:  'Normal';
KW_SOUND:   'Sound';
KW_LIGHT:   'Light';
KW_HORN:    'Horn';
KW_STOP:    'Stop';
KW_STOPPED: 'Stopped';
KW_START:   'Start';
KW_FN:      'F' [0-9] [0-9]? ;

KW_ARROW:   '->';
KW_EQUAL:   '=';
KW_AND:     '&';
KW_NOT:     '!';
KW_PLUS:    '+';
KW_SEMI:    ';';

ID:       IdCharStart IdCharFull* | IdNum IdCharFull* IdCharStart IdCharFull* ;
NUM:      IdNum+;
RESERVED: [:#+-*/,.%^()\[\]{}\"\'`~ ] ;

fragment IdUnreserved: [!$&?_] ;
fragment IdNum:        [0-9] ;
// Letter covers all unicode characters above 0xFF which are not a surrogate
fragment IdLetter:     [a-zA-Z] ~[\u0000-\u00FF\uD800-\uDBFF] ;

fragment IdCharStart: IdUnreserved | [a-zA-Z_] ;
fragment IdCharFull:  IdUnreserved | [a-zA-Z_] | IdNum | [-] ;

