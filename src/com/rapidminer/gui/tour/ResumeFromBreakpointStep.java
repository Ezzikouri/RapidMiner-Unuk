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

import com.rapidminer.BreakpointListener;
import com.rapidminer.Process;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.gui.tools.components.BubbleWindow.BubbleListener;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;

/**
 * 
 * @author Philipp Kersting
 *
 */

public class ResumeFromBreakpointStep extends OperatorStep {

	public enum Position {
		BEFORE, AFTER, DONT_CARE
	}

	private String i18nKey;
	private Alignment alignment;
	private Window owner;
	private Position position;
	private Component component;
	
	public ResumeFromBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, Position position, Component attachTo){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.position = position;
		this.component = attachTo;
	}
	
	public ResumeFromBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, Position position, String attachTo){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.position = position;
		this.component = BubbleWindow.findButton(attachTo, RapidMinerGUI.getMainFrame());
	}
	
	public ResumeFromBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, Component attachTo){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.position = Position.DONT_CARE;
		this.component = attachTo;
	}

	
	public ResumeFromBreakpointStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, String attachTo){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.operator = operator;
		this.position = Position.DONT_CARE;
		this.component = BubbleWindow.findButton(attachTo, RapidMinerGUI.getMainFrame());
	}
	
	

	@Override
	BubbleWindow createBubble() {
		bubble = new BubbleWindow(owner, alignment, i18nKey);
		bubble.positionRelativeTo(component);
		RapidMinerGUI.getMainFrame().getProcess().addBreakpointListener(new BreakpointListener() {
			
			@Override
			public void resume() {
				if(operator.isInstance(RapidMinerGUI.getMainFrame().getProcess().getCurrentOperator())){
					if (position == Position.BEFORE && RapidMinerGUI.getMainFrame().getProcess().getCurrentOperator().hasBreakpoint(BreakpointListener.BREAKPOINT_BEFORE)){
						bubble.triggerFire();
						RapidMinerGUI.getMainFrame().getProcess().removeBreakpointListener(this);
					}else if(position == Position.AFTER && RapidMinerGUI.getMainFrame().getProcess().getCurrentOperator().hasBreakpoint(BreakpointListener.BREAKPOINT_AFTER)){
						bubble.triggerFire();
						RapidMinerGUI.getMainFrame().getProcess().removeBreakpointListener(this);
					}else if (position == Position.DONT_CARE){
						bubble.triggerFire();
						RapidMinerGUI.getMainFrame().getProcess().removeBreakpointListener(this);
					}
				}
				
			}
			
			@Override
			public void breakpointReached(Process process, Operator op, IOContainer iocontainer, int location) {
				if (operator.isInstance(op) && ((location == 1 && position == Position.AFTER) || (location == 0 && position == Position.BEFORE)||(position == Position.DONT_CARE))){
					bubble.setVisible(true);
				}
				
			}
		});
		return bubble;
	}
	
	@Override
	public void start(){
		bubble = createBubble();
		bubble.addBubbleListener(new BubbleListener() {
			
			@Override
			public void bubbleClosed(BubbleWindow bw) {
				bw.removeBubbleListener(this);
				
			}
			
			@Override
			public void actionPerformed(BubbleWindow bw) {
				if (next!=null){
					next.start();
				}
				bw.removeBubbleListener(this);
			}
		});
		bubble.setVisible(false);
	}
	
}
