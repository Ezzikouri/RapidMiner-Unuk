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
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;

/**
 * 
 * @author Philipp Kersting
 *
 */

public class RenameOperatorStep extends OperatorStep {

	private Alignment alignment;
	private Window owner;
	private String i18nKey;
	private String targetName;
	private Component attachTo;
	private String attachToKey;
	
	public RenameOperatorStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, String targetName, Component attachTo) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.targetName = targetName;
		this.operator = operator;
		this.attachTo = attachTo;
	}
	public RenameOperatorStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator>  operator, String targetName, String attachToKey) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.targetName = targetName;
		this.operator = operator;
		this.attachTo = null;
		this.attachToKey = attachToKey;
	}



	@Override
	BubbleWindow createBubble() {
		bubble = new BubbleWindow(owner, alignment, i18nKey);
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(new ProcessSetupListener() {
			
			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void operatorChanged(Operator operator) {
				if (RenameOperatorStep.this.operator.isInstance(operator) && operator.getName().equals(targetName)){
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
		if (attachTo == null){
			attachTo = BubbleWindow.findButton(attachToKey, RapidMinerGUI.getMainFrame());
		}
		bubble.positionRelativeTo(attachTo);
		return bubble;
	}

}
