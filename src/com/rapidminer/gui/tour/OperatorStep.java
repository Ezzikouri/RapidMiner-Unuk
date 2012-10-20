/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.tour;

import com.rapidminer.operator.Operator;


/**
 * A {@link Step} for all steps, who are maybe expecting an interaction with a certain kind of operator and not just any operator.
 * 
 * @author Philipp Kersting
 *
 */

public abstract class OperatorStep extends Step {

	protected Class<? extends Operator> operator;

	
	public Class getOperator() {
		return operator;
	}

	public void setOperator(Class<?extends Operator> operator) {
		this.operator = operator;
	}


	

}
