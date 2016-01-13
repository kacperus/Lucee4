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
package lucee.transformer.bytecode.expression.var;

import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.cast.CastOther;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.ExpressionUtil;

import org.objectweb.asm.Type;


public class Argument extends ExpressionBase {
		
		private Expression raw;
		private String type;

		public Argument(Expression value, String type) {
			super(value.getStart(),value.getEnd());
			this.raw=value;//Cast.toExpression(value,type);
			this.type=type;
		}

		/**
		 * @return the value
		 */
		public Expression getValue() {
			return CastOther.toExpression(raw,type);
		}
		
		/**
		 * return the uncasted value
		 * @return
		 */
		public Expression getRawValue() {
			return raw;
		}
		
		public void setValue(Expression value,String type) {
			this.raw = value;
			this.type=type;
			
		}

		/**
		 *
		 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
		 */
		public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
			return getValue().writeOut(bc, mode);
		}
		
		public Type writeOutValue(BytecodeContext bc, int mode) throws BytecodeException {
			ExpressionUtil.visitLine(bc, getStart());
			ExpressionUtil.markLine(bc, getStart());
			Type t = getValue().writeOut(bc, mode);
			ExpressionUtil.visitLine(bc, getEnd());
			return t;
		}

		/**
		 * @return the type
		 */
		public String getStringType() {
			return type;
		}
	}