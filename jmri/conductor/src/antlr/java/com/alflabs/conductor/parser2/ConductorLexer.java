// Generated from Conductor.g4 by ANTLR 4.5.3
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
		WS=1, EOL=2, SB_COMMENT=3, KW_ARROW=4, KW_EQUAL=5, KW_AND=6, KW_NOT=7, 
		KW_PLUS=8, KW_SEMI=9, KW_VAR=10, KW_THROTTLE=11, KW_SENSOR=12, KW_TURNOUT=13, 
		KW_TIMER=14, KW_FORWARD=15, KW_REVERSE=16, KW_NORMAL=17, KW_SOUND=18, 
		KW_LIGHT=19, KW_HORN=20, KW_STOP=21, KW_STOPPED=22, KW_START=23, KW_END=24, 
		KW_RESET=25, KW_TIMERS=26, KW_FN=27, ID=28, NUM=29;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_EQUAL", "KW_AND", "KW_NOT", 
		"KW_PLUS", "KW_SEMI", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", 
		"KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", 
		"KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_END", "KW_RESET", 
		"KW_TIMERS", "KW_FN", "KW_F0", "KW_F10", "KW_F20", "ID", "NUM", "IdCharStart", 
		"IdCharFull", "IdCharLast", "IdUnreserved", "IdNum", "IdLetter", "IdDash"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'->'", "'='", "'&'", "'!'", "'+'", "';'", "'var'", 
		"'throttle'", "'sensor'", "'turnout'", "'timer'", "'forward'", "'reverse'", 
		"'normal'", "'sound'", "'light'", "'horn'", "'stop'", "'stopped'", "'start'", 
		"'end'", "'reset'", "'timers'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_EQUAL", "KW_AND", "KW_NOT", 
		"KW_PLUS", "KW_SEMI", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", 
		"KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", 
		"KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_END", "KW_RESET", 
		"KW_TIMERS", "KW_FN", "ID", "NUM"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\37\u0117\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\3\2\6\2S\n\2\r\2"+
		"\16\2T\3\2\3\2\3\3\6\3Z\n\3\r\3\16\3[\3\4\3\4\7\4`\n\4\f\4\16\4c\13\4"+
		"\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3"+
		"\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3"+
		"\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3"+
		"\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3"+
		"\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\5\34\u00e3\n\34\3\35\3\35"+
		"\3\35\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3 \3 \7 \u00f4"+
		"\n \f \16 \u00f7\13 \3 \5 \u00fa\n \3!\6!\u00fd\n!\r!\16!\u00fe\3\"\3"+
		"\"\5\"\u0103\n\"\3#\3#\3#\3#\5#\u0109\n#\3$\3$\3$\5$\u010e\n$\3%\3%\3"+
		"&\3&\3\'\3\'\3(\3(\2\2)\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f"+
		"\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63"+
		"\33\65\34\67\359\2;\2=\2?\36A\37C\2E\2G\2I\2K\2M\2O\2\3\2\b\5\2\13\13"+
		"\16\16\"\"\4\2\f\f\17\17\3\2\62;\3\2\62:\f\2$$&\'),..\60\61<<AA]b}}\177"+
		"\u0080\4\2C\\c|\u011a\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2"+
		"\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2"+
		"\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3"+
		"\2\2\2\2?\3\2\2\2\2A\3\2\2\2\3R\3\2\2\2\5Y\3\2\2\2\7]\3\2\2\2\td\3\2\2"+
		"\2\13g\3\2\2\2\ri\3\2\2\2\17k\3\2\2\2\21m\3\2\2\2\23o\3\2\2\2\25q\3\2"+
		"\2\2\27u\3\2\2\2\31~\3\2\2\2\33\u0085\3\2\2\2\35\u008d\3\2\2\2\37\u0093"+
		"\3\2\2\2!\u009b\3\2\2\2#\u00a3\3\2\2\2%\u00aa\3\2\2\2\'\u00b0\3\2\2\2"+
		")\u00b6\3\2\2\2+\u00bb\3\2\2\2-\u00c0\3\2\2\2/\u00c8\3\2\2\2\61\u00ce"+
		"\3\2\2\2\63\u00d2\3\2\2\2\65\u00d8\3\2\2\2\67\u00e2\3\2\2\29\u00e4\3\2"+
		"\2\2;\u00e7\3\2\2\2=\u00ec\3\2\2\2?\u00f1\3\2\2\2A\u00fc\3\2\2\2C\u0102"+
		"\3\2\2\2E\u0108\3\2\2\2G\u010d\3\2\2\2I\u010f\3\2\2\2K\u0111\3\2\2\2M"+
		"\u0113\3\2\2\2O\u0115\3\2\2\2QS\t\2\2\2RQ\3\2\2\2ST\3\2\2\2TR\3\2\2\2"+
		"TU\3\2\2\2UV\3\2\2\2VW\b\2\2\2W\4\3\2\2\2XZ\t\3\2\2YX\3\2\2\2Z[\3\2\2"+
		"\2[Y\3\2\2\2[\\\3\2\2\2\\\6\3\2\2\2]a\7%\2\2^`\n\3\2\2_^\3\2\2\2`c\3\2"+
		"\2\2a_\3\2\2\2ab\3\2\2\2b\b\3\2\2\2ca\3\2\2\2de\7/\2\2ef\7@\2\2f\n\3\2"+
		"\2\2gh\7?\2\2h\f\3\2\2\2ij\7(\2\2j\16\3\2\2\2kl\7#\2\2l\20\3\2\2\2mn\7"+
		"-\2\2n\22\3\2\2\2op\7=\2\2p\24\3\2\2\2qr\7x\2\2rs\7c\2\2st\7t\2\2t\26"+
		"\3\2\2\2uv\7v\2\2vw\7j\2\2wx\7t\2\2xy\7q\2\2yz\7v\2\2z{\7v\2\2{|\7n\2"+
		"\2|}\7g\2\2}\30\3\2\2\2~\177\7u\2\2\177\u0080\7g\2\2\u0080\u0081\7p\2"+
		"\2\u0081\u0082\7u\2\2\u0082\u0083\7q\2\2\u0083\u0084\7t\2\2\u0084\32\3"+
		"\2\2\2\u0085\u0086\7v\2\2\u0086\u0087\7w\2\2\u0087\u0088\7t\2\2\u0088"+
		"\u0089\7p\2\2\u0089\u008a\7q\2\2\u008a\u008b\7w\2\2\u008b\u008c\7v\2\2"+
		"\u008c\34\3\2\2\2\u008d\u008e\7v\2\2\u008e\u008f\7k\2\2\u008f\u0090\7"+
		"o\2\2\u0090\u0091\7g\2\2\u0091\u0092\7t\2\2\u0092\36\3\2\2\2\u0093\u0094"+
		"\7h\2\2\u0094\u0095\7q\2\2\u0095\u0096\7t\2\2\u0096\u0097\7y\2\2\u0097"+
		"\u0098\7c\2\2\u0098\u0099\7t\2\2\u0099\u009a\7f\2\2\u009a \3\2\2\2\u009b"+
		"\u009c\7t\2\2\u009c\u009d\7g\2\2\u009d\u009e\7x\2\2\u009e\u009f\7g\2\2"+
		"\u009f\u00a0\7t\2\2\u00a0\u00a1\7u\2\2\u00a1\u00a2\7g\2\2\u00a2\"\3\2"+
		"\2\2\u00a3\u00a4\7p\2\2\u00a4\u00a5\7q\2\2\u00a5\u00a6\7t\2\2\u00a6\u00a7"+
		"\7o\2\2\u00a7\u00a8\7c\2\2\u00a8\u00a9\7n\2\2\u00a9$\3\2\2\2\u00aa\u00ab"+
		"\7u\2\2\u00ab\u00ac\7q\2\2\u00ac\u00ad\7w\2\2\u00ad\u00ae\7p\2\2\u00ae"+
		"\u00af\7f\2\2\u00af&\3\2\2\2\u00b0\u00b1\7n\2\2\u00b1\u00b2\7k\2\2\u00b2"+
		"\u00b3\7i\2\2\u00b3\u00b4\7j\2\2\u00b4\u00b5\7v\2\2\u00b5(\3\2\2\2\u00b6"+
		"\u00b7\7j\2\2\u00b7\u00b8\7q\2\2\u00b8\u00b9\7t\2\2\u00b9\u00ba\7p\2\2"+
		"\u00ba*\3\2\2\2\u00bb\u00bc\7u\2\2\u00bc\u00bd\7v\2\2\u00bd\u00be\7q\2"+
		"\2\u00be\u00bf\7r\2\2\u00bf,\3\2\2\2\u00c0\u00c1\7u\2\2\u00c1\u00c2\7"+
		"v\2\2\u00c2\u00c3\7q\2\2\u00c3\u00c4\7r\2\2\u00c4\u00c5\7r\2\2\u00c5\u00c6"+
		"\7g\2\2\u00c6\u00c7\7f\2\2\u00c7.\3\2\2\2\u00c8\u00c9\7u\2\2\u00c9\u00ca"+
		"\7v\2\2\u00ca\u00cb\7c\2\2\u00cb\u00cc\7t\2\2\u00cc\u00cd\7v\2\2\u00cd"+
		"\60\3\2\2\2\u00ce\u00cf\7g\2\2\u00cf\u00d0\7p\2\2\u00d0\u00d1\7f\2\2\u00d1"+
		"\62\3\2\2\2\u00d2\u00d3\7t\2\2\u00d3\u00d4\7g\2\2\u00d4\u00d5\7u\2\2\u00d5"+
		"\u00d6\7g\2\2\u00d6\u00d7\7v\2\2\u00d7\64\3\2\2\2\u00d8\u00d9\7v\2\2\u00d9"+
		"\u00da\7k\2\2\u00da\u00db\7o\2\2\u00db\u00dc\7g\2\2\u00dc\u00dd\7t\2\2"+
		"\u00dd\u00de\7u\2\2\u00de\66\3\2\2\2\u00df\u00e3\59\35\2\u00e0\u00e3\5"+
		";\36\2\u00e1\u00e3\5=\37\2\u00e2\u00df\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e2"+
		"\u00e1\3\2\2\2\u00e38\3\2\2\2\u00e4\u00e5\7h\2\2\u00e5\u00e6\t\4\2\2\u00e6"+
		":\3\2\2\2\u00e7\u00e8\7h\2\2\u00e8\u00e9\7\63\2\2\u00e9\u00ea\3\2\2\2"+
		"\u00ea\u00eb\t\4\2\2\u00eb<\3\2\2\2\u00ec\u00ed\7h\2\2\u00ed\u00ee\7\64"+
		"\2\2\u00ee\u00ef\3\2\2\2\u00ef\u00f0\t\5\2\2\u00f0>\3\2\2\2\u00f1\u00f9"+
		"\5C\"\2\u00f2\u00f4\5E#\2\u00f3\u00f2\3\2\2\2\u00f4\u00f7\3\2\2\2\u00f5"+
		"\u00f3\3\2\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f8\3\2\2\2\u00f7\u00f5\3\2"+
		"\2\2\u00f8\u00fa\5G$\2\u00f9\u00f5\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa@"+
		"\3\2\2\2\u00fb\u00fd\5K&\2\u00fc\u00fb\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe"+
		"\u00fc\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ffB\3\2\2\2\u0100\u0103\5I%\2\u0101"+
		"\u0103\5M\'\2\u0102\u0100\3\2\2\2\u0102\u0101\3\2\2\2\u0103D\3\2\2\2\u0104"+
		"\u0109\5I%\2\u0105\u0109\5M\'\2\u0106\u0109\5K&\2\u0107\u0109\5O(\2\u0108"+
		"\u0104\3\2\2\2\u0108\u0105\3\2\2\2\u0108\u0106\3\2\2\2\u0108\u0107\3\2"+
		"\2\2\u0109F\3\2\2\2\u010a\u010e\5I%\2\u010b\u010e\5M\'\2\u010c\u010e\5"+
		"K&\2\u010d\u010a\3\2\2\2\u010d\u010b\3\2\2\2\u010d\u010c\3\2\2\2\u010e"+
		"H\3\2\2\2\u010f\u0110\t\6\2\2\u0110J\3\2\2\2\u0111\u0112\t\4\2\2\u0112"+
		"L\3\2\2\2\u0113\u0114\t\7\2\2\u0114N\3\2\2\2\u0115\u0116\7/\2\2\u0116"+
		"P\3\2\2\2\r\2T[a\u00e2\u00f5\u00f9\u00fe\u0102\u0108\u010d\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
