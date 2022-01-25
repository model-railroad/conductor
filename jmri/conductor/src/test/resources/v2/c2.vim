" Vim syntax file
" Language: Conductor 2
" Maintainer: alf.labs gmail com
" Latest Revision: 2022-01-24
"
" To install: cp/ln into ~/.vim/syntax/c2.vim
" Also install the ftdetect file. Then use :syntax on in the c2 file.

if exists("b:current_syntax")
  finish
endif

syn match c2BlockId /B\d\+[a-z]?/
syn match c2TurnoutId /T\d\+/
syn match c2Identifier /[a-zA-Z][a-zA-Z0-9-]*[a-zA-Z0-9]/
syn match c2Number /\d\+/
syn region c2String start=/"/ end=/"/
syn region c2InstBlock start=/{/ end=/}/ fold transparent

syn match c2Comment "#.*$"

syn keyword c1Keywords ->
syn keyword c1Keywords Sensor nextgroup=c2BlockId
syn keyword c1Keywords Turnout nextgroup=c2TurnoutId
syn keyword c1Keywords Map Enum Timer Int Throttle nextgroup=c2Identifier
syn keyword c1Keywords Start Reset
syn keyword c1Keywords Forward Reverse Stopped Stop Horn Sound Repeat ESTOP Normal
syn keyword c1SpecialVars GA-Event RTAC-PSA-Text
syn keyword c2Keywords Route ActiveRoute Function Block Enter Activate

hi def link c1Keywords     Keyword
hi def link c2Keywords     Keyword
hi def link c2BlockId     Identifier
hi def link c2TurnoutId   Identifier
hi def link c1SpecialVars   Identifier
hi def link c2Identifier   Type
hi def link c2InstBlock    Statement
hi def link c2Number      Number
hi def link c2String      String
hi def link c2Comment      Comment
