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
		KW_FN=25, ID=26, NUM=27;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_EQUAL", "KW_AND", "KW_NOT", 
		"KW_PLUS", "KW_SEMI", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", 
		"KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", 
		"KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_END", "KW_FN", "ID", 
		"NUM", "IdCharStart", "IdCharFull", "IdUnreserved", "IdNum", "IdLetter", 
		"IdDash"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'->'", "'='", "'&'", "'!'", "'+'", "';'", "'var'", 
		"'throttle'", "'sensor'", "'turnout'", "'timer'", "'forward'", "'reverse'", 
		"'normal'", "'sound'", "'light'", "'horn'", "'stop'", "'stopped'", "'start'", 
		"'end'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "WS", "EOL", "SB_COMMENT", "KW_ARROW", "KW_EQUAL", "KW_AND", "KW_NOT", 
		"KW_PLUS", "KW_SEMI", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", 
		"KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", 
		"KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_END", "KW_FN", "ID", 
		"NUM"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\35\u00e9\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\3\2\6\2G\n\2\r\2\16\2H\3\2\3\2\3\3\6\3N\n\3\r\3\16\3O"+
		"\3\4\3\4\7\4T\n\4\f\4\16\4W\13\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3"+
		"\t\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3"+
		"\22\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3"+
		"\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3"+
		"\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3"+
		"\32\3\32\5\32\u00ca\n\32\3\33\3\33\7\33\u00ce\n\33\f\33\16\33\u00d1\13"+
		"\33\3\34\6\34\u00d4\n\34\r\34\16\34\u00d5\3\35\3\35\5\35\u00da\n\35\3"+
		"\36\3\36\3\36\3\36\5\36\u00e0\n\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\2\2#"+
		"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20"+
		"\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\2;\2"+
		"=\2?\2A\2C\2\3\2\7\5\2\13\13\16\16\"\"\4\2\f\f\17\17\3\2\62;\f\2$$&\'"+
		"),..\60\61<<AA]b}}\177\u0080\4\2C\\c|\u00ec\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2"+
		"\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2"+
		"\2\2\65\3\2\2\2\2\67\3\2\2\2\3F\3\2\2\2\5M\3\2\2\2\7Q\3\2\2\2\tX\3\2\2"+
		"\2\13[\3\2\2\2\r]\3\2\2\2\17_\3\2\2\2\21a\3\2\2\2\23c\3\2\2\2\25e\3\2"+
		"\2\2\27i\3\2\2\2\31r\3\2\2\2\33y\3\2\2\2\35\u0081\3\2\2\2\37\u0087\3\2"+
		"\2\2!\u008f\3\2\2\2#\u0097\3\2\2\2%\u009e\3\2\2\2\'\u00a4\3\2\2\2)\u00aa"+
		"\3\2\2\2+\u00af\3\2\2\2-\u00b4\3\2\2\2/\u00bc\3\2\2\2\61\u00c2\3\2\2\2"+
		"\63\u00c6\3\2\2\2\65\u00cb\3\2\2\2\67\u00d3\3\2\2\29\u00d9\3\2\2\2;\u00df"+
		"\3\2\2\2=\u00e1\3\2\2\2?\u00e3\3\2\2\2A\u00e5\3\2\2\2C\u00e7\3\2\2\2E"+
		"G\t\2\2\2FE\3\2\2\2GH\3\2\2\2HF\3\2\2\2HI\3\2\2\2IJ\3\2\2\2JK\b\2\2\2"+
		"K\4\3\2\2\2LN\t\3\2\2ML\3\2\2\2NO\3\2\2\2OM\3\2\2\2OP\3\2\2\2P\6\3\2\2"+
		"\2QU\7%\2\2RT\n\3\2\2SR\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2V\b\3\2\2"+
		"\2WU\3\2\2\2XY\7/\2\2YZ\7@\2\2Z\n\3\2\2\2[\\\7?\2\2\\\f\3\2\2\2]^\7(\2"+
		"\2^\16\3\2\2\2_`\7#\2\2`\20\3\2\2\2ab\7-\2\2b\22\3\2\2\2cd\7=\2\2d\24"+
		"\3\2\2\2ef\7x\2\2fg\7c\2\2gh\7t\2\2h\26\3\2\2\2ij\7v\2\2jk\7j\2\2kl\7"+
		"t\2\2lm\7q\2\2mn\7v\2\2no\7v\2\2op\7n\2\2pq\7g\2\2q\30\3\2\2\2rs\7u\2"+
		"\2st\7g\2\2tu\7p\2\2uv\7u\2\2vw\7q\2\2wx\7t\2\2x\32\3\2\2\2yz\7v\2\2z"+
		"{\7w\2\2{|\7t\2\2|}\7p\2\2}~\7q\2\2~\177\7w\2\2\177\u0080\7v\2\2\u0080"+
		"\34\3\2\2\2\u0081\u0082\7v\2\2\u0082\u0083\7k\2\2\u0083\u0084\7o\2\2\u0084"+
		"\u0085\7g\2\2\u0085\u0086\7t\2\2\u0086\36\3\2\2\2\u0087\u0088\7h\2\2\u0088"+
		"\u0089\7q\2\2\u0089\u008a\7t\2\2\u008a\u008b\7y\2\2\u008b\u008c\7c\2\2"+
		"\u008c\u008d\7t\2\2\u008d\u008e\7f\2\2\u008e \3\2\2\2\u008f\u0090\7t\2"+
		"\2\u0090\u0091\7g\2\2\u0091\u0092\7x\2\2\u0092\u0093\7g\2\2\u0093\u0094"+
		"\7t\2\2\u0094\u0095\7u\2\2\u0095\u0096\7g\2\2\u0096\"\3\2\2\2\u0097\u0098"+
		"\7p\2\2\u0098\u0099\7q\2\2\u0099\u009a\7t\2\2\u009a\u009b\7o\2\2\u009b"+
		"\u009c\7c\2\2\u009c\u009d\7n\2\2\u009d$\3\2\2\2\u009e\u009f\7u\2\2\u009f"+
		"\u00a0\7q\2\2\u00a0\u00a1\7w\2\2\u00a1\u00a2\7p\2\2\u00a2\u00a3\7f\2\2"+
		"\u00a3&\3\2\2\2\u00a4\u00a5\7n\2\2\u00a5\u00a6\7k\2\2\u00a6\u00a7\7i\2"+
		"\2\u00a7\u00a8\7j\2\2\u00a8\u00a9\7v\2\2\u00a9(\3\2\2\2\u00aa\u00ab\7"+
		"j\2\2\u00ab\u00ac\7q\2\2\u00ac\u00ad\7t\2\2\u00ad\u00ae\7p\2\2\u00ae*"+
		"\3\2\2\2\u00af\u00b0\7u\2\2\u00b0\u00b1\7v\2\2\u00b1\u00b2\7q\2\2\u00b2"+
		"\u00b3\7r\2\2\u00b3,\3\2\2\2\u00b4\u00b5\7u\2\2\u00b5\u00b6\7v\2\2\u00b6"+
		"\u00b7\7q\2\2\u00b7\u00b8\7r\2\2\u00b8\u00b9\7r\2\2\u00b9\u00ba\7g\2\2"+
		"\u00ba\u00bb\7f\2\2\u00bb.\3\2\2\2\u00bc\u00bd\7u\2\2\u00bd\u00be\7v\2"+
		"\2\u00be\u00bf\7c\2\2\u00bf\u00c0\7t\2\2\u00c0\u00c1\7v\2\2\u00c1\60\3"+
		"\2\2\2\u00c2\u00c3\7g\2\2\u00c3\u00c4\7p\2\2\u00c4\u00c5\7f\2\2\u00c5"+
		"\62\3\2\2\2\u00c6\u00c7\7h\2\2\u00c7\u00c9\t\4\2\2\u00c8\u00ca\t\4\2\2"+
		"\u00c9\u00c8\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\64\3\2\2\2\u00cb\u00cf"+
		"\59\35\2\u00cc\u00ce\5;\36\2\u00cd\u00cc\3\2\2\2\u00ce\u00d1\3\2\2\2\u00cf"+
		"\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\66\3\2\2\2\u00d1\u00cf\3\2\2"+
		"\2\u00d2\u00d4\5? \2\u00d3\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d3"+
		"\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d68\3\2\2\2\u00d7\u00da\5=\37\2\u00d8"+
		"\u00da\5A!\2\u00d9\u00d7\3\2\2\2\u00d9\u00d8\3\2\2\2\u00da:\3\2\2\2\u00db"+
		"\u00e0\5=\37\2\u00dc\u00e0\5A!\2\u00dd\u00e0\5? \2\u00de\u00e0\5C\"\2"+
		"\u00df\u00db\3\2\2\2\u00df\u00dc\3\2\2\2\u00df\u00dd\3\2\2\2\u00df\u00de"+
		"\3\2\2\2\u00e0<\3\2\2\2\u00e1\u00e2\t\5\2\2\u00e2>\3\2\2\2\u00e3\u00e4"+
		"\t\4\2\2\u00e4@\3\2\2\2\u00e5\u00e6\t\6\2\2\u00e6B\3\2\2\2\u00e7\u00e8"+
		"\7/\2\2\u00e8D\3\2\2\2\13\2HOU\u00c9\u00cf\u00d5\u00d9\u00df\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
