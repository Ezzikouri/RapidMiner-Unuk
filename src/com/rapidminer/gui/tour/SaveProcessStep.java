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

import com.rapidminer.Process;
import com.rapidminer.ProcessStorageListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;

/**
 * 
 * @author Philipp Kersting
 *
 */

public class SaveProcessStep extends Step {

	private String i18nKey;
	private String buttonKey;
	private Alignment alignment;
	private Window owner;

	public SaveProcessStep(Alignment alignment, Window owner, String i18nKey, String buttonKey){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.buttonKey = buttonKey;
	}
	
	public SaveProcessStep(Alignment alignment, Window owner, String i18nKey){
		this.alignment = alignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.buttonKey = "save";
	}
	
	

	@Override
	BubbleWindow createBubble() {
		bubble = new BubbleWindow(owner, alignment, i18nKey);
		bubble.positionRelativeTo(BubbleWindow.findButton(buttonKey, RapidMinerGUI.getMainFrame()));
		RapidMinerGUI.getMainFrame().getProcess().addProcessStorageListener(new ProcessStorageListener() {
			
			@Override
			public void stored(Process process) {
				RapidMinerGUI.getMainFrame().getProcess().removeProcessStorageListener(this);
				bubble.triggerFire();
				
			}
			
			@Override
			public void opened(Process process) {
				
			}
		});
		return bubble;
	}

}
