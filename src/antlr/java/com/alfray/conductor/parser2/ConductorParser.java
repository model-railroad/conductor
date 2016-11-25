// Generated from Conductor.g4 by ANTLR 4.5.3
package com.alfray.conductor.parser2;
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
		RULE_script = 0, RULE_scriptLine = 1, RULE_eol = 2, RULE_comment = 3, 
		RULE_defLine = 4, RULE_defType = 5, RULE_eventLine = 6, RULE_condList = 7, 
		RULE_cond = 8, RULE_cond_op = 9, RULE_instList = 10, RULE_inst = 11, RULE_op = 12;
	public static final String[] ruleNames = {
		"script", "scriptLine", "eol", "comment", "defLine", "defType", "eventLine", 
		"condList", "cond", "cond_op", "instList", "inst", "op"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'Var'", "'Throttle'", "'Sensor'", "'Turnout'", 
		"'Timer'", "'Forward'", "'Reverse'", "'Normal'", "'Sound'", "'Light'", 
		"'Horn'", "'Stop'", "'Stopped'", "'Start'", null, "'->'", "'='", "'&'", 
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
		public TerminalNode EOF() { return getToken(ConductorParser.EOF, 0); }
		public List<ScriptLineContext> scriptLine() {
			return getRuleContexts(ScriptLineContext.class);
		}
		public ScriptLineContext scriptLine(int i) {
			return getRuleContext(ScriptLineContext.class,i);
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
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EOL) | (1L << SB_COMMENT) | (1L << KW_VAR) | (1L << KW_THROTTLE) | (1L << KW_SENSOR) | (1L << KW_TURNOUT) | (1L << KW_TIMER) | (1L << KW_NOT) | (1L << ID))) != 0)) {
				{
				{
				setState(26);
				scriptLine();
				}
				}
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(32);
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
		public EolContext eol() {
			return getRuleContext(EolContext.class,0);
		}
		public DefLineContext defLine() {
			return getRuleContext(DefLineContext.class,0);
		}
		public EventLineContext eventLine() {
			return getRuleContext(EventLineContext.class,0);
		}
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			switch (_input.LA(1)) {
			case KW_VAR:
			case KW_THROTTLE:
			case KW_SENSOR:
			case KW_TURNOUT:
			case KW_TIMER:
				{
				setState(34);
				defLine();
				}
				break;
			case KW_NOT:
			case ID:
				{
				setState(35);
				eventLine();
				}
				break;
			case EOL:
			case SB_COMMENT:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(38);
			eol();
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

	public static class EolContext extends ParserRuleContext {
		public TerminalNode EOL() { return getToken(ConductorParser.EOL, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public EolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterEol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitEol(this);
		}
	}

	public final EolContext eol() throws RecognitionException {
		EolContext _localctx = new EolContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_eol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			_la = _input.LA(1);
			if (_la==SB_COMMENT) {
				{
				setState(40);
				comment();
				}
			}

			setState(43);
			match(EOL);
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

	public static class CommentContext extends ParserRuleContext {
		public TerminalNode SB_COMMENT() { return getToken(ConductorParser.SB_COMMENT, 0); }
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitComment(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			match(SB_COMMENT);
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
		public DefTypeContext defType() {
			return getRuleContext(DefTypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(ConductorParser.ID, 0); }
		public TerminalNode NUM() { return getToken(ConductorParser.NUM, 0); }
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
		enterRule(_localctx, 8, RULE_defLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			defType();
			setState(48);
			match(ID);
			setState(49);
			match(KW_EQUAL);
			setState(50);
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

	public static class DefTypeContext extends ParserRuleContext {
		public TerminalNode KW_VAR() { return getToken(ConductorParser.KW_VAR, 0); }
		public TerminalNode KW_THROTTLE() { return getToken(ConductorParser.KW_THROTTLE, 0); }
		public TerminalNode KW_SENSOR() { return getToken(ConductorParser.KW_SENSOR, 0); }
		public TerminalNode KW_TURNOUT() { return getToken(ConductorParser.KW_TURNOUT, 0); }
		public TerminalNode KW_TIMER() { return getToken(ConductorParser.KW_TIMER, 0); }
		public DefTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).enterDefType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ConductorListener ) ((ConductorListener)listener).exitDefType(this);
		}
	}

	public final DefTypeContext defType() throws RecognitionException {
		DefTypeContext _localctx = new DefTypeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_defType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_VAR) | (1L << KW_THROTTLE) | (1L << KW_SENSOR) | (1L << KW_TURNOUT) | (1L << KW_TIMER))) != 0)) ) {
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
		enterRule(_localctx, 12, RULE_eventLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			condList();
			setState(55);
			match(KW_ARROW);
			setState(56);
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
		enterRule(_localctx, 14, RULE_condList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
			cond();
			setState(63);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==KW_AND) {
				{
				{
				setState(59);
				match(KW_AND);
				setState(60);
				cond();
				}
				}
				setState(65);
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
		enterRule(_localctx, 16, RULE_cond);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_la = _input.LA(1);
			if (_la==KW_NOT) {
				{
				setState(66);
				match(KW_NOT);
				}
			}

			setState(69);
			match(ID);
			setState(73);
			switch (_input.LA(1)) {
			case KW_PLUS:
				{
				setState(70);
				match(KW_PLUS);
				setState(71);
				match(NUM);
				}
				break;
			case KW_FORWARD:
			case KW_REVERSE:
			case KW_STOPPED:
				{
				setState(72);
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
		enterRule(_localctx, 18, RULE_cond_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
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
		enterRule(_localctx, 20, RULE_instList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			inst();
			setState(82);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(78);
					match(KW_SEMI);
					setState(79);
					inst();
					}
					} 
				}
				setState(84);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			setState(86);
			_la = _input.LA(1);
			if (_la==KW_SEMI) {
				{
				setState(85);
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
		enterRule(_localctx, 22, RULE_inst);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(ID);
			setState(90);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << KW_FORWARD) | (1L << KW_REVERSE) | (1L << KW_NORMAL) | (1L << KW_SOUND) | (1L << KW_HORN) | (1L << KW_STOP) | (1L << KW_START) | (1L << KW_FN))) != 0)) {
				{
				setState(89);
				op();
				}
			}

			setState(94);
			_la = _input.LA(1);
			if (_la==KW_EQUAL) {
				{
				setState(92);
				match(KW_EQUAL);
				setState(93);
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
		enterRule(_localctx, 24, RULE_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\35e\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\4\16\t\16\3\2\7\2\36\n\2\f\2\16\2!\13\2\3\2\3\2\3\3\3\3"+
		"\5\3\'\n\3\3\3\3\3\3\4\5\4,\n\4\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3"+
		"\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\7\t@\n\t\f\t\16\tC\13\t\3\n\5\nF\n"+
		"\n\3\n\3\n\3\n\3\n\5\nL\n\n\3\13\3\13\3\f\3\f\3\f\7\fS\n\f\f\f\16\fV\13"+
		"\f\3\f\5\fY\n\f\3\r\3\r\5\r]\n\r\3\r\3\r\5\ra\n\r\3\16\3\16\3\16\2\2\17"+
		"\2\4\6\b\n\f\16\20\22\24\26\30\32\2\6\3\2\6\n\4\2\13\f\22\22\3\2\33\34"+
		"\5\2\13\16\20\21\23\24c\2\37\3\2\2\2\4&\3\2\2\2\6+\3\2\2\2\b/\3\2\2\2"+
		"\n\61\3\2\2\2\f\66\3\2\2\2\168\3\2\2\2\20<\3\2\2\2\22E\3\2\2\2\24M\3\2"+
		"\2\2\26O\3\2\2\2\30Z\3\2\2\2\32b\3\2\2\2\34\36\5\4\3\2\35\34\3\2\2\2\36"+
		"!\3\2\2\2\37\35\3\2\2\2\37 \3\2\2\2 \"\3\2\2\2!\37\3\2\2\2\"#\7\2\2\3"+
		"#\3\3\2\2\2$\'\5\n\6\2%\'\5\16\b\2&$\3\2\2\2&%\3\2\2\2&\'\3\2\2\2\'(\3"+
		"\2\2\2()\5\6\4\2)\5\3\2\2\2*,\5\b\5\2+*\3\2\2\2+,\3\2\2\2,-\3\2\2\2-."+
		"\7\4\2\2.\7\3\2\2\2/\60\7\5\2\2\60\t\3\2\2\2\61\62\5\f\7\2\62\63\7\33"+
		"\2\2\63\64\7\26\2\2\64\65\7\34\2\2\65\13\3\2\2\2\66\67\t\2\2\2\67\r\3"+
		"\2\2\289\5\20\t\29:\7\25\2\2:;\5\26\f\2;\17\3\2\2\2<A\5\22\n\2=>\7\27"+
		"\2\2>@\5\22\n\2?=\3\2\2\2@C\3\2\2\2A?\3\2\2\2AB\3\2\2\2B\21\3\2\2\2CA"+
		"\3\2\2\2DF\7\30\2\2ED\3\2\2\2EF\3\2\2\2FG\3\2\2\2GK\7\33\2\2HI\7\31\2"+
		"\2IL\7\34\2\2JL\5\24\13\2KH\3\2\2\2KJ\3\2\2\2KL\3\2\2\2L\23\3\2\2\2MN"+
		"\t\3\2\2N\25\3\2\2\2OT\5\30\r\2PQ\7\32\2\2QS\5\30\r\2RP\3\2\2\2SV\3\2"+
		"\2\2TR\3\2\2\2TU\3\2\2\2UX\3\2\2\2VT\3\2\2\2WY\7\32\2\2XW\3\2\2\2XY\3"+
		"\2\2\2Y\27\3\2\2\2Z\\\7\33\2\2[]\5\32\16\2\\[\3\2\2\2\\]\3\2\2\2]`\3\2"+
		"\2\2^_\7\26\2\2_a\t\4\2\2`^\3\2\2\2`a\3\2\2\2a\31\3\2\2\2bc\t\5\2\2c\33"+
		"\3\2\2\2\f\37&+AEKTX\\`";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
