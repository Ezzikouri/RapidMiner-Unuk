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
import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;

/**
 * 
 * 
 * @author Kersting and Thilo Kamradt
 *
 */

public class OpenSubprocessStep extends Step {

	private Alignment alignment;
	private Window owner;
	private String i18nKey;
	private Component attachTo;
	private String attachToKey = null;
	private Class<? extends OperatorChain> operatorClass;
	private ProcessEditor editor = null;

	public OpenSubprocessStep(Alignment alignment, Window owner, String i18nKey, String attachToKey, Class<? extends OperatorChain> operator) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.attachToKey = attachToKey;
		this.attachTo = null;
		this.operatorClass = operator;
	}

	public OpenSubprocessStep(Alignment alignment, Window owner, String i18nKey, Component attachTo, Class<? extends OperatorChain> operator) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.attachTo = attachTo;
		this.operatorClass = operator;
	}

	public OpenSubprocessStep(Alignment alignment, Window owner, String i18nKey, String attachToKey) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.attachToKey = attachToKey;
		this.attachTo = null;
		this.operatorClass = null;
	}

	public OpenSubprocessStep(Alignment alignment, Window owner, String i18nKey, Component attachTo) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.attachTo = attachTo;
		this.operatorClass = null;
	}

	public void setOperator(Class<? extends OperatorChain> operator) {
		this.operatorClass = operator;
	}

	public Class<? extends OperatorChain> getOperator() {
		return operatorClass;
	}

	@Override
	BubbleWindow createBubble() {
		if (attachTo == null) {
			if(attachToKey == null)
				throw new IllegalArgumentException("Component attachTo and Buttenkey attachToKey are null. Please add any Component to attach to ");
			bubble = new BubbleWindow(owner, alignment, i18nKey, attachToKey, false);
		} else {
			bubble = new BubbleWindow(owner, alignment, i18nKey, attachTo);
		}
		editor = new ProcessEditor() {

			@Override
			public void setSelection(List<Operator> selection) {
				if (RapidMinerGUI.getMainFrame().getProcessPanel().getProcessRenderer().getDisplayedChain().getClass().equals(OpenSubprocessStep.this.operatorClass)
						|| OpenSubprocessStep.this.operatorClass == null) {
					bubble.triggerFire();
					RapidMinerGUI.getMainFrame().removeProcessEditor(this);
				}
			}

			@Override
			public void processUpdated(Process process) {

			}

			@Override
			public void processChanged(Process process) {
			}
		};
		RapidMinerGUI.getMainFrame().addProcessEditor(editor);
		return bubble;
	}

	@Override
	protected void stepCanceled () {
		if(editor == null)
			RapidMinerGUI.getMainFrame().removeProcessEditor(editor);
	}
}
