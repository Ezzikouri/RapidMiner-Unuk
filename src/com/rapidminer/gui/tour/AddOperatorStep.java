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
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.ProcessSetupListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;

/**
 * 
 * @author Philipp Kersting
 *
 */

public class AddOperatorStep extends Step {

	public interface AddOperatorStepListener {

		public void operatorAvailable(Operator op);
	}

	private String i18nKey;
	private Alignment alignment;
	private Window owner;
	private Class type;
	private String targetDockKey;
	private Operator operator = null;
	private boolean checkForChain = true;
	private Class<? extends OperatorChain> targetEnclosingOperatorChain = OperatorChain.class;
	private List<AddOperatorStepListener> listeners = new LinkedList<AddOperatorStepListener>();

	public AddOperatorStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> type, String targetDockKey) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.type = type;
		this.targetDockKey = targetDockKey;
	}

	public AddOperatorStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> type, String targetDockKey, boolean checkForEnclosingOperatorChain) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.type = type;
		this.targetDockKey = targetDockKey;
		this.checkForChain = checkForEnclosingOperatorChain;
	}

	public AddOperatorStep(String i18nKey, Alignment alignment, Window owner, Class type, String targetDockKey, Class<? extends OperatorChain> targetEnclosingOperatorChain) {
		this.i18nKey = i18nKey;
		this.alignment = alignment;
		this.owner = owner;
		this.type = type;
		this.targetDockKey = targetDockKey;
		this.targetEnclosingOperatorChain = targetEnclosingOperatorChain;
	}

	@Override
	BubbleWindow createBubble() {
		bubble = new BubbleWindow(owner, alignment, i18nKey, BubbleWindow.getDockableByKey(targetDockKey));
		ProcessSetupListener l = new ProcessSetupListener() {

			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {}

			@Override
			public void operatorChanged(Operator operator) {
				if (type.isInstance(operator)) {
					if (checkForChain) {
						if ((targetEnclosingOperatorChain == null || targetEnclosingOperatorChain.isInstance(operator.getExecutionUnit().getEnclosingOperator())) && (operator.getOutputPorts().getNumberOfConnectedPorts() != 0)) {

							bubble.triggerFire();
							RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
							AddOperatorStep.this.operator = operator;
							List<AddOperatorStepListener> cache = new LinkedList<AddOperatorStepListener>(listeners);
							for (AddOperatorStepListener listener : cache) {
								listener.operatorAvailable(operator);
							}
						}
					} else {
						if (operator.getOutputPorts().getNumberOfConnectedPorts() != 0) {

							bubble.triggerFire();
							RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
							AddOperatorStep.this.operator = operator;
							List<AddOperatorStepListener> cache = new LinkedList<AddOperatorStepListener>(listeners);
							for (AddOperatorStepListener listener : cache) {
								listener.operatorAvailable(operator);
							}
						}
					}
				}
			}

			@Override
			public void operatorAdded(Operator operator) {}

			@Override
			public void executionOrderChanged(ExecutionUnit unit) {}
		};
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(l);
		return bubble;
	}

	public Operator getOperator() {
		return this.operator;
	}

	public Class<? extends OperatorChain> getTargetEnclosingOperatorChain() {
		return targetEnclosingOperatorChain;
	}

	public void setTargetEnclosingOperatorChain(Class<? extends OperatorChain> targetEnclosingOperatorChain) {
		this.targetEnclosingOperatorChain = targetEnclosingOperatorChain;
	}

	public void addListener(AddOperatorStepListener l) {
		listeners.add(l);
	}

	public void removeListener(AddOperatorStepListener l) {
		listeners.remove(l);
	}
}
