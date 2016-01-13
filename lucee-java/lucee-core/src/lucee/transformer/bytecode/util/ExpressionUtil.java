/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.transformer.bytecode.util;

import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.expression.ExprString;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;

import org.kacperus.cf.coverage.TemplateCoverageTool;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class ExpressionUtil {
	
	public static final Method START = new Method(
			"exeLogStart",
			Types.VOID,
			new Type[]{Types.INT_VALUE,Types.STRING});
	public static final Method END = new Method(
			"exeLogEnd",
			Types.VOID,
			new Type[]{Types.INT_VALUE,Types.STRING});

	public static final Method CURRENT_LINE = new Method(
			"currentLine",
			Types.VOID,
			new Type[]{Types.INT_VALUE});
	

	private static Map<String,String> last=new HashMap<String,String>();

	public static void writeOutExpressionArray(BytecodeContext bc, Type arrayType, Expression[] array) throws BytecodeException {
    	GeneratorAdapter adapter = bc.getAdapter();
        adapter.push(array.length);
        adapter.newArray(arrayType);
        for (int i = 0; i < array.length; i++) {
            adapter.dup();
            adapter.push(i);
            array[i].writeOut(bc, Expression.MODE_REF);
            adapter.visitInsn(Opcodes.AASTORE);
        }
    }

    /**
     * visit line number
     * @param adapter
     * @param line
     * @param silent id silent this is ignored for log
     */
    public static synchronized void visitLine(BytecodeContext bc, Position pos) {
    	if(pos!=null){
    		visitLine(bc, pos.line);
    	}
   	}

	public static synchronized void markLine(BytecodeContext bc, Position pos) {
		if (pos!=null) {
			markLine(bc, pos.line);
		}
	}

    private static synchronized void visitLine(BytecodeContext bc, int line) {
    	if(line>0){
    		/*Type[] methodTypes = bc.getMethod().getArgumentTypes();
			if(methodTypes!=null && methodTypes.length>0 && methodTypes[0].equals(Types.PAGE_CONTEXT)) {
    			GeneratorAdapter adapter = bc.getAdapter();
    	    	adapter.loadArg(0);
    	    	adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
    	        adapter.push(line);
    		    adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL,CURRENT_LINE );
			}*/
    		
    		if(!(""+line).equals(last.get(bc.getClassName()+":"+bc.getId()))){
	    		
    			
    			
    			bc.visitLineNumber(line);
	    		last.put(bc.getClassName()+":"+bc.getId(),""+line);
	    		last.put(bc.getClassName(),""+line);
	    	}
    	}
   }

	private static synchronized void markLine(BytecodeContext bc, int line) {
		if (line > 0) {
			TemplateCoverageTool.getInstance().markLineAsCovered(
					bc.getPageSource().getFullRealpath(),
					line
			);

			Type[] methodTypes = bc.getMethod().getArgumentTypes();
			if(methodTypes!=null && methodTypes.length>0 && methodTypes[0].equals(Types.PAGE_CONTEXT)) {
				GeneratorAdapter adapter = bc.getAdapter();
				adapter.loadArg(0);
				adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
				adapter.push(line);
				adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL,CURRENT_LINE );
			}
		}
	}

	public static synchronized void lastLine(BytecodeContext bc) {
    	int line = Caster.toIntValue(last.get(bc.getClassName()),-1);
    	visitLine(bc, line);
    }

	/**
	 * write out expression without LNT
	 * @param value
	 * @param bc
	 * @param mode
	 * @throws BytecodeException
	 */
	public static void writeOutSilent(Expression value, BytecodeContext bc, int mode) throws BytecodeException {
		Position start = value.getStart();
		Position end = value.getEnd();
		value.setStart(null);
		value.setEnd(null);
		value.writeOut(bc, mode);
		value.setStart(start);
		value.setEnd(end);
	}
	public static void writeOut(Expression value, BytecodeContext bc, int mode) throws BytecodeException {
		value.writeOut(bc, mode);
	}

	public static void writeOut(final Statement s, BytecodeContext bc) throws BytecodeException {
		if(ExpressionUtil.doLog(bc)) {
    		final String id=BodyBase.id();
    		TryFinallyVisitor tfv=new TryFinallyVisitor(new OnFinally() {
    			public void _writeOut(BytecodeContext bc) {
    				ExpressionUtil.callEndLog(bc, s,id);
    			}
    		},null);
    		
    		tfv.visitTryBegin(bc);
    			ExpressionUtil.callStartLog(bc, s,id);
    			s.writeOut(bc);
    		tfv.visitTryEnd(bc)	;
    	}
    	else s.writeOut(bc);
	}

	public static short toShortType(ExprString expr,boolean alsoAlias, short defaultValue) {
		if(expr instanceof LitString){
			return CFTypes.toShort(((LitString)expr).getString(),alsoAlias,defaultValue);
		}
		return defaultValue;
	}

	public static void callStartLog(BytecodeContext bc, Statement s, String id) {
		call_Log(bc, START, s.getStart(),id);
	}
	public static void callEndLog(BytecodeContext bc, Statement s, String id) {
		call_Log(bc, END, s.getEnd(),id);
	}

	private static void call_Log(BytecodeContext bc, Method method, Position pos, String id) {
    	if(!bc.writeLog() || pos==null || (StringUtil.indexOfIgnoreCase(bc.getMethod().getName(),"call")==-1))return;
    	try{
	    	GeneratorAdapter adapter = bc.getAdapter();
	    	adapter.loadArg(0);
	        //adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
	        adapter.push(pos.pos);
	        adapter.push(id);
		    adapter.invokeVirtual(Types.PAGE_CONTEXT, method);
		}
		catch(Throwable t) {
			t.printStackTrace();
		}		
	}

	public static boolean doLog(BytecodeContext bc) {
		return bc.writeLog() && StringUtil.indexOfIgnoreCase(bc.getMethod().getName(),"call")!=-1;
	}
}
