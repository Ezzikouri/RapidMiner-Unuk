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

import com.rapidminer.BreakpointListener;
import com.rapidminer.ProcessSetupListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;

/**
 * 
 * @author Philipp Kersting
 *
 */

public class AddBreakpointStep extends OperatorStep {

	public enum Position {
		BEFORE, AFTER, DONT_CARE
	}

	private String i18nKey;
	private Alignment alignment;
	private Window owner;
	private Position position;
	
	public AddBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, Position position){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.position = position;
		this.operator = operator;
	}
	

	@Override
	BubbleWindow createBubble() {
			bubble = new BubbleWindow(owner, alignment, i18nKey,"breakpoint_after");
		
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(new ProcessSetupListener() {
			
			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void operatorChanged(Operator operator) {
				if (AddBreakpointStep.this.operator.isInstance(operator) && operator.hasBreakpoint()){
					if (position == Position.BEFORE && operator.hasBreakpoint(BreakpointListener.BREAKPOINT_BEFORE)){
						bubble.triggerFire();
						RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
					}else if(position == Position.AFTER && operator.hasBreakpoint(BreakpointListener.BREAKPOINT_AFTER)){
						bubble.triggerFire();
						RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
					}else if (position == Position.DONT_CARE){
						bubble.triggerFire();
						RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
					}
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
