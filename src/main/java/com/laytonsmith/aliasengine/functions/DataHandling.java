/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.Constructs.Variable;
import com.laytonsmith.aliasengine.LoopBreakException;
import com.laytonsmith.aliasengine.LoopContinueException;
import com.laytonsmith.aliasengine.RunnableAlias;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.Static;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class DataHandling {
    public static String docs(){
        return "";
    }
    @api public static class array implements Function{

        public String getName() {
            return "array";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CArray(line_num, args);
        }

        public String docs() {
            return "array {[var1, [var2...]]} Creates an array of values.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class assign implements Function{
        IVariableList varList;
        public String getName() {
            return "assign";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof IVariable){
                IVariable v = new IVariable(((IVariable)args[0]).getName(), args[1], line_num);
                varList.set(v);
                return v;
            }
            throw new ConfigRuntimeException("assign only accepts an ivariable as the first argument");
        }

        public String docs() {
            return "ivariable {ivar, mixed} Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it. Returns the variable that was assigned.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class _for implements Function{
        IVariableList varList;
        public String getName() {
            return "for";
        }

        public Integer[] numArgs() {
            return new Integer[]{4};
        }
        public Construct execs(int line_num, Player p, Script parent, GenericTreeNode<Construct> assign, 
                GenericTreeNode<Construct> condition, GenericTreeNode<Construct> expression, 
                GenericTreeNode<Construct> runnable, List<Variable> vars) throws CancelCommandException{
            Construct counter = parent.eval(assign, p, vars);
            if(!(counter instanceof IVariable)){
                throw new ConfigRuntimeException("First parameter of for must be an ivariable");
            }
            int _continue = 0;
            while(true){
                Construct cond = parent.eval(condition, p, vars);
                if(!(cond instanceof CBoolean)){
                    throw new ConfigRuntimeException("Second parameter of for must return a boolean");
                }
                CBoolean bcond = ((CBoolean) cond);
                if(bcond.getBoolean() == false){
                    break;
                }
                if(_continue > 1){
                    --_continue;                    
                    parent.eval(expression, p, vars);
                    continue;
                }
                try{
                    parent.eval(runnable, p, vars);
                } catch(LoopBreakException e){
                    int num = e.getTimes();
                    if(num > 1){
                        e.setTimes(--num);
                        throw e;
                    }
                } catch(LoopContinueException e){
                    _continue = e.getTimes() - 1;                    
                    continue;
                }
                parent.eval(expression, p, vars);
            }
            return new CVoid(line_num);
        }
        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return null;
        }

        public String docs() {
            return "void {assign, condition, expression1, expression2} Acts as a typical for loop. The assignment is first run. Then, a"
                    + " condition is checked. If that condition is checked and returns true, expression2 is run. After that, expression1 is run. In java"
                    + " syntax, this would be: for(assign; condition; expression1){expression2}. assign must be an ivariable, either a "
                    + "pre defined one, or the results of the assign() function. condition must be a boolean.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        //Doesn't matter, run out of state
        public Boolean runAsync() {
            return null;
        }
    }
    
    @api public static class foreach implements Function{
        IVariableList varList;
        public String getName() {
            return "foreach";
        }

        public Integer[] numArgs() {
            return new Integer[]{3};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Construct execs(int line_num, Player p, Script that, GenericTreeNode<Construct> array, 
                GenericTreeNode<Construct> ivar, GenericTreeNode<Construct> code, List<Variable> vars) throws CancelCommandException{
            
            Construct arr = that.eval(array, p, vars);
            if(arr instanceof IVariable){
                arr = varList.get(((IVariable)arr).getName()).ival();
            }
            Construct iv = that.eval(ivar, p, vars);
            
            if(arr instanceof CArray){
                if(iv instanceof IVariable){
                    CArray one = (CArray)arr;
                    IVariable two = (IVariable)iv;
                    for(int i = 0; i < one.size(); i++){
                        varList.set(new IVariable(two.getName(), one.get(i), line_num));
                        try{
                        that.eval(code, p, vars);
                        } catch(LoopBreakException e){
                            int num = e.getTimes();
                            if(num > 1){
                                e.setTimes(--num);
                                throw e;
                            }
                        } catch(LoopContinueException e){
                            i += e.getTimes() - 1;
                            continue;
                        }
                    }
                } else {
                    throw new CancelCommandException("Parameter 2 of foreach must be an ivariable");
                }
            } else {
                throw new CancelCommandException("Parameter 1 of foreach must be an array");
            }
            
            return new CVoid(line_num);
        }

        public String docs() {
            return "void {array, ivar, code} Walks through array, setting ivar equal to each element in the array, then running code.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        public String since() {
            return "3.0.1";
        }
        //Doesn't matter, runs out of state anyways
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class _break implements Function{

        public String getName() {
            return "break";
        }

        public Integer[] numArgs() {
            return new Integer[]{0,1};
        }

        public String docs() {
            return "nothing {[int]} Stops the current loop. If int is specified, and is greater than 1, the break travels that many loops up. So, if you had"
                    + " a loop embedded in a loop, and you wanted to break in both loops, you would call break(2). If this function is called outside a loop"
                    + " (or the number specified would cause the break to travel up further than any loops are defined), the function will fail. If no"
                    + " argument is specified, it is the same as calling break(1).";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int num = 1;
            if(args.length == 1){
                num = (int)Static.getInt(args[0]);
            }
            throw new LoopBreakException(num);
        }
        
    }
    
    @api public static class _continue implements Function{

        public String getName() {
            return "continue";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "void {[int]} Skips the rest of the code in this loop, and starts the loop over, with it continuing at the next index. If this function"
                    + " is called outside of a loop, the command will fail. If int is set, it will skip 'int' repetitions. If no argument is specified,"
                    + " 1 is used.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.1.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            int num = 1;
            if(args.length == 1){
                num = (int)Static.getInt(args[0]);
            }
            throw new LoopContinueException(num);
        }
        
    }
    
    
}