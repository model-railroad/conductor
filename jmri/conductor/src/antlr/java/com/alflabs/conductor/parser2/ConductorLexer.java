// Generated from C:\dev\ralf\RalfDev\bitbucket\ralfoide\randall-layout\jmri\conductor\src\antlr\antlr\Conductor.g4 by ANTLR 4.5.3
package com.alflabs.conductor.parser2;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ConductorLexer extends Lexer {
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
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_IS_EQ", "KW_IS_NEQ", 
		"KW_EQUAL", "KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "KW_END", "KW_ENUM", 
		"KW_FORWARD", "KW_HORN", "KW_LIGHT", "KW_MAP", "KW_NORMAL", "KW_RESET", 
		"KW_REVERSE", "KW_ROUTE", "KW_SENSOR", "KW_SOUND", "KW_START", "KW_STATUS", 
		"KW_STOP", "KW_STOPPED", "KW_THROTTLE", "KW_TIMER", "KW_TIMERS", "KW_TOGGLE", 
		"KW_TURNOUT", "KW_VAR", "KW_FN", "KW_F0", "KW_F10", "KW_F20", "ID", "NUM", 
		"STR", "IdCharStart", "IdCharFull", "IdCharLast", "IdUnreserved", "IdNum", 
		"IdLetter", "IdDash", "IdDoubleQuote"
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


	public ConductorLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Conductor.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2)\u015f\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2\3\2"+
		"\3\3\3\3\3\4\6\4m\n\4\r\4\16\4n\3\4\3\4\3\5\6\5t\n\5\r\5\16\5u\3\6\3\6"+
		"\7\6z\n\6\f\6\16\6}\13\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3"+
		"\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20"+
		"\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22"+
		"\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31"+
		"\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\3\37\3 \3 \3 \3 \3 \3 \3!\3!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\""+
		"\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3#\3$\3$\3$\3$\3%\3%\3%\5%\u0120\n%\3&\3"+
		"&\3&\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3)\3)\7)\u0131\n)\f)\16)\u0134"+
		"\13)\3)\5)\u0137\n)\3*\6*\u013a\n*\r*\16*\u013b\3+\3+\7+\u0140\n+\f+\16"+
		"+\u0143\13+\3+\3+\3,\3,\5,\u0149\n,\3-\3-\3-\3-\5-\u014f\n-\3.\3.\3.\5"+
		".\u0154\n.\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\2\2\64\3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!"+
		"A\"C#E$G%I&K\2M\2O\2Q\'S(U)W\2Y\2[\2]\2_\2a\2c\2e\2\3\2\t\5\2\13\13\16"+
		"\16\"\"\4\2\f\f\17\17\3\2\62;\3\2\62:\5\2\f\f\17\17$$\t\2&\'),\60\61A"+
		"A]b}}\177\u0080\4\2C\\c|\u0162\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2"+
		"\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2"+
		"+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2"+
		"\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2"+
		"C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3"+
		"\2\2\2\3g\3\2\2\2\5i\3\2\2\2\7l\3\2\2\2\ts\3\2\2\2\13w\3\2\2\2\r~\3\2"+
		"\2\2\17\u0081\3\2\2\2\21\u0084\3\2\2\2\23\u0087\3\2\2\2\25\u0089\3\2\2"+
		"\2\27\u008b\3\2\2\2\31\u008d\3\2\2\2\33\u008f\3\2\2\2\35\u0091\3\2\2\2"+
		"\37\u0095\3\2\2\2!\u009a\3\2\2\2#\u00a2\3\2\2\2%\u00a7\3\2\2\2\'\u00ad"+
		"\3\2\2\2)\u00b1\3\2\2\2+\u00b8\3\2\2\2-\u00be\3\2\2\2/\u00c6\3\2\2\2\61"+
		"\u00cc\3\2\2\2\63\u00d3\3\2\2\2\65\u00d9\3\2\2\2\67\u00df\3\2\2\29\u00e6"+
		"\3\2\2\2;\u00eb\3\2\2\2=\u00f3\3\2\2\2?\u00fc\3\2\2\2A\u0102\3\2\2\2C"+
		"\u0109\3\2\2\2E\u0110\3\2\2\2G\u0118\3\2\2\2I\u011f\3\2\2\2K\u0121\3\2"+
		"\2\2M\u0124\3\2\2\2O\u0129\3\2\2\2Q\u012e\3\2\2\2S\u0139\3\2\2\2U\u013d"+
		"\3\2\2\2W\u0148\3\2\2\2Y\u014e\3\2\2\2[\u0153\3\2\2\2]\u0155\3\2\2\2_"+
		"\u0157\3\2\2\2a\u0159\3\2\2\2c\u015b\3\2\2\2e\u015d\3\2\2\2gh\7.\2\2h"+
		"\4\3\2\2\2ij\7<\2\2j\6\3\2\2\2km\t\2\2\2lk\3\2\2\2mn\3\2\2\2nl\3\2\2\2"+
		"no\3\2\2\2op\3\2\2\2pq\b\4\2\2q\b\3\2\2\2rt\t\3\2\2sr\3\2\2\2tu\3\2\2"+
		"\2us\3\2\2\2uv\3\2\2\2v\n\3\2\2\2w{\7%\2\2xz\n\3\2\2yx\3\2\2\2z}\3\2\2"+
		"\2{y\3\2\2\2{|\3\2\2\2|\f\3\2\2\2}{\3\2\2\2~\177\7/\2\2\177\u0080\7@\2"+
		"\2\u0080\16\3\2\2\2\u0081\u0082\7?\2\2\u0082\u0083\7?\2\2\u0083\20\3\2"+
		"\2\2\u0084\u0085\7#\2\2\u0085\u0086\7?\2\2\u0086\22\3\2\2\2\u0087\u0088"+
		"\7?\2\2\u0088\24\3\2\2\2\u0089\u008a\7(\2\2\u008a\26\3\2\2\2\u008b\u008c"+
		"\7#\2\2\u008c\30\3\2\2\2\u008d\u008e\7-\2\2\u008e\32\3\2\2\2\u008f\u0090"+
		"\7=\2\2\u0090\34\3\2\2\2\u0091\u0092\7g\2\2\u0092\u0093\7p\2\2\u0093\u0094"+
		"\7f\2\2\u0094\36\3\2\2\2\u0095\u0096\7g\2\2\u0096\u0097\7p\2\2\u0097\u0098"+
		"\7w\2\2\u0098\u0099\7o\2\2\u0099 \3\2\2\2\u009a\u009b\7h\2\2\u009b\u009c"+
		"\7q\2\2\u009c\u009d\7t\2\2\u009d\u009e\7y\2\2\u009e\u009f\7c\2\2\u009f"+
		"\u00a0\7t\2\2\u00a0\u00a1\7f\2\2\u00a1\"\3\2\2\2\u00a2\u00a3\7j\2\2\u00a3"+
		"\u00a4\7q\2\2\u00a4\u00a5\7t\2\2\u00a5\u00a6\7p\2\2\u00a6$\3\2\2\2\u00a7"+
		"\u00a8\7n\2\2\u00a8\u00a9\7k\2\2\u00a9\u00aa\7i\2\2\u00aa\u00ab\7j\2\2"+
		"\u00ab\u00ac\7v\2\2\u00ac&\3\2\2\2\u00ad\u00ae\7o\2\2\u00ae\u00af\7c\2"+
		"\2\u00af\u00b0\7r\2\2\u00b0(\3\2\2\2\u00b1\u00b2\7p\2\2\u00b2\u00b3\7"+
		"q\2\2\u00b3\u00b4\7t\2\2\u00b4\u00b5\7o\2\2\u00b5\u00b6\7c\2\2\u00b6\u00b7"+
		"\7n\2\2\u00b7*\3\2\2\2\u00b8\u00b9\7t\2\2\u00b9\u00ba\7g\2\2\u00ba\u00bb"+
		"\7u\2\2\u00bb\u00bc\7g\2\2\u00bc\u00bd\7v\2\2\u00bd,\3\2\2\2\u00be\u00bf"+
		"\7t\2\2\u00bf\u00c0\7g\2\2\u00c0\u00c1\7x\2\2\u00c1\u00c2\7g\2\2\u00c2"+
		"\u00c3\7t\2\2\u00c3\u00c4\7u\2\2\u00c4\u00c5\7g\2\2\u00c5.\3\2\2\2\u00c6"+
		"\u00c7\7t\2\2\u00c7\u00c8\7q\2\2\u00c8\u00c9\7w\2\2\u00c9\u00ca\7v\2\2"+
		"\u00ca\u00cb\7g\2\2\u00cb\60\3\2\2\2\u00cc\u00cd\7u\2\2\u00cd\u00ce\7"+
		"g\2\2\u00ce\u00cf\7p\2\2\u00cf\u00d0\7u\2\2\u00d0\u00d1\7q\2\2\u00d1\u00d2"+
		"\7t\2\2\u00d2\62\3\2\2\2\u00d3\u00d4\7u\2\2\u00d4\u00d5\7q\2\2\u00d5\u00d6"+
		"\7w\2\2\u00d6\u00d7\7p\2\2\u00d7\u00d8\7f\2\2\u00d8\64\3\2\2\2\u00d9\u00da"+
		"\7u\2\2\u00da\u00db\7v\2\2\u00db\u00dc\7c\2\2\u00dc\u00dd\7t\2\2\u00dd"+
		"\u00de\7v\2\2\u00de\66\3\2\2\2\u00df\u00e0\7u\2\2\u00e0\u00e1\7v\2\2\u00e1"+
		"\u00e2\7c\2\2\u00e2\u00e3\7v\2\2\u00e3\u00e4\7w\2\2\u00e4\u00e5\7u\2\2"+
		"\u00e58\3\2\2\2\u00e6\u00e7\7u\2\2\u00e7\u00e8\7v\2\2\u00e8\u00e9\7q\2"+
		"\2\u00e9\u00ea\7r\2\2\u00ea:\3\2\2\2\u00eb\u00ec\7u\2\2\u00ec\u00ed\7"+
		"v\2\2\u00ed\u00ee\7q\2\2\u00ee\u00ef\7r\2\2\u00ef\u00f0\7r\2\2\u00f0\u00f1"+
		"\7g\2\2\u00f1\u00f2\7f\2\2\u00f2<\3\2\2\2\u00f3\u00f4\7v\2\2\u00f4\u00f5"+
		"\7j\2\2\u00f5\u00f6\7t\2\2\u00f6\u00f7\7q\2\2\u00f7\u00f8\7v\2\2\u00f8"+
		"\u00f9\7v\2\2\u00f9\u00fa\7n\2\2\u00fa\u00fb\7g\2\2\u00fb>\3\2\2\2\u00fc"+
		"\u00fd\7v\2\2\u00fd\u00fe\7k\2\2\u00fe\u00ff\7o\2\2\u00ff\u0100\7g\2\2"+
		"\u0100\u0101\7t\2\2\u0101@\3\2\2\2\u0102\u0103\7v\2\2\u0103\u0104\7k\2"+
		"\2\u0104\u0105\7o\2\2\u0105\u0106\7g\2\2\u0106\u0107\7t\2\2\u0107\u0108"+
		"\7u\2\2\u0108B\3\2\2\2\u0109\u010a\7v\2\2\u010a\u010b\7q\2\2\u010b\u010c"+
		"\7i\2\2\u010c\u010d\7i\2\2\u010d\u010e\7n\2\2\u010e\u010f\7g\2\2\u010f"+
		"D\3\2\2\2\u0110\u0111\7v\2\2\u0111\u0112\7w\2\2\u0112\u0113\7t\2\2\u0113"+
		"\u0114\7p\2\2\u0114\u0115\7q\2\2\u0115\u0116\7w\2\2\u0116\u0117\7v\2\2"+
		"\u0117F\3\2\2\2\u0118\u0119\7x\2\2\u0119\u011a\7c\2\2\u011a\u011b\7t\2"+
		"\2\u011bH\3\2\2\2\u011c\u0120\5K&\2\u011d\u0120\5M\'\2\u011e\u0120\5O"+
		"(\2\u011f\u011c\3\2\2\2\u011f\u011d\3\2\2\2\u011f\u011e\3\2\2\2\u0120"+
		"J\3\2\2\2\u0121\u0122\7h\2\2\u0122\u0123\t\4\2\2\u0123L\3\2\2\2\u0124"+
		"\u0125\7h\2\2\u0125\u0126\7\63\2\2\u0126\u0127\3\2\2\2\u0127\u0128\t\4"+
		"\2\2\u0128N\3\2\2\2\u0129\u012a\7h\2\2\u012a\u012b\7\64\2\2\u012b\u012c"+
		"\3\2\2\2\u012c\u012d\t\5\2\2\u012dP\3\2\2\2\u012e\u0136\5W,\2\u012f\u0131"+
		"\5Y-\2\u0130\u012f\3\2\2\2\u0131\u0134\3\2\2\2\u0132\u0130\3\2\2\2\u0132"+
		"\u0133\3\2\2\2\u0133\u0135\3\2\2\2\u0134\u0132\3\2\2\2\u0135\u0137\5["+
		".\2\u0136\u0132\3\2\2\2\u0136\u0137\3\2\2\2\u0137R\3\2\2\2\u0138\u013a"+
		"\5_\60\2\u0139\u0138\3\2\2\2\u013a\u013b\3\2\2\2\u013b\u0139\3\2\2\2\u013b"+
		"\u013c\3\2\2\2\u013cT\3\2\2\2\u013d\u0141\7$\2\2\u013e\u0140\n\6\2\2\u013f"+
		"\u013e\3\2\2\2\u0140\u0143\3\2\2\2\u0141\u013f\3\2\2\2\u0141\u0142\3\2"+
		"\2\2\u0142\u0144\3\2\2\2\u0143\u0141\3\2\2\2\u0144\u0145\7$\2\2\u0145"+
		"V\3\2\2\2\u0146\u0149\5]/\2\u0147\u0149\5a\61\2\u0148\u0146\3\2\2\2\u0148"+
		"\u0147\3\2\2\2\u0149X\3\2\2\2\u014a\u014f\5]/\2\u014b\u014f\5a\61\2\u014c"+
		"\u014f\5_\60\2\u014d\u014f\5c\62\2\u014e\u014a\3\2\2\2\u014e\u014b\3\2"+
		"\2\2\u014e\u014c\3\2\2\2\u014e\u014d\3\2\2\2\u014fZ\3\2\2\2\u0150\u0154"+
		"\5]/\2\u0151\u0154\5a\61\2\u0152\u0154\5_\60\2\u0153\u0150\3\2\2\2\u0153"+
		"\u0151\3\2\2\2\u0153\u0152\3\2\2\2\u0154\\\3\2\2\2\u0155\u0156\t\7\2\2"+
		"\u0156^\3\2\2\2\u0157\u0158\t\4\2\2\u0158`\3\2\2\2\u0159\u015a\t\b\2\2"+
		"\u015ab\3\2\2\2\u015b\u015c\7/\2\2\u015cd\3\2\2\2\u015d\u015e\7$\2\2\u015e"+
		"f\3\2\2\2\16\2nu{\u011f\u0132\u0136\u013b\u0141\u0148\u014e\u0153\3\b"+
		"\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}