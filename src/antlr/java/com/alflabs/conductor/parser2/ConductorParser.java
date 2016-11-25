// Generated from Conductor.g4 by ANTLR 4.5.3
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
		WS=1, EOL=2, SB_COMMENT=3, KW_ARROW=4, KW_EQUAL=5, KW_AND=6, KW_NOT=7, 
		KW_PLUS=8, KW_SEMI=9, KW_VAR=10, KW_THROTTLE=11, KW_SENSOR=12, KW_TURNOUT=13, 
		KW_TIMER=14, KW_FORWARD=15, KW_REVERSE=16, KW_NORMAL=17, KW_SOUND=18, 
		KW_LIGHT=19, KW_HORN=20, KW_STOP=21, KW_STOPPED=22, KW_START=23, KW_FN=24, 
		ID=25, NUM=26;
	public static final int
		RULE_script = 0, RULE_scriptLine = 1, RULE_defLine = 2, RULE_defStrLine = 3, 
		RULE_defStrType = 4, RULE_defIntLine = 5, RULE_defIntType = 6, RULE_eventLine = 7, 
		RULE_condList = 8, RULE_cond = 9, RULE_condNot = 10, RULE_condTime = 11, 
		RULE_condThrottleOp = 12, RULE_actionList = 13, RULE_action = 14, RULE_throttleOp = 15, 
		RULE_turnoutOp = 16, RULE_timerOp = 17, RULE_funcValue = 18;
	public static final String[] ruleNames = {
		"script", "scriptLine", "defLine", "defStrLine", "defStrType", "defIntLine", 
		"defIntType", "eventLine", "condList", "cond", "condNot", "condTime", 
		"condThrottleOp", "actionList", "action", "throttleOp", "turnoutOp", "timerOp", 
		"funcValue"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'->'", "'='", "'&'", "'!'", "'+'", "';'", "'var'", 
		"'throttle'", "'sensor'", "'turnout'", "'timer'", "'forward'", "'reverse'", 
		"'normal'", "'sound'", "'light'", "'horn'", "'stop'", "'stopped'", "'start'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_EQUAL", "KW_AND", "KW_NOT", 
		"KW_PLUS", "KW_SEMI", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", 
		"KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", 
		"KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_FN", "ID", "NUM"
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
			setState(38);
			scriptLine();
			setState(43);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(39);
					match(EOL);
					setState(40);
					scriptLine();
					}
					} 
				}
				setState(45);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(47);
			_la = _input.LA(1);
			if (_la==EOL) {
				{
				setState(46);
				match(EOL);
				}
			}

			setState(49);
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
			setState(53);
			switch (_input.LA(1)) {
			case KW_VAR:
			case KW_THROTTLE:
			case KW_SENSOR:
			case KW_TURNOUT:
			case KW_TIMER:
				{
				setState(51);
				defLine();
				}
				break;
			case KW_NOT:
			case ID:
				{
				setState(52);
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
			setState(56);
			_la = _input.LA(1);
			if (_la==SB_COMMENT) {
				{
				setState(55);
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
			setState(60);
			switch (_input.LA(1)) {
			case KW_SENSOR:
			case KW_TURNOUT:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				defStrLine();
				}
				break;
			case KW_VAR:
			case KW_THROTTLE:
			case KW_TIMER:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				defIntLine();
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
			setState(62);
			defStrType();
			setState(63);
			match(ID);
			setState(64);
			match(KW_EQUAL);
			setState(65);
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
			setState(67);
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
			setState(69);
			defIntType();
			setState(70);
			match(ID);
			setState(71);
			match(KW_EQUAL);
			setState(72);
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
		public TerminalNode KW_THROTTLE() { return getToken(ConductorParser.KW_THROTTLE, 0); }
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
			setState(74);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_VAR) | (1L << KW_THROTTLE) | (1L << KW_TIMER))) != 0)) ) {
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
		enterRule(_localctx, 14, RULE_eventLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			condList();
			setState(77);
			match(KW_ARROW);
			setState(78);
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
		enterRule(_localctx, 16, RULE_condList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			cond();
			setState(85);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KW_AND) {
				{
				{
				setState(81);
				match(KW_AND);
				setState(82);
				cond();
				}
				}
				setState(87);
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
		public CondTimeContext condTime() {
			return getRuleContext(CondTimeContext.class,0);
		}
		public CondThrottleOpContext condThrottleOp() {
			return getRuleContext(CondThrottleOpContext.class,0);
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
		enterRule(_localctx, 18, RULE_cond);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			_la = _input.LA(1);
			if (_la==KW_NOT) {
				{
				setState(88);
				condNot();
				}
			}

			setState(91);
			match(ID);
			setState(94);
			switch (_input.LA(1)) {
			case KW_PLUS:
				{
				setState(92);
				condTime();
				}
				break;
			case KW_FORWARD:
			case KW_REVERSE:
			case KW_SOUND:
			case KW_LIGHT:
			case KW_STOPPED:
				{
				setState(93);
				condThrottleOp();
				}
				break;
			case KW_ARROW:
			case KW_AND:
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
		enterRule(_localctx, 20, RULE_condNot);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
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
		enterRule(_localctx, 22, RULE_condTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			match(KW_PLUS);
			setState(99);
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
		enterRule(_localctx, 24, RULE_condThrottleOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
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
		enterRule(_localctx, 26, RULE_actionList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			action();
			setState(108);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(104);
					match(KW_SEMI);
					setState(105);
					action();
					}
					} 
				}
				setState(110);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			setState(112);
			_la = _input.LA(1);
			if (_la==KW_SEMI) {
				{
				setState(111);
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
		enterRule(_localctx, 28, RULE_action);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			match(ID);
			setState(118);
			switch (_input.LA(1)) {
			case KW_FORWARD:
			case KW_REVERSE:
			case KW_SOUND:
			case KW_LIGHT:
			case KW_HORN:
			case KW_STOP:
			case KW_FN:
				{
				setState(115);
				throttleOp();
				}
				break;
			case KW_NORMAL:
				{
				setState(116);
				turnoutOp();
				}
				break;
			case KW_START:
				{
				setState(117);
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
			setState(121);
			_la = _input.LA(1);
			if (_la==KW_EQUAL) {
				{
				setState(120);
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
		enterRule(_localctx, 30, RULE_throttleOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
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
		enterRule(_localctx, 32, RULE_turnoutOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
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
		enterRule(_localctx, 34, RULE_timerOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(KW_START);
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
		enterRule(_localctx, 36, RULE_funcValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(KW_EQUAL);
			setState(130);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\34\u0087\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\3\2\3\2\3\2\7\2,\n\2\f\2\16\2/\13\2\3\2\5\2\62\n"+
		"\2\3\2\3\2\3\3\3\3\5\38\n\3\3\3\5\3;\n\3\3\4\3\4\5\4?\n\4\3\5\3\5\3\5"+
		"\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3"+
		"\n\7\nV\n\n\f\n\16\nY\13\n\3\13\5\13\\\n\13\3\13\3\13\3\13\5\13a\n\13"+
		"\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\17\7\17m\n\17\f\17\16\17p\13"+
		"\17\3\17\5\17s\n\17\3\20\3\20\3\20\3\20\5\20y\n\20\3\20\5\20|\n\20\3\21"+
		"\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24\2\2\25\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36 \"$&\2\7\3\2\16\17\4\2\f\r\20\20\5\2\21\22\24\25"+
		"\30\30\5\2\21\22\24\27\32\32\3\2\33\34\u0083\2(\3\2\2\2\4\67\3\2\2\2\6"+
		">\3\2\2\2\b@\3\2\2\2\nE\3\2\2\2\fG\3\2\2\2\16L\3\2\2\2\20N\3\2\2\2\22"+
		"R\3\2\2\2\24[\3\2\2\2\26b\3\2\2\2\30d\3\2\2\2\32g\3\2\2\2\34i\3\2\2\2"+
		"\36t\3\2\2\2 }\3\2\2\2\"\177\3\2\2\2$\u0081\3\2\2\2&\u0083\3\2\2\2(-\5"+
		"\4\3\2)*\7\4\2\2*,\5\4\3\2+)\3\2\2\2,/\3\2\2\2-+\3\2\2\2-.\3\2\2\2.\61"+
		"\3\2\2\2/-\3\2\2\2\60\62\7\4\2\2\61\60\3\2\2\2\61\62\3\2\2\2\62\63\3\2"+
		"\2\2\63\64\7\2\2\3\64\3\3\2\2\2\658\5\6\4\2\668\5\20\t\2\67\65\3\2\2\2"+
		"\67\66\3\2\2\2\678\3\2\2\28:\3\2\2\29;\7\5\2\2:9\3\2\2\2:;\3\2\2\2;\5"+
		"\3\2\2\2<?\5\b\5\2=?\5\f\7\2><\3\2\2\2>=\3\2\2\2?\7\3\2\2\2@A\5\n\6\2"+
		"AB\7\33\2\2BC\7\7\2\2CD\7\33\2\2D\t\3\2\2\2EF\t\2\2\2F\13\3\2\2\2GH\5"+
		"\16\b\2HI\7\33\2\2IJ\7\7\2\2JK\7\34\2\2K\r\3\2\2\2LM\t\3\2\2M\17\3\2\2"+
		"\2NO\5\22\n\2OP\7\6\2\2PQ\5\34\17\2Q\21\3\2\2\2RW\5\24\13\2ST\7\b\2\2"+
		"TV\5\24\13\2US\3\2\2\2VY\3\2\2\2WU\3\2\2\2WX\3\2\2\2X\23\3\2\2\2YW\3\2"+
		"\2\2Z\\\5\26\f\2[Z\3\2\2\2[\\\3\2\2\2\\]\3\2\2\2]`\7\33\2\2^a\5\30\r\2"+
		"_a\5\32\16\2`^\3\2\2\2`_\3\2\2\2`a\3\2\2\2a\25\3\2\2\2bc\7\t\2\2c\27\3"+
		"\2\2\2de\7\n\2\2ef\7\34\2\2f\31\3\2\2\2gh\t\4\2\2h\33\3\2\2\2in\5\36\20"+
		"\2jk\7\13\2\2km\5\36\20\2lj\3\2\2\2mp\3\2\2\2nl\3\2\2\2no\3\2\2\2or\3"+
		"\2\2\2pn\3\2\2\2qs\7\13\2\2rq\3\2\2\2rs\3\2\2\2s\35\3\2\2\2tx\7\33\2\2"+
		"uy\5 \21\2vy\5\"\22\2wy\5$\23\2xu\3\2\2\2xv\3\2\2\2xw\3\2\2\2xy\3\2\2"+
		"\2y{\3\2\2\2z|\5&\24\2{z\3\2\2\2{|\3\2\2\2|\37\3\2\2\2}~\t\5\2\2~!\3\2"+
		"\2\2\177\u0080\7\23\2\2\u0080#\3\2\2\2\u0081\u0082\7\31\2\2\u0082%\3\2"+
		"\2\2\u0083\u0084\7\7\2\2\u0084\u0085\t\6\2\2\u0085\'\3\2\2\2\16-\61\67"+
		":>W[`nrx{";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
