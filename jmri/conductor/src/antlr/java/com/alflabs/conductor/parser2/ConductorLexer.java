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
		WS=1, EOL=2, SB_COMMENT=3, KW_ARROW=4, KW_IS_EQ=5, KW_IS_NEQ=6, KW_EQUAL=7, 
		KW_AND=8, KW_NOT=9, KW_PLUS=10, KW_SEMI=11, KW_MAP=12, KW_VAR=13, KW_ENUM=14, 
		KW_THROTTLE=15, KW_SENSOR=16, KW_TURNOUT=17, KW_TIMER=18, KW_FORWARD=19, 
		KW_REVERSE=20, KW_NORMAL=21, KW_SOUND=22, KW_LIGHT=23, KW_HORN=24, KW_STOP=25, 
		KW_STOPPED=26, KW_START=27, KW_END=28, KW_RESET=29, KW_TIMERS=30, KW_FN=31, 
		ID=32, NUM=33, STR=34;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_IS_EQ", "KW_IS_NEQ", "KW_EQUAL", 
		"KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "KW_MAP", "KW_VAR", "KW_ENUM", 
		"KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", "KW_TIMER", "KW_FORWARD", "KW_REVERSE", 
		"KW_NORMAL", "KW_SOUND", "KW_LIGHT", "KW_HORN", "KW_STOP", "KW_STOPPED", 
		"KW_START", "KW_END", "KW_RESET", "KW_TIMERS", "KW_FN", "KW_F0", "KW_F10", 
		"KW_F20", "ID", "NUM", "STR", "IdCharStart", "IdCharFull", "IdCharLast", 
		"IdUnreserved", "IdNum", "IdLetter", "IdDash", "IdDoubleQuote"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2$\u013e\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\3\2\6\2_\n\2\r\2\16\2`\3\2\3\2\3\3\6\3f\n\3\r\3\16\3"+
		"g\3\4\3\4\7\4l\n\4\f\4\16\4o\13\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7"+
		"\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3"+
		"\16\3\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3"+
		"\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3"+
		"\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3"+
		"\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3"+
		"\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3"+
		"\30\3\30\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3"+
		"\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3"+
		"\35\3\35\3\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3"+
		"\37\3 \3 \3 \5 \u00fe\n \3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3"+
		"$\3$\7$\u010f\n$\f$\16$\u0112\13$\3$\5$\u0115\n$\3%\6%\u0118\n%\r%\16"+
		"%\u0119\3&\3&\7&\u011e\n&\f&\16&\u0121\13&\3&\3&\3\'\3\'\5\'\u0127\n\'"+
		"\3(\3(\3(\3(\3(\5(\u012e\n(\3)\3)\3)\5)\u0133\n)\3*\3*\3+\3+\3,\3,\3-"+
		"\3-\3.\3.\2\2/\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34"+
		"\67\359\36;\37= ?!A\2C\2E\2G\"I#K$M\2O\2Q\2S\2U\2W\2Y\2[\2\3\2\t\5\2\13"+
		"\13\16\16\"\"\4\2\f\f\17\17\3\2\62;\3\2\62:\5\2\f\f\17\17$$\13\2&\'),"+
		"..\60\61<<AA]b}}\177\u0080\4\2C\\c|\u0142\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2"+
		"\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2"+
		"\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2"+
		"\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2"+
		"\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\3^\3\2\2\2\5e\3\2\2\2\7i\3\2\2\2\tp"+
		"\3\2\2\2\13s\3\2\2\2\rv\3\2\2\2\17y\3\2\2\2\21{\3\2\2\2\23}\3\2\2\2\25"+
		"\177\3\2\2\2\27\u0081\3\2\2\2\31\u0083\3\2\2\2\33\u0087\3\2\2\2\35\u008b"+
		"\3\2\2\2\37\u0090\3\2\2\2!\u0099\3\2\2\2#\u00a0\3\2\2\2%\u00a8\3\2\2\2"+
		"\'\u00ae\3\2\2\2)\u00b6\3\2\2\2+\u00be\3\2\2\2-\u00c5\3\2\2\2/\u00cb\3"+
		"\2\2\2\61\u00d1\3\2\2\2\63\u00d6\3\2\2\2\65\u00db\3\2\2\2\67\u00e3\3\2"+
		"\2\29\u00e9\3\2\2\2;\u00ed\3\2\2\2=\u00f3\3\2\2\2?\u00fd\3\2\2\2A\u00ff"+
		"\3\2\2\2C\u0102\3\2\2\2E\u0107\3\2\2\2G\u010c\3\2\2\2I\u0117\3\2\2\2K"+
		"\u011b\3\2\2\2M\u0126\3\2\2\2O\u012d\3\2\2\2Q\u0132\3\2\2\2S\u0134\3\2"+
		"\2\2U\u0136\3\2\2\2W\u0138\3\2\2\2Y\u013a\3\2\2\2[\u013c\3\2\2\2]_\t\2"+
		"\2\2^]\3\2\2\2_`\3\2\2\2`^\3\2\2\2`a\3\2\2\2ab\3\2\2\2bc\b\2\2\2c\4\3"+
		"\2\2\2df\t\3\2\2ed\3\2\2\2fg\3\2\2\2ge\3\2\2\2gh\3\2\2\2h\6\3\2\2\2im"+
		"\7%\2\2jl\n\3\2\2kj\3\2\2\2lo\3\2\2\2mk\3\2\2\2mn\3\2\2\2n\b\3\2\2\2o"+
		"m\3\2\2\2pq\7/\2\2qr\7@\2\2r\n\3\2\2\2st\7?\2\2tu\7?\2\2u\f\3\2\2\2vw"+
		"\7#\2\2wx\7?\2\2x\16\3\2\2\2yz\7?\2\2z\20\3\2\2\2{|\7(\2\2|\22\3\2\2\2"+
		"}~\7#\2\2~\24\3\2\2\2\177\u0080\7-\2\2\u0080\26\3\2\2\2\u0081\u0082\7"+
		"=\2\2\u0082\30\3\2\2\2\u0083\u0084\7o\2\2\u0084\u0085\7c\2\2\u0085\u0086"+
		"\7r\2\2\u0086\32\3\2\2\2\u0087\u0088\7x\2\2\u0088\u0089\7c\2\2\u0089\u008a"+
		"\7t\2\2\u008a\34\3\2\2\2\u008b\u008c\7g\2\2\u008c\u008d\7p\2\2\u008d\u008e"+
		"\7w\2\2\u008e\u008f\7o\2\2\u008f\36\3\2\2\2\u0090\u0091\7v\2\2\u0091\u0092"+
		"\7j\2\2\u0092\u0093\7t\2\2\u0093\u0094\7q\2\2\u0094\u0095\7v\2\2\u0095"+
		"\u0096\7v\2\2\u0096\u0097\7n\2\2\u0097\u0098\7g\2\2\u0098 \3\2\2\2\u0099"+
		"\u009a\7u\2\2\u009a\u009b\7g\2\2\u009b\u009c\7p\2\2\u009c\u009d\7u\2\2"+
		"\u009d\u009e\7q\2\2\u009e\u009f\7t\2\2\u009f\"\3\2\2\2\u00a0\u00a1\7v"+
		"\2\2\u00a1\u00a2\7w\2\2\u00a2\u00a3\7t\2\2\u00a3\u00a4\7p\2\2\u00a4\u00a5"+
		"\7q\2\2\u00a5\u00a6\7w\2\2\u00a6\u00a7\7v\2\2\u00a7$\3\2\2\2\u00a8\u00a9"+
		"\7v\2\2\u00a9\u00aa\7k\2\2\u00aa\u00ab\7o\2\2\u00ab\u00ac\7g\2\2\u00ac"+
		"\u00ad\7t\2\2\u00ad&\3\2\2\2\u00ae\u00af\7h\2\2\u00af\u00b0\7q\2\2\u00b0"+
		"\u00b1\7t\2\2\u00b1\u00b2\7y\2\2\u00b2\u00b3\7c\2\2\u00b3\u00b4\7t\2\2"+
		"\u00b4\u00b5\7f\2\2\u00b5(\3\2\2\2\u00b6\u00b7\7t\2\2\u00b7\u00b8\7g\2"+
		"\2\u00b8\u00b9\7x\2\2\u00b9\u00ba\7g\2\2\u00ba\u00bb\7t\2\2\u00bb\u00bc"+
		"\7u\2\2\u00bc\u00bd\7g\2\2\u00bd*\3\2\2\2\u00be\u00bf\7p\2\2\u00bf\u00c0"+
		"\7q\2\2\u00c0\u00c1\7t\2\2\u00c1\u00c2\7o\2\2\u00c2\u00c3\7c\2\2\u00c3"+
		"\u00c4\7n\2\2\u00c4,\3\2\2\2\u00c5\u00c6\7u\2\2\u00c6\u00c7\7q\2\2\u00c7"+
		"\u00c8\7w\2\2\u00c8\u00c9\7p\2\2\u00c9\u00ca\7f\2\2\u00ca.\3\2\2\2\u00cb"+
		"\u00cc\7n\2\2\u00cc\u00cd\7k\2\2\u00cd\u00ce\7i\2\2\u00ce\u00cf\7j\2\2"+
		"\u00cf\u00d0\7v\2\2\u00d0\60\3\2\2\2\u00d1\u00d2\7j\2\2\u00d2\u00d3\7"+
		"q\2\2\u00d3\u00d4\7t\2\2\u00d4\u00d5\7p\2\2\u00d5\62\3\2\2\2\u00d6\u00d7"+
		"\7u\2\2\u00d7\u00d8\7v\2\2\u00d8\u00d9\7q\2\2\u00d9\u00da\7r\2\2\u00da"+
		"\64\3\2\2\2\u00db\u00dc\7u\2\2\u00dc\u00dd\7v\2\2\u00dd\u00de\7q\2\2\u00de"+
		"\u00df\7r\2\2\u00df\u00e0\7r\2\2\u00e0\u00e1\7g\2\2\u00e1\u00e2\7f\2\2"+
		"\u00e2\66\3\2\2\2\u00e3\u00e4\7u\2\2\u00e4\u00e5\7v\2\2\u00e5\u00e6\7"+
		"c\2\2\u00e6\u00e7\7t\2\2\u00e7\u00e8\7v\2\2\u00e88\3\2\2\2\u00e9\u00ea"+
		"\7g\2\2\u00ea\u00eb\7p\2\2\u00eb\u00ec\7f\2\2\u00ec:\3\2\2\2\u00ed\u00ee"+
		"\7t\2\2\u00ee\u00ef\7g\2\2\u00ef\u00f0\7u\2\2\u00f0\u00f1\7g\2\2\u00f1"+
		"\u00f2\7v\2\2\u00f2<\3\2\2\2\u00f3\u00f4\7v\2\2\u00f4\u00f5\7k\2\2\u00f5"+
		"\u00f6\7o\2\2\u00f6\u00f7\7g\2\2\u00f7\u00f8\7t\2\2\u00f8\u00f9\7u\2\2"+
		"\u00f9>\3\2\2\2\u00fa\u00fe\5A!\2\u00fb\u00fe\5C\"\2\u00fc\u00fe\5E#\2"+
		"\u00fd\u00fa\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fc\3\2\2\2\u00fe@\3"+
		"\2\2\2\u00ff\u0100\7h\2\2\u0100\u0101\t\4\2\2\u0101B\3\2\2\2\u0102\u0103"+
		"\7h\2\2\u0103\u0104\7\63\2\2\u0104\u0105\3\2\2\2\u0105\u0106\t\4\2\2\u0106"+
		"D\3\2\2\2\u0107\u0108\7h\2\2\u0108\u0109\7\64\2\2\u0109\u010a\3\2\2\2"+
		"\u010a\u010b\t\5\2\2\u010bF\3\2\2\2\u010c\u0114\5M\'\2\u010d\u010f\5O"+
		"(\2\u010e\u010d\3\2\2\2\u010f\u0112\3\2\2\2\u0110\u010e\3\2\2\2\u0110"+
		"\u0111\3\2\2\2\u0111\u0113\3\2\2\2\u0112\u0110\3\2\2\2\u0113\u0115\5Q"+
		")\2\u0114\u0110\3\2\2\2\u0114\u0115\3\2\2\2\u0115H\3\2\2\2\u0116\u0118"+
		"\5U+\2\u0117\u0116\3\2\2\2\u0118\u0119\3\2\2\2\u0119\u0117\3\2\2\2\u0119"+
		"\u011a\3\2\2\2\u011aJ\3\2\2\2\u011b\u011f\7$\2\2\u011c\u011e\n\6\2\2\u011d"+
		"\u011c\3\2\2\2\u011e\u0121\3\2\2\2\u011f\u011d\3\2\2\2\u011f\u0120\3\2"+
		"\2\2\u0120\u0122\3\2\2\2\u0121\u011f\3\2\2\2\u0122\u0123\7$\2\2\u0123"+
		"L\3\2\2\2\u0124\u0127\5S*\2\u0125\u0127\5W,\2\u0126\u0124\3\2\2\2\u0126"+
		"\u0125\3\2\2\2\u0127N\3\2\2\2\u0128\u012e\5S*\2\u0129\u012e\5W,\2\u012a"+
		"\u012e\5U+\2\u012b\u012e\5[.\2\u012c\u012e\5Y-\2\u012d\u0128\3\2\2\2\u012d"+
		"\u0129\3\2\2\2\u012d\u012a\3\2\2\2\u012d\u012b\3\2\2\2\u012d\u012c\3\2"+
		"\2\2\u012eP\3\2\2\2\u012f\u0133\5S*\2\u0130\u0133\5W,\2\u0131\u0133\5"+
		"U+\2\u0132\u012f\3\2\2\2\u0132\u0130\3\2\2\2\u0132\u0131\3\2\2\2\u0133"+
		"R\3\2\2\2\u0134\u0135\t\7\2\2\u0135T\3\2\2\2\u0136\u0137\t\4\2\2\u0137"+
		"V\3\2\2\2\u0138\u0139\t\b\2\2\u0139X\3\2\2\2\u013a\u013b\7/\2\2\u013b"+
		"Z\3\2\2\2\u013c\u013d\7$\2\2\u013d\\\3\2\2\2\16\2`gm\u00fd\u0110\u0114"+
		"\u0119\u011f\u0126\u012d\u0132\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}