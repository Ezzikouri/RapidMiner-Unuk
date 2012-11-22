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

import javax.swing.AbstractButton;

import com.rapidminer.Process;
import com.rapidminer.ProcessStorageListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;

/**
 * 
 * @author Philipp Kersting and Thilo Kamradt
 *
 */

public class OpenProcessStep extends Step {

	private Alignment alignment;
	private Window owner;
	private String i18nKey;
	private Component attachTo;
	private String attachToKey;
	private ProcessStorageListener listener = null;

	
	public OpenProcessStep(Alignment alignment, Window owner, String i18nKey, Component attachTo) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.attachTo = attachTo;
		this.attachToKey = null;
	}

	public OpenProcessStep(Alignment alignment, Window owner, String i18nKey, String attachToKey) {
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.attachTo = null;
		this.attachToKey = attachToKey;
	}

	@Override
	BubbleWindow createBubble() {
		if (attachTo == null) {
			if (attachToKey == null)
				throw new IllegalArgumentException("no component to attach !");
			bubble = new BubbleWindow(owner, alignment, i18nKey, attachToKey, false);
		} else {
			bubble = new BubbleWindow(owner, alignment, i18nKey,(AbstractButton) attachTo, false);
		}
		listener = new ProcessStorageListener() {
			@Override
			public void stored(Process process) { }

			@Override
			public void opened(Process process) {				
				bubble.triggerFire();
				RapidMinerGUI.getMainFrame().removeProcessStorageListener(listener);
			}
		};
		RapidMinerGUI.getMainFrame().addProcessStorageListener(listener);
		return bubble;
	}
	
	@Override
	protected void stepCanceled() {
		if(listener == null)
			RapidMinerGUI.getMainFrame().getProcess().removeProcessStorageListener(listener);
	}
}
