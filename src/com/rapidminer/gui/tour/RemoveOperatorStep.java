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
 * This Subclass of {@link Step} will open a {@link BubbleWindow} which closes if an {@link Operator} of the given Class was removed.
 *
 * @author Philipp Kersting
 *
 */
public class RemoveOperatorStep extends Step {

	private String i18nKey;
	private Window owner;
	private Class<? extends Operator> operatorClass;
	private Alignment alignment;
	private Component component;
	private String buttonKey = null;
	private ProcessSetupListener listener = null;

	
	/**
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param owner the {@link Window} on which the {@link BubbleWindow} should be shown.
	 * @param i18nKey of the message which will be shown in the {@link BubbleWindow}.
	 * @param operatorClass Class or SuperClass of the {@link Operator} which should be deleted.
	 * @param componentKey key of the {@link Component} to which the {@link BubbleWindow} should point to. 
	 */
	public RemoveOperatorStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> operatorClass, String componentKey) {
		this(alignment, owner, i18nKey, operatorClass, (Component) null);
		this.buttonKey = componentKey;
	}
	
	/**
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param owner the {@link Window} on which the {@link BubbleWindow} should be shown.
	 * @param i18nKey of the message which will be shown in the {@link BubbleWindow}.
	 * @param operatorClass Class or SuperClass of the {@link Operator} which should be deleted.
	 * @param component {@link Component} to which the {@link BubbleWindow} should point to.
	 */
	public RemoveOperatorStep(Alignment alignment, Window owner, String i18nKey, Class<? extends Operator> operatorClass, Component component) {
		this.i18nKey = i18nKey;
		this.owner = owner;
		this.alignment = alignment;
		this.operatorClass = operatorClass;
		this.component = component;
	}

	@Override
	boolean createBubble() {
		if (component == null) {
			if (buttonKey == null)
				throw new IllegalArgumentException("Component is null. Please add any Component to attach to ");
			bubble = new BubbleWindow(owner, alignment, i18nKey, buttonKey);
		} else {
			bubble = new BubbleWindow(owner, alignment, i18nKey, component);
		}
		listener = new ProcessSetupListener() {

			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {
				if (RemoveOperatorStep.this.operatorClass.isInstance(operator)) {
					bubble.triggerFire();
					RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
				}

			}

			@Override
			public void operatorChanged(Operator operator) {
				// TODO Auto-generated method stub

			}

			@Override
			public void operatorAdded(Operator operator) {
				// TODO Auto-generated method stub

			}

			@Override
			public void executionOrderChanged(ExecutionUnit unit) {
				// TODO Auto-generated method stub

			}
		};
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(listener);
		return true;
	}

	@Override
	protected void stepCanceled() {
		if(listener != null)
		RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(listener);
	}

	@Override
	public Step[] getPreconditions() {
		return new Step[] {new PerspectivesStep(1)};
	}

}
