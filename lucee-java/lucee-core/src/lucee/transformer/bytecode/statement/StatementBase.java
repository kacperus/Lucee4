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
package lucee.transformer.bytecode.statement;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.util.ExpressionUtil;

/**
 * A single Statement
 */
public abstract class StatementBase implements Statement {

	private Position start;
	private Position end;
	private Statement parent;
	private int hasReturnChild=-1;
	
	/**
     * constructor of the class
     * @param line
     */
    public StatementBase(Position start, Position end) {
        this.start=start;
        this.end=end;
    }
    
    /**
	 * @see lucee.transformer.bytecode.Statement#getParent()
	 */
	public Statement getParent() {
		return parent;
	}


	/**
	 * @see lucee.transformer.bytecode.Statement#setParent(lucee.transformer.bytecode.Statement)
	 */
	public void setParent(Statement parent) {
		this.parent=parent;
		if(hasReturnChild!=-1 && parent!=null)
			parent.setHasFlowController(hasReturnChild==1);
	}


	/**
     * write out the stament to adapter
     * @param adapter
     * @throws TemplateException
     */
    public final void writeOut(BytecodeContext bc) throws BytecodeException {
    	ExpressionUtil.visitLine(bc, start);
    	ExpressionUtil.markLine(bc, start);
        _writeOut(bc);
    	ExpressionUtil.visitLine(bc, end);
    	
    }
    

    /**
     * write out the stament to the adater
     * @param adapter
     * @throws BytecodeException 
     */
    public abstract void _writeOut(BytecodeContext bc) throws BytecodeException;



    /**
     * sets the line value.
     * @param line The line to set.
     */
    public void setStart(Position start) {
        this.start = start;
    }

    /**
     * sets the line value.
     * @param line The line to set.
     */
    public void setEnd(Position end) {
        this.end = end;
    }
    
    @Override
	public Position getStart() {
		return start;
	}
	
	@Override
	public Position getEnd() {
		return end;
	}
    
    /**
	 * @see lucee.transformer.bytecode.Statement#getDescendantCount()
	 */
	public final int getDescendantCount() {
		return 0;
	}

    /**
	 *
	 * @see lucee.transformer.bytecode.Statement#hasFlowController()
	 */
	public boolean hasFlowController() {
		return hasReturnChild==1;
	}

	/**
	 * @param hasReturnChild the hasReturnChild to set
	 */
	public void setHasFlowController(boolean hasReturnChild) {
		if(parent!=null)parent.setHasFlowController(hasReturnChild);
		this.hasReturnChild = hasReturnChild?1:0;
	}
}
