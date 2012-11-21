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

import java.awt.Window;

import com.rapidminer.ProcessSetupListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * 
 * @author Philipp Kersting
 *
 */

public class ChangeParameterStep extends OperatorStep {

	private String i18nKey;
	private String targetDockKey;
	private String parameter;
	private String targetValue;
	private Alignment alignment;
	private Window owner;

	

	public ChangeParameterStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, String parameter, String targetDockKey, String targetValue) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.targetDockKey = targetDockKey;
		this.parameter = parameter;
		this.targetValue = targetValue;
		this.operator = operator;
	}

	@Override
	BubbleWindow createBubble() {
		bubble = new BubbleWindow(owner, alignment, i18nKey, BubbleWindow.getDockableByKey(targetDockKey));
		ProcessSetupListener l = new ProcessSetupListener() {
			
			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {
			}
			
			@Override
			public void operatorChanged(Operator operator) {
				if (ChangeParameterStep.this.operator.isInstance(operator)){
					try {
						if (operator.getParameterAsString(parameter).equals(targetValue)){
							bubble.triggerFire();
							RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
						}
					} catch (UndefinedParameterError e) {
						e.printStackTrace();
					}
				} 
			}
			
			@Override
			public void operatorAdded(Operator operator) {
			}
			
			@Override
			public void executionOrderChanged(ExecutionUnit unit) {
			}
		};
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(l);
		return bubble;
	}


}

