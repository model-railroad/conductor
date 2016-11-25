// Generated from Conductor.g4 by ANTLR 4.5.3
package com.alflabs.conductor.parser2;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ConductorParser}.
 */
public interface ConductorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ConductorParser#script}.
	 * @param ctx the parse tree
	 */
	void enterScript(ConductorParser.ScriptContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#script}.
	 * @param ctx the parse tree
	 */
	void exitScript(ConductorParser.ScriptContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#scriptLine}.
	 * @param ctx the parse tree
	 */
	void enterScriptLine(ConductorParser.ScriptLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#scriptLine}.
	 * @param ctx the parse tree
	 */
	void exitScriptLine(ConductorParser.ScriptLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#eol}.
	 * @param ctx the parse tree
	 */
	void enterEol(ConductorParser.EolContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#eol}.
	 * @param ctx the parse tree
	 */
	void exitEol(ConductorParser.EolContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(ConductorParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(ConductorParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#defLine}.
	 * @param ctx the parse tree
	 */
	void enterDefLine(ConductorParser.DefLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defLine}.
	 * @param ctx the parse tree
	 */
	void exitDefLine(ConductorParser.DefLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#defType}.
	 * @param ctx the parse tree
	 */
	void enterDefType(ConductorParser.DefTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defType}.
	 * @param ctx the parse tree
	 */
	void exitDefType(ConductorParser.DefTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#eventLine}.
	 * @param ctx the parse tree
	 */
	void enterEventLine(ConductorParser.EventLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#eventLine}.
	 * @param ctx the parse tree
	 */
	void exitEventLine(ConductorParser.EventLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#condList}.
	 * @param ctx the parse tree
	 */
	void enterCondList(ConductorParser.CondListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#condList}.
	 * @param ctx the parse tree
	 */
	void exitCondList(ConductorParser.CondListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#cond}.
	 * @param ctx the parse tree
	 */
	void enterCond(ConductorParser.CondContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#cond}.
	 * @param ctx the parse tree
	 */
	void exitCond(ConductorParser.CondContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#cond_op}.
	 * @param ctx the parse tree
	 */
	void enterCond_op(ConductorParser.Cond_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#cond_op}.
	 * @param ctx the parse tree
	 */
	void exitCond_op(ConductorParser.Cond_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#instList}.
	 * @param ctx the parse tree
	 */
	void enterInstList(ConductorParser.InstListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#instList}.
	 * @param ctx the parse tree
	 */
	void exitInstList(ConductorParser.InstListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#inst}.
	 * @param ctx the parse tree
	 */
	void enterInst(ConductorParser.InstContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#inst}.
	 * @param ctx the parse tree
	 */
	void exitInst(ConductorParser.InstContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#op}.
	 * @param ctx the parse tree
	 */
	void enterOp(ConductorParser.OpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#op}.
	 * @param ctx the parse tree
	 */
	void exitOp(ConductorParser.OpContext ctx);
}
