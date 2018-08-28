/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

grammar Conductor;

script     : scriptLine ( EOL scriptLine )* EOL? EOF;
scriptLine : ( defLine | eventLine )? SB_COMMENT?;

defLine: defIdLine | defStrLine | defIntLine | defThrottleLine | defEnumLine | defRouteLine | defGaIdLine ;

defGaIdLine: KW_GA_ID '=' STR;

defIdLine: defIdType ID '=' ID;
defIdType: KW_SENSOR | KW_TURNOUT;

defIntLine: defIntType ID '=' NUM;
defIntType: KW_EXPORT? KW_INT | KW_TIMER;

defStrLine: defStrType ID '=' ( STR | STR_BLOCK );
defStrType: KW_EXPORT? KW_STRING | KW_MAP;

defThrottleLine: KW_THROTTLE ID '=' NUM+;

defEnumLine: KW_EXPORT? KW_ENUM ID '=' defEnumValues;
defEnumValues: ( ID )+;

defRouteLine:  KW_ROUTE ID '=' routeInfoList;
routeInfoList: routeInfo ( ',' routeInfo )* ','? ;
routeInfo:     routeInfoOp ':' ID;
routeInfoOp:   KW_TOGGLE | KW_STATUS | KW_COUNTER | KW_THROTTLE;

eventLine: condList '->' actionList;

condList:   cond ( '&' cond )* ;
cond:       condNot? ID condEnum? condThrottleOp? condTime? ;
condNot:    '!';
condTime:   '+' NUM;
condThrottleOp: KW_FORWARD | KW_REVERSE | KW_STOPPED | KW_SOUND | KW_LIGHT;
condEnum:   condEnumOp ID;
condEnumOp: KW_IS_EQ | KW_IS_NEQ;

actionList: action ( ';' action )* ';'? ;
action:     EOL? ( idAction | fnAction | gaAction ) ;
idAction:   ID ( throttleOp | turnoutOp | timerOp )? ( funcValue? | funcInt? ) ;
fnAction:   KW_RESET KW_TIMERS;

gaAction:   gaActionOp gaParamList;
gaActionOp: KW_GA_EVENT | KW_GA_PAGE;
gaParamList:gaParam ( ',' gaParam )* ;
gaParam:    gaParamOp ':' (ID | KW_STOP | KW_START);
gaParamOp:  KW_ACTION | KW_CATEGORY | KW_LABEL | KW_PATH | KW_URL | KW_USER;

throttleOp: KW_FORWARD | KW_REVERSE | KW_STOP | KW_SOUND | KW_LIGHT | KW_HORN | KW_FN | KW_REPEAT;
turnoutOp:  KW_NORMAL ;  // KW_REVERSE is captured by throttleOp.
timerOp:    KW_START | KW_END;

funcValue:  '='  ( NUM | ID ) ;
funcInt:    ( KW_INC | KW_DEC ) ( NUM | ID ) ;


WS:  [ \t\u000C]+ -> skip ;   // from https://github.com/antlr/grammars-v4/blob/master/java/Java.g4
EOL: [\r\n]+ ;

SB_COMMENT: '#' ~[\r\n]*;

KW_ARROW:   '->';
KW_IS_EQ:   '==';
KW_IS_NEQ:  '!=';
KW_INC:     '+=';
KW_DEC:     '-=';
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
KW_ACTION:  'action';
KW_CATEGORY:'category';
KW_COUNTER: 'counter';
KW_END:     'end';
KW_ENUM:    'enum';
KW_EXPORT:  'export';
KW_FORWARD: 'forward';
KW_GA_EVENT:'ga-event';
KW_GA_PAGE: 'ga-page';
KW_GA_ID:   'ga-tracking-id';
KW_HORN:    'horn';
KW_INT:     'int';
KW_LABEL:   'label';
KW_LIGHT:   'light';
KW_MAP:     'map';
KW_NORMAL:  'normal';
KW_PATH:    'path';
KW_REPEAT:  'repeat';
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
KW_STRING:  'string';
KW_URL:     'url';
KW_USER:    'user';
KW_FN:      KW_F0 | KW_F10 | KW_F20 ;
fragment KW_F0 :      'f'  [0-9] ;
fragment KW_F10:      'f1' [0-9] ;
fragment KW_F20:      'f2' [0-8] ;

// An ID. Can be 1 or more characters. Some specific non-alpha chars are accepted.
// An ID can contain a dash in the middle but not start or end with one (to avoid conflict with ->).
ID:      IdCharStart ( IdCharFull* IdCharLast )? ;
NUM:     IdNum+ ;      // An *positive* int literal
STR:     '"' ~["\r\n]* '"';
STR_BLOCK: '\'\'\'' ~[']* '\'\'\'';

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
