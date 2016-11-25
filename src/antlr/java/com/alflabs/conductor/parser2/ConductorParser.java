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
		WS=1, EOL=2, SB_COMMENT=3, KW_VAR=4, KW_THROTTLE=5, KW_SENSOR=6, KW_TURNOUT=7, 
		KW_TIMER=8, KW_FORWARD=9, KW_REVERSE=10, KW_NORMAL=11, KW_SOUND=12, KW_LIGHT=13, 
		KW_HORN=14, KW_STOP=15, KW_STOPPED=16, KW_START=17, KW_FN=18, KW_ARROW=19, 
		KW_EQUAL=20, KW_AND=21, KW_NOT=22, KW_PLUS=23, KW_SEMI=24, ID=25, NUM=26, 
		RESERVED=27;
	public static final int
		RULE_script = 0, RULE_scriptLine = 1, RULE_defLine = 2, RULE_defStrLine = 3, 
		RULE_defStrType = 4, RULE_defIntLine = 5, RULE_defIntType = 6, RULE_eventLine = 7, 
		RULE_condList = 8, RULE_cond = 9, RULE_cond_op = 10, RULE_instList = 11, 
		RULE_inst = 12, RULE_op = 13;
	public static final String[] ruleNames = {
		"script", "scriptLine", "defLine", "defStrLine", "defStrType", "defIntLine", 
		"defIntType", "eventLine", "condList", "cond", "cond_op", "instList", 
		"inst", "op"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'var'", "'throttle'", "'sensor'", "'turnout'", 
		"'timer'", "'forward'", "'reverse'", "'normal'", "'sound'", "'light'", 
		"'horn'", "'stop'", "'stopped'", "'start'", null, "'->'", "'='", "'&'", 
		"'!'", "'+'", "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "WS", "EOL", "SB_COMMENT", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", 
		"KW_TURNOUT", "KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", 
		"KW_LIGHT", "KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_FN", "KW_ARROW", 
		"KW_EQUAL", "KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "ID", "NUM", "RESERVED"
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
			setState(28);
			scriptLine();
			setState(33);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(29);
					match(EOL);
					setState(30);
					scriptLine();
					}
					} 
				}
				setState(35);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(37);
			_la = _input.LA(1);
			if (_la==EOL) {
				{
				setState(36);
				match(EOL);
				}
			}

			setState(39);
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
			setState(43);
			switch (_input.LA(1)) {
			case KW_VAR:
			case KW_THROTTLE:
			case KW_SENSOR:
			case KW_TURNOUT:
			case KW_TIMER:
				{
				setState(41);
				defLine();
				}
				break;
			case KW_NOT:
			case ID:
				{
				setState(42);
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
			setState(46);
			_la = _input.LA(1);
			if (_la==SB_COMMENT) {
				{
				setState(45);
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
			setState(50);
			switch (_input.LA(1)) {
			case KW_SENSOR:
			case KW_TURNOUT:
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				defStrLine();
				}
				break;
			case KW_VAR:
			case KW_THROTTLE:
			case KW_TIMER:
				enterOuterAlt(_localctx, 2);
				{
				setState(49);
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
			setState(52);
			defStrType();
			setState(53);
			match(ID);
			setState(54);
			match(KW_EQUAL);
			setState(55);
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
			setState(57);
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
			setState(59);
			defIntType();
			setState(60);
			match(ID);
			setState(61);
			match(KW_EQUAL);
			setState(62);
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
			setState(64);
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
		public InstListContext instList() {
			return getRuleContext(InstListContext.class,0);
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
			setState(66);
			condList();
			setState(67);
			match(KW_ARROW);
			setState(68);
			instList();
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
			setState(70);
			cond();
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KW_AND) {
				{
				{
				setState(71);
				match(KW_AND);
				setState(72);
				cond();
				}
				}
				setState(77);
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
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
		public Cond_opContext cond_op() {
			return getRuleContext(Cond_opContext.class,0);
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
			setState(79);
			_la = _input.LA(1);
			if (_la==KW_NOT) {
				{
				setState(78);
				match(KW_NOT);
				}
			}

			setState(81);
			match(ID);
			setState(85);
			switch (_input.LA(1)) {
			case KW_PLUS:
				{
				setState(82);
				match(KW_PLUS);
				setState(83);
				match(NUM);
				}
				break;
			case KW_FORWARD:
			case KW_REVERSE:
			case KW_STOPPED:
				{
				setState(84);
				cond_op();
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

	public static class Cond_opContext extends ParserRuleContext {
		public TerminalNode KW_FORWARD() { return getToken(ConductorParser.KW_FORWARD, 0); }
		public TerminalNode KW_REVERSE() { return getToken(ConductorParser.KW_REVERSE, 0); }
		public TerminalNode KW_STOPPED() { return getToken(ConductorParser.KW_STOPPED, 0); }
		public Cond_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cond_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterCond_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitCond_op(this);
		}
	}

	public final Cond_opContext cond_op() throws RecognitionException {
		Cond_opContext _localctx = new Cond_opContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_cond_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_STOPPED))) != 0)) ) {
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

	public static class InstListContext extends ParserRuleContext {
		public List<InstContext> inst() {
			return getRuleContexts(InstContext.class);
		}
		public InstContext inst(int i) {
			return getRuleContext(InstContext.class,i);
		}
		public InstListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterInstList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitInstList(this);
		}
	}

	public final InstListContext instList() throws RecognitionException {
		InstListContext _localctx = new InstListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_instList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			inst();
			setState(94);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(90);
					match(KW_SEMI);
					setState(91);
					inst();
					}
					} 
				}
				setState(96);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			setState(98);
			_la = _input.LA(1);
			if (_la==KW_SEMI) {
				{
				setState(97);
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

	public static class InstContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(ConductorParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ConductorParser.ID, i);
		}
		public OpContext op() {
			return getRuleContext(OpContext.class,0);
		}
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
		public InstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitInst(this);
		}
	}

	public final InstContext inst() throws RecognitionException {
		InstContext _localctx = new InstContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_inst);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(ID);
			setState(102);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_NORMAL) | (1L << KW_SOUND) | (1L << KW_HORN) | (1L << KW_STOP) | (1L << KW_START) | (1L << KW_FN))) != 0)) {
				{
				setState(101);
				op();
				}
			}

			setState(106);
			_la = _input.LA(1);
			if (_la==KW_EQUAL) {
				{
				setState(104);
				match(KW_EQUAL);
				setState(105);
				_la = _input.LA(1);
				if ( !(_la==ID || _la==NUM) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
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

	public static class OpContext extends ParserRuleContext {
		public TerminalNode KW_FORWARD() { return getToken(ConductorParser.KW_FORWARD, 0); }
		public TerminalNode KW_REVERSE() { return getToken(ConductorParser.KW_REVERSE, 0); }
		public TerminalNode KW_NORMAL() { return getToken(ConductorParser.KW_NORMAL, 0); }
		public TerminalNode KW_SOUND() { return getToken(ConductorParser.KW_SOUND, 0); }
		public TerminalNode KW_HORN() { return getToken(ConductorParser.KW_HORN, 0); }
		public TerminalNode KW_STOP() { return getToken(ConductorParser.KW_STOP, 0); }
		public TerminalNode KW_START() { return getToken(ConductorParser.KW_START, 0); }
		public TerminalNode KW_FN() { return getToken(ConductorParser.KW_FN, 0); }
		public OpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitOp(this);
		}
	}

	public final OpContext op() throws RecognitionException {
		OpContext _localctx = new OpContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_NORMAL) | (1L << KW_SOUND) | (1L << KW_HORN) | (1L << KW_STOP) | (1L << KW_START) | (1L << KW_FN))) != 0)) ) {
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\35q\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\2\7\2\"\n\2\f\2\16\2%\13"+
		"\2\3\2\5\2(\n\2\3\2\3\2\3\3\3\3\5\3.\n\3\3\3\5\3\61\n\3\3\4\3\4\5\4\65"+
		"\n\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3"+
		"\t\3\t\3\n\3\n\3\n\7\nL\n\n\f\n\16\nO\13\n\3\13\5\13R\n\13\3\13\3\13\3"+
		"\13\3\13\5\13X\n\13\3\f\3\f\3\r\3\r\3\r\7\r_\n\r\f\r\16\rb\13\r\3\r\5"+
		"\re\n\r\3\16\3\16\5\16i\n\16\3\16\3\16\5\16m\n\16\3\17\3\17\3\17\2\2\20"+
		"\2\4\6\b\n\f\16\20\22\24\26\30\32\34\2\7\3\2\b\t\4\2\6\7\n\n\4\2\13\f"+
		"\22\22\3\2\33\34\5\2\13\16\20\21\23\24p\2\36\3\2\2\2\4-\3\2\2\2\6\64\3"+
		"\2\2\2\b\66\3\2\2\2\n;\3\2\2\2\f=\3\2\2\2\16B\3\2\2\2\20D\3\2\2\2\22H"+
		"\3\2\2\2\24Q\3\2\2\2\26Y\3\2\2\2\30[\3\2\2\2\32f\3\2\2\2\34n\3\2\2\2\36"+
		"#\5\4\3\2\37 \7\4\2\2 \"\5\4\3\2!\37\3\2\2\2\"%\3\2\2\2#!\3\2\2\2#$\3"+
		"\2\2\2$\'\3\2\2\2%#\3\2\2\2&(\7\4\2\2\'&\3\2\2\2\'(\3\2\2\2()\3\2\2\2"+
		")*\7\2\2\3*\3\3\2\2\2+.\5\6\4\2,.\5\20\t\2-+\3\2\2\2-,\3\2\2\2-.\3\2\2"+
		"\2.\60\3\2\2\2/\61\7\5\2\2\60/\3\2\2\2\60\61\3\2\2\2\61\5\3\2\2\2\62\65"+
		"\5\b\5\2\63\65\5\f\7\2\64\62\3\2\2\2\64\63\3\2\2\2\65\7\3\2\2\2\66\67"+
		"\5\n\6\2\678\7\33\2\289\7\26\2\29:\7\33\2\2:\t\3\2\2\2;<\t\2\2\2<\13\3"+
		"\2\2\2=>\5\16\b\2>?\7\33\2\2?@\7\26\2\2@A\7\34\2\2A\r\3\2\2\2BC\t\3\2"+
		"\2C\17\3\2\2\2DE\5\22\n\2EF\7\25\2\2FG\5\30\r\2G\21\3\2\2\2HM\5\24\13"+
		"\2IJ\7\27\2\2JL\5\24\13\2KI\3\2\2\2LO\3\2\2\2MK\3\2\2\2MN\3\2\2\2N\23"+
		"\3\2\2\2OM\3\2\2\2PR\7\30\2\2QP\3\2\2\2QR\3\2\2\2RS\3\2\2\2SW\7\33\2\2"+
		"TU\7\31\2\2UX\7\34\2\2VX\5\26\f\2WT\3\2\2\2WV\3\2\2\2WX\3\2\2\2X\25\3"+
		"\2\2\2YZ\t\4\2\2Z\27\3\2\2\2[`\5\32\16\2\\]\7\32\2\2]_\5\32\16\2^\\\3"+
		"\2\2\2_b\3\2\2\2`^\3\2\2\2`a\3\2\2\2ad\3\2\2\2b`\3\2\2\2ce\7\32\2\2dc"+
		"\3\2\2\2de\3\2\2\2e\31\3\2\2\2fh\7\33\2\2gi\5\34\17\2hg\3\2\2\2hi\3\2"+
		"\2\2il\3\2\2\2jk\7\26\2\2km\t\5\2\2lj\3\2\2\2lm\3\2\2\2m\33\3\2\2\2no"+
		"\t\6\2\2o\35\3\2\2\2\16#\'-\60\64MQW`dhl";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
