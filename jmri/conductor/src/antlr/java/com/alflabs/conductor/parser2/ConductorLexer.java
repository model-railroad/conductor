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
		KW_AND=8, KW_NOT=9, KW_PLUS=10, KW_SEMI=11, KW_VAR=12, KW_ENUM=13, KW_THROTTLE=14, 
		KW_SENSOR=15, KW_TURNOUT=16, KW_TIMER=17, KW_FORWARD=18, KW_REVERSE=19, 
		KW_NORMAL=20, KW_SOUND=21, KW_LIGHT=22, KW_HORN=23, KW_STOP=24, KW_STOPPED=25, 
		KW_START=26, KW_END=27, KW_RESET=28, KW_TIMERS=29, KW_FN=30, ID=31, NUM=32;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_IS_EQ", "KW_IS_NEQ", "KW_EQUAL", 
		"KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "KW_VAR", "KW_ENUM", "KW_THROTTLE", 
		"KW_SENSOR", "KW_TURNOUT", "KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", 
		"KW_SOUND", "KW_LIGHT", "KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", 
		"KW_END", "KW_RESET", "KW_TIMERS", "KW_FN", "KW_F0", "KW_F10", "KW_F20", 
		"ID", "NUM", "IdCharStart", "IdCharFull", "IdCharLast", "IdUnreserved", 
		"IdNum", "IdLetter", "IdDash"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'->'", "'=='", "'!='", "'='", "'&'", "'!'", "'+'", 
		"';'", "'var'", "'enum'", "'throttle'", "'sensor'", "'turnout'", "'timer'", 
		"'forward'", "'reverse'", "'normal'", "'sound'", "'light'", "'horn'", 
		"'stop'", "'stopped'", "'start'", "'end'", "'reset'", "'timers'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_IS_EQ", "KW_IS_NEQ", 
		"KW_EQUAL", "KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "KW_VAR", "KW_ENUM", 
		"KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", "KW_TIMER", "KW_FORWARD", "KW_REVERSE", 
		"KW_NORMAL", "KW_SOUND", "KW_LIGHT", "KW_HORN", "KW_STOP", "KW_STOPPED", 
		"KW_START", "KW_END", "KW_RESET", "KW_TIMERS", "KW_FN", "ID", "NUM"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\"\u0128\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\3"+
		"\2\6\2Y\n\2\r\2\16\2Z\3\2\3\2\3\3\6\3`\n\3\r\3\16\3a\3\4\3\4\7\4f\n\4"+
		"\f\4\16\4i\13\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3"+
		"\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31"+
		"\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\35"+
		"\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\5\37\u00f4\n\37\3 "+
		"\3 \3 \3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3#\3#\7#\u0105\n#\f#\16#\u0108"+
		"\13#\3#\5#\u010b\n#\3$\6$\u010e\n$\r$\16$\u010f\3%\3%\5%\u0114\n%\3&\3"+
		"&\3&\3&\5&\u011a\n&\3\'\3\'\3\'\5\'\u011f\n\'\3(\3(\3)\3)\3*\3*\3+\3+"+
		"\2\2,\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?\2A\2C\2E!G\"I\2K\2M\2O\2Q\2S\2U\2\3\2\b\5\2\13\13\16\16\"\"\4"+
		"\2\f\f\17\17\3\2\62;\3\2\62:\f\2$$&\'),..\60\61<<AA]b}}\177\u0080\4\2"+
		"C\\c|\u012b\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\3X\3\2\2\2\5_\3\2\2\2"+
		"\7c\3\2\2\2\tj\3\2\2\2\13m\3\2\2\2\rp\3\2\2\2\17s\3\2\2\2\21u\3\2\2\2"+
		"\23w\3\2\2\2\25y\3\2\2\2\27{\3\2\2\2\31}\3\2\2\2\33\u0081\3\2\2\2\35\u0086"+
		"\3\2\2\2\37\u008f\3\2\2\2!\u0096\3\2\2\2#\u009e\3\2\2\2%\u00a4\3\2\2\2"+
		"\'\u00ac\3\2\2\2)\u00b4\3\2\2\2+\u00bb\3\2\2\2-\u00c1\3\2\2\2/\u00c7\3"+
		"\2\2\2\61\u00cc\3\2\2\2\63\u00d1\3\2\2\2\65\u00d9\3\2\2\2\67\u00df\3\2"+
		"\2\29\u00e3\3\2\2\2;\u00e9\3\2\2\2=\u00f3\3\2\2\2?\u00f5\3\2\2\2A\u00f8"+
		"\3\2\2\2C\u00fd\3\2\2\2E\u0102\3\2\2\2G\u010d\3\2\2\2I\u0113\3\2\2\2K"+
		"\u0119\3\2\2\2M\u011e\3\2\2\2O\u0120\3\2\2\2Q\u0122\3\2\2\2S\u0124\3\2"+
		"\2\2U\u0126\3\2\2\2WY\t\2\2\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2"+
		"[\\\3\2\2\2\\]\b\2\2\2]\4\3\2\2\2^`\t\3\2\2_^\3\2\2\2`a\3\2\2\2a_\3\2"+
		"\2\2ab\3\2\2\2b\6\3\2\2\2cg\7%\2\2df\n\3\2\2ed\3\2\2\2fi\3\2\2\2ge\3\2"+
		"\2\2gh\3\2\2\2h\b\3\2\2\2ig\3\2\2\2jk\7/\2\2kl\7@\2\2l\n\3\2\2\2mn\7?"+
		"\2\2no\7?\2\2o\f\3\2\2\2pq\7#\2\2qr\7?\2\2r\16\3\2\2\2st\7?\2\2t\20\3"+
		"\2\2\2uv\7(\2\2v\22\3\2\2\2wx\7#\2\2x\24\3\2\2\2yz\7-\2\2z\26\3\2\2\2"+
		"{|\7=\2\2|\30\3\2\2\2}~\7x\2\2~\177\7c\2\2\177\u0080\7t\2\2\u0080\32\3"+
		"\2\2\2\u0081\u0082\7g\2\2\u0082\u0083\7p\2\2\u0083\u0084\7w\2\2\u0084"+
		"\u0085\7o\2\2\u0085\34\3\2\2\2\u0086\u0087\7v\2\2\u0087\u0088\7j\2\2\u0088"+
		"\u0089\7t\2\2\u0089\u008a\7q\2\2\u008a\u008b\7v\2\2\u008b\u008c\7v\2\2"+
		"\u008c\u008d\7n\2\2\u008d\u008e\7g\2\2\u008e\36\3\2\2\2\u008f\u0090\7"+
		"u\2\2\u0090\u0091\7g\2\2\u0091\u0092\7p\2\2\u0092\u0093\7u\2\2\u0093\u0094"+
		"\7q\2\2\u0094\u0095\7t\2\2\u0095 \3\2\2\2\u0096\u0097\7v\2\2\u0097\u0098"+
		"\7w\2\2\u0098\u0099\7t\2\2\u0099\u009a\7p\2\2\u009a\u009b\7q\2\2\u009b"+
		"\u009c\7w\2\2\u009c\u009d\7v\2\2\u009d\"\3\2\2\2\u009e\u009f\7v\2\2\u009f"+
		"\u00a0\7k\2\2\u00a0\u00a1\7o\2\2\u00a1\u00a2\7g\2\2\u00a2\u00a3\7t\2\2"+
		"\u00a3$\3\2\2\2\u00a4\u00a5\7h\2\2\u00a5\u00a6\7q\2\2\u00a6\u00a7\7t\2"+
		"\2\u00a7\u00a8\7y\2\2\u00a8\u00a9\7c\2\2\u00a9\u00aa\7t\2\2\u00aa\u00ab"+
		"\7f\2\2\u00ab&\3\2\2\2\u00ac\u00ad\7t\2\2\u00ad\u00ae\7g\2\2\u00ae\u00af"+
		"\7x\2\2\u00af\u00b0\7g\2\2\u00b0\u00b1\7t\2\2\u00b1\u00b2\7u\2\2\u00b2"+
		"\u00b3\7g\2\2\u00b3(\3\2\2\2\u00b4\u00b5\7p\2\2\u00b5\u00b6\7q\2\2\u00b6"+
		"\u00b7\7t\2\2\u00b7\u00b8\7o\2\2\u00b8\u00b9\7c\2\2\u00b9\u00ba\7n\2\2"+
		"\u00ba*\3\2\2\2\u00bb\u00bc\7u\2\2\u00bc\u00bd\7q\2\2\u00bd\u00be\7w\2"+
		"\2\u00be\u00bf\7p\2\2\u00bf\u00c0\7f\2\2\u00c0,\3\2\2\2\u00c1\u00c2\7"+
		"n\2\2\u00c2\u00c3\7k\2\2\u00c3\u00c4\7i\2\2\u00c4\u00c5\7j\2\2\u00c5\u00c6"+
		"\7v\2\2\u00c6.\3\2\2\2\u00c7\u00c8\7j\2\2\u00c8\u00c9\7q\2\2\u00c9\u00ca"+
		"\7t\2\2\u00ca\u00cb\7p\2\2\u00cb\60\3\2\2\2\u00cc\u00cd\7u\2\2\u00cd\u00ce"+
		"\7v\2\2\u00ce\u00cf\7q\2\2\u00cf\u00d0\7r\2\2\u00d0\62\3\2\2\2\u00d1\u00d2"+
		"\7u\2\2\u00d2\u00d3\7v\2\2\u00d3\u00d4\7q\2\2\u00d4\u00d5\7r\2\2\u00d5"+
		"\u00d6\7r\2\2\u00d6\u00d7\7g\2\2\u00d7\u00d8\7f\2\2\u00d8\64\3\2\2\2\u00d9"+
		"\u00da\7u\2\2\u00da\u00db\7v\2\2\u00db\u00dc\7c\2\2\u00dc\u00dd\7t\2\2"+
		"\u00dd\u00de\7v\2\2\u00de\66\3\2\2\2\u00df\u00e0\7g\2\2\u00e0\u00e1\7"+
		"p\2\2\u00e1\u00e2\7f\2\2\u00e28\3\2\2\2\u00e3\u00e4\7t\2\2\u00e4\u00e5"+
		"\7g\2\2\u00e5\u00e6\7u\2\2\u00e6\u00e7\7g\2\2\u00e7\u00e8\7v\2\2\u00e8"+
		":\3\2\2\2\u00e9\u00ea\7v\2\2\u00ea\u00eb\7k\2\2\u00eb\u00ec\7o\2\2\u00ec"+
		"\u00ed\7g\2\2\u00ed\u00ee\7t\2\2\u00ee\u00ef\7u\2\2\u00ef<\3\2\2\2\u00f0"+
		"\u00f4\5? \2\u00f1\u00f4\5A!\2\u00f2\u00f4\5C\"\2\u00f3\u00f0\3\2\2\2"+
		"\u00f3\u00f1\3\2\2\2\u00f3\u00f2\3\2\2\2\u00f4>\3\2\2\2\u00f5\u00f6\7"+
		"h\2\2\u00f6\u00f7\t\4\2\2\u00f7@\3\2\2\2\u00f8\u00f9\7h\2\2\u00f9\u00fa"+
		"\7\63\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fc\t\4\2\2\u00fcB\3\2\2\2\u00fd"+
		"\u00fe\7h\2\2\u00fe\u00ff\7\64\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\t\5"+
		"\2\2\u0101D\3\2\2\2\u0102\u010a\5I%\2\u0103\u0105\5K&\2\u0104\u0103\3"+
		"\2\2\2\u0105\u0108\3\2\2\2\u0106\u0104\3\2\2\2\u0106\u0107\3\2\2\2\u0107"+
		"\u0109\3\2\2\2\u0108\u0106\3\2\2\2\u0109\u010b\5M\'\2\u010a\u0106\3\2"+
		"\2\2\u010a\u010b\3\2\2\2\u010bF\3\2\2\2\u010c\u010e\5Q)\2\u010d\u010c"+
		"\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u010d\3\2\2\2\u010f\u0110\3\2\2\2\u0110"+
		"H\3\2\2\2\u0111\u0114\5O(\2\u0112\u0114\5S*\2\u0113\u0111\3\2\2\2\u0113"+
		"\u0112\3\2\2\2\u0114J\3\2\2\2\u0115\u011a\5O(\2\u0116\u011a\5S*\2\u0117"+
		"\u011a\5Q)\2\u0118\u011a\5U+\2\u0119\u0115\3\2\2\2\u0119\u0116\3\2\2\2"+
		"\u0119\u0117\3\2\2\2\u0119\u0118\3\2\2\2\u011aL\3\2\2\2\u011b\u011f\5"+
		"O(\2\u011c\u011f\5S*\2\u011d\u011f\5Q)\2\u011e\u011b\3\2\2\2\u011e\u011c"+
		"\3\2\2\2\u011e\u011d\3\2\2\2\u011fN\3\2\2\2\u0120\u0121\t\6\2\2\u0121"+
		"P\3\2\2\2\u0122\u0123\t\4\2\2\u0123R\3\2\2\2\u0124\u0125\t\7\2\2\u0125"+
		"T\3\2\2\2\u0126\u0127\7/\2\2\u0127V\3\2\2\2\r\2Zag\u00f3\u0106\u010a\u010f"+
		"\u0113\u0119\u011e\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}