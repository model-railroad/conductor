// Generated from C:\dev\ralf\RalfDev\bitbucket\ralfoide\randall-layout\jmri\conductor\src\antlr\antlr\Conductor.g4 by ANTLR 4.5.3
package com.alflabs.conductor.parser2;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ConductorParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WS=1, EOL=2, SB_COMMENT=3, KW_ARROW=4, KW_IS_EQ=5, KW_IS_NEQ=6, KW_EQUAL=7, 
		KW_AND=8, KW_NOT=9, KW_PLUS=10, KW_SEMI=11, KW_MAP=12, KW_VAR=13, KW_ENUM=14, 
		KW_THROTTLE=15, KW_SENSOR=16, KW_TURNOUT=17, KW_TIMER=18, KW_FORWARD=19, 
		KW_REVERSE=20, KW_NORMAL=21, KW_SOUND=22, KW_LIGHT=23, KW_HORN=24, KW_STOP=25, 
		KW_STOPPED=26, KW_START=27, KW_END=28, KW_RESET=29, KW_TIMERS=30, KW_FN=31, 
		ID=32, NUM=33, STR=34;
	public static final int
		RULE_script = 0, RULE_scriptLine = 1, RULE_defLine = 2, RULE_defStrLine = 3, 
		RULE_defStrType = 4, RULE_defIntLine = 5, RULE_defIntType = 6, RULE_defThrottleLine = 7, 
		RULE_defEnumLine = 8, RULE_defEnumValues = 9, RULE_defMapLine = 10, RULE_eventLine = 11, 
		RULE_condList = 12, RULE_cond = 13, RULE_condNot = 14, RULE_condTime = 15, 
		RULE_condThrottleOp = 16, RULE_condEnum = 17, RULE_condEnumOp = 18, RULE_actionList = 19, 
		RULE_action = 20, RULE_idAction = 21, RULE_fnAction = 22, RULE_throttleOp = 23, 
		RULE_turnoutOp = 24, RULE_timerOp = 25, RULE_funcValue = 26;
	public static final String[] ruleNames = {
		"script", "scriptLine", "defLine", "defStrLine", "defStrType", "defIntLine", 
		"defIntType", "defThrottleLine", "defEnumLine", "defEnumValues", "defMapLine", 
		"eventLine", "condList", "cond", "condNot", "condTime", "condThrottleOp", 
		"condEnum", "condEnumOp", "actionList", "action", "idAction", "fnAction", 
		"throttleOp", "turnoutOp", "timerOp", "funcValue"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'->'", "'=='", "'!='", "'='", "'&'", "'!'", "'+'", 
		"';'", "'map'", "'var'", "'enum'", "'throttle'", "'sensor'", "'turnout'", 
		"'timer'", "'forward'", "'reverse'", "'normal'", "'sound'", "'light'", 
		"'horn'", "'stop'", "'stopped'", "'start'", "'end'", "'reset'", "'timers'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_IS_EQ", "KW_IS_NEQ", 
		"KW_EQUAL", "KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "KW_MAP", "KW_VAR", 
		"KW_ENUM", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", "KW_TIMER", "KW_FORWARD", 
		"KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", "KW_HORN", "KW_STOP", 
		"KW_STOPPED", "KW_START", "KW_END", "KW_RESET", "KW_TIMERS", "KW_FN", 
		"ID", "NUM", "STR"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Conductor.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ConductorParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ScriptContext extends ParserRuleContext {
		public List<ScriptLineContext> scriptLine() {
			return getRuleContexts(ScriptLineContext.class);
		}
		public ScriptLineContext scriptLine(int i) {
			return getRuleContext(ScriptLineContext.class,i);
		}
		public TerminalNode EOF() { return getToken(ConductorParser.EOF, 0); }
		public List<TerminalNode> EOL() { return getTokens(ConductorParser.EOL); }
		public TerminalNode EOL(int i) {
			return getToken(ConductorParser.EOL, i);
		}
		public ScriptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_script; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterScript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitScript(this);
		}
	}

	public final ScriptContext script() throws RecognitionException {
		ScriptContext _localctx = new ScriptContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_script);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			scriptLine();
			setState(59);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(55);
					match(EOL);
					setState(56);
					scriptLine();
					}
					} 
				}
				setState(61);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(63);
			_la = _input.LA(1);
			if (_la==EOL) {
				{
				setState(62);
				match(EOL);
				}
			}

			setState(65);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScriptLineContext extends ParserRuleContext {
		public DefLineContext defLine() {
			return getRuleContext(DefLineContext.class,0);
		}
		public EventLineContext eventLine() {
			return getRuleContext(EventLineContext.class,0);
		}
		public TerminalNode SB_COMMENT() { return getToken(ConductorParser.SB_COMMENT, 0); }
		public ScriptLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scriptLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterScriptLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitScriptLine(this);
		}
	}

	public final ScriptLineContext scriptLine() throws RecognitionException {
		ScriptLineContext _localctx = new ScriptLineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_scriptLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			switch (_input.LA(1)) {
			case KW_MAP:
			case KW_VAR:
			case KW_ENUM:
			case KW_THROTTLE:
			case KW_SENSOR:
			case KW_TURNOUT:
			case KW_TIMER:
				{
				setState(67);
				defLine();
				}
				break;
			case KW_NOT:
			case ID:
				{
				setState(68);
				eventLine();
				}
				break;
			case EOF:
			case EOL:
			case SB_COMMENT:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(72);
			_la = _input.LA(1);
			if (_la==SB_COMMENT) {
				{
				setState(71);
				match(SB_COMMENT);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefLineContext extends ParserRuleContext {
		public DefStrLineContext defStrLine() {
			return getRuleContext(DefStrLineContext.class,0);
		}
		public DefIntLineContext defIntLine() {
			return getRuleContext(DefIntLineContext.class,0);
		}
		public DefThrottleLineContext defThrottleLine() {
			return getRuleContext(DefThrottleLineContext.class,0);
		}
		public DefEnumLineContext defEnumLine() {
			return getRuleContext(DefEnumLineContext.class,0);
		}
		public DefMapLineContext defMapLine() {
			return getRuleContext(DefMapLineContext.class,0);
		}
		public DefLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefLine(this);
		}
	}

	public final DefLineContext defLine() throws RecognitionException {
		DefLineContext _localctx = new DefLineContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_defLine);
		try {
			setState(79);
			switch (_input.LA(1)) {
			case KW_SENSOR:
			case KW_TURNOUT:
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				defStrLine();
				}
				break;
			case KW_VAR:
			case KW_TIMER:
				enterOuterAlt(_localctx, 2);
				{
				setState(75);
				defIntLine();
				}
				break;
			case KW_THROTTLE:
				enterOuterAlt(_localctx, 3);
				{
				setState(76);
				defThrottleLine();
				}
				break;
			case KW_ENUM:
				enterOuterAlt(_localctx, 4);
				{
				setState(77);
				defEnumLine();
				}
				break;
			case KW_MAP:
				enterOuterAlt(_localctx, 5);
				{
				setState(78);
				defMapLine();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefStrLineContext extends ParserRuleContext {
		public DefStrTypeContext defStrType() {
			return getRuleContext(DefStrTypeContext.class,0);
		}
		public List<TerminalNode> ID() { return getTokens(ConductorParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ConductorParser.ID, i);
		}
		public DefStrLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defStrLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefStrLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefStrLine(this);
		}
	}

	public final DefStrLineContext defStrLine() throws RecognitionException {
		DefStrLineContext _localctx = new DefStrLineContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_defStrLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			defStrType();
			setState(82);
			match(ID);
			setState(83);
			match(KW_EQUAL);
			setState(84);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefStrTypeContext extends ParserRuleContext {
		public TerminalNode KW_SENSOR() { return getToken(ConductorParser.KW_SENSOR, 0); }
		public TerminalNode KW_TURNOUT() { return getToken(ConductorParser.KW_TURNOUT, 0); }
		public DefStrTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defStrType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefStrType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefStrType(this);
		}
	}

	public final DefStrTypeContext defStrType() throws RecognitionException {
		DefStrTypeContext _localctx = new DefStrTypeContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_defStrType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			_la = _input.LA(1);
			if ( !(_la==KW_SENSOR || _la==KW_TURNOUT) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefIntLineContext extends ParserRuleContext {
		public DefIntTypeContext defIntType() {
			return getRuleContext(DefIntTypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
		public DefIntLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defIntLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefIntLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefIntLine(this);
		}
	}

	public final DefIntLineContext defIntLine() throws RecognitionException {
		DefIntLineContext _localctx = new DefIntLineContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_defIntLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			defIntType();
			setState(89);
			match(ID);
			setState(90);
			match(KW_EQUAL);
			setState(91);
			match(NUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefIntTypeContext extends ParserRuleContext {
		public TerminalNode KW_VAR() { return getToken(ConductorParser.KW_VAR, 0); }
		public TerminalNode KW_TIMER() { return getToken(ConductorParser.KW_TIMER, 0); }
		public DefIntTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defIntType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefIntType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefIntType(this);
		}
	}

	public final DefIntTypeContext defIntType() throws RecognitionException {
		DefIntTypeContext _localctx = new DefIntTypeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_defIntType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			_la = _input.LA(1);
			if ( !(_la==KW_VAR || _la==KW_TIMER) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefThrottleLineContext extends ParserRuleContext {
		public TerminalNode KW_THROTTLE() { return getToken(ConductorParser.KW_THROTTLE, 0); }
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public List<TerminalNode> NUM() { return getTokens(ConductorParser.NUM); }
		public TerminalNode NUM(int i) {
			return getToken(ConductorParser.NUM, i);
		}
		public DefThrottleLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defThrottleLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefThrottleLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefThrottleLine(this);
		}
	}

	public final DefThrottleLineContext defThrottleLine() throws RecognitionException {
		DefThrottleLineContext _localctx = new DefThrottleLineContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_defThrottleLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			match(KW_THROTTLE);
			setState(96);
			match(ID);
			setState(97);
			match(KW_EQUAL);
			setState(99); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(98);
				match(NUM);
				}
				}
				setState(101); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NUM );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefEnumLineContext extends ParserRuleContext {
		public TerminalNode KW_ENUM() { return getToken(ConductorParser.KW_ENUM, 0); }
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public DefEnumValuesContext defEnumValues() {
			return getRuleContext(DefEnumValuesContext.class,0);
		}
		public DefEnumLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defEnumLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefEnumLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefEnumLine(this);
		}
	}

	public final DefEnumLineContext defEnumLine() throws RecognitionException {
		DefEnumLineContext _localctx = new DefEnumLineContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_defEnumLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(KW_ENUM);
			setState(104);
			match(ID);
			setState(105);
			match(KW_EQUAL);
			setState(106);
			defEnumValues();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefEnumValuesContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(ConductorParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ConductorParser.ID, i);
		}
		public DefEnumValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defEnumValues; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefEnumValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefEnumValues(this);
		}
	}

	public final DefEnumValuesContext defEnumValues() throws RecognitionException {
		DefEnumValuesContext _localctx = new DefEnumValuesContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_defEnumValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(108);
				match(ID);
				}
				}
				setState(111); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ID );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefMapLineContext extends ParserRuleContext {
		public TerminalNode KW_MAP() { return getToken(ConductorParser.KW_MAP, 0); }
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public TerminalNode STR() { return getToken(ConductorParser.STR, 0); }
		public DefMapLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defMapLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefMapLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefMapLine(this);
		}
	}

	public final DefMapLineContext defMapLine() throws RecognitionException {
		DefMapLineContext _localctx = new DefMapLineContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_defMapLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			match(KW_MAP);
			setState(114);
			match(ID);
			setState(115);
			match(KW_EQUAL);
			setState(116);
			match(STR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EventLineContext extends ParserRuleContext {
		public CondListContext condList() {
			return getRuleContext(CondListContext.class,0);
		}
		public ActionListContext actionList() {
			return getRuleContext(ActionListContext.class,0);
		}
		public EventLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eventLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterEventLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitEventLine(this);
		}
	}

	public final EventLineContext eventLine() throws RecognitionException {
		EventLineContext _localctx = new EventLineContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_eventLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			condList();
			setState(119);
			match(KW_ARROW);
			setState(120);
			actionList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondListContext extends ParserRuleContext {
		public List<CondContext> cond() {
			return getRuleContexts(CondContext.class);
		}
		public CondContext cond(int i) {
			return getRuleContext(CondContext.class,i);
		}
		public CondListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCondList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCondList(this);
		}
	}

	public final CondListContext condList() throws RecognitionException {
		CondListContext _localctx = new CondListContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_condList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			cond();
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KW_AND) {
				{
				{
				setState(123);
				match(KW_AND);
				setState(124);
				cond();
				}
				}
				setState(129);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public CondNotContext condNot() {
			return getRuleContext(CondNotContext.class,0);
		}
		public CondEnumContext condEnum() {
			return getRuleContext(CondEnumContext.class,0);
		}
		public CondThrottleOpContext condThrottleOp() {
			return getRuleContext(CondThrottleOpContext.class,0);
		}
		public CondTimeContext condTime() {
			return getRuleContext(CondTimeContext.class,0);
		}
		public CondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCond(this);
		}
	}

	public final CondContext cond() throws RecognitionException {
		CondContext _localctx = new CondContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_cond);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			_la = _input.LA(1);
			if (_la==KW_NOT) {
				{
				setState(130);
				condNot();
				}
			}

			setState(133);
			match(ID);
			setState(135);
			_la = _input.LA(1);
			if (_la==KW_IS_EQ || _la==KW_IS_NEQ) {
				{
				setState(134);
				condEnum();
				}
			}

			setState(138);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_SOUND) | (1L << KW_LIGHT) | (1L << KW_STOPPED))) != 0)) {
				{
				setState(137);
				condThrottleOp();
				}
			}

			setState(141);
			_la = _input.LA(1);
			if (_la==KW_PLUS) {
				{
				setState(140);
				condTime();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondNotContext extends ParserRuleContext {
		public CondNotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condNot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCondNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCondNot(this);
		}
	}

	public final CondNotContext condNot() throws RecognitionException {
		CondNotContext _localctx = new CondNotContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_condNot);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(KW_NOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondTimeContext extends ParserRuleContext {
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
		public CondTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCondTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCondTime(this);
		}
	}

	public final CondTimeContext condTime() throws RecognitionException {
		CondTimeContext _localctx = new CondTimeContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_condTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			match(KW_PLUS);
			setState(146);
			match(NUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondThrottleOpContext extends ParserRuleContext {
		public TerminalNode KW_FORWARD() { return getToken(ConductorParser.KW_FORWARD, 0); }
		public TerminalNode KW_REVERSE() { return getToken(ConductorParser.KW_REVERSE, 0); }
		public TerminalNode KW_STOPPED() { return getToken(ConductorParser.KW_STOPPED, 0); }
		public TerminalNode KW_SOUND() { return getToken(ConductorParser.KW_SOUND, 0); }
		public TerminalNode KW_LIGHT() { return getToken(ConductorParser.KW_LIGHT, 0); }
		public CondThrottleOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condThrottleOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCondThrottleOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCondThrottleOp(this);
		}
	}

	public final CondThrottleOpContext condThrottleOp() throws RecognitionException {
		CondThrottleOpContext _localctx = new CondThrottleOpContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_condThrottleOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_SOUND) | (1L << KW_LIGHT) | (1L << KW_STOPPED))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondEnumContext extends ParserRuleContext {
		public CondEnumOpContext condEnumOp() {
			return getRuleContext(CondEnumOpContext.class,0);
		}
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public CondEnumContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condEnum; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCondEnum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCondEnum(this);
		}
	}

	public final CondEnumContext condEnum() throws RecognitionException {
		CondEnumContext _localctx = new CondEnumContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_condEnum);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			condEnumOp();
			setState(151);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondEnumOpContext extends ParserRuleContext {
		public TerminalNode KW_IS_EQ() { return getToken(ConductorParser.KW_IS_EQ, 0); }
		public TerminalNode KW_IS_NEQ() { return getToken(ConductorParser.KW_IS_NEQ, 0); }
		public CondEnumOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condEnumOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCondEnumOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCondEnumOp(this);
		}
	}

	public final CondEnumOpContext condEnumOp() throws RecognitionException {
		CondEnumOpContext _localctx = new CondEnumOpContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_condEnumOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			_la = _input.LA(1);
			if ( !(_la==KW_IS_EQ || _la==KW_IS_NEQ) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ActionListContext extends ParserRuleContext {
		public List<ActionContext> action() {
			return getRuleContexts(ActionContext.class);
		}
		public ActionContext action(int i) {
			return getRuleContext(ActionContext.class,i);
		}
		public ActionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_actionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterActionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitActionList(this);
		}
	}

	public final ActionListContext actionList() throws RecognitionException {
		ActionListContext _localctx = new ActionListContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_actionList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			action();
			setState(160);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(156);
					match(KW_SEMI);
					setState(157);
					action();
					}
					} 
				}
				setState(162);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			}
			setState(164);
			_la = _input.LA(1);
			if (_la==KW_SEMI) {
				{
				setState(163);
				match(KW_SEMI);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ActionContext extends ParserRuleContext {
		public IdActionContext idAction() {
			return getRuleContext(IdActionContext.class,0);
		}
		public FnActionContext fnAction() {
			return getRuleContext(FnActionContext.class,0);
		}
		public TerminalNode EOL() { return getToken(ConductorParser.EOL, 0); }
		public ActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_action; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitAction(this);
		}
	}

	public final ActionContext action() throws RecognitionException {
		ActionContext _localctx = new ActionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_action);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_la = _input.LA(1);
			if (_la==EOL) {
				{
				setState(166);
				match(EOL);
				}
			}

			setState(171);
			switch (_input.LA(1)) {
			case ID:
				{
				setState(169);
				idAction();
				}
				break;
			case KW_RESET:
				{
				setState(170);
				fnAction();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdActionContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public ThrottleOpContext throttleOp() {
			return getRuleContext(ThrottleOpContext.class,0);
		}
		public TurnoutOpContext turnoutOp() {
			return getRuleContext(TurnoutOpContext.class,0);
		}
		public TimerOpContext timerOp() {
			return getRuleContext(TimerOpContext.class,0);
		}
		public FuncValueContext funcValue() {
			return getRuleContext(FuncValueContext.class,0);
		}
		public IdActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterIdAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitIdAction(this);
		}
	}

	public final IdActionContext idAction() throws RecognitionException {
		IdActionContext _localctx = new IdActionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_idAction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(ID);
			setState(177);
			switch (_input.LA(1)) {
			case KW_FORWARD:
			case KW_REVERSE:
			case KW_SOUND:
			case KW_LIGHT:
			case KW_HORN:
			case KW_STOP:
			case KW_FN:
				{
				setState(174);
				throttleOp();
				}
				break;
			case KW_NORMAL:
				{
				setState(175);
				turnoutOp();
				}
				break;
			case KW_START:
			case KW_END:
				{
				setState(176);
				timerOp();
				}
				break;
			case EOF:
			case EOL:
			case SB_COMMENT:
			case KW_EQUAL:
			case KW_SEMI:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(180);
			_la = _input.LA(1);
			if (_la==KW_EQUAL) {
				{
				setState(179);
				funcValue();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FnActionContext extends ParserRuleContext {
		public TerminalNode KW_RESET() { return getToken(ConductorParser.KW_RESET, 0); }
		public TerminalNode KW_TIMERS() { return getToken(ConductorParser.KW_TIMERS, 0); }
		public FnActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterFnAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitFnAction(this);
		}
	}

	public final FnActionContext fnAction() throws RecognitionException {
		FnActionContext _localctx = new FnActionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_fnAction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			match(KW_RESET);
			setState(183);
			match(KW_TIMERS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ThrottleOpContext extends ParserRuleContext {
		public TerminalNode KW_FORWARD() { return getToken(ConductorParser.KW_FORWARD, 0); }
		public TerminalNode KW_REVERSE() { return getToken(ConductorParser.KW_REVERSE, 0); }
		public TerminalNode KW_STOP() { return getToken(ConductorParser.KW_STOP, 0); }
		public TerminalNode KW_SOUND() { return getToken(ConductorParser.KW_SOUND, 0); }
		public TerminalNode KW_LIGHT() { return getToken(ConductorParser.KW_LIGHT, 0); }
		public TerminalNode KW_HORN() { return getToken(ConductorParser.KW_HORN, 0); }
		public TerminalNode KW_FN() { return getToken(ConductorParser.KW_FN, 0); }
		public ThrottleOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_throttleOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterThrottleOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitThrottleOp(this);
		}
	}

	public final ThrottleOpContext throttleOp() throws RecognitionException {
		ThrottleOpContext _localctx = new ThrottleOpContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_throttleOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_SOUND) | (1L << KW_LIGHT) | (1L << KW_HORN) | (1L << KW_STOP) | (1L << KW_FN))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TurnoutOpContext extends ParserRuleContext {
		public TerminalNode KW_NORMAL() { return getToken(ConductorParser.KW_NORMAL, 0); }
		public TurnoutOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_turnoutOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterTurnoutOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitTurnoutOp(this);
		}
	}

	public final TurnoutOpContext turnoutOp() throws RecognitionException {
		TurnoutOpContext _localctx = new TurnoutOpContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_turnoutOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(KW_NORMAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimerOpContext extends ParserRuleContext {
		public TerminalNode KW_START() { return getToken(ConductorParser.KW_START, 0); }
		public TerminalNode KW_END() { return getToken(ConductorParser.KW_END, 0); }
		public TimerOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timerOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterTimerOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitTimerOp(this);
		}
	}

	public final TimerOpContext timerOp() throws RecognitionException {
		TimerOpContext _localctx = new TimerOpContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_timerOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			_la = _input.LA(1);
			if ( !(_la==KW_START || _la==KW_END) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FuncValueContext extends ParserRuleContext {
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public FuncValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterFuncValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitFuncValue(this);
		}
	}

	public final FuncValueContext funcValue() throws RecognitionException {
		FuncValueContext _localctx = new FuncValueContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_funcValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191);
			match(KW_EQUAL);
			setState(192);
			_la = _input.LA(1);
			if ( !(_la==ID || _la==NUM) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3$\u00c5\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\3\2\3\2\3\2\7\2<\n\2\f\2\16\2?\13\2\3\2"+
		"\5\2B\n\2\3\2\3\2\3\3\3\3\5\3H\n\3\3\3\5\3K\n\3\3\4\3\4\3\4\3\4\3\4\5"+
		"\4R\n\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3"+
		"\t\3\t\3\t\6\tf\n\t\r\t\16\tg\3\n\3\n\3\n\3\n\3\n\3\13\6\13p\n\13\r\13"+
		"\16\13q\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\7\16\u0080"+
		"\n\16\f\16\16\16\u0083\13\16\3\17\5\17\u0086\n\17\3\17\3\17\5\17\u008a"+
		"\n\17\3\17\5\17\u008d\n\17\3\17\5\17\u0090\n\17\3\20\3\20\3\21\3\21\3"+
		"\21\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\25\7\25\u00a1\n\25"+
		"\f\25\16\25\u00a4\13\25\3\25\5\25\u00a7\n\25\3\26\5\26\u00aa\n\26\3\26"+
		"\3\26\5\26\u00ae\n\26\3\27\3\27\3\27\3\27\5\27\u00b4\n\27\3\27\5\27\u00b7"+
		"\n\27\3\30\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\34\3\34"+
		"\2\2\35\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\66\2"+
		"\t\3\2\22\23\4\2\17\17\24\24\5\2\25\26\30\31\34\34\3\2\7\b\5\2\25\26\30"+
		"\33!!\3\2\35\36\3\2\"#\u00c1\28\3\2\2\2\4G\3\2\2\2\6Q\3\2\2\2\bS\3\2\2"+
		"\2\nX\3\2\2\2\fZ\3\2\2\2\16_\3\2\2\2\20a\3\2\2\2\22i\3\2\2\2\24o\3\2\2"+
		"\2\26s\3\2\2\2\30x\3\2\2\2\32|\3\2\2\2\34\u0085\3\2\2\2\36\u0091\3\2\2"+
		"\2 \u0093\3\2\2\2\"\u0096\3\2\2\2$\u0098\3\2\2\2&\u009b\3\2\2\2(\u009d"+
		"\3\2\2\2*\u00a9\3\2\2\2,\u00af\3\2\2\2.\u00b8\3\2\2\2\60\u00bb\3\2\2\2"+
		"\62\u00bd\3\2\2\2\64\u00bf\3\2\2\2\66\u00c1\3\2\2\28=\5\4\3\29:\7\4\2"+
		"\2:<\5\4\3\2;9\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>A\3\2\2\2?=\3\2\2"+
		"\2@B\7\4\2\2A@\3\2\2\2AB\3\2\2\2BC\3\2\2\2CD\7\2\2\3D\3\3\2\2\2EH\5\6"+
		"\4\2FH\5\30\r\2GE\3\2\2\2GF\3\2\2\2GH\3\2\2\2HJ\3\2\2\2IK\7\5\2\2JI\3"+
		"\2\2\2JK\3\2\2\2K\5\3\2\2\2LR\5\b\5\2MR\5\f\7\2NR\5\20\t\2OR\5\22\n\2"+
		"PR\5\26\f\2QL\3\2\2\2QM\3\2\2\2QN\3\2\2\2QO\3\2\2\2QP\3\2\2\2R\7\3\2\2"+
		"\2ST\5\n\6\2TU\7\"\2\2UV\7\t\2\2VW\7\"\2\2W\t\3\2\2\2XY\t\2\2\2Y\13\3"+
		"\2\2\2Z[\5\16\b\2[\\\7\"\2\2\\]\7\t\2\2]^\7#\2\2^\r\3\2\2\2_`\t\3\2\2"+
		"`\17\3\2\2\2ab\7\21\2\2bc\7\"\2\2ce\7\t\2\2df\7#\2\2ed\3\2\2\2fg\3\2\2"+
		"\2ge\3\2\2\2gh\3\2\2\2h\21\3\2\2\2ij\7\20\2\2jk\7\"\2\2kl\7\t\2\2lm\5"+
		"\24\13\2m\23\3\2\2\2np\7\"\2\2on\3\2\2\2pq\3\2\2\2qo\3\2\2\2qr\3\2\2\2"+
		"r\25\3\2\2\2st\7\16\2\2tu\7\"\2\2uv\7\t\2\2vw\7$\2\2w\27\3\2\2\2xy\5\32"+
		"\16\2yz\7\6\2\2z{\5(\25\2{\31\3\2\2\2|\u0081\5\34\17\2}~\7\n\2\2~\u0080"+
		"\5\34\17\2\177}\3\2\2\2\u0080\u0083\3\2\2\2\u0081\177\3\2\2\2\u0081\u0082"+
		"\3\2\2\2\u0082\33\3\2\2\2\u0083\u0081\3\2\2\2\u0084\u0086\5\36\20\2\u0085"+
		"\u0084\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089\7\""+
		"\2\2\u0088\u008a\5$\23\2\u0089\u0088\3\2\2\2\u0089\u008a\3\2\2\2\u008a"+
		"\u008c\3\2\2\2\u008b\u008d\5\"\22\2\u008c\u008b\3\2\2\2\u008c\u008d\3"+
		"\2\2\2\u008d\u008f\3\2\2\2\u008e\u0090\5 \21\2\u008f\u008e\3\2\2\2\u008f"+
		"\u0090\3\2\2\2\u0090\35\3\2\2\2\u0091\u0092\7\13\2\2\u0092\37\3\2\2\2"+
		"\u0093\u0094\7\f\2\2\u0094\u0095\7#\2\2\u0095!\3\2\2\2\u0096\u0097\t\4"+
		"\2\2\u0097#\3\2\2\2\u0098\u0099\5&\24\2\u0099\u009a\7\"\2\2\u009a%\3\2"+
		"\2\2\u009b\u009c\t\5\2\2\u009c\'\3\2\2\2\u009d\u00a2\5*\26\2\u009e\u009f"+
		"\7\r\2\2\u009f\u00a1\5*\26\2\u00a0\u009e\3\2\2\2\u00a1\u00a4\3\2\2\2\u00a2"+
		"\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a6\3\2\2\2\u00a4\u00a2\3\2"+
		"\2\2\u00a5\u00a7\7\r\2\2\u00a6\u00a5\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7"+
		")\3\2\2\2\u00a8\u00aa\7\4\2\2\u00a9\u00a8\3\2\2\2\u00a9\u00aa\3\2\2\2"+
		"\u00aa\u00ad\3\2\2\2\u00ab\u00ae\5,\27\2\u00ac\u00ae\5.\30\2\u00ad\u00ab"+
		"\3\2\2\2\u00ad\u00ac\3\2\2\2\u00ae+\3\2\2\2\u00af\u00b3\7\"\2\2\u00b0"+
		"\u00b4\5\60\31\2\u00b1\u00b4\5\62\32\2\u00b2\u00b4\5\64\33\2\u00b3\u00b0"+
		"\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b2\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4"+
		"\u00b6\3\2\2\2\u00b5\u00b7\5\66\34\2\u00b6\u00b5\3\2\2\2\u00b6\u00b7\3"+
		"\2\2\2\u00b7-\3\2\2\2\u00b8\u00b9\7\37\2\2\u00b9\u00ba\7 \2\2\u00ba/\3"+
		"\2\2\2\u00bb\u00bc\t\6\2\2\u00bc\61\3\2\2\2\u00bd\u00be\7\27\2\2\u00be"+
		"\63\3\2\2\2\u00bf\u00c0\t\7\2\2\u00c0\65\3\2\2\2\u00c1\u00c2\7\t\2\2\u00c2"+
		"\u00c3\t\b\2\2\u00c3\67\3\2\2\2\24=AGJQgq\u0081\u0085\u0089\u008c\u008f"+
		"\u00a2\u00a6\u00a9\u00ad\u00b3\u00b6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}