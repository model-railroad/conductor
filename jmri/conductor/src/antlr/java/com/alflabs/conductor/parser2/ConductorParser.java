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
		T__0=1, T__1=2, WS=3, EOL=4, SB_COMMENT=5, KW_ARROW=6, KW_IS_EQ=7, KW_IS_NEQ=8, 
		KW_EQUAL=9, KW_AND=10, KW_NOT=11, KW_PLUS=12, KW_SEMI=13, KW_END=14, KW_ENUM=15, 
		KW_FORWARD=16, KW_HORN=17, KW_LIGHT=18, KW_MAP=19, KW_NORMAL=20, KW_RESET=21, 
		KW_REVERSE=22, KW_ROUTE=23, KW_SENSOR=24, KW_SOUND=25, KW_START=26, KW_STATUS=27, 
		KW_STOP=28, KW_STOPPED=29, KW_THROTTLE=30, KW_TIMER=31, KW_TIMERS=32, 
		KW_TOGGLE=33, KW_TURNOUT=34, KW_VAR=35, KW_FN=36, ID=37, NUM=38, STR=39;
	public static final int
		RULE_script = 0, RULE_scriptLine = 1, RULE_defLine = 2, RULE_defStrLine = 3, 
		RULE_defStrType = 4, RULE_defIntLine = 5, RULE_defIntType = 6, RULE_defThrottleLine = 7, 
		RULE_defEnumLine = 8, RULE_defEnumValues = 9, RULE_defMapLine = 10, RULE_defRouteLine = 11, 
		RULE_routeInfoList = 12, RULE_routeInfo = 13, RULE_routeInfoOpId = 14, 
		RULE_routeInfoOpNum = 15, RULE_eventLine = 16, RULE_condList = 17, RULE_cond = 18, 
		RULE_condNot = 19, RULE_condTime = 20, RULE_condThrottleOp = 21, RULE_condEnum = 22, 
		RULE_condEnumOp = 23, RULE_actionList = 24, RULE_action = 25, RULE_idAction = 26, 
		RULE_fnAction = 27, RULE_throttleOp = 28, RULE_turnoutOp = 29, RULE_timerOp = 30, 
		RULE_funcValue = 31;
	public static final String[] ruleNames = {
		"script", "scriptLine", "defLine", "defStrLine", "defStrType", "defIntLine", 
		"defIntType", "defThrottleLine", "defEnumLine", "defEnumValues", "defMapLine", 
		"defRouteLine", "routeInfoList", "routeInfo", "routeInfoOpId", "routeInfoOpNum", 
		"eventLine", "condList", "cond", "condNot", "condTime", "condThrottleOp", 
		"condEnum", "condEnumOp", "actionList", "action", "idAction", "fnAction", 
		"throttleOp", "turnoutOp", "timerOp", "funcValue"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "','", "':'", null, null, null, "'->'", "'=='", "'!='", "'='", "'&'", 
		"'!'", "'+'", "';'", "'end'", "'enum'", "'forward'", "'horn'", "'light'", 
		"'map'", "'normal'", "'reset'", "'reverse'", "'route'", "'sensor'", "'sound'", 
		"'start'", "'status'", "'stop'", "'stopped'", "'throttle'", "'timer'", 
		"'timers'", "'toggle'", "'turnout'", "'var'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_IS_EQ", "KW_IS_NEQ", 
		"KW_EQUAL", "KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "KW_END", "KW_ENUM", 
		"KW_FORWARD", "KW_HORN", "KW_LIGHT", "KW_MAP", "KW_NORMAL", "KW_RESET", 
		"KW_REVERSE", "KW_ROUTE", "KW_SENSOR", "KW_SOUND", "KW_START", "KW_STATUS", 
		"KW_STOP", "KW_STOPPED", "KW_THROTTLE", "KW_TIMER", "KW_TIMERS", "KW_TOGGLE", 
		"KW_TURNOUT", "KW_VAR", "KW_FN", "ID", "NUM", "STR"
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
			setState(64);
			scriptLine();
			setState(69);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(65);
					match(EOL);
					setState(66);
					scriptLine();
					}
					} 
				}
				setState(71);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(73);
			_la = _input.LA(1);
			if (_la==EOL) {
				{
				setState(72);
				match(EOL);
				}
			}

			setState(75);
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
			setState(79);
			switch (_input.LA(1)) {
			case KW_ENUM:
			case KW_MAP:
			case KW_ROUTE:
			case KW_SENSOR:
			case KW_THROTTLE:
			case KW_TIMER:
			case KW_TURNOUT:
			case KW_VAR:
				{
				setState(77);
				defLine();
				}
				break;
			case KW_NOT:
			case ID:
				{
				setState(78);
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
			setState(82);
			_la = _input.LA(1);
			if (_la==SB_COMMENT) {
				{
				setState(81);
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
		public DefRouteLineContext defRouteLine() {
			return getRuleContext(DefRouteLineContext.class,0);
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
			setState(90);
			switch (_input.LA(1)) {
			case KW_SENSOR:
			case KW_TURNOUT:
				enterOuterAlt(_localctx, 1);
				{
				setState(84);
				defStrLine();
				}
				break;
			case KW_TIMER:
			case KW_VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				defIntLine();
				}
				break;
			case KW_THROTTLE:
				enterOuterAlt(_localctx, 3);
				{
				setState(86);
				defThrottleLine();
				}
				break;
			case KW_ENUM:
				enterOuterAlt(_localctx, 4);
				{
				setState(87);
				defEnumLine();
				}
				break;
			case KW_MAP:
				enterOuterAlt(_localctx, 5);
				{
				setState(88);
				defMapLine();
				}
				break;
			case KW_ROUTE:
				enterOuterAlt(_localctx, 6);
				{
				setState(89);
				defRouteLine();
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
			setState(92);
			defStrType();
			setState(93);
			match(ID);
			setState(94);
			match(KW_EQUAL);
			setState(95);
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
			setState(97);
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
			setState(99);
			defIntType();
			setState(100);
			match(ID);
			setState(101);
			match(KW_EQUAL);
			setState(102);
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
			setState(104);
			_la = _input.LA(1);
			if ( !(_la==KW_TIMER || _la==KW_VAR) ) {
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
			setState(106);
			match(KW_THROTTLE);
			setState(107);
			match(ID);
			setState(108);
			match(KW_EQUAL);
			setState(110); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(109);
				match(NUM);
				}
				}
				setState(112); 
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
			setState(114);
			match(KW_ENUM);
			setState(115);
			match(ID);
			setState(116);
			match(KW_EQUAL);
			setState(117);
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
			setState(120); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(119);
				match(ID);
				}
				}
				setState(122); 
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
			setState(124);
			match(KW_MAP);
			setState(125);
			match(ID);
			setState(126);
			match(KW_EQUAL);
			setState(127);
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

	public static class DefRouteLineContext extends ParserRuleContext {
		public TerminalNode KW_ROUTE() { return getToken(ConductorParser.KW_ROUTE, 0); }
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public RouteInfoListContext routeInfoList() {
			return getRuleContext(RouteInfoListContext.class,0);
		}
		public DefRouteLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defRouteLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefRouteLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefRouteLine(this);
		}
	}

	public final DefRouteLineContext defRouteLine() throws RecognitionException {
		DefRouteLineContext _localctx = new DefRouteLineContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_defRouteLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(KW_ROUTE);
			setState(130);
			match(ID);
			setState(131);
			match(KW_EQUAL);
			setState(132);
			routeInfoList();
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

	public static class RouteInfoListContext extends ParserRuleContext {
		public List<RouteInfoContext> routeInfo() {
			return getRuleContexts(RouteInfoContext.class);
		}
		public RouteInfoContext routeInfo(int i) {
			return getRuleContext(RouteInfoContext.class,i);
		}
		public RouteInfoListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_routeInfoList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterRouteInfoList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitRouteInfoList(this);
		}
	}

	public final RouteInfoListContext routeInfoList() throws RecognitionException {
		RouteInfoListContext _localctx = new RouteInfoListContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_routeInfoList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			routeInfo();
			setState(139);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(135);
					match(T__0);
					setState(136);
					routeInfo();
					}
					} 
				}
				setState(141);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			setState(143);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(142);
				match(T__0);
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

	public static class RouteInfoContext extends ParserRuleContext {
		public RouteInfoOpIdContext routeInfoOpId() {
			return getRuleContext(RouteInfoOpIdContext.class,0);
		}
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public RouteInfoOpNumContext routeInfoOpNum() {
			return getRuleContext(RouteInfoOpNumContext.class,0);
		}
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
		public RouteInfoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_routeInfo; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterRouteInfo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitRouteInfo(this);
		}
	}

	public final RouteInfoContext routeInfo() throws RecognitionException {
		RouteInfoContext _localctx = new RouteInfoContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_routeInfo);
		try {
			setState(153);
			switch (_input.LA(1)) {
			case KW_STATUS:
			case KW_TOGGLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				routeInfoOpId();
				setState(146);
				match(T__1);
				setState(147);
				match(ID);
				}
				break;
			case KW_THROTTLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(149);
				routeInfoOpNum();
				setState(150);
				match(T__1);
				setState(151);
				match(NUM);
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

	public static class RouteInfoOpIdContext extends ParserRuleContext {
		public TerminalNode KW_TOGGLE() { return getToken(ConductorParser.KW_TOGGLE, 0); }
		public TerminalNode KW_STATUS() { return getToken(ConductorParser.KW_STATUS, 0); }
		public RouteInfoOpIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_routeInfoOpId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterRouteInfoOpId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitRouteInfoOpId(this);
		}
	}

	public final RouteInfoOpIdContext routeInfoOpId() throws RecognitionException {
		RouteInfoOpIdContext _localctx = new RouteInfoOpIdContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_routeInfoOpId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			_la = _input.LA(1);
			if ( !(_la==KW_STATUS || _la==KW_TOGGLE) ) {
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

	public static class RouteInfoOpNumContext extends ParserRuleContext {
		public TerminalNode KW_THROTTLE() { return getToken(ConductorParser.KW_THROTTLE, 0); }
		public RouteInfoOpNumContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_routeInfoOpNum; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterRouteInfoOpNum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitRouteInfoOpNum(this);
		}
	}

	public final RouteInfoOpNumContext routeInfoOpNum() throws RecognitionException {
		RouteInfoOpNumContext _localctx = new RouteInfoOpNumContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_routeInfoOpNum);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(KW_THROTTLE);
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
		enterRule(_localctx, 32, RULE_eventLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			condList();
			setState(160);
			match(KW_ARROW);
			setState(161);
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
		enterRule(_localctx, 34, RULE_condList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			cond();
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KW_AND) {
				{
				{
				setState(164);
				match(KW_AND);
				setState(165);
				cond();
				}
				}
				setState(170);
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
		enterRule(_localctx, 36, RULE_cond);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			_la = _input.LA(1);
			if (_la==KW_NOT) {
				{
				setState(171);
				condNot();
				}
			}

			setState(174);
			match(ID);
			setState(176);
			_la = _input.LA(1);
			if (_la==KW_IS_EQ || _la==KW_IS_NEQ) {
				{
				setState(175);
				condEnum();
				}
			}

			setState(179);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_LIGHT) | (1L << KW_REVERSE) | (1L << KW_SOUND) | (1L << KW_STOPPED))) != 0)) {
				{
				setState(178);
				condThrottleOp();
				}
			}

			setState(182);
			_la = _input.LA(1);
			if (_la==KW_PLUS) {
				{
				setState(181);
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
		enterRule(_localctx, 38, RULE_condNot);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
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
		enterRule(_localctx, 40, RULE_condTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			match(KW_PLUS);
			setState(187);
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
		enterRule(_localctx, 42, RULE_condThrottleOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_LIGHT) | (1L << KW_REVERSE) | (1L << KW_SOUND) | (1L << KW_STOPPED))) != 0)) ) {
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
		enterRule(_localctx, 44, RULE_condEnum);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191);
			condEnumOp();
			setState(192);
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
		enterRule(_localctx, 46, RULE_condEnumOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
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
		enterRule(_localctx, 48, RULE_actionList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			action();
			setState(201);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(197);
					match(KW_SEMI);
					setState(198);
					action();
					}
					} 
				}
				setState(203);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			}
			setState(205);
			_la = _input.LA(1);
			if (_la==KW_SEMI) {
				{
				setState(204);
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
		enterRule(_localctx, 50, RULE_action);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			_la = _input.LA(1);
			if (_la==EOL) {
				{
				setState(207);
				match(EOL);
				}
			}

			setState(212);
			switch (_input.LA(1)) {
			case ID:
				{
				setState(210);
				idAction();
				}
				break;
			case KW_RESET:
				{
				setState(211);
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
		enterRule(_localctx, 52, RULE_idAction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			match(ID);
			setState(218);
			switch (_input.LA(1)) {
			case KW_FORWARD:
			case KW_HORN:
			case KW_LIGHT:
			case KW_REVERSE:
			case KW_SOUND:
			case KW_STOP:
			case KW_FN:
				{
				setState(215);
				throttleOp();
				}
				break;
			case KW_NORMAL:
				{
				setState(216);
				turnoutOp();
				}
				break;
			case KW_END:
			case KW_START:
				{
				setState(217);
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
			setState(221);
			_la = _input.LA(1);
			if (_la==KW_EQUAL) {
				{
				setState(220);
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
		enterRule(_localctx, 54, RULE_fnAction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(KW_RESET);
			setState(224);
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
		enterRule(_localctx, 56, RULE_throttleOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_HORN) | (1L << KW_LIGHT) | (1L << KW_REVERSE) | (1L << KW_SOUND) | (1L << KW_STOP) | (1L << KW_FN))) != 0)) ) {
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
		enterRule(_localctx, 58, RULE_turnoutOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
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
		enterRule(_localctx, 60, RULE_timerOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			_la = _input.LA(1);
			if ( !(_la==KW_END || _la==KW_START) ) {
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
		enterRule(_localctx, 62, RULE_funcValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			match(KW_EQUAL);
			setState(233);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3)\u00ee\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\3\2\3\2\3\2\7\2F\n\2\f\2\16\2I\13\2\3\2\5\2L\n\2\3\2\3\2\3\3\3\3\5"+
		"\3R\n\3\3\3\5\3U\n\3\3\4\3\4\3\4\3\4\3\4\3\4\5\4]\n\4\3\5\3\5\3\5\3\5"+
		"\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\6\tq\n\t\r\t"+
		"\16\tr\3\n\3\n\3\n\3\n\3\n\3\13\6\13{\n\13\r\13\16\13|\3\f\3\f\3\f\3\f"+
		"\3\f\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\7\16\u008c\n\16\f\16\16\16\u008f"+
		"\13\16\3\16\5\16\u0092\n\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5"+
		"\17\u009c\n\17\3\20\3\20\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23"+
		"\7\23\u00a9\n\23\f\23\16\23\u00ac\13\23\3\24\5\24\u00af\n\24\3\24\3\24"+
		"\5\24\u00b3\n\24\3\24\5\24\u00b6\n\24\3\24\5\24\u00b9\n\24\3\25\3\25\3"+
		"\26\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\31\3\31\3\32\3\32\3\32\7\32\u00ca"+
		"\n\32\f\32\16\32\u00cd\13\32\3\32\5\32\u00d0\n\32\3\33\5\33\u00d3\n\33"+
		"\3\33\3\33\5\33\u00d7\n\33\3\34\3\34\3\34\3\34\5\34\u00dd\n\34\3\34\5"+
		"\34\u00e0\n\34\3\35\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3!\3!\2"+
		"\2\"\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@"+
		"\2\n\4\2\32\32$$\4\2!!%%\4\2\35\35##\7\2\22\22\24\24\30\30\33\33\37\37"+
		"\3\2\t\n\7\2\22\24\30\30\33\33\36\36&&\4\2\20\20\34\34\3\2\'(\u00e9\2"+
		"B\3\2\2\2\4Q\3\2\2\2\6\\\3\2\2\2\b^\3\2\2\2\nc\3\2\2\2\fe\3\2\2\2\16j"+
		"\3\2\2\2\20l\3\2\2\2\22t\3\2\2\2\24z\3\2\2\2\26~\3\2\2\2\30\u0083\3\2"+
		"\2\2\32\u0088\3\2\2\2\34\u009b\3\2\2\2\36\u009d\3\2\2\2 \u009f\3\2\2\2"+
		"\"\u00a1\3\2\2\2$\u00a5\3\2\2\2&\u00ae\3\2\2\2(\u00ba\3\2\2\2*\u00bc\3"+
		"\2\2\2,\u00bf\3\2\2\2.\u00c1\3\2\2\2\60\u00c4\3\2\2\2\62\u00c6\3\2\2\2"+
		"\64\u00d2\3\2\2\2\66\u00d8\3\2\2\28\u00e1\3\2\2\2:\u00e4\3\2\2\2<\u00e6"+
		"\3\2\2\2>\u00e8\3\2\2\2@\u00ea\3\2\2\2BG\5\4\3\2CD\7\6\2\2DF\5\4\3\2E"+
		"C\3\2\2\2FI\3\2\2\2GE\3\2\2\2GH\3\2\2\2HK\3\2\2\2IG\3\2\2\2JL\7\6\2\2"+
		"KJ\3\2\2\2KL\3\2\2\2LM\3\2\2\2MN\7\2\2\3N\3\3\2\2\2OR\5\6\4\2PR\5\"\22"+
		"\2QO\3\2\2\2QP\3\2\2\2QR\3\2\2\2RT\3\2\2\2SU\7\7\2\2TS\3\2\2\2TU\3\2\2"+
		"\2U\5\3\2\2\2V]\5\b\5\2W]\5\f\7\2X]\5\20\t\2Y]\5\22\n\2Z]\5\26\f\2[]\5"+
		"\30\r\2\\V\3\2\2\2\\W\3\2\2\2\\X\3\2\2\2\\Y\3\2\2\2\\Z\3\2\2\2\\[\3\2"+
		"\2\2]\7\3\2\2\2^_\5\n\6\2_`\7\'\2\2`a\7\13\2\2ab\7\'\2\2b\t\3\2\2\2cd"+
		"\t\2\2\2d\13\3\2\2\2ef\5\16\b\2fg\7\'\2\2gh\7\13\2\2hi\7(\2\2i\r\3\2\2"+
		"\2jk\t\3\2\2k\17\3\2\2\2lm\7 \2\2mn\7\'\2\2np\7\13\2\2oq\7(\2\2po\3\2"+
		"\2\2qr\3\2\2\2rp\3\2\2\2rs\3\2\2\2s\21\3\2\2\2tu\7\21\2\2uv\7\'\2\2vw"+
		"\7\13\2\2wx\5\24\13\2x\23\3\2\2\2y{\7\'\2\2zy\3\2\2\2{|\3\2\2\2|z\3\2"+
		"\2\2|}\3\2\2\2}\25\3\2\2\2~\177\7\25\2\2\177\u0080\7\'\2\2\u0080\u0081"+
		"\7\13\2\2\u0081\u0082\7)\2\2\u0082\27\3\2\2\2\u0083\u0084\7\31\2\2\u0084"+
		"\u0085\7\'\2\2\u0085\u0086\7\13\2\2\u0086\u0087\5\32\16\2\u0087\31\3\2"+
		"\2\2\u0088\u008d\5\34\17\2\u0089\u008a\7\3\2\2\u008a\u008c\5\34\17\2\u008b"+
		"\u0089\3\2\2\2\u008c\u008f\3\2\2\2\u008d\u008b\3\2\2\2\u008d\u008e\3\2"+
		"\2\2\u008e\u0091\3\2\2\2\u008f\u008d\3\2\2\2\u0090\u0092\7\3\2\2\u0091"+
		"\u0090\3\2\2\2\u0091\u0092\3\2\2\2\u0092\33\3\2\2\2\u0093\u0094\5\36\20"+
		"\2\u0094\u0095\7\4\2\2\u0095\u0096\7\'\2\2\u0096\u009c\3\2\2\2\u0097\u0098"+
		"\5 \21\2\u0098\u0099\7\4\2\2\u0099\u009a\7(\2\2\u009a\u009c\3\2\2\2\u009b"+
		"\u0093\3\2\2\2\u009b\u0097\3\2\2\2\u009c\35\3\2\2\2\u009d\u009e\t\4\2"+
		"\2\u009e\37\3\2\2\2\u009f\u00a0\7 \2\2\u00a0!\3\2\2\2\u00a1\u00a2\5$\23"+
		"\2\u00a2\u00a3\7\b\2\2\u00a3\u00a4\5\62\32\2\u00a4#\3\2\2\2\u00a5\u00aa"+
		"\5&\24\2\u00a6\u00a7\7\f\2\2\u00a7\u00a9\5&\24\2\u00a8\u00a6\3\2\2\2\u00a9"+
		"\u00ac\3\2\2\2\u00aa\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab%\3\2\2\2"+
		"\u00ac\u00aa\3\2\2\2\u00ad\u00af\5(\25\2\u00ae\u00ad\3\2\2\2\u00ae\u00af"+
		"\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\u00b2\7\'\2\2\u00b1\u00b3\5.\30\2\u00b2"+
		"\u00b1\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b5\3\2\2\2\u00b4\u00b6\5,"+
		"\27\2\u00b5\u00b4\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00b8\3\2\2\2\u00b7"+
		"\u00b9\5*\26\2\u00b8\u00b7\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\'\3\2\2\2"+
		"\u00ba\u00bb\7\r\2\2\u00bb)\3\2\2\2\u00bc\u00bd\7\16\2\2\u00bd\u00be\7"+
		"(\2\2\u00be+\3\2\2\2\u00bf\u00c0\t\5\2\2\u00c0-\3\2\2\2\u00c1\u00c2\5"+
		"\60\31\2\u00c2\u00c3\7\'\2\2\u00c3/\3\2\2\2\u00c4\u00c5\t\6\2\2\u00c5"+
		"\61\3\2\2\2\u00c6\u00cb\5\64\33\2\u00c7\u00c8\7\17\2\2\u00c8\u00ca\5\64"+
		"\33\2\u00c9\u00c7\3\2\2\2\u00ca\u00cd\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb"+
		"\u00cc\3\2\2\2\u00cc\u00cf\3\2\2\2\u00cd\u00cb\3\2\2\2\u00ce\u00d0\7\17"+
		"\2\2\u00cf\u00ce\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\63\3\2\2\2\u00d1\u00d3"+
		"\7\6\2\2\u00d2\u00d1\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00d6\3\2\2\2\u00d4"+
		"\u00d7\5\66\34\2\u00d5\u00d7\58\35\2\u00d6\u00d4\3\2\2\2\u00d6\u00d5\3"+
		"\2\2\2\u00d7\65\3\2\2\2\u00d8\u00dc\7\'\2\2\u00d9\u00dd\5:\36\2\u00da"+
		"\u00dd\5<\37\2\u00db\u00dd\5> \2\u00dc\u00d9\3\2\2\2\u00dc\u00da\3\2\2"+
		"\2\u00dc\u00db\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00df\3\2\2\2\u00de\u00e0"+
		"\5@!\2\u00df\u00de\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\67\3\2\2\2\u00e1"+
		"\u00e2\7\27\2\2\u00e2\u00e3\7\"\2\2\u00e39\3\2\2\2\u00e4\u00e5\t\7\2\2"+
		"\u00e5;\3\2\2\2\u00e6\u00e7\7\26\2\2\u00e7=\3\2\2\2\u00e8\u00e9\t\b\2"+
		"\2\u00e9?\3\2\2\2\u00ea\u00eb\7\13\2\2\u00eb\u00ec\t\t\2\2\u00ecA\3\2"+
		"\2\2\27GKQT\\r|\u008d\u0091\u009b\u00aa\u00ae\u00b2\u00b5\u00b8\u00cb"+
		"\u00cf\u00d2\u00d6\u00dc\u00df";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}