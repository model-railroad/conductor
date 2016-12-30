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
	 * Enter a parse tree produced by {@link ConductorParser#defStrLine}.
	 * @param ctx the parse tree
	 */
	void enterDefStrLine(ConductorParser.DefStrLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defStrLine}.
	 * @param ctx the parse tree
	 */
	void exitDefStrLine(ConductorParser.DefStrLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#defStrType}.
	 * @param ctx the parse tree
	 */
	void enterDefStrType(ConductorParser.DefStrTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defStrType}.
	 * @param ctx the parse tree
	 */
	void exitDefStrType(ConductorParser.DefStrTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#defIntLine}.
	 * @param ctx the parse tree
	 */
	void enterDefIntLine(ConductorParser.DefIntLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defIntLine}.
	 * @param ctx the parse tree
	 */
	void exitDefIntLine(ConductorParser.DefIntLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#defIntType}.
	 * @param ctx the parse tree
	 */
	void enterDefIntType(ConductorParser.DefIntTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defIntType}.
	 * @param ctx the parse tree
	 */
	void exitDefIntType(ConductorParser.DefIntTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#defThrottleLine}.
	 * @param ctx the parse tree
	 */
	void enterDefThrottleLine(ConductorParser.DefThrottleLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#defThrottleLine}.
	 * @param ctx the parse tree
	 */
	void exitDefThrottleLine(ConductorParser.DefThrottleLineContext ctx);
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
	 * Enter a parse tree produced by {@link ConductorParser#condNot}.
	 * @param ctx the parse tree
	 */
	void enterCondNot(ConductorParser.CondNotContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#condNot}.
	 * @param ctx the parse tree
	 */
	void exitCondNot(ConductorParser.CondNotContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#condTime}.
	 * @param ctx the parse tree
	 */
	void enterCondTime(ConductorParser.CondTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#condTime}.
	 * @param ctx the parse tree
	 */
	void exitCondTime(ConductorParser.CondTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#condThrottleOp}.
	 * @param ctx the parse tree
	 */
	void enterCondThrottleOp(ConductorParser.CondThrottleOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#condThrottleOp}.
	 * @param ctx the parse tree
	 */
	void exitCondThrottleOp(ConductorParser.CondThrottleOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#actionList}.
	 * @param ctx the parse tree
	 */
	void enterActionList(ConductorParser.ActionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#actionList}.
	 * @param ctx the parse tree
	 */
	void exitActionList(ConductorParser.ActionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(ConductorParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(ConductorParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#idAction}.
	 * @param ctx the parse tree
	 */
	void enterIdAction(ConductorParser.IdActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#idAction}.
	 * @param ctx the parse tree
	 */
	void exitIdAction(ConductorParser.IdActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#fnAction}.
	 * @param ctx the parse tree
	 */
	void enterFnAction(ConductorParser.FnActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#fnAction}.
	 * @param ctx the parse tree
	 */
	void exitFnAction(ConductorParser.FnActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#throttleOp}.
	 * @param ctx the parse tree
	 */
	void enterThrottleOp(ConductorParser.ThrottleOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#throttleOp}.
	 * @param ctx the parse tree
	 */
	void exitThrottleOp(ConductorParser.ThrottleOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#turnoutOp}.
	 * @param ctx the parse tree
	 */
	void enterTurnoutOp(ConductorParser.TurnoutOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#turnoutOp}.
	 * @param ctx the parse tree
	 */
	void exitTurnoutOp(ConductorParser.TurnoutOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#timerOp}.
	 * @param ctx the parse tree
	 */
	void enterTimerOp(ConductorParser.TimerOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#timerOp}.
	 * @param ctx the parse tree
	 */
	void exitTimerOp(ConductorParser.TimerOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ConductorParser#funcValue}.
	 * @param ctx the parse tree
	 */
	void enterFuncValue(ConductorParser.FuncValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ConductorParser#funcValue}.
	 * @param ctx the parse tree
	 */
	void exitFuncValue(ConductorParser.FuncValueContext ctx);
}
