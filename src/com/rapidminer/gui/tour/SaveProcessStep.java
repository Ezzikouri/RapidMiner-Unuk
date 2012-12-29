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
 * This Subclass of {@link Step} will open a {@link BubbleWindow} which closes if the user saves the Process.
 * 
 * @author Philipp Kersting
 *
 */

public class SaveProcessStep extends Step {

	private String i18nKey;
	private String buttonKey;
	private Alignment alignment;
	private Window owner;
	private ProcessStorageListener listener = null;

	/**
	 * This Class will automatically point to the normal save-Button
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param owner the {@link Window} on which the {@link BubbleWindow} should be shown.
	 * @param i18nKey of the message which will be shown in the {@link BubbleWindow}.
	 */
	public SaveProcessStep(Alignment preferedAlignment, Window owner, String i18nKey){
		this(preferedAlignment, owner, i18nKey, "save");
	}
	
	/**
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param owner the {@link Window} on which the {@link BubbleWindow} should be shown.
	 * @param i18nKey of the message which will be shown in the {@link BubbleWindow}.
	 * @param buttonKey i18nKey of the Button to which the {@link BubbleWindow} should point to.
	 */
	public SaveProcessStep(Alignment preferedAlignment, Window owner, String i18nKey, String buttonKey){
		this.alignment = preferedAlignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.buttonKey = buttonKey;
	}
	
	@Override
	boolean createBubble() {
		if(buttonKey == null)
			throw new IllegalArgumentException("NO Buttonkey to attach to. Please enter a Buttonkey or call Constructor without Buttonkey");
		bubble = new BubbleWindow(owner, alignment, i18nKey, buttonKey, false);
		listener = new ProcessStorageListener() {
			
			@Override
			public void stored(Process process) {
				RapidMinerGUI.getMainFrame().getProcess().removeProcessStorageListener(this);
				bubble.triggerFire();
				
			}
			
			@Override
			public void opened(Process process) {
				
			}
		};
		RapidMinerGUI.getMainFrame().getProcess().addProcessStorageListener(listener);
		return true;
	}
	
	@Override
	protected void stepCanceled() {
		if(listener != null)
			RapidMinerGUI.getMainFrame().getProcess().removeProcessStorageListener(listener);
	}

	@Override
	public Step [] getPreconditions() {
		return new Step[] {};
	}

}
