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
		WS=1, EOL=2, SB_COMMENT=3, KW_VAR=4, KW_THROTTLE=5, KW_SENSOR=6, KW_TURNOUT=7, 
		KW_TIMER=8, KW_FORWARD=9, KW_REVERSE=10, KW_NORMAL=11, KW_SOUND=12, KW_LIGHT=13, 
		KW_HORN=14, KW_STOP=15, KW_STOPPED=16, KW_START=17, KW_FN=18, KW_ARROW=19, 
		KW_EQUAL=20, KW_AND=21, KW_NOT=22, KW_PLUS=23, KW_SEMI=24, ID=25, NUM=26, 
		RESERVED=27;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"WS", "EOL", "SB_COMMENT", "KW_VAR", "KW_THROTTLE", "KW_SENSOR", "KW_TURNOUT", 
		"KW_TIMER", "KW_FORWARD", "KW_REVERSE", "KW_NORMAL", "KW_SOUND", "KW_LIGHT", 
		"KW_HORN", "KW_STOP", "KW_STOPPED", "KW_START", "KW_FN", "KW_ARROW", "KW_EQUAL", 
		"KW_AND", "KW_NOT", "KW_PLUS", "KW_SEMI", "ID", "NUM", "RESERVED", "IdUnreserved", 
		"IdNum", "IdLetter", "IdCharStart", "IdCharFull"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\35\u00f4\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\3\2\6\2E\n\2\r\2\16\2F\3\2\3\2\3\3\6\3L\n\3\r\3\16\3M\3\4\3\4"+
		"\7\4R\n\4\f\4\16\4U\13\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\20\3\20"+
		"\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\23\3\23\3\23\5\23\u00b7\n\23\3\24\3\24\3\24\3\25\3\25"+
		"\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\7\32\u00c8\n\32\f\32"+
		"\16\32\u00cb\13\32\3\32\3\32\7\32\u00cf\n\32\f\32\16\32\u00d2\13\32\3"+
		"\32\3\32\7\32\u00d6\n\32\f\32\16\32\u00d9\13\32\5\32\u00db\n\32\3\33\6"+
		"\33\u00de\n\33\r\33\16\33\u00df\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37"+
		"\3\37\3 \3 \5 \u00ed\n \3!\3!\3!\3!\5!\u00f3\n!\2\2\"\3\3\5\4\7\5\t\6"+
		"\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24"+
		"\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\2;\2=\2?\2A\2\3\2\13\5"+
		"\2\13\13\16\16\"\"\4\2\f\f\17\17\3\2\62;\b\2\"#%%((--==??\f\2$$&\'),."+
		".\60\61<<AA]b}}\177\u0080\4\2C\\c|\4\2\2\u0101\ud802\udc01\5\2C\\aac|"+
		"\3\2//\u00fb\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2"+
		"\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2"+
		"\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\3D\3"+
		"\2\2\2\5K\3\2\2\2\7O\3\2\2\2\tV\3\2\2\2\13Z\3\2\2\2\rc\3\2\2\2\17j\3\2"+
		"\2\2\21r\3\2\2\2\23x\3\2\2\2\25\u0080\3\2\2\2\27\u0088\3\2\2\2\31\u008f"+
		"\3\2\2\2\33\u0095\3\2\2\2\35\u009b\3\2\2\2\37\u00a0\3\2\2\2!\u00a5\3\2"+
		"\2\2#\u00ad\3\2\2\2%\u00b3\3\2\2\2\'\u00b8\3\2\2\2)\u00bb\3\2\2\2+\u00bd"+
		"\3\2\2\2-\u00bf\3\2\2\2/\u00c1\3\2\2\2\61\u00c3\3\2\2\2\63\u00da\3\2\2"+
		"\2\65\u00dd\3\2\2\2\67\u00e1\3\2\2\29\u00e3\3\2\2\2;\u00e5\3\2\2\2=\u00e7"+
		"\3\2\2\2?\u00ec\3\2\2\2A\u00f2\3\2\2\2CE\t\2\2\2DC\3\2\2\2EF\3\2\2\2F"+
		"D\3\2\2\2FG\3\2\2\2GH\3\2\2\2HI\b\2\2\2I\4\3\2\2\2JL\t\3\2\2KJ\3\2\2\2"+
		"LM\3\2\2\2MK\3\2\2\2MN\3\2\2\2N\6\3\2\2\2OS\7%\2\2PR\n\3\2\2QP\3\2\2\2"+
		"RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2T\b\3\2\2\2US\3\2\2\2VW\7x\2\2WX\7c\2\2"+
		"XY\7t\2\2Y\n\3\2\2\2Z[\7v\2\2[\\\7j\2\2\\]\7t\2\2]^\7q\2\2^_\7v\2\2_`"+
		"\7v\2\2`a\7n\2\2ab\7g\2\2b\f\3\2\2\2cd\7u\2\2de\7g\2\2ef\7p\2\2fg\7u\2"+
		"\2gh\7q\2\2hi\7t\2\2i\16\3\2\2\2jk\7v\2\2kl\7w\2\2lm\7t\2\2mn\7p\2\2n"+
		"o\7q\2\2op\7w\2\2pq\7v\2\2q\20\3\2\2\2rs\7v\2\2st\7k\2\2tu\7o\2\2uv\7"+
		"g\2\2vw\7t\2\2w\22\3\2\2\2xy\7h\2\2yz\7q\2\2z{\7t\2\2{|\7y\2\2|}\7c\2"+
		"\2}~\7t\2\2~\177\7f\2\2\177\24\3\2\2\2\u0080\u0081\7t\2\2\u0081\u0082"+
		"\7g\2\2\u0082\u0083\7x\2\2\u0083\u0084\7g\2\2\u0084\u0085\7t\2\2\u0085"+
		"\u0086\7u\2\2\u0086\u0087\7g\2\2\u0087\26\3\2\2\2\u0088\u0089\7p\2\2\u0089"+
		"\u008a\7q\2\2\u008a\u008b\7t\2\2\u008b\u008c\7o\2\2\u008c\u008d\7c\2\2"+
		"\u008d\u008e\7n\2\2\u008e\30\3\2\2\2\u008f\u0090\7u\2\2\u0090\u0091\7"+
		"q\2\2\u0091\u0092\7w\2\2\u0092\u0093\7p\2\2\u0093\u0094\7f\2\2\u0094\32"+
		"\3\2\2\2\u0095\u0096\7n\2\2\u0096\u0097\7k\2\2\u0097\u0098\7i\2\2\u0098"+
		"\u0099\7j\2\2\u0099\u009a\7v\2\2\u009a\34\3\2\2\2\u009b\u009c\7j\2\2\u009c"+
		"\u009d\7q\2\2\u009d\u009e\7t\2\2\u009e\u009f\7p\2\2\u009f\36\3\2\2\2\u00a0"+
		"\u00a1\7u\2\2\u00a1\u00a2\7v\2\2\u00a2\u00a3\7q\2\2\u00a3\u00a4\7r\2\2"+
		"\u00a4 \3\2\2\2\u00a5\u00a6\7u\2\2\u00a6\u00a7\7v\2\2\u00a7\u00a8\7q\2"+
		"\2\u00a8\u00a9\7r\2\2\u00a9\u00aa\7r\2\2\u00aa\u00ab\7g\2\2\u00ab\u00ac"+
		"\7f\2\2\u00ac\"\3\2\2\2\u00ad\u00ae\7u\2\2\u00ae\u00af\7v\2\2\u00af\u00b0"+
		"\7c\2\2\u00b0\u00b1\7t\2\2\u00b1\u00b2\7v\2\2\u00b2$\3\2\2\2\u00b3\u00b4"+
		"\7h\2\2\u00b4\u00b6\t\4\2\2\u00b5\u00b7\t\4\2\2\u00b6\u00b5\3\2\2\2\u00b6"+
		"\u00b7\3\2\2\2\u00b7&\3\2\2\2\u00b8\u00b9\7/\2\2\u00b9\u00ba\7@\2\2\u00ba"+
		"(\3\2\2\2\u00bb\u00bc\7?\2\2\u00bc*\3\2\2\2\u00bd\u00be\7(\2\2\u00be,"+
		"\3\2\2\2\u00bf\u00c0\7#\2\2\u00c0.\3\2\2\2\u00c1\u00c2\7-\2\2\u00c2\60"+
		"\3\2\2\2\u00c3\u00c4\7=\2\2\u00c4\62\3\2\2\2\u00c5\u00c9\5? \2\u00c6\u00c8"+
		"\5A!\2\u00c7\u00c6\3\2\2\2\u00c8\u00cb\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9"+
		"\u00ca\3\2\2\2\u00ca\u00db\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cc\u00d0\5;"+
		"\36\2\u00cd\u00cf\5A!\2\u00ce\u00cd\3\2\2\2\u00cf\u00d2\3\2\2\2\u00d0"+
		"\u00ce\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d3\3\2\2\2\u00d2\u00d0\3\2"+
		"\2\2\u00d3\u00d7\5? \2\u00d4\u00d6\5A!\2\u00d5\u00d4\3\2\2\2\u00d6\u00d9"+
		"\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00db\3\2\2\2\u00d9"+
		"\u00d7\3\2\2\2\u00da\u00c5\3\2\2\2\u00da\u00cc\3\2\2\2\u00db\64\3\2\2"+
		"\2\u00dc\u00de\5;\36\2\u00dd\u00dc\3\2\2\2\u00de\u00df\3\2\2\2\u00df\u00dd"+
		"\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\66\3\2\2\2\u00e1\u00e2\t\5\2\2\u00e2"+
		"8\3\2\2\2\u00e3\u00e4\t\6\2\2\u00e4:\3\2\2\2\u00e5\u00e6\t\4\2\2\u00e6"+
		"<\3\2\2\2\u00e7\u00e8\t\7\2\2\u00e8\u00e9\n\b\2\2\u00e9>\3\2\2\2\u00ea"+
		"\u00ed\59\35\2\u00eb\u00ed\t\t\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00eb\3\2"+
		"\2\2\u00ed@\3\2\2\2\u00ee\u00f3\59\35\2\u00ef\u00f3\t\t\2\2\u00f0\u00f3"+
		"\5;\36\2\u00f1\u00f3\t\n\2\2\u00f2\u00ee\3\2\2\2\u00f2\u00ef\3\2\2\2\u00f2"+
		"\u00f0\3\2\2\2\u00f2\u00f1\3\2\2\2\u00f3B\3\2\2\2\16\2FMS\u00b6\u00c9"+
		"\u00d0\u00d7\u00da\u00df\u00ec\u00f2\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
