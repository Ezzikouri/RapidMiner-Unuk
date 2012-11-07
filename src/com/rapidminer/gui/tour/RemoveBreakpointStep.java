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

import java.awt.Component;
import java.awt.Window;

import com.rapidminer.ProcessSetupListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.gui.tour.AddBreakpointStep.Position;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;

/**
 * 
 * @author Kersting
 *
 */

public class RemoveBreakpointStep extends OperatorStep {
	
	Alignment alignment;
	Window owner;
	String i18nKey;
	Component attachTo;
	

	public RemoveBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> operator, Component attachTo) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.attachTo = attachTo;
	}
	
	public RemoveBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> operator, String attachToKey) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.attachTo = BubbleWindow.findButton(attachToKey, RapidMinerGUI.getMainFrame());
	}
	
	/** the Breakpoint after will be chosen as default*/
	public RemoveBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> operator) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.attachTo = null;
	}
	

	@Override
	BubbleWindow createBubble() {
				if (attachTo == null){
						bubble = new BubbleWindow(owner, alignment, i18nKey,"breakpoint_after");
				} else {
					bubble = new BubbleWindow(owner, alignment, i18nKey, attachTo);
				}
		
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(new ProcessSetupListener() {
			
			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void operatorChanged(Operator operator) {
				if (RemoveBreakpointStep.this.operator.isInstance(operator) && !operator.hasBreakpoint()){
					bubble.triggerFire();
					RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
				}
				
			}
			
			@Override
			public void operatorAdded(Operator operator) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void executionOrderChanged(ExecutionUnit unit) {
				// TODO Auto-generated method stub
				
			}
		});
		return bubble;
	}
}
